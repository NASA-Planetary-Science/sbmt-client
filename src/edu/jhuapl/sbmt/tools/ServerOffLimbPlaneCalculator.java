package edu.jhuapl.sbmt.tools;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;

import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageToPolyDataFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.model.ImageKey;
import edu.jhuapl.sbmt.model.bennu.OcamsFlightImage;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

public class ServerOffLimbPlaneCalculator
{
    private int szW, szH;
    private Vector3D scPos;
    private Rotation lookRot, upRot;
    private vtkPolyData imagePolyData;
    private double maxRayDepth;
    private Stopwatch sw;
    private double ffacx;
    private double ffacy;
    private double fovx;
    private double fovy;

    public ServerOffLimbPlaneCalculator(PerspectiveImage img)
    {
        sw = new Stopwatch();
        sw.start();
        setupParameters(img);
    }

    private void setupParameters(PerspectiveImage img)
    {
        // Step (1): Discretize the view frustum into macro-pixels, from which geometry will later be derived

        // (1a) get camera parameters
        double[] spacecraftPosition=new double[3];
        double[] focalPoint=new double[3];
        double[] upVector=new double[3];
        img.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
        fovx=img.getHorizontalFovAngle();//img.getMaxFovAngle();
        fovy=img.getVerticalFovAngle();

        // (1b) guess at a resolution for the macro-pixels; these will be used to create quadrilateral cells (i.e. what will eventually be the off-limb geometry) in the camera view-plane
        int res=(int)Math.sqrt(img.getFootprint(img.getDefaultSlice()).GetNumberOfPoints());    // for now just grab the number of points in the on-body footprint; assuming img is roughly planar we apply sqrt to get an approximate number of points on a "side" of the on-body geometry, within the rectangular frustuma
        int[] resolution=new int[]{res,res};    // cast to int and store s- and t- resolutions; NOTE: s and t can be thought of as respectively "horizontal" and "vertical" when viewing the image in the "Properties..." pane (t-hat cross s-hat = look direction in a righthanded coordinate system)
        // allow for later possibility of unequal macro-pixel resolutions; take the highest resolution
//        int szMax=Math.max(resolution[0], resolution[1]);
        szW=resolution[0];//szMax;//(int)(aspect*szMax);
        szH=resolution[1];//szMax;

        // Step (2): Shoot rays from the camera position toward each macro-pixel & record which ones don't hit the body (these will comprise the off-limb geometry)

        // (2a) determine ray-cast depth; currently implemented as camera-to-origin distance plus body bounding-box diagonal length -- that way rays will always extend from the camera position past the entire body
        scPos=new Vector3D(spacecraftPosition);
        int currentSlice = img.getCurrentSlice();
        if (img.minFrustumDepth[currentSlice]==0)
            img.minFrustumDepth[currentSlice]=0;
        if (img.maxFrustumDepth[currentSlice]==0)
            img.maxFrustumDepth[currentSlice]=scPos.getNorm()+img.getSmallBodyModel().getBoundingBoxDiagonalLength();
        maxRayDepth=(img.minFrustumDepth[currentSlice]+img.maxFrustumDepth[currentSlice]);
        ffacx=maxRayDepth*Math.tan(Math.toRadians(fovx/2));    // img is the scaling factor in the plane perpendicular to the boresight, which maps unit vectors from the camera position onto the chosen max depth, in frustum coordinates, thus forming a ray
        ffacy=maxRayDepth*Math.tan(Math.toRadians(fovy/2));

        // (2b) figure out rotations lookRot and upRot, which transform frustum (s,t) coordinates into a direction in 3D space, pointing in the implied direction from the 3D camera position:
        //    (A) lookRot * negative z-hat = look-hat               [ transform negative z-hat from 3D space into the look direction ; img is the boresight of the camera ]
        //    (B) upRot * (lookRot * y-hat) = up-hat = t-hat        [ first transform y-hat from 3D space into the "boresight frame" (it was perpendicular to z-hat before, so will now be perpendicular to the boresight direction) and second rotate that vector around the boresight axis to align with camera up, i.e. t-hat
        // NOTE: t-hat cross s-hat = look-hat, thus completing the frustum coordinate system
        // NOTE: given two scalar values -1<=s<=1 and -1<=t<=1, the corresponding ray extends (in 3D space) from the camera position to upRot*lookRot*
        Vector3D lookVec=new Vector3D(focalPoint).subtract(new Vector3D(spacecraftPosition));
        Vector3D upVec=new Vector3D(upVector);
        lookRot=new Rotation(Vector3D.MINUS_K, lookVec.normalize());
        upRot=new Rotation(lookRot.applyTo(Vector3D.PLUS_J), upVec.normalize());
    }

    public void generateOffLimbPlane(PerspectiveImage img, double offLimbFootprintDepth)
    {
    	// (2c) use a vtkImageCanvasSource2D to represent the macro-pixels, with an unsigned char color type "true = (0, 0, 0) = ray hits surface" and "false = (255, 255, 255) = ray misses surface"... img might seem backwards but 0-values can be thought of as forming the "shadow" of the body against the sky as viewed by the camera
        // NOTE: img could be done more straightforwardly (and possibly more efficiently) just by using a java boolean[][] array... I think the present implementation is a hangover from prior experimentation with a vtkPolyDataSilhouette filter
        vtkImageCanvasSource2D imageSource=new vtkImageCanvasSource2D();
        imageSource.SetScalarTypeToUnsignedChar();
        imageSource.SetNumberOfScalarComponents(3);
        imageSource.SetExtent(-szW/2, szW/2, -szH/2, szH/2, 0, 0);
        //first check if the boundary cells all intersect the body - if so, there is no offlimb, so the rest can be skipped
        boolean isEdgeOffBody = true; //false;
        int[] edgeValues = new int[] {-szW/2, szW/2};
        int[] edgeValues2 = new int[] {-szH/2, szH/2};
        for (int i : edgeValues)
        {
            if (isEdgeOffBody == true) break;
            for (int j : edgeValues2)
            {
                double s=(double)i/((double)szW/2)*ffacx;
                double t=(double)j/((double)szW/2)*ffacy;
                Vector3D ray=new Vector3D(s,t,-maxRayDepth);  // ray construction starts from s,t coordinates each on the interval [-1 1]
                ray=upRot.applyTo(lookRot.applyTo(ray));//upRot.applyInverseTo(lookRot.applyInverseTo(ray.normalize()));
                Vector3D rayEnd=ray.add(scPos);
                //
                vtkIdList ids=new vtkIdList();
                img.getSmallBodyModel().getCellLocator().FindCellsAlongLine(scPos.toArray(), rayEnd.toArray(), 1e-12, ids);
                if (ids.GetNumberOfIds() == 0) isEdgeOffBody = true;
            }
        }

        for (int i=-szW/2; i<=szW/2; i++)
            for (int j=-szH/2; j<=szH/2; j++)
            {
                if (isEdgeOffBody == false)
                {
                    imageSource.SetDrawColor(0,0,0);
                    imageSource.DrawPoint(i, j);
                    continue;
                }
                double s=(double)i/((double)szW/2)*ffacx;
                double t=(double)j/((double)szH/2)*ffacy;
                Vector3D ray=new Vector3D(s,t,-maxRayDepth);  // ray construction starts from s,t coordinates each on the interval [-1 1]
                ray=upRot.applyTo(lookRot.applyTo(ray));//upRot.applyInverseTo(lookRot.applyInverseTo(ray.normalize()));
                Vector3D rayEnd=ray.add(scPos);
                //
                vtkIdList ids=new vtkIdList();
                img.getSmallBodyModel().getCellLocator().FindCellsAlongLine(scPos.toArray(), rayEnd.toArray(), 1e-12, ids);
                if (ids.GetNumberOfIds()>0)
                    imageSource.SetDrawColor(0,0,0);
                else
                    imageSource.SetDrawColor(255,255,255);
                imageSource.DrawPoint(i, j);
            }

        imageSource.Update();
        vtkImageData imageData=imageSource.GetOutput();
        // (3) process the resulting black & white 2D image, which captures the "shadow" of the body against the sky, from the viewpoint of the camera

        // (3a) Actually create some polydata from the imagedata... pixels in the image-data become pairs of triangles (each pair forming a quad)
        vtkImageToPolyDataFilter imageConverter=new vtkImageToPolyDataFilter();
        imageConverter.SetInputData(imageData);
        imageConverter.SetOutputStyleToPixelize();
        imageConverter.Update();
        vtkPolyData tempImagePolyData=imageConverter.GetOutput();   // NOTE: the output of vtkImageToPolyDataFilter is in pixel coordinates, e.g. 0 to width-1 and 0 to height-1, probably with origin at the top-left of the image

        // (3b) create a new cell array to hold "white" cells, i.e. "off-limb" cells, from the "temporary" polydata representation of the silhouette
        vtkCellArray cells=new vtkCellArray();
        for (int c=0; c<tempImagePolyData.GetNumberOfCells(); c++)
        {
            double[] rgb=tempImagePolyData.GetCellData().GetScalars().GetTuple3(c);
            if (rgb[0]>0 || rgb[1]>0 || rgb[2]>0)
            {
                vtkCell cell=tempImagePolyData.GetCell(c);
                cells.InsertNextCell(cell.GetPointIds());
            }
        }
        // (3c) start to assemble a "final" polydata from the new cell array and points of the temporary silhouette (img could eventually be run through some sort of cleaning filter to get rid of orphaned points)
        imagePolyData=new vtkPolyData();
        imagePolyData.SetPoints(tempImagePolyData.GetPoints());
        imagePolyData.SetPolys(cells);
        double sfacx=offLimbFootprintDepth*Math.tan(Math.toRadians(fovx/2)); // scaling factor that "fits" the polydata into the frustum at the given footprintDepth (in the s,t plane perpendicular to the boresight)
        double sfacy=offLimbFootprintDepth*Math.tan(Math.toRadians(fovy/2));
        // make sure all points are reset with the correct transformation (though on-body cells have been culled, topology of the remaining cells doesn't need to be touched; just the respective points need to be unprojected into 3d space)
        for (int i=0; i<imagePolyData.GetNumberOfPoints(); i++)
        {
            Vector3D pt=new Vector3D(imagePolyData.GetPoint(i));            // here's a pixel coordinate
            pt=pt.subtract(new Vector3D((double)szW/2,(double)szH/2,0));    // move the origin to the center of the image, so values range from [-szW/2 szW/2] and [szH/2 szH/2]
            pt=new Vector3D(pt.getX()/((double)szW/2)*sfacx,pt.getY()/((double)szH/2)*sfacy,-offLimbFootprintDepth); // a number of things happen here; first a conversion to s,t-coordinates on [-1 1] in each direction, then scaling of the s,t-coordinates to fit the physical dimensions of the frustum at the chosen depth, and finally translation along the boresight axis to the chosen depth
            pt=scPos.add(upRot.applyTo(lookRot.applyTo(pt)));               // transform from (s,t) coordinates into the implied 3D direction vector, with origin at the camera's position in space; depth along the boresight was enforced on the previous line
            imagePolyData.GetPoints().SetPoint(i, pt.toArray());        // overwrite the old (pixel-coordinate) point with the new (3D cartesian) point
        }
    }

    public void saveToDisk(String filename)
    {
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(imagePolyData);
        writer.SetFileName(new File(filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
    }

    public static void main(String[] args) throws Exception
	{
    	boolean reprocess = true;
    	ShapeModelBody body = ShapeModelBody.RQ36;
        ShapeModelType type = ShapeModelType.ALTWG_SPC_v20190121;
    	boolean aplVersion = true;
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
//        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
        String rootURL = safeUrlPaths.getUrl("http://sbmt.jhuapl.edu/sbmt/prod/");
    	Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        SbmtMultiMissionTool.configureMission();

         // authentication
        Authenticator.authenticate();
        NativeLibraryLoader.loadVtkLibraries();
         // initialize view config
        SmallBodyViewConfig.initialize();
    	SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
    	SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
    	ImageKeyInterface key = new ImageKey("http://sbmt.jhuapl.edu/sbmt/prod/data/bennu/altwg-spc-v20190121/polycam/images/ocams20181108t042907s071_pol_iofl2pan_53003.fits", ImageSource.GASKELL);
    	OcamsFlightImage image = OcamsFlightImage.of(key, smallBodyModel, false);
    	String outputDir = ".";
    	String filename = outputDir +  File.separator  + FilenameUtils.getBaseName(image.getFitFileFullPath()) + "_" + 2 + "_offLimbImageData_new.vtk";
    	File file = new File(filename);
    	if (file.exists() && (reprocess == false)) return;

    	ServerOffLimbPlaneCalculator calculator = new ServerOffLimbPlaneCalculator(image);
    	calculator.generateOffLimbPlane(image, new Vector3D(image.getSpacecraftPosition()).getNorm());
    	if (!(new File(filename).exists())) new File(filename).getParentFile().mkdirs();
    	calculator.saveToDisk(filename);
	}
}
