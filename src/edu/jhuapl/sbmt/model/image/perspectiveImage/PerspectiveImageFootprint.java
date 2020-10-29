package edu.jhuapl.sbmt.model.image.perspectiveImage;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkLookupTable;
import vtk.vtkPlane;
import vtk.vtkPlaneCollection;
import vtk.vtkPointData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTexture;
import vtk.vtkUnsignedCharArray;
import vtk.vtkXMLPolyDataReader;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.FacetColoringData;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.ImageKeyInterface;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;

public class PerspectiveImageFootprint implements PlannedDataActor
{
	vtkPolyData[] footprint = new vtkPolyData[1];
	boolean[] footprintGenerated = new boolean[1];
	final vtkPolyData[] shiftedFootprint = new vtkPolyData[1];
	private vtkActor footprintActor;
	private List<vtkProp> footprintActors = new ArrayList<vtkProp>();
	private boolean useDefaultFootprint = true;
	int nslices;
	double[][] scPos, frus1, frus2, frus3, frus4;
	int defaultSlice, currentSlice;
//	PerspectiveImage image;
    // If true, then the footprint is generated by intersecting a frustum with the
    // asteroid.
    // This setting is used when generating the files on the server.
    // If false, then the footprint is downloaded from the server. This setting is
    // used by the GUI.
    private static boolean generateFootprint = true;
    private vtkFloatArray textureCoords;
    private double imageOpacity = 1.0;
    protected vtkTexture imageTexture = null;
    private vtkPolyData boundary;
    private vtkActor boundaryActor;
    private SmallBodyModel smallBodyModel;
//  private PerspectiveImageFrustum frustum;
    private double imageOffset;
    private vtkImageData displayedImage = null;
    private String preRenderedName = null;
    private int imageWidth=0, imageHeight=0, imageDepth=0;
    private ImageKeyInterface key = null;
    private vtkPolyDataMapper footprintMapper;
    private vtkPolyDataMapper boundaryMapper;
    Logger logger = Logger.getAnonymousLogger();
    private String instrumentName;
    private boolean isVisible = false;
    private Color color;
    private String plateColoringName = null;
    private double time;
    private boolean staticFootprint = false;
    private boolean staticFootprintSet = false;


	PerspectiveImageFootprint(PerspectiveImage image)
	{
		this.smallBodyModel = image.getSmallBodyModel();
		this.imageOffset = image.getOffset();
		defaultSlice = image.getDefaultSlice();
		currentSlice = image.getCurrentSlice();
		this.key = image.getKey();
		this.imageHeight = image.getImageHeight();
		this.imageWidth = image.getImageWidth();
		this.imageDepth = image.getImageDepth();
		this.preRenderedName = image.getPrerenderingFileNameBase();
		this.scPos = image.getSpacecraftPositionAdjusted();
		this.frus1 = image.getFrustum1Adjusted();
		this.frus2 = image.getFrustum2Adjusted();
		this.frus3 = image.getFrustum3Adjusted();
		this.frus4 = image.getFrustum4Adjusted();


		nslices = image.getImageDepth();
		footprint = new vtkPolyData[nslices];
		footprint[0] = new vtkPolyData();
		footprintGenerated = new boolean[nslices];
		shiftedFootprint[0] = new vtkPolyData();
		boundaryActor = new vtkActor();
		boundaryActor.VisibilityOff();
		footprintActor = new vtkActor();
		footprintActor.VisibilityOff();
	}

	public PerspectiveImageFootprint()
	{
		this(1, 0, 0, 0.00001);
	}

	public PerspectiveImageFootprint(int numSlices, int currentSlice, int defaultSlice, double imageOffset)
	{
		this.nslices = numSlices;
		this.defaultSlice = defaultSlice;
		this.currentSlice = currentSlice;
		this.imageOffset = imageOffset;
		this.scPos = new double[1][];
		this.frus1 = new double[1][];
		this.frus2 = new double[1][];
		this.frus3 = new double[1][];
		this.frus4 = new double[1][];
		footprint = new vtkPolyData[nslices];
		footprint[0] = new vtkPolyData();
		footprintGenerated = new boolean[nslices];
		shiftedFootprint[0] = new vtkPolyData();
		boundaryActor = new vtkActor();
		boundaryActor.VisibilityOff();
		footprintActor = new vtkActor();
		footprintActor.VisibilityOff();
	}

	PerspectiveImageFootprint(PerspectiveImageFrustum frustum, SmallBodyModel smallBodyModel, int numSlices,
			int currentSlice, int defaultSlice, double imageOffset, double[] scPos,
			double[] frus1, double[] frus2, double[] frus3, double[] frus4)
	{
		this.nslices = numSlices;
		this.defaultSlice = defaultSlice;
		this.currentSlice = currentSlice;
		this.imageOffset = imageOffset;
		this.scPos = new double[1][];
		this.frus1 = new double[1][];
		this.frus2 = new double[1][];
		this.frus3 = new double[1][];
		this.frus4 = new double[1][];
		this.scPos[0] = scPos;
		this.frus1[0] = frus1;
		this.frus2[0] = frus2;
		this.frus3[0] = frus3;
		this.frus4[0] = frus4;
		footprint = new vtkPolyData[nslices];
		footprint[0] = new vtkPolyData();
		footprintGenerated = new boolean[nslices];
		shiftedFootprint[0] = new vtkPolyData();
		boundaryActor = new vtkActor();
		boundaryActor.VisibilityOff();
		footprintActor = new vtkActor();
		footprintActor.VisibilityOff();
	}

	public void initialize()
	{

	}

	public void initSpacecraftStateVariables()
	{
		footprint = new vtkPolyData[nslices];
        footprintGenerated = new boolean[nslices];
	}

	public void updatePointing(PerspectiveImage image)
	{
		this.scPos = image.getSpacecraftPositionAdjusted();
		this.frus1 = image.getFrustum1Adjusted();
		this.frus2 = image.getFrustum2Adjusted();
		this.frus3 = image.getFrustum3Adjusted();
		this.frus4 = image.getFrustum4Adjusted();
		this.imageHeight = image.getImageHeight();
		this.imageWidth = image.getImageWidth();
		this.imageDepth = image.getImageDepth();
		this.footprint[0] = new vtkPolyData();
		this.shiftedFootprint[0] = new vtkPolyData();
		image.setDisplayedImageRange(null);
		this.displayedImage = image.getDisplayedImage();
	}



	public void updatePointing(double[] scPos, double[] frus1, double[] frus2, double[] frus3, double[] frus4,
			int height, int width, int depth)
	{
//		System.out.println("PerspectiveImageFootprint: updatePointing: updating pointing");
		this.scPos[0] = scPos;
		this.frus1[0] = frus1;
		this.frus2[0] = frus2;
		this.frus3[0] = frus3;
		this.frus4[0] = frus4;
		this.imageHeight = height;
		this.imageWidth = width;
		this.imageDepth = depth;
//		System.out.println("PerspectiveImageFootprint: updatePointing: updating scpos to " + scPos[0] + " " + scPos[1] + " " + scPos[2]);
//		System.out.println("PerspectiveImageFootprint: updatePointing: updating frus1 to " + frus1[0] + " " + frus1[1] + " " + frus1[2]);
//		System.out.println("PerspectiveImageFootprint: updatePointing: updating frus2 to " + frus2[0] + " " + frus2[1] + " " + frus2[2]);
		footprintGenerated[currentSlice] = false;
		useDefaultFootprint = false;
		generateBoundary();
		setFootprintColor();
		getProps();
	}

	public boolean[] getFootprintGenerated()
	{
		return footprintGenerated;
	}

	public void setFootprintGenerated(boolean footprintGenerated)
	{
		this.footprintGenerated[defaultSlice] = footprintGenerated;
	}

	public void setFootprintGenerated(boolean footprintGenerated, int slice)
	{
		this.footprintGenerated[slice] = footprintGenerated;
	}

	public static boolean isGenerateFootprint()
	{
		return generateFootprint;
	}

	/**
	 * The shifted footprint is the original footprint shifted slightly in the
	 * normal direction so that it will be rendered correctly and not obscured
	 * by the asteroid.
	 *
	 * @return
	 */
	vtkPolyData getShiftedFootprint()
	{
		return shiftedFootprint[0];
	}

	/**
	 * The original footprint whose cells exactly overlap the original asteroid.
	 * If rendered as is, it would interfere with the asteroid. Note: this is
	 * made public in this class for the benefit of backplane generators, which
	 * use it.
	 *
	 * @return
	 */
	vtkPolyData getUnshiftedFootprint()
	{
		return footprint[currentSlice];
	}

	vtkPolyData generateBoundary()
	{
		loadFootprint();

		if (footprint[currentSlice].GetNumberOfPoints() == 0)
			return null;

		return updateBoundary();
	}

	private vtkPolyData updateBoundary()
	{
		vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
		edgeExtracter.SetInputData(footprint[currentSlice]);
		edgeExtracter.BoundaryEdgesOn();
		edgeExtracter.FeatureEdgesOff();
		edgeExtracter.NonManifoldEdgesOff();
		edgeExtracter.ManifoldEdgesOff();
		edgeExtracter.ColoringOff();
		edgeExtracter.Update();

		boundary = new vtkPolyData();
		vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
		boundary.DeepCopy(edgeExtracterOutput);
		if (boundaryMapper != null)
		{
	        boundaryMapper.SetInputData(boundary);
	        boundaryMapper.Update();
	        boundaryActor.SetMapper(boundaryMapper);
		}
		return boundary;
	}

	public void loadFootprint()
    {
//		logger.log(Level.INFO, "Loading footprint");
        vtkPolyData existingFootprint = checkForExistingFootprint();
        if (existingFootprint != null)
        {
            footprint[0] = existingFootprint;

            vtkPointData pointData = footprint[currentSlice].GetPointData();
            pointData.SetTCoords(textureCoords);
            PolyDataUtil.generateTextureCoordinates(scPos[currentSlice], frus1[currentSlice], frus2[currentSlice], frus3[currentSlice],
					imageWidth, imageHeight, footprint[currentSlice]);
            pointData.Delete();

            shiftedFootprint[0].DeepCopy(footprint[currentSlice]);
            PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint[0], imageOffset);
            return;
        }

        if (generateFootprint)
        {
            vtkPolyData tmp = null;

            if (!footprintGenerated[currentSlice])
            {
                if (useDefaultFootprint())
                {
                    if (footprintGenerated[defaultSlice] == false)
                    {
                        footprint[defaultSlice] = getFootprint(defaultSlice);
                        if (footprint[defaultSlice] == null)
                            return;

                        // Need to clear out scalar data since if coloring data is being shown,
                        // then the color might mix-in with the image.
                        footprint[defaultSlice].GetCellData().SetScalars(null);
                        footprint[defaultSlice].GetPointData().SetScalars(null);

                        footprintGenerated[defaultSlice] = true;
                    }

                    tmp = footprint[defaultSlice];

                }
                else
                {
//            		Logger.getAnonymousLogger().log(Level.INFO, "Calculating frustum intersection");
                    tmp = smallBodyModel.computeFrustumIntersection(scPos[currentSlice], frus1[currentSlice], frus3[currentSlice], frus4[currentSlice], frus2[currentSlice]);
                    if (footprintActor != null && isVisible == true)
                    	footprintActor.SetVisibility(1);
                    if (tmp == null)
                    {
                    	//TODO why is footprintActor null here?
                    	if (footprintActor != null) footprintActor.SetVisibility(0);
                        return;
                    }
                    // Need to clear out scalar data since if coloring data is being shown,
                    // then the color might mix-in with the image.
                    tmp.GetCellData().SetScalars(null);
                    tmp.GetPointData().SetScalars(null);
                }

                footprint[currentSlice].DeepCopy(tmp);

                footprintGenerated[currentSlice] = true;
            }
//    		Logger.getAnonymousLogger().log(Level.INFO, "Footprint generated, loading texture");

            vtkPointData pointData = footprint[currentSlice].GetPointData();
            pointData.SetTCoords(textureCoords);
            PolyDataUtil.generateTextureCoordinates(scPos[currentSlice], frus1[currentSlice], frus2[currentSlice], frus3[currentSlice],
					imageWidth, imageHeight, footprint[currentSlice]);
            pointData.Delete();
//    		Logger.getAnonymousLogger().log(Level.INFO, "Loaded texture");

        }
        else
        {
            int resolutionLevel = smallBodyModel.getModelResolution();
            String footprintFilename = null;
            File file = null;

            if (key.getSource() == ImageSource.SPICE || key.getSource() == ImageSource.CORRECTED_SPICE)
                footprintFilename = key.getName() + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
            else
                footprintFilename = key.getName() + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

            file = FileCache.getFileFromServer(footprintFilename);

            if (file == null || !file.exists())
            {
                System.out.println("Warning: " + footprintFilename + " not found");
                return;
            }

            vtkXMLPolyDataReader footprintReader = new vtkXMLPolyDataReader();
            footprintReader.SetFileName(file.getAbsolutePath());
            footprintReader.Update();

            vtkPolyData footprintReaderOutput = footprintReader.GetOutput();
            footprint[currentSlice].DeepCopy(footprintReaderOutput);
        }

        shiftedFootprint[0].DeepCopy(footprint[currentSlice]);
        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint[0], imageOffset);
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(footprint[0]);
        String intersectionFileName = preRenderedName + "_frustumIntersection.vtk";
        File file = FileCache.instance().getFile(intersectionFileName);
        writer.SetFileName(file.getPath());
        writer.SetFileTypeToBinary();
        writer.Write();
        setFootprintGenerated(true);
//        logger.log(Level.INFO, "Footprint load complete");
    }



	public vtkPolyData getFootprint(int defaultSlice)
    {
        if (footprint[0] != null && footprint[0].GetNumberOfPoints() > 0)
            return footprint[0];
        // first check the cache
        vtkPolyData existingFootprint = checkForExistingFootprint();
        if (existingFootprint != null)
        {
            return existingFootprint;
        }
        else
        {
            vtkPolyData footprint = smallBodyModel.computeFrustumIntersection(scPos[defaultSlice], frus1[defaultSlice], frus3[defaultSlice], frus4[defaultSlice], frus2[defaultSlice]);
            return footprint;
        }
    }

    vtkPolyData checkForExistingFootprint()
    {
    	if (getFootprintGenerated()[currentSlice] == false) return null;
        String intersectionFileName = preRenderedName + "_frustumIntersection.vtk.gz";
        if (FileCache.isFileGettable(intersectionFileName))
        {
            File file = FileCache.getFileFromServer(intersectionFileName);
            vtkPolyDataReader reader = new vtkPolyDataReader();
//            reader.SetFileName(file.getPath().replaceFirst("\\.[^\\.]*$", ""));	//This is wrong.  The old code was stripping off .gz from the intersection name.  This now further removes .vtk which is bad.
            reader.SetFileName(file.getAbsolutePath()); // now just reads in the file path as it should.
            reader.Update();
            vtkPolyData footprint = reader.GetOutput();
            return footprint;
        }
        return null;
    }

    public vtkPolyData[] getFootprint()
	{
		return footprint;
	}

	public void setFootprint(vtkPolyData[] footprint)
	{
		this.footprint = footprint;
	}

    static void setGenerateFootprint(boolean b)
    {
        generateFootprint = b;
    }

	void setUseDefaultFootprint(boolean useDefaultFootprint)
	{
		this.useDefaultFootprint = useDefaultFootprint;
		for (int i = 0; i < nslices; i++)
		{
			footprintGenerated[i] = false;
		}
	}

	boolean useDefaultFootprint()
	{
		return useDefaultFootprint;
	}

	void Delete()
	{
		for (int i = 0; i < footprint.length; i++)
        {
            // Footprints can be null if no frustum intersection is found
            if (footprint[i] != null)
            {
                footprint[i].Delete();
            }
        }

        for (int i = 0; i < shiftedFootprint.length; i++)
        {
            if (shiftedFootprint[i] != null)
            {
                shiftedFootprint[i].Delete();
            }
        }
        textureCoords.Delete();
	}

	private vtkPlaneCollection clipPlanes()
	{
		double[] top = new double[3];
		double[] right = new double[3];
		double[] bottom = new double[3];
		double[] left = new double[3];
		double[] origin = scPos[currentSlice];
		double[] ul = frus1[currentSlice];
		double[] ur = frus3[currentSlice];
		double[] lr = frus4[currentSlice];
		double[] ll = frus2[currentSlice];

		MathUtil.vcrss(ur, ul, top);
		MathUtil.vcrss(lr, ur, right);
		MathUtil.vcrss(ll, lr, bottom);
		MathUtil.vcrss(ul, ll, left);
		double dx = MathUtil.vnorm(origin);
		double[] UL2 = { origin[0] + ul[0] * dx, origin[1] + ul[1] * dx, origin[2] + ul[2] * dx };
		double[] UR2 = { origin[0] + ur[0] * dx, origin[1] + ur[1] * dx, origin[2] + ur[2] * dx };
		double[] LL2 = { origin[0] + ll[0] * dx, origin[1] + ll[1] * dx, origin[2] + ll[2] * dx };
		double[] LR2 = { origin[0] + lr[0] * dx, origin[1] + lr[1] * dx, origin[2] + lr[2] * dx };

		vtkPlane plane1 = new vtkPlane();
		plane1.SetOrigin(UL2);
		plane1.SetNormal(top);
		vtkPlane plane2 = new vtkPlane();
		plane2.SetOrigin(UR2);
		plane2.SetNormal(right);
		vtkPlane plane3 = new vtkPlane();
		plane3.SetOrigin(LR2);
		plane3.SetNormal(bottom);
		vtkPlane plane4 = new vtkPlane();
		plane4.SetOrigin(LL2);
		plane4.SetNormal(left);

		vtkPlaneCollection collection = new vtkPlaneCollection();
		collection.AddItem(plane1);
		collection.AddItem(plane2);
		collection.AddItem(plane3);
//		collection.AddItem(plane4);
		return collection;
	}

	List<vtkProp> getProps()
	{
		if (imageTexture == null)
        {
            loadFootprint();

            imageTexture = new vtkTexture();
            imageTexture.InterpolateOn();
            imageTexture.RepeatOff();
            imageTexture.EdgeClampOn();
            imageTexture.SetInputData(displayedImage);
			footprintMapper = new vtkPolyDataMapper();
	        footprintMapper.SetInputData(shiftedFootprint[0]);

	        footprintMapper.Update();
//	        footprintActor = new vtkActor();
	        footprintActor.SetMapper(footprintMapper);
	        footprintActor.SetTexture(imageTexture);
//	        footprintActor.VisibilityOn();
	        vtkProperty footprintProperty = footprintActor.GetProperty();
	        footprintProperty.LightingOff();

	        if (boundary == null) generateBoundary();

	        boundaryMapper = new vtkPolyDataMapper();
	        boundaryMapper.SetInputData(boundary);
	        boundaryMapper.Update();
	        boundaryActor.SetMapper(boundaryMapper);
	        boundaryActor.GetProperty().SetLineWidth(3.0);
	        boundaryActor.VisibilityOff();
	        footprintActors.add(boundaryActor);
	        footprintActors.add(footprintActor);
        }

	    return footprintActors;
	}

	void initializeMaskingAfterLoad()
    {
		for (int k = 0; k < nslices; k++)
        {
            footprint[k] = new vtkPolyData();
        }

        shiftedFootprint[0] = new vtkPolyData();
        textureCoords = new vtkFloatArray();
    }

	public void setOpacity(double imageOpacity)
    {
        this.imageOpacity = imageOpacity;
        vtkProperty smallBodyProperty = footprintActor.GetProperty();
        smallBodyProperty.SetOpacity(imageOpacity);
//        image.firePropertyChange(Properties.MODEL_CHANGED, null, this);
    }

    public double getOpacity()
    {
        return imageOpacity;
    }

    public void setVisible(boolean b)
    {
    	isVisible = b;
        footprintActor.SetVisibility(b ? 1 : 0);
        setFootprintColor();
    }

    /**
	 * @return the isVisible
	 */
	public boolean isVisible()
	{
		return isVisible;
	}

	vtkTexture getTexture()
    {
        return imageTexture;
    }

	public vtkActor getFootprintActor()
	{
		if (imageTexture == null && scPos[0] != null)
		{
			getProps();
		}
		return footprintActor;
	}

	public vtkActor getFootprintBoundaryActor()
	{
		return boundaryActor;
	}

	public void setBoundaryVisible(boolean isVisible)
	{
		boundaryActor.SetVisibility(isVisible ? 1 : 0);
		boundaryActor.Modified();
	}

	public void setBoundaryColor(Color color)
	{
		boundaryActor.GetProperty().SetColor(new double[] {(double)color.getRed()/255.0,
				(double)color.getGreen()/255.0,
				(double)color.getBlue()/255.0});
		boundaryActor.Modified();
	}

	public FacetColoringData[] getFacetColoringDataForFootprint()
	{
		return smallBodyModel.getPlateDataInsidePolydata(getShiftedFootprint());
	}

	private vtkLookupTable updateColorFromPlate(String coloringPlateName)
	{
//		logger.log(Level.INFO, "Updating Colors");
		//grab coloring data for plates near the footprint
		FacetColoringData[] plateDataInsidePolydata = getFacetColoringDataForFootprint();
		Colormap colormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
		int numberElements = smallBodyModel.getColoringDataManager().getResolutions().get(smallBodyModel.getModelResolution());
		ColoringData globalColoringData = smallBodyModel.getColoringDataManager().get(coloringPlateName, numberElements);

		double[] range = globalColoringData.getDefaultRange();
		colormap.setRangeMin(range[0]);
		colormap.setRangeMax(range[1]);
		colormap.setNumberOfLevels(32);

		//create and setup the LUT
		vtkLookupTable lut = new vtkLookupTable();
		lut.SetIndexedLookup(1);
        lut.SetNumberOfTableValues(plateDataInsidePolydata.length);
        lut.Build();

        //now populated the LUT using the coloring in the FacetColoringData
        int i=0;
		for (FacetColoringData coloringData : plateDataInsidePolydata)
		{
			double[] coloringValuesFor = null;
			try
			{
				coloringValuesFor = coloringData.getColoringValuesFor(coloringPlateName);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Color c = colormap.getColor(coloringValuesFor[0]);
			lut.SetTableValue(i, new double[] {((double)c.getRed())/255.0, ((double)c.getGreen())/255.0, ((double)c.getBlue())/255.0});
			lut.SetAnnotation("" + i++, ""+ coloringData.getCellId());
		}
		Vector<Integer> cellIds = new Vector<Integer>();
		for (i=0; i< shiftedFootprint[0].GetNumberOfCells(); i++)
		{
			double index = shiftedFootprint[0].GetCellData().GetArray(0).GetTuple1(i);
			if (!cellIds.contains((int)index))
				cellIds.add((int)index);
		}

		vtkUnsignedCharArray cellData = new vtkUnsignedCharArray();
		cellData.SetNumberOfComponents(4);
		for (i=0; i< shiftedFootprint[0].GetNumberOfCells(); i++)
		{
			int cellId = (int)(shiftedFootprint[0].GetCellData().GetArray(0).GetTuple1(i));
			double[] colorArray = lut.GetColor(cellIds.indexOf(cellId));
			cellData.InsertNextTuple4(colorArray[0]*255, colorArray[1]*255, colorArray[2]*255, 255);	//this needs to be the color for the cell
		}

		shiftedFootprint[0].GetCellData().SetScalars(cellData);

//		logger.log(Level.INFO, "Colors Updated");
		return lut;
	}

	public void setFootprintColor()
	{
		if (footprintActor == null) getProps();
		if (footprintActor.GetVisibility() == 0) return;

		if (plateColoringName != null)
		{
			updateColorFromPlate(plateColoringName);
			footprintMapper.SetScalarModeToUseCellData();
		}
		else
		{
			if (color == null) color = Color.white;
			footprintActor.GetProperty().SetColor(new double[] {(double)color.getRed()/255.0,
					(double)color.getGreen()/255.0,
					(double)color.getBlue()/255.0});
		}
		footprintMapper.Update();
		footprintActor.Modified();

	}

	public void setSmallBodyModel(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}

	/**
	 * @return the instrumentName
	 */
	public String getInstrumentName()
	{
		return instrumentName;
	}

	/**
	 * @param instrumentName the instrumentName to set
	 */
	public void setInstrumentName(String instrumentName)
	{
		this.instrumentName = instrumentName;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
	{
		this.color = color;
		setBoundaryColor(color);
	}

	public Color getColor()
	{
		return this.color;
	}

	/**
	 * @param plateColoringName the plateColoringName to set
	 */
	public void setPlateColoringName(String plateColoringName)
	{
		this.plateColoringName = plateColoringName;
		setFootprintColor();
	}

	/**
	 * @return the imageTime
	 */
	public double getImageTime()
	{
		return time;
	}

	public double getTime()
	{
		return time;
	}

	/**
	 * @param imageTime the imageTime to set
	 */
	public void setTime(double imageTime)
	{
		this.time = imageTime;
	}

	@Override
	public void SetVisibility(int visible)
	{
		if (getFootprintActor() == null) return;
		getFootprintActor().SetVisibility(visible);
	}

	/**
	 * @return the staticFootprint
	 */
	public boolean isStaticFootprint()
	{
		return staticFootprint;
	}

	/**
	 * @param staticFootprint the staticFootprint to set
	 */
	public void setStaticFootprint(boolean staticFootprint)
	{
		this.staticFootprint = staticFootprint;
	}

	/**
	 * @return the staticFootprintSet
	 */
	public boolean isStaticFootprintSet()
	{
		return staticFootprintSet;
	}

	/**
	 * @param staticFootprintSet the staticFootprintSet to set
	 */
	public void setStaticFootprintSet(boolean staticFootprintSet)
	{
		this.staticFootprintSet = staticFootprintSet;
	}
}