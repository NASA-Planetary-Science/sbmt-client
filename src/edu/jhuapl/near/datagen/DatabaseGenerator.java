package edu.jhuapl.near.datagen;

import java.io.*;
import java.util.*;

import edu.jhuapl.near.*;

import nom.tam.fits.FitsException;

public class DatabaseGenerator 
{

	static class MSIDatabase
	{
		static class ImageInfo
		{
			String name;
			String data;
			double[] scPos;
			double[] scPoint;
			double[] latBoundary;
			double[] lonBoundary;
		}
		
		static class Cell
		{
			ArrayList<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
			int cellId;
			int row;
			int col;
			double latMin;
			double lonMin;
			double latMax;
			double lonMax;
		}
		
		ArrayList<Cell> cells = new ArrayList<Cell>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

		// Read in a list of files which we need to process
		String msiFiles = args[0];
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(msiFiles);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		String line;
        try 
        {
			while ((line = in.readLine()) != null)
			{
				NearImage image = new NearImage(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
