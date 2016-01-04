package edu.jhuapl.near.tools;

import edu.jhuapl.near.model.LidarSearchDataCollection.TrackFileType;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.bennu.Bennu;
import edu.jhuapl.near.util.Configuration;

/**
 * This program goes through all the OLA data and divides all the data
 * up into cubes and saves each cube to a separate file.
 *
 * This program also can generate a single vtk file containing all
 * the OLA data (see comments in code).
 */
public class OlaCubesGenerator extends LidarCubesGenerator
{
    private static Bennu bennu = null;
    public static TrackFileType type = TrackFileType.OLA_LEVEL_2;

    public static void main(String[] args)
    {
        Configuration.setAPLVersion(true);
        new OlaCubesGenerator().run();
    }

    @Override
    protected SmallBodyModel getSmallBodyModel()
    {
        if (bennu == null)
        {
            bennu = new Bennu(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"));
        }

        return bennu;
    }

    @Override
    protected int[] getXYZIndices()
    {
        return new int[]{96, 104, 112};
    }

    @Override
    protected int[] getSpacecraftIndices()
    {
        return new int[]{144, 152, 160};
    }

    @Override
    protected String getFileListPath()
    {
//        //Use this to run locally in Eclipse:
//        return "C:/Users/nguyel1/Projects/workspace/sbmt/src/edu/jhuapl/near/data/allOlaFiles.txt";
        //To run on DMZ:
        return "/project/sbmtpipeline/processed/osirisrex/OLA/allOlaFiles.txt";
    }

    @Override
    protected String getOutputFolderPath()
    {
//        //Use this to run locally in Eclipse:
//        return "C:/Users/nguyel1/Projects/workspace/sbmt/src/edu/jhuapl/near/data/OLA";
        //To run on DMZ:
        return "/project/sbmtpipeline/processed/osirisrex/OLA";
    }

    @Override
    protected int getNumberHeaderLines()
    {
        return 0;
    }

    @Override
    protected boolean isInMeters()
    {
        return true;
    }

    @Override
    protected boolean isSpacecraftInSphericalCoordinates()
    {
        return false;
    }

    @Override
    protected int getTimeIndex()
    {
        return 18;
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

    @Override
    protected LidarDataType getLidarDataType()
    {
        return LidarDataType.OLA_LEVEL_2;
    }
}
