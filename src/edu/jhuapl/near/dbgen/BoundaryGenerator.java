package edu.jhuapl.near.dbgen;

import vtk.*;

import edu.jhuapl.near.*;
import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class BoundaryGenerator 
{

	/**
	 * This program takes a path to a FIT image and generates a vtk file containing the
	 * boundary of the image in the same directory as the original file. 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		String fitfile = args[0];
		
		if (!fitfile.toLowerCase().endsWith(".fit"))
		{
			System.err.println("The specified file does not have the right extension.");
			System.exit(0);
		}
		
		try 
		{
		
			NearImage image = new NearImage(fitfile);
			
			vtkPolyData boundary = image.generateImageBorder();

			if (boundary == null)
			{
				System.err.println("Error: Boundary generation failed");
				System.exit(0);
			}
			
	        String vtkfile = fitfile.substring(0, fitfile.length()-4) + "_BOUNDARY.VTK";

	        vtkPolyDataWriter writer = new vtkPolyDataWriter();
	        writer.SetInput(boundary);
	        writer.SetFileName(vtkfile);
	        writer.SetFileTypeToBinary();
	        writer.Write();
				
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}

}
