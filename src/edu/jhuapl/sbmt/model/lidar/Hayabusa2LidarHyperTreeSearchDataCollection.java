package edu.jhuapl.sbmt.model.lidar;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import vtk.vtkCellArray;
import vtk.vtkPoints;

import edu.jhuapl.saavtk.gui.ProgressBarSwingWorker;
import edu.jhuapl.saavtk.model.PointInRegionChecker;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.lidar.LidarPoint;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;
import edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2LidarHypertreeSkeleton;
import edu.jhuapl.sbmt.lidar.hyperoctree.hayabusa2.Hayabusa2LidarPoint;

public class Hayabusa2LidarHyperTreeSearchDataCollection extends LidarSearchDataCollection
{
    public enum TrackFileType
    {
        TEXT
    };

    private Map<String, Hayabusa2LidarHypertreeSkeleton> skeletons = new HashMap<String, Hayabusa2LidarHypertreeSkeleton>();
    private Hayabusa2LidarHypertreeSkeleton currentSkeleton;
    private JComponent parentForProgressMonitor;
    private boolean loading=false;

    @Override
    public boolean isLoading()
    {
        return loading;
    }

    public Hayabusa2LidarHyperTreeSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
    }

    public void clearDatasourceSkeletons()
    {
        skeletons.clear();
    }

    /**
     * Creates a skeleton for the specified datasource name, assumes the data source path for the name
     * is already added to the lidarDatasourceMap
     * @param datasourceName
     */
    public void addDatasourceSkeleton(String datasourceName, String datasourcePath)
    {
        if (datasourceName != null && datasourceName.length() > 0)
        {
            System.out.println("Adding datasource: " + datasourceName + " - " + datasourcePath);
            Path basePath = Paths.get(datasourcePath);
            Hayabusa2LidarHypertreeSkeleton skeleton = skeletons.get(datasourceName);
            if (skeleton == null)
            {
                skeleton = new Hayabusa2LidarHypertreeSkeleton(basePath);
                skeletons.put(datasourceName, skeleton);
            }
        }
    }

    public void setCurrentDatasourceSkeleton(String datasourceName)
    {
        if (datasourceName != null && datasourceName.length() > 0)
        {
            System.out.println("Setting current datasource: " + datasourceName);
            Hayabusa2LidarHypertreeSkeleton skeleton = skeletons.get(datasourceName);
            if (skeleton != null)
                currentSkeleton = skeleton;
        }
    }

    private Set<FSHyperTreeSkeleton> readIn = new HashSet<FSHyperTreeSkeleton>();

    public void readSkeleton()
    {
        if (!readIn.contains(currentSkeleton))
        {
            currentSkeleton.read();
            readIn.add(currentSkeleton);
        }
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(BoundingBox bbox, double[] tlims)
    {
        double[] bounds=new double[]{bbox.xmin,bbox.xmax,bbox.ymin,bbox.ymax,bbox.zmin,bbox.zmax,tlims[0],tlims[1]};
        return currentSkeleton.getLeavesIntersectingBoundingBox(bounds);
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(BoundingBox bbox, double[] tlims, double[] scrangeLims)
    {
        double[] bounds=new double[]{bbox.xmin,bbox.xmax,bbox.ymin,bbox.ymax,bbox.zmin,bbox.zmax,tlims[0],tlims[1],scrangeLims[0],scrangeLims[1]};
        return currentSkeleton.getLeavesIntersectingBoundingBox(bounds);
    }

    public void setParentForProgressMonitor(JComponent component)
    {
        this.parentForProgressMonitor=component;
    }


    @Override
    public void setLidarData(String dataSource, final double startDate,
            final double stopDate, final TreeSet<Integer> cubeList,
            final PointInRegionChecker pointInRegionChecker,
            double timeSeparationBetweenTracks, int minTrackLength, final double minRange, final double maxRange)
                    throws IOException, ParseException
    {
        // In the old LidarSearchDataCollection class the cubeList came from a predetermined set of cubes all of equal size.
        // Here it corresponds to the list of leaves of an octree that intersect the bounding box of the user selection area.

        ProgressBarSwingWorker dataLoader=new ProgressBarSwingWorker(parentForProgressMonitor,"Loading Hayabusa2 Lidar datapoints ("+cubeList.size()+" individual chunks)")
        {
            @Override
            protected Void doInBackground() throws Exception
           {
                Stopwatch sw=new Stopwatch();
                sw.start();
                loading=true;

//                originalPoints.clear();
                int cnt=0;
                for (Integer cidx : cubeList)
                {
                    Path leafPath=currentSkeleton.getNodeById(cidx).getPath();
                    System.out.println("Hayabusa2LidarSearchDataCollection: setLidarData: Loading data partition "+(cnt+1)+"/"+cubeList.size()+" (id="+cidx+") \""+leafPath+"\"");
                    Path dataFilePath=leafPath.resolve("data");
                    File dataFile=FileCache.getFileFromServer(dataFilePath.toString());
                    if (!dataFile.exists())
                        dataFile=FileCache.getFileFromServer(FileCache.FILE_PREFIX+dataFilePath.toString());
                    List<LidarPoint> pts=readDataFile(dataFile,pointInRegionChecker,new double[]{startDate,stopDate});
                    for (int i=0; i<pts.size(); i++)
                    {

                        LidarPoint currPt = pts.get(i);
                        if (currPt.getRangeToSC() > minRange && currPt.getRangeToSC() < maxRange) {
                            originalPoints.add(pts.get(i));
                            originalPointsSourceFiles.put(pts.get(i),((Hayabusa2LidarPoint)pts.get(i)).getFileNum());
                        }
                    }
                    //
                    cnt++;
                    double progressPercentage=((double)cnt/(double)cubeList.size()*100);
                   setProgress((int)progressPercentage);
                    if (isCancelled())
                        break;
               }
                cancel(true);
                loading=false;

                System.out.println("Data Reading Time="+sw.elapsedMillis()+" ms");
                sw.reset();
                sw.start();

                return null;
           }

        };
        dataLoader.executeDialog();
        initTranslationArray(originalPoints.size());
        System.out.println(
                "Hayabusa2LidarSearchDataCollection: setLidarData: before while loop");

                    radialOffset = 0.0;

                    computeTracks();

                    Stopwatch sw=new Stopwatch();
                    System.out.println("Hayabusa2LidarSearchDataCollection: setLidarData:  Compute Track Time="+sw.elapsedMillis()+" ms");
                    sw.reset();
                    sw.start();

                    removeTracksThatAreTooSmall();

                    // sometimes the last track ends up with bad times because the user cancelled the search, so remove any that are bad in this respect
                    List<Track> tracksToRemove=Lists.newArrayList();
                    for (Track t : tracks)
                        if (t.timeRange[0].length()==0 || t.timeRange[1].length()==0)
                            tracksToRemove.add(t);
                    for (Track t : tracksToRemove)
                        tracks.remove(t);

                    System.out.println("Remove Small Tracks Time="+sw.elapsedMillis()+" ms");
                    sw.reset();
                    sw.start();

                    assignInitialColorToTrack();

                    System.out.println("Assign Initial Colors Time="+sw.elapsedMillis()+" ms");
                    sw.reset();
                    sw.start();


                    updateTrackPolydata();

                    System.out.println("UpdatePolyData Time="+sw.elapsedMillis()+" ms");


            selectPoint(-1);

            pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    static vtkPoints points=new vtkPoints();
    static vtkCellArray cells=new vtkCellArray();

    public static List<LidarPoint> readDataFile(File dataInputFile, PointInRegionChecker pointInRegionChecker, double[] timeLimits) {
        List<LidarPoint> pts=Lists.newArrayList();
        try {
            DataInputStream stream=new DataInputStream(new FileInputStream(dataInputFile));
            while (true) {
                Hayabusa2LidarPoint pt=new Hayabusa2LidarPoint(stream);
                if (pt.getTime()<timeLimits[0] || pt.getTime()>timeLimits[1])   // throw away points outside time limits
                    continue;
                if (pointInRegionChecker!=null)
                {
                    if (pointInRegionChecker.checkPointIsInRegion(pt.getTargetPosition().toArray()))
                        pts.add(pt);    // if region checker exists then filter on space as well as time
                    continue;
                }
                //
/*                int id=points.InsertNextPoint(pt.getTargetPosition().toArray());
                vtkVertex vert=new vtkVertex();
                vert.GetPointIds().SetId(0, id);
                cells.InsertNextCell(vert);*/
                pts.add(pt);    // if the region checker does not exist and the point is within the time limits then add it
            }
        } catch (IOException e)
        {
            if (!e.getClass().equals(EOFException.class))
                e.printStackTrace();
        }
/*        //
        vtkPolyData polyData=new vtkPolyData();
        polyData.SetPoints(points);
        polyData.SetVerts(cells);
        //
        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
        writer.SetFileTypeToBinary();
        writer.SetInputData(polyData);
        writer.Write();*/
        return pts;
    }

    @Override
    protected void computeTracks()
    {
        localFileMap.clear();
        localFileMap.putAll(getCurrentSkeleton().getFileMap());
        super.computeTracks();
        //
        for (int i=0; i<tracks.size(); i++)
        {
            Track track=tracks.get(i);
            for (int j=track.startId; j<=track.stopId; j++)
                track.registerSourceFileIndex(((Hayabusa2LidarPoint)originalPoints.get(j)).getFileNum(), getCurrentSkeleton().getFileMap());
        }
    }
    public Hayabusa2LidarHypertreeSkeleton getCurrentSkeleton()
    {
        return currentSkeleton;
    }

}
