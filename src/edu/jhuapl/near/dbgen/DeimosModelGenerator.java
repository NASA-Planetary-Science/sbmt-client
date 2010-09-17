package edu.jhuapl.near.dbgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class DeimosModelGenerator {

	/**
	 * This program converts the Deimos model to
	 * vtk format. Currently there is only 1 resolution level
	 * 
	 * In addition this program generates normals and saves it to the vtk file.
	 * In addition this program generates a coordinate grid and saves it to
	 * vtk files for each resolution.
	 * 
	 * To run this program, 2 arguments are required:
	 * - the folder containing the DEIMOS.PLT file
	 * - the folder where you want to save the generated files.
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

		String[] pltfiles = {"DEIMOS"};

		try
		{
			for (int i=0; i<1; ++i)
			{
				String filename = datadir + "/" + pltfiles[i] + ".PLT";

				InputStream fs = new FileInputStream(filename);
				InputStreamReader isr = new InputStreamReader(fs);
				BufferedReader in = new BufferedReader(isr);

				String vtkfile = outputdir + "/" + pltfiles[i] + ".vtk";
				FileWriter fstream = new FileWriter(vtkfile);
				BufferedWriter out = new BufferedWriter(fstream);

				// Read in the first line which list the number of points and plates
				String val = in.readLine().trim();
				int numPoints = Integer.parseInt(val);

				out.write("# vtk DataFile Version 2.0\n");
				out.write("DEIMOS\n");
				out.write("ASCII\n");
				out.write("DATASET POLYDATA\n");
				out.write("POINTS " + numPoints + " float\n");

				for (int j=0; j<numPoints; ++j)
				{
					String[] vals = in.readLine().trim().split("\\s+");
					out.write(vals[1] + " " + vals[2] + " " + vals[3] + "\n");
				}

				val = in.readLine().trim();
				int numPlates = Integer.parseInt(val);

				out.write("POLYGONS " + numPlates + " " + (numPlates*4) + "\n");

				// Note that the point indices are 1-based in the Gaskell model but vtk
				// requires 0-based. Therefore subract 1 from each index.
				for (int j=0; j<numPlates; ++j)
				{
					String[] vals = in.readLine().trim().split("\\s+");
					int idx1 = Integer.parseInt(vals[1]) - 1;
					int idx2 = Integer.parseInt(vals[2]) - 1;
					int idx3 = Integer.parseInt(vals[3]) - 1;
					out.write("3 " + idx1 + " " + idx2 + " " + idx3 + "\n");
				}

				out.close();
				in.close();

				// Now load the vtk file, generate normals, and save as binary
				vtkPolyDataReader erosReader = new vtkPolyDataReader();
				erosReader.SetFileName(vtkfile);
				erosReader.Update();

				vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
				normalsFilter.SetInputConnection(erosReader.GetOutputPort());
				normalsFilter.SetComputeCellNormals(0);
				normalsFilter.SetComputePointNormals(1);
				normalsFilter.SplittingOff();
				normalsFilter.Update();

				vtkPolyDataWriter writer = new vtkPolyDataWriter();
				writer.SetInputConnection(normalsFilter.GetOutputPort());
				writer.SetFileName(vtkfile);
				writer.SetFileTypeToBinary();
				writer.Write();

				// Generate the coordinate grid
				String gridfile = outputdir + "/coordinate_grid_res" + i + ".vtk";

				vtkPolyData erosPolyData = new vtkPolyData();
				erosPolyData.DeepCopy(erosReader.GetOutput());

				Graticule grid = ModelFactory.createErosGraticuleModel(null);
				grid.generateGrid(erosPolyData);

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
