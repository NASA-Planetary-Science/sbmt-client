package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.PerspectiveImage.ImageKey;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.gaspra.SSIGaspraImage;
import edu.jhuapl.near.model.ida.SSIIdaImage;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.mathilde.MSIMathildeImage;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.model.vesta.Vesta;

public class ImageFactory
{
    static public PerspectiveImage createImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        if (smallBodyModel instanceof Eros)
            return new MSIImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else if (smallBodyModel instanceof Itokawa)
            return new AmicaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else if (smallBodyModel instanceof Vesta)
            return new FcImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else if (smallBodyModel.getModelName().toLowerCase().equals("gaspra"))
            return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else if (smallBodyModel.getModelName().toLowerCase().equals("ida"))
            return new SSIIdaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else if (smallBodyModel.getModelName().toLowerCase().equals("mathilde"))
            return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        else
            return null;
    }
}
