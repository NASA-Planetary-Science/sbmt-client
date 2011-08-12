package edu.jhuapl.near.model.vesta;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageBoundary;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class FcBoundaryCollection extends ImageBoundaryCollection
{
    private SmallBodyModel vestaModel;

    public FcBoundaryCollection(SmallBodyModel itokawaModel)
    {
        super(itokawaModel);

        this.vestaModel = itokawaModel;
    }

    @Override
    protected ImageBoundary createBoundary(ImageKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        return new ImageBoundary(new FcImage(key, smallBodyModel, true, null), vestaModel);
    }
}
