package edu.jhuapl.near.model.ida;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;
import vtk.vtkImageFlip;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class SSIIdaImage extends PerspectiveImage
{
    // Note the tangent!
    public static final double FOV_PARAMETER1 = -Math.tan(0.00813/2.0);
    public static final double FOV_PARAMETER2 = -Math.tan(0.00813/2.0);
    public static final double FOV_PARAMETER3 = 1.0;

    public SSIIdaImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly, File rootFolder) throws FitsException,
            IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis. For some reason we need to do
        // this so the image is displayed properly.
        int[] dims = rawImage.GetDimensions();
        vtkImageFlip flip = new vtkImageFlip();
        flip.SetInput(rawImage);
        flip.SetInterpolationModeToNearestNeighbor();
        flip.SetOutputSpacing(1.0, 1.0, 1.0);
        flip.SetOutputOrigin(0.0, 0.0, 0.0);
        flip.SetOutputExtent(0, dims[1]-1, 0, dims[0]-1, 0, 0);
        flip.FlipAboutOriginOff();
        flip.SetFilteredAxes(1);
        flip.Update();

        vtkImageData flipOutput = flip.GetOutput();
        rawImage.DeepCopy(flipOutput);
    }

    @Override
    public double getFovParameter1()
    {
        return FOV_PARAMETER1;
    }

    @Override
    public double getFovParameter2()
    {
        return FOV_PARAMETER2;
    }

    @Override
    public double getFovParameter3()
    {
        return FOV_PARAMETER3;
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected int getFilter()
    {
        return 0;
    }

    @Override
    public String generateBackplanesLabel() throws IOException
    {
        return "";
    }

    @Override
    public LinkedHashMap<String, String> getProperties() throws IOException
    {
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
        return properties;
    }

    @Override
    protected String initializeFitFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(key.name + ".fit", false).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + key.name + ".fit";
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
        return null;
    }

    @Override
    protected String initializeSumfileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent() + "/sumfiles/"
        + keyFile.getName().substring(0, 5) + ".SUM";
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(sumFilename, false).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + sumFilename;
        }
    }

}
