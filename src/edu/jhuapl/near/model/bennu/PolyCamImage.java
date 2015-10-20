package edu.jhuapl.near.model.bennu;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileCache;


public class PolyCamImage extends MapCamImage
{

    public PolyCamImage(ImageKey key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String initializeInfoFileFullPath()
    {
        ImageKey key = getKey();
        String result = null;

        // if the source is GASKELL, then return a null
        if (key.source == null || key.source != null && key.source == ImageSource.GASKELL)
            result = null;
        else
        {
            File keyFile = new File(key.name);
            String infodir = "infofiles";
            String pointingFileName = keyFile.getParentFile().getParent() + File.separator + infodir + File.separator + keyFile.getName() + ".INFO";

            try {
                result = FileCache.getFileFromServer(pointingFileName).getAbsolutePath();
            } catch (Exception e) {
                result = null;
            }
        }

        return result;
    }



    @Override
    protected String initializeSumfileFullPath()
    {
        ImageKey key = getKey();
        String result = null;

        // if the source is SPICE, then return a null
        if (key.source == null || key.source != null && key.source == ImageSource.SPICE)
            result = null;
        else
        {
            File keyFile = new File(key.name);
            String sumdir = "sumfiles";
            String sumFilename = keyFile.getParentFile().getParent() + File.separator + sumdir + File.separator + keyFile.getName() + ".SUM";

            try {
                result = FileCache.getFileFromServer(sumFilename).getAbsolutePath();
            } catch (Exception e) {
                result = null;
            }
        }

        return result;
    }
}
