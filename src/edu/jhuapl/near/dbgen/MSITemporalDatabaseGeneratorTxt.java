package edu.jhuapl.near.dbgen;

import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.io.*;
import java.util.*;


public class MSITemporalDatabaseGeneratorTxt 
{
	
	void addImageToDataStructure(String filename, BufferedWriter out) throws IOException
	{
		int iof_or_cif = -1;
		String dayOfYearStr = "";
		String yearStr = "";
		
		//String fullpath = image.getFullPath();
        File origFile = new File(filename);
        File f = origFile;
        
        f = f.getParentFile();
        if (f.getName().equals("iofdbl"))
        	iof_or_cif = 0;
        else if (f.getName().equals("cifdbl"))
        	iof_or_cif = 1;
        
        f = f.getParentFile();
        dayOfYearStr = f.getName();
        
        f = f.getParentFile();
        yearStr = f.getName();
        
        System.out.println("year: " + yearStr);
        System.out.println("dayofyear: " + dayOfYearStr);
        System.out.println("iof_or_cif: " + iof_or_cif);
        
    	String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
    	HashMap<String, String> properties = NearImage.parseLblFile(lblFilename);
        
        out.write(
        		origFile.getName().substring(2, 11) + "\t" +
        		yearStr + "\t" +
        		dayOfYearStr + "\t" +
        		(iof_or_cif==0 ? "iofdbl" : "cifdbl") + "\t" +
        		origFile.getName().substring(12, 13) + "\t" +
        		properties.get(NearImage.START_TIME) + "\t" +
        		properties.get(NearImage.STOP_TIME) + "\t" +
        		properties.get(NearImage.TARGET_CENTER_DISTANCE) + "\t" +
        		properties.get(NearImage.HORIZONTAL_PIXEL_SCALE) + "\n"
        		);
	}
    
	MSITemporalDatabaseGeneratorTxt(String msiFiles)
	{
		// Read in a list of files which we need to process
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(msiFiles);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		int numberOfFailures = 0;
		int count = 1;
		ArrayList<String> imageFailures = new ArrayList<String>();
		
		String line = "";

		
		try 
		{
			BufferedWriter out = new BufferedWriter(new FileWriter((new File(msiFiles)).getParent() + "/msiTemporalDb.txt"));

			while ((line = in.readLine()) != null)
			{
				System.out.println("\n");
				System.out.println("Processing image " + count++);
				System.out.println(line);

				try 
				{
					//NearImage image = new NearImage(line);

					addImageToDataStructure(line, out);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					++numberOfFailures;
					imageFailures.add(line);
				} 
			}
			
			out.close();
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("\n\n\n");
        System.out.println("Warning: " + numberOfFailures + " images could not be processed");
        System.out.println("They are:");
        for (String s: imageFailures)
        {
        	System.out.println(s);
        }
	}
	
	/**
	 * The purpose of this program is to generate an xml file containing a list
	 * of which images were taken per day in the mission, the start and stop time
	 * and the paths to the images.
	 * @param args name of file which contains a list of the full paths of
	 * all images which need to be indexed, one path per line.
	 */
	public static void main(String[] args) 
	{
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();
		
		//String msiFiles="/media/KANGURU2.0/near/data/filelist2.txt";
		String msiFiles = args[0];

    	new MSITemporalDatabaseGeneratorTxt(msiFiles);
	}

}
