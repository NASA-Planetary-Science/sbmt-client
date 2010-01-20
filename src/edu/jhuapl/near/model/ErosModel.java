package edu.jhuapl.near.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import vtk.*;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.ConvertResourceToFile;
import edu.jhuapl.near.util.IntersectionUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Properties;
import edu.jhuapl.near.util.Spice;

public class ErosModel extends Model 
{
    private vtkPolyData erosPolyData;
    private vtkActor erosActor;
    private boolean showLighting = true;
    private ArrayList<vtkActor> erosActors = new ArrayList<vtkActor>();
    //private vtkCellLocator locator;
    private vtkOBBTree locator;
    private vtkPoints intersectPoints;
    private vtkFloatArray elevationValues;
    private vtkFloatArray gravAccValues;
    private vtkFloatArray gravPotValues;
    private vtkFloatArray slopeValues;
    
    public enum ColoringType { 
    	NONE, 
    	ELEVATION, 
    	GRAVITATIONAL_ACCELERATION,
    	GRAVITATIONAL_POTENTIAL,
    	SLOPE
    }
    
    private ColoringType coloringType;
    
	public ErosModel()
	{
    	vtkPolyDataReader erosReader = new vtkPolyDataReader();
        File file = ConvertResourceToFile.convertResourceToTempFile(this, "/edu/jhuapl/near/data/Eros_Dec2006_0.vtk");
        erosReader.SetFileName(file.getAbsolutePath());
        erosReader.Update();

        erosPolyData = erosReader.GetOutput();
        
        // Initialize the cell locator
        //locator = new vtkCellLocator();
        locator = new vtkOBBTree();
        locator.SetDataSet(erosReader.GetOutput());
        locator.CacheCellBoundsOn();
        locator.AutomaticOn();
        //locator.SetMaxLevel(10);
        //locator.SetNumberOfCellsPerNode(5);

        locator.BuildLocator();

        intersectPoints = new vtkPoints();
	}
	
	public void setShowEros(boolean show)
	{
		if (show)
		{
			if (erosActors.isEmpty())
			{
				erosActors.add(erosActor);
				this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
			}
		}
		else
		{
			if (!erosActors.isEmpty())
			{
				erosActors.clear();
				this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
			}
		}
		
	}
	
	private void loadColoring(ColoringType type) throws IOException
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
			array.SetNumberOfTuples(erosPolyData.GetNumberOfPoints());
			
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
	
	public void setColorBy(ColoringType type)
	{
		if (coloringType == type)
			return;
		
		coloringType = type;
		
		switch(type)
		{
		case NONE:
			break;
		case ELEVATION:
			break;
		case GRAVITATIONAL_ACCELERATION:
			break;
		case GRAVITATIONAL_POTENTIAL:
			break;
		case SLOPE:
			break;
		}
		
		this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
	}
	
	public void setShowLighting(boolean lighting)
	{
		this.showLighting = lighting;
		this.erosActor.GetProperty().SetLighting(showLighting);
		this.pcs.firePropertyChange(Properties.EROS_MODEL_CHANGED, null, null);
	}

	/*
	 * Given only the latitude and longitude of a point on the surface of
	 * Eros, this function finds the xyz coordinates of the point by
	 * shooting out a ray from the center of Eros and returning the intersection
	 * point. If more than one point intersects Eros (unlikely), the first one is returned.
	 */
	public double[] latLonToXyz(double lat, double lon)
	{
		LatLon ll = new LatLon(lat, lon, 1.0);
		double xyz[] = Spice.latrec(ll);
		
		// Cast a ray from the origin in the direction of xyz to about 50 km out
		// which is definitely outside of Eros
		double distance = 50.0;
		
		double[] sourcePnt = {0.0, 0.0, 0.0};
		double[] destinPnt = {xyz[0]*distance, xyz[1]*distance, xyz[2]*distance};

		intersectPoints.Reset();

		locator.IntersectWithLine(sourcePnt, destinPnt, intersectPoints, null);
		
		if (intersectPoints.GetNumberOfPoints() > 0)
			return intersectPoints.GetPoint(0);
		
		return null;
	}

	public vtkPolyData computeFrustumIntersection(
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		return IntersectionUtil.computeFrustumIntersection(erosPolyData, locator, origin, ul, ur, lr, ll);
	}

	public vtkPolyData computePlaneIntersection(
			double[] origin, 
			double[] pt1,
			double[] pt2)
	{
		return IntersectionUtil.computePlaneIntersection(erosPolyData, locator, origin, pt1, pt2);
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		if (erosActor == null)
		{
	        vtkPolyDataMapper erosMapper = new vtkPolyDataMapper();
	        erosMapper.SetInput(erosPolyData);

	        erosActor = new vtkActor();
	        //erosActor.GetProperty().SetRepresentationToWireframe();
	        erosActor.SetMapper(erosMapper);

	        erosActors.add(erosActor);
		}
		
		return erosActors;
	}
	
	public BoundingBox computeBoundingBox()
	{
		BoundingBox bb = new BoundingBox();
		vtkPoints points = erosPolyData.GetPoints();
		int numberPoints = points.GetNumberOfPoints();
		for (int i=0; i<numberPoints; ++i)
		{
			double[] pt = points.GetPoint(i);
			bb.update(pt[0], pt[1], pt[2]);
		}
		
		return bb;
	}
}
