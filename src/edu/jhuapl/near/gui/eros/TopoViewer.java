package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.near.gui.vtkRenderWindowPanelWithMouseWheel;

public class TopoViewer extends JFrame
{
	private vtkRenderWindowPanelWithMouseWheel renWin;
	private vtkPolyData dem;
    private static final float PDS_NA = -1.e32f;
    private static int WIDTH = 1027;
    private static int HEIGHT = 1027;
    
	public TopoViewer()
	{
		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
		setIconImage(erosIcon.getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		renWin = new vtkRenderWindowPanelWithMouseWheel();

        JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(renWin, BorderLayout.CENTER);

		add(panel, BorderLayout.CENTER);
		
        dem = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();

        dem.SetPoints(points);
        dem.SetPolys(polys);
	}

	public void setCubeFile(String filename) throws IOException
	{
		FileInputStream fs = new FileInputStream(filename);
		BufferedInputStream bs = new BufferedInputStream(fs);
		DataInputStream in = new DataInputStream(bs);
		
		float[] data = new float[WIDTH*HEIGHT*6];

		for (int i=0;i<data.length; ++i)
		{
			data[i] = swap(in.readFloat());
		}

		in.close();

		
		vtkPoints points = dem.GetPoints();
		vtkCellArray polys = dem.GetPolys();

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(3);

        int[][] indices = new int[WIDTH][HEIGHT];
        int c = 0;
        float x, y, z;
        int i0, i1, i2, i3;

        // First add points to the vtkPoints array
        for (int m=0; m<WIDTH; ++m)
			for (int n=0; n<HEIGHT; ++n)
			{
				x = data[index(m,n,3)];
				y = data[index(m,n,4)];
				z = data[index(m,n,5)];
		
				if (isValidPoint(x, y, z))
				{
					//points.SetPoint(c, x, y, z);
					points.InsertNextPoint(x, y, z);
					
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
        
        vtkPolyDataMapper demMapper = new vtkPolyDataMapper();
        demMapper.SetInput(dem);
        demMapper.Update();
        
        vtkActor demActor = new vtkActor();
        demActor.SetMapper(demMapper);

		renWin.GetRenderer().AddViewProp(demActor);
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

	private static boolean isValidPoint(float x, float y, float z)
    {
    	if (x <= PDS_NA || y <= PDS_NA || z <= PDS_NA)
    		return false;
    	else
    		return true;
    }
}
