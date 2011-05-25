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

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class AmicaImage extends Image
{
    public static final int IMAGE_WIDTH = 1024;
    public static final int IMAGE_HEIGHT = 1024;

    public static final double FOV_PARAMETER1 = -0.049782949;
    public static final double FOV_PARAMETER2 = -0.049782949;

    private String fitFileFullPath; // The actual path of the image stored on the local disk (after downloading from the server)
    private String infoFileFullPath;
    private String sumfileFullPath;

    public AmicaImage(ImageKey key)
    {
        super(key);
    }

    public AmicaImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        super(key, smallBodyModel);
    }

    public AmicaImage(File fitFile, SmallBodyModel eros, ImageSource source)
            throws FitsException, IOException
    {
        super(fitFile, eros, source);
    }

    @Override
    protected void downloadFilesIntoCache() throws IOException
    {
        // Download the FIT file, the LBL file, and, if gaskell source, the sumfile

        getFitFileFullPath();

        getInfoFileFullPath();

        ImageKey key = getKey();
        if (key.source.equals(ImageSource.GASKELL))
            getSumfileFullPath();
    }

    @Override
    protected void initializeFilePaths(File fitFile)
    {
        this.fitFileFullPath = fitFile.getAbsolutePath();

        this.infoFileFullPath = fitFileFullPath.substring(0, fitFileFullPath.length()-4) + ".lbl";

        String id = fitFile.getName().substring(3, 13);
        File parentdir = fitFile.getParentFile().getParentFile();
        this.sumfileFullPath = parentdir.getAbsolutePath() + "/sumfiles/N" + id + ".SUM";
    }

    @Override
    public void loadImageInfo(
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

        int numBands = 16;
        String productName = null;

        // The following flag is used to ensure that two or more
        // consecutive blank lines are never output.
        boolean lastLineWasEmpty = false;

        String str;
        while ((str = in.readLine()) != null)
        {
            // The software name and version in the downloaded ddr is not correct
            if (str.startsWith("RECORD_TYPE") ||
                    str.startsWith("RECORD_BYTES") ||
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
                appendWithPadding(strbuf, "OBJECT                       = FILE");

                String[] tokens = productName.split(":");
                String imageName = tokens[1].trim().toLowerCase();
                appendWithPadding(strbuf, "  ^IMAGE                     = \"" + imageName + "\"");

                appendWithPadding(strbuf, "  RECORD_TYPE                = FIXED_LENGTH");
                appendWithPadding(strbuf, "  RECORD_BYTES               = " + (IMAGE_WIDTH * 4));
                appendWithPadding(strbuf, "  FILE_RECORDS               = " + (IMAGE_HEIGHT * numBands));
                appendWithPadding(strbuf, "");

                appendWithPadding(strbuf, "  OBJECT                     = IMAGE");
                appendWithPadding(strbuf, "    LINES                    = " + IMAGE_HEIGHT);
                appendWithPadding(strbuf, "    LINE_SAMPLES             = " + IMAGE_WIDTH);
                appendWithPadding(strbuf, "    SAMPLE_TYPE              = PC_REAL");
                appendWithPadding(strbuf, "    SAMPLE_BITS              = 32");

                appendWithPadding(strbuf, "    BANDS                    = " + numBands);
                appendWithPadding(strbuf, "    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL");
                appendWithPadding(strbuf, "    BAND_NAME                = (\"Pixel value\",");
                appendWithPadding(strbuf, "                                \"x coordinate of center of pixel, km\",");
                appendWithPadding(strbuf, "                                \"y coordinate of center of pixel, km\",");
                appendWithPadding(strbuf, "                                \"z coordinate of center of pixel, km\",");
                appendWithPadding(strbuf, "                                \"Latitude, deg\",");
                appendWithPadding(strbuf, "                                \"Longitude, deg\",");
                appendWithPadding(strbuf, "                                \"Distance from center of body, km\",");
                appendWithPadding(strbuf, "                                \"Incidence angle, deg\",");
                appendWithPadding(strbuf, "                                \"Emission angle, deg\",");
                appendWithPadding(strbuf, "                                \"Phase angle, deg\",");
                appendWithPadding(strbuf, "                                \"Horizontal pixel scale, km per pixel\",");
                appendWithPadding(strbuf, "                                \"Vertical pixel scale, km per pixel\",");
                appendWithPadding(strbuf, "                                \"Slope, deg\",");
                appendWithPadding(strbuf, "                                \"Elevation, m\",");
                appendWithPadding(strbuf, "                                \"Gravitational acceleration, m/s^2\",");
                appendWithPadding(strbuf, "                                \"Gravitational potential, J/kg\")");
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "  END_OBJECT                 = IMAGE");
                appendWithPadding(strbuf, "");
                appendWithPadding(strbuf, "END_OBJECT                   = FILE");
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
    public int getImageWidth()
    {
        return IMAGE_WIDTH;
    }

    @Override
    public int getImageHeight()
    {
        return IMAGE_HEIGHT;
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
    protected int getLeftMask()
    {
        return 0;
    }

    @Override
    protected int getRightMask()
    {
        return 0;
    }

    @Override
    protected int getTopMask()
    {
        return 0;
    }

    @Override
    protected int getBottomMask()
    {
        return 0;
    }

    @Override
    public String getFitFileFullPath()
    {
        if (fitFileFullPath == null)
        {
            ImageKey key = getKey();
            File fitFile = FileCache.getFileFromServer(key.name + ".fit");
            this.fitFileFullPath = fitFile.getAbsolutePath();
        }

        return fitFileFullPath;
    }

    @Override
    public String getInfoFileFullPath()
    {
        if (infoFileFullPath == null)
        {
            ImageKey key = getKey();
            String imgLblFilename = key.name + ".lbl";
            File infoFile = FileCache.getFileFromServer(imgLblFilename);
            this.infoFileFullPath = infoFile.getAbsolutePath();
        }

        return infoFileFullPath;
    }

    @Override
    public String getSumfileFullPath()
    {
        if (sumfileFullPath == null)
        {
            ImageKey key = getKey();
            File tmp = new File(key.name);
            String sumFilename = "/ITOKAWA/AMICA/sumfiles/N" + tmp.getName().substring(3, 13) + ".SUM";
            File sumfile = FileCache.getFileFromServer(sumFilename);
            this.sumfileFullPath = sumfile.getAbsolutePath();
        }

        return sumfileFullPath;
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
