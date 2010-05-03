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
		double[] vec = {
				pt.xyz[0]-xyz[0],
				pt.xyz[1]-xyz[1],
				pt.xyz[2]-xyz[2]
		};
		return Spice.vnorm(vec);
//		return Math.sqrt(
//				(pt.xyz[0]-xyz[0])*(pt.xyz[0]-xyz[0]) +
//				(pt.xyz[1]-xyz[1])*(pt.xyz[1]-xyz[1]) +
//				(pt.xyz[2]-xyz[2])*(pt.xyz[2]-xyz[2])
//				);
	}

	static public double distanceBetween(Point3D pt1, Point3D pt2)
	{
		double[] vec = {
				pt2.xyz[0]-pt1.xyz[0],
				pt2.xyz[1]-pt1.xyz[1],
				pt2.xyz[2]-pt1.xyz[2]
		};
		return Spice.vnorm(vec);
	}
}
