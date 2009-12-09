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
	static vtkMath math = null;
	
	public static double[] computeIntersectionPolyLine(
			vtkPolyData polyData, 
			double[] vertex, 
			double[] ray1, 
			double[] ray2)
	{
		return null;
	}
	
	public static vtkPolyData computeFrustumIntersection(
			vtkPolyData polyData, 
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		if (math == null)
			math = new vtkMath();

//		math.Normalize(ul);
//		math.Normalize(ur);
//		math.Normalize(lr);
//		math.Normalize(ll);
		
		// First compute the normals of the 6 planes.
		// Start with computing the normals of the 4 side planes of the frustum.
		double[] top = new double[3];
		double[] right = new double[3];
		double[] bottom = new double[3];
		double[] left = new double[3];
		double[] near = new double[3];
		double[] far = new double[3];
		
		math.Cross(ur, ul, top);
		math.Cross(lr, ur, right);
		math.Cross(ll, lr, bottom);
		math.Cross(ul, ll, left);
		
		// let the near clipping plane be very close to the origin
		double dx = 0.1;
		double[] nearUL = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
		double[] nearUR = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
		double[] nearLL = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
		double[] nearLR = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
		double[] nearX = {nearLR[0]-nearLL[0], nearLR[1]-nearLL[1], nearLR[2]-nearLL[2]};
		double[] nearY = {nearUL[0]-nearLL[0], nearUL[1]-nearLL[1], nearUL[2]-nearLL[2]};
		math.Cross(nearX, nearY, near);
		
		// let the far clipping plane be a multiple of the distance to the origin
		dx = 2.0 * math.Norm(origin);
		double[] farUL = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
		double[] farUR = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
		double[] farLL = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
		double[] farLR = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
		double[] farX = {farLL[0]-farLR[0], farLL[1]-farLR[1], farLL[2]-farLR[2]};
		double[] farY = {farUR[0]-farLR[0], farUR[1]-farLR[1], farUR[2]-farLR[2]};
		math.Cross(farX, farY, far);
		
		vtkPoints points = new vtkPoints();
		points.SetNumberOfPoints(6);
		points.SetPoint(0, origin);
		points.SetPoint(1, origin);
		points.SetPoint(2, origin);
		points.SetPoint(3, origin);
		points.SetPoint(4, nearLL);
		points.SetPoint(5, farLR);
		
		vtkDoubleArray normals = new vtkDoubleArray();
		normals.SetNumberOfComponents(3);
		normals.SetNumberOfTuples(6);
		normals.SetTuple3(0, top[0], top[1], top[2]);
		normals.SetTuple3(1, right[0], right[1], right[2]);
		normals.SetTuple3(2, bottom[0], bottom[1], bottom[2]);
		normals.SetTuple3(3, left[0], left[1], left[2]);
		normals.SetTuple3(4, near[0], near[1], near[2]);
		normals.SetTuple3(5, far[0], far[1], far[2]);

		vtkPlanes implicitPlanes = new vtkPlanes();
		implicitPlanes.SetPoints(points);
		implicitPlanes.SetNormals(normals);
		
		vtkClipPolyData clipPolyData = new vtkClipPolyData();
		clipPolyData.SetInput(polyData);
		clipPolyData.SetClipFunction(implicitPlanes);
		clipPolyData.Update();
		
		vtkPolyData clippedData = new vtkPolyData();
		clippedData.DeepCopy(clipPolyData.GetOutput());
		
		// Now remove from this clipped poly data all the cells that are facing away from the viewer.
		
		return null;
	}

}
