package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.near.gui.AbstractStructureMappingControlPanel;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.GenericPopupManager;

public class TopoViewer extends JFrame
{
	private vtkPolyData dem;
    private static int WIDTH = 1027;
    private static int HEIGHT = 1027;
    
	public TopoViewer(String filename) throws IOException
	{
        dem = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();

        dem.SetPoints(points);
        dem.SetPolys(polys);

        setCubeFile(filename);
        
        setupGui();
	}
	
	private void setupGui()
	{
		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
		setIconImage(erosIcon.getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    	StatusBar statusBar = new StatusBar();
    	add(statusBar, BorderLayout.PAGE_END);
    	
		ModelManager modelManager = new ModelManager();
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        SmallBodyModel body = new SmallBodyModel(dem);
        LineModel lineModel = new LineModel(body);
        lineModel.setMaximumVerticesPerLine(2);
        allModels.put(ModelNames.SMALL_BODY, body);
    	allModels.put(ModelNames.LINE_STRUCTURES, lineModel);
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(body));
    	allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(body));
    	allModels.put(ModelNames.CIRCLE_SELECTION, new RegularPolygonModel(body,20,false,"Selection",ModelNames.CIRCLE_SELECTION));
    	modelManager.setModels(allModels);

		Renderer renderer = new Renderer(modelManager);

		GenericPopupManager popupManager = new GenericPopupManager(modelManager);

		PickManager pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(400, 400));

        JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(renderer, BorderLayout.CENTER);

		TopoPlot plot = new TopoPlot(lineModel);
		plot.setMinimumSize(new Dimension(100, 100));
        plot.setPreferredSize(new Dimension(400, 400));
		
		panel.add(plot, BorderLayout.EAST);
		
		StructureModel structureModel = 
			(StructureModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
		JPanel lineStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.LINE_DRAW,
				true) {});

		panel.add(lineStructuresMapperPanel, BorderLayout.SOUTH);

		add(panel, BorderLayout.CENTER);

        // Finally make the frame visible
        setTitle("Mapmaker View");
        pack();
        setVisible(true);
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

				if (m > 0 && m < WIDTH-1 && n > 0 && n < HEIGHT-1)
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
        
		vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
		normalsFilter.SetInput(dem);
		normalsFilter.SetComputeCellNormals(0);
		normalsFilter.SetComputePointNormals(1);
		normalsFilter.SplittingOff();
		normalsFilter.FlipNormalsOn();
		normalsFilter.Update();
		
		dem.DeepCopy(normalsFilter.GetOutput());
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
}
