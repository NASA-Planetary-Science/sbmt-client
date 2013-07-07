package edu.jhuapl.near.model.saturnmoon;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class SaturnMoonImage extends PerspectiveImage
{
    // These values are derived from the sumfiles.
    public static final double FOV_NAC_PARAMETER1 = -Math.tan(0.006129698948356052/2.0);
    public static final double FOV_WAC_PARAMETER1 = -Math.tan(0.06112372254906546/2.0);
    public static final double FOV_VOY_PARAMETER1 = -Math.tan(0.007603911064821517/2.0);
    public static final double FOV_NAC_PARAMETER2 = -Math.tan(0.006129000246423659/2.0);
    public static final double FOV_WAC_PARAMETER2 = -Math.tan(0.061118005462794944/2.0);
    public static final double FOV_VOY_PARAMETER2 = -Math.tan(0.007603911064821517/2.0);
    public static final double FOV_PARAMETER3 = 1.0;

    public SaturnMoonImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    private boolean getIfUseAPLServer()
    {
        ImageKey key = getKey();

        boolean useAPLServer = true;

        if (key.name.toLowerCase().contains("phoebe") || key.name.toLowerCase().contains("mimas"))
            useAPLServer = false;

        return useAPLServer;
    }

    @Override
    public double getFovParameter1()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
            return FOV_NAC_PARAMETER1;
        else if (keyFile.getName().startsWith("W"))
            return FOV_WAC_PARAMETER1;
        else
            return FOV_VOY_PARAMETER1;
    }

    @Override
    public double getFovParameter2()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("N"))
            return FOV_NAC_PARAMETER2;
        else if (keyFile.getName().startsWith("W"))
            return FOV_WAC_PARAMETER2;
        else
            return FOV_VOY_PARAMETER2;
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
            boolean useAPLServer = getIfUseAPLServer();
            return FileCache.getFileFromServer(key.name + ".FIT", useAPLServer).getAbsolutePath();
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
            boolean useAPLServer = getIfUseAPLServer();
            return FileCache.getFileFromServer(sumFilename, useAPLServer).getAbsolutePath();
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
            boolean useAPLServer = getIfUseAPLServer();
            return FileCache.getFileFromServer(sumFilename, useAPLServer).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + sumFilename;
        }
    }
}
