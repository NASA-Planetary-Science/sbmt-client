package edu.jhuapl.near.model.deimos;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.app.SmallBodyModel;
import edu.jhuapl.near.model.phobos.PhobosImage;

public class DeimosImage extends PhobosImage
{
    public DeimosImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }
}
