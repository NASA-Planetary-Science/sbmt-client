package edu.jhuapl.near.dbgen;

import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.io.*;
import java.util.*;


public class MSITemporalDatabaseGeneratorTxt 
{
	/*
	static class Images
	{
		//int iof_or_cif; // 0 for iofdbl, 1 for cifdbl
		//String suffix;
		//String prefix;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> startTimes = new ArrayList<String>();
		ArrayList<String> stopTimes = new ArrayList<String>();
//		Images(int iof_or_cif)
//		{
//			this.iof_or_cif = iof_or_cif;
//			this.prefix = "M0";
//			if (iof_or_cif == 0)
//			{
//				this.suffix = "_2P_IOF_DBL.FIT";
//			}
//			else
//			{
//				this.suffix = "_2P_CIF_DBL.FIT";
//			}
//		}
	}
	
	static class Day
	{
		String dayOfYear;
		Images iofdbl = new Images();
		Images cifdbl = new Images();
	}
	
	static class Year
	{
		TreeMap<String, Day> daysOfYear = new TreeMap<String, Day>();
		String year;
	}

	TreeMap<String, Year> years = new TreeMap<String, Year>();
	*/
	
	void addImageToDataStructure(NearImage image, BufferedWriter out) throws IOException
	{
		int iof_or_cif = -1;
		String dayOfYearStr = "";
		String yearStr = "";
		
		String fullpath = image.getFullPath();
        File origFile = new File(fullpath);
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
        
//        if (!years.containsKey(yearStr))
//        	years.put(yearStr, new Year());
        
//        Year year = years.get(yearStr);
//        year.year = yearStr;
        
//        if (!year.daysOfYear.containsKey(dayOfYearStr))
//        	year.daysOfYear.put(dayOfYearStr, new Day());
        
//        Day dayOfYear = year.daysOfYear.get(dayOfYearStr);
//        dayOfYear.dayOfYear = dayOfYearStr;
    	StringPair startStopTimes = image.getImageStartStopTime();
//        if (iof_or_cif == 0)
//        {
//        	dayOfYear.iofdbl.names.add(origFile.getName().substring(0, 13));
//        	dayOfYear.iofdbl.startTimes.add(startStopTimes.s1);
//        	dayOfYear.iofdbl.stopTimes.add(startStopTimes.s2);
//        }
//        else if (iof_or_cif == 1)
//        {
//        	dayOfYear.cifdbl.names.add(origFile.getName().substring(0, 13));
//        	dayOfYear.cifdbl.startTimes.add(startStopTimes.s1);
//        	dayOfYear.cifdbl.stopTimes.add(startStopTimes.s2);
//        }
        
        out.write(
        		origFile.getName().substring(2, 11) + "\t" +
        		yearStr + "\t" +
        		dayOfYearStr + "\t" +
        		(iof_or_cif==0 ? "iofdbl" : "cifdbl") + "\t" +
        		origFile.getName().substring(12, 13) + "\t" +
        		startStopTimes.s1 + "\t" +
        		startStopTimes.s2 + "\t" +
        		image.getSpaceCraftDistance() + "\n"
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
					NearImage image = new NearImage(line);

					addImageToDataStructure(image, out);
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

		// Now write out the database
//		try 
//		{
//			this.writeDatabaseXml((new File(msiFiles)).getParent() + "/msiTemporalDb.xml");
//		} 
//		catch (IOException e) 
//		{
//			e.printStackTrace();
//		}

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
		
		String msiFiles="/media/KANGURU2.0/near/data/filelist2.txt";
		//String msiFiles = args[0];

    	new MSITemporalDatabaseGeneratorTxt(msiFiles);
	}

}
