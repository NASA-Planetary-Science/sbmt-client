package edu.jhuapl.near.util;

import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPointData;

public class ImageDataUtil
{
    /**
     * Accessing individual pixels of a vtkImageData is slow in java.
     * Therefore this function was written to allow converting a vtkImageData
     * to a java 2d array.
     * @param image
     * @return
     */
    static public float[][] vtkImageDataToArray2D(vtkImageData image)
    {
        int[] dims = image.GetDimensions();
        int height = dims[0];
        int width = dims[1];
        vtkPointData pointdata = image.GetPointData();
        vtkFloatArray data = (vtkFloatArray)pointdata.GetScalars();
        float[][] array = new float[height][width];
        int count = 0;
        for (int j=0; j < width; ++j)
            for (int i=0; i < height; ++i)
            {
                array[i][j] = (float)data.GetValue(count++);
            }

        return array;
    }

    /**
     * Given a 2D image of given length and width as well as texture coordinates
     * in the image (i.e. between 0 and 1) interpolate within the image to find
     * the pixel value at the texture coordinates using linear interpolation.
     * @param image
     * @param width
     * @param height
     * @param u
     * @param v
     * @return
     */
    static public float interpolateWithinImage(float[][] image, int width, int height, double u, double v)
    {
        final double x = u * (width - 1.0);
        final double y = v * (height - 1.0);
        final int x1 = (int)Math.floor(x);
        final int x2 = (int)Math.ceil(x);
        final int y1 = (int)Math.floor(y);
        final int y2 = (int)Math.ceil(y);

        // From http://en.wikipedia.org/wiki/Bilinear_interpolation
        final double value =
            image[x1][y1]*(x2-x)*(y2-y) +
            image[x2][y1]*(x-x1)*(y2-y) +
            image[x1][y2]*(x2-x)*(y-y1) +
            image[x2][y2]*(x-x1)*(y-y1);

        return (float)value;
    }
}
