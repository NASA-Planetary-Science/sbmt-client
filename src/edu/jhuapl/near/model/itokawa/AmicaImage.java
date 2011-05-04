package edu.jhuapl.near.model.itokawa;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;

public class AmicaImage extends Image
{
    public static final int IMAGE_WIDTH = 1024;
    public static final int IMAGE_HEIGHT = 1024;

    private String fitFileFullPath; // The actual path of the image stored on the local disk (after downloading from the server)
    private String infoFileFullPath;
    private String sumfileFullPath;

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
    protected void doLoadImageInfo(
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
        StringBuffer strbuf = new StringBuffer("");

        // The software name and version in the downloaded ddr is not correct
        strbuf.append("SOFTWARE_NAME                = \"Small Body Mapping Tool\"\r\n");

        strbuf.append("SOFTWARE_VERSION_ID          = \"2.0\"\r\n");

        // The planes in the downloaded ddr are all wrong
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

        strbuf.append("\r\n");

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
    protected void downloadFilesIntoCache() throws IOException
    {
        ImageKey key = getKey();

        // Download the image, and all the companion files if necessary.
        File fitFile = FileCache.getFileFromServer(key.name + ".fit");

        if (fitFile == null)
            throw new IOException("Could not download " + key.name);

        this.fitFileFullPath = fitFile.getAbsolutePath();

        this.infoFileFullPath = "";

        if (key.source.equals(ImageSource.GASKELL))
        {
            // Try to load a sumfile if there is one
            File tmp = new File(key.name);
            String sumFilename = "/ITOKAWA/AMICA/sumfiles/N" + tmp.getName().substring(3, 13) + ".SUM";
            File sumfile = FileCache.getFileFromServer(sumFilename);
            this.sumfileFullPath = sumfile.getAbsolutePath();
        }

        //String footprintFilename = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
        //FileCache.getFileFromServer(footprintFilename);
    }

    @Override
    protected void initializeFilePaths(File fitFile)
    {
        this.fitFileFullPath = fitFile.getAbsolutePath();

        this.infoFileFullPath = "";

        String id = fitFile.getName().substring(3, 13);
        File parentdir = fitFile.getParentFile().getParentFile();
        this.sumfileFullPath = parentdir.getAbsolutePath() + "/sumfiles/N" + id + ".SUM";
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
    protected String getFitFileFullPath()
    {
        return fitFileFullPath;
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
        String fitName = new File(getFitFileFullPath()).getName();
        return Integer.parseInt(fitName.substring(12,13));
    }


}
