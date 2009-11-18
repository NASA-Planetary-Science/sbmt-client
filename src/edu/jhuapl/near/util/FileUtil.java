package edu.jhuapl.near.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileUtil 
{
	/**
	 * The function takes a file and returns its contents as a list of strings, 
	 * one line per string.
	 * @param filename file to read
	 * @return contents of file as list of strings
	 * @throws IOException 
	 */
	public static ArrayList<String> getFileLinesAsStringList(String filename)
	{
		FileInputStream fs = null;
		try 
		{
			fs = new FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		ArrayList<String> lines = new ArrayList<String>();
		String line;
		
		try 
		{
			while ((line = in.readLine()) != null)
			{
				lines.add(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return lines;
	}
	
	/**
	 * The function takes a file and returns its contents as a list of strings, 
	 * one word per string.
	 * @param filename file to read
	 * @return contents of file as list of strings
	 * @throws IOException 
	 */
	public static ArrayList<String> getFileWordsAsStringList(String filename)
	{
		FileInputStream fs = null;
		try 
		{
			fs = new FileInputStream(filename);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		ArrayList<String> words = new ArrayList<String>();
		String line;
		
		try 
		{
			while ((line = in.readLine()) != null)
			{
	            String [] tokens = line.split("\\s");

	            for (String word : tokens)
	            	words.add(word);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return words;
	}
	
}
