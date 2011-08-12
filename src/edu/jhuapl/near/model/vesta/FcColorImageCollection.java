package edu.jhuapl.near.model.vesta;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.ColorImage;
import edu.jhuapl.near.model.ColorImage.ColorImageKey;
import edu.jhuapl.near.model.ColorImage.NoOverlapException;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class FcColorImageCollection extends ColorImageCollection
{

    public FcColorImageCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    protected ColorImage createImage(ColorImageKey key,
            SmallBodyModel smallBodyModel) throws FitsException, IOException, NoOverlapException
    {
        return new FcColorImage(key, smallBodyModel);
    }
}
