package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.SmallBodyModel;

public class NLRSearchDataCollection extends LidarSearchDataCollection
{
    public NLRSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    @Override
    public double getOffsetScale()
    {
        return 0.025;
    }

    @Override
    protected String getCubeFolderPath()
    {
        return "/NLR/cubes";
    }

}
