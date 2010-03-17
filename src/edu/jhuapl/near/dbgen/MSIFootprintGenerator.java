package edu.jhuapl.near.dbgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import vtk.*;

import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.model.MSIImage;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class MSIFootprintGenerator 
{
	private static ErosModel erosModel;
	
	private static void generateMSIFootprints(ArrayList<String> msiFiles) throws IOException, FitsException
    {
		vtkPolyDataWriter writer = new vtkPolyDataWriter();
    	int count = 0;
    	for (String filename : msiFiles)
    	{
			boolean filesExist = DatabaseGeneratorSql.checkIfAllFilesExist(filename);
			if (filesExist == false)
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

    		//MSIImage image = new MSIImage(origFile);
    		//HashMap<String, String> properties = image.getProperties();

    		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    		HashMap<String, String> properties = MSIImage.parseLblFile(lblFilename);
    		
    		double[] spacecraftPosition = new double[3];
    	    double[] frustum1 = new double[3];
    	    double[] frustum2 = new double[3];
    	    double[] frustum3 = new double[3];
    	    double[] frustum4 = new double[3];

    		String[] tmp = properties.get(MSIImage.SPACECRAFT_POSITION).split(" ");
    		spacecraftPosition[0] = Double.parseDouble(tmp[0]);
    		spacecraftPosition[1] = Double.parseDouble(tmp[1]);
    		spacecraftPosition[2] = Double.parseDouble(tmp[2]);
    		tmp = properties.get(MSIImage.MSI_FRUSTUM1).split(" ");
    		frustum1[0] = Double.parseDouble(tmp[0]);
    		frustum1[1] = Double.parseDouble(tmp[1]);
    		frustum1[2] = Double.parseDouble(tmp[2]);
    		tmp = properties.get(MSIImage.MSI_FRUSTUM2).split(" ");
    		frustum2[0] = Double.parseDouble(tmp[0]);
    		frustum2[1] = Double.parseDouble(tmp[1]);
    		frustum2[2] = Double.parseDouble(tmp[2]);
    		tmp = properties.get(MSIImage.MSI_FRUSTUM3).split(" ");
    		frustum3[0] = Double.parseDouble(tmp[0]);
    		frustum3[1] = Double.parseDouble(tmp[1]);
    		frustum3[2] = Double.parseDouble(tmp[2]);
    		tmp = properties.get(MSIImage.MSI_FRUSTUM4).split(" ");
    		frustum4[0] = Double.parseDouble(tmp[0]);
    		frustum4[1] = Double.parseDouble(tmp[1]);
    		frustum4[2] = Double.parseDouble(tmp[2]);
    		
    		//DateTime midtime = new DateTime(nisSpectrum.getDateTime().toString(), DateTimeZone.UTC);
    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		//System.out.println("midtime: " + midtime);
    		System.out.println(" ");
    	
    		vtkPolyData footprint = erosModel.computeFrustumIntersection(spacecraftPosition, 
					frustum1, frustum2, frustum3, frustum4);

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
	        
	        //System.gc();
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
			generateMSIFootprints(nisFiles);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
