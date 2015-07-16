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
    public static final String INFOFILENAMES = "InfofileNames";

    private String imageName;

    public CustomPerspectiveImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException,
            IOException
    {
        super(key, smallBodyModel, loadPointingOnly);

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
                break;
            }
        }
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along y axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageKey key = getKey();
        if (key.source == ImageSource.LOCAL_CYLINDRICAL)
        {
            ImageDataUtil.flipImageXAxis(rawImage);
        }
        else if (key.source == ImageSource.LOCAL_PERSPECTIVE)
        {
            // only rotate INFO files, not SUM files
            if (key.fileType == FileType.INFO)
            {
                ImageDataUtil.rotateImage(rawImage, 270.0);
            }
            else if (key.fileType == FileType.SUM)

            {
                ImageDataUtil.flipImageXAxis(rawImage);
//                ImageDataUtil.rotateImage(rawImage, 180.0);
            }
        }
    }

    @Override
    protected int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected String initializePngFileFullPath()
    {
        return getKey().name.endsWith(".png") ? getKey().name : null;
    }

    @Override
    protected String initializeFitFileFullPath()
    {
        return getKey().name.endsWith(".png") ? null : getKey().name;
    }

    @Override
    protected String initializeLabelFileFullPath()
    {
        return null;
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        String configFilename = new File(getKey().name).getParent() + File.separator + "config.txt";
        MapUtil configMap = new MapUtil(configFilename);
        String[] imageFilenames = configMap.getAsArray(IMAGE_FILENAMES);
        for (int i=0; i<imageFilenames.length; ++i)
        {
            String filename = new File(getKey().name).getName();
            if (filename.equals(imageFilenames[i]))
            {
                return new File(getKey().name).getParent() + File.separator + configMap.getAsArray(INFOFILENAMES)[i];
            }
        }

        return null;
    }

    @Override
    protected String initializeSumfileFullPath()
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
