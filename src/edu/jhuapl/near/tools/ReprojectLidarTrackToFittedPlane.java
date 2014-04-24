package edu.jhuapl.near.tools;

import java.io.File;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.ModelConfig;
import edu.jhuapl.near.model.ModelConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.ModelConfig.ShapeModelBody;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program takes a lidar track, fits a plane through it and reorients the track into
 * a new coordinate system such that the fitted plane is the XY plane in the new coordinate
 * system.
 *
 * This program takes 2 arguments:
 * 1. <input-track> - path to input track file
 * 2. <output-track> - path to output track file
 *
 * On output this program also generates a file containing the transformation matrix which
 * was used to convert the original track to the new coordinate system. This file has the
 * same name as <output-track> but with "-transformation.txt" appended to it.
 */
public class ReprojectLidarTrackToFittedPlane
{

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: ReprojectLidarTrackToFittedPlane <input-track> <output-track>");
            System.exit(0);
        }

        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        String inputFile = args[0];
        String outputFile = args[1];

        ModelConfig config = ModelConfig.getModelConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL);
        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(config);
        LidarSearchDataCollection lidarModel = (LidarSearchDataCollection) ModelFactory.
                createLidarModels(smallBodyModel).get(ModelNames.LIDAR_SEARCH);

        try
        {
            lidarModel.loadTracksFromFiles(new File[]{new File(inputFile)});

            String transformationFile = outputFile + "-transformation.txt";
            lidarModel.reprojectedTrackOntoFittedPlane(0, new File(outputFile), new File(transformationFile));
        }
        catch (Exception e)
        {
            System.out.println("An error saving the track to disk.");
            System.exit(1);
        }

    }

}
