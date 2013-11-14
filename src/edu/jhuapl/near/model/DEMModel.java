package edu.jhuapl.near.model;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import vtk.vtkCellArray;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkPointDataToCellData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Point3D;
import edu.jhuapl.near.util.PolyDataUtil;

public class DEMModel extends SmallBodyModel
{
    private static final int MAX_WIDTH = 1027;
    private static final int MAX_HEIGHT = 1027;
    private vtkIdList idList;
    private vtkPolyData dem;
    private vtkPolyData boundary;
    private vtkFloatArray heightsGravityPerPoint;
    private vtkFloatArray heightsGravity; // per cell
    private vtkFloatArray heightsPlane; // per cell
    private vtkFloatArray slopes; // per cell
    private int liveSize;
    //private int height;
    private int startPixel;
    private double[] centerOfDEM = null;
    private double[] normalOfDEM = null;

    public DEMModel(String filename, String lblfilename) throws IOException
    {
        idList = new vtkIdList();
        dem = new vtkPolyData();
        boundary = new vtkPolyData();
        heightsGravityPerPoint = new vtkFloatArray();
        heightsGravity = new vtkFloatArray();
        heightsPlane = new vtkFloatArray();
        slopes = new vtkFloatArray();

        initializeDEM(filename, lblfilename);

        vtkFloatArray[] coloringValues =
        {
                heightsGravity, heightsPlane, slopes
        };

        String[] coloringNames = {
                "Elevation Relative to Gravity",
                "Elevation Relative to Plane",
                "Slope"
        };
        String[] coloringUnits = {
                "m", "m", "deg"
        };

        setSmallBodyPolyData(dem, coloringValues, coloringNames, coloringUnits, ColoringValueType.CELLDATA);
        setColoringIndex(0);
    }

    private vtkPolyData initializeDEM(String filename, String lblfilename) throws IOException
    {
        loadLblFile(lblfilename);

        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();
        //vtkFloatArray heights = new vtkFloatArray();
        dem.SetPoints(points);
        dem.SetPolys(polys);
        //dem.GetPointData().SetScalars(heights);

        vtkPoints boundaryPoints = new vtkPoints();
        vtkCellArray boundaryPolys = new vtkCellArray();
        boundary.SetPoints(boundaryPoints);
        boundary.SetVerts(boundaryPolys);

        heightsGravityPerPoint.SetNumberOfComponents(1);
        heightsGravity.SetNumberOfComponents(1);
        heightsPlane.SetNumberOfComponents(1);
        slopes.SetNumberOfComponents(1);

        FileInputStream fs = new FileInputStream(filename);
        BufferedInputStream bs = new BufferedInputStream(fs);
        DataInputStream in = new DataInputStream(bs);

        float[] data = new float[MAX_WIDTH*MAX_HEIGHT*6];

        for (int i=0;i<data.length; ++i)
        {
            data[i] = MathUtil.swap(in.readFloat());
        }

        in.close();

        idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        int[][] indices = new int[MAX_WIDTH][MAX_HEIGHT];
        int c = 0;
        int d = 0;
        float x, y, z, h, h2, s;
        int i0, i1, i2, i3;

        int endPixel = startPixel + liveSize - 1;

        // First add points to the vtkPoints array
//        for (int m=0; m<WIDTH-500; ++m)
//            for (int n=0; n<HEIGHT-500; ++n)
        for (int m=0; m<MAX_WIDTH; ++m)
            for (int n=0; n<MAX_HEIGHT; ++n)
//        for (int m=startPixel; m<=endPixel; ++m)
//            for (int n=startPixel; n<=endPixel; ++n)
            {
                if (m >= startPixel && m <= endPixel && n >= startPixel && n <= endPixel)
                {
                    x = data[index(m,n,3)];
                    y = data[index(m,n,4)];
                    z = data[index(m,n,5)];
                    h = 1000.0f * data[index(m,n,0)];
                    h2 = 1000.0f * data[index(m,n,1)];
                    s = (float)(180.0/Math.PI) * data[index(m,n,2)];

                    if (m == startPixel || m == endPixel || n == startPixel || n == endPixel)
                    {
                        boundaryPoints.InsertNextPoint(x, y, z);
                        idList.SetId(0, d);
                        boundaryPolys.InsertNextCell(idList);
                        ++d;
                    }

                    //points.SetPoint(c, x, y, z);
                    points.InsertNextPoint(x, y, z);
                    heightsGravity.InsertNextTuple1(h);
                    heightsPlane.InsertNextTuple1(h2);
                    slopes.InsertNextTuple1(s);

                    indices[m][n] = c;

                    ++c;
                }
                else
                {
                    indices[m][n] = -1;
                }
            }

        idList.SetNumberOfIds(3);

        // Now add connectivity information
//        for (int m=1; m<WIDTH-500; ++m)
//            for (int n=1; n<HEIGHT-500; ++n)
//        for (int m=1; m<MAX_WIDTH; ++m)
//            for (int n=1; n<MAX_HEIGHT; ++n)
        for (int m=startPixel+1; m<=endPixel; ++m)
            for (int n=startPixel+1; n<=endPixel; ++n)
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
                    idList.SetId(1, i1);
                    idList.SetId(2, i2);
                    polys.InsertNextCell(idList);
                }
                // Add bottom right triangle
                if (i2>=0 && i1>=0 && i3>=0)
                {
                    idList.SetId(0, i2);
                    idList.SetId(1, i1);
                    idList.SetId(2, i3);
                    polys.InsertNextCell(idList);
                }
            }

        vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
        normalsFilter.SetInput(dem);
        normalsFilter.SetComputeCellNormals(0);
        normalsFilter.SetComputePointNormals(1);
        normalsFilter.SplittingOff();
        normalsFilter.FlipNormalsOn();
        normalsFilter.Update();

        vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
        dem.DeepCopy(normalsFilterOutput);

        // Make a copy of heightsGravity per point since we need that later for
        // drawing profile plots.
        heightsGravityPerPoint.DeepCopy(heightsGravity);
        convertPointDataToCellData();

        int centerIndex = startPixel + (endPixel - startPixel) / 2;
        centerOfDEM = new double[3];
        centerOfDEM[0] = data[index(centerIndex,centerIndex,3)];
        centerOfDEM[1] = data[index(centerIndex,centerIndex,4)];
        centerOfDEM[2] = data[index(centerIndex,centerIndex,5)];

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
        pointToCell.SetInput(dem);

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

    private static int index(int i, int j, int k)
    {
        return ((k * MAX_HEIGHT + j) * MAX_WIDTH + i);
    }

    public void generateProfile(ArrayList<Point3D> xyzPointList,
            ArrayList<Double> profileHeights, ArrayList<Double> profileDistances)
    {
        profileHeights.clear();
        profileDistances.clear();

        // For each point in xyzPointList, find the cell containing that
        // point and then, using barycentric coordinates find the value
        // of the height at that point
        //
        // To compute the distance, assume we have a straight line connecting the first
        // and last points of xyzPointList. For each point, p, in xyzPointList, find the point
        // on the line closest to p. The distance from p to the start of the line is what
        // is placed in heights. Use SPICE's nplnpt function for this.

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

        for (Point3D p : xyzPointList)
        {
            int cellId = findClosestCell(p.xyz);

            double val = PolyDataUtil.interpolateWithinCell(dem, heightsGravityPerPoint, cellId, p.xyz, idList);

            profileHeights.add(val);

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

    private void loadLblFile(String file) throws IOException
    {
        InputStream fs = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        while ((line = in.readLine()) != null)
        {
            line = line.trim();

            if (line.startsWith("NAXIS1_LIVE"))
            {
                String[] tokens = line.split("=");
                String size = tokens[1].trim();
                tokens = size.split("/");
                size = tokens[0].trim();
                liveSize = Integer.parseInt(size);
            }
            else if (line.startsWith("NAXIS1_0"))
            {
                String[] tokens = line.split("=");
                String size = tokens[1].trim();
                startPixel = Integer.parseInt(size);
            }
        }

        in.close();
    }

    /**
     * Return whether or not point is inside the DEM. By "inside the DEM" we
     * mean that the point is displaced parallel to the mean normal vector of the
     * DEM, it will intersect the DEM.
     *
     * @param point
     * @return
     */
    public boolean isPointWithinDEM(double[] point)
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

        return cellId >= 0;
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
        heightsGravity.Delete();
        heightsPlane.Delete();
        slopes.Delete();
        super.delete();
    }
}
