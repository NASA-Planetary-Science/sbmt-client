package edu.jhuapl.near.model.eros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;

import vtk.vtkImageData;
import vtk.vtkImageReslice;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class MSIImage extends Image
{
    // Size of image after resampling. Before resampling image is 537 by 244 pixels.
    public static final int RESAMPLED_IMAGE_WIDTH = 537;
    public static final int RESAMPLED_IMAGE_HEIGHT = 412;

    // Values from MSI instrument kernel file.
    public static final double FOV_PARAMETER1 = -0.025753661240;
    public static final double FOV_PARAMETER2 = -0.019744857140;
    public static final double FOV_PARAMETER3 = 1.0;

    // Number of pixels on each side of the image that are
    // masked out (invalid) due to filtering.
    public static final int LEFT_MASK = 14;
    public static final int RIGHT_MASK = 14;
    public static final int TOP_MASK = 2;
    public static final int BOTTOM_MASK = 2;

    //public static final String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    //public static final String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";

    private SmallBodyModel erosModel;

    public MSIImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
        this.erosModel = smallBodyModel;
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        int[] dims = rawImage.GetDimensions();
        int originalHeight = dims[1];

        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(rawImage);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)RESAMPLED_IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, RESAMPLED_IMAGE_WIDTH-1, 0, RESAMPLED_IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();

        vtkImageData resliceOutput = reslice.GetOutput();
        rawImage.DeepCopy(resliceOutput);
        rawImage.SetSpacing(1, 1, 1);
    }

    public String generateBackplanesLabel() throws IOException
    {
        String lblFilename = getLabelFileFullPath();

        FileInputStream fs = null;
        try {
            fs = new FileInputStream(lblFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        StringBuffer strbuf = new StringBuffer("");

        String str;
        while ((str = in.readLine()) != null)
        {
            // The software name and version in the downloaded ddr is not correct
            if (str.trim().startsWith("SOFTWARE_NAME"))
            {
                strbuf.append("SOFTWARE_NAME                = \"Small Body Mapping Tool\"\r\n");
                continue;
            }

            if (str.trim().startsWith("SOFTWARE_VERSION_ID"))
            {
                strbuf.append("SOFTWARE_VERSION_ID          = \"2.0\"\r\n");
                continue;
            }

            // The planes in the downloaded ddr are all wrong
            if (str.trim().startsWith("BANDS"))
            {
                strbuf.append("    BANDS                    = 16\r\n");
                strbuf.append("    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL\r\n");
                strbuf.append("    BAND_NAME                = (\"MSI pixel value\",\r\n");
                strbuf.append("                                \"x coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"y coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"z coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
                strbuf.append("                                \"Latitude, deg\",\r\n");
                strbuf.append("                                \"Longitude, deg\",\r\n");
                strbuf.append("                                \"Distance from center of body, km\",\r\n");
                strbuf.append("                                \"Incidence angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Emission angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Phase angle, measured against the plate model, deg\",\r\n");
                strbuf.append("                                \"Horizontal pixel scale, km per pixel\",\r\n");
                strbuf.append("                                \"Vertical pixel scale, km per pixel\",\r\n");
                strbuf.append("                                \"Slope, deg\",\r\n");
                strbuf.append("                                \"Elevation, m\",\r\n");
                strbuf.append("                                \"Gravitational acceleration, m/s^2\",\r\n");
                strbuf.append("                                \"Gravitational potential, J/kg\")\r\n");
                strbuf.append("\r\n");
                strbuf.append("  END_OBJECT                 = IMAGE\r\n");
                strbuf.append("\r\n");
                strbuf.append("END_OBJECT                   = FILE\r\n");
                strbuf.append("\r\n");
                strbuf.append("END\r\n");

                break;
            }

            strbuf.append(str).append("\r\n");

            // Add the shape model used for generating the ddr right aftet the sun position
            // since this is not in the label file on the server
            if (str.trim().startsWith("SUN_POSITION_LT"))
                strbuf.append("\r\nSHAPE_MODEL = \"" + erosModel.getModelName() + "\"\r\n");
        }

        return strbuf.toString();

        /*
         TODO consider generating the entire lbl file on the fly

        StringBuffer str = new StringBuffer("");

        String nl = System.getProperty("line.separator");

        str.append("PDS_VERSION_ID               = PDS3").append(nl);
        str.append("").append(nl);
        str.append("/* DDR Identification *//*").append(nl);
        str.append("").append(nl);
        str.append("INSTRUMENT_HOST_NAME         = \"NEAR EARTH ASTEROID RENDEZVOUS\"").append(nl);
        str.append("SPACECRAFT_ID                = NEAR").append(nl);
        str.append("INSTRUMENT_NAME              = \"MULTI-SPECTRAL IMAGER\"").append(nl);
        str.append("INSTRUMENT_ID                = MSI").append(nl);
        str.append("TARGET_NAME                  = EROS").append(nl);
        str.append("PRODUCT_TYPE                 = DDR").append(nl);
        str.append("PRODUCT_CREATION_TIME        = ").append(nl);
        str.append("START_TIME                   = ").append(nl);
        str.append("STOP_TIME                    = ").append(nl);
        str.append("SPACECRAFT_CLOCK_START_COUNT = ").append(nl);
        str.append("SPACECRAFT_CLOCK_STOP_COUNT  = ").append(nl);
        str.append("").append(nl);

        return str.toString();
        */
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
        return new int[]{TOP_MASK, RIGHT_MASK, BOTTOM_MASK, LEFT_MASK};
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
        String imgLblFilename = key.name + "_DDR.LBL";
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(imgLblFilename).getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + imgLblFilename;
        }
    }

    @Override
    protected String initializeInfoFileFullPath(File rootFolder)
    {
        return initializeLabelFileFullPath(rootFolder);
    }

    @Override
    protected String initializeSumfileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParentFile().getParentFile().getParent()
        + "/sumfiles/" + keyFile.getName().substring(0, 11) + ".SUM";
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
        String fitName = new File(getFitFileFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
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

        String fullpath = getFitFileFullPath();
        properties.put("Name", new File(getFitFileFullPath()).getName()); //TODO remove extension and possibly prefix
        properties.put("Start Time", getStartTime());
        properties.put("Stop Time", getStopTime());
        properties.put("Filter", String.valueOf(getFilter()));
        properties.put("Day of Year", (new File(fullpath)).getParentFile().getParentFile().getName());
        properties.put("Year", (new File(fullpath)).getParentFile().getParentFile().getParentFile().getName());
        properties.put("Deblur Type", (new File(fullpath)).getParentFile().getName());
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
