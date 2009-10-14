package edu.jhuapl.near;

import java.io.*;

public class Database 
{
	String version;
	File rootDir;
	String urlBase;
	double cellSize;
	int numRows;
	int numCols;
	
	Database()
	{
		// Create a directory in the user's home space for storing
		// the cells of the database that get downloaded.
	}
	
	File getCell(int row, int col)
	{
		// First check the cache to see if the cell index is there. If
		// not download it.
		
	}
	
	ArrayList<File> getCells(
			double latMin,
			double lonMin,
			double latMax,
			double lonMax)
	{
		for (int i=0; i<numRows; ++i)
			for (int j=0; j<numCols; ++j)
			{
				
			}
	}
}
