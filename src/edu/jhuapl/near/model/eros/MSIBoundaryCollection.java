package edu.jhuapl.near.model.eros;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageBoundary;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class MSIBoundaryCollection extends ImageBoundaryCollection
{
    private SmallBodyModel erosModel;

    public MSIBoundaryCollection(SmallBodyModel erosModel)
    {
        super(erosModel);

        this.erosModel = erosModel;
    }

    @Override
    protected ImageBoundary createBoundary(ImageKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        return new ImageBoundary(new MSIImage(key, smallBodyModel, true, null), erosModel);
    }
}
