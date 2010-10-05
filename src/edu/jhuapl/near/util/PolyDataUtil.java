package edu.jhuapl.near.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import vtk.*;

/**
 * This class contains various utility functions for operating on a vtkPolyData.
 *
 * Note this class is written in the following crazy style. Each public function
 * is in it's own static class. The reason for this is so that each function can
 * use static variables.
 * The reason for these static variables is that the functions in this file can potentially
 * be called many times within the program and we don't want to keep reallocating vtk data
 * for them. Thus we declare all the vtk classes as static and they are declared once 
 * and always reused with new inputs. Another reason for these static variables is that
 * since these functions may be used as part of a pipeline, we don't want filters or 
 * other vtk objects to get garbage collected if the pipeline is still active following 
 * this function call. 
 * 
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

	public static class ComputeFrustumIntersection
	{
		private static vtkPlane plane1_f4;
		private static vtkPlane plane2_f4;
		private static vtkPlane plane3_f4;
		private static vtkPlane plane4_f4;
		private static vtkClipPolyData clipPolyData1_f4;
		private static vtkClipPolyData clipPolyData2_f4;
		private static vtkClipPolyData clipPolyData3_f4;
		private static vtkClipPolyData clipPolyData4_f4;
		private static vtkPolyDataNormals normalsFilter_f4;
		private static vtkPolyData tmpPolyData_f4;
		private static vtkIdList idList_f4;
		private static vtkCleanPolyData cleanPoly_f4;
		//private static vtkPolyDataConnectivityFilter connectivityFilter_f4;
		private static vtkGenericCell cell_f4;
		public static vtkPolyData func(
				vtkPolyData polyData,
				vtksbCellLocator locator,
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
			dx = 2.0 * GeometryUtil.vnorm(origin);
			//double[] farUL = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
			double[] farUR = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
			double[] farLL = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
			double[] farLR = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};
			double[] farX = {farLL[0]-farLR[0], farLL[1]-farLR[1], farLL[2]-farLR[2]};
			double[] farY = {farUR[0]-farLR[0], farUR[1]-farLR[1], farUR[2]-farLR[2]};
			math.Cross(farX, farY, far);

			dx = GeometryUtil.vnorm(origin);
			double[] UL2 = {origin[0]+ul[0]*dx, origin[1]+ul[1]*dx, origin[2]+ul[2]*dx};
			double[] UR2 = {origin[0]+ur[0]*dx, origin[1]+ur[1]*dx, origin[2]+ur[2]*dx};
			double[] LL2 = {origin[0]+ll[0]*dx, origin[1]+ll[1]*dx, origin[2]+ll[2]*dx};
			double[] LR2 = {origin[0]+lr[0]*dx, origin[1]+lr[1]*dx, origin[2]+lr[2]*dx};


			if (plane1_f4 == null) plane1_f4 = new vtkPlane();
			plane1_f4.SetOrigin(UL2);
			plane1_f4.SetNormal(top);
			if (plane2_f4 == null) plane2_f4 = new vtkPlane();
			plane2_f4.SetOrigin(UR2);
			plane2_f4.SetNormal(right);
			if (plane3_f4 == null) plane3_f4 = new vtkPlane();
			plane3_f4.SetOrigin(LR2);
			plane3_f4.SetNormal(bottom);
			if (plane4_f4 == null) plane4_f4 = new vtkPlane();
			plane4_f4.SetOrigin(LL2);
			plane4_f4.SetNormal(left);

			// I found that the results are MUCH better when you use a separate vtkClipPolyData
			// for each plane of the frustum rather than trying to use a single vtkClipPolyData
			// with an vtkImplicitBoolean or vtkPlanes that combines all the planes together.
			if (clipPolyData1_f4 == null) clipPolyData1_f4 = new vtkClipPolyData();
			clipPolyData1_f4.SetInput(polyData);
			clipPolyData1_f4.SetClipFunction(plane1_f4);
			clipPolyData1_f4.SetInsideOut(1);
			if (clipPolyData2_f4 == null) clipPolyData2_f4 = new vtkClipPolyData();
			clipPolyData2_f4.SetInputConnection(clipPolyData1_f4.GetOutputPort());
			clipPolyData2_f4.SetClipFunction(plane2_f4);
			clipPolyData2_f4.SetInsideOut(1);
			if (clipPolyData3_f4 == null) clipPolyData3_f4 = new vtkClipPolyData();
			clipPolyData3_f4.SetInputConnection(clipPolyData2_f4.GetOutputPort());
			clipPolyData3_f4.SetClipFunction(plane3_f4);
			clipPolyData3_f4.SetInsideOut(1);
			if (clipPolyData4_f4 == null) clipPolyData4_f4 = new vtkClipPolyData();
			clipPolyData4_f4.SetInputConnection(clipPolyData3_f4.GetOutputPort());
			clipPolyData4_f4.SetClipFunction(plane4_f4);
			clipPolyData4_f4.SetInsideOut(1);
			clipPolyData4_f4.Update();

			if (clipPolyData4_f4.GetOutput().GetNumberOfCells() == 0)
			{
				System.out.println("clipped data is empty");
				return null;
			}

			if (normalsFilter_f4 == null) normalsFilter_f4 = new vtkPolyDataNormals();
			normalsFilter_f4.SetInputConnection(clipPolyData4_f4.GetOutputPort());
			normalsFilter_f4.SetComputeCellNormals(1);
			normalsFilter_f4.SetComputePointNormals(0);
			normalsFilter_f4.SplittingOff();
			normalsFilter_f4.Update();

			if (tmpPolyData_f4 == null) tmpPolyData_f4 = new vtkPolyData();
			tmpPolyData_f4.DeepCopy(normalsFilter_f4.GetOutput());

			// Now remove from this clipped poly data all the cells that are facing away from the viewer.
			vtkDataArray cellNormals = tmpPolyData_f4.GetCellData().GetNormals();
			vtkPoints points = tmpPolyData_f4.GetPoints();

			int numCells = cellNormals.GetNumberOfTuples();

			if (idList_f4 == null) idList_f4 = new vtkIdList();
			idList_f4.SetNumberOfIds(0);

			double[] viewDir = new double[3];
			for (int i=0; i<numCells; ++i)
			{
				double[] n = cellNormals.GetTuple3(i);
				GeometryUtil.vhat(n, n);

				// Compute the direction to the viewer from one of the point of the cell.
				tmpPolyData_f4.GetCellPoints(i, idList_f4);
				double[] pt = points.GetPoint(idList_f4.GetId(0));

				viewDir[0] = origin[0] - pt[0];
				viewDir[1] = origin[1] - pt[1];
				viewDir[2] = origin[2] - pt[2];
				GeometryUtil.vhat(viewDir, viewDir);

				double dot = math.Dot(n, viewDir);
				if (dot <= 0.0)
					tmpPolyData_f4.DeleteCell(i);
			}

			tmpPolyData_f4.RemoveDeletedCells();
			tmpPolyData_f4.GetCellData().SetNormals(null);

			if (cleanPoly_f4 == null) cleanPoly_f4 = new vtkCleanPolyData();
			cleanPoly_f4.SetInput(tmpPolyData_f4);
			cleanPoly_f4.Update();

			//polyData = new vtkPolyData();
			tmpPolyData_f4.DeepCopy(cleanPoly_f4.GetOutput());

			// If the body was a convex shape we would be done now.
			// Unfortunately, since it's not, it's possible for the polydata to have multiple connected
			// pieces in view of the camera and some of these pieces are obscured by other pieces. 
			// Thus first check how many connected pieces there are in the clipped polydata. 
			// If there's only one, we're done. If there's more than one, we need to remove the 
			// obscured cells. To remove
			// cells that are obscured by other cells we do the following: Go through every point in the
			// polydata and form a line segment connecting it to the origin (i.e. the camera
			// location). Remove all cells for which all three of its points are obscured.
			// Now you may argue that it's possible that such
			// cells are only partially obscured (if say only one of its points are obscured),
			// not fully obscured and therefore we should split
			// these cells into pieces and only throw out the pieces that are fully obscured.
			// However, doing this would require a lot more computation and I don't think
			// going this far is really necessary. You might also argue that it's possible
			// for there to be cells whos points are not obscured though the interior of the cell
			// is obscured and thus this cell should have been removed. However, I think
			// this is highly unlikely given that the cells are all very small, and it's probably
			// not worth the trouble.

			// So now, first count the number of connected pieces.
//			if (connectivityFilter_f4 == null) connectivityFilter_f4 = new vtkPolyDataConnectivityFilter();
//			connectivityFilter_f4.SetInputConnection(cleanPoly_f4.GetOutputPort());
//			connectivityFilter_f4.SetExtractionModeToAllRegions();
//			connectivityFilter_f4.Update();
//			int numRegions = connectivityFilter_f4.GetNumberOfExtractedRegions();
//			System.out.println("numRegions: " + numRegions);
//			if (numRegions == 1)
//			{
//				return tmpPolyData_f4;
//			}



			if (cell_f4 == null)
			    cell_f4 = new vtkGenericCell();

			points = tmpPolyData_f4.GetPoints();
			int numPoints = points.GetNumberOfPoints();

			int[] numberOfObscuredPointsPerCell = new int[tmpPolyData_f4.GetNumberOfCells()];
			Arrays.fill(numberOfObscuredPointsPerCell, 0);

            double tol = 1e-6;
            double[] t = new double[1];
            double[] x = new double[3];
            double[] pcoords = new double[3];
            int[] subId = new int[1];
            int[] cell_id = new int[1];
            
			for (int i=0; i<numPoints; ++i)
			{
				double[] sourcePnt = points.GetPoint(i);

                int result = locator.IntersectWithLine(origin, sourcePnt, tol, t, x, pcoords, subId, cell_id, cell_f4);

				if (result == 1)
				{
				    int ptid = polyData.FindPoint(sourcePnt);
				    polyData.GetPointCells(ptid, idList_f4);
				    
				    // The following check makes sure we don't delete any cells
				    // if the intersection point happens to coincides with sourcePnt.
				    // To do this we test to see of the intersected cell 
				    // is one of the cells which share a point with sourcePnt.
				    // If it is we skip to the next point.
                    if (idList_f4.IsId(cell_id[0]) >= 0)
                    {
                        //System.out.println("Too close  " + i);
                        continue;
                    }
				    
					tmpPolyData_f4.GetPointCells(i, idList_f4);
                    int numPtCells = idList_f4.GetNumberOfIds();
					for (int j=0; j<numPtCells; ++j)
					{
						// The following makes sure that only cells for which ALL three of its
						// points are obscured get deleted
						int cellId = idList_f4.GetId(j);
						++numberOfObscuredPointsPerCell[cellId];
						if (numberOfObscuredPointsPerCell[cellId] == 3)
							tmpPolyData_f4.DeleteCell(cellId);
					}
				}
			}
			tmpPolyData_f4.RemoveDeletedCells();

			//cleanPoly_f4 = new vtkCleanPolyData();
			cleanPoly_f4.SetInput(tmpPolyData_f4);
			cleanPoly_f4.Update();

			//polyData = new vtkPolyData();
			tmpPolyData_f4.DeepCopy(cleanPoly_f4.GetOutput());

			return tmpPolyData_f4;
		}
	}

	/*
	 * This is an older version of that uses a vtkCylinder to do
	 * the intersection rather than a series of planes. Unfortunately, the results
	 * look crappy. Use drawPolygonOnPolyData instead.
	 */
	/*

	public static class DrawCircleOnPolyData
	{
	public static vtkPolyData func(
			vtkPolyData polyData,
			vtkAbstractPointLocator pointLocator,
			double[] center, 
			double radius,
			boolean filled)
	{
		if (math == null)
			math = new vtkMath();

		double[] normal = getPolyDataNormalAtPoint(center, polyData, pointLocator);

		radius += 0.5;
		if (radius < 1.0)
			radius = 1.0;

		vtkCylinder cylinder = new vtkCylinder();
		cylinder.SetRadius(radius);
		cylinder.SetCenter(center);

		// Note the cylinder has its axis pointing to the y-axis (0,1,0). 
		// Create a transform that rotates this axis to normal.
		// To do this we need to first find the axis of rotation
		// (note don't confuse this with the axis of the cylindar. 
		// We're using the word axis to refer to 2 different things)
		// which is the cross product between the y-axis and normal.
		double[] originalCylindarAxis = {0.0, 1.0, 0.0};
		double[] axisOfRotation = new double[3];
		math.Cross(normal, originalCylindarAxis, axisOfRotation);
		Spice.vhat(axisOfRotation,axisOfRotation);

		// Now compute the angle between these 2 cylinder axes.
		double angle = Spice.vsep(originalCylindarAxis, normal) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(center);
		transform.RotateWXYZ(angle, axisOfRotation);
		transform.Translate(-center[0],-center[1],-center[2]);

		cylinder.SetTransform(transform);

		vtkPolyDataAlgorithm filter = null;

		if(filled)
		{
			vtkClipPolyData clipPolyData = new vtkClipPolyData();
			clipPolyData.SetInput(polyData);
			clipPolyData.SetClipFunction(cylinder);
			clipPolyData.SetInsideOut(1);
			clipPolyData.Update();
			filter = clipPolyData;
		}
		else
		{
			vtkCutter cutPolyData = new vtkCutter();
			cutPolyData.SetInput(polyData);
			cutPolyData.SetCutFunction(cylinder);
			cutPolyData.Update();
			filter = cutPolyData;
		}

		vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
		connectivityFilter.SetInputConnection(filter.GetOutputPort());
		connectivityFilter.SetExtractionModeToClosestPointRegion();
		connectivityFilter.SetClosestPoint(center);
		connectivityFilter.Update();

		polyData = new vtkPolyData();
		polyData.DeepCopy(connectivityFilter.GetOutput());


//        vtkPolyDataWriter writer = new vtkPolyDataWriter();
//        writer.SetInput(polyData);
//        writer.SetFileName("/tmp/coneeros.vtk");
//        //writer.SetFileTypeToBinary();
//        writer.Write();

		return polyData;
	}
	}
	 */

	public static class DrawPolygonOnPolyData
	{
		static private ArrayList<vtkClipPolyData> clipFilters_f1 = new ArrayList<vtkClipPolyData>();
		static private ArrayList<vtkPlane> clipPlanes_f1 = new ArrayList<vtkPlane>();
		static private ArrayList<vtkPolyData> clipOutputs_f1 = new ArrayList<vtkPolyData>(); // not sure is this one is really needed
		static private vtkSphere sphere_f1;
		static private vtkExtractPolyDataGeometry extract_f1;
		static private vtkRegularPolygonSource polygonSource_f1;
		static private vtkPolyDataConnectivityFilter connectivityFilter_f1;
		static private vtkFeatureEdges edgeExtracter_f1;
		public static void func(
				vtkPolyData polyData,
				vtkAbstractPointLocator pointLocator,
				double[] center, 
				double radius,
				int numberOfSides,
				vtkPolyData outputInterior,
				vtkPolyData outputBoundary)
		{
			if (math == null)
				math = new vtkMath();

			double[] normal = GetPolyDataNormalAtPoint.func(center, polyData, pointLocator);


			// Reduce the size of the polydata we need to process by only
			// considering cells within twice radius of center.
			//vtkSphere sphere = new vtkSphere();
			if (sphere_f1 == null)
				sphere_f1 = new vtkSphere();
			sphere_f1.SetCenter(center);
			sphere_f1.SetRadius(radius >= 0.2 ? 1.2*radius : 1.2*0.2);

			//vtkExtractPolyDataGeometry extract = new vtkExtractPolyDataGeometry();
			if (extract_f1 == null)
				extract_f1 = new vtkExtractPolyDataGeometry();
			extract_f1.SetImplicitFunction(sphere_f1);
			extract_f1.SetExtractInside(1);
			extract_f1.SetExtractBoundaryCells(1);
			extract_f1.SetInput(polyData);
			extract_f1.Update();
			polyData = extract_f1.GetOutput();


			//vtkRegularPolygonSource polygonSource = new vtkRegularPolygonSource();
			if (polygonSource_f1 == null)
				polygonSource_f1 = new vtkRegularPolygonSource();
			polygonSource_f1.SetCenter(center);
			polygonSource_f1.SetRadius(radius);
			polygonSource_f1.SetNormal(normal);
			polygonSource_f1.SetNumberOfSides(numberOfSides);
			polygonSource_f1.SetGeneratePolygon(0);
			polygonSource_f1.SetGeneratePolyline(0);
			polygonSource_f1.Update();

			vtkPoints points = polygonSource_f1.GetOutput().GetPoints();


			// randomly shuffling the order of the sides we process can speed things up
			ArrayList<Integer> sides = new ArrayList<Integer>();
			for (int i=0; i<numberOfSides; ++i)
				sides.add(i);
			Collections.shuffle(sides);

			vtkPolyData nextInput = polyData;
			vtkClipPolyData clipPolyData = null;
			for (int i=0; i<sides.size(); ++i)	
			{
				int side = sides.get(i);

				// compute normal to plane formed by this side of polygon
				double[] currentPoint = points.GetPoint(side);

				double[] nextPoint = null;
				if (side < numberOfSides-1)
					nextPoint = points.GetPoint(side+1);
				else
					nextPoint = points.GetPoint(0);

				double[] vec = {nextPoint[0]-currentPoint[0],
						nextPoint[1]-currentPoint[1], 
						nextPoint[2]-currentPoint[2]};

				double[] planeNormal = new double[3];
				math.Cross(normal, vec, planeNormal);
				GeometryUtil.vhat(planeNormal, planeNormal);

				if (i > clipPlanes_f1.size()-1)
					clipPlanes_f1.add(new vtkPlane());
				vtkPlane plane = clipPlanes_f1.get(i);
				//			vtkPlane plane = new vtkPlane();
				plane.SetOrigin(currentPoint);
				plane.SetNormal(planeNormal);

				if (i > clipFilters_f1.size()-1)
					clipFilters_f1.add(new vtkClipPolyData());
				clipPolyData = clipFilters_f1.get(i);
				//			clipPolyData = new vtkClipPolyData();
				clipPolyData.SetInput(nextInput);
				clipPolyData.SetClipFunction(plane);
				clipPolyData.SetInsideOut(1);
				//clipPolyData.Update();

				nextInput = clipPolyData.GetOutput();

				if (i > clipOutputs_f1.size()-1)
					clipOutputs_f1.add(nextInput);
				clipOutputs_f1.set(i, nextInput);
			}


			//vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
			if (connectivityFilter_f1 == null)
				connectivityFilter_f1 = new vtkPolyDataConnectivityFilter();
			connectivityFilter_f1.SetInputConnection(clipPolyData.GetOutputPort());
			connectivityFilter_f1.SetExtractionModeToClosestPointRegion();
			connectivityFilter_f1.SetClosestPoint(center);
			connectivityFilter_f1.Update();

			//		polyData = new vtkPolyData();
			//if (outputPolyData_f1 == null)
			//	outputPolyData_f1 = new vtkPolyData();

			if (outputInterior != null)
			{
				//			polyData.DeepCopy(f1_connectivityFilter.GetOutput());
				outputInterior.DeepCopy(connectivityFilter_f1.GetOutput());
			}

			if (outputBoundary != null)
			{
				// Compute the bounding edges of this surface
				//vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
				if (edgeExtracter_f1 == null)
					edgeExtracter_f1 = new vtkFeatureEdges();
				edgeExtracter_f1.SetInput(connectivityFilter_f1.GetOutput());
				edgeExtracter_f1.BoundaryEdgesOn();
				edgeExtracter_f1.FeatureEdgesOff();
				edgeExtracter_f1.NonManifoldEdgesOff();
				edgeExtracter_f1.ManifoldEdgesOff();
				edgeExtracter_f1.Update();

				//polyData.DeepCopy(edgeExtracter.GetOutput());
				outputBoundary.DeepCopy(edgeExtracter_f1.GetOutput());
			}


			//vtkPolyDataWriter writer = new vtkPolyDataWriter();
			//writer.SetInput(polygonSource.GetOutput());
			//writer.SetFileName("/tmp/coneeros.vtk");
			//writer.SetFileTypeToBinary();
			//writer.Write();

			//return polyData;
			//return outputPolyData_f1;
		}
	}


	public static class DrawPathOnPolyData
	{
		private static vtkPlane cutPlane_f5;
		private static vtkCutter cutPolyData_f5;
		private static vtkPolyData polyLine_f5;
		private static vtkExtractPolyDataGeometry extract1_f5;
		private static vtkExtractPolyDataGeometry extract2_f5;
		private static vtkIdList idList_f5;
		private static vtkGenericCell genericCell1_f5;
		private static vtkGenericCell genericCell2_f5;
		private static vtksbCellLocator cellLocator_f5;
		public static vtkPolyData func(
				vtkPolyData polyData,
				vtkAbstractPointLocator pointLocator,
				double[] pt1,
				double[] pt2)
		{
			if (math == null)
				math = new vtkMath();

			double[] normal1 = GetPolyDataNormalAtPoint.func(pt1, polyData, pointLocator);
			double[] normal2 = GetPolyDataNormalAtPoint.func(pt2, polyData, pointLocator);

			double[] avgNormal = new double[3];
			avgNormal[0] = (normal1[0] + normal2[0])/2.0;
			avgNormal[1] = (normal1[1] + normal2[1])/2.0;
			avgNormal[2] = (normal1[2] + normal2[2])/2.0;

			double[] vec1 = {pt1[0]-pt2[0], pt1[1]-pt2[1], pt1[2]-pt2[2]};
			//double[] vec2 = {pt2[0]-pt1[0], pt2[1]-pt1[1], pt2[2]-pt1[2]};

			double[] normal = new double[3];
			math.Cross(vec1, avgNormal, normal);
			GeometryUtil.vhat(normal, normal);

			if (cutPlane_f5 == null)
				cutPlane_f5 = new vtkPlane();
			cutPlane_f5.SetOrigin(pt1);
			cutPlane_f5.SetNormal(normal);


			if (extract1_f5 == null)
				extract1_f5 = new vtkExtractPolyDataGeometry();
			extract1_f5.SetImplicitFunction(cutPlane_f5);
			extract1_f5.SetExtractInside(1);
			extract1_f5.SetExtractBoundaryCells(1);
			extract1_f5.SetInput(polyData);
			extract1_f5.Update();


			if (extract2_f5 == null)
				extract2_f5 = new vtkExtractPolyDataGeometry();
			extract2_f5.SetImplicitFunction(cutPlane_f5);
			extract2_f5.SetExtractInside(0);
			extract2_f5.SetExtractBoundaryCells(1);
			extract2_f5.SetInputConnection(extract1_f5.GetOutputPort());
			extract2_f5.Update();


			if (cutPolyData_f5 == null)
				cutPolyData_f5 = new vtkCutter();
			cutPolyData_f5.SetInputConnection(extract2_f5.GetOutputPort());
			cutPolyData_f5.CreateDefaultLocator();
			cutPolyData_f5.SetCutFunction(cutPlane_f5);
			cutPolyData_f5.Update();

			if (polyLine_f5 == null)
				polyLine_f5 = new vtkPolyData();
			polyLine_f5.DeepCopy(cutPolyData_f5.GetOutput());

			// Take this line and put it into a cell locator so we can find the cells
			// closest to the end points
			if (cellLocator_f5 == null)
				cellLocator_f5 = new vtksbCellLocator();
			cellLocator_f5.SetDataSet(polyLine_f5);
			cellLocator_f5.CacheCellBoundsOn();
			cellLocator_f5.AutomaticOn();
			cellLocator_f5.BuildLocator();

			// Search for the cells closest to the 2 endpoints
			double[] closestPoint1 = new double[3];
			double[] closestPoint2 = new double[3];
			if (genericCell1_f5 == null)
				genericCell1_f5 = new vtkGenericCell();
			if (genericCell2_f5 == null)
				genericCell2_f5 = new vtkGenericCell();
			int[] cellId1 = new int[1];
			int[] cellId2 = new int[1];
			int[] subId = new int[1];
			double[] dist2 = new double[1];

			cellLocator_f5.FindClosestPoint(pt1, closestPoint1, genericCell1_f5, cellId1, subId, dist2);
			cellLocator_f5.FindClosestPoint(pt2, closestPoint2, genericCell2_f5, cellId2, subId, dist2);


			// In the rare case where both points are on the same cell, simply 
			// return a single line connecting them
			if (cellId1[0] == cellId2[0])
			{
				vtkPoints points = polyLine_f5.GetPoints();
				points.SetNumberOfPoints(2);
				points.SetPoint(0, closestPoint1);
				points.SetPoint(1, closestPoint2);

				vtkCellArray lines = polyLine_f5.GetLines();
				lines.SetNumberOfCells(0);

				if (idList_f5 == null)
					idList_f5 = new vtkIdList();
				idList_f5.SetNumberOfIds(2);
				idList_f5.SetId(0, 0);
				idList_f5.SetId(1, 1);

				lines.InsertNextCell(idList_f5);

				return polyLine_f5;
			}


			boolean okay = ConvertPartOfLinesToPolyLineWithSplitting.func(polyLine_f5, closestPoint1, cellId1[0], closestPoint2, cellId2[0]);

			//System.out.println("number points: " + polyLine.GetNumberOfPoints());

			//vtkPolyDataWriter writer = new vtkPolyDataWriter();
			//writer.SetInput(polyLine);
			//writer.SetFileName("/tmp/cuteros.vtk");
			//writer.Write();

			if (okay)
				return polyLine_f5;
			else
				return null;
		}
	}


	public static class DrawConeOnPolyData
	{
		static private ArrayList<vtkClipPolyData> clipFilters_f7 = new ArrayList<vtkClipPolyData>();
		static private ArrayList<vtkPlane> clipPlanes_f7 = new ArrayList<vtkPlane>();
		static private ArrayList<vtkPolyData> clipOutputs_f7 = new ArrayList<vtkPolyData>(); // not sure is this one is really needed
		//static private vtkSphere sphere_f7;
		//static private vtkExtractPolyDataGeometry extract_f7;
		static private vtkRegularPolygonSource polygonSource_f7;
		static private vtkPolyDataConnectivityFilter connectivityFilter_f7;
		static private vtkFeatureEdges edgeExtracter_f7;
		public static void func(
				vtkPolyData polyData,
				vtkAbstractPointLocator pointLocator,
				double[] vertex,
				double[] axis, // must be unit vector
				double angle,
				int numberOfSides,
				vtkPolyData outputInterior,
				vtkPolyData outputBoundary)
		{
			/*
		double[] normal = GetPolyDataNormalAtPoint.func(center, polyData, pointLocator);


		// Reduce the size of the polydata we need to process by only
		// considering cells within twice radius of center.
		//vtkSphere sphere = new vtkSphere();
		if (sphere_f7 == null)
			sphere_f7 = new vtkSphere();
		sphere_f7.SetCenter(center);
		sphere_f7.SetRadius(radius >= 0.2 ? 1.2*radius : 1.2*0.2);

		//vtkExtractPolyDataGeometry extract = new vtkExtractPolyDataGeometry();
		if (extract_f7 == null)
			extract_f7 = new vtkExtractPolyDataGeometry();
		extract_f7.SetImplicitFunction(sphere_f7);
		extract_f7.SetExtractInside(1);
		extract_f7.SetExtractBoundaryCells(1);
		extract_f7.SetInput(polyData);
		extract_f7.Update();
		polyData = extract_f7.GetOutput();
			 */

			double radius = Math.tan(angle);
			double[] polygonCenter = {
					vertex[0] + axis[0],
					vertex[1] + axis[1],
					vertex[2] + axis[2]
			};

			//vtkRegularPolygonSource polygonSource = new vtkRegularPolygonSource();
			if (polygonSource_f7 == null)
				polygonSource_f7 = new vtkRegularPolygonSource();
			polygonSource_f7.SetCenter(polygonCenter);
			polygonSource_f7.SetRadius(radius);
			polygonSource_f7.SetNormal(axis);
			polygonSource_f7.SetNumberOfSides(numberOfSides);
			polygonSource_f7.SetGeneratePolygon(0);
			polygonSource_f7.SetGeneratePolyline(0);
			polygonSource_f7.Update();

			vtkPoints points = polygonSource_f7.GetOutput().GetPoints();


			// randomly shuffling the order of the sides we process can speed things up
			ArrayList<Integer> sides = new ArrayList<Integer>();
			for (int i=0; i<numberOfSides; ++i)
				sides.add(i);
			Collections.shuffle(sides);

			vtkPolyData nextInput = polyData;
			vtkClipPolyData clipPolyData = null;
			for (int i=0; i<sides.size(); ++i)	
			{
				int side = sides.get(i);

				// compute normal to plane formed by this side of polygon
				double[] currentPoint = points.GetPoint(side);

				double[] nextPoint = null;
				if (side < numberOfSides-1)
					nextPoint = points.GetPoint(side+1);
				else
					nextPoint = points.GetPoint(0);

				double[] vec = {nextPoint[0]-currentPoint[0],
						nextPoint[1]-currentPoint[1], 
						nextPoint[2]-currentPoint[2]};

				double[] vec2 = {
						currentPoint[0] - vertex[0],
						currentPoint[1] - vertex[1],
						currentPoint[2] - vertex[2]
				};
				double[] planeNormal = new double[3];
				GeometryUtil.vcrss(vec2, vec, planeNormal);
				GeometryUtil.vhat(planeNormal, planeNormal);

				if (i > clipPlanes_f7.size()-1)
					clipPlanes_f7.add(new vtkPlane());
				vtkPlane plane = clipPlanes_f7.get(i);
				//			vtkPlane plane = new vtkPlane();
				plane.SetOrigin(currentPoint);
				plane.SetNormal(planeNormal);

				if (i > clipFilters_f7.size()-1)
					clipFilters_f7.add(new vtkClipPolyData());
				clipPolyData = clipFilters_f7.get(i);
				//			clipPolyData = new vtkClipPolyData();
				clipPolyData.SetInput(nextInput);
				clipPolyData.SetClipFunction(plane);
				clipPolyData.SetInsideOut(1);
				//clipPolyData.Update();

				nextInput = clipPolyData.GetOutput();

				if (i > clipOutputs_f7.size()-1)
					clipOutputs_f7.add(nextInput);
				clipOutputs_f7.set(i, nextInput);
			}


			//vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
			if (connectivityFilter_f7 == null)
				connectivityFilter_f7 = new vtkPolyDataConnectivityFilter();
			connectivityFilter_f7.SetInputConnection(clipPolyData.GetOutputPort());
			connectivityFilter_f7.SetExtractionModeToClosestPointRegion();
			connectivityFilter_f7.SetClosestPoint(polygonCenter);
			connectivityFilter_f7.Update();

			//		polyData = new vtkPolyData();
			//if (outputPolyData_f7 == null)
			//	outputPolyData_f7 = new vtkPolyData();

			if (outputInterior != null)
			{
				//			polyData.DeepCopy(f1_connectivityFilter.GetOutput());
				outputInterior.DeepCopy(connectivityFilter_f7.GetOutput());
			}

			if (outputBoundary != null)
			{
				// Compute the bounding edges of this surface
				//vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
				if (edgeExtracter_f7 == null)
					edgeExtracter_f7 = new vtkFeatureEdges();
				edgeExtracter_f7.SetInput(connectivityFilter_f7.GetOutput());
				edgeExtracter_f7.BoundaryEdgesOn();
				edgeExtracter_f7.FeatureEdgesOff();
				edgeExtracter_f7.NonManifoldEdgesOff();
				edgeExtracter_f7.ManifoldEdgesOff();
				edgeExtracter_f7.Update();

				//polyData.DeepCopy(edgeExtracter.GetOutput());
				outputBoundary.DeepCopy(edgeExtracter_f7.GetOutput());
			}


			//vtkPolyDataWriter writer = new vtkPolyDataWriter();
			//writer.SetInput(polygonSource.GetOutput());
			//writer.SetFileName("/tmp/coneeros.vtk");
			//writer.SetFileTypeToBinary();
			//writer.Write();

			//return polyData;
			//return outputPolyData_f7;
		}
	}


	public static class ShiftPolyDataInNormalDirection
	{
		static private vtkPolyDataNormals normalsFilter_f2;
		public static void func(vtkPolyData polyData, double shiftAmount)
		{
			//vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
			if (normalsFilter_f2 == null)
				normalsFilter_f2 = new vtkPolyDataNormals();
			normalsFilter_f2.SetInput(polyData);
			normalsFilter_f2.SetComputeCellNormals(0);
			normalsFilter_f2.SetComputePointNormals(1);
			normalsFilter_f2.SplittingOff();
			normalsFilter_f2.Update();

			vtkDataArray pointNormals = normalsFilter_f2.GetOutput().GetPointData().GetNormals();
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
	}


	public static class ShiftPolyLineInNormalDirectionOfPolyData
	{
		public static void func(
				vtkPolyData polyLine,
				vtkPolyData polyData,
				double shiftAmount)
		{
			vtkDataArray pointNormals = polyData.GetPointData().GetNormals();
			vtkPoints points = polyLine.GetPoints();

			int numPoints = points.GetNumberOfPoints();

			for (int i=0; i<numPoints; ++i)
			{
				double[] point = points.GetPoint(i);
				int idx = polyData.FindPoint(point);

				if (idx < 0)
					continue;

				double[] normal = pointNormals.GetTuple3(idx);

				point[0] += normal[0]*shiftAmount;
				point[1] += normal[1]*shiftAmount;
				point[2] += normal[2]*shiftAmount;

				points.SetPoint(i, point);
			}

			polyLine.Modified();
		}
	}


	public static class ConvertPartOfLinesToPolyLineWithSplitting
	{
		private static vtkIdList idList_f6;
		private static vtkPoints points_f6;
		private static vtkCellArray new_lines_f6;
		/** 
		 * The boundary generated in getImageBorder is great, unfortunately the
		 * border consists of many lines of 2 vertices each. We, however, need a
		 * single polyline consisting of all the points. I was not able to find
		 * something in vtk that can convert this, so we will have to implement it
		 * here. Fortunately, the algorithm is pretty simple (assuming the list of
		 * lines has no intersections or other anomalies):
		 * Start with the first 2-vertex line segment. These 2 points will
		 * be the first 2 points of our new polyline we're creating. 
		 * Choose the second point. Now
		 * in addition to this line segment, there is only one other line segment 
		 * that contains the second point. Search for that line segment and let the
		 * other point in that line segment be the 3rd point of our polyline. Repeat
		 * this till we've formed the polyline.
		 * 
		 * @param polyline
		 * @param startPoint
		 * @return
		 */

		/**
		 * This function takes a polyline
		 * @param polyline
		 * @param pt1
		 * @param id1
		 * @param pt2
		 * @param id2
		 * @return
		 */
		public static boolean func(
				vtkPolyData polyline,
				double[] pt1,
				int id1,
				double[] pt2,
				int id2)
		{
			vtkCellArray lines_orig = polyline.GetLines();
			vtkPoints points_orig = polyline.GetPoints();

			vtkIdTypeArray idArray = lines_orig.GetData();
			int size = idArray.GetNumberOfTuples();
			//System.out.println(size);
			//System.out.println(idArray.GetNumberOfComponents());

			if (points_orig.GetNumberOfPoints() < 2)
				return true;

			if (size < 3)
			{
				System.out.println("Error: polydata corrupted");
				return false;
			}

			ArrayList<IdPair> lines = new ArrayList<IdPair>();
			for (int i=0; i<size; i+=3)
			{
				//System.out.println(idArray.GetValue(i));
				if (idArray.GetValue(i) != 2)
				{
					System.out.println("Big problem: polydata corrupted");
					return false;
				}
				lines.add(new IdPair(idArray.GetValue(i+1), idArray.GetValue(i+2)));
			}

			int newPointId1 = -1;
			int newPointId2 = -1;

			{
				newPointId1 = points_orig.InsertNextPoint(pt1);
				IdPair line1 = lines.get(id1);
				int tmp = line1.id2;
				line1.id2 = newPointId1;
				IdPair line = new IdPair(newPointId1, tmp);
				lines.add(line);
			}

			{
				newPointId2 = points_orig.InsertNextPoint(pt2);
				IdPair line2 = lines.get(id2);
				int tmp = line2.id2;
				line2.id2 = newPointId2;
				IdPair line = new IdPair(newPointId2, tmp);
				lines.add(line);
			}



			int startIdx = newPointId1;
			int numPoints = points_orig.GetNumberOfPoints();


			// Find which line segment contains the startIdx, and move this line segment first.
			// Also make sure startIdx is the first id of the pair
			for (int i=0; i<lines.size(); ++i)
			{
				IdPair line = lines.get(i);

				if (line.id1 == startIdx || line.id2 == startIdx)
				{
					if (line.id2 == startIdx)
					{
						// swap the pair
						line.id2 = line.id1;
						line.id1 = startIdx;
					}

					lines.remove(i);
					lines.add(0, line);
					break;
				}
			}


			// First do first direction ("left")
			IdPair line = lines.get(0);
			ArrayList<Integer> idListLeft = new ArrayList<Integer>();
			idListLeft.add(line.id1);
			idListLeft.add(line.id2);
			boolean leftDirectionSuccess = true;
			
			for (int i=2; i<numPoints; ++i)
			{
				int id = line.id2;

				if (newPointId2 == idListLeft.get(idListLeft.size()-1))
					break;
				// Find the other line segment that contains id
				for (int j=1; j<lines.size(); ++j)
				{
					IdPair nextLine = lines.get(j);
					if (id == nextLine.id1)
					{
						idListLeft.add(nextLine.id2);

						line = nextLine;
						break;
					}
					else if (id == nextLine.id2 && line.id1 != nextLine.id1)
					{
						idListLeft.add(nextLine.id1);

						// swap the ids
						int tmp = nextLine.id1;
						nextLine.id1 = nextLine.id2;
						nextLine.id2 = tmp;

						line = nextLine;
						break;
					}

					if (j==lines.size()-1)
					{
						/*
						System.out.println("Error: Could not find other line segment");
						System.out.println("i, j = " + i + " " + j);
						System.out.println("numPoints = " + numPoints);
						System.out.println("lines.size() = " + lines.size());
						System.out.println("startIdx = " + startIdx);
						for (int k=0; k<lines.size(); ++k)
						{
							System.out.println("line " + k + " - " + lines.get(k).id1 + " " + lines.get(k).id2);
						}
						*/

						leftDirectionSuccess = false;
						break;
					}
				}
				
				if (!leftDirectionSuccess)
					break;
			}

			// Then do second direction ("right")
			line = lines.get(0);
			ArrayList<Integer> idListRight = new ArrayList<Integer>();
			idListRight.add(line.id1);
			boolean rightDirectionSuccess = true;

			for (int i=1; i<numPoints; ++i)
			{
				int id = line.id1;

				if (newPointId2 == idListRight.get(idListRight.size()-1))
					break;
				// Find the other line segment that contains id
				for (int j=1; j<lines.size(); ++j)
				{
					IdPair nextLine = lines.get(j);
					if (id == nextLine.id2)
					{
						idListRight.add(nextLine.id1);

						line = nextLine;
						break;
					}
					else if (id == nextLine.id1 && line.id2 != nextLine.id2)
					{
						idListRight.add(nextLine.id2);

						// swap the ids
						int tmp = nextLine.id2;
						nextLine.id2 = nextLine.id1;
						nextLine.id1 = tmp;

						line = nextLine;
						break;
					}

					if (j==lines.size()-1)
					{
						/*
						System.out.println("Error: Could not find other line segment");
						System.out.println("i, j = " + i + " " + j);
						System.out.println("numPoints = " + numPoints);
						System.out.println("lines.size() = " + lines.size());
						System.out.println("startIdx = " + startIdx);
						for (int k=0; k<lines.size(); ++k)
						{
							System.out.println("line " + k + " - " + lines.get(k).id1 + " " + lines.get(k).id2);
						}
						*/

						rightDirectionSuccess = false;
						break;
					}
				}

				if (!rightDirectionSuccess)
					break;
			}

			//System.out.println("id left  " + idListLeft);
			//System.out.println("id right " + idListRight);
			//if (idListLeft.size() < idListRight.size())
			//	idList = idListLeft;
			ArrayList<Integer> idList = idListRight;
			if (leftDirectionSuccess && rightDirectionSuccess)
			{
				if (computePathLength(points_orig, idListLeft) < computePathLength(points_orig, idListRight))
					idList = idListLeft;
			}
			else if (leftDirectionSuccess && !rightDirectionSuccess)
			{
				idList = idListLeft;
			}
			else if (!leftDirectionSuccess && !rightDirectionSuccess)
			{
				System.out.println("Error: Could not find other line segment");
				return false;
			}

			// It would be nice if the points were in the order they are drawn rather
			// than some other arbitrary order. Therefore reorder the points so that
			// the id list will just be increasing numbers in order
			int numIds = idList.size();
			if (idList_f6 == null)
				idList_f6 = new vtkIdList();
			if (points_f6 == null)
				points_f6 = new vtkPoints();
			points_f6.SetNumberOfPoints(numIds);
			idList_f6.SetNumberOfIds(numIds);
			for (int i=0; i<numIds; ++i)
			{
				int id = idList.get(i);
				points_f6.SetPoint(i, points_orig.GetPoint(id));
				idList_f6.SetId(i, i);
			}

			//System.out.println("num points: " + numPoints);
			//System.out.println("num ids: " + idList_f6.GetNumberOfIds());
			//System.out.println(idList.size());

			polyline.SetPoints(null);
			polyline.SetPoints(points_f6);

			polyline.SetLines(null);
			if (new_lines_f6 == null)
				new_lines_f6 = new vtkCellArray();
			new_lines_f6.InsertNextCell(idList_f6);
			polyline.SetLines(new_lines_f6);

			return true;
		}

		private static double computePathLength(vtkPoints points, ArrayList<Integer> ids)
		{
			int size = ids.size();
			double length = 0.0;

			double[] pt1 = points.GetPoint(ids.get(0));
			for (int i=1;i<size;++i)
			{
				double[] pt2 = points.GetPoint(ids.get(i));
				double dist = GeometryUtil.distanceBetween(pt1, pt2);
				length += dist;
				pt1 = pt2;
			}

			return length;
		}
	}


	public static class GetPolyDataNormalAtPoint
	{
		static private vtkIdList idList_f3;
		public static double[] func(
				double[] pt,
				vtkPolyData polyData,
				vtkAbstractPointLocator pointLocator)
		{
			//vtkIdList idList = new vtkIdList();
			if (idList_f3 == null)
				idList_f3 = new vtkIdList();
			idList_f3.Reset();

			pointLocator.FindClosestNPoints(20, pt, idList_f3);

			// Average the normals
			double[] normal = {0.0, 0.0, 0.0};

			int N = idList_f3.GetNumberOfIds();
			if (N < 1)
				return null;

			vtkDataArray normals = polyData.GetPointData().GetNormals();
			for (int i=0; i<N; ++i)
			{
				double[] tmp = normals.GetTuple3(idList_f3.GetId(i));
				normal[0] += tmp[0];
				normal[1] += tmp[1];
				normal[2] += tmp[2];
			}

			normal[0] /= N;
			normal[1] /= N;
			normal[2] /= N;

			return normal;
		}
	}


	public static class GeneratePointScalarsFromCellScalars
	{
		/**
		 * This function takes 
		 * @param polydata
		 * @param celldata
		 * @return
		 */
		static public void func(vtkPolyData polydata)
		{
			polydata.BuildLinks(0);
			int numberOfPoints = polydata.GetNumberOfPoints();

			vtkIdList idList = new vtkIdList();

			vtkFloatArray pointScalars = new vtkFloatArray();
			pointScalars.SetNumberOfComponents(1);
			pointScalars.SetNumberOfTuples(numberOfPoints);

			vtkDataArray cellScalars = polydata.GetCellData().GetScalars();

			for (int i=0; i<numberOfPoints; ++i)
			{
				polydata.GetPointCells(i, idList);
				int numberOfCells = idList.GetNumberOfIds();
				double pointValue = 0.0;
				for (int j=0; j<numberOfCells; ++j)
				{
					pointValue += cellScalars.GetTuple1(idList.GetId(j));
				}

				pointValue /= (double)numberOfCells;
				pointScalars.SetTuple1(i, pointValue);
			}

			polydata.GetPointData().SetScalars(pointScalars);
		}
	}
}
