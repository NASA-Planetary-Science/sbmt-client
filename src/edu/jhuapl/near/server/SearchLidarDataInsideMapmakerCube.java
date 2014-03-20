package edu.jhuapl.near.server;

import java.io.File;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.AbstractEllipsePolygonModel;
import edu.jhuapl.near.model.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.DEMModel;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointInDEMChecker;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class SearchLidarDataInsideMapmakerCube
{
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        if (args.length != 7)
        {
            System.out.println("Usage: SearchLidarDataInsideMapmakerCube <maplet-file> <start-date> <end-date> <min-track-size> <track-separation-time> <min_distance-from-boundary> <output-folder>");
            System.exit(0);
        }

        String mapletFile = args[0];
        DateTime startDate = new DateTime(args[1]);
        DateTime endDate = new DateTime(args[2]);
        int minTrackLength = 10;
        double timeSeparationBetweenTracks = 10.0;
        double minDistanceFromBoundary = 1.0;

        try
        {
            minTrackLength = Integer.parseInt(args[3]);

            if (minTrackLength < 1)
            {
                System.out.println("Error: Minimum track length must be a positive integer.");
                System.exit(1);
            }
        }
        catch(NumberFormatException e)
        {
            System.out.println("An error occurred parsing the min track length.");
            System.exit(1);
        }


        try
        {
            timeSeparationBetweenTracks = Double.parseDouble(args[4]);

            if (timeSeparationBetweenTracks < 0.0)
            {
                System.out.println("Error: Track separation must be nonnegative.");
                System.exit(1);
            }
        }
        catch(NumberFormatException e)
        {
            System.out.println("An error occurred parsing the track separation time.");
            System.exit(1);
        }

        try
        {
            minDistanceFromBoundary = Double.parseDouble(args[5]);

            if (minDistanceFromBoundary < 0.0)
            {
                System.out.println("Error: Minimum distance from boundary nonnegative.");
                System.exit(1);
            }
        }
        catch(NumberFormatException e)
        {
            System.out.println("An error occurred parsing the minimum distance from boundary.");
            System.exit(1);
        }

        File outputFolder = new File(args[6]);

        NativeLibraryLoader.loadVtkLibraries();

        DEMModel dem = null;
        try
        {
            dem = new DEMModel(mapletFile);
        }
        catch (Exception e)
        {
            System.out.println("An error occurred loading the maplet file. Check that the maplet file exists and is in the correct format.");
            System.exit(1);
        }

        ModelConfig config = ModelFactory.getModelConfig(ModelFactory.EROS, ModelFactory.GASKELL);
        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(config);
        LidarSearchDataCollection lidarModel = (LidarSearchDataCollection) ModelFactory.
                createLidarModels(smallBodyModel).get(ModelNames.LIDAR_SEARCH);
        AbstractEllipsePolygonModel selectionModel = new CircleSelectionModel(smallBodyModel);

        selectionModel.addNewStructure(dem.getCenter(), dem.getBoundingBoxDiagonalLength()/2.0, 1.0, 0.0);

        EllipsePolygon region = (EllipsePolygon)selectionModel.getStructure(0);

        TreeSet<Integer> cubeList = smallBodyModel.getIntersectingCubes(new BoundingBox(region.interiorPolyData.GetBounds()));

        String source = (String)config.lidarSearchDataSourceMap.keySet().toArray()[0];
        try
        {
            lidarModel.setLidarData(
                    source,
                    startDate,
                    endDate,
                    cubeList,
                    new PointInDEMChecker(dem, minDistanceFromBoundary),
                    Math.round(1000.0*timeSeparationBetweenTracks), // convert to milliseconds
                    minTrackLength);

            lidarModel.saveAllVisibleTracksToFolder(outputFolder, false);
        }
        catch (Exception e)
        {
            System.out.println("An error saving the tracks to disk.");
            System.exit(1);
        }

    }

}
