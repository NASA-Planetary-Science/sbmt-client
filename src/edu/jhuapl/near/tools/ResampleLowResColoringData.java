package edu.jhuapl.near.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkTriangle;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.util.NativeLibraryLoader;


public class ResampleLowResColoringData
{

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        // Load in the low res model
        SmallBodyModel modelLow = new Eros(ModelFactory.getModelConfig(ModelFactory.EROS, ModelFactory.GASKELL));
        //SmallBodyModel modelLow = new Itokawa();
        modelLow.setModelResolution(0);

        // Load in the high res model
        SmallBodyModel modelHigh = new Eros(ModelFactory.getModelConfig(ModelFactory.EROS, ModelFactory.GASKELL));
        //SmallBodyModel modelHigh = new Itokawa();
        vtkPolyData polydata = modelHigh.getSmallBodyPolyData();
        for (int resLevel = 1; resLevel < 4; ++resLevel)
        {
            modelHigh.setModelResolution(resLevel);

            int numColors = modelLow.getNumberOfColors();

            DecimalFormat df = new DecimalFormat("0.######E00");

            for (int i=0; i<numColors; ++i)
            {
                String name = "Eros_" + modelLow.getColoringName(i).replace(" ", "") + "_res" + resLevel + ".txt";
                //String name = "Itokawa_" + modelLow.getColoringName(i).replace(" ", "") + "_res" + resLevel + ".txt";
                FileWriter fw = new FileWriter(name);
                BufferedWriter out = new BufferedWriter(fw);

                int numPolygons = polydata.GetNumberOfCells();
                for (int j=0; j<numPolygons; ++j)
                {
                    if (j % 10000 == 0)
                        System.out.println("progress " + resLevel + " " + i + " " + j);

                    vtkTriangle cell = (vtkTriangle) polydata.GetCell(j);
                    vtkPoints points = cell.GetPoints();
                    double[] pt0 = points.GetPoint(0);
                    double[] pt1 = points.GetPoint(1);
                    double[] pt2 = points.GetPoint(2);
                    double[] polygonCenter = new double[3];
                    cell.TriangleCenter(pt0, pt1, pt2, polygonCenter);
                    //System.out.println("A " + pt0[0] + " " + pt0[1] + " " + pt0[2]);
                    //System.out.println("B " + pt1[0] + " " + pt1[1] + " " + pt1[2]);
                    //System.out.println("C " + pt2[0] + " " + pt2[1] + " " + pt2[2]);
                    //System.out.println("D " + polygonCenter[0] + " " + polygonCenter[1] + " " + polygonCenter[2]);

                    double value = modelLow.getColoringValue(i, polygonCenter);
                    out.write(df.format(value) + "\n");
                }

                out.close();
            }
        }
    }

}
