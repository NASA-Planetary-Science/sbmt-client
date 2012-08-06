package edu.jhuapl.near.util;

import java.util.ArrayList;
import java.util.TreeSet;

import vtk.vtkPolyData;

/**
 * This class is used to subdivide the bounding box of a shape model
 * into a contiguous grid of 3D cubes.
 * @author kahneg1
 *
 */
public class SmallBodyCubes
{
    private BoundingBox boundingBox;
    private ArrayList<BoundingBox> allCubes = new ArrayList<BoundingBox>();
    private final double cubeSize;
    private final double buffer;

    /**
     * Create a cube set structure for the given model, where each cube has side <tt>cubeSize</tt>
     * and <tt>buffer</tt> is added to all sides of the bounding box of the model. Cubes
     * that do not intersect the asteroid at all are removed.
     *
     * @param smallBodyPolyData
     * @param cubeSize
     * @param buffer
     */
    public SmallBodyCubes(
            vtkPolyData smallBodyPolyData,
            double cubeSize,
            double buffer,
            boolean removeEmptyCubes)
    {
        this.cubeSize = cubeSize;
        this.buffer = buffer;

        initialize(smallBodyPolyData);

        if (removeEmptyCubes)
            removeEmptyCubes(smallBodyPolyData);
    }

    private void initialize(vtkPolyData smallBodyPolyData)
    {
        smallBodyPolyData.ComputeBounds();
        boundingBox = new BoundingBox(smallBodyPolyData.GetBounds());

        boundingBox.xmax += buffer;
        boundingBox.xmin -= buffer;
        boundingBox.ymax += buffer;
        boundingBox.ymin -= buffer;
        boundingBox.zmax += buffer;
        boundingBox.zmin -= buffer;


        int numCubesX = (int)Math.ceil( (boundingBox.xmax - boundingBox.xmin) / cubeSize );
        int numCubesY = (int)Math.ceil( (boundingBox.ymax - boundingBox.ymin) / cubeSize );
        int numCubesZ = (int)Math.ceil( (boundingBox.zmax - boundingBox.zmin) / cubeSize );

        for (int k=0; k<numCubesZ; ++k)
        {
            double zmin = boundingBox.zmin + k * cubeSize;
            double zmax = boundingBox.zmin + (k+1) * cubeSize;
            for (int j=0; j<numCubesY; ++j)
            {
                double ymin = boundingBox.ymin + j * cubeSize;
                double ymax = boundingBox.ymin + (j+1) * cubeSize;
                for (int i=0; i<numCubesX; ++i)
                {
                    double xmin = boundingBox.xmin + i * cubeSize;
                    double xmax = boundingBox.xmin + (i+1) * cubeSize;
                    BoundingBox bb = new BoundingBox();
                    bb.xmin = xmin;
                    bb.xmax = xmax;
                    bb.ymin = ymin;
                    bb.ymax = ymax;
                    bb.zmin = zmin;
                    bb.zmax = zmax;
                    allCubes.add(bb);
                }
            }
        }
    }

    private void removeEmptyCubes(vtkPolyData smallBodyPolyData)
    {
        System.out.println("total cubes before reduction = " + allCubes.size());

        // Remove from allCubes all cubes that do not intersect the asteroid
        //long t0 = System.currentTimeMillis();
        TreeSet<Integer> intersectingCubes = getIntersectingCubes(smallBodyPolyData);
        //System.out.println("Time elapsed:  " + ((double)System.currentTimeMillis()-t0)/1000.0);

        ArrayList<BoundingBox> tmpCubes = new ArrayList<BoundingBox>();
        for (Integer i : intersectingCubes)
        {
            tmpCubes.add(allCubes.get(i));
        }

        allCubes = tmpCubes;

        System.out.println("finished initializing cubes, total = " + allCubes.size());
    }

    public BoundingBox getCube(int cubeId)
    {
        return allCubes.get(cubeId);
    }

    /**
     * Get all the cubes that intersect with <tt>polydata</tt>
     * @param polydata
     * @return
     */
    public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
    {
        TreeSet<Integer> cubeIds = new TreeSet<Integer>();

        // Iterate through each cube and check if it intersects
        // with the bounding box of any of the polygons of the polydata

        BoundingBox polydataBB = new BoundingBox(polydata.GetBounds());
        int numberPolygons = polydata.GetNumberOfCells();


        // Store all the bounding boxes of all the individual polygons in an array first
        // since the call to GetCellBounds is very slow.
        double[] cellBounds = new double[6];
        ArrayList<BoundingBox> polyCellsBB = new ArrayList<BoundingBox>();
        for (int j=0; j<numberPolygons; ++j)
        {
            polydata.GetCellBounds(j, cellBounds);
            polyCellsBB.add(new BoundingBox(cellBounds));
        }


        int numberCubes = allCubes.size();
        for (int i=0; i<numberCubes; ++i)
        {
            // Before checking each polygon individually, first see if the
            // polydata as a whole intersects the cube
            BoundingBox cube = getCube(i);
            if (cube.intersects(polydataBB))
            {
                for (int j=0; j<numberPolygons; ++j)
                {
                    BoundingBox bb = polyCellsBB.get(j);
                    if (cube.intersects(bb))
                    {
                        cubeIds.add(i);
                        break;
                    }
                }
            }
        }

        return cubeIds;
    }

    /**
     * Get all the cubes that intersect with BoundingBox <tt>bb</tt>
     * @param bb
     * @return
     */
    public TreeSet<Integer> getIntersectingCubes(BoundingBox bb)
    {
        TreeSet<Integer> cubeIds = new TreeSet<Integer>();

        int numberCubes = allCubes.size();
        for (int i=0; i<numberCubes; ++i)
        {
            BoundingBox cube = getCube(i);
            if (cube.intersects(bb))
            {
                cubeIds.add(i);
            }
        }

        return cubeIds;
    }

    /**
     * Get the id of the cube containing <tt>point</tt>
     * @param point
     * @return
     */
    public int getCubeId(double[] point)
    {
        if (!boundingBox.contains(point))
            return -1;

        int numberCubes = allCubes.size();
        for (int i=0; i<numberCubes; ++i)
        {
            BoundingBox cube = getCube(i);
            if (cube.contains(point))
                return i;
        }

        // If we reach here something is wrong
        System.err.println("Error: could not find cube");

        return -1;
    }
}

