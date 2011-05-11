package edu.jhuapl.near.model.itokawa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public String generateBackplanesLabel() throws IOException
    {
        String lblFilename = getInfoFileFullPath();

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

        int numBands = 16;
        String productName = null;

        // The following flag is used to ensure that two or more
        // consecutive blank lines are never output.
        boolean lastLineWasEmpty = false;

        String str;
        while ((str = in.readLine()) != null)
        {
            str = str.trim();

            // The software name and version in the downloaded ddr is not correct
            if (str.startsWith("RECORD_TYPE") ||
                    str.startsWith("RECORD_BYTES") ||
                    str.startsWith("FILE_RECORDS") ||
                    str.startsWith("^HEADER") ||
                    str.startsWith("^IMAGE"))
            {
                continue;
            }

            if (str.startsWith("PRODUCT_NAME"))
            {
                String[] tokens = str.split("=");
                productName = tokens[1].trim().replaceAll("\"","");
                productName = productName.substring(0, productName.length()-4) + "_DDR.IMG";
                strbuf.append("PRODUCT_NAME                       = \"" + productName + "\"\r\n");
                continue;
            }

            if (str.startsWith("PRODUCT_ID"))
            {
                String[] tokens = str.split("=");
                String name = tokens[1].trim().replaceAll("\"","");
                name = name.substring(0, name.length()-4) + "_DDR_IMG";
                strbuf.append("PRODUCT_ID                         = \"" + name + "\"\r\n");
                continue;
            }

            if (str.startsWith("PRODUCT_CREATION_TIME"))
            {
                strbuf.append("PRODUCT_TYPE                       = DDR\r\n");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String dateStr = sdf.format(date).replace(' ', 'T');
                strbuf.append("PRODUCT_CREATION_TIME              = " + dateStr + "\r\n");
                continue;
            }

            if (str.startsWith("OBJECT"))
            {
                strbuf.append("\r\n");
                strbuf.append("/* This DDR label describes one data file:                               */\r\n");
                strbuf.append("/* 1. A multiple-band backplane image file with wavelength-independent,  */\r\n");
                strbuf.append("/* spatial pixel-dependent geometric and timing information.             */\r\n");
                strbuf.append("\r\n");
                strbuf.append("OBJECT                       = FILE\r\n");

                String[] tokens = productName.split(":");
                String imageName = tokens[1].trim().toLowerCase();
                strbuf.append("  ^IMAGE                     = \"" + imageName + "\"\r\n");

                strbuf.append("  RECORD_TYPE                = FIXED_LENGTH\r\n");
                strbuf.append("  RECORD_BYTES               = " + (IMAGE_WIDTH * 4) + "\r\n");
                strbuf.append("  FILE_RECORDS               = " + (IMAGE_HEIGHT * numBands) + "\r\n");
                strbuf.append("\r\n");

                strbuf.append("  OBJECT                     = IMAGE\r\n");
                strbuf.append("    LINES                    = " + IMAGE_HEIGHT + "\r\n");
                strbuf.append("    LINE_SAMPLES             = " + IMAGE_WIDTH + "\r\n");
                strbuf.append("    SAMPLE_TYPE              = PC_REAL\r\n");
                strbuf.append("    SAMPLE_BITS              = 32\r\n");

                strbuf.append("    BANDS                    = " + numBands + "\r\n");
                strbuf.append("    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL\r\n");
                strbuf.append("    BAND_NAME                = (\"Pixel value\",\r\n");
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

            if (!lastLineWasEmpty || !str.isEmpty())
                strbuf.append(str).append("\r\n");

            lastLineWasEmpty = str.isEmpty();
        }

        return strbuf.toString();
    }

//    public String generateBackplanesLabel() throws IOException
//    {
//        StringBuffer strbuf = new StringBuffer("");
//
//        int numBands = 16;
//
//        strbuf.append("PDS_VERSION_ID               = PDS3\r\n");
//        strbuf.append("LABEL_REVISION_NOTE          = \"2004-11-22, S. Slavney (GEO);\r\n");
//        strbuf.append("                                2006-04-05, S. Murchie (JHU/APL);\"\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("/* DDR Identification */\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("DATA_SET_ID                  = \"MRO-M-CRISM-6-DDR-V1.0\"\r\n");
//        strbuf.append("PRODUCT_ID                   = \"???\"\r\n");
//        strbuf.append("/* cccnnnnnnnn_xx_ooaaas_DDRv            */\r\n");
//        strbuf.append("/* ccc = Class Type                      */\r\n");
//        strbuf.append("/* nnnnnnnn = Observation ID (hex)       */\r\n");
//        strbuf.append("/* xx = counter this observation (hex)   */\r\n");
//        strbuf.append("/* ooaaa = obs type, macro number        */\r\n");
//        strbuf.append("/* s = sensor ID (S or L)                */\r\n");
//        strbuf.append("/* v = version number                    */\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("INSTRUMENT_HOST_NAME         = \"MARS RECONNAISSANCE ORBITER\"\r\n");
//        strbuf.append("SPACECRAFT_ID                = MRO\r\n");
//        strbuf.append("INSTRUMENT_NAME              = \"COMPACT RECONNAISSANCE IMAGING\r\n");
//        strbuf.append("                                SPECTROMETER FOR MARS\"\r\n");
//        strbuf.append("INSTRUMENT_ID                = CRISM\r\n");
//        strbuf.append("TARGET_NAME                  = MARS\r\n");
//        strbuf.append("PRODUCT_TYPE                 = DDR\r\n");
//        strbuf.append("PRODUCT_CREATION_TIME        = " + "???" + "\r\n");
//        //et2utc_c(startEt, "ISOC", 3, 40, utc);
//        strbuf.append("START_TIME                   = " + getStartTime() + "\r\n");
//        //et2utc_c(stopEt, "ISOC", 3, 40, utc);
//        strbuf.append("STOP_TIME                    = " + getStopTime() + "\r\n");
//        strbuf.append("SPACECRAFT_CLOCK_START_COUNT = \"" + "???" + "\"\r\n");
//        strbuf.append("SPACECRAFT_CLOCK_STOP_COUNT  = \"" + "???" + "\"\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("ORBIT_NUMBER                 = 0\r\n");
//        strbuf.append("OBSERVATION_TYPE             = \"" + "???" + "\"\r\n");
//        strbuf.append("OBSERVATION_ID               = \"" + "???" + "\r\n");
//        strbuf.append("MRO:OBSERVATION_NUMBER       = " + "???" + "\r\n");
//        strbuf.append("MRO:ACTIVITY_ID              = \"" + "???" + "\"\r\n");
//        strbuf.append("MRO:SENSOR_ID                = \"" + "???" + "\"\r\n");
//        strbuf.append("PRODUCT_VERSION_ID           = \"" + "???" + "\"\r\n");
//        strbuf.append("SOURCE_PRODUCT_ID            = {\r\n");
//        // kernel list here
//        strbuf.append("\r\n");
//        strbuf.append("PRODUCER_INSTITUTION_NAME    = \"APPLIED PHYSICS LABORATORY\"\r\n");
//
//
//        // The software name and version in the downloaded ddr is not correct
//        strbuf.append("SOFTWARE_NAME                = \"Small Body Mapping Tool\"\r\n");
//
//        strbuf.append("SOFTWARE_VERSION_ID          = \"2.0\"\r\n");
//
//
//        strbuf.append("\r\n");
//        strbuf.append("/* DDR Instrument and Observation Parameters */\r\n");
//        strbuf.append("\r\n");
//
//        //spkezr_c("mars", startEt, "J2000", "NONE", "MRO", state, &ltime);
//        strbuf.append("TARGET_CENTER_DISTANCE       = " + "???" + " <KM>\r\n");
//        strbuf.append("                               ");
//        strbuf.append("/* distance to Mars center at first frame */\r\n");
//
//        //spkezr_c("sun", startEt, "J2000", "NONE", "MRO", state, &ltime);
//        strbuf.append("SOLAR_DISTANCE               = " + "???" + " <KM>\r\n");
//
//        // Calculate Solar Longitude
//        // Get vector from Sun to Mars
//        //longitudeRadians = lspcn_c("MARS", startEt, "NONE");
//        // Convert to degrees
//        //convrt_c(longitudeRadians, "RADIANS", "DEGREES", &solarLongitude);
//
//        strbuf.append("SOLAR_LONGITUDE              = " + "???" + " <DEGREES>\r\n");
//        strbuf.append("MRO:FRAME_RATE               = " + "???" + " <HZ>\r\n");
//        strbuf.append("PIXEL_AVERAGING_WIDTH        = " + "???" + "\r\n");
////        strbuf.append("MRO:INSTRUMENT_POINTING_MODE = \"%s POINTING\"\r\n", scanMode == 3 ? "DYNAMIC" : "FIXED");
//
//        strbuf.append("SCAN_MODE_ID                 = \"" + "???" + "\"\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("/* This DDR label describes one data file:                               */\r\n");
//        strbuf.append("/* 1. A multiple-band backplane image file with wavelength-independent,  */\r\n");
//        strbuf.append("/* spatial pixel-dependent geometric and timing information.             */\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("/* See the CRISM Data Products SIS for more detailed description.        */\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("OBJECT                       = FILE\r\n");
//        strbuf.append("  ^IMAGE                     = \"" + "???" + ".img\"\r\n");
//        strbuf.append("  RECORD_TYPE                = FIXED_LENGTH\r\n");
//        strbuf.append("  RECORD_BYTES               = " + (IMAGE_WIDTH * 4) + "\r\n");
//        strbuf.append("  FILE_RECORDS               = " + (IMAGE_HEIGHT * numBands) + "\r\n");
//        strbuf.append("\r\n");
//
//        strbuf.append("  OBJECT                     = IMAGE\r\n");
//        strbuf.append("    LINES                    = " + IMAGE_HEIGHT + "\r\n");
//        strbuf.append("    LINE_SAMPLES             = " + IMAGE_WIDTH + "\r\n");
//        strbuf.append("    SAMPLE_TYPE              = PC_REAL\r\n");
//        strbuf.append("    SAMPLE_BITS              = 32\r\n");
//
//        strbuf.append("    BANDS                    = " + numBands + "\r\n");
//        strbuf.append("    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL\r\n");
//        strbuf.append("    BAND_NAME                = (\"Pixel value\",\r\n");
//        strbuf.append("                                \"x coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
//        strbuf.append("                                \"y coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
//        strbuf.append("                                \"z coordinate of center of pixel, body fixed coordinate system, km\",\r\n");
//        strbuf.append("                                \"Latitude, deg\",\r\n");
//        strbuf.append("                                \"Longitude, deg\",\r\n");
//        strbuf.append("                                \"Distance from center of body, km\",\r\n");
//        strbuf.append("                                \"Incidence angle, measured against the plate model, deg\",\r\n");
//        strbuf.append("                                \"Emission angle, measured against the plate model, deg\",\r\n");
//        strbuf.append("                                \"Phase angle, measured against the plate model, deg\",\r\n");
//        strbuf.append("                                \"Horizontal pixel scale, km per pixel\",\r\n");
//        strbuf.append("                                \"Vertical pixel scale, km per pixel\",\r\n");
//        strbuf.append("                                \"Slope, deg\",\r\n");
//        strbuf.append("                                \"Elevation, m\",\r\n");
//        strbuf.append("                                \"Gravitational acceleration, m/s^2\",\r\n");
//        strbuf.append("                                \"Gravitational potential, J/kg\")\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("  END_OBJECT                 = IMAGE\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("END_OBJECT                   = FILE\r\n");
//        strbuf.append("\r\n");
//        strbuf.append("END\r\n");
//
//        strbuf.append("\r\n");
//
//        return strbuf.toString();
//    }

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
        String fitName = new File(getFitFileFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
    }
}
