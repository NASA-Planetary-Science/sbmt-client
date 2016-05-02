package edu.jhuapl.near.model;

import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.vtkCellArray;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPointDataToCellData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Point3D;
import edu.jhuapl.near.util.PolyDataUtil;

public class DEMModel extends SmallBodyModel
{
    private static final float INVALID_VALUE = -1.0e38f;
    private vtkIdList idList;
    private vtkPolyData dem;
    private vtkPolyData boundary;
    private vtkFloatArray heightsGravityPerPoint;
    private vtkFloatArray heightsPlanePerPoint;
    private vtkFloatArray slopesPerPoint;
    private vtkFloatArray heightsGravity; // per cell
    private vtkFloatArray heightsPlane; // per cell
    private vtkFloatArray slopes; // per cell
    private double[] centerOfDEM = null;
    private double[] normalOfDEM = null;
    private vtksbCellLocator boundaryLocator;
    private vtkGenericCell genericCell;

    public DEMModel(String filename) throws IOException, FitsException
    {
        idList = new vtkIdList();
        dem = new vtkPolyData();
        boundary = new vtkPolyData();
        heightsGravityPerPoint = new vtkFloatArray();
        heightsPlanePerPoint = new vtkFloatArray();
        slopesPerPoint = new vtkFloatArray();
        heightsGravity = new vtkFloatArray();
        heightsPlane = new vtkFloatArray();
        slopes = new vtkFloatArray();

        initializeDEM(filename);

        vtkFloatArray[] coloringValues =
        {
                heightsGravity, heightsPlane, slopes
        };

        String[] coloringNames = {
                "Geopotential Height",
                "Height Relative to Normal Plane",
                "Slope"
        };
        String[] coloringUnits = {
                "m", "m", "deg"
        };

        setSmallBodyPolyData(dem, coloringValues, coloringNames, coloringUnits, ColoringValueType.CELLDATA);
    }

    private vtkPolyData initializeDEM(String filename) throws IOException, FitsException
    {
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();
        dem.SetPoints(points);
        dem.SetPolys(polys);

        heightsGravityPerPoint.SetNumberOfComponents(1);
        heightsPlanePerPoint.SetNumberOfComponents(1);
        slopesPerPoint.SetNumberOfComponents(1);
        heightsGravity.SetNumberOfComponents(1);
        heightsPlane.SetNumberOfComponents(1);
        slopes.SetNumberOfComponents(1);


        Fits f = new Fits(filename);
        BasicHDU hdu = f.getHDU(0);

        int[] axes = hdu.getAxes();

        final int NUM_PLANES = 6;
        if (axes.length != 3 || axes[0] != NUM_PLANES || axes[1] != axes[2])
        {
            throw new IOException("FITS file has incorrect dimensions");
        }

        int liveSize = axes[1];

        float[][][] data = (float[][][])hdu.getData().getData();
        f.getStream().close();

        int[][] indices = new int[liveSize][liveSize];
        int c = 0;
        float x, y, z, h, h2, s;
        int i0, i1, i2, i3;

        // First add points to the vtkPoints array
        for (int m=0; m<liveSize; ++m)
            for (int n=0; n<liveSize; ++n)
            {
                indices[m][n] = -1;

                // A pixel value of -1.0e38 means that pixel is invalid and should be skipped
                x = data[3][m][n];
                y = data[4][m][n];
                z = data[5][m][n];
                h = data[0][m][n];
                h2 = data[1][m][n];
                s = data[2][m][n];

                boolean valid = (x != INVALID_VALUE && y != INVALID_VALUE && z != INVALID_VALUE
                        && h != INVALID_VALUE && h2 != INVALID_VALUE && s != INVALID_VALUE);

                if (valid)
                {
                    h = 1000.0f * h;
                    h2 = 1000.0f * h2;
                    s = (float)(180.0/Math.PI) * s;

                    points.InsertNextPoint(x, y, z);
                    heightsGravity.InsertNextTuple1(h);
                    heightsPlane.InsertNextTuple1(h2);
                    slopes.InsertNextTuple1(s);

                    indices[m][n] = c;

                    ++c;
                }
            }

        idList.SetNumberOfIds(3);

        // Now add connectivity information
        for (int m=1; m<liveSize; ++m)
            for (int n=1; n<liveSize; ++n)
            {
                // Get the indices of the 4 corners of the rectangle to the upper left
                i0 = indices[m-1][n-1];
                i1 = indices[m][n-1];
                i2 = indices[m-1][n];
                i3 = indices[m][n];

                // Add upper left triangle
                if (i0>=0 && i1>=0 && i2>=0)
                {
                    idList.SetId(0, i0);
                    idList.SetId(1, i2);
                    idList.SetId(2, i1);
                    polys.InsertNextCell(idList);
                }
                // Add bottom right triangle
                if (i2>=0 && i1>=0 && i3>=0)
                {
                    idList.SetId(0, i2);
                    idList.SetId(1, i3);
                    idList.SetId(2, i1);
                    polys.InsertNextCell(idList);
                }
            }

        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInputData(dem);
        normalsFilter.SetComputeCellNormals(0);
        normalsFilter.SetComputePointNormals(1);
        normalsFilter.SplittingOff();
        normalsFilter.FlipNormalsOn();
        normalsFilter.Update();

        vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
        dem.DeepCopy(normalsFilterOutput);

        PolyDataUtil.getBoundary(dem, boundary);
        // Remove scalar data since it interferes with setting the boundary color
        boundary.GetCellData().SetScalars(null);

        // Make a copy of  per point data structures since we need that later for
        // drawing profile plots.
        heightsGravityPerPoint.DeepCopy(heightsGravity);
        heightsPlanePerPoint.DeepCopy(heightsPlane);
        slopesPerPoint.DeepCopy(slopes);
        convertPointDataToCellData();

        int centerIndex = liveSize / 2;
        centerOfDEM = new double[3];
        centerOfDEM[0] = data[3][centerIndex][centerIndex];
        centerOfDEM[1] = data[4][centerIndex][centerIndex];
        centerOfDEM[2] = data[5][centerIndex][centerIndex];

        return dem;
    }

    /**
     * Convert the point data (which is how they are stored in the Gaskell's cube file)
     * to cell data.
     */
    private void convertPointDataToCellData()
    {
        vtkFloatArray[] dataArrays = {slopes, heightsGravity, heightsPlane};
        vtkFloatArray[] cellDataArrays = {null, null, null};

        vtkPointDataToCellData pointToCell = new vtkPointDataToCellData();
        pointToCell.SetInputData(dem);

        for (int i=0; i<dataArrays.length; ++i)
        {
            vtkFloatArray array = dataArrays[i];
            dem.GetPointData().SetScalars(array);
            pointToCell.Update();
            vtkFloatArray arrayCell = new vtkFloatArray();
            vtkDataArray outputScalars = ((vtkPolyData)pointToCell.GetOutput()).GetCellData().GetScalars();
            arrayCell.DeepCopy(outputScalars);
            cellDataArrays[i] = arrayCell;
        }

        dem.GetPointData().SetScalars(null);

        slopes = cellDataArrays[0];
        heightsGravity = cellDataArrays[1];
        heightsPlane = cellDataArrays[2];

        pointToCell.Delete();
    }

    public vtkPolyData getBoundary()
    {
        return boundary;
    }

    public void generateProfile(ArrayList<Point3D> xyzPointList,
            ArrayList<Double> profileValues, ArrayList<Double> profileDistances, int coloringIndex)
    {
        profileValues.clear();
        profileDistances.clear();

        // For each point in xyzPointList, find the cell containing that
        // point and then, using barycentric coordinates find the value
        // of the dem at that point
        //
        // To compute the distance, assume we have a straight line connecting the first
        // and last points of xyzPointList. For each point, p, in xyzPointList, find the point
        // on the line closest to p. The distance from p to the start of the line is what
        // is placed in heights. Use SPICE's nplnpt function for this.
        //
        // coloringIndex = 0 : heightsGravityPerPoint
        // coloringIndex = 1 : heightsPlanePerPoint
        // coloringIndex = 2 : slopesPerPoint

        double[] first = xyzPointList.get(0).xyz;
        double[] last = xyzPointList.get(xyzPointList.size()-1).xyz;
        double[] lindir = new double[3];
        lindir[0] = last[0] - first[0];
        lindir[1] = last[1] - first[1];
        lindir[2] = last[2] - first[2];

        // The following can be true if the user clicks on the same point twice
        boolean zeroLineDir = MathUtil.vzero(lindir);

        double[] pnear = new double[3];
        double[] notused = new double[1];
        vtkIdList idList = new vtkIdList();

        // Figure out which data set to sample
        vtkFloatArray valuePerPoint = null;
        switch(coloringIndex){
        case 0:
            valuePerPoint = heightsGravityPerPoint;
            break;
        case 1:
            valuePerPoint = heightsPlanePerPoint;
            break;
        case 2:
            valuePerPoint = slopesPerPoint;
            break;
        }

        // Sample
        if(valuePerPoint != null)
        {
            for (Point3D p : xyzPointList)
            {
                int cellId = findClosestCell(p.xyz);

                double val = PolyDataUtil.interpolateWithinCell(dem, valuePerPoint, cellId, p.xyz, idList);

                profileValues.add(val);

                if (zeroLineDir)
                {
                    profileDistances.add(0.0);
                }
                else
                {
                    MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
                    double dist = 1000.0f * MathUtil.distanceBetween(first, pnear);
                    profileDistances.add(dist);
                }
            }
        }
    }

    private double getDistanceToBoundary(double[] point)
    {
        if (boundaryLocator == null)
        {
            boundaryLocator = new vtksbCellLocator();
            boundaryLocator.FreeSearchStructure();
            boundaryLocator.SetDataSet(boundary);
            boundaryLocator.CacheCellBoundsOn();
            boundaryLocator.AutomaticOn();
            //boundaryLocator.SetMaxLevel(10);
            //boundaryLocator.SetNumberOfCellsPerNode(5);
            boundaryLocator.BuildLocator();

            genericCell = new vtkGenericCell();
        }

        double[] closestPoint = new double[3];
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        boundaryLocator.FindClosestPoint(point, closestPoint, genericCell, cellId, subId, dist2);

        return MathUtil.distanceBetween(point, closestPoint);
    }

    /**
     * Return whether or not point is inside the DEM. By "inside the DEM" we
     * mean that the point is displaced parallel to the mean normal vector of the
     * DEM, it will intersect the DEM.
     *
     * @param point
     * @param minDistanceToBoundary only consider point inside if it is minDistanceToBoundary
     *        or greater away from the boundary
     * @return
     */
    public boolean isPointWithinDEM(double[] point, double minDistanceToBoundary)
    {
        // Take the point and using the normal vector, form a line parallel
        // to the normal vector which passes through the given point.
        // If this line intersects the DEM, return true. Otherwise return
        // false.
        double[] normal = getNormal();

        double size = getBoundingBoxDiagonalLength();
        double[] origin = {
                point[0] + size*normal[0],
                point[1] + size*normal[1],
                point[2] + size*normal[2]
        };

        double[] direction = {
                -normal[0],
                -normal[1],
                -normal[2]
        };

        double[] intersectPoint = new double[3];
        int cellId = computeRayIntersection(origin, direction, intersectPoint );

        if (cellId >= 0 && getDistanceToBoundary(intersectPoint) >= minDistanceToBoundary)
            return true;
        else
            return false;
    }

    /**
     * Return the center point of the DEM.
     */
    public double[] getCenter()
    {
        return centerOfDEM;
    }

    /**
     * return the mean normal vector to the DEM. This is computed by averaging
     * the normal vectors of all plates in the DEM.
     */
    public double[] getNormal()
    {
        if (normalOfDEM == null)
            normalOfDEM = PolyDataUtil.computePolyDataNormal(dem);

        return centerOfDEM;
    }

    public void delete()
    {
        idList.Delete();
        dem.Delete();
        boundary.Delete();
        heightsGravityPerPoint.Delete();
        heightsPlanePerPoint.Delete();
        slopesPerPoint.Delete();
        heightsGravity.Delete();
        heightsPlane.Delete();
        slopes.Delete();
        super.delete();
    }
}
