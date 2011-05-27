package edu.jhuapl.near.model.itokawa;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.SmallBodyModel;

public class AmicaColorImage extends ColorImage
{

    public AmicaColorImage(ColorImageKey key, SmallBodyModel smallBodyModel)
            throws FitsException, IOException, NoOverlapException
    {
        super(key, smallBodyModel);
    }

    @Override
    protected Image createImage(ImageKey key, SmallBodyModel smallBodyModel)
            throws FitsException, IOException
    {
        return new AmicaImage(key, smallBodyModel, false, null);
    }
}
