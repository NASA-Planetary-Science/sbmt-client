package edu.jhuapl.near.dbgen;

import java.io.File;

import vtk.*;

import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.model.MSIImage;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class MSIBoundaryGenerator 
{
	private static ErosModel erosModel;

	/**
	 * This program takes a path to a FIT image and generates a vtk file containing the
	 * boundary of the image in the same directory as the original file. 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		erosModel = new ErosModel();

		String filename = args[0];
		
		if (!filename.toLowerCase().endsWith(".fit"))
		{
			System.err.println("The specified file does not have the right extension.");
			System.exit(0);
		}
		
		File fitfile = new File(args[0]);
		
		try 
		{
		
			MSIImage image = new MSIImage(fitfile, erosModel);
			
			vtkPolyData boundary = image.generateImageBorder();

			if (boundary == null)
			{
				System.err.println("Error: Boundary generation failed");
				System.exit(0);
			}
			
	        String vtkfile = filename.substring(0, filename.length()-4) + "_BOUNDARY.VTK";

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
