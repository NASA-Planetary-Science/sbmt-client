package edu.jhuapl.near.model.phobos;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;

public class PhobosImage extends PerspectiveImage
{
    public static final double FOV_VIKING_PARAMETER1 = -Math.tan(0.0316815603057/2.0);
    public static final double FOV_VIKING_PARAMETER2 = -Math.tan(0.0277876855117/2.0);
    public static final double FOV_PHOBOS2_FILTER2_PARAMETER1 = -Math.tan(0.091734880135/2.0);
    public static final double FOV_PHOBOS2_FILTER2_PARAMETER2 = -Math.tan(0.0714889658869/2.0);
    public static final double FOV_PHOBOS2_FILTER13_PARAMETER1 = -Math.tan(0.486390796269/2.0);
    public static final double FOV_PHOBOS2_FILTER13_PARAMETER2 = -Math.tan(0.381881868455/2.0);
    public static final double FOV_MEX_HRSC_SRC_PARAMETER1 = -Math.tan(0.0094/2.0);
    public static final double FOV_MEX_HRSC_SRC_PARAMETER2 = -Math.tan(0.0094/2.0);
    public static final double FOV_PARAMETER3 = 1.0;

    public PhobosImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
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
        if (keyFile.getName().startsWith("h"))
            return FOV_MEX_HRSC_SRC_PARAMETER1;
        else if (keyFile.getName().startsWith("V"))
            return FOV_VIKING_PARAMETER1;
        else if (keyFile.getName().endsWith("2"))
            return FOV_PHOBOS2_FILTER2_PARAMETER1;
        else
            return FOV_PHOBOS2_FILTER13_PARAMETER1;
    }

    @Override
    public double getFovParameter2()
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("h"))
            return FOV_MEX_HRSC_SRC_PARAMETER2;
        else if (keyFile.getName().startsWith("V"))
            return FOV_VIKING_PARAMETER2;
        else if (keyFile.getName().endsWith("2"))
            return FOV_PHOBOS2_FILTER2_PARAMETER2;
        else
            return FOV_PHOBOS2_FILTER13_PARAMETER2;
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
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (!keyFile.getName().startsWith("V"))
            return null;
        String labelFilename = keyFile.getParent() + "/f" + keyFile.getName().substring(2, 8).toLowerCase() + ".lbl";
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(labelFilename).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + labelFilename;
        }
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
        + keyFile.getName() + ".SUM";
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
        // For Phobos 2 image, return 1, 2, or 3 which we can get by looking at the last number in the filename.
        // For viking images, we need to parse the label file to get the filter.
        // for MEX images, return -1
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        if (keyFile.getName().startsWith("h"))
        {
            return -1;
        }
        else if (keyFile.getName().startsWith("P"))
        {
            return Integer.parseInt(keyFile.getName().substring(7, 8));
        }
        else
        {
            try
            {
                String filterLine = FileUtil.getFirstLineStartingWith(getLabelFileFullPath(), "FILTER_NAME");
                String[] words = filterLine.trim().split("\\s+");
                return getVikingFilterNumberFromName(words[2]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return -1;
        }
    }

    private int getVikingFilterNumberFromName(String name)
    {
        int num = -1;
        if (name.equals("BLUE"))
            num = 4;
        if (name.equals("MINUS_BLUE"))
            num = 5;
        else if (name.equals("VIOLET"))
            num = 6;
        else if (name.equals("CLEAR"))
            num = 7;
        else if (name.equals("GREEN"))
            num = 8;
        else if (name.equals("RED"))
            num = 9;

        return num;
    }

    private String getFilterNameFromNumber(int num)
    {
        String name = null;
        if (num == 1)
            name = "Channel 1";
        else if (num == 2)
            name = "Channel 2";
        else if (num == 3)
            name = "Channel 3";
        else if (num == 4)
            name = "BLUE";
        else if (num == 5)
            name = "MINUS_BLUE";
        else if (num == 6)
            name = "VIOLET";
        else if (num == 7)
            name = "CLEAR";
        else if (num == 8)
            name = "GREEN";
        else if (num == 9)
            name = "RED";

        return name;
    }

    private String getCameraNameFromNumber(int num)
    {
        String name = null;
        if (num == 1)
            name = "Phobos 2, VSK";
        else if (num == 2)
            name = "Viking Orbiter 1, Camera A";
        else if (num == 3)
            name = "Viking Orbiter 1, Camera B";
        else if (num == 4)
            name = "Viking Orbiter 2, Camera A";
        else if (num == 5)
            name = "Viking Orbiter 2, Camera B";
        else if (num == 6)
            name = "Mars Express, HRSC";

        return name;
    }

    @Override
    public String getFilterName()
    {
        return getFilterNameFromNumber(getFilter());
    }

    @Override
    public String getCameraName()
    {
        return getCameraNameFromNumber(getCamera());
    }

    @Override
    public int getCamera()
    {
        // Return the following:
        // 1 for phobos 2 images
        // 2 for viking orbiter 1 images camera A
        // 3 for viking orbiter 1 images camera B
        // 4 for viking orbiter 2 images camera A
        // 5 for viking orbiter 2 images camera B
        // 6 for MEX HRSC camera
        // We need to parse the label file to get which viking spacecraft

        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String name = keyFile.getName();
        if (name.startsWith("h"))
        {
            return 6;
        }
        else if (name.startsWith("P"))
        {
            return 1;
        }
        else
        {
            try
            {
                String filterLine = FileUtil.getFirstLineStartingWith(getLabelFileFullPath(), "SPACECRAFT_NAME");
                String[] words = filterLine.trim().split("\\s+");
                if (words[2].equals("VIKING_ORBITER_1"))
                {
                    if (name.contains("A"))
                        return 2;
                    else
                        return 3;
                }
                else
                {
                    if (name.contains("A"))
                        return 4;
                    else
                        return 5;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return -1;
        }
    }
}
