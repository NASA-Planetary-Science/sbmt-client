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
    float[][] ImageDataTo2DArray(vtkImageData image)
    {
        int[] dims = image.GetDimensions();
        int width = dims[0];
        int height = dims[1];
        vtkPointData pointdata = image.GetPointData();
        vtkFloatArray data = (vtkFloatArray)pointdata.GetScalars();
        float[][] array = new float[height][width];
        int count = 0;
        for (int i=0; i < height; ++i)
            for (int j=0; j < width; ++j)
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
    float interpolateInImage(float[][] image, int width, int height, double u, double v)
    {
        return 0.0f;
    }
}
