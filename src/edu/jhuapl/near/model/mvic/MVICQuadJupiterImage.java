package edu.jhuapl.near.model.mvic;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.ImageDataUtil;

public class MVICQuadJupiterImage extends PerspectiveImage
{
    public static final int INITIAL_BAND = 3;

    public MVICQuadJupiterImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException,
            IOException
    {
        super(key, smallBodyModel, loadPointingOnly, INITIAL_BAND);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis and y axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageDataUtil.flipImageYAxis(rawImage);
        ImageDataUtil.flipImageXAxis(rawImage);
    }

    protected int getNumberBands()
    {
        return 4;
    }
    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    public String[] getFitFilesFullPath()
    {
        String path = getFitFileFullPath();

        String[] pathArray = path.split("/");
        int size = pathArray.length;
        String fileNameSuffix = pathArray[size-1].substring(4);
        String resultPath = "/";
        for (int i=0; i<size-1; i++)
            resultPath += pathArray[i] + "/";

        String[] result = new String[4];
        for (int i=0; i<4; i++)
        {
            String fileName = "mc" + i + "_" + fileNameSuffix;
            result[i] = resultPath + fileName;
        }
        return result;
    }

    public String[] getInfoFilesFullPath()
    {
        String path = getInfoFileFullPath();

        String[] pathArray = path.split("/");
        int size = pathArray.length;
        String fileNameSuffix = pathArray[size-1].substring(4);
        String resultPath = "/";
        for (int i=0; i<size-1; i++)
            resultPath += pathArray[i] + "/";

        String[] result = new String[4];
        for (int i=0; i<4; i++)
        {
            String fileName = "mc" + i + "_" + fileNameSuffix;
            result[i] = resultPath + fileName;
        }
        return result;
    }

    protected double getFocalLength() { return 657.5; }    // in mm

    protected double getPixelWidth() { return 0.013; }    // in mm

    protected double getPixelHeight() { return 0.013; }   // in mm

    @Override
    protected String initializeFitFileFullPath()
    {
        ImageKey key = getKey();
        int band = getCurrentSlice();
        String path = key.name;
        String[] pathArray = path.split("/");
        int size = pathArray.length;
        String fileName = "mc" + band + "_" + pathArray[size-1];
        String resultPath = "/";
        for (int i=0; i<size-1; i++)
            resultPath += pathArray[i] + "/";

        return FileCache.getFileFromServer(resultPath + fileName + ".fit").getAbsolutePath();
    }

    @Override
    protected String initializeLabelFileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/labelfiles/"
        + keyFile.getName().split("\\.")[0] + ".lbl";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKey key = getKey();
        int band = getCurrentSlice();
        String path = key.name;
        String[] pathArray = path.split("/");
        int size = pathArray.length;
        String fileName = "mc" + band + "_" + pathArray[size-1];
        String resultPath = "/";
        for (int i=0; i<size-2; i++)
            resultPath += pathArray[i] + "/";

        return FileCache.getFileFromServer(resultPath + "infofiles/" + fileName + ".INFO").getAbsolutePath();
    }

    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/sumfiles/"
        + keyFile.getName().split("\\.")[0] + ".SUM";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    protected vtkImageData createRawImage(int height, int width, int depth, float[][] array2D, float[][][] array3D)
    {
        return createRawImage(height, width, depth, false, array2D, array3D);
    }
}
