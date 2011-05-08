package edu.jhuapl.near.model.itokawa;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class AmicaImageCollection extends ImageCollection
{
    public AmicaImageCollection(SmallBodyModel eros)
    {
        super(eros);
    }

    @Override
    protected Image createImage(ImageKey key, SmallBodyModel smallBodyModel) throws FitsException, IOException
    {
        return new AmicaImage(key, smallBodyModel);
    }
}
