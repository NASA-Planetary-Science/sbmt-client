package edu.jhuapl.near.server;

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
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ErosModelGenerator {

    /**
     * This program converts a Bob Gaskell shape models that can be downloaded
     * from http://sbn.psi.edu/pds/asteroid/NEAR_A_MSI_5_EROSSHAPE_V1_0.zip to
     * vtk format. This program can convert these 4 files:
     *
     * 1. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver64q.tab
     * 2. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver128q.tab
     * 3. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver256q.tab
     * 4. NEAR_A_MSI_5_EROSSHAPE_V1_0/data/vertex/ver512q.tab
     *
     * This program can also be used to convert Itokawa shape models as well.
     * (download at http://sbn.psi.edu/pds/asteroid/HAY_A_AMICA_5_ITOKAWASHAPE_V1_0.zip)
     *
     * In addition this program generates normals and saves it to the vtk file.
     * In addition this program generates a coordinate grid and saves it to
     * vtk files for each resolution.
     *
     * To run this program, 2 arguments are required:
     * - the folder containing the ver*q.tab files
     * - the folder where you want to save the generated files.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String datadir = args[0];
        String outputdir = args[1];

        String[] tabfiles = {"ver64q", "ver128q", "ver256q", "ver512q"};

        try
        {
            for (int i=0; i<4; ++i)
            {
                String filename = datadir + "/" + tabfiles[i] + ".tab";

                InputStream fs = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fs);
                BufferedReader in = new BufferedReader(isr);

                String vtkfile = outputdir + "/" + tabfiles[i] + ".vtk";
                FileWriter fstream = new FileWriter(vtkfile);
                BufferedWriter out = new BufferedWriter(fstream);

                // Read in the first line which list the number of points and plates
                String[] vals = in.readLine().trim().split("\\s+");

                int numPoints = Integer.parseInt(vals[0]);
                int numPlates = Integer.parseInt(vals[1]);

                out.write("# vtk DataFile Version 2.0\n");
                out.write("NEAR-A-MSI-5-EROSSHAPE-V1.0\n");
                out.write("ASCII\n");
                out.write("DATASET POLYDATA\n");
                out.write("POINTS " + numPoints + " float\n");

                for (int j=0; j<numPoints; ++j)
                {
                    vals = in.readLine().trim().split("\\s+");
                    out.write(vals[1] + " " + vals[2] + " " + vals[3] + "\n");
                }

                out.write("POLYGONS " + numPlates + " " + (numPlates*4) + "\n");

                // Note that the point indices are 1-based in the Gaskell model but vtk
                // requires 0-based. Therefore subract 1 from each index.
                for (int j=0; j<numPlates; ++j)
                {
                    vals = in.readLine().trim().split("\\s+");
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

                Graticule grid = new Graticule(null);
                grid.generateGrid(erosPolyData);

                writer = new vtkPolyDataWriter();
                writer.SetInput(grid.getGridAsPolyData());
                writer.SetFileName(gridfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                if (i == 0)
                {
                    String coloringfile = datadir + "/Eros_Dec2006_0_Elevation.txt";
                    ModelGeneratorUtil.convertCellDataToPointData(erosPolyData, coloringfile);
                    coloringfile = datadir + "/Eros_Dec2006_0_GravitationalAcceleration.txt";
                    ModelGeneratorUtil.convertCellDataToPointData(erosPolyData, coloringfile);
                    coloringfile = datadir + "/Eros_Dec2006_0_GravitationalPotential.txt";
                    ModelGeneratorUtil.convertCellDataToPointData(erosPolyData, coloringfile);
                    coloringfile = datadir + "/Eros_Dec2006_0_Slope.txt";
                    ModelGeneratorUtil.convertCellDataToPointData(erosPolyData, coloringfile);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
