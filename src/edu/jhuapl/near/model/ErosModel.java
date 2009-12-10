package edu.jhuapl.near.model;

import java.io.File;
import java.util.ArrayList;

import vtk.*;
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
	
}
