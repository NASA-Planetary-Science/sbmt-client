package edu.jhuapl.near.model.eros;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkCellArray;
import vtk.vtkFloatArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.Point3D;
import edu.jhuapl.near.util.PolyDataUtil;

public class DEMModel extends SmallBodyModel
{
    private static int WIDTH = 1027;
    private static int HEIGHT = 1027;
    private vtkIdList idList;
    private vtkPolyData dem;
    private vtkPolyData boundary;
    private vtkFloatArray heights;
    
    public DEMModel(String filename) throws IOException
	{
		idList = new vtkIdList();
		dem = new vtkPolyData();
		boundary = new vtkPolyData();
		heights = new vtkFloatArray();
		
		initializeDEM(filename);

		setSmallBodyPolyData(dem, null, null, null, null);
	}

    private vtkPolyData initializeDEM(String filename) throws IOException
	{
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

        heights.SetNumberOfComponents(1);
        
		FileInputStream fs = new FileInputStream(filename);
		BufferedInputStream bs = new BufferedInputStream(fs);
		DataInputStream in = new DataInputStream(bs);
		
		float[] data = new float[WIDTH*HEIGHT*6];

		for (int i=0;i<data.length; ++i)
		{
			data[i] = MathUtil.swap(in.readFloat());
		}

		in.close();

		idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        int[][] indices = new int[WIDTH][HEIGHT];
        int c = 0;
        int d = 0;
        float x, y, z, h;
        int i0, i1, i2, i3;

        // First add points to the vtkPoints array
        for (int m=0; m<WIDTH; ++m)
			for (int n=0; n<HEIGHT; ++n)
			{
				x = data[index(m,n,3)];
				y = data[index(m,n,4)];
				z = data[index(m,n,5)];
				h = data[index(m,n,0)];
				
				if (m > 0 && m < WIDTH-1 && n > 0 && n < HEIGHT-1)
				{
					if (m == 1 || m == WIDTH-2 || n == 1 || n == HEIGHT-2)
					{
						boundaryPoints.InsertNextPoint(x, y, z);
						idList.SetId(0, d);
						boundaryPolys.InsertNextCell(idList);
						++d;
					}
					
					//points.SetPoint(c, x, y, z);
					points.InsertNextPoint(x, y, z);
					heights.InsertNextTuple1(h);
					
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
        for (int m=1; m<WIDTH; ++m)
			for (int n=1; n<HEIGHT; ++n)
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
		
		dem.DeepCopy(normalsFilter.GetOutput());
		
		return dem;
	}
    
    public vtkPolyData getBoundary()
    {
    	return boundary;
    }

	private static int index(int i, int j, int k)
	{
		return ((k * HEIGHT + j) * WIDTH + i);
	}

    public void generateProfile(ArrayList<Point3D> xyzPointList,
    		ArrayList<Double> profileHeights, ArrayList<Double> profileDistances)
    {
    	profileHeights.clear();
    	profileDistances.clear();
    	
    	vtkPoints points = dem.GetPoints();
    	//vtkFloatArray data = (vtkFloatArray)dem.GetPointData().GetScalars();
    	
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
        
    	for (Point3D p : xyzPointList)
    	{
    		int cellId = findClosestCell(p.xyz);
    		
    		double val = PolyDataUtil.InterpolateWithinCell.func(dem, heights, cellId, p.xyz);
    		
    		profileHeights.add(val);
    		
            if (zeroLineDir)
            {
        		profileDistances.add(0.0);
            }
            else
            {
            	MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
            	double dist = MathUtil.distanceBetween(first, pnear);
            	profileDistances.add(dist);
            }
    	}
    }
}
