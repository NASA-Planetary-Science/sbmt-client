package edu.jhuapl.near.util;

import vtk.*;

public class IntersectionUtil 
{
	static vtkMath math = null;
	
	static void printpt(double[] p, String s)
	{
		System.out.println(s + " " + p[0] + " " + p[1] + " " + p[2]);
	}
	
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
	public static double[] computeIntersectionPolyLinee(
			vtkPolyData polyData, 
			double[] vertex, 
			double[] ray1, 
			double[] ray2)
	{
		return null;
	}
	
	public static vtkPolyData computeFrustumIntersection(
			vtkPolyData polyData,
			vtkAbstractCellLocator locator,
			double[] origin, 
			double[] ul, 
			double[] ur,
			double[] lr,
			double[] ll)
	{
		if (math == null)
			math = new vtkMath();

		//vtkPolyData origPolyData = polyData;

		printpt(ul, "ul");
		printpt(ur, "ur");
		printpt(lr, "lr");
		printpt(ll, "ll");
//		math.Normalize(ul);
//		math.Normalize(ur);
//		math.Normalize(lr);
//		math.Normalize(ll);
		
		//printpt()
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
		//double[] nearUR = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
		double[] nearLL = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
		double[] nearLR = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
		double[] nearX = {nearLR[0]-nearLL[0], nearLR[1]-nearLL[1], nearLR[2]-nearLL[2]};
		double[] nearY = {nearUL[0]-nearLL[0], nearUL[1]-nearLL[1], nearUL[2]-nearLL[2]};
		math.Cross(nearX, nearY, near);
		
		// let the far clipping plane be a multiple of the distance to the origin
		dx = 2.0 * math.Norm(origin);
		//double[] farUL = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
		double[] farUR = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
		double[] farLL = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
		double[] farLR = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
		double[] farX = {farLL[0]-farLR[0], farLL[1]-farLR[1], farLL[2]-farLR[2]};
		double[] farY = {farUR[0]-farLR[0], farUR[1]-farLR[1], farUR[2]-farLR[2]};
		math.Cross(farX, farY, far);
		
		vtkPoints frustumPoints = new vtkPoints();
		frustumPoints.SetNumberOfPoints(6);
		frustumPoints.SetPoint(0, origin);
		frustumPoints.SetPoint(1, origin);
		frustumPoints.SetPoint(2, origin);
		frustumPoints.SetPoint(3, origin);
		frustumPoints.SetPoint(4, nearLL);
		frustumPoints.SetPoint(5, farLR);
		
		vtkDoubleArray frustumNormals = new vtkDoubleArray();
		frustumNormals.SetNumberOfComponents(3);
		frustumNormals.SetNumberOfTuples(6);
		frustumNormals.SetTuple3(0, top[0], top[1], top[2]);
		frustumNormals.SetTuple3(1, right[0], right[1], right[2]);
		frustumNormals.SetTuple3(2, bottom[0], bottom[1], bottom[2]);
		frustumNormals.SetTuple3(3, left[0], left[1], left[2]);
		frustumNormals.SetTuple3(4, near[0], near[1], near[2]);
		frustumNormals.SetTuple3(5, far[0], far[1], far[2]);

		vtkPlanes implicitPlanes = new vtkPlanes();
		implicitPlanes.SetPoints(frustumPoints);
		implicitPlanes.SetNormals(frustumNormals);
		
		vtkClipPolyData clipPolyData = new vtkClipPolyData();
		clipPolyData.SetInput(polyData);
		clipPolyData.SetClipFunction(implicitPlanes);
		clipPolyData.SetInsideOut(1);
		//clipPolyData.GetLocator().SetTolerance(0.0001);
		
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputConnection(clipPolyData.GetOutputPort());
		normalsFilter.SetComputeCellNormals(1);
		normalsFilter.SetComputePointNormals(0);
		normalsFilter.Update();
		
		polyData = new vtkPolyData();
		polyData.DeepCopy(normalsFilter.GetOutput());
		
		// Now remove from this clipped poly data all the cells that are facing away from the viewer.
		vtkDataArray cellNormals = polyData.GetCellData().GetNormals();
		vtkPoints points = polyData.GetPoints();
		
		int numCells = cellNormals.GetNumberOfTuples();
		
		vtkIdList idList = new vtkIdList();
		
		for (int i=0; i<numCells; ++i)
		{
			double[] n = cellNormals.GetTuple3(i);
			math.Normalize(n);
			
			// Compute the direction to the viewer from one of the point of the cell.
			polyData.GetCellPoints(i, idList);
			double[] pt = points.GetPoint(idList.GetId(0));
			
			double[] viewDir = {origin[0] - pt[0], origin[1] - pt[1], origin[2] - pt[2]};
			math.Normalize(viewDir);
			
			double dot = math.Dot(n, viewDir);
			if (dot <= 0.0)
				polyData.DeleteCell(i);
		}
		
		polyData.RemoveDeletedCells();
		
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInput(polyData);
		
		// If the eros body was a convex shape we would be done now.
		// Unfortunately, since it's not, it's possible for the polydata to have multiple connected
		// pieces in view of the camera and some of these pieces are obscured by other pieces. 
		// Thus first check how many connected pieces there are in the clipped polydata. 
		// If there's only one, we're done. If there's more than one, we need to remove the 
		// obscured cells. To remove
		// cells that are obscured by other cells we do the following: Go through every point in the
		// polydata and form a line segment connecting it to the origin (i.e. the camera
		// location). If this line segment intersects any other cell in the polydata, then we can remove
		// all cells that contain this point. Now you may argue that it's possible that such
		// cells are only partially obscured, not fully obscured and therefore we should split
		// these cells into pieces and only throw out the pieces that are fully obscured.
		// However, doing this would require a lot more computation and I don't think
		// going this far is really necessary. You might also argue that it's possible
		// for there to be cells whos points are not obscured though the interior of the cell
		// is obscured and thus this cell should have been removed. However, I think
		// this is highly unlikely given that the cells are all very small, and it's probably
		// not worth the trouble.
		
		// So now, first count the number of connected pieces.
		vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
		connectivityFilter.SetInputConnection(cleanPoly.GetOutputPort());
		connectivityFilter.SetExtractionModeToAllRegions();
		connectivityFilter.Update();
		int numRegions = connectivityFilter.GetNumberOfExtractedRegions();
		System.out.println("numRegions: " + numRegions);
		//if (numRegions == 1)
		if (true)
		{
			polyData = new vtkPolyData();
			polyData.DeepCopy(connectivityFilter.GetOutput());
			return polyData;
		}

		polyData = new vtkPolyData();
		polyData.DeepCopy(cleanPoly.GetOutput());
		polyData.BuildLinks(0);

//		vtkOBBTree locator;
		vtkPoints intersectPoints;
//        locator = new vtkOBBTree();
//        locator.SetDataSet(origPolyData);
//        locator.CacheCellBoundsOn();
//        locator.AutomaticOn();
//        //locator.SetMaxLevel(10);
//        //locator.SetNumberOfCellsPerNode(5);
//
//        locator.BuildLocator();

        intersectPoints = new vtkPoints();
//		System.out.println("deleted cell");

		points = polyData.GetPoints();
		int numPoints = points.GetNumberOfPoints();
		for (int i=0; i<numPoints; ++i)
		{
			double[] sourcePnt = points.GetPoint(i);
			//double[] p = sourcePnt;
			//System.out.println("source pnt " + p[0] + " " + p[1] + " " + p[2]);

			intersectPoints.Reset();

			/*int v =*/ locator.IntersectWithLine(origin, sourcePnt, intersectPoints, null);
			//System.out.println("v " + v);
			if (intersectPoints.GetNumberOfPoints() >= 1)
			{
				// If there's only 1 intersection make sure the intersection point is
				// not sourcePnt
				if (intersectPoints.GetNumberOfPoints() == 1)
				{
					double[] pt = intersectPoints.GetPoint(0);
					if (math.Distance2BetweenPoints(sourcePnt, pt) < 1e-10)
					{
						//System.out.println("bad");
						continue;
					}
				}
				else
				{
					//System.out.println("not bad");
				}
				
				polyData.GetPointCells(i, idList);
				
				//p = intersectPoints.GetPoint(0);
				//System.out.println("intersect pt " + intersectPoints.GetNumberOfPoints() + "  "  + p[0] + " " + p[1] + " " + p[2]);
				int numPtCells = idList.GetNumberOfIds();
				for (int j=0; j<numPtCells; ++j)
				{
					polyData.DeleteCell(idList.GetId(j));
					//System.out.println("deleted cell");
				}
			}

		}
		polyData.RemoveDeletedCells();
		
		return polyData;
	}

}
