package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

import vtk.*;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.ConvertResourceToFile;
import edu.jhuapl.near.util.SmallBodyCubes;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class SmallBodyModel extends Model 
{
    public enum ColoringType { 
    	NONE, 
    	ELEVATION, 
    	GRAVITATIONAL_ACCELERATION,
    	GRAVITATIONAL_POTENTIAL,
    	SLOPE
    }

    public enum ShadingType {
    	FLAT,
    	SMOOTH,
    }
    
    static public final String ElevStr = "Elevation";
    static public final String GravAccStr = "Gravitational Acceleration";
    static public final String GravPotStr = "Gravitational Potential";
    static public final String SlopeStr = "Slope";
    static public final String ElevUnitsStr = "m";
    static public final String GravAccUnitsStr = "m/s^2";
    static public final String GravPotUnitsStr = "J/kg";
    static public final String SlopeUnitsStr = "deg";
    static public final String FlatShadingStr = "Flat";
    static public final String SmoothShadingStr = "Smooth";
    static public final String LowResModelStr = "Low (49152 plates)";
    static public final String MedResModelStr = "Medium (196608 plates)";
    static public final String HighResModelStr = "High (786432 plates)";
    static public final String VeryHighResModelStr = "Very High (3145728 plates)";

    private vtkPolyData smallBodyPolyData;
    private vtkPolyData lowResSmallBodyPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private vtksbCellLocator cellLocator;
    private vtkPointLocator pointLocator;
    private vtkPointLocator lowResPointLocator;
    private vtkFloatArray elevationPointDataValues;
    private vtkFloatArray gravAccPointDataValues;
    private vtkFloatArray gravPotPointDataValues;
    private vtkFloatArray slopePointDataValues;
    private vtkImageData elevationImage;
    private vtkImageData gravAccImage;
    private vtkImageData gravPotImage;
    private vtkImageData slopeImage;
    private vtkImageData originalImageMap;
    private vtkImageData displayedImageMap;
    private vtkScalarBarActor scalarBarActor;
    private vtkPolyDataReader smallBodyReader;
	private SmallBodyCubes smallBodyCubes;
    private ColoringType coloringType = ColoringType.NONE;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    private String[] modelNames;
    private String[] modelFiles;
	private String[] coloringFiles;
	private String imageMapName = null;
    private boolean showImageMap = false;
	private vtkTexture imageMapTexture = null;
	private BoundingBox boundingBox = null;
	private double imageMapOpacity = 0.50;
	private vtkImageBlend blendFilter;
	
	/**
	 * Default constructor. Must be followed by a call to setSmallBodyPolyData.
	 */
	public SmallBodyModel()
	{
		smallBodyPolyData = new vtkPolyData();
		genericCell = new vtkGenericCell();
	}
			
	public SmallBodyModel(
			String[] modelNames,
			String[] modelFiles,
			String[] coloringFiles,
			String imageMapName,
			boolean lowestResolutionModelStoredInResource)
	{
		super(ModelNames.SMALL_BODY);
		
		this.modelNames = modelNames;
		this.modelFiles = modelFiles;
		this.coloringFiles = coloringFiles;
		this.imageMapName = imageMapName;
		
    	smallBodyReader = new vtkPolyDataReader();
		smallBodyPolyData = new vtkPolyData();
		genericCell = new vtkGenericCell();
		
		if (lowestResolutionModelStoredInResource)
			defaultModelFile = ConvertResourceToFile.convertResourceToTempFile(this, modelFiles[0]);
		else
			defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);

		initialize(defaultModelFile);
	}

	public void setSmallBodyPolyData(vtkPolyData polydata,
			vtkFloatArray elevationPointDataValues,
			vtkFloatArray gravAccPointDataValues,
			vtkFloatArray gravPotPointDataValues,
			vtkFloatArray slopePointDataValues)
	{
		smallBodyPolyData.DeepCopy(polydata);
		this.elevationPointDataValues = elevationPointDataValues;
		this.gravAccPointDataValues = gravAccPointDataValues;
		this.gravPotPointDataValues = gravPotPointDataValues;
		this.slopePointDataValues = slopePointDataValues;

		initializeLocators();
	}

	private void initialize(File modelFile)
	{
		smallBodyReader.SetFileName(modelFile.getAbsolutePath());
		smallBodyReader.Update();

		smallBodyPolyData.DeepCopy(smallBodyReader.GetOutput());

		initializeLocators();
		
		//this.computeLargestSmallestEdgeLength();
		//this.computeSurfaceArea();
	}

	private void initializeLocators()
	{
		if (cellLocator == null)
		{
			cellLocator = new vtksbCellLocator();
			pointLocator = new vtkPointLocator();
		}
		
		// Initialize the cell locator
		cellLocator.FreeSearchStructure();
		cellLocator.SetDataSet(smallBodyPolyData);
		cellLocator.CacheCellBoundsOn();
		cellLocator.AutomaticOn();
		//cellLocator.SetMaxLevel(10);
		//cellLocator.SetNumberOfCellsPerNode(5);
		cellLocator.BuildLocator();

		pointLocator.FreeSearchStructure();
		pointLocator.SetDataSet(smallBodyPolyData);
		pointLocator.BuildLocator();
	}
	
	private void initializeLowResData()
	{
        if (lowResPointLocator == null)
        {
            lowResSmallBodyPolyData = new vtkPolyData();
            smallBodyReader.SetFileName(defaultModelFile.getAbsolutePath());
            smallBodyReader.Update();

            lowResSmallBodyPolyData.DeepCopy(smallBodyReader.GetOutput());
            
            lowResPointLocator = new vtkPointLocator();
            lowResPointLocator.SetDataSet(lowResSmallBodyPolyData);
            lowResPointLocator.BuildLocator();
        }
	}
	
	public vtkPolyData getSmallBodyPolyData()
	{
		return smallBodyPolyData;
	}
	
    public vtkPolyData getLowResSmallBodyPolyData()
    {
        initializeLowResData();
        
        return smallBodyPolyData;
    }
    
	public vtksbCellLocator getCellLocator()
	{
		return cellLocator;
	}
	
	public vtkAbstractPointLocator getPointLocator()
	{
		return pointLocator;
	}
	
	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		if (smallBodyCubes == null)
		{
			smallBodyCubes = new SmallBodyCubes(
			        getLowResSmallBodyPolyData(),
			        this.getIntersectingCubes());
		}
		
		return smallBodyCubes.getIntersectingCubes(polydata);
	}
	
	public void setShowSmallBody(boolean show)
	{
		if (show)
		{
			if (!smallBodyActors.contains(smallBodyActor))
			{
				smallBodyActors.add(smallBodyActor);
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (smallBodyActors.contains(smallBodyActor))
			{
				smallBodyActors.remove(smallBodyActor);
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}
		
	}

	public vtkPolyData computeFrustumIntersection(
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		return PolyDataUtil.ComputeFrustumIntersection.func(smallBodyPolyData, cellLocator, pointLocator, origin, ul, ur, lr, ll);
	}

	/**
	 * Given 2 points on the surface of the body, draw a nice looking path between the 2 
	 * that is not obscured anywhere or too distant from the surface. Return this
	 * path as a vtkPolyData
	 * @param pt1
	 * @param pt2
	 * @return
	 */
	public vtkPolyData drawPath(
			double[] pt1,
			double[] pt2)
	{
		return PolyDataUtil.DrawPathOnPolyData.func(smallBodyPolyData, pointLocator, pt1, pt2);
	}

	public void drawPolygon(
			double[] center,
			double radius,
			int numberOfSides,
			vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
	{
		PolyDataUtil.DrawPolygonOnPolyData.func(
				smallBodyPolyData,
				pointLocator,
				center,
				radius,
				numberOfSides,
				outputInterior,
				outputBoundary);
	}

	public void drawPolygonLowRes(
			double[] center,
			double radius,
			int numberOfSides,
			vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
	{
		if (resolutionLevel == 0)
		{
			drawPolygon(center, radius, numberOfSides, outputInterior, outputBoundary);
			return;
		}

		initializeLowResData();

		PolyDataUtil.DrawPolygonOnPolyData.func(
				lowResSmallBodyPolyData,
				lowResPointLocator,
				center,
				radius,
				numberOfSides,
				outputInterior,
				outputBoundary);
	}

	public void drawCone(
			double[] vertex,
			double[] axis,
			double angle,
			int numberOfSides,
			vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
	{
		PolyDataUtil.DrawConeOnPolyData.func(
				smallBodyPolyData,
				pointLocator,
				vertex,
				axis,
				angle,
				numberOfSides,
				outputInterior,
				outputBoundary);
	}
	
	public void shiftPolyLineInNormalDirection(
			vtkPolyData polyLine,
			double shiftFactor)
	{
		PolyDataUtil.ShiftPolyLineInNormalDirectionOfPolyData.func(
				polyLine,
				smallBodyPolyData,
				pointLocator,
				shiftFactor * getMinShiftAmount());
	}
	
	/**
	 * This returns the closest point to the model to pt. Note the returned point need
	 * not be a vertex of the model and can lie anywhere on a plate.
	 * @param pt
	 * @return
	 */
	public double[] findClosestPoint(double[] pt)
	{
		double[] closestPoint = new double[3];
		int[] cellId = new int[1];
		int[] subId = new int[1];
		double[] dist2 = new double[1];
		
		cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);
		
		return closestPoint;
	}
	
    /**
     * This returns the index of the closest cell in the model to pt.
     * The closest point within the cell is returned in closestPoint
     * @param pt
     * @param closestPoint the closest point within the cell is returned here
     * @return
     */
    public int findClosestCell(double[] pt, double[] closestPoint)
    {
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];
     
        // Use FindClosestPoint rather the FindCell since not sure what tolerance to use in the latter.
        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);
        
        return cellId[0];
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     * @param pt
     * @return
     */
    public int findClosestCell(double[] pt)
    {
        double[] closestPoint = new double[3];
        return findClosestCell(pt, closestPoint);
    }
    
    /**
     * Compute the point on the asteroid that has the specified latitude and longitude. Returns the
     * cell id of the cell containing that point. This is done by shooting a ray from the origin in the
     * specified direction.  
     * @param lat
     * @param lon
     * @param intersectPoint
     * @return the cellId of the cell containing the intersect point
     */
    public int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint)
    {
    	LatLon lla = new LatLon(lat, lon);
    	double[] lookPt = MathUtil.latrec(lla);
    	
    	// Move in the direction of lookPt until we are definitely outside the asteroid
    	BoundingBox bb = getBoundingBox();
    	double largestSide = bb.getLargestSide() * 1.1;
    	lookPt[0] *= largestSide;
    	lookPt[1] *= largestSide;
    	lookPt[2] *= largestSide;
    	
    	double[] origin = {0.0, 0.0, 0.0};
		double tol = 1e-6;
		double[] t = new double[1];
		double[] x = new double[3];
		double[] pcoords = new double[3];
		int[] subId = new int[1];
		int[] cellId = new int[1];
		
		int result = cellLocator.IntersectWithLine(origin, lookPt, tol, t, x, pcoords, subId, cellId, genericCell);
		
		intersectPoint[0] = x[0];
		intersectPoint[1] = x[1];
		intersectPoint[2] = x[2];
		
		if (result > 0)
			return cellId[0];
		else
			return -1;
    }

    protected void initializeActorsAndMappers()
    {
		if (smallBodyActor == null)
		{
	        smallBodyMapper = new vtkPolyDataMapper();
	        smallBodyMapper.SetInput(smallBodyPolyData);
			vtkLookupTable lookupTable = new vtkLookupTable();
			smallBodyMapper.SetLookupTable(lookupTable);
	        smallBodyMapper.UseLookupTableScalarRangeOn();
			
	        smallBodyActor = new vtkActor();
	        smallBodyActor.SetMapper(smallBodyMapper);
	        smallBodyActor.GetProperty().SetInterpolationToGouraud();
	        
	        smallBodyActors.add(smallBodyActor);
	        
	        scalarBarActor = new vtkScalarBarActor();
	        scalarBarActor.GetPositionCoordinate().SetCoordinateSystemToNormalizedViewport();
	        scalarBarActor.GetPositionCoordinate().SetValue(0.2, 0.01);
	        scalarBarActor.SetOrientationToHorizontal();
	        scalarBarActor.SetWidth(0.6);
	        scalarBarActor.SetHeight(0.1275);
	        vtkTextProperty tp = new vtkTextProperty();
	        tp.SetFontSize(10);
	        scalarBarActor.SetTitleTextProperty(tp);
		}
    }
    
	public ArrayList<vtkProp> getProps() 
	{
		initializeActorsAndMappers();
		
		return smallBodyActors;
	}
	
	public void setShadingToFlat()
	{
		initializeActorsAndMappers();
		
        smallBodyActor.GetProperty().SetInterpolationToFlat();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public void setShadingToSmooth()
	{
		initializeActorsAndMappers();

		smallBodyActor.GetProperty().SetInterpolationToGouraud();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public BoundingBox getBoundingBox()
	{
		if (boundingBox == null)
		{
			smallBodyPolyData.ComputeBounds();
			boundingBox = new BoundingBox(smallBodyPolyData.GetBounds());
		}
		
		return boundingBox;
		
		/*
		BoundingBox bb = new BoundingBox();
		vtkPoints points = smallBodyPolyData.GetPoints();
		int numberPoints = points.GetNumberOfPoints();
		for (int i=0; i<numberPoints; ++i)
		{
			double[] pt = points.GetPoint(i);
			bb.update(pt[0], pt[1], pt[2]);
		}
		
		return bb;
		*/
	}
	
	public double getBoundingBoxDiagonalLength()
	{
		return getBoundingBox().getDiagonalLength();
	}
	
	/**
	 * Get the minimum shift amount needed so shift an object away from
	 * the model so it is not obscured by the model and looks like it's
	 * laying on the model
	 * @return
	 */
	public double getMinShiftAmount()
	{
		return getBoundingBoxDiagonalLength() / 38660.0;
	}
	
    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
    	float value = 0;

    	// Coloring is currently only supported in the lowest resolution level
    	if (resolutionLevel == 0)
    	{
    		switch(coloringType)
    		{
    		case ELEVATION:
    			value = (float)PolyDataUtil.InterpolateWithinCell.func(
    					smallBodyPolyData, elevationPointDataValues, cellId, pickPosition);
    			return ElevStr + ": " + value + " " + ElevUnitsStr;
    		case GRAVITATIONAL_ACCELERATION:
    			value = (float)PolyDataUtil.InterpolateWithinCell.func(
    					smallBodyPolyData, gravAccPointDataValues, cellId, pickPosition);
    			return GravAccStr + ": " + value + " " + GravAccUnitsStr;
    		case GRAVITATIONAL_POTENTIAL:
    			value = (float)PolyDataUtil.InterpolateWithinCell.func(
    					smallBodyPolyData, gravPotPointDataValues, cellId, pickPosition);
    			return GravPotStr + ": " + value + " " + GravPotUnitsStr;
    		case SLOPE:
    			value = (float)PolyDataUtil.InterpolateWithinCell.func(
    					smallBodyPolyData, slopePointDataValues, cellId, pickPosition);
    			return SlopeStr + ": " + value + "\u00B0"; //(\u00B0 is the unicode degree symbol)
    		}
    	}
		return "";
    }

    public double[] computeLargestSmallestMeanEdgeLength()
    {
    	double[] largestSmallestMean = new double[3];
    	
    	double minLength = Double.MAX_VALUE;
    	double maxLength = 0.0;
    	double meanLength = 0.0;

    	int numberOfCells = smallBodyPolyData.GetNumberOfCells();
		
    	System.out.println(numberOfCells);

		for (int i=0; i<numberOfCells; ++i)
    	{
			vtkPoints points = smallBodyPolyData.GetCell(i).GetPoints();
    		double[] pt0 = points.GetPoint(0);
    		double[] pt1 = points.GetPoint(1);
    		double[] pt2 = points.GetPoint(2);
    		double dist0 = MathUtil.distanceBetween(pt0, pt1);
    		double dist1 = MathUtil.distanceBetween(pt1, pt2);
    		double dist2 = MathUtil.distanceBetween(pt2, pt0);
    		if (dist0 < minLength)
    			minLength = dist0;
    		if (dist0 > maxLength)
    			maxLength = dist0;
    		if (dist1 < minLength)
    			minLength = dist1;
    		if (dist1 > maxLength)
    			maxLength = dist1;
    		if (dist2 < minLength)
    			minLength = dist2;
    		if (dist2 > maxLength)
    			maxLength = dist2;
    		
    		meanLength += (dist0 + dist1 + dist2);
    	}
		
		meanLength /= ((double)(numberOfCells * 3));
		
		System.out.println("minLength  " + minLength);
		System.out.println("maxLength  " + maxLength);
		System.out.println("meanLength  " + meanLength);
		
		largestSmallestMean[0] = minLength;
		largestSmallestMean[1] = maxLength;
		largestSmallestMean[2] = meanLength;

		return largestSmallestMean;
    }

    public void computeSurfaceArea()
    {
    	vtkMassProperties massProp = new vtkMassProperties();
    	massProp.SetInput(smallBodyPolyData);
    	massProp.Update();
    	
    	System.out.println("Surface area " + massProp.GetSurfaceArea());
    	System.out.println("Volume " + massProp.GetVolume());
    }
    
    public void setModelResolution(int level) throws IOException
    {
    	if (level == resolutionLevel)
    		return;
    	
    	resolutionLevel = level;
    	if (level < 0)
    		resolutionLevel = 0;
    	else if (level > 3)
    		resolutionLevel = 3;
    	
    	smallBodyCubes = null;
		elevationPointDataValues = null;
		gravAccPointDataValues = null;
		gravPotPointDataValues = null;
		slopePointDataValues = null;
	
		File smallBodyFile = defaultModelFile;
		switch(level)
		{
		case 1:
			smallBodyFile = FileCache.getFileFromServer(modelFiles[1]);
			break;
		case 2:
			smallBodyFile = FileCache.getFileFromServer(modelFiles[2]);
			break;
		case 3:
			smallBodyFile = FileCache.getFileFromServer(modelFiles[3]);
			break;
		}

		if (resolutionLevel != 0)
			setColorBy(ColoringType.NONE);
		
		this.initialize(smallBodyFile);
		
		this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public int getModelResolution()
    {
    	return resolutionLevel;
    }
    
    public int getNumberResolutionLevels()
	{
		return modelFiles.length;
	}

    public String getModelName()
    {
    	if (resolutionLevel >= 0 && resolutionLevel < 4)
    		return modelNames[resolutionLevel];
    	else
    		return null;
    }
    
	/**
	 * This file loads the coloring used when the coloring method is point data
	 * @throws IOException
	 */
	private void loadColoringPointData() throws IOException
	{
		if (elevationPointDataValues == null)
		{
			elevationPointDataValues = new vtkFloatArray();
			gravAccPointDataValues = new vtkFloatArray();
			gravPotPointDataValues = new vtkFloatArray();
			slopePointDataValues = new vtkFloatArray();
		}
		else
		{
			return;
		}
		
		vtkFloatArray[] arrays = {
				this.elevationPointDataValues,
				this.gravAccPointDataValues,
				this.gravPotPointDataValues,
				this.slopePointDataValues
		};
		
		for (int i=0; i<4; ++i)
		{
			File file = FileCache.getFileFromServer(coloringFiles[i]);
			vtkFloatArray array = arrays[i];
			
			array.SetNumberOfComponents(1);
			array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfPoints());
			
	    	FileInputStream fs =  new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fs);
			BufferedReader in = new BufferedReader(isr);

			String line;
			int j = 0;
			while ((line = in.readLine()) != null)
			{
				array.SetTuple1(j, Float.parseFloat(line));
				++j;
			}
			
			in.close();
		}
	}

	/**
	 * This file loads the coloring used when the coloring method is texture
	 * @throws IOException
	 */
	private void loadColoringTexture() throws IOException
	{
		if (elevationImage == null)
		{
			loadColoringPointData();
			
			elevationImage = new vtkImageData();
			gravAccImage = new vtkImageData();
			gravPotImage = new vtkImageData();
			slopeImage = new vtkImageData();
		}
		else
		{
			return;
		}
		
		vtkImageData[] images = {
				this.elevationImage,
				this.gravAccImage,
				this.gravPotImage,
				this.slopeImage
		};
		vtkFloatArray[] arrays = {
				this.elevationPointDataValues,
				this.gravAccPointDataValues,
				this.gravPotPointDataValues,
				this.slopePointDataValues
		};
		
		for (int i=0; i<4; ++i)
		{
			int length = coloringFiles[i].length();
			File file = FileCache.getFileFromServer(coloringFiles[i].substring(0, length-6) + "vti");
			
			vtkImageData image = images[i];

	    	vtkXMLImageDataReader reader = new vtkXMLImageDataReader();
	    	reader.SetFileName(file.getAbsolutePath());
	    	reader.Update();
	    	
	    	vtkLookupTable lookupTable = new vtkLookupTable();
	        lookupTable.SetRange(arrays[i].GetRange());
	        lookupTable.Build();
	        invertLookupTableCharArray(lookupTable.GetTable());
	        
	    	vtkImageMapToColors mapToColors = new vtkImageMapToColors();
	        mapToColors.SetInputConnection(reader.GetOutputPort());
	        mapToColors.SetLookupTable(lookupTable);
	        mapToColors.SetOutputFormatToRGB();
	        mapToColors.Update();
	        
	    	image.DeepCopy(mapToColors.GetOutput());
		}
	}

	private void invertLookupTableCharArray(vtkUnsignedCharArray table)
	{
		int numberOfValues = table.GetNumberOfTuples();
		for (int i=0; i<numberOfValues/2; ++i)
		{
			double[] v1 = table.GetTuple4(i);
			double[] v2 = table.GetTuple4(numberOfValues-i-1);
			table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
			table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
		}
	}
	
	/**
	 * Invert the lookup table so that red is high values 
	 * and blue is low values (rather than the reverse).
	 */
	private void invertLookupTable()
	{
		vtkUnsignedCharArray table = ((vtkLookupTable)smallBodyMapper.GetLookupTable()).GetTable();
		
		invertLookupTableCharArray(table);
//		int numberOfValues = table.GetNumberOfTuples();
//		for (int i=0; i<numberOfValues/2; ++i)
//		{
//			double[] v1 = table.GetTuple4(i);
//			double[] v2 = table.GetTuple4(numberOfValues-i-1);
//			table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
//			table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
//		}
		
		((vtkLookupTable)smallBodyMapper.GetLookupTable()).SetTable(table);
		smallBodyMapper.Modified();
	}
	
	public void setColorBy(ColoringType type) throws IOException
	{
    	// Coloring is currently only supported in the lowest resolution level
		if (resolutionLevel == 0)
		{
			coloringType = type;

			paintBody();
		}
	}
	
    public boolean isColoringDataAvailable()
    {
    	return coloringFiles != null && coloringFiles.length == 4;
    }
    
    private void blendImageMapWithColoring()
    {
    	vtkImageData image = null;
    	
		switch(coloringType)
		{
		case ELEVATION:
			image = this.elevationImage;
			scalarBarActor.SetTitle(ElevStr + " (" + ElevUnitsStr + ")");
			break;
		case GRAVITATIONAL_ACCELERATION:
			image = this.gravAccImage;
			scalarBarActor.SetTitle(GravAccStr + " (" + GravAccUnitsStr + ")");
			break;
		case GRAVITATIONAL_POTENTIAL:
			image = this.gravPotImage;
			scalarBarActor.SetTitle(GravPotStr + " (" + GravPotUnitsStr + ")");
			break;
		case SLOPE:
			image = this.slopeImage;
			scalarBarActor.SetTitle(SlopeStr + " (" + SlopeUnitsStr + ")");
			break;
		}

		if (blendFilter == null)
		{
			blendFilter = new vtkImageBlend();
			blendFilter.AddInput(originalImageMap);
			blendFilter.AddInput(image);
		}
		else
		{
			blendFilter.SetInput(0, originalImageMap);
			blendFilter.SetInput(1, image);
			blendFilter.Modified();
		}
        blendFilter.SetOpacity(1, 1.0 - imageMapOpacity);
        blendFilter.Update();
        
        displayedImageMap.DeepCopy(blendFilter.GetOutput());
    }
    
    private void generateTextureCoordinates()
    {
        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = smallBodyPolyData.GetNumberOfPoints();

		textureCoords.SetNumberOfComponents(2);
		textureCoords.SetNumberOfTuples(numberOfPoints);

		vtkPoints points = smallBodyPolyData.GetPoints();
    
		for (int i=0; i<numberOfPoints; ++i)
		{
			double[] pt = points.GetPoint(i);

			LatLon ll = MathUtil.reclat(pt);
	
			double u = ll.lon;
			if (u < 0.0)
				u += 2.0 * Math.PI;
			u /= 2.0 * Math.PI;
			double v = (ll.lat + Math.PI/2.0) / Math.PI;
			
			if (u < 0.0) u = 0.0;
			else if (u > 1.0) u = 1.0;
			if (v < 0.0) v = 0.0;
			else if (v > 1.0) v = 1.0;

			textureCoords.SetTuple2(i, u, v);
		}

		/*
		// The plates that cross the zero longitude meridian might be messed up
		// since some of the points might be to the left and some to right. Fix
		// this here to make all the points either on the left or on the right.
		int numberOfCells = smallBodyPolyData.GetNumberOfCells();
		for (int i=0; i<numberOfCells; ++i)
    	{
    		int id0 = smallBodyPolyData.GetCell(i).GetPointId(0);
    		int id1 = smallBodyPolyData.GetCell(i).GetPointId(1);
    		int id2 = smallBodyPolyData.GetCell(i).GetPointId(2);
    		double[] lon = new double[3];
    		lon[0] = textureCoords.GetTuple2(id0)[0];
    		lon[1] = textureCoords.GetTuple2(id1)[0];
    		lon[2] = textureCoords.GetTuple2(id2)[0];
    		
    		if ( Math.abs(lon[0] - lon[1]) > 0.5 ||
    			 Math.abs(lon[1] - lon[2]) > 0.5 ||
    			 Math.abs(lon[2] - lon[0]) > 0.5)
    		{
        		double[] lat = new double[3];
        		lat[0] = textureCoords.GetTuple2(id0)[1];
        		lat[1] = textureCoords.GetTuple2(id1)[1];
        		lat[2] = textureCoords.GetTuple2(id2)[1];

        		System.out.println("messed up " + i);
    			System.out.println(lon[0] - lon[1]);
    			System.out.println(lon[1] - lon[2]);
    			System.out.println(lon[2] - lon[0]);
    			// First determine which side we're on
    			
    			// Do this by first determining which point is the greatest distance from the
    			// meridian
    			double[] dist = {-1000.0, -1000.0, -1000.0};
    			int maxIdx = -1;
    			double maxDist = -1000.0;
    			for (int j=0; j<3; ++j)
    			{
    				if (lon[j] > 0.5)
    					dist[j] = 1.0 - lon[j];
    				else
    					dist[j] = lon[j];
    				
    				if (dist[j] > maxDist)
    				{
    					maxDist = dist[j];
    					maxIdx = j;
    				}
    			}
    			
    			if (lon[maxIdx] < 0.5) // If true, we're on left
    			{
    				// Make sure all the coordinates are on left
    				for (int j=0; j<3; ++j)
        			{
        				if (lon[j] > 0.5)
        					lon[j] = 0.0;
        			}
    			}
    			else
    			{
    				// Make sure all the coordinates are on right
    				for (int j=0; j<3; ++j)
        			{
        				if (lon[j] < 0.5)
        					lon[j] = 1.0;
        			}
    			}
				textureCoords.SetTuple2(id0, lon[0], lat[0]);
				textureCoords.SetTuple2(id1, lon[1], lat[1]);
				textureCoords.SetTuple2(id2, lon[2], lat[2]);
    		}
    	}
    	*/
    	
		smallBodyPolyData.GetPointData().SetTCoords(textureCoords);
    }

    public boolean isImageMapAvailable()
    {
    	return imageMapName != null;
    }
    
    public void setShowImageMap(boolean b)
    {
    	showImageMap = b;

    	try
		{
			paintBody();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }

    private double getColoringValue(double[] pt, vtkFloatArray pointData) throws IOException
    {
        double[] closestPoint = new double[3];
    	int cellId = findClosestCell(pt, closestPoint);
    	return PolyDataUtil.InterpolateWithinCell.func(
    			smallBodyPolyData, pointData, cellId, closestPoint);
    }
    
    /**
     * Get the elevation value at a particular point
     * @param pt
     * @return elevation
     * @throws IOException 
     */
    public double getElevation(double[] pt) throws IOException
    {
    	if (elevationPointDataValues == null)
    		loadColoringPointData();
    	
    	return getColoringValue(pt, elevationPointDataValues);
    }

    /**
     * Get the gravitational acceleration value at a particular point
     * @param pt
     * @return gravitational acceleration
     * @throws IOException 
     */
    public double getGravitationalAcceleration(double[] pt) throws IOException
    {
    	if (gravAccPointDataValues == null)
    		loadColoringPointData();

    	return getColoringValue(pt, gravAccPointDataValues);
    }

    /**
     * Get the gravitational potential value at a particular point
     * @param pt
     * @return gravitational potential
     * @throws IOException 
     */
    public double getGravitationalPotential(double[] pt) throws IOException
    {
    	if (gravPotPointDataValues == null)
    		loadColoringPointData();

    	return getColoringValue(pt, gravPotPointDataValues);
    }

    /**
     * Get the slope value at a particular point
     * @param pt
     * @return slope
     * @throws IOException 
     */
    public double getSlope(double[] pt) throws IOException
    {
    	if (slopePointDataValues == null)
    		loadColoringPointData();

    	return getColoringValue(pt, slopePointDataValues);
    }

	public double getImageMapOpacity()
	{
		return imageMapOpacity;
	}

	public void setImageMapOpacity(double imageMapOpacity)
	{
		this.imageMapOpacity = imageMapOpacity;
		try
		{
			paintBody();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void paintBody() throws IOException
	{
		if (resolutionLevel != 0)
			return;

		initializeActorsAndMappers();

		loadColoringPointData();

		vtkFloatArray array = null;

		switch(coloringType)
		{
		case NONE:
			array = null;
			break;
		case ELEVATION:
			array = this.elevationPointDataValues;
			scalarBarActor.SetTitle(ElevStr + " (" + ElevUnitsStr + ")");
			break;
		case GRAVITATIONAL_ACCELERATION:
			array = this.gravAccPointDataValues;
			scalarBarActor.SetTitle(GravAccStr + " (" + GravAccUnitsStr + ")");
			break;
		case GRAVITATIONAL_POTENTIAL:
			array = this.gravPotPointDataValues;
			scalarBarActor.SetTitle(GravPotStr + " (" + GravPotUnitsStr + ")");
			break;
		case SLOPE:
			array = this.slopePointDataValues;
			scalarBarActor.SetTitle(SlopeStr + " (" + SlopeUnitsStr + ")");
			break;
		}

		this.smallBodyPolyData.GetPointData().SetScalars(array);
		if (coloringType == ColoringType.NONE)
		{
			if (smallBodyActors.contains(scalarBarActor))
				smallBodyActors.remove(scalarBarActor);
		}
		else
		{
			smallBodyMapper.GetLookupTable().SetRange(array.GetRange());
			((vtkLookupTable)smallBodyMapper.GetLookupTable()).ForceBuild();
			this.invertLookupTable();

			if (!smallBodyActors.contains(scalarBarActor))
				smallBodyActors.add(scalarBarActor);

			scalarBarActor.SetLookupTable(smallBodyMapper.GetLookupTable());
		}


		if (showImageMap == false)
		{
			smallBodyActor.SetTexture(null);

			if (coloringType == ColoringType.NONE)
			{
				smallBodyMapper.ScalarVisibilityOff();
				smallBodyMapper.SetScalarModeToDefault();
			}
			else
			{
				smallBodyMapper.ScalarVisibilityOn();
				smallBodyMapper.SetScalarModeToUsePointData();
			}
		}
		else 
		{
			if (resolutionLevel != 0)
				return;

			if (originalImageMap == null)
			{
				File imageFile = FileCache.getFileFromServer(imageMapName);
				vtkPNGReader reader = new vtkPNGReader();
				reader.SetFileName(imageFile.getAbsolutePath());
				reader.Update();

				originalImageMap = new vtkImageData();
				originalImageMap.DeepCopy(reader.GetOutput());
			}	    	

	    	if (displayedImageMap == null)
	    	{
	    		displayedImageMap = new vtkImageData();
	    	}

	    	if (imageMapTexture == null)
	    	{
	    		imageMapTexture = new vtkTexture();
	    		imageMapTexture.InterpolateOn();
	    		imageMapTexture.RepeatOff();
	    		imageMapTexture.EdgeClampOn();
	    		imageMapTexture.SetInput(displayedImageMap);

    			generateTextureCoordinates();
	    	}

    		smallBodyActor.SetTexture(imageMapTexture);

			smallBodyMapper.ScalarVisibilityOff();
			smallBodyMapper.SetScalarModeToDefault();

			if (coloringType == ColoringType.NONE)
			{
				displayedImageMap.DeepCopy(originalImageMap);
			}
			else
			{
				loadColoringTexture();
				blendImageMapWithColoring();
			}
		}

		this.smallBodyPolyData.Modified();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public int[] getIntersectingCubes()
	{
	    return null;
	}
}
