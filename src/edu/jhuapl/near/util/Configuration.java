package edu.jhuapl.near.util;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class Configuration 
{
	static private String appDir = null;
	static private String cacheDir = null;
	static private String cacheVersion = "2";
	static private String mapMaperDir = null;
	
	/**
	 * @return Return the location where all application specific files should be stored. This is within
	 * the .neartool folder located in the users home directory.
	 */
	static public String getApplicationDataDir()
	{
		if (appDir == null)
		{
			appDir = System.getProperty("user.home") + File.separator + ".neartool";

			// if the directory does not exist, create it
			File dir = new File(appDir);
			if (!dir.exists())
			{
				dir.mkdir();
			}
		}

		return appDir;
	}
	
	/**
	 * The cache folder is where files downloaded from the server are placed. The
	 * URL of server is returned by getDataRootURL()
	 * @return
	 */
	static public String getCacheDir()
	{
		if (cacheDir == null)
		{
			cacheDir = Configuration.getApplicationDataDir() + File.separator + 
			"cache" + File.separator + cacheVersion;
		}
		
		return cacheDir;
	}

	/**
	 * @return Return the url of the server where data is downloaded from.
	 */
	static public String getDataRootURL()
	{
		return "http://near.jhuapl.edu/software/data";
	}
	
	static public String getQueryRootURL()
	{
		return "http://near.jhuapl.edu/software/query2";
	}

	static public String getMapmakerDir()
	{
		return mapMaperDir;
	}
	
	static public void setMapmakerDir(String folder)
	{
		mapMaperDir = folder;
	}
}
