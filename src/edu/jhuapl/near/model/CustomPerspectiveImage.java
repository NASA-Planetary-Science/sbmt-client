package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;

import edu.jhuapl.near.util.ImageDataUtil;
import edu.jhuapl.near.util.MapUtil;

public class CustomPerspectiveImage extends PerspectiveImage
{
    public static final String SUMFILENAMES = "SumfileNames";
    public static final String X_FOV = "XFov";
    public static final String Y_FOV = "YFov";

    private static final double FOV_PARAMETER3 = 1.0;

    private String imageName;

    public CustomPerspectiveImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly, File rootFolder) throws FitsException,
            IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);

        loadImageInfoFromConfigFile();
    }

    private void loadImageInfoFromConfigFile()
    {
        // Look in the config file and figure out which index this image
        // corresponds to. The config file is located in the same folder
        // as the image file
        String configFilename = new File(getKey().name).getParent() + File.separator + "config.txt";
        MapUtil configMap = new MapUtil(configFilename);
        String[] imageFilenames = configMap.getAsArray(IMAGE_FILENAMES);
        for (int i=0; i<imageFilenames.length; ++i)
        {
            String filename = new File(getKey().name).getName();
            if (filename.equals(imageFilenames[i]))
            {
                imageName = configMap.getAsArray(Image.IMAGE_NAMES)[i];
                //sumfilefullpath = new File(getKey().name).getParent() + File.separator + configMap.getAsArray(SUMFILENAMES)[i];
                //double xfov = configMap.getAsDoubleArray(X_FOV)[i] * Math.PI / 180.0;
                //double yfov = configMap.getAsDoubleArray(Y_FOV)[i] * Math.PI / 180.0;
                //fov_parameter1 = -Math.tan(xfov/2.0);
                //fov_parameter2 = -Math.tan(yfov/2.0);
                break;
            }
        }
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageDataUtil.flipImageYAxis(rawImage);
    }

    @Override
    public double getFovParameter1()
    {
        // TODO this is bad in that we read from the config file 3 times in this class

        // Look in the config file and figure out which index this image
        // corresponds to. The config file is located in the same folder
        // as the image file
        String configFilename = new File(getKey().name).getParent() + File.separator + "config.txt";
        MapUtil configMap = new MapUtil(configFilename);
        String[] imageFilenames = configMap.getAsArray(IMAGE_FILENAMES);
        for (int i=0; i<imageFilenames.length; ++i)
        {
            String filename = new File(getKey().name).getName();
            if (filename.equals(imageFilenames[i]))
            {
                double xfov = configMap.getAsDoubleArray(X_FOV)[i] * Math.PI / 180.0;
                return -Math.tan(xfov/2.0);
            }
        }

        return 0.0;
    }

    @Override
    public double getFovParameter2()
    {
        // TODO this is bad in that we read from the config file 3 times in this class

        // Look in the config file and figure out which index this image
        // corresponds to. The config file is located in the same folder
        // as the image file
        String configFilename = new File(getKey().name).getParent() + File.separator + "config.txt";
        MapUtil configMap = new MapUtil(configFilename);
        String[] imageFilenames = configMap.getAsArray(IMAGE_FILENAMES);
        for (int i=0; i<imageFilenames.length; ++i)
        {
            String filename = new File(getKey().name).getName();
            if (filename.equals(imageFilenames[i]))
            {
                double yfov = configMap.getAsDoubleArray(Y_FOV)[i] * Math.PI / 180.0;
                return -Math.tan(yfov/2.0);
            }
        }

        return 0.0;
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
    public String generateBackplanesLabel() throws IOException
    {
        return "";
    }

    @Override
    protected String initializeFitFileFullPath(File rootFolder)
    {
        return getKey().name;
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
        // TODO this is bad in that we read from the config file 3 times in this class

        // Look in the config file and figure out which index this image
        // corresponds to. The config file is located in the same folder
        // as the image file
        String configFilename = new File(getKey().name).getParent() + File.separator + "config.txt";
        MapUtil configMap = new MapUtil(configFilename);
        String[] imageFilenames = configMap.getAsArray(IMAGE_FILENAMES);
        for (int i=0; i<imageFilenames.length; ++i)
        {
            String filename = new File(getKey().name).getName();
            if (filename.equals(imageFilenames[i]))
            {
                return new File(getKey().name).getParent() + File.separator + configMap.getAsArray(SUMFILENAMES)[i];
            }
        }

        return null;
    }

    @Override
    public String getImageName()
    {
        return imageName;
    }
}
