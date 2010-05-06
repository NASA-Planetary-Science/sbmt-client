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
import edu.jhuapl.near.util.PolyDataUtil;
import edu.jhuapl.near.util.Properties;

public class ErosModel extends Model 
{
    private vtkPolyData erosPolyData;
    private vtkActor erosActor;
    private vtkPolyDataMapper erosMapper;
    private boolean showLighting = true;
    private ArrayList<vtkProp> erosActors = new ArrayList<vtkProp>();
    private vtkOBBTree cellLocator;
    private vtkKdTreePointLocator pointLocator;
    private vtkFloatArray elevationValues;
    private vtkFloatArray gravAccValues;
    private vtkFloatArray gravPotValues;
    private vtkFloatArray slopeValues;
    private vtkScalarBarActor scalarBarActor;
    private vtkPolyDataReader erosReader;
    private vtkPolyDataNormals normalsFilter;
	private ErosCubes erosCubes;

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
    
    static public String ElevStr = "Elevation";
    static public String GravAccStr = "Gravitational Acceleration";
    static public String GravPotStr = "Gravitational Potential";
    static public String SlopeStr = "Slope";
    static public String ElevUnitsStr = "m";
    static public String GravAccUnitsStr = "m/s^2";
    static public String GravPotUnitsStr = "J/kg";
    static public String SlopeUnitsStr = "deg";
    static public String FlatShadingStr = "Flat";
    static public String SmoothShadingStr = "Smooth";
    static public String LowResModelStr = "Low (49152 plates)";
    static public String MedResModelStr = "Medium (196608 plates)";
    static public String HighResModelStr = "High (786432 plates)";
    static public String VeryHighResModelStr = "Very High (3145728 plates)";
    
    private ColoringType coloringType = ColoringType.NONE;
    
	public ErosModel()
	{
    	erosReader = new vtkPolyDataReader();
		normalsFilter = new vtkPolyDataNormals();
		erosPolyData = new vtkPolyData();
		cellLocator = new vtkOBBTree();
		pointLocator = new vtkKdTreePointLocator();
		
		initialize();
	}
	
	private void initialize()
	{
		File file = ConvertResourceToFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
		//File file = ConvertResourceToFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/ver512q.vtk");
		erosReader.SetFileName(file.getAbsolutePath());
		erosReader.Update();

		normalsFilter.SetInputConnection(erosReader.GetOutputPort());
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.Update();

		erosPolyData.DeepCopy(normalsFilter.GetOutput());
		//erosPolyData = erosReader.GetOutput();

		// Initialize the cell locator
		cellLocator.SetDataSet(erosReader.GetOutput());
		cellLocator.CacheCellBoundsOn();
		cellLocator.AutomaticOn();
		//cellLocator.SetMaxLevel(10);
		//cellLocator.SetNumberOfCellsPerNode(5);
		cellLocator.BuildLocator();

		pointLocator.SetDataSet(erosReader.GetOutput());
		pointLocator.BuildLocator();

		//this.computeLargestSmallestEdgeLength();
		//this.computeSurfaceArea();
	}
	
	public vtkPolyData getErosPolyData()
	{
		return erosPolyData;
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
		if (coloringType == type)
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
    
    public void setModelResolution(int level)
    {
    	erosCubes = null;
		elevationValues = null;
		gravAccValues = null;
		gravPotValues = null;
		slopeValues = null;
    }
}
