package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

import vtk.*;
import edu.jhuapl.near.query.ErosCubes;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.ConvertResourceToFile;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.GeometryUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public abstract class SmallBodyModel extends Model 
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
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private boolean showLighting = true;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private vtksbCellLocator cellLocator;
    private vtkKdTreePointLocator pointLocator;
    private vtkFloatArray elevationValues;
    private vtkFloatArray gravAccValues;
    private vtkFloatArray gravPotValues;
    private vtkFloatArray slopeValues;
    private vtkScalarBarActor scalarBarActor;
    private vtkPolyDataReader smallBodyReader;
    //private vtkPolyDataNormals normalsFilter;
	private ErosCubes smallBodyCubes;
    private ColoringType coloringType = ColoringType.NONE;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    private String[] modelNames;
    private String[] modelFiles;
	private String[] coloringFiles;
	private String imageMap;
    private boolean showImageMap = false;
	private vtkTexture imageMapTexture;
	
	public SmallBodyModel(
			String[] modelNames,
			String[] modelFiles,
			String[] coloringFiles,
			String imageMap,
			boolean lowestResolutionModelStoredInResource)
	{
		this.modelNames = modelNames;
		this.modelFiles = modelFiles;
		this.coloringFiles = coloringFiles;
		this.imageMap = imageMap;
		
    	smallBodyReader = new vtkPolyDataReader();
		//normalsFilter = new vtkPolyDataNormals();
		smallBodyPolyData = new vtkPolyData();
		cellLocator = new vtksbCellLocator();
		pointLocator = new vtkKdTreePointLocator();
		genericCell = new vtkGenericCell();
		
		if (lowestResolutionModelStoredInResource)
			defaultModelFile = ConvertResourceToFile.convertResourceToTempFile(this, modelFiles[0]);
		else
			defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);

		initialize(defaultModelFile);
	}
	
	private void initialize(File modelFile)
	{
		smallBodyReader.SetFileName(modelFile.getAbsolutePath());
		smallBodyReader.Update();

		smallBodyPolyData.DeepCopy(smallBodyReader.GetOutput());

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

		//this.computeLargestSmallestEdgeLength();
		//this.computeSurfaceArea();
	}
	
	public vtkPolyData getSmallBodyPolyData()
	{
		return smallBodyPolyData;
	}
	
	public vtksbCellLocator getLocator()
	{
		return cellLocator;
	}
	
	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		if (smallBodyCubes == null)
			smallBodyCubes = new ErosCubes(this);
			
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
	
	private void loadColoring() throws IOException
	{
		if (elevationValues == null)
		{
			elevationValues = new vtkFloatArray();
			gravAccValues = new vtkFloatArray();
			gravPotValues = new vtkFloatArray();
			slopeValues = new vtkFloatArray();
		}
		else
		{
			return;
		}
		
		vtkFloatArray[] arrays = {
				this.elevationValues,
				this.gravAccValues,
				this.gravPotValues,
				this.slopeValues
		};
		
		for (int i=0; i<4; ++i)
		{
			File file = FileCache.getFileFromServer(coloringFiles[i]);
			vtkFloatArray array = arrays[i];
			
			array.SetNumberOfComponents(1);
			array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfCells());
			
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
		}
	}

	/**
	 * Invert the lookup table so that red is high values 
	 * and blue is low values (rather than the reverse).
	 */
	private void invertLookupTable()
	{
		vtkUnsignedCharArray table = ((vtkLookupTable)smallBodyMapper.GetLookupTable()).GetTable();
		
		int numberOfValues = table.GetNumberOfTuples();
		for (int i=0; i<numberOfValues/2; ++i)
		{
			double[] v1 = table.GetTuple4(i);
			double[] v2 = table.GetTuple4(numberOfValues-i-1);
			table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
			table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
		}
		
		((vtkLookupTable)smallBodyMapper.GetLookupTable()).SetTable(table);
		smallBodyMapper.Modified();
	}
	
	public void setColorBy(ColoringType type) throws IOException
	{
		if (coloringType == type || resolutionLevel != 0)
			return;
		
		loadColoring();
		
		coloringType = type;
		
		vtkFloatArray array = null;
		
		switch(type)
		{
		case NONE:
			array = null;
			break;
		case ELEVATION:
			array = this.elevationValues;
			scalarBarActor.SetTitle(ElevStr + " (" + ElevUnitsStr + ")");
			break;
		case GRAVITATIONAL_ACCELERATION:
			array = this.gravAccValues;
			scalarBarActor.SetTitle(GravAccStr + " (" + GravAccUnitsStr + ")");
			break;
		case GRAVITATIONAL_POTENTIAL:
			array = this.gravPotValues;
			scalarBarActor.SetTitle(GravPotStr + " (" + GravPotUnitsStr + ")");
			break;
		case SLOPE:
			array = this.slopeValues;
			scalarBarActor.SetTitle(SlopeStr + " (" + SlopeUnitsStr + ")");
			break;
		}

		this.smallBodyPolyData.GetCellData().SetScalars(array);
		if (type == ColoringType.NONE)
		{
			smallBodyMapper.ScalarVisibilityOff();
			smallBodyMapper.SetScalarModeToDefault();
			if (smallBodyActors.contains(scalarBarActor))
				smallBodyActors.remove(scalarBarActor);
		}
		else
		{
			smallBodyMapper.ScalarVisibilityOn();
			smallBodyMapper.SetScalarModeToUseCellData();
			smallBodyMapper.GetLookupTable().SetRange(array.GetRange());
			((vtkLookupTable)smallBodyMapper.GetLookupTable()).ForceBuild();
			this.invertLookupTable();
			
			if (!smallBodyActors.contains(scalarBarActor))
				smallBodyActors.add(scalarBarActor);
			
			scalarBarActor.SetLookupTable(smallBodyMapper.GetLookupTable());
		}
		this.smallBodyPolyData.Modified();
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public void setShowLighting(boolean lighting)
	{
		this.showLighting = lighting;
		this.smallBodyActor.GetProperty().SetLighting(showLighting);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public vtkPolyData computeFrustumIntersection(
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		return PolyDataUtil.computeFrustumIntersection(smallBodyPolyData, cellLocator, origin, ul, ur, lr, ll);
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
		return PolyDataUtil.drawPathOnPolyData(smallBodyPolyData, pointLocator, pt1, pt2);
	}

	public void drawPolygon(
			double[] center,
			double radius,
			int numberOfSides,
			vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawPolygonOnPolyData(
				smallBodyPolyData,
				pointLocator,
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
		PolyDataUtil.drawConeOnPolyData(
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
			double shiftAmount)
	{
		PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(polyLine, smallBodyPolyData, shiftAmount);
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
	
	public ArrayList<vtkProp> getProps() 
	{
		if (smallBodyActor == null)
		{
	        smallBodyMapper = new vtkPolyDataMapper();
	        smallBodyMapper.SetInput(smallBodyPolyData);
			vtkLookupTable lookupTable = new vtkLookupTable();
			smallBodyMapper.SetLookupTable(lookupTable);
	        smallBodyMapper.UseLookupTableScalarRangeOn();
			
	        smallBodyActor = new vtkActor();
	        smallBodyActor.SetTexture(imageMapTexture);
	        smallBodyActor.SetMapper(smallBodyMapper);
	        smallBodyActor.GetProperty().SetInterpolationToPhong();
	        
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
		
		return smallBodyActors;
	}
	
	public void setShadingToFlat()
	{
        smallBodyActor.GetProperty().SetInterpolationToFlat();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public void setShadingToSmooth()
	{
        smallBodyActor.GetProperty().SetInterpolationToPhong();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public BoundingBox computeBoundingBox()
	{
		smallBodyPolyData.ComputeBounds();
		return new BoundingBox(smallBodyPolyData.GetBounds());
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
	
    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	float value = 0;
    	if (coloringType != ColoringType.NONE)
    		value = (float)smallBodyPolyData.GetCellData().GetScalars().GetTuple1(cellId);
    	
		switch(coloringType)
		{
		case ELEVATION:
			return ElevStr + ": " + value + " " + ElevUnitsStr;
		case GRAVITATIONAL_ACCELERATION:
			return GravAccStr + ": " + value + " " + GravAccUnitsStr;
		case GRAVITATIONAL_POTENTIAL:
			return GravPotStr + ": " + value + " " + GravPotUnitsStr;
		case SLOPE:
			return SlopeStr + ": " + value + "\u00B0"; //(\u00B0 is the unicode degree symbol)
		}

		return "";
    }

    public void computeLargestSmallestEdgeLength()
    {
    	double minLength = Double.MAX_VALUE;
    	double maxLength = 0.0;

    	vtkMath math = new vtkMath();

		System.out.println(smallBodyPolyData.GetNumberOfCells());

		for (int i=0; i<smallBodyPolyData.GetNumberOfCells(); ++i)
    	{
    		double[] pt0 = smallBodyPolyData.GetCell(i).GetPoints().GetPoint(0);
    		double[] pt1 = smallBodyPolyData.GetCell(i).GetPoints().GetPoint(1);
    		double[] pt2 = smallBodyPolyData.GetCell(i).GetPoints().GetPoint(2);
    		double dist0 = Math.sqrt(math.Distance2BetweenPoints(pt0, pt1));
    		double dist1 = Math.sqrt(math.Distance2BetweenPoints(pt1, pt2));
    		double dist2 = Math.sqrt(math.Distance2BetweenPoints(pt2, pt0));
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
    	}
		
		System.out.println("minLength  " + minLength);
		System.out.println("maxLength  " + maxLength);
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
		elevationValues = null;
		gravAccValues = null;
		gravPotValues = null;
		slopeValues = null;
	
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
    
    public abstract int getNumberResolutionLevels();
    
    public String getModelName()
    {
    	if (resolutionLevel >= 0 && resolutionLevel < 4)
    		return modelNames[resolutionLevel];
    	else
    		return null;
    }
    
    public boolean isColoringDataAvailable()
    {
    	return coloringFiles != null && coloringFiles.length == 4;
    }
    
    protected void generateImageMapValuesForAllCells()
    {
    	File imageFile = FileCache.getFileFromServer(imageMap);
    	vtkPNGReader reader = new vtkPNGReader();
    	reader.SetFileName(imageFile.getAbsolutePath());
    	reader.Update();
    	
    	vtkImageData image = reader.GetOutput();
    	System.out.println(image);
    	
        vtkFloatArray textureCoords;
		textureCoords = new vtkFloatArray();

		imageMapTexture = new vtkTexture();
		imageMapTexture.InterpolateOn();
		imageMapTexture.RepeatOn();
		imageMapTexture.EdgeClampOn();
		imageMapTexture.SetInput(image);

        int numberOfPoints = smallBodyPolyData.GetNumberOfPoints();

		textureCoords.SetNumberOfComponents(2);
		textureCoords.SetNumberOfTuples(numberOfPoints);

		vtkPoints points = smallBodyPolyData.GetPoints();
    
		for (int i=0; i<numberOfPoints; ++i)
		{
			double[] pt = points.GetPoint(i);

			LatLon ll = GeometryUtil.reclat(pt);
	
			double u = ll.lon;
			if (u < 0.0)
				u += 2.0 * Math.PI;
			u /= 2.0 * Math.PI;
			double v = (ll.lat + Math.PI/2.0) / Math.PI;
			if (u == 1110.0 || u > 1.0)
				System.out.println(i);
			
			if (u < 0.0) u = 0.0;
			else if (u > 1.0) u = 1.0;
			if (v < 0.0) v = 0.0;
			else if (v > 1.0) v = 1.0;
			
			textureCoords.SetTuple2(i, u, v);
		}

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
		smallBodyPolyData.GetPointData().SetTCoords(textureCoords);
    }
    
    public void setShowImageMap(boolean b)
    {
    	showImageMap = b;
    }
}
