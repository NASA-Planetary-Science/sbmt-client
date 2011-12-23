package edu.jhuapl.near.server;

import java.io.IOException;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.itokawa.Itokawa;

/**
 * This program goes through all the NLR data and divides all the data
 * up into cubes and saves each cube to a separate file.
 *
 * This program also can generate a single vtk file containing all
 * the NLR data (see comments in code).
 *
 * @author kahneg1
 *
 */
public class HayLidarCubesGenerator extends LidarCubesGenerator
{
    private static String fileListPath = null;
    private static String outputFolder = null;
    private static Itokawa itokawa;


    public static void main(String[] args)
    {
        fileListPath = "/project/nearsdc/data/ITOKAWA/LIDAR/HayLidarFiles.txt";
        outputFolder = "/project/nearsdc/data/ITOKAWA/LIDAR/cubes-optimized";
        new HayLidarCubesGenerator().run();

        fileListPath = "/project/nearsdc/data/ITOKAWA/LIDAR/HayLidarFilesUnfiltered.txt";
        outputFolder = "/project/nearsdc/data/ITOKAWA/LIDAR/cubes-unfiltered";
        new HayLidarCubesGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        if (itokawa == null)
        {
            itokawa = new Itokawa();

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
    protected String getFileListPath()
    {
        return fileListPath;
    }

    @Override
    protected String getOutputFolderPath()
    {
        return outputFolder;
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
    protected int getPotentialIndex()
    {
        return -1;
    }
}
