package edu.jhuapl.near.model.custom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkFloatArray;
import vtk.vtkImageConstantPad;
import vtk.vtkImageData;
import vtk.vtkImageTranslateExtent;
import vtk.vtkPoints;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;

public class CustomShapeModel extends SmallBodyModel
{
    private static final int PADDING_SIZE = 2;

    private int imageWidth;
    private int imageHeight;

    public CustomShapeModel(String name)
    {
        super(
                new String[] { name },
                new String[] { getModelPath(name) },
                null,
                null,
                null,
                null,
                false,
                getImagePath(name),
                ColoringValueType.CELLDATA,
                false);
    }

    private static String getModelPath(String name)
    {
        return FileCache.FILE_PREFIX +
                Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator +
                "model.vtk";
    }

    private static String getImagePath(String name)
    {
        String path = Configuration.getImportedShapeModelsDir() +
                File.separator +
                name +
                File.separator +
                "image.png";

        if (!(new File(path)).exists())
            return null;
        else
            return FileCache.FILE_PREFIX + path;
    }

    private double[] loadTextureCorners()
    {
        try
        {
            // Load in the corners.txt file
            String cornersFilename = Configuration.getImportedShapeModelsDir() +
                    File.separator +
                    getModelName() +
                    File.separator +
                    "corners.txt";

            ArrayList<String> words = FileUtil.getFileWordsAsStringList(cornersFilename);
            double lllat = Double.parseDouble(words.get(0));
            double lllon = Double.parseDouble(words.get(1));
            double urlat = Double.parseDouble(words.get(2));
            double urlon = Double.parseDouble(words.get(3));

            return new double[]{lllat, lllon, urlat, urlon};
        }
        catch (IOException ex)
        {

        }

        return null;
    }

    @Override
    protected vtkImageData loadImageMap(String name)
    {
        vtkImageData image = super.loadImageMap(name);

        double[] corners = loadTextureCorners();
        double lllat = corners[0];
        double lllon = corners[1];
        double urlat = corners[2];
        double urlon = corners[3];
        if (lllat == -90 && lllon == 0.0 && urlat == 90.0 && urlon == 360.0)
            return image;

        // pad the image with 2 pixels on either side if the image does not cover the entire body

        int[] dims = image.GetDimensions();
        imageWidth = dims[0];
        imageHeight = dims[1];

        int[] extent = image.GetWholeExtent();
        System.out.println(image);

        vtkImageConstantPad padFilter = new vtkImageConstantPad();
        padFilter.SetInput(image);
        padFilter.SetOutputWholeExtent(extent[0]-2, extent[1]+2,
                extent[2]-2, extent[3]+2,
                extent[4], extent[5]);
        padFilter.SetOutputNumberOfScalarComponents(image.GetNumberOfScalarComponents());
        padFilter.SetConstant(255.0);
        padFilter.Update();
        System.out.println(padFilter.GetOutput());

        vtkImageTranslateExtent translateFilter = new vtkImageTranslateExtent();
        translateFilter.SetInputConnection(padFilter.GetOutputPort());
        translateFilter.SetTranslation(2, 2, 0);
        translateFilter.Update();

        vtkImageData output = translateFilter.GetOutput();
        output.SetOrigin(0.0, 0.0, 0.0);

        return output;
    }

    @Override
    protected void generateTextureCoordinates()
    {
        double[] corners = loadTextureCorners();
        double lllat = corners[0];
        double lllon = corners[1];
        double urlat = corners[2];
        double urlon = corners[3];

        if (lllat == -90 && lllon == 0.0 && urlat == 90.0 && urlon == 360.0)
        {
            super.generateTextureCoordinates();
            return;
        }

        lllat *= (Math.PI / 180.0);
        lllon *= (Math.PI / 180.0);
        urlat *= (Math.PI / 180.0);
        urlon *= (Math.PI / 180.0);

        vtkFloatArray textureCoords = new vtkFloatArray();
        vtkPolyData smallBodyPolyData = getSmallBodyPolyData();

        int numberOfPoints = smallBodyPolyData.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = smallBodyPolyData.GetPoints();

        double xsize = urlon - lllon;
        double ysize = urlat - lllat;

        double umin = PADDING_SIZE / (2.0 * PADDING_SIZE + imageWidth - 1.0);
        double umax = 1.0 - umin;
        double vmin = PADDING_SIZE / (2.0 * PADDING_SIZE + imageHeight - 1.0);
        double vmax = 1.0 - vmin;
        double usize = umax - umin;
        double vsize = vmax - vmin;

        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

            if (ll.lon < 0.0)
               ll.lon += (2.0 * Math.PI);

            double u = 0.0;
            double v = 0.0;

            if (ll.lat >= lllat-0.04 && ll.lat <= urlat+0.04 && ll.lon >= lllon-0.04 && ll.lon <= urlon+0.04)
            {
                System.out.println(ll.lat * 180.0 / Math.PI);
                u = (ll.lon - lllon) / xsize;
                v = (ll.lat - lllat) / ysize;

                u = umin + u * usize;
                v = vmin + v * vsize;

                if (u < 0.0) u = 0.0;
                else if (u > 1.0) u = 1.0;
                if (v < 0.0) v = 0.0;
                else if (v > 1.0) v = 1.0;
            }

            textureCoords.SetTuple2(i, u, v);
        }

        smallBodyPolyData.GetPointData().SetTCoords(textureCoords);
    }
}
