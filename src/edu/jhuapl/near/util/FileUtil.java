package edu.jhuapl.near.util;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class FileUtil 
{
	/**
	 * The function takes a file and returns its contents as a list of strings, 
	 * one line per string.
	 * @param filename file to read
	 * @return contents of file as list of strings
	 * @throws IOException 
	 */
	public static ArrayList<String> getFileLinesAsStringList(String filename) throws IOException
	{
		InputStream fs = new FileInputStream(filename);
		if (filename.toLowerCase().endsWith(".gz"))
			fs = new GZIPInputStream(fs);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		ArrayList<String> lines = new ArrayList<String>();
		String line;
		
		while ((line = in.readLine()) != null)
		{
			lines.add(line);
		}
		
		in.close();
		
		return lines;
	}
	
	/**
	 * The function takes a file and returns its contents as a list of strings, 
	 * one word per string.
	 * @param filename file to read
	 * @return contents of file as list of strings
	 * @throws IOException 
	 */
	public static ArrayList<String> getFileWordsAsStringList(String filename) throws IOException
	{
		InputStream fs = new FileInputStream(filename);
		if (filename.toLowerCase().endsWith(".gz"))
			fs = new GZIPInputStream(fs);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		ArrayList<String> words = new ArrayList<String>();
		String line;
		
		while ((line = in.readLine()) != null)
		{
			String [] tokens = line.trim().split("\\s+");

			for (String word : tokens)
				words.add(word);
		}
		
		in.close();
		
		return words;
	}

	public static ArrayList<String> getFileWordsAsStringList(String filename, String separator) throws IOException
	{
		InputStream fs = new FileInputStream(filename);
		if (filename.toLowerCase().endsWith(".gz"))
			fs = new GZIPInputStream(fs);
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		ArrayList<String> words = new ArrayList<String>();
		String line;
		
		while ((line = in.readLine()) != null)
		{
			String [] tokens = line.trim().split(separator);

			for (String word : tokens)
				words.add(word);
		}
		
		in.close();
		
		return words;
	}

	/**
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyFile(File inFile, File outFile) throws IOException
	{
		InputStream in = new FileInputStream(inFile);
		OutputStream out = new FileOutputStream(outFile);

		byte[] buf = new byte[2048];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

}
