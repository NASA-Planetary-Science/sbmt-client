package edu.jhuapl.near.model;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.near.lidar.hyperoctree.OlaFSHyperPoint;
import edu.jhuapl.near.lidar.hyperoctree.OlaFSHyperTreeSkeleton;
import edu.jhuapl.near.lidar.test.LidarPoint;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;

public class LidarHyperTreeSearchDataCollection extends LidarSearchDataCollection
{
    public enum TrackFileType
    {
        TEXT,
        BINARY,
        OLA_LEVEL_2
    };

    OlaFSHyperTreeSkeleton skeleton;

    public LidarHyperTreeSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
        Path basePath=Paths.get(getLidarDataSourceMap().get("Local Test Data"));
        Path skeletonFile=basePath.resolve("skeleton.txt");
        skeleton=new OlaFSHyperTreeSkeleton(Paths.get(getLidarDataSourceMap().get("Local Test Data")));
        File f=FileCache.getFileFromServer(skeletonFile.toString());
        skeleton.read(f.toPath());
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(BoundingBox bbox, double[] tlims)
    {
        double[] bounds=new double[]{bbox.xmin,bbox.xmax,bbox.ymin,bbox.ymax,bbox.zmin,bbox.zmax,tlims[0],tlims[1]};
        return skeleton.getLeavesIntersectingBoundingBox(bounds);
    }

    @Override
    public void setLidarData(String dataSource, double startDate,
            double stopDate, TreeSet<Integer> cubeList,
            PointInRegionChecker pointInRegionChecker,
            double timeSeparationBetweenTracks, int minTrackLength)
                    throws IOException, ParseException
    {
        // In the old LidarSearchDataCollection class the cubeList came from a predetermined set of cubes all of equal size.
        // Here it corresponds to the list of leaves of an octree that intersect the bounding box of the user selection area.

        Stopwatch sw=new Stopwatch();
        sw.start();

        int cnt=0;
        for (Integer cidx : cubeList)
        {
            Path leafPath=skeleton.getNodeById(cidx).getPath();
            System.out.println("Loading data partition "+(++cnt)+"/"+cubeList.size()+" (id="+cidx+") \""+leafPath+"\"");
            Path dataFilePath=leafPath.resolve("data");
            File dataFile=FileCache.getFileFromServer(dataFilePath.toString());
            originalPoints.addAll(readDataFile(dataFile,pointInRegionChecker,new double[]{startDate,stopDate}));
        }

        System.out.println("Data Reading Time="+sw.elapsedMillis()+" ms");
        sw.reset();

        // Sort points in time order
        Collections.sort(originalPoints);

        System.out.println("Sorting Time="+sw.elapsedMillis()+" ms");
        sw.reset();

        radialOffset = 0.0;
        translation[0] = translation[1] = translation[2] = 0.0;

        computeTracks();

        System.out.println("Compute Track Time="+sw.elapsedMillis()+" ms");
        sw.reset();

        removeTracksThatAreTooSmall();

        System.out.println("Remove Small Tracks Time="+sw.elapsedMillis()+" ms");
        sw.reset();

        assignInitialColorToTrack();

        System.out.println("Assign Initial Colors Time="+sw.elapsedMillis()+" ms");
        sw.reset();


        updateTrackPolydata();

        System.out.println("UpdatePolyData Time="+sw.elapsedMillis()+" ms");
        sw.reset();


    }

    List<LidarPoint> readDataFile(File dataInputFile, PointInRegionChecker pointInRegionChecker, double[] timeLimits) {
        List<LidarPoint> pts=Lists.newArrayList();
        try {
            DataInputStream stream=new DataInputStream(new FileInputStream(dataInputFile));
            while (stream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaFSHyperPoint pt=new OlaFSHyperPoint(stream);
                if (pt.getTime()<timeLimits[0] || pt.getTime()>timeLimits[1])   // throw away points outside time limits
                    continue;
                if (pointInRegionChecker!=null && pointInRegionChecker.checkPointIsInRegion(pt.getTargetPosition().toArray()))
                {
                    pts.add(pt);    // if region checker exists then filter on space as well as time
                    continue;
                }
                pts.add(pt);    // if the region checker does not exist and the point is within the time limits then add it
            }
        } catch (IOException e) {
            if (!e.getClass().equals(EOFException.class))
                e.printStackTrace();
        }
        return pts;
    }

}
