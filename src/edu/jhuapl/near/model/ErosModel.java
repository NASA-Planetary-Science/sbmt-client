package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

import vtk.*;
import edu.jhuapl.near.query.ErosCubes;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.ConvertResourceToFile;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class ErosModel extends Model 
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

    private vtkPolyData erosPolyData;
    private vtkActor erosActor;
    private vtkPolyDataMapper erosMapper;
    private boolean showLighting = true;
    private ArrayList<vtkProp> erosActors = new ArrayList<vtkProp>();
    private vtksbCellLocator cellLocator;
    private vtkKdTreePointLocator pointLocator;
    private vtkFloatArray elevationValues;
    private vtkFloatArray gravAccValues;
    private vtkFloatArray gravPotValues;
    private vtkFloatArray slopeValues;
    private vtkScalarBarActor scalarBarActor;
    private vtkPolyDataReader erosReader;
    //private vtkPolyDataNormals normalsFilter;
	private ErosCubes erosCubes;
    private ColoringType coloringType = ColoringType.NONE;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    
	public ErosModel()
	{
    	erosReader = new vtkPolyDataReader();
		//normalsFilter = new vtkPolyDataNormals();
		erosPolyData = new vtkPolyData();
		cellLocator = new vtksbCellLocator();
		pointLocator = new vtkKdTreePointLocator();
		genericCell = new vtkGenericCell();
		
		//defaultModelFile = ConvertResourceToFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
		defaultModelFile = ConvertResourceToFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/ver64q.vtk");

		initialize(defaultModelFile);
	}
	
	private void initialize(File modelFile)
	{
		erosReader.SetFileName(modelFile.getAbsolutePath());
		erosReader.Update();

		//normalsFilter.SetInputConnection(erosReader.GetOutputPort());
		//normalsFilter.SetComputeCellNormals(0);
		//normalsFilter.SetComputePointNormals(1);
		//normalsFilter.SplittingOff();
		//normalsFilter.Update();

		//erosPolyData.DeepCopy(normalsFilter.GetOutput());
		erosPolyData.DeepCopy(erosReader.GetOutput());

		//vtkPolyDataWriter writer = new vtkPolyDataWriter();
		//writer.SetInput(erosPolyData);
		//writer.SetFileName("/tmp/" + modelFile.getName());
		//writer.SetFileTypeToBinary();
		//writer.Write();


		// Initialize the cell locator
		cellLocator.FreeSearchStructure();
		cellLocator.SetDataSet(erosPolyData);
		cellLocator.CacheCellBoundsOn();
		cellLocator.AutomaticOn();
		//cellLocator.SetMaxLevel(10);
		//cellLocator.SetNumberOfCellsPerNode(5);
		cellLocator.BuildLocator();

		pointLocator.FreeSearchStructure();
		pointLocator.SetDataSet(erosPolyData);
		pointLocator.BuildLocator();

		//this.computeLargestSmallestEdgeLength();
		//this.computeSurfaceArea();
	}
	
	public vtkPolyData getErosPolyData()
	{
		return erosPolyData;
	}
	
	public vtksbCellLocator getLocator()
	{
		return cellLocator;
	}
	
	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		if (erosCubes == null)
			erosCubes = new ErosCubes(this);
			
		return erosCubes.getIntersectingCubes(polydata);
	}
	
	public void setShowEros(boolean show)
	{
		if (show)
		{
			if (!erosActors.contains(erosActor))
			{
				erosActors.add(erosActor);
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (erosActors.contains(erosActor))
			{
				erosActors.remove(erosActor);
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
		
		String[] coloringFiles = {
				"/edu/jhuapl/near/data/Eros_Dec2006_0_Elevation.txt",
				"/edu/jhuapl/near/data/Eros_Dec2006_0_GravitationalAcceleration.txt",
				"/edu/jhuapl/near/data/Eros_Dec2006_0_GravitationalPotential.txt",
				"/edu/jhuapl/near/data/Eros_Dec2006_0_Slope.txt"
		};
		vtkFloatArray[] arrays = {
				this.elevationValues,
				this.gravAccValues,
				this.gravPotValues,
				this.slopeValues
		};
		
		for (int i=0; i<4; ++i)
		{
			String file = coloringFiles[i];
			vtkFloatArray array = arrays[i];
			
			array.SetNumberOfComponents(1);
			array.SetNumberOfTuples(erosPolyData.GetNumberOfCells());
			
			InputStream is = getClass().getResourceAsStream(file);
			InputStreamReader isr = new InputStreamReader(is);
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
		vtkUnsignedCharArray table = ((vtkLookupTable)erosMapper.GetLookupTable()).GetTable();
		
		int numberOfValues = table.GetNumberOfTuples();
		for (int i=0; i<numberOfValues/2; ++i)
		{
			double[] v1 = table.GetTuple4(i);
			double[] v2 = table.GetTuple4(numberOfValues-i-1);
			table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
			table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
		}
		
		((vtkLookupTable)erosMapper.GetLookupTable()).SetTable(table);
		erosMapper.Modified();
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

		this.erosPolyData.GetCellData().SetScalars(array);
		if (type == ColoringType.NONE)
		{
			erosMapper.ScalarVisibilityOff();
			erosMapper.SetScalarModeToDefault();
			if (erosActors.contains(scalarBarActor))
				erosActors.remove(scalarBarActor);
		}
		else
		{
			erosMapper.ScalarVisibilityOn();
			erosMapper.SetScalarModeToUseCellData();
			erosMapper.GetLookupTable().SetRange(array.GetRange());
			((vtkLookupTable)erosMapper.GetLookupTable()).ForceBuild();
			this.invertLookupTable();
			
			if (!erosActors.contains(scalarBarActor))
				erosActors.add(scalarBarActor);
			
			scalarBarActor.SetLookupTable(erosMapper.GetLookupTable());
		}
		this.erosPolyData.Modified();
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public void setShowLighting(boolean lighting)
	{
		this.showLighting = lighting;
		this.erosActor.GetProperty().SetLighting(showLighting);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public vtkPolyData computeFrustumIntersection(
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		return PolyDataUtil.computeFrustumIntersection(erosPolyData, cellLocator, origin, ul, ur, lr, ll);
	}

	/**
	 * Given 2 points on the surface of Eros, draw a nice looking path between the 2 
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
		return PolyDataUtil.drawPathOnPolyData(erosPolyData, pointLocator, pt1, pt2);
	}

	public void drawPolygon(
			double[] center,
			double radius,
			int numberOfSides,
			vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawPolygonOnPolyData(
				erosPolyData,
				pointLocator,
				center,
				radius,
				numberOfSides,
				outputInterior,
				outputBoundary);
	}

	public void shiftPolyLineInNormalDirection(
			vtkPolyData polyLine,
			double shiftAmount)
	{
		PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(polyLine, erosPolyData, shiftAmount);
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
		if (erosActor == null)
		{
	        erosMapper = new vtkPolyDataMapper();
	        erosMapper.SetInput(erosPolyData);
			vtkLookupTable lookupTable = new vtkLookupTable();
			erosMapper.SetLookupTable(lookupTable);
	        erosMapper.UseLookupTableScalarRangeOn();
			
	        erosActor = new vtkActor();
	        erosActor.SetMapper(erosMapper);
	        erosActor.GetProperty().SetInterpolationToPhong();
	        
	        erosActors.add(erosActor);
	        
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
		
		return erosActors;
	}
	
	public void setShadingToFlat()
	{
        erosActor.GetProperty().SetInterpolationToFlat();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public void setShadingToSmooth()
	{
        erosActor.GetProperty().SetInterpolationToPhong();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public BoundingBox computeBoundingBox()
	{
		erosPolyData.ComputeBounds();
		return new BoundingBox(erosPolyData.GetBounds());
		/*
		BoundingBox bb = new BoundingBox();
		vtkPoints points = erosPolyData.GetPoints();
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
    		value = (float)erosPolyData.GetCellData().GetScalars().GetTuple1(cellId);
    	
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

		System.out.println(erosPolyData.GetNumberOfCells());

		for (int i=0; i<erosPolyData.GetNumberOfCells(); ++i)
    	{
    		double[] pt0 = erosPolyData.GetCell(i).GetPoints().GetPoint(0);
    		double[] pt1 = erosPolyData.GetCell(i).GetPoints().GetPoint(1);
    		double[] pt2 = erosPolyData.GetCell(i).GetPoints().GetPoint(2);
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
    	massProp.SetInput(erosPolyData);
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
    	
    	erosCubes = null;
		elevationValues = null;
		gravAccValues = null;
		gravPotValues = null;
		slopeValues = null;
	
		File erosFile = defaultModelFile;
		switch(level)
		{
		case 1:
			erosFile = FileCache.getFileFromServer("/EROS/ver128q.vtk.gz");
			break;
		case 2:
			erosFile = FileCache.getFileFromServer("/EROS/ver256q.vtk.gz");
			break;
		case 3:
			erosFile = FileCache.getFileFromServer("/EROS/ver512q.vtk.gz");
			break;
		}

		if (resolutionLevel != 0)
			setColorBy(ColoringType.NONE);
		
		this.initialize(erosFile);
		
		this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public int getModelResolution()
    {
    	return resolutionLevel;
    }
    
    public String getModelName()
    {
		switch(resolutionLevel)
		{
		case 0:
			return "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver64q";
		case 1:
			return "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver128q";
		case 2:
			return "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver256q";
		case 3:
			return "NEAR-A-MSI-5-EROSSHAPE-V1.0 ver512q";
		}
		
		// Bug if we reach here
		return null;
    }
}
