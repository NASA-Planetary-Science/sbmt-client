package edu.jhuapl.near.util;

import vtk.*;

/**
 * This class contains various utility functions for operating on vtkPolyData
 * @author kahneg1
 *
 */
public class PolyDataUtil 
{
	private static vtkMath math = null;
	
	private static void printpt(double[] p, String s)
	{
		System.out.println(s + " " + p[0] + " " + p[1] + " " + p[2]);
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

		printpt(origin, "origin");
		printpt(ul, "ul");
		printpt(ur, "ur");
		printpt(lr, "lr");
		printpt(ll, "ll");
		
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
		
		dx = math.Norm(origin);
		double[] UL2 = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
		double[] UR2 = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
		double[] LL2 = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
		double[] LR2 = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
		

		vtkPlane plane1 = new vtkPlane();
		plane1.SetOrigin(UL2);
		plane1.SetNormal(top);
		vtkPlane plane2 = new vtkPlane();
		plane2.SetOrigin(UR2);
		plane2.SetNormal(right);
		vtkPlane plane3 = new vtkPlane();
		plane3.SetOrigin(LR2);
		plane3.SetNormal(bottom);
		vtkPlane plane4 = new vtkPlane();
		plane4.SetOrigin(LL2);
		plane4.SetNormal(left);
//		vtkPlane plane5 = new vtkPlane();
//		plane5.SetOrigin(nearLL);
//		plane5.SetNormal(near);
//		vtkPlane plane6 = new vtkPlane();
//		plane6.SetOrigin(UL2);
//		plane6.SetNormal(farLR);
		
		// I found that the results are MUCH better when you use a separate vtkClipPolyData
		// for each plane of the frustum rather than trying to use a single vtkClipPolyData
		// with an vtkImplicitBoolean or vtkPlanes that combines all the planes together.
		vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
		clipPolyData1.SetInput(polyData);
		clipPolyData1.SetClipFunction(plane1);
		clipPolyData1.SetInsideOut(1);
		vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
		clipPolyData2.SetInputConnection(clipPolyData1.GetOutputPort());
		clipPolyData2.SetClipFunction(plane2);
		clipPolyData2.SetInsideOut(1);
		vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
		clipPolyData3.SetInputConnection(clipPolyData2.GetOutputPort());
		clipPolyData3.SetClipFunction(plane3);
		clipPolyData3.SetInsideOut(1);
		vtkClipPolyData clipPolyData4 = new vtkClipPolyData();
		clipPolyData4.SetInputConnection(clipPolyData3.GetOutputPort());
		clipPolyData4.SetClipFunction(plane4);
		clipPolyData4.SetInsideOut(1);
		clipPolyData4.Update();
		
		if (clipPolyData4.GetOutput().GetNumberOfCells() == 0)
			return null;
		
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInputConnection(clipPolyData4.GetOutputPort());
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
		polyData.GetCellData().SetNormals(null);
		
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInput(polyData);
		cleanPoly.Update();
		
		polyData = new vtkPolyData();
		polyData.DeepCopy(cleanPoly.GetOutput());
		
		// If the Eros body was a convex shape we would be done now.
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
		if (numRegions == 1)
		{
			return polyData;
		}

		polyData.BuildLinks(0);

		vtkPoints intersectPoints = new vtkPoints();

		points = polyData.GetPoints();
		int numPoints = points.GetNumberOfPoints();
		
		for (int i=0; i<numPoints; ++i)
		{
			double[] sourcePnt = points.GetPoint(i);

			intersectPoints.Reset();

			locator.IntersectWithLine(origin, sourcePnt, intersectPoints, null);

			if (intersectPoints.GetNumberOfPoints() >= 1)
			{
				// If there's only 1 intersection point make sure the intersection point is
				// not sourcePnt
				if (intersectPoints.GetNumberOfPoints() == 1)
				{
					double[] pt = intersectPoints.GetPoint(0);
					if (math.Distance2BetweenPoints(sourcePnt, pt) < 1e-6)
					{
						continue;
					}
				}
				
				polyData.GetPointCells(i, idList);
				
				int numPtCells = idList.GetNumberOfIds();
				for (int j=0; j<numPtCells; ++j)
				{
					polyData.DeleteCell(idList.GetId(j));
				}
			}
		}
		polyData.RemoveDeletedCells();

		System.out.println("before clean  " + polyData.GetNumberOfPoints());

		cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInput(polyData);
		cleanPoly.Update();

		polyData = new vtkPolyData();
		polyData.DeepCopy(cleanPoly.GetOutput());

		System.out.println("after clean  " + polyData.GetNumberOfPoints());
		
		return polyData;
	}

	public static vtkPolyData computeConeIntersection(
			vtkPolyData polyData,
			vtkAbstractCellLocator locator,
			double[] origin, 
			double[] vec,
			double angle)
	{
		if (math == null)
			math = new vtkMath();

//		double[] vec1 = {pt1[0]-origin[0], pt1[1]-origin[1], pt1[2]-origin[2]};
//		double[] vec2 = {pt2[0]-origin[0], pt2[1]-origin[1], pt2[2]-origin[2]};
//		double[] normal = new double[3];
//		math.Cross(vec1, vec2, normal);
//		math.Normalize(normal);

		vtkCone cone = new vtkCone();
		cone.SetAngle(angle);
		
		vtkTransform transform = new vtkTransform();
		
		cone.SetTransform(transform);
		
		vtkClipPolyData clipPolyData = new vtkClipPolyData();
		clipPolyData.SetInput(polyData);
		clipPolyData.SetClipFunction(cone);
		clipPolyData.SetInsideOut(1);
		clipPolyData.Update();
		
		polyData = new vtkPolyData();
		polyData.DeepCopy(clipPolyData.GetOutput());
		
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInput(polyData);
        writer.SetFileName("/tmp/coneeros.vtk");
        //writer.SetFileTypeToBinary();
        writer.Write();

		return polyData;
	}

    private static vtkOBBTree lineLocator;

	public static vtkPolyData computePlaneIntersectionBetween2Points(
			vtkPolyData polyData,
			vtkAbstractCellLocator locator,
			double[] origin, 
			double[] pt1,
			double[] pt2)
	{
		if (math == null)
			math = new vtkMath();
		if (lineLocator == null)
		{
			lineLocator = new vtkOBBTree();
			lineLocator.CacheCellBoundsOn();
			lineLocator.AutomaticOn();
	        //lineLocator.SetMaxLevel(10);
	        //lineLocator.SetNumberOfCellsPerNode(5);
			lineLocator.BuildLocator();
		}
		
		double[] vec1 = {pt1[0]-origin[0], pt1[1]-origin[1], pt1[2]-origin[2]};
		double[] vec2 = {pt2[0]-origin[0], pt2[1]-origin[1], pt2[2]-origin[2]};
		double[] normal = new double[3];
		math.Cross(vec1, vec2, normal);
		math.Normalize(normal);

		vtkPlane plane = new vtkPlane();
		plane.SetOrigin(pt1);
		plane.SetNormal(normal);

		vtkCutter clipPolyData = new vtkCutter();
		clipPolyData.SetInput(polyData);
		clipPolyData.SetCutFunction(plane);
		clipPolyData.Update();
		
		// Get the cells closest to pt1 and p2
		locator.FindCell(pt1);
		
		polyData = new vtkPolyData();
		polyData.DeepCopy(clipPolyData.GetOutput());
		
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInput(polyData);
        writer.SetFileName("/tmp/cuteros.vtk");
        //writer.SetFileTypeToBinary();
        writer.Write();

		return polyData;
	}
	
	public static void shiftPolyDataInNormalDirection(vtkPolyData polyData, double shiftAmount)
	{
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInput(polyData);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.Update();
		
		vtkDataArray pointNormals = normalsFilter.GetOutput().GetPointData().GetNormals();
		vtkPoints points = polyData.GetPoints();
		
		int numPoints = points.GetNumberOfPoints();
		
		for (int i=0; i<numPoints; ++i)
		{
			double[] point = points.GetPoint(i);
			double[] normal = pointNormals.GetTuple3(i);
			
			point[0] += normal[0]*shiftAmount;
			point[1] += normal[1]*shiftAmount;
			point[2] += normal[2]*shiftAmount;
			
			points.SetPoint(i, point);
		}
		
		polyData.Modified();
	}

	/*
	public static void shiftPolyLineInNormalDirectionOfPolyData(
			vtkPolyData polyLine,
			vtkPolyData polyData,
			double shiftAmount)
	{
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInput(polyData);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.Update();
		
		vtkDataArray pointNormals = normalsFilter.GetOutput().GetPointData().GetNormals();
		vtkPoints points = polyData.GetPoints();
		
		int numPoints = points.GetNumberOfPoints();
		
		for (int i=0; i<numPoints; ++i)
		{
			double[] point = points.GetPoint(i);
			double[] normal = pointNormals.GetTuple3(i);
			
			point[0] += normal[0]*shiftAmount;
			point[1] += normal[1]*shiftAmount;
			point[2] += normal[2]*shiftAmount;
			
			points.SetPoint(i, point);
		}
		
		polyData.Modified();
	}
	
	public static vtkPoints vtkPointToDouble(vtkPoints inPoints)
	{
		vtkPoints outPoints = new vtkPoints();
		outPoints.SetDataTypeToDouble();

		int numPoints = inPoints.GetNumberOfPoints();
		
		outPoints.SetNumberOfPoints(numPoints);

		
		for (int i=0; i<numPoints; ++i)
		{
			double[] point = inPoints.GetPoint(i);
			outPoints.SetPoint(i, point);
		}
	
		return outPoints;
	}
	*/
}
