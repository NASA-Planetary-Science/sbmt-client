package edu.jhuapl.near.util;

import java.io.*;

public class ConvertToRealFile 
{
	public static File convertResource(Object o, String resource)
	{
		File temp = null;
		try 
		{
			temp = File.createTempFile("resource", ".vtk");
			temp.deleteOnExit();
			
			InputStream is = o.getClass().getResourceAsStream(resource);

			FileOutputStream os = new FileOutputStream(temp);

			byte[] buff = new byte[1024];
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
