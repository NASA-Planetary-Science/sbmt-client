package edu.jhuapl.near.model.eros;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class MSIImageCollection extends ImageCollection
{
    public MSIImageCollection(SmallBodyModel eros)
    {
        super(eros);
    }

    @Override
    protected Image createImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return new MSIImage(key, smallBodyModel);
    }
}
