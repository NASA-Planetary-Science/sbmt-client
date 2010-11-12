package edu.jhuapl.near.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil 
{
	private static volatile boolean abortUnzipping = false;
	private static volatile double unzipProgress = 0.0;
	
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

	public static void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buf = new byte[2048];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
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

		copyInputStream(in, out);
	}

	/**
	 * The following function is adapted from http://www.devx.com/getHelpOn/10MinuteSolution/20447
	 * @param file
	 */
	public static void unzipFile(File file, String extractToFolder)
	{
		Enumeration entries;
		ZipFile zipFile;

		try 
		{
			zipFile = new ZipFile(file);

			entries = zipFile.entries();

			unzipProgress = 0.0;
			
			int count = 0;
			int totalEntries = zipFile.size();
			
			while(entries.hasMoreElements())
			{
				unzipProgress = 100.0 * (double)count / (double)totalEntries;
				
				if (abortUnzipping)
				{
					break;
				}

				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory())
				{
					//System.err.println("Extracting directory: " + entry.getName());
					(new File(extractToFolder + File.separator + entry.getName())).mkdirs();
					continue;
				}

				//System.err.println("Extracting file: " + entry.getName());
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(extractToFolder + File.separator + entry.getName())));
				
				++count;
			}

			zipFile.close();
			
			unzipProgress = 100.0;
		}
		catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}

	static public void abortUnzipping()
	{
		System.out.println("aborted");
		abortUnzipping = true;
	}
	
	static public double getUnzipProgress()
	{
		return unzipProgress;
	}

	static public void resetUnzipProgress()
	{
		unzipProgress = 0.0;
	}
}
