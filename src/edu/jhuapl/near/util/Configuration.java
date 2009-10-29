package edu.jhuapl.near.util;

import java.io.File;

public class Configuration 
{
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
	
	static public String getRootURL()
	{
		return "http://near.jhuapl.edu/software/data/";
	}
}
