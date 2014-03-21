package edu.jhuapl.near.tools;

import java.io.IOException;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.itokawa.Itokawa;

public class HayLidarSqlDatabaseGenerator extends LidarSqlDatabaseGenerator
{
    private static String fileListPath = null;
    private static String dbName = null;
    private static Itokawa itokawa = null;

    static public void main(String[] args)
    {
        fileListPath = "/project/nearsdc/data/ITOKAWA/LIDAR/HayLidarFiles.txt";
        dbName = "/project/nearsdc/data/ITOKAWA/LIDAR/lidar";
        new HayLidarSqlDatabaseGenerator().run();

        fileListPath = "/project/nearsdc/data/ITOKAWA/LIDAR/HayLidarFilesUnfiltered.txt";
        dbName = "/project/nearsdc/data/ITOKAWA/LIDAR/lidar-unfiltered";
        new HayLidarSqlDatabaseGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        if (itokawa == null)
        {
            itokawa = new Itokawa(ModelFactory.getModelConfig(ModelFactory.ITOKAWA, ModelFactory.GASKELL));

            try
            {
                itokawa.setModelResolution(3);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return itokawa;
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
        return fileListPath;
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

    @Override
    protected String getDatabasePath()
    {
        return dbName;
    }
}
