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

public class MSIFootprintGenerator 
{
	private static ErosModel erosModel;
	
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

		return true;
	}

	private static void generateMSIFootprints(ArrayList<String> msiFiles) throws IOException, FitsException
    {
		vtkPolyDataWriter writer = new vtkPolyDataWriter();
    	int count = 0;
    	for (String filename : msiFiles)
    	{
			boolean filesExist = checkIfMsiFilesExist(filename);
			if (filesExist == true)
				continue;

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
    		System.out.println(" ");
    	
    		vtkPolyData footprint = image.generateFootprint();
    		
			if (footprint == null)
			{
				System.err.println("Error: Footprint generation failed");
				continue;
			}
			
	        String vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";

	        writer.SetInput(footprint);
	        writer.SetFileName(vtkfile);
	        writer.SetFileTypeToBinary();
	        writer.Write();
    	}
    }

	/**
	 * This program takes a file containing a list if FIT images and generates a vtk file containing the
	 * footprint of the image in the same directory as the original file. 
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
			generateMSIFootprints(msiFiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
