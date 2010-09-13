package edu.jhuapl.near.util;

public class Mapmaker
{
	private double latitude;
	private double longitude;
	private int halfWidth;
	private double pixelSize;

	public void runMapmaker()
	{
		
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
