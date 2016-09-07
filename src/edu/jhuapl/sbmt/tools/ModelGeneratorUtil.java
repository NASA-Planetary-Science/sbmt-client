package edu.jhuapl.sbmt.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import vtk.vtkFloatArray;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.PolyDataUtil;

public class ModelGeneratorUtil
{
    public static void convertCellDataToPointData(vtkPolyData polydata, String cellDataFile)
    {
        vtkFloatArray cellData = new vtkFloatArray();

        cellData.SetNumberOfComponents(1);
        cellData.SetNumberOfTuples(polydata.GetNumberOfCells());

        FileInputStream fs;
        try
        {
            fs = new FileInputStream(cellDataFile);
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            String line;
            int j = 0;
            while ((line = in.readLine()) != null)
            {
                cellData.SetTuple1(j, Float.parseFloat(line));
                ++j;
            }

            in.close();

            vtkFloatArray pointData = new vtkFloatArray();
            PolyDataUtil.generatePointScalarsFromCellScalars(polydata, cellData, pointData);

            int l = cellDataFile.length();
            FileWriter fstream = new FileWriter(cellDataFile.substring(0, l-4) + "_PointData.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            int numPoints = pointData.GetNumberOfTuples();
            for (int i=0; i<numPoints; ++i)
            {
                out.write((float)pointData.GetTuple1(i) + "\n");
            }

            out.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
