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

    private String fullpath; // The actual path of the image stored on the local disk (after downloading from the server)
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

        this.fullpath = fitFile.getAbsolutePath();

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
        this.fullpath = fitFile.getAbsolutePath();

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
