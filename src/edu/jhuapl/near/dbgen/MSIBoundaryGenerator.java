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
	private static int resolutionLevel = 0;
	
	private static boolean checkIfMsiFilesExist(String line)
	{
		File file = new File(line);
		if (!file.exists())
			return false;

		String name = line.substring(0, line.length()-4) + ".LBL";
		file = new File(name);
		if (!file.exists())
			return false;
		
		name = line.substring(0, line.length()-4) + "_DDR.LBL";
		file = new File(name);
		if (!file.exists())
			return false;

		name = line.substring(0, line.length()-4) + "_FOOTPRINT.VTK";
		file = new File(name);
		if (!file.exists())
			return false;

		return true;
	}

	private static void generateMSIBoundaries(ArrayList<String> msiFiles) throws IOException, FitsException
    {
		vtkPolyDataWriter writer = new vtkPolyDataWriter();
    	int count = 0;
    	for (String filename : msiFiles)
    	{
			boolean filesExist = checkIfMsiFilesExist(filename);
			if (filesExist == true)
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
    		
			if (boundary == null || boundary.GetNumberOfPoints() == 0)
			{
				System.err.println("Error: Boundary generation failed");
				continue;
			}
			
	        String vtkfile = filename.substring(0, filename.length()-4) + "_BOUNDARY_RES" + resolutionLevel + ".VTK";

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

		String msiFileList = args[0];

		erosModel = new ErosModel();
		resolutionLevel = Integer.parseInt(args[1]);
		try {
			erosModel.setModelResolution(resolutionLevel);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
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
