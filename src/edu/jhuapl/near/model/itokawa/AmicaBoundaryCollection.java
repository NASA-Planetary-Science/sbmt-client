package edu.jhuapl.near.model.itokawa;

import java.io.IOException;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.ImageBoundary;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class AmicaBoundaryCollection extends ImageBoundaryCollection
{
    private SmallBodyModel itokawaModel;

    public AmicaBoundaryCollection(SmallBodyModel itokawaModel)
    {
        super(itokawaModel);

        this.itokawaModel = itokawaModel;
    }

    @Override
    protected ImageBoundary createBoundary(ImageKey key,
            SmallBodyModel smallBodyModel) throws IOException, FitsException
    {
        return new ImageBoundary(new AmicaImage(key, smallBodyModel, true, null), itokawaModel);
    }
}
