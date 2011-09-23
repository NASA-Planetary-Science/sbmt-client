package edu.jhuapl.near.model.vesta;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;
import vtk.vtkImageFlip;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class FcImage extends Image
{
    // Values derived from FC instrument kernel file. Note the tangent!
    public static final double FOV_PARAMETER1 = -Math.tan(0.095480/2.0);
    public static final double FOV_PARAMETER2 = -Math.tan(0.095420/2.0);
    public static final double FOV_PARAMETER3 = 1.0;

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

    public int getCamera()
    {
        ImageKey key = getKey();
        String cameraId = new File(key.name).getName().substring(2, 3);
        return Integer.parseInt(cameraId);
    }

    public String generateBackplanesLabel() throws IOException
    {
        return "";
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
    protected String initializeFitFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(key.name + ".FIT", true).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + key.name + ".FIT";
        }
    }

    @Override
    protected String initializeLabelFileFullPath(File rootFolder)
    {
        return initializeInfoFileFullPath(rootFolder);
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
            return FileCache.getFileFromServer(sumFilename, true).getAbsolutePath();
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
            return FileCache.getFileFromServer(sumFilename, true).getAbsolutePath();
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
    public LinkedHashMap<String, String> getProperties() throws IOException
    {
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();

        if (getMaxPhase() < getMinPhase())
        {
            this.computeIlluminationAngles();
            this.computePixelScale();
        }

        DecimalFormat df = new DecimalFormat("#.######");

        properties.put("Name", new File(getFitFileFullPath()).getName()); //TODO remove extension and possibly prefix
        properties.put("Time", getStartTime());
        properties.put("Spacecraft Distance", df.format(getSpacecraftDistance()) + " km");
        properties.put("Filter", String.valueOf(getFilter()));
        properties.put("Camera", "FC" + String.valueOf(getCamera()));

        // Note \u00B0 is the unicode degree symbol
        String deg = "\u00B0";
        properties.put("Minimum Incidence", df.format(getMinIncidence())+deg);
        properties.put("Maximum Incidence", df.format(getMaxIncidence())+deg);
        properties.put("Minimum Emission", df.format(getMinEmission())+deg);
        properties.put("Maximum Emission", df.format(getMaxIncidence())+deg);
        properties.put("Minimum Phase", df.format(getMinPhase())+deg);
        properties.put("Maximum Phase", df.format(getMaxPhase())+deg);
        properties.put("Minimum Horizontal Pixel Scale", df.format(1000.0*getMinimumHorizontalPixelScale()) + " meters/pixel");
        properties.put("Maximum Horizontal Pixel Scale", df.format(1000.0*getMaximumHorizontalPixelScale()) + " meters/pixel");
        properties.put("Minimum Vertical Pixel Scale", df.format(1000.0*getMinimumVerticalPixelScale()) + " meters/pixel");
        properties.put("Maximum Vertical Pixel Scale", df.format(1000.0*getMaximumVerticalPixelScale()) + " meters/pixel");

        return properties;
    }

}
