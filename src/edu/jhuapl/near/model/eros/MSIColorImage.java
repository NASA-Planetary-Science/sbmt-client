package edu.jhuapl.near.model.eros;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.SmallBodyModel;

public class MSIColorImage extends ColorImage
{

    public MSIColorImage(ColorImageKey key, SmallBodyModel smallBodyModel)
            throws FitsException, IOException, NoOverlapException
    {
        super(key, smallBodyModel);
    }

    @Override
    protected Image createImage(ImageKey key, SmallBodyModel smallBodyModel)
            throws FitsException, IOException
    {
        return new MSIImage(key, smallBodyModel, false, null);
    }
}
