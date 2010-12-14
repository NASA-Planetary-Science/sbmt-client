package edu.jhuapl.near.util;

import java.io.*;

public class ConvertResourceToFile
{
	/**
	 * Convert a specified resource to a real file to be placed in a certain directory.
	 * @param o
	 * @param resource
	 * @param parentDir
	 * @return
	 */
	public static File convertResourceToRealFile(Object o, String resource, String parentDir)
	{
		// Get the name of the resource after the last slash
		File tmp = new File(resource);
		String name = tmp.getName();
		
		File parent = new File(parentDir);
		if (!parent.exists())
			parent.mkdirs();
		
		File file = new File(parentDir + File.separator + name);
		try
		{
			InputStream is = o.getClass().getResourceAsStream(resource);

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

	public static File convertResourceToTempFile(Object o, String resource)
	{
		File temp = null;
		try
		{
			temp = File.createTempFile("resource-", null);
			temp.deleteOnExit();
			
			InputStream is = o.getClass().getResourceAsStream(resource);

			FileOutputStream os = new FileOutputStream(temp);

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
			temp = null;
			e.printStackTrace();
		}
		
		return temp;
	}
}
