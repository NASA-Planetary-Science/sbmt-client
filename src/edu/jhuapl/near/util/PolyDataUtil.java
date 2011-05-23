package edu.jhuapl.near.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import vtk.vtkAbstractPointLocator;
import vtk.vtkCellArray;
import vtk.vtkCleanPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkCutter;
import vtk.vtkDataArray;
import vtk.vtkExtractPolyDataGeometry;
import vtk.vtkFeatureEdges;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkPlane;
import vtk.vtkPointData;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataConnectivityFilter;
import vtk.vtkPolyDataNormals;
import vtk.vtkRegularPolygonSource;
import vtk.vtkSphere;
import vtk.vtksbCellLocator;

/**
 * This class contains various utility functions for operating on a vtkPolyData.
 *
 * @author kahneg1
 *
 */
public class PolyDataUtil
{
    private static void printpt(double[] p, String s)
    {
        System.out.println(s + " " + p[0] + " " + p[1] + " " + p[2]);
    }

    public static vtkPolyData computeMultipleFrustumIntersection(
            vtkPolyData polyData,
            vtksbCellLocator locator,
            vtkPointLocator pointLocator,
            ArrayList<Frustum> frustums)
    {
        Frustum f = frustums.get(0);
        polyData = computeFrustumIntersection(polyData, locator, pointLocator, f.origin, f.ul, f.ur, f.ll, f.lr);

        for ( int i=1; i<frustums.size(); ++i)
        {
            if (polyData == null || polyData.GetNumberOfPoints() == 0 || polyData.GetNumberOfCells() == 0)
                return null;

            locator = new vtksbCellLocator();
            pointLocator = new vtkPointLocator();

            locator.SetDataSet(polyData);
            locator.CacheCellBoundsOn();
            locator.AutomaticOn();
            //locator.SetMaxLevel(10);
            //locator.SetNumberOfCellsPerNode(5);
            locator.BuildLocator();

            pointLocator.SetDataSet(polyData);
            pointLocator.BuildLocator();

            f = frustums.get(i);
            polyData = computeFrustumIntersection(polyData, locator, pointLocator, f.origin, f.ul, f.ur, f.ll, f.lr);
        }

        return polyData;
    }

    public static vtkPolyData computeFrustumIntersection(
            vtkPolyData polyData,
            vtksbCellLocator locator,
            vtkAbstractPointLocator pointLocator,
            double[] origin,
            double[] ul,
            double[] ur,
            double[] lr,
            double[] ll)
    {
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

        MathUtil.vcrss(ur, ul, top);
        MathUtil.vcrss(lr, ur, right);
        MathUtil.vcrss(ll, lr, bottom);
        MathUtil.vcrss(ul, ll, left);

        double dx = MathUtil.vnorm(origin);
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
        {
            System.out.println("clipped data is empty");
            return null;
        }

        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInputConnection(clipPolyData4.GetOutputPort());
        normalsFilter.SetComputeCellNormals(1);
        normalsFilter.SetComputePointNormals(0);
        normalsFilter.SplittingOff();
        normalsFilter.Update();

        vtkPolyData tmpPolyData = new vtkPolyData();
        tmpPolyData.DeepCopy(normalsFilter.GetOutput());

        // Now remove from this clipped poly data all the cells that are facing away from the viewer.
        vtkDataArray cellNormals = tmpPolyData.GetCellData().GetNormals();
        vtkPoints points = tmpPolyData.GetPoints();

        int numCells = cellNormals.GetNumberOfTuples();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(0);

        double[] viewDir = new double[3];
        for (int i=0; i<numCells; ++i)
        {
            double[] n = cellNormals.GetTuple3(i);
            MathUtil.vhat(n, n);

            // Compute the direction to the viewer from one of the point of the cell.
            tmpPolyData.GetCellPoints(i, idList);
            double[] pt = points.GetPoint(idList.GetId(0));

            viewDir[0] = origin[0] - pt[0];
            viewDir[1] = origin[1] - pt[1];
            viewDir[2] = origin[2] - pt[2];
            MathUtil.vhat(viewDir, viewDir);

            double dot = MathUtil.vdot(n, viewDir);
            if (dot <= 0.0)
                tmpPolyData.DeleteCell(i);
        }

        tmpPolyData.RemoveDeletedCells();
        tmpPolyData.GetCellData().SetNormals(null);

        vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
        cleanPoly.SetInput(tmpPolyData);
        cleanPoly.Update();

        //polyData = new vtkPolyData();
        tmpPolyData.DeepCopy(cleanPoly.GetOutput());

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
        //            if (connectivityFilter == null) connectivityFilter = new vtkPolyDataConnectivityFilter();
        //            connectivityFilter.SetInputConnection(cleanPoly.GetOutputPort());
        //            connectivityFilter.SetExtractionModeToAllRegions();
        //            connectivityFilter.Update();
        //            int numRegions = connectivityFilter.GetNumberOfExtractedRegions();
        //            System.out.println("numRegions: " + numRegions);
        //            if (numRegions == 1)
        //            {
        //                return tmpPolyData;
        //            }



        vtkGenericCell cell = new vtkGenericCell();

        points = tmpPolyData.GetPoints();
        int numPoints = points.GetNumberOfPoints();

        int[] numberOfObscuredPointsPerCell = new int[tmpPolyData.GetNumberOfCells()];
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

            int result = locator.IntersectWithLine(origin, sourcePnt, tol, t, x, pcoords, subId, cell_id, cell);

            if (result == 1)
            {
                int ptid = pointLocator.FindClosestPoint(sourcePnt);
                polyData.GetPointCells(ptid, idList);

                // The following check makes sure we don't delete any cells
                // if the intersection point happens to coincides with sourcePnt.
                // To do this we test to see of the intersected cell
                // is one of the cells which share a point with sourcePnt.
                // If it is we skip to the next point.
                if (idList.IsId(cell_id[0]) >= 0)
                {
                    //System.out.println("Too close  " + i);
                    continue;
                }

                tmpPolyData.GetPointCells(i, idList);
                int numPtCells = idList.GetNumberOfIds();
                for (int j=0; j<numPtCells; ++j)
                {
                    // The following makes sure that only cells for which ALL three of its
                    // points are obscured get deleted
                    int cellId = idList.GetId(j);
                    ++numberOfObscuredPointsPerCell[cellId];
                    if (numberOfObscuredPointsPerCell[cellId] == 3)
                        tmpPolyData.DeleteCell(cellId);
                }
            }
        }
        tmpPolyData.RemoveDeletedCells();

        //cleanPoly = new vtkCleanPolyData();
        cleanPoly.SetInput(tmpPolyData);
        cleanPoly.Update();

        //polyData = new vtkPolyData();
        tmpPolyData.DeepCopy(cleanPoly.GetOutput());

        return tmpPolyData;
    }

    /*
     * This is an older version of that uses a vtkCylinder to do
     * the intersection rather than a series of planes. Unfortunately, the results
     * look crappy. Use drawPolygonOnPolyData instead.
     */
    /*

    public static vtkPolyData drawCircleOnPolyData(
            vtkPolyData polyData,
            vtkAbstractPointLocator pointLocator,
            double[] center,
            double radius,
            boolean filled)
    {
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
        MathUtil.vcrss(normal, originalCylindarAxis, axisOfRotation);
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
     */

    public static void drawPolygonOnPolyData(
            vtkPolyData polyData,
            vtkAbstractPointLocator pointLocator,
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        double[] normal = getPolyDataNormalAtPoint(center, polyData, pointLocator);

        // If the number of points are too small, then vtkExtractPolyDataGeometry
        // as used here might fail, so skip this part (which is just an optimization
        // not really needed when the points are few) in this case.
        if (polyData.GetNumberOfPoints() >= 10000)
        {
            // Reduce the size of the polydata we need to process by only
            // considering cells within twice radius of center.
            vtkSphere sphere = new vtkSphere();
            sphere.SetCenter(center);
            sphere.SetRadius(radius >= 0.2 ? 1.2*radius : 1.2*0.2);

            vtkExtractPolyDataGeometry extract = new vtkExtractPolyDataGeometry();
            extract.SetImplicitFunction(sphere);
            extract.SetExtractInside(1);
            extract.SetExtractBoundaryCells(1);
            extract.SetInput(polyData);
            extract.Update();
            polyData = extract.GetOutput();
        }

        vtkRegularPolygonSource polygonSource = new vtkRegularPolygonSource();
        polygonSource.SetCenter(center);
        polygonSource.SetRadius(radius);
        polygonSource.SetNormal(normal);
        polygonSource.SetNumberOfSides(numberOfSides);
        polygonSource.SetGeneratePolygon(0);
        polygonSource.SetGeneratePolyline(0);
        polygonSource.Update();

        vtkPoints points = polygonSource.GetOutput().GetPoints();

        ArrayList<vtkPlane> clipPlanes = new ArrayList<vtkPlane>();
        ArrayList<vtkClipPolyData> clipFilters = new ArrayList<vtkClipPolyData>();
        ArrayList<vtkPolyData> clipOutputs = new ArrayList<vtkPolyData>();

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
            MathUtil.vcrss(normal, vec, planeNormal);
            MathUtil.vhat(planeNormal, planeNormal);

            if (i > clipPlanes.size()-1)
                clipPlanes.add(new vtkPlane());
            vtkPlane plane = clipPlanes.get(i);
            //            vtkPlane plane = new vtkPlane();
            plane.SetOrigin(currentPoint);
            plane.SetNormal(planeNormal);

            if (i > clipFilters.size()-1)
                clipFilters.add(new vtkClipPolyData());
            clipPolyData = clipFilters.get(i);
            //            clipPolyData = new vtkClipPolyData();
            clipPolyData.SetInput(nextInput);
            clipPolyData.SetClipFunction(plane);
            clipPolyData.SetInsideOut(1);
            //clipPolyData.Update();

            nextInput = clipPolyData.GetOutput();

            if (i > clipOutputs.size()-1)
                clipOutputs.add(nextInput);
            clipOutputs.set(i, nextInput);
        }


        vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
        connectivityFilter.SetInputConnection(clipPolyData.GetOutputPort());
        connectivityFilter.SetExtractionModeToClosestPointRegion();
        connectivityFilter.SetClosestPoint(center);
        connectivityFilter.Update();

        //        polyData = new vtkPolyData();
        //if (outputPolyData == null)
        //    outputPolyData = new vtkPolyData();

        if (outputInterior != null)
        {
            //            polyData.DeepCopy(connectivityFilter.GetOutput());
            outputInterior.DeepCopy(connectivityFilter.GetOutput());
        }

        if (outputBoundary != null)
        {
            // Compute the bounding edges of this surface
            vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
            edgeExtracter.SetInput(connectivityFilter.GetOutput());
            edgeExtracter.BoundaryEdgesOn();
            edgeExtracter.FeatureEdgesOff();
            edgeExtracter.NonManifoldEdgesOff();
            edgeExtracter.ManifoldEdgesOff();
            edgeExtracter.Update();

            //polyData.DeepCopy(edgeExtracter.GetOutput());
            outputBoundary.DeepCopy(edgeExtracter.GetOutput());
        }


        //vtkPolyDataWriter writer = new vtkPolyDataWriter();
        //writer.SetInput(polygonSource.GetOutput());
        //writer.SetFileName("/tmp/coneeros.vtk");
        //writer.SetFileTypeToBinary();
        //writer.Write();

        //return polyData;
        //return outputPolyData;
    }


    public static vtkPolyData drawPathOnPolyData(
            vtkPolyData polyData,
            vtkAbstractPointLocator pointLocator,
            double[] pt1,
            double[] pt2)
    {
        double[] normal1 = getPolyDataNormalAtPoint(pt1, polyData, pointLocator);
        double[] normal2 = getPolyDataNormalAtPoint(pt2, polyData, pointLocator);

        double[] avgNormal = new double[3];
        avgNormal[0] = (normal1[0] + normal2[0])/2.0;
        avgNormal[1] = (normal1[1] + normal2[1])/2.0;
        avgNormal[2] = (normal1[2] + normal2[2])/2.0;

        double[] vec1 = {pt1[0]-pt2[0], pt1[1]-pt2[1], pt1[2]-pt2[2]};
        //double[] vec2 = {pt2[0]-pt1[0], pt2[1]-pt1[1], pt2[2]-pt1[2]};

        double[] normal = new double[3];
        MathUtil.vcrss(vec1, avgNormal, normal);
        MathUtil.vhat(normal, normal);

        vtkPlane cutPlane = new vtkPlane();
        cutPlane.SetOrigin(pt1);
        cutPlane.SetNormal(normal);


        vtkExtractPolyDataGeometry extract1 = new vtkExtractPolyDataGeometry();
        extract1.SetImplicitFunction(cutPlane);
        extract1.SetExtractInside(1);
        extract1.SetExtractBoundaryCells(1);
        extract1.SetInput(polyData);
        extract1.Update();


        vtkExtractPolyDataGeometry extract2 = new vtkExtractPolyDataGeometry();
        extract2.SetImplicitFunction(cutPlane);
        extract2.SetExtractInside(0);
        extract2.SetExtractBoundaryCells(1);
        extract2.SetInputConnection(extract1.GetOutputPort());
        extract2.Update();


        vtkCutter cutPolyData = new vtkCutter();
        cutPolyData.SetInputConnection(extract2.GetOutputPort());
        cutPolyData.CreateDefaultLocator();
        cutPolyData.SetCutFunction(cutPlane);
        cutPolyData.Update();

        vtkPolyData polyLine = new vtkPolyData();
        polyLine.DeepCopy(cutPolyData.GetOutput());

        // Take this line and put it into a cell locator so we can find the cells
        // closest to the end points
        vtksbCellLocator cellLocator = new vtksbCellLocator();
        cellLocator.SetDataSet(polyLine);
        cellLocator.CacheCellBoundsOn();
        cellLocator.AutomaticOn();
        cellLocator.BuildLocator();

        // Search for the cells closest to the 2 endpoints
        double[] closestPoint1 = new double[3];
        double[] closestPoint2 = new double[3];
        vtkGenericCell genericCell1 = new vtkGenericCell();
        vtkGenericCell genericCell2 = new vtkGenericCell();
        int[] cellId1 = new int[1];
        int[] cellId2 = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        cellLocator.FindClosestPoint(pt1, closestPoint1, genericCell1, cellId1, subId, dist2);
        cellLocator.FindClosestPoint(pt2, closestPoint2, genericCell2, cellId2, subId, dist2);


        // In the rare case where both points are on the same cell, simply
        // return a single line connecting them
        if (cellId1[0] == cellId2[0])
        {
            vtkPoints points = polyLine.GetPoints();
            points.SetNumberOfPoints(2);
            points.SetPoint(0, closestPoint1);
            points.SetPoint(1, closestPoint2);

            vtkCellArray lines = polyLine.GetLines();
            lines.SetNumberOfCells(0);

            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(2);
            idList.SetId(0, 0);
            idList.SetId(1, 1);

            lines.InsertNextCell(idList);

            return polyLine;
        }


        boolean okay = convertPartOfLinesToPolyLineWithSplitting(polyLine, closestPoint1, cellId1[0], closestPoint2, cellId2[0]);

        //System.out.println("number points: " + polyLine.GetNumberOfPoints());

        //vtkPolyDataWriter writer = new vtkPolyDataWriter();
        //writer.SetInput(polyLine);
        //writer.SetFileName("/tmp/cuteros.vtk");
        //writer.Write();

        if (okay)
            return polyLine;
        else
            return null;
    }


    public static void drawConeOnPolyData(
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
        double[] normal = getPolyDataNormalAtPoint(center, polyData, pointLocator);


        // Reduce the size of the polydata we need to process by only
        // considering cells within twice radius of center.
        //vtkSphere sphere = new vtkSphere();
        if (sphere == null)
            sphere = new vtkSphere();
        sphere.SetCenter(center);
        sphere.SetRadius(radius >= 0.2 ? 1.2*radius : 1.2*0.2);

        //vtkExtractPolyDataGeometry extract = new vtkExtractPolyDataGeometry();
        if (extract == null)
            extract = new vtkExtractPolyDataGeometry();
        extract.SetImplicitFunction(sphere);
        extract.SetExtractInside(1);
        extract.SetExtractBoundaryCells(1);
        extract.SetInput(polyData);
        extract.Update();
        polyData = extract.GetOutput();
         */

        double radius = Math.tan(angle);
        double[] polygonCenter = {
                vertex[0] + axis[0],
                vertex[1] + axis[1],
                vertex[2] + axis[2]
        };

        vtkRegularPolygonSource polygonSource = new vtkRegularPolygonSource();
        polygonSource.SetCenter(polygonCenter);
        polygonSource.SetRadius(radius);
        polygonSource.SetNormal(axis);
        polygonSource.SetNumberOfSides(numberOfSides);
        polygonSource.SetGeneratePolygon(0);
        polygonSource.SetGeneratePolyline(0);
        polygonSource.Update();

        vtkPoints points = polygonSource.GetOutput().GetPoints();

        ArrayList<vtkClipPolyData> clipFilters = new ArrayList<vtkClipPolyData>();
        ArrayList<vtkPlane> clipPlanes = new ArrayList<vtkPlane>();
        ArrayList<vtkPolyData> clipOutputs = new ArrayList<vtkPolyData>(); // not sure is this one is really needed

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
            MathUtil.vcrss(vec2, vec, planeNormal);
            MathUtil.vhat(planeNormal, planeNormal);

            if (i > clipPlanes.size()-1)
                clipPlanes.add(new vtkPlane());
            vtkPlane plane = clipPlanes.get(i);
            //            vtkPlane plane = new vtkPlane();
            plane.SetOrigin(currentPoint);
            plane.SetNormal(planeNormal);

            if (i > clipFilters.size()-1)
                clipFilters.add(new vtkClipPolyData());
            clipPolyData = clipFilters.get(i);
            //            clipPolyData = new vtkClipPolyData();
            clipPolyData.SetInput(nextInput);
            clipPolyData.SetClipFunction(plane);
            clipPolyData.SetInsideOut(1);
            //clipPolyData.Update();

            nextInput = clipPolyData.GetOutput();

            if (i > clipOutputs.size()-1)
                clipOutputs.add(nextInput);
            clipOutputs.set(i, nextInput);
        }


        vtkPolyDataConnectivityFilter connectivityFilter = new vtkPolyDataConnectivityFilter();
        connectivityFilter.SetInputConnection(clipPolyData.GetOutputPort());
        connectivityFilter.SetExtractionModeToClosestPointRegion();
        connectivityFilter.SetClosestPoint(polygonCenter);
        connectivityFilter.Update();

        //        polyData = new vtkPolyData();
        //if (outputPolyData == null)
        //    outputPolyData = new vtkPolyData();

        if (outputInterior != null)
        {
            //            polyData.DeepCopy(connectivityFilter.GetOutput());
            outputInterior.DeepCopy(connectivityFilter.GetOutput());
        }

        if (outputBoundary != null)
        {
            // Compute the bounding edges of this surface
            vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
            edgeExtracter.SetInput(connectivityFilter.GetOutput());
            edgeExtracter.BoundaryEdgesOn();
            edgeExtracter.FeatureEdgesOff();
            edgeExtracter.NonManifoldEdgesOff();
            edgeExtracter.ManifoldEdgesOff();
            edgeExtracter.Update();

            //polyData.DeepCopy(edgeExtracter.GetOutput());
            outputBoundary.DeepCopy(edgeExtracter.GetOutput());
        }


        //vtkPolyDataWriter writer = new vtkPolyDataWriter();
        //writer.SetInput(polygonSource.GetOutput());
        //writer.SetFileName("/tmp/coneeros.vtk");
        //writer.SetFileTypeToBinary();
        //writer.Write();

        //return polyData;
        //return outputPolyData;
    }


    public static void shiftPolyDataInNormalDirection(vtkPolyData polyData, double shiftAmount)
    {
        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInput(polyData);
        normalsFilter.SetComputeCellNormals(0);
        normalsFilter.SetComputePointNormals(1);
        normalsFilter.SplittingOff();
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


    public static void shiftPolyLineInNormalDirectionOfPolyData(
            vtkPolyData polyLine,
            vtkPolyData polyData,
            vtkAbstractPointLocator pointLocator,
            double shiftAmount)
    {
        vtkPointData pointData = polyData.GetPointData();
        vtkDataArray pointNormals = pointData.GetNormals();
        vtkPoints points = polyLine.GetPoints();

        int numPoints = points.GetNumberOfPoints();

        for (int i=0; i<numPoints; ++i)
        {
            double[] point = points.GetPoint(i);
            int idx = pointLocator.FindClosestPoint(point);

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
    public static boolean convertPartOfLinesToPolyLineWithSplitting(
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
        //    idList = idListLeft;
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
        vtkIdList idList2 = new vtkIdList();
        vtkPoints points = new vtkPoints();
        points.SetNumberOfPoints(numIds);
        idList2.SetNumberOfIds(numIds);
        for (int i=0; i<numIds; ++i)
        {
            int id = idList.get(i);
            points.SetPoint(i, points_orig.GetPoint(id));
            idList2.SetId(i, i);
        }

        //System.out.println("num points: " + numPoints);
        //System.out.println("num ids: " + idList.GetNumberOfIds());
        //System.out.println(idList.size());

        polyline.SetPoints(null);
        polyline.SetPoints(points);

        polyline.SetLines(null);
        vtkCellArray new_lines = new vtkCellArray();
        new_lines.InsertNextCell(idList2);
        polyline.SetLines(new_lines);

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
            double dist = MathUtil.distanceBetween(pt1, pt2);
            length += dist;
            pt1 = pt2;
        }

        return length;
    }


    private static double[] getPolyDataNormalAtPoint(
            double[] pt,
            vtkPolyData polyData,
            vtkAbstractPointLocator pointLocator)
    {
        vtkIdList idList = new vtkIdList();

        pointLocator.FindClosestNPoints(20, pt, idList);

        // Average the normals
        double[] normal = {0.0, 0.0, 0.0};

        int N = idList.GetNumberOfIds();
        if (N < 1)
            return null;

        vtkDataArray normals = polyData.GetPointData().GetNormals();
        for (int i=0; i<N; ++i)
        {
            double[] tmp = normals.GetTuple3(idList.GetId(i));
            normal[0] += tmp[0];
            normal[1] += tmp[1];
            normal[2] += tmp[2];
        }

        normal[0] /= N;
        normal[1] /= N;
        normal[2] /= N;

        return normal;
    }


    /**
     * Get the area of a given cell. Assumes cells are triangles.
     * @author eli
     *
     */
    /*
        // The idList parameter is needed only to avoid repeated memory
        // allocation when this function is called within a loop.
        static public double getCellArea(vtkPolyData polydata, int cellId, vtkIdList idList)
        {
            polydata.GetCellPoints(cellId, idList);

            int numberOfCells = idList.GetNumberOfIds();
            if (numberOfCells != 3)
            {
                System.err.println("Error: Cells must have exactly 3 vertices!");
                return 0.0;
            }

            double[] pt0 = polydata.GetPoint(idList.GetId(0));
            double[] pt1 = polydata.GetPoint(idList.GetId(1));
            double[] pt2 = polydata.GetPoint(idList.GetId(2));

            return MathUtil.triangleArea(pt0, pt1, pt2);
        }
     */


    /**
     *
     * @param polydata
     * @param pointdata
     * @param cellId
     * @param pt
     * @param idList this parameter is needed only to avoid repeated memory
     *           allocation when this function is called within a loop.
     * @return
     */
    static public double interpolateWithinCell(
            vtkPolyData polydata,
            vtkDataArray pointdata,
            int cellId,
            double[] pt,
            vtkIdList idList)
    {
        polydata.GetCellPoints(cellId, idList);

        int numberOfCells = idList.GetNumberOfIds();
        if (numberOfCells != 3)
        {
            System.err.println("Error: Cells must have exactly 3 vertices!");
            return 0.0;
        }

        double[] p1 = polydata.GetPoint(idList.GetId(0));
        double[] p2 = polydata.GetPoint(idList.GetId(1));
        double[] p3 = polydata.GetPoint(idList.GetId(2));
        double v1 = pointdata.GetTuple1(idList.GetId(0));
        double v2 = pointdata.GetTuple1(idList.GetId(1));
        double v3 = pointdata.GetTuple1(idList.GetId(2));

        return MathUtil.interpolateWithinTriangle(pt, p1, p2, p3, v1, v2, v3);
    }


    /**
     * This function takes cell data and computes point data from it
     * by computing an average over all cells that share that point.
     * Cells that are large carry more weight than those that are smaller.
     * @param polydata
     * @param cellScalars
     * @param pointScalars
     */
    static public void generatePointScalarsFromCellScalars(vtkPolyData polydata,
            vtkFloatArray cellScalars,
            vtkFloatArray pointScalars)
    {
        polydata.BuildLinks(0);
        int numberOfPoints = polydata.GetNumberOfPoints();

        vtkIdList idList = new vtkIdList();

        pointScalars.SetNumberOfComponents(1);
        pointScalars.SetNumberOfTuples(numberOfPoints);

        for (int i=0; i<numberOfPoints; ++i)
        {
            polydata.GetPointCells(i, idList);
            int numberOfCells = idList.GetNumberOfIds();

            /*
                 // After writing the following, wasn't sure if it was mathematically correct.
                double totalArea = 0.0;
                double[] areas = new double[numberOfCells];

                for (int j=0; j<numberOfCells; ++j)
                {
                    areas[j] = getCellArea(polydata, idList.GetId(j));
                    totalArea += areas[j];
                }

                double pointValue = 0.0;
                if (totalArea > 0.0)
                {
                    for (int j=0; j<numberOfCells; ++j)
                        pointValue += (areas[j]/totalArea) * cellScalars.GetTuple1(idList.GetId(j));
                }
                else
                {
                    for (int j=0; j<numberOfCells; ++j)
                        pointValue += cellScalars.GetTuple1(idList.GetId(j));

                    pointValue /= (double)numberOfCells;
                }
             */

            double pointValue = 0.0;

            for (int j=0; j<numberOfCells; ++j)
                pointValue += cellScalars.GetTuple1(idList.GetId(j));

            pointValue /= (double)numberOfCells;

            pointScalars.SetTuple1(i, pointValue);
        }
    }

    /**
     * Given a frustum and a polydata footprint, generate texture coordinates for all points in
     * the polydata assuming an image acquired with that frustum is texture mapped to it.
     *
     * @param frustum
     * @param polyData
     */
    static public void generateTextureCoordinates(Frustum frustum, vtkPolyData footprint)
    {
        int numberOfPoints = footprint.GetNumberOfPoints();

        double[] spacecraftPosition = frustum.origin;
        double[] frustum1 = frustum.ul;
        double[] frustum3 = frustum.ur;
        double[] frustum2 = frustum.lr;

        vtkPointData pointData = footprint.GetPointData();
        vtkDataArray textureCoordinates = pointData.GetTCoords();
        vtkFloatArray textureCoords = null;

        if (textureCoordinates != null && textureCoordinates instanceof vtkFloatArray)
        {
            textureCoords = (vtkFloatArray)textureCoordinates;
        }
        else
        {
            textureCoords = new vtkFloatArray();
            pointData.SetTCoords(textureCoords);
        }

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = footprint.GetPoints();

        double a = MathUtil.vsep(frustum1, frustum3);
        double b = MathUtil.vsep(frustum1, frustum2);

        double[] vec = new double[3];

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            vec[0] = pt[0] - spacecraftPosition[0];
            vec[1] = pt[1] - spacecraftPosition[1];
            vec[2] = pt[2] - spacecraftPosition[2];
            MathUtil.vhat(vec, vec);

            double d1 = MathUtil.vsep(vec, frustum1);
            double d2 = MathUtil.vsep(vec, frustum2);

            double v = (d1*d1 + b*b - d2*d2) / (2.0*b);
            double u = d1*d1 - v*v;
            if (u <= 0.0)
                u = 0.0;
            else
                u = Math.sqrt(u);

            //System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);

            v = v/b;
            u = u/a;

            if (v < 0.0) v = 0.0;
            if (v > 1.0) v = 1.0;
            if (u < 0.0) u = 0.0;
            if (u > 1.0) u = 1.0;

            textureCoords.SetTuple2(i, v, u);
        }
    }
}
