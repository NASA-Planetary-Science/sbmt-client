package edu.jhuapl.near.dbgen;

import edu.jhuapl.near.*;
import edu.jhuapl.near.model.NearImage;
import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.NativeLibraryLoader;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*; 
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;


public class MSITemporalDatabaseGenerator 
{
	
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
	
	void addImageToDataStructure(NearImage image) throws IOException
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
        
        if (!years.containsKey(yearStr))
        	years.put(yearStr, new Year());
        
        Year year = years.get(yearStr);
        year.year = yearStr;
        
        if (!year.daysOfYear.containsKey(dayOfYearStr))
        	year.daysOfYear.put(dayOfYearStr, new Day());
        
        Day dayOfYear = year.daysOfYear.get(dayOfYearStr);
        dayOfYear.dayOfYear = dayOfYearStr;
    	StringPair startStopTimes = image.getImageStartStopTime();
        if (iof_or_cif == 0)
        {
        	dayOfYear.iofdbl.names.add(origFile.getName().substring(0, 13));
        	dayOfYear.iofdbl.startTimes.add(startStopTimes.s1);
        	dayOfYear.iofdbl.stopTimes.add(startStopTimes.s2);
        }
        else if (iof_or_cif == 1)
        {
        	dayOfYear.cifdbl.names.add(origFile.getName().substring(0, 13));
        	dayOfYear.cifdbl.startTimes.add(startStopTimes.s1);
        	dayOfYear.cifdbl.stopTimes.add(startStopTimes.s2);
        }
	}

    void writeDatabaseXml(String msiTemporalDbName) throws IOException
    {
    	try
    	{
    		//Create instance of DocumentBuilderFactory
    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    		//Get the DocumentBuilder
    		DocumentBuilder parser = factory.newDocumentBuilder();
    		
    		//Create blank DOM Document
    		Document doc = parser.newDocument();

    		//create the root element
    		Element root = doc.createElement("msi_temporal_database");
    		
    		//all it to the xml tree
    		doc.appendChild(root);

    		//create children elements
    
    		Iterator<String> it = years.keySet().iterator(); 
    		while(it.hasNext()) 
    		{ 
    			String key = it.next(); 
    			Year year = years.get(key); 

    			Element yearElement = doc.createElement("year");
        		//Add the attribute to the child
        		yearElement.setAttribute("value", year.year);
        		
        		Iterator<String> it2 = year.daysOfYear.keySet().iterator(); 
        		while(it2.hasNext()) 
        		{ 
        			String key2 = it2.next(); 
        			Day day = year.daysOfYear.get(key2); 
        			
        			Element dayElement = doc.createElement("day");
            		dayElement.setAttribute("value", day.dayOfYear);

            		for (int j=0; j<2; ++j)
            		{
            			Element dblElement = doc.createElement(j==0 ? "iofdbl" : "cifdbl");
            		
            			String imageNames = "";
            			String startTimes = "";
            			String stopTimes = "";

            			Images images = j==0 ? day.iofdbl : day.cifdbl;
            			
            			int numImages = images.names.size();
            			for (int i=0; i<numImages; ++i)
            			{
            				imageNames += images.names.get(i) + " ";
            				startTimes += images.startTimes.get(i) + " ";
            				stopTimes += images.stopTimes.get(i) + " ";
            			}
            			
            			dblElement.setAttribute("images", imageNames);
            			dblElement.setAttribute("start_times", startTimes);
            			dblElement.setAttribute("stop_times", stopTimes);
            			
            			dayElement.appendChild(dblElement);
            		}
            		
            		yearElement.appendChild(dayElement);
        		}        		
        		
        		root.appendChild(yearElement);
    		} 
    		

    		// Seems strange that 6 lines of java code are needed just to write it out to the file.
    		TransformerFactory tranFactory = TransformerFactory.newInstance(); 
    		Transformer aTransformer = tranFactory.newTransformer(); 
    		Source src = new DOMSource(doc);
    		BufferedWriter out = new BufferedWriter(new FileWriter(msiTemporalDbName));
    		Result dest = new StreamResult(out); 
    		aTransformer.transform(src, dest); 
    	}
    	catch(Exception e)
    	{
			e.printStackTrace();
    	}
    }
    
	MSITemporalDatabaseGenerator(String msiFiles)
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
			while ((line = in.readLine()) != null)
			{
				System.out.println("\n");
				System.out.println("Processing image " + count++);
				System.out.println(line);

				try 
				{
					NearImage image = new NearImage(line);

					addImageToDataStructure(image);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					++numberOfFailures;
					imageFailures.add(line);
				} 
			}
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Now write out the database
		try 
		{
			this.writeDatabaseXml((new File(msiFiles)).getParent() + "/msiTemporalDb.xml");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
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

		String msiFiles = args[0];

    	new MSITemporalDatabaseGenerator(msiFiles);
	}

}
