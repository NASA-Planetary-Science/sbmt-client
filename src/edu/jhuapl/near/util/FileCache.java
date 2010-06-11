package edu.jhuapl.near.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class FileCache 
{
    // Stores files already downloaded in this process
    private static HashSet<String> downloadedFiles = new HashSet<String>();
    
    /**
     *  This function retrieves the specifed file from the server and places it in the cache.
     *  It first checks the cache to see if the file is already there.
     *  The cache mirrors the file hierarchy on the server.
     *   
     *  The rules for determining whether or not we download the file
     *  from the server or use the file already in the cache are as follows:
     *  
     *  - If the file does not exist in the cache, download it.
     *  - If the file does exist, and was already downloaded by this very process
     *    (files already downloaded are stored in the downloadedFiles hash set), then
     *    return the cached file without comparing last modified times.
     *  - If the file does exist, and has not been previously downloaded by this process,
     *    compare the last modified time of the cached file to the remote file on
     *    server. If the remote file is newer, download it, otherwise return the cached file.
     *  - If there was a failure connecting to the server simply return the file if it
     *    exists in the cache.
     *  - If the file could not be retrieved for any reason, null is returned.
     *
     * @param path
     * @return
     */
	static public File getFileFromServer(String path)
	{
		String unzippedPath = path;
		if (unzippedPath.toLowerCase().endsWith(".gz"))
			unzippedPath = unzippedPath.substring(0, unzippedPath.length()-3);
		
		File file = new File(Configuration.getCacheDir() + File.separator + unzippedPath);

		// If we've already downloaded the file previously in this process,
		// simply return without making any network connections.
		boolean exists = file.exists();
		if (exists && downloadedFiles.contains(path))
		    return file;
		
		// Open a connection the file on the server
		try 
		{
			URL u = new URL(Configuration.getDataRootURL() + path);
			URLConnection conn = u.openConnection();

			long urlLastModified = conn.getLastModified();

			if (exists && file.lastModified() >= urlLastModified)
			{
				return file;
			}
			else
			{
				file = addToCache(path, conn.getInputStream(), urlLastModified);
				downloadedFiles.add(path);
				return file;
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		// If something happens that we reach here, simply return the file if it exists.
        if (exists)
            return file;
        else
            return null;
	}
	
	//static private void deleteOldCaches()
	//{
	//	
	//}
	
	/**
	 * When adding to the cache, gzipped files are always uncompressed and saved
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

		// Change the modified time of the file to that of the server.
		if (urlLastModified > 0)
			file.setLastModified(urlLastModified);

		return file;	
	}
}
