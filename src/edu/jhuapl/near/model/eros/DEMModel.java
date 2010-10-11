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

public class DEMModel extends SmallBodyModel
{
    private static int WIDTH = 1027;
    private static int HEIGHT = 1027;
    private vtkIdList idList;

    public DEMModel(String filename) throws IOException
	{
		super(initializeDEM(filename));

		idList = new vtkIdList();
	}

    static private vtkPolyData initializeDEM(String filename) throws IOException
	{
		vtkPolyData dem = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();
        vtkFloatArray heights = new vtkFloatArray();
        dem.SetPoints(points);
        dem.SetPolys(polys);
        dem.GetPointData().SetScalars(heights);
		
        heights.SetNumberOfComponents(1);
        
		FileInputStream fs = new FileInputStream(filename);
		BufferedInputStream bs = new BufferedInputStream(fs);
		DataInputStream in = new DataInputStream(bs);
		
		float[] data = new float[WIDTH*HEIGHT*6];

		for (int i=0;i<data.length; ++i)
		{
			data[i] = swap(in.readFloat());
		}

		in.close();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(3);

        int[][] indices = new int[WIDTH][HEIGHT];
        int c = 0;
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

	// This function is taken from http://www.java2s.com/Code/Java/Language-Basics/Utilityforbyteswappingofalljavadatatypes.htm
	private static int swap(int value)
	{
		int b1 = (value >>  0) & 0xff;
	    int b2 = (value >>  8) & 0xff;
	    int b3 = (value >> 16) & 0xff;
	    int b4 = (value >> 24) & 0xff;

	    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	}
	
	// This function is taken from http://www.java2s.com/Code/Java/Language-Basics/Utilityforbyteswappingofalljavadatatypes.htm
	private static float swap(float value)
	{
		int intValue = Float.floatToRawIntBits(value);
		intValue = swap(intValue);
		return Float.intBitsToFloat(intValue);
	}

	private static int index(int i, int j, int k)
	{
		return ((k * HEIGHT + j) * WIDTH + i);
	}

    public void generateProfile(ArrayList<Point3D> xyzPointList,
    		ArrayList<Double> heights, ArrayList<Double> distances)
    {
    	heights.clear();
    	distances.clear();
    	
    	vtkPolyData dem = getSmallBodyPolyData();
    	vtkPoints points = dem.GetPoints();
    	vtkFloatArray data = (vtkFloatArray)dem.GetPointData().GetScalars();
    	
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
        
        double[] pnear = new double[3];
        double[] notused = new double[1];
        
    	for (Point3D p : xyzPointList)
    	{
    		int cellId = findClosestCell(p.xyz);
    		dem.GetCellPoints(cellId, idList);
    		double[] p1 = points.GetPoint(idList.GetId(0));
    		double[] p2 = points.GetPoint(idList.GetId(1));
    		double[] p3 = points.GetPoint(idList.GetId(2));
    		double v1 = data.GetTuple1(idList.GetId(0));
    		double v2 = data.GetTuple1(idList.GetId(1));
    		double v3 = data.GetTuple1(idList.GetId(2));
    		double val = MathUtil.interpolateWithinTriangle(p.xyz, p1, p2, p3, v1, v2, v3);
    		heights.add(val);
    		
    		MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
    		double dist = MathUtil.distanceBetween(first, pnear);
    		distances.add(dist);
    	}
    }
}
