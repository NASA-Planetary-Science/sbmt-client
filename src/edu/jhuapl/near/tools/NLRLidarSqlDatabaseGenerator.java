package edu.jhuapl.near.tools;

import java.io.IOException;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ShapeModelAuthor;
import edu.jhuapl.near.model.ModelFactory.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;

public class NLRLidarSqlDatabaseGenerator extends LidarSqlDatabaseGenerator
{
    private static String fileListPath = null;
    private static String dbName = null;
    private static Eros eros = null;

    static public void main(String[] args)
    {
        fileListPath = "/project/nearsdc/data/NLR/NlrFiles.txt";
        dbName = "/project/nearsdc/data/NLR/lidar";
        new NLRLidarSqlDatabaseGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        if (eros == null)
        {
            eros = new Eros(ModelFactory.getModelConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL));

            try
            {
                eros.setModelResolution(3);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return eros;
    }

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
    protected String getFileListPath()
    {
        return fileListPath;
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
    protected int getPotentialIndex()
    {
        return 18;
    }

    @Override
    protected String getDatabasePath()
    {
        return dbName;
    }
}
