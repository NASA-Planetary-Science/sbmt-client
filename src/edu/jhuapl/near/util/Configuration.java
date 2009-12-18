package edu.jhuapl.near.util;

import java.io.File;

public class Configuration 
{
	/**
	 * @return Return the location where all application specific files should be stored. This is within
	 * the .neartool folder located in the users home directory.
	 */
	static public String getApplicationDataDir()
	{
		String appdir = System.getProperty("user.home") + File.separator + ".neartool";
		
		// if the directory does not exist, create it
		File dir = new File(appdir);
		if (!dir.exists())
		{
			dir.mkdir();
		}
		
		return appdir;
	}
	
	/**
	 * @return Return the path where the database files are (or should be) stored.
	 */
	static public String getDatabaseDir()
	{
		return Configuration.getApplicationDataDir() + File.separator + "neardb";
	}
	
	/**
	 * @return Return the name of the database. For the HyperSQL database which is currently being
	 * used, the database name is the prefix of all files located in the database directory
	 * (this directory is returned by the getDatabaseDir function).
	 */
	static public String getDatabaseName()
	{
		return "near";
	}

	/**
	 * @return Return the url of the server where data is downloaded from.
	 */
	static public String getDataRootURL()
	{
		return "http://near.jhuapl.edu/software/data2";
	}
	
	static public String getQueryRootURL()
	{
		return "http://near.jhuapl.edu/software/query";
	}

}
