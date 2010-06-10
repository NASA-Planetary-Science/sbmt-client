package edu.jhuapl.near.util;

import java.util.ArrayList;
import java.util.TreeSet;

import vtk.vtkPolyData;
import edu.jhuapl.near.model.SmallBodyModel;

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
	private double cubeSize = 1.0;
	private double buffer = 0.01;
//	private boolean useHardCodedValues = true;
	private int numCubesX;
	private int numCubesY;
	private int numCubesZ;

	public SmallBodyCubes(SmallBodyModel smallBodyModel, double cubeSize, double buffer)
	{
		this.cubeSize = cubeSize;
		this.buffer = buffer;
//		this.useHardCodedValues = false;
		initialize(smallBodyModel);
	}
	
	public SmallBodyCubes(SmallBodyModel smallBodyModel)
	{
		this.initialize(smallBodyModel);
	}
	
	private void initialize(SmallBodyModel smallBodyModel)
	{
		boundingBox = smallBodyModel.getBoundingBox();

		boundingBox.xmax += buffer;
		boundingBox.xmin -= buffer;
		boundingBox.ymax += buffer;
		boundingBox.ymin -= buffer;
		boundingBox.zmax += buffer;
		boundingBox.zmin -= buffer;
		
		
		numCubesX = (int)(Math.ceil(boundingBox.xmax - boundingBox.xmin) / cubeSize);
		numCubesY = (int)(Math.ceil(boundingBox.ymax - boundingBox.ymin) / cubeSize);
		numCubesZ = (int)(Math.ceil(boundingBox.zmax - boundingBox.zmin) / cubeSize);
		
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
		
		// Change the following to false to actually compute the 
		// values stored in the erosIntersectingCubes array. This can take
		// a long time which is why we hard code the values into this class.
//		if (useHardCodedValues)
//		{
//			ArrayList<BoundingBox> tmpCubes = new ArrayList<BoundingBox>();
//			for (int i : erosIntersectingCubes)
//			{
//				tmpCubes.add(allCubes.get(i));
//			}
//			allCubes = tmpCubes;
//		}
//		else
		{
			System.out.println("numCubesX " + numCubesX);
			System.out.println("numCubesY " + numCubesY);
			System.out.println("numCubesZ " + numCubesZ);

			System.out.println("total cubes before reduction = " + allCubes.size());
			System.out.println("int[] erosIntersectingCubes = {");

			// Remove from allCubes all cubes that do not intersect the asteroid
			long t0 = System.currentTimeMillis();
			TreeSet<Integer> intersectingCubes = getIntersectingCubes(smallBodyModel.getSmallBodyPolyData());
			System.out.println("Time elapsed:  " + ((double)System.currentTimeMillis()-t0)/1000.0);

			ArrayList<BoundingBox> tmpCubes = new ArrayList<BoundingBox>();
			int count = 0;
			for (Integer i : intersectingCubes)
			{
				tmpCubes.add(allCubes.get(i));
				System.out.print(i);
				if (count < intersectingCubes.size()-1)
					System.out.print(",");
				++count;
				if (count % 15 == 0)
					System.out.println("");
			}
			System.out.println("};");

			allCubes = tmpCubes;

			System.out.println("finished initializing cubes, total = " + allCubes.size());
		}
	}
	
	public BoundingBox getCube(int cubeId)
	{
		return allCubes.get(cubeId);
	}

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

	
	public int getCubeId(double[] pt)
	{
		if (!boundingBox.contains(pt))
			return -1;
		
		int numberCubes = allCubes.size();
		for (int i=0; i<numberCubes; ++i)
		{
			BoundingBox cube = getCube(i);
			if (cube.contains(pt))
				return i;
		}

		// If we reach here something is wrong
		System.err.println("Error: could not find cube");
		
		return -1;
		
//		double x = pt[0];
//		double y = pt[1];
//		double z = pt[2];
//		
//		return (int)Math.floor((x - erosBB.xmin) / cubeSize) +
//		(int)Math.floor((y - erosBB.ymin) / cubeSize)*numCubesX +
//		(int)Math.floor((z - erosBB.zmin) / cubeSize)*numCubesX*numCubesY; 
	}
}

