package edu.jhuapl.near.model.eros;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;
import vtk.vtkImageReslice;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class MSIImage extends PerspectiveImage
{
    // Size of image after resampling. Before resampling image is 537 by 244 pixels.
    public static final int RESAMPLED_IMAGE_WIDTH = 537;
    public static final int RESAMPLED_IMAGE_HEIGHT = 412;

    // Number of pixels on each side of the image that are
    // masked out (invalid) due to filtering.
    public static final int LEFT_MASK = 14;
    public static final int RIGHT_MASK = 14;
    public static final int TOP_MASK = 2;
    public static final int BOTTOM_MASK = 2;

    public MSIImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        int[] dims = rawImage.GetDimensions();
        int originalHeight = dims[1];

        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(rawImage);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)RESAMPLED_IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, RESAMPLED_IMAGE_WIDTH-1, 0, RESAMPLED_IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();

        vtkImageData resliceOutput = reslice.GetOutput();
        rawImage.DeepCopy(resliceOutput);
        rawImage.SetSpacing(1, 1, 1);
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{TOP_MASK, RIGHT_MASK, BOTTOM_MASK, LEFT_MASK};
    }

    @Override
    protected String initializeFitFileFullPath()
    {
        ImageKey key = getKey();
        return FileCache.getFileFromServer(key.name + ".FIT").getAbsolutePath();
    }

    @Override
    protected String initializeLabelFileFullPath()
    {
        ImageKey key = getKey();
        String imgLblFilename = key.name + ".LBL";
        return FileCache.getFileFromServer(imgLblFilename).getAbsolutePath();
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String infoFilename = keyFile.getParentFile().getParent()
        + "/infofiles/" + keyFile.getName() + ".INFO";
        return FileCache.getFileFromServer(infoFilename).getAbsolutePath();
    }

    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent()
        + "/sumfiles/" + keyFile.getName().substring(0, 11) + ".SUM";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    public int getFilter()
    {
        String fitName = new File(getFitFileFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
    }

    /**
     * Note although there is only 1 MSI camera, we are abusing the following function
     * to return 1 if image is IOF or 2 if image is CIF.
     */
    @Override
    public int getCamera()
    {
        String fitName = new File(getFitFileFullPath()).getName();
        if (fitName.toUpperCase().contains("_IOF_"))
            return 1;
        else // CIF
            return 2;
    }

    public String getCameraName()
    {
        return "MSI";
    }
}
