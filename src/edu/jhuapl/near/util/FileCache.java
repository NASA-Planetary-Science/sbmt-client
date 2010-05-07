package edu.jhuapl.near.util;

import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class FileCache 
{
	static public File getFileFromServer(String path)
	{
		// First check the cache to see if the file is there.
		// If not place the file in the cache. The cache should mirror
		// the file hierarchy on the server.
	
		String unzippedPath = path;
		if (unzippedPath.toLowerCase().endsWith(".gz"))
			unzippedPath = unzippedPath.substring(0, unzippedPath.length()-3);
		
		File file = new File(Configuration.getCacheDir() + File.separator + unzippedPath);
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
	
	/**
	 * When adding to cache, gzipped files are always uncompressed and saved
	 * without the ".gz" extension.
	 */
	static private File addToCache(String path)
	{
		File file = null;
		try 
		{
			URL u = new URL(Configuration.getDataRootURL() + path);
			
			InputStream is = u.openStream();
			
			if (path.toLowerCase().endsWith(".gz"))
				is = new GZIPInputStream(is);

			if (path.toLowerCase().endsWith(".gz"))
				path = path.substring(0, path.length()-3);
			
			file = new File(Configuration.getCacheDir() + File.separator + path);
			
			file.getParentFile().mkdirs();
			
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
