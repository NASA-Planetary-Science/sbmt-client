package edu.jhuapl.near.dbgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.*;

import edu.jhuapl.near.model.eros.ErosModel;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class NISFootprintGenerator 
{
	private static ErosModel erosModel;
    
	private static void generateNISFootprints(ArrayList<String> nisFiles) throws IOException
    {
    	int count = 0;
    	for (String filename : nisFiles)
    	{
			System.out.println("starting nis " + count++);
			
    		String dayOfYearStr = "";
    		String yearStr = "";

    		File origFile = new File(filename);
    		File f = origFile;

    		f = f.getParentFile();
    		dayOfYearStr = f.getName();

    		f = f.getParentFile();
    		yearStr = f.getName();


    		NISSpectrum nisSpectrum = new NISSpectrum(origFile, erosModel);
    		
    		DateTime midtime = new DateTime(nisSpectrum.getDateTime().toString(), DateTimeZone.UTC);
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		System.out.println("midtime: " + midtime);
    		System.out.println(" ");

    		nisSpectrum.generateFootprint();
    		vtkPolyData footprint = nisSpectrum.getUnshiftedFootprint();
    		
			if (footprint == null || footprint.GetNumberOfPoints() == 0)
			{
				System.err.println("Error: Footprint generation failed");
				continue;
			}
			
	        String vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";

	        vtkPolyDataWriter writer = new vtkPolyDataWriter();
	        writer.SetInput(footprint);
	        writer.SetFileName(vtkfile);
	        writer.SetFileTypeToBinary();
	        writer.Write();
    	}
    }

	/**
	 * This program takes a path to a FIT image and generates a vtk file containing the
	 * boundary of the image in the same directory as the original file. 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		erosModel = new ErosModel();
		
		String nisFileList=args[0];

		
		ArrayList<String> nisFiles = null;
		try {
			nisFiles = FileUtil.getFileLinesAsStringList(nisFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		try 
		{
			generateNISFootprints(nisFiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
