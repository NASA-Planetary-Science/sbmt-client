package edu.jhuapl.near.database;

import java.util.ArrayList;
import java.util.TreeSet;

import vtk.vtkPolyData;
import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.util.BoundingBox;

public class ErosCubes
{
	private BoundingBox erosBB;
	private ArrayList<BoundingBox> allCubes = new ArrayList<BoundingBox>();
	private double cubeSize = 1.0;
	
	public ErosCubes(ErosModel eros)
	{
		erosBB = eros.computeBoundingBox();

		double buffer = 0.01;
		erosBB.xmax += buffer;
		erosBB.xmin -= buffer;
		erosBB.ymax += buffer;
		erosBB.ymin -= buffer;
		erosBB.zmax += buffer;
		erosBB.zmin -= buffer;
		
		int numCubesX = (int)(Math.ceil(erosBB.xmax - erosBB.xmin) / cubeSize);
		int numCubesY = (int)(Math.ceil(erosBB.ymax - erosBB.ymin) / cubeSize);
		int numCubesZ = (int)(Math.ceil(erosBB.zmax - erosBB.zmin) / cubeSize);
		
		System.out.println("numCubesX " + numCubesX);
		System.out.println("numCubesY " + numCubesY);
		System.out.println("numCubesZ " + numCubesZ);

		for (int k=0; k<numCubesZ; ++k)
		{
			double zmin = erosBB.zmin + k * cubeSize;
			double zmax = erosBB.zmin + (k+1) * cubeSize;
			for (int j=0; j<numCubesY; ++j)
			{
				double ymin = erosBB.ymin + j * cubeSize;
				double ymax = erosBB.ymin + (j+1) * cubeSize;
				for (int i=0; i<numCubesX; ++i)
				{
					double xmin = erosBB.xmin + i * cubeSize;
					double xmax = erosBB.xmin + (i+1) * cubeSize;
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

		System.out.println("total cubes before reduction = " + allCubes.size());

		// We can remove from allCubes all cubes that do not intersect the asteroid
		TreeSet<Integer> intersectingCubes = getIntersectingCubes(eros.getErosPolyData());
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
	
	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		TreeSet<Integer> cubeIds = new TreeSet<Integer>();

		// Iterate through each cube and check if it intersects
		// with the bounding box of any of the polygons of the polydata

		BoundingBox polydataBB = new BoundingBox(polydata.GetBounds());
		int numberPolygons = polydata.GetNumberOfCells();
		
		double[] cellBounds = new double[6];
		BoundingBox polyCellBB = new BoundingBox();

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
					polydata.GetCellBounds(j, cellBounds);
					polyCellBB.setBounds(cellBounds);
					if (cube.intersects(polyCellBB))
					{
						cubeIds.add(i);
						break;
					}
				}
			}
		}
		
		return cubeIds;
	}

	/*
	public int getCubeId(double[] pt)
	{
		double x = pt[0];
		double y = pt[1];
		double z = pt[2];
		
		return (int)Math.floor((x - erosBB.xmin) / cubeSize) +
		(int)Math.floor((y - erosBB.ymin) / cubeSize)*numCubesX +
		(int)Math.floor((z - erosBB.zmin) / cubeSize)*numCubesX*numCubesY; 
	}
	*/
}
