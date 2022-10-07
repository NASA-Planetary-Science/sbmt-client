package edu.jhuapl.sbmt.image.model.bodies.saturnmoon;

import java.io.File;
import java.io.IOException;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;

import nom.tam.fits.FitsException;

public class SaturnMoonImage extends PerspectiveImage
{
    public SaturnMoonImage(ImageKeyInterface key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    @Override
    public int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected String initializeFitFileFullPath()
    {
        ImageKeyInterface key = getKey();
        return FileCache.getFileFromServer(key.getName() + ".FIT").getAbsolutePath();
    }

    @Override
    protected String initializeLabelFileFullPath()
    {
        return null;
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKeyInterface key = getKey();
        File keyFile = new File(key.getName());
        String sumFilename = keyFile.getParentFile().getParent() + "/infofiles/"
        + keyFile.getName() + ".INFO";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKeyInterface key = getKey();
        File keyFile = new File(key.getName());
        String sumFilename = keyFile.getParentFile().getParent() + "/sumfiles/"
        + keyFile.getName() + ".SUM";
        return FileCache.getFileFromServer(sumFilename).getAbsolutePath();
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
    	// TODO Auto-generated method stub
    	super.processRawImage(rawImage);
    }
}
