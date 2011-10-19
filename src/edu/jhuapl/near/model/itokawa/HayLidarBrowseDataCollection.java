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
    protected int[] getSpacecraftXYZIndices()
    {
        return new int[]{3, 4, 5};
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

    @Override
    public double getOffsetScale()
    {
        // The following value is the Itokawa diagonal length divided by 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is 0.025 km.
        return 0.00044228259621279913;
    }
}
