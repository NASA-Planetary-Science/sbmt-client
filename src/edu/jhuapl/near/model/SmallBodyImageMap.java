package edu.jhuapl.near.model;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkImplicitBoolean;
import vtk.vtkPNGReader;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class SmallBodyImageMap extends Model
{
    public static class ImageInfo
    {
        public String filename = "";
        public double lllat = -90.0;
        public double lllon = 0.0;
        public double urlat = 90.0;
        public double urlon = 360.0;

        @Override
        public String toString()
        {
            DecimalFormat df = new DecimalFormat("#.#####");
            return filename + "  ["
                    + df.format(lllat) + ", "
                    + df.format(lllon) + ", "
                    + df.format(urlat) + ", "
                    + df.format(urlon)
                    + "]";
        }
    }

    private vtkPolyData imageMapPolyData;
    private vtkPolyData shiftedImageMapPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private double imageMapOpacity = 1.0;
    private SmallBodyModel smallBodyModel;
    private vtkTexture imageMapTexture;
    private boolean initialized = false;
    private ImageInfo imageInfo;
    private double offset;

    public SmallBodyImageMap(
            SmallBodyModel smallBodyModel,
            ImageInfo imageInfo)
    {
        super(ModelNames.SMALL_BODY);
        this.smallBodyModel = smallBodyModel;
        this.imageInfo = imageInfo;

        imageMapPolyData = new vtkPolyData();
        shiftedImageMapPolyData = new vtkPolyData();

        this.offset = getDefaultOffset();

        setVisible(false);
    }

    private void initialize()
    {
        if (initialized)
            return;

        vtkPolyData smallBodyPolyData = smallBodyModel.getSmallBodyPolyData();

        double[] origin = {0.0, 0.0, 0.0};
        double[] zaxis = {0.0, 0.0, 1.0};
        double[] mzaxis = {0.0, 0.0, -1.0};

        double lllat = imageInfo.lllat;
        double lllon = imageInfo.lllon;
        double urlat = imageInfo.urlat;
        double urlon = imageInfo.urlon;

        // If the texture does not cover the entire model, then clip out the part
        // that it does cover. Note that this is valid ONLY for an ellipsoid. For
        // a non-ellipsoidal body this is NOT valid. It is assumed that for a
        // non-ellipsoid body, that the texture covers the ENTIRE body.
        if (lllat != -90.0 || lllon != 0.0 || urlat != 90.0 || urlon != 360.0)
        {
            lllat *= (Math.PI / 180.0);
            urlat *= (Math.PI / 180.0);
            lllon *= (Math.PI / 180.0);
            urlon *= (Math.PI / 180.0);

            boolean textureCrossesZeroLon = urlon < lllon;
            boolean widerThan180 = false;
            if (!textureCrossesZeroLon)
            {
                if (urlon - lllon > Math.PI)
                    widerThan180 = true;
            }
            else
            {
                if (2.0 * Math.PI - (lllon - urlon) > Math.PI)
                    widerThan180 = true;
            }

            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon, 1.0));
            double[] normal = new double[3];
            if (widerThan180)
                MathUtil.vcrss(vec, mzaxis, normal);
            else
                MathUtil.vcrss(vec, zaxis, normal);

            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            vec = MathUtil.latrec(new LatLon(0.0, urlon, 1.0));
            if (widerThan180)
                MathUtil.vcrss(vec, zaxis, normal);
            else
                MathUtil.vcrss(vec, mzaxis, normal);

            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            vtkImplicitBoolean implicitBoolean = new vtkImplicitBoolean();
            implicitBoolean.SetOperationTypeToIntersection();
            implicitBoolean.AddFunction(plane1);
            implicitBoolean.AddFunction(plane2);

            vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
            clipPolyData1.SetClipFunction(implicitBoolean);
            clipPolyData1.SetInput(smallBodyPolyData);
            if (widerThan180)
                clipPolyData1.SetInsideOut(0);
            else
                clipPolyData1.SetInsideOut(1);
            vtkAlgorithmOutput clipPolyData1Output = clipPolyData1.GetOutputPort();


            double[] intersectPoint = new double[3];
            smallBodyModel.getPointAndCellIdFromLatLon(lllat, 0.0, intersectPoint);
            vec = new double[]{0.0, 0.0, intersectPoint[2]};


            vtkPlane plane3 = new vtkPlane();
            plane3.SetOrigin(vec);
            plane3.SetNormal(zaxis);

            vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
            clipPolyData2.SetClipFunction(plane3);
            clipPolyData2.SetInputConnection(clipPolyData1Output);
            vtkAlgorithmOutput clipPolyData2Output = clipPolyData2.GetOutputPort();


            smallBodyModel.getPointAndCellIdFromLatLon(urlat, 0.0, intersectPoint);
            vec = new double[]{0.0, 0.0, intersectPoint[2]};

            vtkPlane plane4 = new vtkPlane();
            plane4.SetOrigin(vec);
            plane4.SetNormal(zaxis);

            vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
            clipPolyData3.SetClipFunction(plane4);
            clipPolyData3.SetInputConnection(clipPolyData2Output);
            clipPolyData3.SetInsideOut(1);

            smallBodyPolyData = clipPolyData3.GetOutput();
        }

        // Now divide the above along the zero longitude line and compute
        // texture coordinates for each half separately, then combine the two.
        // This avoids having a "seam" at the zero longitude line.

        double[] vec = MathUtil.latrec(new LatLon(0.0, 0.0, 1.0));
        double[] normal = new double[3];
        MathUtil.vcrss(vec, zaxis, normal);

        vtkPlane plane1 = new vtkPlane();
        plane1.SetOrigin(origin);
        plane1.SetNormal(normal);

        vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
        clipPolyData1.SetClipFunction(plane1);
        clipPolyData1.SetInput(smallBodyPolyData);
        clipPolyData1.Update();
        vtkPolyData clipPolyData1Output = clipPolyData1.GetOutput();

        generateTextureCoordinates(clipPolyData1Output, true);



        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(plane1);
        clipPolyData2.SetInput(smallBodyPolyData);
        clipPolyData2.SetInsideOut(1);
        clipPolyData2.Update();
        vtkPolyData clipPolyData2Output = clipPolyData2.GetOutput();

        generateTextureCoordinates(clipPolyData2Output, false);



        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.UserManagedInputsOn();
        appendFilter.SetNumberOfInputs(2);
        appendFilter.SetInputByNumber(0, clipPolyData1Output);
        appendFilter.SetInputByNumber(1, clipPolyData2Output);
        appendFilter.Update();

        vtkPolyData appendFilterOutput = appendFilter.GetOutput();
        imageMapPolyData.DeepCopy(appendFilterOutput);

        shiftedImageMapPolyData.DeepCopy(imageMapPolyData);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedImageMapPolyData, offset);

        initialized = true;
    }

    protected void generateTextureCoordinates(vtkPolyData polydata, boolean mapZeroLongitudeToRight)
    {
        double lllat = imageInfo.lllat;
        double lllon = imageInfo.lllon;
        double urlat = imageInfo.urlat;
        double urlon = imageInfo.urlon;

        lllat *= (Math.PI / 180.0);
        lllon *= (Math.PI / 180.0);
        urlat *= (Math.PI / 180.0);
        urlon *= (Math.PI / 180.0);

        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = polydata.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = polydata.GetPoints();

        // If the texture crosses over the line of zero longitude,
        // then shift all longitudes to the right so that the left
        // side of the texture is at zero longitude.
        boolean textureCrossesZeroLon = urlon < lllon;
        double shift = 0.0;
        if (textureCrossesZeroLon)
        {
            shift = (2.0 * Math.PI - lllon);
            urlon += shift;
            lllon = 0.0;
        }

        double xsize = urlon - lllon;
        double ysize = urlat - lllat;

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

            if (ll.lon < 0.0)
               ll.lon += (2.0 * Math.PI);
            if (ll.lon >= 2.0 * Math.PI)
                ll.lon = 0.0;

            double origLon = ll.lon;

            if (textureCrossesZeroLon)
            {
                ll.lon += shift;
                if (ll.lon >= 2.0 * Math.PI)
                    ll.lon -= (2.0 * Math.PI);

                if (ll.lon > urlon)
                {
                    if (2.0*Math.PI - ll.lon < ll.lon - urlon)
                        ll.lon = lllon;
                    else
                        ll.lon = urlon;
                }
            }

            double u = (ll.lon - lllon) / xsize;
            double v = (ll.lat - lllat) / ysize;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;

            if (origLon == 0.0 && u == 0.0 && mapZeroLongitudeToRight)
                u = 1.0;

            textureCoords.SetTuple2(i, u, v);
        }

        polydata.GetPointData().SetTCoords(textureCoords);
    }

    public ArrayList<vtkProp> getProps()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.ScalarVisibilityOff();
            smallBodyMapper.SetScalarModeToDefault();
            smallBodyMapper.SetInput(shiftedImageMapPolyData);
            smallBodyMapper.Update();

            imageMapTexture = new vtkTexture();
            imageMapTexture.InterpolateOn();
            imageMapTexture.RepeatOff();
            imageMapTexture.EdgeClampOn();
            vtkImageData image = loadImageMap(imageInfo.filename);
            imageMapTexture.SetInput(image);

            smallBodyActor = new vtkActor();
            smallBodyActor.SetMapper(smallBodyMapper);
            smallBodyActor.SetTexture(imageMapTexture);

            smallBodyActors.add(smallBodyActor);
        }

        return smallBodyActors;
    }

    public void setShowImageMap(boolean b)
    {
        if (b)
            initialize();

        smallBodyActor.SetVisibility(b ? 1 : 0);
        super.setVisible(b);
    }

    public double getImageMapOpacity()
    {
        return imageMapOpacity;
    }

    public void setImageMapOpacity(double imageMapOpacity)
    {
        this.imageMapOpacity = imageMapOpacity;
        vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
        smallBodyProperty.SetOpacity(imageMapOpacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected vtkImageData loadImageMap(String name)
    {
        File imageFile = FileCache.getFileFromServer(name);
        vtkPNGReader reader = new vtkPNGReader();
        reader.SetFileName(imageFile.getAbsolutePath());
        reader.Update();
        return reader.GetOutput();
    }

    public double getDefaultOffset()
    {
        return 2.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        shiftedImageMapPolyData.DeepCopy(imageMapPolyData);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedImageMapPolyData, offset);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

    public void delete()
    {
    }
}
