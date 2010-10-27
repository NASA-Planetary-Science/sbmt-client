package edu.jhuapl.near.dbgen;

import vtk.*;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ColoringImageMapGenerator
{

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
	    java.awt.Toolkit.getDefaultToolkit();
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		SmallBodyModel model = ModelFactory.createDeimosBodyModel();

		vtkImageData slopeImage = new vtkImageData();
		slopeImage.SetDimensions(3600, 1800, 1);
		slopeImage.SetScalarTypeToFloat();
		slopeImage.SetNumberOfScalarComponents(1);
		slopeImage.AllocateScalars();
		vtkImageData elevImage = new vtkImageData();
		elevImage.SetDimensions(3600, 1800, 1);
		elevImage.SetScalarTypeToFloat();
		elevImage.SetNumberOfScalarComponents(1);
		elevImage.AllocateScalars();
		vtkImageData gravAccImage = new vtkImageData();
		gravAccImage.SetDimensions(3600, 1800, 1);
		gravAccImage.SetScalarTypeToFloat();
		gravAccImage.SetNumberOfScalarComponents(1);
		gravAccImage.AllocateScalars();
		vtkImageData gravPotImage = new vtkImageData();
		gravPotImage.SetDimensions(3600, 1800, 1);
		gravPotImage.SetScalarTypeToFloat();
		gravPotImage.SetNumberOfScalarComponents(1);
		gravPotImage.AllocateScalars();

		double[] point = new double[3];
		int height = 1800;
		int width = 3600;
		
		for (int i=0; i<height; ++i)
		{
			System.out.println("line " + i);
			for (int j=0; j<width; ++j)
			{
				double lat = (Math.PI/180.0) * (((double)i / ((double)height-1.0)) * 180.0 - 90.0);
				double lon = (Math.PI/180.0) * (((double)j / ((double)width-1.0)) * 360.0);
				//System.out.println(i + " " + lat + " " + j + " " + lon);
				
				int cellId = model.getPointAndCellIdFromLatLon(lat, lon, point);
				
				if (cellId < 0)
				{
					throw new Exception("Problem converting lat lon");
				}
				
				double slope = model.getSlope(point);
				double elev = model.getElevation(point);
				double gravAcc = model.getGravitationalAcceleration(point);
				double gravPot = model.getGravitationalPotential(point);
				
				slopeImage.SetScalarComponentFromDouble(j, i, 0, 0, slope);
				elevImage.SetScalarComponentFromDouble(j, i, 0, 0, elev);
				gravAccImage.SetScalarComponentFromDouble(j, i, 0, 0, gravAcc);
				gravPotImage.SetScalarComponentFromDouble(j, i, 0, 0, gravPot);
			}
		}
		
		vtkXMLImageDataWriter writer = new vtkXMLImageDataWriter();
		//writer.SetDataModeToAscii();
		writer.SetDataModeToBinary();
		
		writer.SetFileName("DEIMOS_Slope_PointData.vti");
		writer.SetInput(slopeImage);
		writer.Write();
		
		writer.SetFileName("DEIMOS_Elevation_PointData.vti");
		writer.SetInput(elevImage);
		writer.Write();

		writer.SetFileName("DEIMOS_GravitationalAcceleration_PointData.vti");
		writer.SetInput(gravAccImage);
		writer.Write();

		writer.SetFileName("DEIMOS_GravitationalPotential_PointData.vti");
		writer.SetInput(gravPotImage);
		writer.Write();
	}

}
