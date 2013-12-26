package edu.jhuapl.near.model.vesta;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.ImageDataUtil;

public class FcImage extends PerspectiveImage
{
    public FcImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageDataUtil.flipImageYAxis(rawImage);
    }

    public int getCamera()
    {
        ImageKey key = getKey();
        String cameraId = new File(key.name).getName().substring(2, 3);
        return Integer.parseInt(cameraId);
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected String initializeFitFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(key.name + ".FIT").getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + key.name + ".FIT";
        }
    }

    @Override
    protected String initializeLabelFileFullPath(File rootFolder)
    {
        return null;
    }

    @Override
    protected String initializeInfoFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/infofiles/"
        + keyFile.getName() + ".INFO";
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + sumFilename;
        }
    }

    @Override
    protected String initializeSumfileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/sumfiles/"
        + keyFile.getName().substring(0, 12).replace('B', 'A') + ".SUM";
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + sumFilename;
        }
    }

    @Override
    public int getFilter()
    {
        ImageKey key = getKey();
        String filterId = new File(key.name).getName().substring(25, 26);
        return Integer.parseInt(filterId);
    }

    @Override
    public String getCameraName()
    {
        return "FC" + getCamera();
    }
}
