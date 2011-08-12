package edu.jhuapl.near.model.vesta;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class FcImageCollection extends ImageCollection
{
    public FcImageCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    protected Image createImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return new FcImage(key, smallBodyModel, false, null);
    }
}
