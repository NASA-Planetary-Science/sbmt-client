package edu.jhuapl.near.dbgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.*;

import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.model.MSIImage;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class MSIBoundaryGenerator 
{
	private static ErosModel erosModel;
	
	private static void generateMSIBoundaries(ArrayList<String> msiFiles) throws IOException, FitsException
    {
		vtkPolyDataWriter writer = new vtkPolyDataWriter();
    	int count = 0;
    	for (String filename : msiFiles)
    	{
			boolean filesExist = DatabaseGeneratorSql.checkIfAllMsiFilesExist(filename);
			if (filesExist == false)
				continue;

    		System.out.println("\n\n");
			System.out.println("starting msi " + count++);
			
    		String dayOfYearStr = "";
    		String yearStr = "";

    		File origFile = new File(filename);
    		File f = origFile;

    		f = f.getParentFile();

    		f = f.getParentFile();
    		dayOfYearStr = f.getName();

    		f = f.getParentFile();
    		yearStr = f.getName();

    		MSIImage image = new MSIImage(origFile, erosModel);

    		
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		//System.out.println("midtime: " + midtime);
    	
    		vtkPolyData boundary = image.generateBoundary();
    		
			if (boundary == null)
			{
				System.err.println("Error: Boundary generation failed");
				continue;
			}
			
	        String vtkfile = filename.substring(0, filename.length()-4) + "_BOUNDARY.VTK";

	        writer.SetInput(boundary);
	        writer.SetFileName(vtkfile);
	        writer.SetFileTypeToBinary();
	        writer.Write();
    	}
    }

	/**
	 * This program takes a file containing a list of FIT images and generates a vtk file containing the
	 * boundary of the image in the same directory as the original file. 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		erosModel = new ErosModel();
		
		String msiFileList=args[0];

		
		ArrayList<String> msiFiles = null;
		try {
			msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		try 
		{
			generateMSIBoundaries(msiFiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}

/*
public class MSIBoundaryGenerator 
{
	private static ErosModel erosModel;

	
	 //This program takes a path to a FIT image and generates a vtk file containing the
	 //boundary of the image in the same directory as the original file. 
	 //@param args
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
*/