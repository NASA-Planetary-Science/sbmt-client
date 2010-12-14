package edu.jhuapl.near.dbgen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import vtk.*;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImage.MSISource;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class MSIFootprintGenerator
{
	private static SmallBodyModel erosModel;
    private static int resolutionLevel = 0;

	private static boolean checkIfMsiFilesExist(String line, MSIImage.MSISource source)
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

		// Check for the sumfile if source is Gaskell
        if (source.equals(MSISource.GASKELL))
        {
            File msirootdir = (new File(line)).getParentFile().getParentFile().getParentFile().getParentFile();
            String msiId = (new File(line)).getName().substring(0, 11);
            name = msirootdir.getAbsolutePath() + "/sumfiles/" + msiId + ".SUM";
            file = new File(name);
            if (!file.exists())
                return false;
        }

		return true;
	}

	private static void generateMSIFootprints(
	        ArrayList<String> msiFiles, MSIImage.MSISource msiSource) throws IOException, FitsException
    {
    	int count = 0;
    	for (String filename : msiFiles)
    	{
            System.out.println("starting msi " + count++ + " / " + msiFiles.size() + " " + filename + "\n");

			boolean filesExist = checkIfMsiFilesExist(filename, msiSource);
			if (filesExist == false)
				continue;

    		String dayOfYearStr = "";
    		String yearStr = "";

    		File origFile = new File(filename);
    		File f = origFile;

    		f = f.getParentFile();

    		f = f.getParentFile();
    		dayOfYearStr = f.getName();

    		f = f.getParentFile();
    		yearStr = f.getName();

            String vtkfile = null;
            if (msiSource == MSISource.PDS)
                vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_PDS.VTP";
            else
                vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT_RES" + resolutionLevel + "_GASKELL.VTP";

            // If the file we are trying to generate already exists, go on to the next file
            File realVtkFile = new File(vtkfile);
            if (realVtkFile.exists())
            {
                System.out.println("Already exists. Skipping\n\n");
                continue;
            }

    		MSIImage image = new MSIImage(origFile, erosModel, msiSource);


    		System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
    		System.out.println("year: " + yearStr);
    		System.out.println("dayofyear: " + dayOfYearStr);
    		//System.out.println("midtime: " + midtime);

    		image.loadFootprint();
    		vtkPolyData footprint = image.getUnshiftedFootprint();

			if (footprint == null || footprint.GetNumberOfPoints() == 0)
			{
				System.err.println("Error: Footprint generation failed");
				continue;
			}

//			footprint.GetPointData().SetTCoords(null);
//	        footprint.GetPointData().Reset();
//	        footprint.GetCellData().Reset();

	        // Use a tmp name while saving the file in case we abort during the saving
	        vtkXMLPolyDataWriter writer = new vtkXMLPolyDataWriter();
	        writer.SetInput(footprint);
	        writer.SetFileName(vtkfile + "_tmp");
	        writer.SetCompressorTypeToZLib();
	        writer.SetDataModeToBinary();
	        writer.Write();

	        // Okay, now rename the file to the real name.
            File tmpFile = new File(vtkfile + "_tmp");
	        realVtkFile.delete();
	        tmpFile.renameTo(realVtkFile);

	        writer.Delete();
	        image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());

            System.out.println("\n\n");
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

        String msiFileList=args[0];

		erosModel = ModelFactory.createErosBodyModel();
        resolutionLevel = Integer.parseInt(args[1]);
        try {
            erosModel.setModelResolution(resolutionLevel);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MSIImage.setGenerateFootprint(true);

		ArrayList<String> msiFiles = null;
		try {
			msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		try
		{
            generateMSIFootprints(msiFiles, MSISource.PDS);
            generateMSIFootprints(msiFiles, MSISource.GASKELL);
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
