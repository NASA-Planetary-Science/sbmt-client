package edu.jhuapl.near.model;

import java.util.ArrayList;

import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.Spice;

import vtk.vtkCutter;
import vtk.vtkPlane;
import vtk.vtkProp;

public class Graticule extends Model
{
	public Graticule(ErosModel erosModel)
	{
		double longitudeSpacing = 10.0;
		double latitudeSpacing = 10.0;
		
		int numberLonCircles = (int)(180.0/longitudeSpacing);
		int numberLatCircles = (int)(180.0/latitudeSpacing) - 1;

		double[] origin = {0.0, 0.0, 0.0};
		double[] zaxis = {0.0, 0.0, 1.0};
		
		// First do the longitudes.
		for (int i=0; i<numberLonCircles; ++i)
		{
			double lon = longitudeSpacing * (double)i * Math.PI / 180.0;
			double[] vec = Spice.latrec(new LatLon(0.0, lon, 1.0));
			double[] normal = new double[3];
			
			
			vtkPlane cutPlane = new vtkPlane();
			cutPlane.SetOrigin(origin);
			cutPlane.SetNormal(normal);

			vtkCutter cutPolyData = new vtkCutter();
			cutPolyData.SetInput(erosModel.getErosPolyData());
			cutPolyData.SetCutFunction(cutPlane);
			cutPolyData.Update();

		}
	}
	
	@Override
	public ArrayList<vtkProp> getProps()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
