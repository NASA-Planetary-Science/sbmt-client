package edu.jhuapl.near.util;

import vtk.*;

public class IntersectionUtil 
{
	/**
	 * Given a <code>polyData</code>, a vertex and two rays extending from that vertex forming a plane
	 * that intersects the polydata, compute the intersection of that plane (bounded by the 2 rays) and 
	 * the polydata. This intersection should be in the form of a polyline.
	 * @param polyData
	 * @param vertex
	 * @param ray1
	 * @param ray2
	 * @return
	 */
	public static double[] computeIntersectionPolyLine(
			vtkPolyData polyData, 
			double[] vertex, 
			double[] ray1, 
			double[] ray2)
	{
		return null;
	}
	
	public static double[] computeFrustumIntersection(
			vtkPolyData polyData, 
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		return null;
	}

}
