package edu.jhuapl.near.dbgen;

import java.io.*;

import nom.tam.fits.FitsException;
import vtk.*;

import edu.jhuapl.near.*;

public class BoundaryGenerator 
{

	/**
	 * This program takes a path to a FIT image and generates a vtk file containing the
	 * boundary of the image in the same directory as the original file. 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibraries();

		String fitfile = args[0];
		
		if (!fitfile.toLowerCase().endsWith(".fit"))
		{
			System.err.println("The specified file does not have the right extension.");
			System.exit(1);
		}
		
		try 
		{
		
			NearImage image = new NearImage(fitfile);
			vtkPolyData boundary = image.getImageBorder();

	        String vtkfile = fitfile.substring(0, fitfile.length()-5) + "_BOUNDARY.VTK";

	        vtkPolyDataWriter writer = new vtkPolyDataWriter();
	        writer.SetInput(boundary);
	        writer.SetFileName(vtkfile);
	        writer.SetFileTypeToBinary();
	        writer.Write();
				
		} catch (FitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
