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
import edu.jhuapl.near.model.vesta.VestaGraticule;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class VestaModelGenerator
{
    /**
     *
     * This program converts the Gaskell Vesta shape model
     *
     * In addition this program generates normals and saves it to the vtk file.
     * In addition this program generates a coordinate grid and saves it to a
     * vtk file.
     *
     * To run this program, 2 arguments are required:
     * - the folder containing the .tab file
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

        String[] pltfiles = {"SHAPE64","SHAPE128","SHAPE256","SHAPE512"};
        String[] outfiles = {"Vesta_res0","Vesta_res1","Vesta_res2","Vesta_res3"};

        try
        {
            for (int i=0; i<4; ++i)
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
                out.write("VESTA\n");
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
                vtkPolyDataReader vestaReader = new vtkPolyDataReader();
                vestaReader.SetFileName(vtkfile);
                vestaReader.Update();

                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
                normalsFilter.SetInputConnection(vestaReader.GetOutputPort());
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

                vtkPolyData vestaPolyData = new vtkPolyData();
                vestaPolyData.DeepCopy(vestaReader.GetOutput());

                Graticule grid = new VestaGraticule(null);
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

    /*
     * Similar to previous version but uses VTK's vtkDecimatePro to
     * do the reductions rather than Gaskell's DUMBER program.
     * Resulting shape model doesn't look as nice as compared to Gaskell's.
     *
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

        String shapefile = args[0];
        String outputdir = args[1];

        try
        {
//            for (int i=0; i<1; ++i)
//            {
                String filename = shapefile;

                InputStream fs = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fs);
                BufferedReader in = new BufferedReader(isr);

                String vtkfile = outputdir + "/Vesta_res3.vtk";
                FileWriter fstream = new FileWriter(vtkfile);
                BufferedWriter out = new BufferedWriter(fstream);

                // Read in the first line which list the number of points and plates
                String[] vals = in.readLine().trim().split("\\s+");

                int numPoints = Integer.parseInt(vals[0]);
                int numPlates = Integer.parseInt(vals[1]);

                out.write("# vtk DataFile Version 2.0\n");
                out.write("VESTA\n");
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
                vtkPolyDataReader vestaReader = new vtkPolyDataReader();
                vestaReader.SetFileName(vtkfile);
                vestaReader.Update();

                vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
                normalsFilter.SetInputConnection(vestaReader.GetOutputPort());
                normalsFilter.SetComputeCellNormals(0);
                normalsFilter.SetComputePointNormals(1);
                normalsFilter.SplittingOff();
                normalsFilter.Update();
                vtkAlgorithmOutput normalsOutput = normalsFilter.GetOutputPort();

                vtkPolyDataWriter writer = new vtkPolyDataWriter();
                writer.SetInputConnection(normalsOutput);
                writer.SetFileName(vtkfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                // Generate the coordinate grid
                String gridfile = outputdir + "/coordinate_grid_res" + "3" + ".vtk";

                vtkPolyData vestaPolyData = new vtkPolyData();
                vestaPolyData.DeepCopy(vestaReader.GetOutput());

                Graticule grid = new VestaGraticule(null);
                grid.generateGrid(vestaPolyData);

                writer = new vtkPolyDataWriter();
                writer.SetInput(grid.getGridAsPolyData());
                writer.SetFileName(gridfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                System.out.println(vestaPolyData);
                System.out.println("Finished converting level 3");
//            }


            double[] reductions = {49152.0/3145728.0, 196608.0/3145728.0, 786432.0/3145728.0};

            // Now decimate the model to produce lower res models
            for (int i=0; i<3; ++i)
            {
                vtkDecimatePro dec = new vtkDecimatePro();
                //vtkQuadricDecimation dec = new vtkQuadricDecimation();
                dec.SetInputConnection(normalsOutput);
                dec.SetTargetReduction(1.0 - reductions[i]);
                dec.PreserveTopologyOn();
                dec.SplittingOff();
                dec.BoundaryVertexDeletionOff();
                dec.SetMaximumError(Double.MAX_VALUE);
                dec.AccumulateErrorOn();
                dec.PreSplitMeshOn();
                //dec.SetSplitAngle(45);
                //dec.SetFeatureAngle(75);
                dec.Update();
                vtkAlgorithmOutput decOutput = dec.GetOutputPort();

                vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
                cleanFilter.SetInputConnection(decOutput);
                cleanFilter.Update();
                vtkAlgorithmOutput cleanOutput = cleanFilter.GetOutputPort();

                vtkfile = outputdir + "/Vesta_res" + i + ".vtk";

                writer = new vtkPolyDataWriter();
                writer.SetInputConnection(cleanOutput);
                writer.SetFileName(vtkfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                // Generate the coordinate grid
                gridfile = outputdir + "/coordinate_grid_res" + i + ".vtk";

                vestaPolyData = new vtkPolyData();
                vestaPolyData.DeepCopy(dec.GetOutput());

                grid = new VestaGraticule(null);
                grid.generateGrid(vestaPolyData);

                writer = new vtkPolyDataWriter();
                writer.SetInput(grid.getGridAsPolyData());
                writer.SetFileName(gridfile);
                writer.SetFileTypeToBinary();
                writer.Write();

                System.out.println(vestaPolyData.GetNumberOfCells());
                //System.out.println("actual reduction " + dec.GetActualReduction());
                System.out.println("Finished converting level " + i);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
     */

    /*

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

                Graticule grid = new VestaGraticule(null);
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
    */
}
