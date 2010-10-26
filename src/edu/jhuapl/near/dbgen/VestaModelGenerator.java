package edu.jhuapl.near.dbgen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import vtk.vtkCellArray;
import vtk.vtkCleanPolyData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class VestaModelGenerator {

	/**
	 * This program converts the Hubble Vesta shape model that can be downloaded
	 * from http://sbn.psi.edu/pds/asteroid/EAR_A_5_DDR_SHAPE_MODELS_V2_1.zip to
	 * vtk format. This program can convert the file:
	 * 
	 * 1. EAR_A_5_DDR_SHAPE_MODELS_V2_1/4vesta.tab
	 * 
	 * In addition this program generates normals and saves it to the vtk file.
	 * In addition this program generates a coordinate grid and saves it to a
	 * vtk file.
	 * 
	 * To run this program, 2 arguments are required:
	 * - the folder containing the 4vesta.tab file
	 * - the folder where you want to save the generated files
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
        	public void run()
        	{
        		// do nothing
        	}
        });
		
		NativeLibraryLoader.loadVtkLibrariesLinuxNoX11();

		String datadir = args[0];
		String outputdir = args[1];

		String[] tabfiles = {"4vesta"};

		try
		{
			for (int i=0; i<1; ++i)
			{
				String filename = datadir + "/" + tabfiles[i] + ".tab";

				InputStream fs = new FileInputStream(filename);
				InputStreamReader isr = new InputStreamReader(fs);
				BufferedReader in = new BufferedReader(isr);

				String vtkfile = outputdir + "/" + tabfiles[i] + ".vtk";

				vtkPolyData body = new vtkPolyData();
				vtkPoints points = new vtkPoints();
		        vtkCellArray polys = new vtkCellArray();
		        body.SetPoints(points);
		        body.SetPolys(polys);
				
				int numRows = 37;
				int numCols = 73;
				int[][] indices = new int[numRows][numCols];
				int count = 0;
				for (int m=0; m<numRows; ++m)
					for (int n=0; n<numCols; ++n)
					{
						String[] vals = in.readLine().trim().split("\\s+");
						double lat = Double.parseDouble(vals[0]);
						double lon = Double.parseDouble(vals[1]);
						double rad = Double.parseDouble(vals[2]);

						if (lat == -90.0 || lat == 90.0 || lon == 360.0)
							lon = 0.0;
						
						LatLon ll = new LatLon(lat*Math.PI/180.0, lon*Math.PI/180.0, rad);
						double[] pt = MathUtil.latrec(ll);
						indices[m][n] = count++;
						points.InsertNextPoint(pt);
					}

				in.close();

				
		        // Now add connectivity information
		        int i0, i1, i2, i3;
		        vtkIdList idList = new vtkIdList();
		        idList.SetNumberOfIds(3);
		        for (int m=1; m<numRows; ++m)
					for (int n=1; n<numCols; ++n)
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
						else
						{
							System.out.println("Error occurred");
						}
						
						// Add bottom right triangle
						if (i2>=0 && i1>=0 && i3>=0)
						{
							idList.SetId(0, i2);
							idList.SetId(1, i1);
							idList.SetId(2, i3);
							polys.InsertNextCell(idList);
						}
						else
						{
							System.out.println("Error occurred");
						}
					}


				vtkCleanPolyData cleanPolyData = new vtkCleanPolyData();
				cleanPolyData.SetInput(body);
				cleanPolyData.PointMergingOn();
				cleanPolyData.ConvertLinesToPointsOff();
				cleanPolyData.ConvertPolysToLinesOff();
				cleanPolyData.ConvertStripsToPolysOff();
				cleanPolyData.Update();
				
				vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
				normalsFilter.SetInputConnection(cleanPolyData.GetOutputPort());
				normalsFilter.SetComputeCellNormals(0);
				normalsFilter.SetComputePointNormals(1);
				normalsFilter.SplittingOff();
				normalsFilter.AutoOrientNormalsOn();
				normalsFilter.Update();

				vtkPolyDataWriter writer = new vtkPolyDataWriter();
				writer.SetInputConnection(normalsFilter.GetOutputPort());
				writer.SetFileName(vtkfile);
				writer.SetFileTypeToBinary();
				writer.Write();

				// Generate the coordinate grid
				String gridfile = outputdir + "/coordinate_grid_res" + i + ".vtk";

				vtkPolyData vestaPolyData = new vtkPolyData();
				vestaPolyData.DeepCopy(cleanPolyData.GetOutput());

				Graticule grid = ModelFactory.createVestaGraticuleModel(null);
				grid.generateGrid(vestaPolyData);

				writer = new vtkPolyDataWriter();
				writer.SetInput(grid.getGridAsPolyData());
				writer.SetFileName(gridfile);
				writer.SetFileTypeToBinary();
				writer.Write();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
