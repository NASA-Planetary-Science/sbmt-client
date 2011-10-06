package edu.jhuapl.near.model.itokawa;

import edu.jhuapl.near.model.LidarBrowseDataCollection;

public class HayLidarBrowseDataCollection extends LidarBrowseDataCollection
{
    @Override
    protected int[] getXYZIndices()
    {
        return new int[]{6, 7, 8};
    }

    @Override
    protected int getTimeIndex()
    {
        return 1;
    }

    @Override
    protected int getNoiseIndex()
    {
        return -1;
    }

    @Override
    protected String getFileListResourcePath()
    {
        return "/edu/jhuapl/near/data/HayLidarFiles.txt";
    }

    @Override
    protected int getNumberHeaderLines()
    {
        return 0;
    }

    @Override
    protected boolean isInMeters()
    {
        return false;
    }
}
