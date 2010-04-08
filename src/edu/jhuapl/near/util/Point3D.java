package edu.jhuapl.near.util;

public class Point3D 
{
	public double[] xyz = new double[3];
	
	public Point3D(double[] pt)
	{
		xyz[0] = pt[0];
		xyz[1] = pt[1];
		xyz[2] = pt[2];
	}
	
	public String toString()
	{
		return "(" + xyz[0] + "," + xyz[1] + "," + xyz[2] + ")";
	}
	
	public double distanceTo(Point3D pt)
	{
		return Math.sqrt(
				(pt.xyz[0]-xyz[0])*(pt.xyz[0]-xyz[0]) +
				(pt.xyz[1]-xyz[1])*(pt.xyz[1]-xyz[1]) +
				(pt.xyz[2]-xyz[2])*(pt.xyz[2]-xyz[2])
				);
	}
}
