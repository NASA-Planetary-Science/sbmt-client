package edu.jhuapl.near.model.eros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.MathUtil;

public class MSIImage extends Image
{
    public static final int IMAGE_WIDTH = 537;
    public static final int IMAGE_HEIGHT = 412;

    // Number of pixels on each side of the image that are
    // masked out (invalid) due to filtering.
    public static final int LEFT_MASK = 14;
    public static final int RIGHT_MASK = 14;
    public static final int TOP_MASK = 2;
    public static final int BOTTOM_MASK = 2;

    public static final String MSI_FRUSTUM1 = "MSI_FRUSTUM1";
    public static final String MSI_FRUSTUM2 = "MSI_FRUSTUM2";
    public static final String MSI_FRUSTUM3 = "MSI_FRUSTUM3";
    public static final String MSI_FRUSTUM4 = "MSI_FRUSTUM4";
    public static final String MSI_BORESIGHT_DIRECTION = "MSI_BORESIGHT_DIRECTION";
    public static final String MSI_UP_DIRECTION = "MSI_UP_DIRECTION";
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    //public static final String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    //public static final String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String SUN_POSITION_LT = "SUN_POSITION_LT";

    private SmallBodyModel erosModel;
    private String fullpath; // The actual path of the image stored on the local disk (after downloading from the server)
    private String infoFileFullPath;
    private String sumfileFullPath;

    public MSIImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        super(key, smallBodyModel);
    }

    public MSIImage(File fitFile, SmallBodyModel eros, ImageSource source)
            throws FitsException, IOException
    {
        super(fitFile, eros, source);
        this.erosModel = eros;
    }

    @Override
    protected void downloadFilesIntoCache() throws IOException
    {
        ImageKey key = getKey();

        // Download the FIT file, the LBL file, and, if gaskell source, the sumfile
        File fitFile = FileCache.getFileFromServer(key.name + ".FIT");
        this.fullpath = fitFile.getAbsolutePath();

        if (fitFile == null)
            throw new IOException("Could not download " + key.name);


        String imgLblFilename = key.name + "_DDR.LBL";
        File infoFile = FileCache.getFileFromServer(imgLblFilename);
        this.infoFileFullPath = infoFile.getAbsolutePath();

        if (key.source.equals(ImageSource.GASKELL))
        {
            // Try to load a sumfile if there is one
            File tmp = new File(key.name);
            String sumFilename = "/MSI/sumfiles/" + tmp.getName().substring(0, 11) + ".SUM";
            File sumfile = FileCache.getFileFromServer(sumFilename);
            this.sumfileFullPath = sumfile.getAbsolutePath();
        }

        //String footprintFilename = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
        //FileCache.getFileFromServer(footprintFilename);
    }


    @Override
    protected void initializeFilePaths(File fitFile)
    {
        this.fullpath = fitFile.getAbsolutePath();

        this.infoFileFullPath = fullpath.substring(0, fullpath.length()-4) + "_DDR.LBL";

        File sumfile = new File(fullpath);
        String sumname = sumfile.getName().substring(0, 11);
        sumfile = sumfile.getParentFile().getParentFile().getParentFile().getParentFile();
        this.sumfileFullPath = sumfile.getAbsolutePath() + "/sumfiles/" + sumname + ".SUM";
    }


    /**
     *     Make this static so it can be called without needing access
     *     to an MSIImage object.
     */
    static public void loadImageInfo(
            String lblFilename,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunPosition,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws NumberFormatException, IOException
    {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(lblFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String str;
        while ((str = in.readLine()) != null)
        {
            StringTokenizer st = new StringTokenizer(str);
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                if (START_TIME.equals(token))
                {
                    st.nextToken();
                    startTime[0] = st.nextToken();
                }
                if (STOP_TIME.equals(token))
                {
                    st.nextToken();
                    stopTime[0] = st.nextToken();
                }
                if (SPACECRAFT_POSITION.equals(token) ||
                        MSI_FRUSTUM1.equals(token) ||
                        MSI_FRUSTUM2.equals(token) ||
                        MSI_FRUSTUM3.equals(token) ||
                        MSI_FRUSTUM4.equals(token) ||
                        SUN_POSITION_LT.equals(token) ||
                        MSI_BORESIGHT_DIRECTION.equals(token) ||
                        MSI_UP_DIRECTION.equals(token))
                {
                    st.nextToken();
                    st.nextToken();
                    double x = Double.parseDouble(st.nextToken());
                    st.nextToken();
                    double y = Double.parseDouble(st.nextToken());
                    st.nextToken();
                    double z = Double.parseDouble(st.nextToken());
                    if (SPACECRAFT_POSITION.equals(token))
                    {
                        spacecraftPosition[0] = x;
                        spacecraftPosition[1] = y;
                        spacecraftPosition[2] = z;
                    }
                    else if (MSI_FRUSTUM1.equals(token))
                    {
                        frustum1[0] = x;
                        frustum1[1] = y;
                        frustum1[2] = z;
                        MathUtil.vhat(frustum1, frustum1);
                    }
                    else if (MSI_FRUSTUM2.equals(token))
                    {
                        frustum2[0] = x;
                        frustum2[1] = y;
                        frustum2[2] = z;
                        MathUtil.vhat(frustum2, frustum2);
                    }
                    else if (MSI_FRUSTUM3.equals(token))
                    {
                        frustum3[0] = x;
                        frustum3[1] = y;
                        frustum3[2] = z;
                        MathUtil.vhat(frustum3, frustum3);
                    }
                    else if (MSI_FRUSTUM4.equals(token))
                    {
                        frustum4[0] = x;
                        frustum4[1] = y;
                        frustum4[2] = z;
                        MathUtil.vhat(frustum4, frustum4);
                    }
                    if (SUN_POSITION_LT.equals(token))
                    {
                        sunPosition[0] = x;
                        sunPosition[1] = y;
                        sunPosition[2] = z;
                    }
                    if (MSI_BORESIGHT_DIRECTION.equals(token))
                    {
                        boresightDirection[0] = x;
                        boresightDirection[1] = y;
                        boresightDirection[2] = z;
                    }
                    if (MSI_UP_DIRECTION.equals(token))
                    {
                        upVector[0] = x;
                        upVector[1] = y;
                        upVector[2] = z;
                    }
                }
            }
        }

        in.close();
    }

    @Override
    protected void doLoadImageInfo(
            String lblFilename,
            String[] startTime,
            String[] stopTime,
            double[] spacecraftPosition,
            double[] sunPosition,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws NumberFormatException, IOException
    {
        MSIImage.loadImageInfo(
                lblFilename,
                startTime,
                stopTime,
                spacecraftPosition,
                sunPosition,
                frustum1,
                frustum2,
                frustum3,
                frustum4,
                boresightDirection,
                upVector);
    }

    public String generateBackplanesLabel() throws IOException
    {
        String lblFilename = getFullPath().substring(0, getFullPath().length()-4) + "_DDR.LBL";

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
    protected int getLeftMask()
    {
        return LEFT_MASK;
    }

    @Override
    protected int getRightMask()
    {
        return RIGHT_MASK;
    }

    @Override
    protected int getTopMask()
    {
        return TOP_MASK;
    }

    @Override
    protected int getBottomMask()
    {
        return BOTTOM_MASK;
    }

    @Override
    protected String getFullPath()
    {
        return fullpath;
    }

    @Override
    protected String getInfoFileFullPath()
    {
        return infoFileFullPath;
    }

    @Override
    protected String getSumfileFullPath()
    {
        return sumfileFullPath;
    }

    @Override
    public int getFilter()
    {
        String fitName = new File(getFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
    }


}
