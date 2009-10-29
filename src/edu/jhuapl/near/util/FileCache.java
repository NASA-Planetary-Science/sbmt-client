package edu.jhuapl.near.util;

import java.io.*;
import java.net.URL;

public class FileCache 
{
	static private String cacheRoot = null;
	static private String cacheVersion = "1";
	
	static public File getFileFromServer(String path)
	{
		// First check the cache to see if the file is there.
		// If not place the file in the cache. The cache should mirror
		// the file hierarchy on the server.
	
		if (cacheRoot == null)
		{
			cacheRoot = Configuration.getApplicationDataDir() + File.separator + 
			"cache" + File.separator + cacheVersion;
		}
		
		File file = new File("cacheRoot" + File.separator + path);
		if (file.exists())
		{
			return file;
		}
		else
		{
			file = addToCache(path);
			return file;
		}
	}
	
	//static private void deleteOldCaches()
	//{
	//	
	//}
	
	static private File addToCache(String path)
	{
		File file = null;
		try 
		{
			URL u = new URL(Configuration.getRootURL() + path);
			
			file = new File(cacheRoot + File.separator + path);
			
			file.getParentFile().mkdirs();
			
			InputStream is = u.openStream();

			FileOutputStream os = new FileOutputStream(file);

			byte[] buff = new byte[2048];
			int len;
			while((len = is.read(buff)) > 0)
			{
				os.write(buff, 0, len);
			}
			
			os.close();
			is.close();
		} 
		catch (IOException e) 
		{
			file = null;
			e.printStackTrace();
		}
		
		return file;	
	}
}
