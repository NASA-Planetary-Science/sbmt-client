package edu.jhuapl.near.model.mathilde;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.util.FileCache;

public class MSIMathildeImage extends MSIImage
{
    public MSIMathildeImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly, rootFolder);
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
        return null;
    }

}
