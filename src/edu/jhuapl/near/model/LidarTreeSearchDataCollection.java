package edu.jhuapl.near.model;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import vtk.vtkBoxClipDataSet;
import vtk.vtkCell;
import vtk.vtkCubeSource;
import vtk.vtkSelectEnclosedPoints;
import vtk.vtkStringArray;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridReader;

import edu.jhuapl.near.lidar.test.BasicLidarPoint;
import edu.jhuapl.near.lidar.test.OlaOctreePoint;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;

public class LidarTreeSearchDataCollection extends LidarSearchDataCollection
{
    public enum TrackFileType
    {
        TEXT,
        BINARY,
        OLA_LEVEL_2
    };

    vtkUnstructuredGrid leaves;
    vtkSelectEnclosedPoints enclosedPoints;
    Multimap<Integer, Integer> vertexToCellMap=ArrayListMultimap.create();

    public LidarTreeSearchDataCollection(SmallBodyModel smallBodyModel)
    {
        super(smallBodyModel);
        readVtkLeafFile();
        createVertexToCellMap();
        initSearchStructure();
    }

    void readVtkLeafFile()
    {
        String path=Paths.get(getLidarDataSourceMap().get("TreeBased")).resolve("tree.vtk").toString();
        vtkUnstructuredGridReader reader=new vtkUnstructuredGridReader();
        File file=FileCache.getFileFromServer(path);
        reader.SetFileName(file.getAbsolutePath());
        reader.Update();
        leaves=reader.GetOutput();
    }

    void initSearchStructure()
    {
        enclosedPoints=new vtkSelectEnclosedPoints();
        enclosedPoints.SetInputData(leaves);
    }

    void createVertexToCellMap()
    {
        for (int i=0; i<leaves.GetNumberOfCells(); i++)
        {
            vtkCell cell=leaves.GetCell(i);
            for (int j=0; j<cell.GetNumberOfPoints(); j++)
                vertexToCellMap.put(cell.GetPointIds().GetId(j), i);
        }
    }

    public TreeSet<Integer> getTreeLeavesIntersectingBoundingBox(BoundingBox bbox)
    {
        TreeSet<Integer> cellIndexSet=Sets.newTreeSet();
        vtkCubeSource cube = new vtkCubeSource();
        cube.SetBounds(bbox.getBounds());
        cube.Update();
        enclosedPoints.SetSurfaceData(cube.GetOutput());
        enclosedPoints.Update();
        vtkBoxClipDataSet clipper=new vtkBoxClipDataSet();
        clipper.SetBoxClip(bbox.xmin, bbox.xmax, bbox.ymin, bbox.ymax, bbox.zmin, bbox.zmax);
        clipper.SetInputData(leaves);
        clipper.Update();
        for (int vidx=0; vidx<leaves.GetNumberOfPoints(); vidx++)
        {
            if (enclosedPoints.IsInside(vidx)!=0)
            {
                Collection<Integer> cellsUsingPoint=vertexToCellMap.get(vidx);
                for (Integer cidx : cellsUsingPoint)
                    if (!cellIndexSet.contains(cidx))
                        cellIndexSet.add(cidx);
            }
        }
        return cellIndexSet;
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

        vtkStringArray pathArray=(vtkStringArray)leaves.GetCellData().GetAbstractArray(0);
        for (Integer cidx : cubeList)
        {
            Path leafPath=Paths.get(pathArray.GetValue(cidx));
            System.out.println("Loading data partition #"+cidx+" \""+leafPath+"\"");
            Path dataFilePath=leafPath.resolve("data");
            File dataFile=FileCache.getFileFromServer(dataFilePath.toString());
            originalPoints.addAll(readDataFile(dataFile,pointInRegionChecker));
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

    List<BasicLidarPoint> readDataFile(File dataInputFile, PointInRegionChecker pointInRegionChecker) {
        List<BasicLidarPoint> pts=Lists.newArrayList();
        try {
            DataInputStream stream=new DataInputStream(new FileInputStream(dataInputFile));
            while (stream.skipBytes(0)==0) {  // dirty trick to keep reading until EOF
                OlaOctreePoint pt=new OlaOctreePoint(stream);
                if (!pt.isFullyRead())
                    break;
                if (pointInRegionChecker.checkPointIsInRegion(pt.getTargetPositionAsArray()))
                    pts.add(pt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pts;
    }

}
