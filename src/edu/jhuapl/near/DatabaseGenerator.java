package edu.jhuapl.near;

import java.io.*;

public class DatabaseGenerator 
{

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
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
