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

import javax.swing.JComponent;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.near.gui.ProgressBarSwingWorker;
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
    JComponent parentForProgressMonitor;
    boolean loading=false;

    @Override
    public boolean isLoading()
    {
        return loading;
    }

    public LidarHyperTreeSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
        Path basePath=Paths.get(getLidarDataSourceMap().get("Default"));
        skeleton=new OlaFSHyperTreeSkeleton(basePath);
        skeleton.read();
    }

    public LidarHyperTreeSearchDataCollection(SmallBodyModel smallBodyModel, Path basePath)
    {
        super(smallBodyModel);
        skeleton=new OlaFSHyperTreeSkeleton(basePath);
        skeleton.read();
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(BoundingBox bbox, double[] tlims)
    {
        double[] bounds=new double[]{bbox.xmin,bbox.xmax,bbox.ymin,bbox.ymax,bbox.zmin,bbox.zmax,tlims[0],tlims[1]};
        return skeleton.getLeavesIntersectingBoundingBox(bounds);
    }

    public void setParentForProgressMonitor(JComponent component)
    {
        this.parentForProgressMonitor=component;
    }

    @Override
    public void setLidarData(String dataSource, final double startDate,
            final double stopDate, final TreeSet<Integer> cubeList,
            final PointInRegionChecker pointInRegionChecker,
            double timeSeparationBetweenTracks, int minTrackLength)
                    throws IOException, ParseException
    {
        // In the old LidarSearchDataCollection class the cubeList came from a predetermined set of cubes all of equal size.
        // Here it corresponds to the list of leaves of an octree that intersect the bounding box of the user selection area.

        ProgressBarSwingWorker dataLoader=new ProgressBarSwingWorker(parentForProgressMonitor,"Loading OLA datapoints")
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                Stopwatch sw=new Stopwatch();
                sw.start();
                loading=true;

                int cnt=0;
                for (Integer cidx : cubeList)
                {
                    Path leafPath=skeleton.getNodeById(cidx).getPath();
                    System.out.println("Loading data partition "+(cnt+1)+"/"+cubeList.size()+" (id="+cidx+") \""+leafPath+"\"");
                    Path dataFilePath=leafPath.resolve("data");
                    File dataFile=FileCache.getFileFromServer(dataFilePath.toString());
                    if (!dataFile.exists())
                        dataFile=FileCache.getFileFromServer(FileCache.FILE_PREFIX+dataFilePath.toString());
                    originalPoints.addAll(readDataFile(dataFile,pointInRegionChecker,new double[]{startDate,stopDate}));
                    //
                    cnt++;
                    setProgress((int)((double)cnt/(double)cubeList.size()*100));
                    if (isCancelled())
                        break;
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

                loading=false;
                cancel(true);

                return null;
            }
        };
        dataLoader.executeDialog();
    }

    List<LidarPoint> readDataFile(File dataInputFile, PointInRegionChecker pointInRegionChecker, double[] timeLimits) {
        List<LidarPoint> pts=Lists.newArrayList();
        try {
            DataInputStream stream=new DataInputStream(new FileInputStream(dataInputFile));
            while (stream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaFSHyperPoint pt=new OlaFSHyperPoint(stream);
                if (pt.getTime()<timeLimits[0] || pt.getTime()>timeLimits[1])   // throw away points outside time limits
                    continue;
                if (pointInRegionChecker!=null)
                {
                    if (pointInRegionChecker.checkPointIsInRegion(pt.getTargetPosition().toArray()))
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
