package edu.jhuapl.near.tools;

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

import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class SmallBodyModelGenerator
{
    /**
     *
     * This program converts a Gaskell shape model
     *
     * In addition this program generates normals and saves it to the vtk file.
     * In addition this program generates a coordinate grid and saves it to a
     * vtk file.
     *
     * To run this program, 3 arguments are required:
     * - the folder containing the original .PLT or .tab files
     * - the folder where you want to save the generated files
     * - the name of the model (e.g. "CERES")
     * @param args
    */
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String datadir = args[0];
        String outputdir = args[1];

        String name = args[2];

        String[] pltfiles = {"SHAPE64","SHAPE128","SHAPE256","SHAPE512"};
        String[] outfiles = {"ver64q","ver128q","ver256q","ver512q"};

        try
        {
            for (int i=0; i<pltfiles.length; ++i)
            {
                String filename = datadir + "/" + pltfiles[i] + ".PLT";

                InputStream fs = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fs);
                BufferedReader in = new BufferedReader(isr);

                String vtkfile = outputdir + "/" + outfiles[i] + ".vtk";
                FileWriter fstream = new FileWriter(vtkfile);
                BufferedWriter out = new BufferedWriter(fstream);

                // Read in the first line which list the number of points and plates
                String[] val = in.readLine().trim().split("\\s+");
                int numPoints = Integer.parseInt(val[0]);
                int numPlates = -1;
                if (val.length >= 2)
                    numPlates = Integer.parseInt(val[1]);

                out.write("# vtk DataFile Version 2.0\n");
                out.write(name.toUpperCase() + "\n");
                out.write("ASCII\n");
                out.write("DATASET POLYDATA\n");
                out.write("POINTS " + numPoints + " float\n");

                for (int j=0; j<numPoints; ++j)
                {
                    String[] vals = in.readLine().trim().split("\\s+");
                    out.write(vals[1] + " " + vals[2] + " " + vals[3] + "\n");
                }

                if (numPlates == -1)
                {
                    String numPlatesStr = in.readLine().trim();
                    numPlates = Integer.parseInt(numPlatesStr);
                }

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
                vtkPolyDataReader smallBodyReader = new vtkPolyDataReader();
                smallBodyReader.SetFileName(vtkfile);
                smallBodyReader.Update();

                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
                normalsFilter.SetInputConnection(smallBodyReader.GetOutputPort());
                normalsFilter.SetComputeCellNormals(0);
                normalsFilter.SetComputePointNormals(1);
                normalsFilter.AutoOrientNormalsOn();
                normalsFilter.SplittingOff();
                normalsFilter.Update();

                vtkPolyDataWriter writer = new vtkPolyDataWriter();
                writer.SetInputConnection(normalsFilter.GetOutputPort());
                writer.SetFileName(vtkfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                // Generate the coordinate grid
                String gridfile = outputdir + "/coordinate_grid_res" + i + ".vtk";

                vtkPolyData smallBodyPolyData = new vtkPolyData();
                smallBodyPolyData.DeepCopy(smallBodyReader.GetOutput());

                Graticule grid = new Graticule(null);
                grid.generateGrid(smallBodyPolyData);

                writer = new vtkPolyDataWriter();
                writer.SetInputData(grid.getGridAsPolyData());
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
