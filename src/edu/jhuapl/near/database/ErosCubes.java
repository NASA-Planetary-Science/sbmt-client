package edu.jhuapl.near.database;

import java.util.ArrayList;
import java.util.TreeSet;

import vtk.vtkPolyData;
import edu.jhuapl.near.model.ErosModel;
import edu.jhuapl.near.util.BoundingBox;

public class ErosCubes
{
	public BoundingBox erosBB;
	ArrayList<BoundingBox> allCubes = new ArrayList<BoundingBox>();
	double cubeSize = 1.0;
	int numCubesX;
	int numCubesY;
	int numCubesZ;
	
	public ErosCubes(ErosModel eros)
	{
		erosBB = eros.computeBoundingBox();
		numCubesX = (int)(Math.ceil(erosBB.xmax - erosBB.xmin) / cubeSize);
		numCubesY = (int)(Math.ceil(erosBB.ymax - erosBB.ymin) / cubeSize);
		numCubesZ = (int)(Math.ceil(erosBB.zmax - erosBB.zmin) / cubeSize);
		
		for (int k=0; k<numCubesZ; ++k)
		{
			double zmin = k * cubeSize;
			double zmax = (k+1) * cubeSize;
			for (int j=0; j<numCubesY; ++j)
			{
				double ymin = j * cubeSize;
				double ymax = (j+1) * cubeSize;
				for (int i=0; i<numCubesX; ++i)
				{
					double xmin = i * cubeSize;
					double xmax = (i+1) * cubeSize;
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
	
	public BoundingBox getCube(int cubeId)
	{
		return allCubes.get(cubeId);
	}
	
	public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
	{
		TreeSet<Integer> cubeIds = new TreeSet<Integer>();

		// Iterate through each cube and check if it intersects
		// with the bounding box of any of the polygons of the polydata
		BoundingBox spectrumBB = new BoundingBox(polydata.GetBounds());
		double[] bounds = new double[6];
		
		int numberCubes = numCubesX * numCubesY * numCubesZ;
		for (int i=0; i<numberCubes; ++i)
		{
			// Before checking each polygon individually, first see if the
			// polydata as a whole intersects the cube
			BoundingBox cube = getCube(i);
			if (cube.intersects(spectrumBB))
			{
				int numberPolygons = polydata.GetNumberOfCells();
				for (int j=0; j<numberPolygons; ++j)
				{
					polydata.GetCellBounds(j, bounds);
					BoundingBox polyBB = new BoundingBox(bounds);
					if (cube.intersects(polyBB))
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
