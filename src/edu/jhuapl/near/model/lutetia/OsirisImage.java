package edu.jhuapl.near.model.lutetia;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.ImageDataUtil;

public class OsirisImage extends PerspectiveImage
{
    // These values are slightly different from those in the instrument kernel.
    // They seem to work okay. Note the tangent!
    public static final double FOV_NAC_PARAMETER1 = -Math.tan(0.0385386967361/2.0);
    public static final double FOV_NAC_PARAMETER2 = -Math.tan(0.0385386967361/2.0);
    public static final double FOV_NAC_PARAMETER3 = 1.0;
    public static final double FOV_WAC_PARAMETER1 = -Math.tan(0.207/2.0);
    public static final double FOV_WAC_PARAMETER2 = -Math.tan(0.207/2.0);
    public static final double FOV_WAC_PARAMETER3 = 1.0;

    public OsirisImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis an rotate it. Only needed for NAC images.
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
        {
            ImageDataUtil.flipImageYAxis(rawImage);
            ImageDataUtil.rotateImage(rawImage, 180.0);
        }
    }

    public String generateBackplanesLabel() throws IOException
    {
        return "";
    }

    @Override
    public double getFovParameter1()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
            return FOV_NAC_PARAMETER1;
        else
            return FOV_WAC_PARAMETER1;
    }

    @Override
    public double getFovParameter2()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
            return FOV_NAC_PARAMETER2;
        else
            return FOV_WAC_PARAMETER2;
    }

    @Override
    public double getFovParameter3()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
            return FOV_NAC_PARAMETER3;
        else
            return FOV_WAC_PARAMETER3;
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
        + keyFile.getName() + ".SUM";
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
        return 1; // TODO fix this
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
