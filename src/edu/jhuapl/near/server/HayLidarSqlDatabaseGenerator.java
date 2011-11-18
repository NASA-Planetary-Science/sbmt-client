package edu.jhuapl.near.server;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.itokawa.Itokawa;

public class HayLidarSqlDatabaseGenerator extends LidarSqlDatabaseGenerator
{
    static public void main(String[] args)
    {
        new HayLidarSqlDatabaseGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        return new Itokawa();
    }

    @Override
    protected int[] getXYZIndices()
    {
        return new int[]{6, 7, 8};
    }

    @Override
    protected int[] getSpacecraftIndices()
    {
        return new int[]{3, 4, 5};
    }

    @Override
    protected boolean isSpacecraftInSphericalCoordinates()
    {
        return false;
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
    protected String getFileListPath()
    {
        return "/project/nearsdc/data/ITOKAWA/LIDAR/HayLidarFiles.txt";
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
    protected int getPotentialIndex()
    {
        return -1;
    }
}
