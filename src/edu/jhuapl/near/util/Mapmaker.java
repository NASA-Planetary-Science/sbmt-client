package edu.jhuapl.near.util;

import java.io.File;
import java.util.ArrayList;


public class Mapmaker
{
	private String mapmakerRootDir;
	private ProcessBuilder processBuilder;
	private ArrayList<String> processCommand = new ArrayList<String>();
	private double latitude;
	private double longitude;
	private int halfWidth;
	private double pixelSize;
	private String name;

	public Mapmaker()
	{
		File file = FileCache.getFileFromServer("/MSI/mapmaker.zip");
		mapmakerRootDir = file.getParent() + File.separator + "mapmaker";
		
		processBuilder = new ProcessBuilder(processCommand);
	}
	
	public void runMapmaker()
	{
		processCommand.clear();
		//processCommand.add
//		processCommand.add(halfWidth);
	}
	
	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public int getHalfWidth()
	{
		return halfWidth;
	}

	public void setHalfWidth(int halfWidth)
	{
		this.halfWidth = halfWidth;
	}

	public double getPixelSize()
	{
		return pixelSize;
	}

	public void setPixelSize(double pixelSize)
	{
		this.pixelSize = pixelSize;
	}

}
