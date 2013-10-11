package edu.jhuapl.near.server;

import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;

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
public class NLRCubesGenerator extends LidarCubesGenerator
{
    private static Eros eros = null;

    public static void main(String[] args)
    {
        new NLRCubesGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        if (eros == null)
        {
            eros = new Eros(ModelFactory.getModelConfig(ModelFactory.EROS, ModelFactory.GASKELL));
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
    protected String getFileListPath()
    {
        return "/project/nearsdc/data/NLR/NlrFiles.txt";
    }

    @Override
    protected String getOutputFolderPath()
    {
        return "/project/nearsdc/data/NLR/cubes";
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
    protected int getPotentialIndex()
    {
        return 18;
    }
}
