package edu.jhuapl.near.model.itokawa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.vtkImageConstantPad;
import vtk.vtkImageData;
import vtk.vtkImageTranslateExtent;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class AmicaImage extends Image
{
    //private static final int IMAGE_WIDTH = 1024;
    //private static final int IMAGE_HEIGHT = 1024;

    public static final double FOV_PARAMETER1 = -0.049782949;
    public static final double FOV_PARAMETER2 = -0.049782949;

    public AmicaImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
    }

    @Override
    protected void loadImageInfo(
            String lblFilename,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunVector,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws NumberFormatException, IOException
    {
        // Not used
    }

    @Override
    protected void resizeRawImage(vtkImageData rawImage)
    {
        int[] dims = rawImage.GetDimensions();
        if (dims[0] == 1024 && dims[1] == 1024)
            return;

        // Some of the AMICA images were cropped from their original size
        // of 1024x1024. Therefore, the following pads the images with zero back to
        // original size. The vtkImageTranslateExtent first translates the cropped image
        // to its proper position in the original and the vtkImageConstantPad then pads
        // it with zero to size 1024x1024.

        int[] masking = getMaskSizes();
        //int topMask =    masking[0];
        //int rightMask =  masking[1];
        int bottomMask = masking[2];
        int leftMask =   masking[3];

        vtkImageTranslateExtent translateExtent = new vtkImageTranslateExtent();
        translateExtent.SetInputConnection(rawImage.GetProducerPort());
        translateExtent.SetTranslation(leftMask, bottomMask, 0);
        translateExtent.Update();

        vtkImageConstantPad pad = new vtkImageConstantPad();
        pad.SetInputConnection(translateExtent.GetOutputPort());
        pad.SetOutputWholeExtent(0, 1023, 0, 1023, 0, 0);
        pad.Update();

        vtkImageData padOutput = pad.GetOutput();
        rawImage.DeepCopy(padOutput);

        // shift origin back to zero
        rawImage.SetOrigin(0.0, 0.0, 0.0);
    }

    private void appendWithPadding(StringBuffer strbuf, String str)
    {
        strbuf.append(str);

        int length = str.length();
        while(length < 78)
        {
            strbuf.append(' ');
            ++length;
        }

        strbuf.append("\r\n");
    }

    public String generateBackplanesLabel() throws IOException
    {
        String lblFilename = getInfoFileFullPath();

        FileInputStream fs = new FileInputStream(lblFilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        StringBuffer strbuf = new StringBuffer("");

        String productName = null;

        // The following flag is used to ensure that two or more
        // consecutive blank lines are never output.
        boolean lastLineWasEmpty = false;

        String str;
        while ((str = in.readLine()) != null)
        {
            if (str.startsWith("RECORD_TYPE"))
            {
                String name = new File(lblFilename).getName().toLowerCase();
                name = name.substring(0, name.length()-4) + "_ddr.img";
                appendWithPadding(strbuf, "RECORD_TYPE                        = FIXED_LENGTH");
                appendWithPadding(strbuf, "RECORD_BYTES                       = 4096");
                appendWithPadding(strbuf, "^IMAGE                             = (\"" + name + "\", 1)");
                appendWithPadding(strbuf, "^XCOORD_TABLE                      = (\"" + name + "\", 1025)");
                appendWithPadding(strbuf, "^YCOORD_TABLE                      = (\"" + name + "\", 2049)");
                appendWithPadding(strbuf, "^ZCOORD_TABLE                      = (\"" + name + "\", 3073)");
                appendWithPadding(strbuf, "^LATITUDE_TABLE                    = (\"" + name + "\", 4097)");
                appendWithPadding(strbuf, "^LONGITUDE_TABLE                   = (\"" + name + "\", 5121)");
                appendWithPadding(strbuf, "^DISTANCE_TABLE                    = (\"" + name + "\", 6145)");
                appendWithPadding(strbuf, "^INCIDENCE_ANGLE_TABLE             = (\"" + name + "\", 7169)");
                appendWithPadding(strbuf, "^EMISSION_ANGLE_TABLE              = (\"" + name + "\", 8193)");
                appendWithPadding(strbuf, "^PHASE_ANGLE_TABLE                 = (\"" + name + "\", 9217)");
                appendWithPadding(strbuf, "^HORIZONTALSCALE_TABLE             = (\"" + name + "\", 10241)");
                appendWithPadding(strbuf, "^VERTICAL_SCALE_TABLE              = (\"" + name + "\", 11265)");
                appendWithPadding(strbuf, "^SLOPE_TABLE                       = (\"" + name + "\", 12289)");
                appendWithPadding(strbuf, "^ELEVATION_TABLE                   = (\"" + name + "\", 13313)");
                appendWithPadding(strbuf, "^GRAV_ACCELERATION_TABLE           = (\"" + name + "\", 14337)");
                appendWithPadding(strbuf, "^GRAV_POTENTIAL_TABLE              = (\"" + name + "\", 15361)");
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "");

                continue;
            }

            // The software name and version in the downloaded ddr is not correct
            if (str.startsWith("RECORD_BYTES") ||
                    str.startsWith("FILE_RECORDS") ||
                    str.startsWith("^HEADER") ||
                    str.startsWith("^IMAGE"))
            {
                continue;
            }

            if (str.startsWith("DATA_SET_ID"))
            {
                appendWithPadding(strbuf, "DATA_SET_ID                        = \"HAY-A-AMICA-3-AMICAGEOM-V1.0\"");
                continue;
            }

            if (str.startsWith("PRODUCT_NAME"))
            {
                String[] tokens = str.split("=");
                productName = tokens[1].trim().replaceAll("\"","");
                productName = productName.substring(0, productName.length()-4) + "_DDR.IMG";
                appendWithPadding(strbuf, "PRODUCT_NAME                       = \"" + productName + "\"");
                continue;
            }

            if (str.startsWith("PRODUCT_ID"))
            {
                String[] tokens = str.split("=");
                String name = tokens[1].trim().replaceAll("\"","");
                name = name.substring(0, name.length()-4) + "_DDR_IMG";
                appendWithPadding(strbuf, "PRODUCT_ID                         = \"" + name + "\"");
                continue;
            }

            if (str.startsWith("PRODUCT_CREATION_TIME"))
            {
                appendWithPadding(strbuf, "PRODUCT_TYPE                       = DDR");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String dateStr = sdf.format(date).replace(' ', 'T');
                appendWithPadding(strbuf, "PRODUCT_CREATION_TIME              = " + dateStr);
                continue;
            }

            if (str.startsWith("OBJECT"))
            {
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "/* This DDR label describes one data file:                               */");
                appendWithPadding(strbuf, "/* 1. A multiple-band backplane image file with wavelength-independent,  */");
                appendWithPadding(strbuf, "/* spatial pixel-dependent geometric and timing information.             */");
                appendWithPadding(strbuf, "");

                appendWithPadding(strbuf, "OBJECT                     = IMAGE");
                appendWithPadding(strbuf, "  LINES                    = 1024");
                appendWithPadding(strbuf, "  LINE_SAMPLES             = 1024");
                appendWithPadding(strbuf, "  SAMPLE_TYPE              = PC_REAL");
                appendWithPadding(strbuf, "  SAMPLE_BITS              = 32");
                appendWithPadding(strbuf, "  BANDS                    = 1");
                appendWithPadding(strbuf, "END_OBJECT                 = IMAGE");
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "OBJECT = XCOORD_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"x coordinate of center of pixel, km\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = XCOORD_TABLE");
                appendWithPadding(strbuf, "OBJECT = YCOORD_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"y coordinate of center of pixel, km\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = YCOORD_TABLE");
                appendWithPadding(strbuf, "OBJECT = ZCOORD_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"z coordinate of center of pixel, km\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = ZCOORD_TABLE");
                appendWithPadding(strbuf, "OBJECT = LATITUDE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Latitude, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = LATITUDE_TABLE");
                appendWithPadding(strbuf, "OBJECT = LONGITUDE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Longitude, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = LONGITUDE_TABLE");
                appendWithPadding(strbuf, "OBJECT = DISTANCE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Distance from center of body, km\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = DISTANCE_TABLE");
                appendWithPadding(strbuf, "OBJECT = INCIDENCE_ANGLE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Incidence angle, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = INCIDENCE_ANGLE_TABLE");
                appendWithPadding(strbuf, "OBJECT = EMISSION_ANGLE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Emission angle, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = EMISSION_ANGLE_TABLE");
                appendWithPadding(strbuf, "OBJECT = PHASE_ANGLE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Phase angle, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = PHASE_ANGLE_TABLE");
                appendWithPadding(strbuf, "OBJECT = HORIZONTALSCALE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Horizontal pixel scale, km per pixel\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = HORIZONTALSCALE_TABLE");
                appendWithPadding(strbuf, "OBJECT = VERTICAL_SCALE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Vertical pixel scale, km per pixel\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = VERTICAL_SCALE_TABLE");
                appendWithPadding(strbuf, "OBJECT = SLOPE_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Slope, deg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = SLOPE_TABLE");
                appendWithPadding(strbuf, "OBJECT = ELEVATION_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Elevation, m\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = ELEVATION_TABLE");
                appendWithPadding(strbuf, "OBJECT = GRAV_ACCELERATION_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Gravitational acceleration, m/s^2\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = GRAV_ACCELERATION_TABLE");
                appendWithPadding(strbuf, "OBJECT = GRAV_POTENTIAL_TABLE");
                appendWithPadding(strbuf, "  NAME                     = \"Gravitational potential, J/kg\"");
                appendWithPadding(strbuf, "  ^STRUCTURE               = \"st_ddr.fmt\"");
                appendWithPadding(strbuf, "END_OBJECT = GRAV_POTENTIAL_TABLE");
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "END");

                break;
            }

            if (!lastLineWasEmpty || !str.trim().isEmpty())
                appendWithPadding(strbuf, str);

            lastLineWasEmpty = str.trim().isEmpty();
        }

        in.close();

        return strbuf.toString();
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
    protected int[] getMaskSizes()
    {
        String filename = getFitFileFullPath();

        try
        {
            Fits f = new Fits(filename);
            BasicHDU h = f.getHDU(0);

            int startH = h.getHeader().getIntValue("START_H");
            int startV = h.getHeader().getIntValue("START_V");
            int lastH  = h.getHeader().getIntValue("LAST_H");
            int lastV  = h.getHeader().getIntValue("LAST_V");

            return new int[]{startV, 1023-lastH, 1023-lastV, startH};
        }
        catch (FitsException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Should never reach here
        return new int[]{0, 0, 0, 0};
    }

    @Override
    protected String initializeFitFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        if (rootFolder == null)
        {
            return FileCache.getFileFromServer(key.name + ".fit").getAbsolutePath();
        }
        else
        {
            return rootFolder.getAbsolutePath() + key.name + ".fit";
        }
    }

    @Override
    protected String initializeInfoFileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        String imgLblFilename = key.name + ".lbl";
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
    protected String initializeSumfileFullPath(File rootFolder)
    {
        ImageKey key = getKey();
        File keyFile = new File(key.name);
        String sumFilename = keyFile.getParentFile().getParent()
        + "/sumfiles/N" + keyFile.getName().substring(3, 13) + ".SUM";
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
        String fitName = getFitFileFullPath();

        int ind1 = fitName.lastIndexOf('_');
        int ind2 = fitName.lastIndexOf('.');

        String filterName = fitName.substring(ind1+1, ind2);

        return getFilterNumberFromName(filterName);
    }

    private int getFilterNumberFromName(String name)
    {
        int num = -1;
        if (name.equals("ul"))
            num = 1;
        else if (name.equals("b"))
            num = 2;
        else if (name.equals("v"))
            num = 3;
        else if (name.equals("w"))
            num = 4;
        else if (name.equals("x"))
            num = 5;
        else if (name.equals("p"))
            num = 6;
        else if (name.equals("zs"))
            num = 7;

        return num;
    }

    private String getFilterNameFromNumber(int num)
    {
        String name = "";
        if (num == 1)
            name = "ul";
        else if (num == 2)
            name = "b";
        else if (num == 3)
            name = "v";
        else if (num == 4)
            name = "w";
        else if (num == 5)
            name = "x";
        else if (num == 6)
            name = "p";
        else if (num == 7)
            name = "zs";

        return name;
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
        properties.put("Filter", getFilterNameFromNumber(getFilter()));

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
