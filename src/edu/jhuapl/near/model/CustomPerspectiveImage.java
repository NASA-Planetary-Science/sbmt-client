package edu.jhuapl.near.model;

import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.util.ImageDataUtil;

public class CustomPerspectiveImage extends PerspectiveImage
{
    public CustomPerspectiveImage(ImageKey key, SmallBodyModel smallBodyModel, boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    protected void initialize() throws FitsException, IOException
    {

        super.initialize();

        setUseDefaultFootprint(true);
    }

    @Override
    protected int getNumberBands()
    {
        return imageDepth;
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageKey key = getKey();
        if (key.source == ImageSource.LOCAL_CYLINDRICAL)
        {
            // is this ever called? -turnerj1
            ImageDataUtil.flipImageXAxis(rawImage);
        }
        else if (key.source == ImageSource.LOCAL_PERSPECTIVE)
        {
                if (getFlip().equals("X"))
                {
                    ImageDataUtil.flipImageXAxis(rawImage);
                }
                else if (getFlip().equals("Y"))
                {
                    ImageDataUtil.flipImageYAxis(rawImage);
                }

                if (getRotation() != 0.0)
                    ImageDataUtil.rotateImage(rawImage, 360.0 - getRotation());
        }
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

//    public int getDefaultSlice() { return this.imageDepth > 1 ? 127 : 0; }
//
//    public boolean shiftBands() { return this.imageDepth > 1 ? true : false; }
}
