package edu.jhuapl.near.dbgen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
//import vtk.vtkXMLPolyDataWriter;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.SmallBodyCubes;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program goes through all the NLR data and divides all the data
 * up into cubes and saves each cube to a separate file.
 * @author kahneg1
 *
 */
public class NLRCubesGenerator
{
    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();

        SmallBodyModel erosModel = ModelFactory.createErosBodyModel();

        String nlrFileList = args[0];
        String outputFolder = args[1];

        ArrayList<String> nlrFiles = null;
        try {
            nlrFiles = FileUtil.getFileLinesAsStringList(nlrFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        SmallBodyCubes cubes = new SmallBodyCubes(erosModel.getSmallBodyPolyData(), 1.0, 1.0, true);

        double[] pt = new double[3];

        // If a point is farther than MAX_DIST from the asteroid, then throw it out.
        final double MAX_DIST = 1.0;

        vtkPolyData polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray vert = new vtkCellArray();
        polydata.SetPoints( points );
        polydata.SetVerts( vert );

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(1);

        try
        {
            int count = 0;
            int filecount = 1;
            for (String filename : nlrFiles)
            {
                System.out.println("Begin processing file " + filename + " - " + filecount++ + " / " + nlrFiles.size());

                ArrayList<String> lines = FileUtil.getFileLinesAsStringList(filename);

                int[] cubeIdOfPoints = new int[lines.size()];
                cubeIdOfPoints[0] = -1;
                cubeIdOfPoints[1] = -1;

                for (int i=2; i<lines.size(); ++i)
                {
                    String[] vals = lines.get(i).trim().split("\\s+");

                       pt[0] = Double.parseDouble(vals[14])/1000.0;
                    pt[1] = Double.parseDouble(vals[15])/1000.0;
                    pt[2] = Double.parseDouble(vals[16])/1000.0;

                    points.InsertNextPoint(pt);
                    idList.SetId(0, count);
                    vert.InsertNextCell(idList);
                    ++count;

                    if (true)
                        continue;

                    double[] closestPt = erosModel.findClosestPoint(pt);

                    double dist = MathUtil.distanceBetween(pt, closestPt);

                    if (dist > MAX_DIST)
                    {
                        cubeIdOfPoints[i] = -1;
                        continue;
                    }

                    int cubeid = cubes.getCubeId(closestPt);
                    cubeIdOfPoints[i] = cubeid;

                    if (cubeid >= 0)
                    {
                        // Open the file for appending
                        FileWriter fstream = new FileWriter(outputFolder + "/" + cubeid + ".nlr", true);
                        BufferedWriter out = new BufferedWriter(fstream);

                        out.write(lines.get(i) + "\n");

                        out.close();
                    }
                }

                // Save out the cube ids of each line to a new file
                FileWriter fstream = new FileWriter(filename.substring(0, filename.length()-3)+ ".cubeids");
                BufferedWriter out = new BufferedWriter(fstream);

                for (int i=0; i<cubeIdOfPoints.length; ++i)
                    out.write(String.valueOf(cubeIdOfPoints[i]) + "\n");

                out.close();
            }

//            vtkXMLPolyDataWriter writer = new vtkXMLPolyDataWriter();
//            writer.SetInput(polydata);
//            writer.SetFileName("nlrdata.vtp");
//            writer.SetCompressorTypeToZLib();
//            writer.SetDataModeToBinary();
//            writer.Write();
            vtkPolyDataWriter writer = new vtkPolyDataWriter();
            writer.SetInput(polydata);
            writer.SetFileName("nlrdata.vtk");
            writer.SetFileTypeToBinary();
            writer.Write();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
