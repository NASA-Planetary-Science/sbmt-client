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
            // only rotate INFO files, not SUM files
            if (key.fileType == FileType.INFO)
            {
                // Originally was just this one rotate 270 transformation
                //ImageDataUtil.rotateImage(rawImage, 270.0);

                // Commented out above and put this in instead to make perspective image
                // work with ENVI files
//                ImageDataUtil.flipImageYAxis(rawImage);

                if (getFlip().equals("X"))
                {
                    ImageDataUtil.flipImageXAxis(rawImage);
                }
                else if (getFlip().equals("Y"))
                {
                    ImageDataUtil.flipImageYAxis(rawImage);
                }
                else // no flip
                {
                }

                if (getRotation() != 0.0)
                    ImageDataUtil.rotateImage(rawImage, getRotation());
            }
            else if (key.fileType == FileType.SUM)
            {
//                ImageDataUtil.flipImageXAxis(rawImage);
//                ImageDataUtil.rotateImage(rawImage, 180.0);

                if (getFlip().equals("X"))
                    ImageDataUtil.flipImageXAxis(rawImage);
                else if (getFlip().equals("Y"))
                    ImageDataUtil.flipImageYAxis(rawImage);

                if (getRotation() != 0.0)
                    ImageDataUtil.rotateImage(rawImage, getRotation());
            }
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
