package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

public class MakeDeepImpactImageList
{
	public MakeDeepImpactImageList(File sumfileDir, File imagesDir, File pdsDir, File translator, File imageIndex) throws Exception
	{
		//Get the sumfile base names. These are the image METs prepended with instrument ID string.
		Vector<String> sumfileMets = getSumfileBasenames(sumfileDir);

		//Load the translator, which is a table of EPOXI file names and their corresponding MET file names.
		BufferedReader in = new BufferedReader(new FileReader(translator));
        String s = in.readLine().trim();
		s = in.readLine();
		HashMap<String, String> fitsFiles = new HashMap<String, String>();
		while (s != null)
		{
	        StringTokenizer tokens = new StringTokenizer(s, " ");
	        String epoxiPdsFileName = tokens.nextToken();
	        tokens.nextToken();
	        String metPdsFileName = tokens.nextToken();

	        //Find the EPOXI filename for each sumfile.
	        //There can only be a single image matching the given MET string.
	        for (String sumfileMet : sumfileMets)
	        {
	        	if (metPdsFileName.toUpperCase().contains(sumfileMet.toUpperCase()))
	        	{
                    fitsFiles.put(epoxiPdsFileName, sumfileMet + ".FIT");  //Use this to match SUMFILE name
//                    fitsFiles.put(epoxiPdsFileName, metPdsFileName);         //Use this to keep PDS MET name
	        		break;
	        	}
	        }
			s = in.readLine();
		}
		in.close();

		//Now find the full path to the EPOXI file that matched the sumfile the PDS index file.
		File[] topLevel = new File[]{pdsDir};
		for (String fitsFile : fitsFiles.keySet())
		{
			System.err.println("Looking for file: " + fitsFile);
			File epoxiFits = findFile(topLevel, fitsFile);
			if (epoxiFits == null)
			{
				System.err.println("ERROR! Cannot find FITS file " + fitsFile + " in PDS archive. Matching SUMFILE is " + fitsFiles.get(fitsFile));
			}
			System.err.println("   FOUND " + epoxiFits.getAbsolutePath());

			//Now copy the EPOXI .fit file to the images/ directory, and rename it with the met.
			try
			{
				FileUtils.copyFileToDirectory(epoxiFits, imagesDir);
				File epoxiFile = new File(imagesDir, epoxiFits.getName());
				File metFile = new File(imagesDir, fitsFiles.get(fitsFile));
				FileUtils.moveFile(epoxiFile, metFile);
				System.err.println("Renamed " + epoxiFile.getAbsolutePath() + " to " + metFile.getAbsolutePath());
			}
			catch(Exception e)
			{
				System.err.println("Error copying epoxi fits file from PDS directory to met fits file in images/ dir.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This function reads in all the sumfiles, uses their METs to find the Deep Impace
	 * image file, which is in EPOXI file name format, then copies that file to the input
	 * images directory and renames it to match the sumfile name.
	 *
	 * Find the fits file matching the sumfile name's MET by using the PDS
	 * translator table, which links an EPOXI pipeline filename having format
	 *     <instIdStr>YYMMDDHH_<seqID>*.fit
	 * with an MET based filename having format
	 *     <instrIdStr>MET*.fit
	 * The version 3 PDS archive uses EPOXI filenames, but the sumfiles are MET-based,
	 * so we need to pull the EPOXI files and rename them to match the sumfiles.
	 *
	 *
	 * @param sumfileDir
	 * @param imagesDir
	 * @param pdsDir
	 * @param translator
	 * @param imageIndex
	 * @throws Exception
	 */
	private void prepareImages()
	{

	}

	//Recursive method to find a files in a directory tree.
	//Return when the first copy of the file is found.
	private File findFile(File[] files, String fileToFind)
	{
		for (File file : files)
		{
			if (file.isDirectory())
			{
//				System.out.println("Directory: " + file.getAbsolutePath());
				File found = findFile(file.listFiles(), fileToFind); // recursive
				if (found != null)
				{
					return found;
				}
			}
			else
			{
//				System.out.println("    want " + fileToFind.toUpperCase());
//			    System.out.println("    current " + file.getName().toUpperCase());
//				System.out.println("File: " + file.getAbsolutePath());
				if (file.getName().compareToIgnoreCase(fileToFind) == 0)
				{
					return file;
				}
			}
		}
		return null;
	}

	//returns the sumfile base names, which are the image MET preceded by
	//an instrument identifier string.
	private Vector<String> getSumfileBasenames(File sumfileDir)
	{
	    Vector<String> sumfileMets = new Vector<String>();
		File[] sumfiles = sumfileDir.listFiles();
		if (sumfiles != null)
		{
			for (File sumfile : sumfiles)
			{
				//Is it a sumfile?
				String name = sumfile.getName();
				int end = name.length();
				String ext = name.substring(end-3, end);
				if (ext.toUpperCase().equals("SUM"))
				{
					String metStr = name.substring(0, end-4);
					sumfileMets.add(metStr);
				}
			}
		}
		return sumfileMets;
	}


    public static void main(String[] args) throws Exception
    {
        File basedir = new File("C:\\Users\\nguyel1\\Projects\\SBMT\\CarolynErnstGrant\\data\\DeepImpact");
    	File sumfileDir = new File(basedir, "testData\\sumfiles");
    	File imagesDir = new File(basedir, "testData\\images");
    	if (!imagesDir.exists())
    	{
    		imagesDir.mkdirs();
    	}

        File pdsDir = new File(basedir, "v3_filenameByYYMMDDHH_FromCarolynEmail\\ITS\\dii-c-its-3_4-9p-encounter-v3.0");
        File dataDir = new File(pdsDir, "data");
        //Translate filenames from EPOXI YYMMDDHH format to MET format (to match sumfile names)
        File translatorFile = new File(pdsDir, "document\\its_translate_product_id.tab");
        //PDS data index file, to get path location of found file.
	    File indexFile = new File(pdsDir, "index\\index.tab");

	    MakeDeepImpactImageList doit = new MakeDeepImpactImageList(sumfileDir, imagesDir, dataDir, translatorFile, indexFile);
    }
}
