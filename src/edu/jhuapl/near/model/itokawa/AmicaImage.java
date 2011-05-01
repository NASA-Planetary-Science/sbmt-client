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
            double[] sunPosition,
            double[] frustum1,
            double[] frustum2,
            double[] frustum3,
            double[] frustum4,
            double[] boresightDirection,
            double[] upVector) throws NumberFormatException, IOException
    {

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
        File fitFile = FileCache.getFileFromServer(key.name + ".FIT");

        if (fitFile == null)
            throw new IOException("Could not download " + key.name);

        String imgLblFilename = key.name + "_DDR.LBL";
        FileCache.getFileFromServer(imgLblFilename);

        if (key.source.equals(ImageSource.GASKELL))
        {
            // Try to load a sumfile if there is one
            File tmp = new File(key.name);
            String sumFilename = "/ITOKAWA/sumfiles/" + tmp.getName().substring(0, 11) + ".SUM";
            FileCache.getFileFromServer(sumFilename);
        }

        //String footprintFilename = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
        //FileCache.getFileFromServer(footprintFilename);

        String filename = fitFile.getAbsolutePath();
        this.fullpath = filename;
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
