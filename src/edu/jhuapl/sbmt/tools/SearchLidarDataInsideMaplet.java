package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.util.TreeSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.QuietSceneChangeNotifier;
import edu.jhuapl.saavtk.model.LidarDataSource;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.status.QuietStatusNotifier;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.model.DEM;
import edu.jhuapl.sbmt.dtm.service.PointInDEMChecker;
import edu.jhuapl.sbmt.lidar.LidarSearchParms;
import edu.jhuapl.sbmt.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.lidar.util.LidarFileUtil;
import edu.jhuapl.sbmt.lidar.util.LidarQueryUtil;
import edu.jhuapl.sbmt.util.TimeUtil;

public class SearchLidarDataInsideMaplet
{
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");

        if (args.length != 7)
        {
            System.out.println("Usage: SearchLidarDataInsideMaplet <maplet-file> <start-date> <end-date> <min-track-size> <track-separation-time> <min_distance-from-boundary> <output-folder>");
            System.exit(0);
        }

        String mapletFile = args[0];
        double startDate = TimeUtil.str2et(args[1]);
        double endDate = TimeUtil.str2et(args[2]);
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

        DEM dem = null;
        try
        {
            dem = new DEM(mapletFile);
        }
        catch (Exception e)
        {
            System.out.println("An error occurred loading the maplet file. Check that the maplet file exists and is in the correct format.");
            System.exit(1);
        }

        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
        LidarTrackManager trackManager = new LidarTrackManager(QuietSceneChangeNotifier.Instance, QuietStatusNotifier.Instance, smallBodyModel);
        AbstractEllipsePolygonModel selectionModel = new CircleSelectionModel(QuietSceneChangeNotifier.Instance, QuietStatusNotifier.Instance, smallBodyModel);

        Vector3D center = new Vector3D(dem.getCenter());
        selectionModel.addNewStructure(center, dem.getBoundingBoxDiagonalLength()/2.0, 1.0, 0.0);

        Ellipse region = selectionModel.getItem(0);

        TreeSet<Integer> cubeList = smallBodyModel.getIntersectingCubes(new BoundingBox(selectionModel.getVtkInteriorPolyDataFor(region).GetBounds()));

        String name = (String)config.lidarSearchDataSourceMap.keySet().toArray()[0];
        String path = (String)config.lidarSearchDataSourceMap.values().toArray()[0];
        try
        {
            LidarDataSource tmpDataSource = new LidarDataSource(name, path);
            LidarSearchParms tmpSearchParms = new LidarSearchParms(tmpDataSource, startDate, endDate, Double.NaN, Double.NaN,
				    timeSeparationBetweenTracks, minTrackLength, cubeList);
            LidarQueryUtil.executeQueryClassic(trackManager, tmpSearchParms,
                new PointInDEMChecker(dem, minDistanceFromBoundary));

            LidarFileUtil.saveTracksToFolder(trackManager, outputFolder, trackManager.getAllItems(), "track", false);
        }
        catch (Exception e)
        {
            System.out.println("An error saving the tracks to disk.");
            System.exit(1);
        }

    }

}
