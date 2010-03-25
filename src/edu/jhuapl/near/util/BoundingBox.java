package edu.jhuapl.near.util;

public class BoundingBox 
{
	public double xmin = Double.MAX_VALUE;
	public double xmax = -Double.MAX_VALUE;
	public double ymin = Double.MAX_VALUE;
	public double ymax = -Double.MAX_VALUE;
	public double zmin = Double.MAX_VALUE;
	public double zmax = -Double.MAX_VALUE;
	
	public BoundingBox()
	{
	}
	
	public BoundingBox(double[] bounds)
	{
		setBounds(bounds);
	}
	
	public void setBounds(double[] bounds)
	{
		xmin = bounds[0];
		xmax = bounds[1];
		ymin = bounds[2];
		ymax = bounds[3];
		zmin = bounds[4];
		zmax = bounds[5];
	}
	
	public void update(double x, double y, double z)
	{
        if (xmin > x)
        	xmin = x;
        if (xmax < x)
        	xmax = x;
        if (ymin > y)
        	ymin = y;
        if (ymax < y)
        	ymax = y;
        if (zmin > z)
        	zmin = z;
        if (zmax < z)
        	zmax = z;
	}
	
	public boolean intersects(BoundingBox other)
	{
		if (other.xmax >= xmin && other.xmin <= xmax &&
			other.ymax >= ymin && other.ymin <= ymax &&
			other.zmax >= zmin && other.zmin <= zmax)
			return true;
		else
			return false;
	}
	
	public double getLargestSide()
	{
		return Math.max(xmax-xmin, Math.max(ymax-ymin, zmax-zmin));
	}
	
	public String toString()
	{
		return "xmin: " + xmin + " xmax: " + xmax +
			   " ymin: " + ymin + " ymax: " + ymax +
			   " zmin: " + zmin + " zmax: " + zmax;
	}
	
}
