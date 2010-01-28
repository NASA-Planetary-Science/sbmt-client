package edu.jhuapl.near.util;

public class Point 
{
	public double[] xyz = new double[3];
	
	public Point(double[] pt)
	{
		xyz[0] = pt[0];
		xyz[1] = pt[1];
		xyz[2] = pt[2];
	}
}
