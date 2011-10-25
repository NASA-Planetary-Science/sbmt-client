package edu.jhuapl.near.model.eros;

import edu.jhuapl.near.model.LidarBrowseDataCollection;

public class NLRBrowseDataCollection extends LidarBrowseDataCollection
{

    @Override
    protected int[] getXYZIndices()
    {
        return new int[]{14, 15, 16};
    }

    @Override
    protected int[] getSpacecraftIndices()
    {
        return new int[]{8, 9, 10};
    }

    @Override
    protected boolean isSpacecraftInSphericalCoordinates()
    {
        return true;
    }

    @Override
    protected int getTimeIndex()
    {
        return 4;
    }

    @Override
    protected int getNoiseIndex()
    {
        return 7;
    }

    @Override
    protected String getFileListResourcePath()
    {
        return "/edu/jhuapl/near/data/NlrFiles.txt";
    }

    @Override
    protected int getNumberHeaderLines()
    {
        return 2;
    }

    @Override
    protected boolean isInMeters()
    {
        return true;
    }

    @Override
    public double getOffsetScale()
    {
        return 0.025;
    }
}
