package edu.jhuapl.near.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
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
		
		// Open a connection the file on the server
		try 
		{
			URL u = new URL(Configuration.getDataRootURL() + path);
			URLConnection conn = u.openConnection();

			long fileLastModified = file.lastModified();
			long urlLastModified = conn.getLastModified();

			if (file.exists() && fileLastModified >= urlLastModified)
			{
				return file;
			}
			else
			{
				file = addToCache(path, conn.getInputStream(), urlLastModified);
				return file;
			}
		}
		catch (IOException e) 
		{
			file = null;
			e.printStackTrace();
		}

		return null;
	}
	
	//static private void deleteOldCaches()
	//{
	//	
	//}
	
	/**
	 * When adding to cache, gzipped files are always uncompressed and saved
	 * without the ".gz" extension.
	 * @throws IOException 
	 */
	static private File addToCache(String path, InputStream is, long urlLastModified) throws IOException
	{
		if (path.toLowerCase().endsWith(".gz"))
			is = new GZIPInputStream(is);

		if (path.toLowerCase().endsWith(".gz"))
			path = path.substring(0, path.length()-3);

		File file = new File(Configuration.getCacheDir() + File.separator + path);

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

		if (urlLastModified > 0)
			file.setLastModified(urlLastModified);

		return file;	
	}
}
