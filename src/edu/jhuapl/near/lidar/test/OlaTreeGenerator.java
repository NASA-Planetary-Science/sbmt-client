package edu.jhuapl.near.lidar.test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import vtk.vtkIdTypeArray;
import vtk.vtkKdTree;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.near.util.NativeLibraryLoader;

public class OlaTreeGenerator
{
    //public static Bennu bennu;// = new Bennu(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"));
    static final String rawFileWildcardString="OBJLIST*.l2";
    static final long approximateNumberOfPointsPerTree=10000000;
    static final int approximateNumberOfLeaves=10000;
    static final int numberOfRegionsPerSubDirectory=500;

    public OlaTreeGenerator()
    {
        //bennu = new Bennu(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"));
    }

    public Path getOutputPath()
    {
        return Paths.get("/Volumes/dumbledore/sbmt/tree");
        //To run on DMZ:
        //return "/project/sbmtpipeline/processed/osirisrex/OLA/cubes";
    }

    List<File> getRawFiles(Path inputPath) {
        List<File> fileList=Lists.newArrayList();
        Collection<File> fileCollection=FileUtils.listFiles(inputPath.toFile(), new WildcardFileFilter(rawFileWildcardString), null);
        for (File f : fileCollection)
            fileList.add(f);
        return fileList;
    }

    public olaTree generateTree(OlaPointList pointList) {
        olaVtkPoints olaPoints=new olaVtkPoints(pointList);
        olaTree tree=new olaTree();
        tree.SetNumberOfRegionsOrLess(approximateNumberOfLeaves);
        tree.BuildLocatorFromPoints(olaPoints);
        System.out.println(" --- built tree with "+tree.GetNumberOfRegions()+" regions");
        return tree;
    }

    // TODO: write OLA points from tree leaves into files
    // TODO: create searchable polydata for spatial queries

    void partitionRawDataFiles(Path inputDirectory) {
        OlaPointList pointList=new OlaPointList();
        List<File> l2Files=getRawFiles(inputDirectory);
        int partitionCount=0;
        for (int f=0; f<l2Files.size(); f++) {
            Path l2FilePath=Paths.get(l2Files.get(f).getAbsolutePath());
            System.out.println(f+"/"+l2Files.size()+": "+l2FilePath);
            pointList.appendFromPath(l2FilePath);
            if (pointList.getNumberOfPoints()>approximateNumberOfPointsPerTree || f==l2Files.size()-1) {   // have to add last batch to partition list
                System.out.println("********** # points="+pointList.getNumberOfPoints());
                Path outputFilePath=getOutputPath().resolve(getPartitionDirectoryString(partitionCount));
                outputFilePath.toFile().mkdir();
                commitTreeToDisk(outputFilePath, generateTree(pointList));
                partitionCount++;
                pointList.clear();
            }
        }
    }

    private String getPartitionVtkFileName(int whichPartition) {
        return "partition_"+whichPartition+".vtk";
    }

    private String getPartitionDirectoryString(int whichPartition) {
        return "partition_"+whichPartition;
    }

    private String getPartitionSubDirectoryString(int partitionRegionCount) {
        return String.valueOf(partitionRegionCount);
    }

    void commitTreeToDisk(Path partitionOutputDirectory, olaTree tree) {
        int partitionCount=0;
        partitionOutputDirectory.resolve(getPartitionSubDirectoryString(partitionCount)).toFile().mkdir();
        int regionCount=0;
        for (int i=0; i<tree.GetNumberOfRegions(); i++) {
            Path outputFilePath=partitionOutputDirectory.resolve(getPartitionSubDirectoryString(partitionCount));
            writeRegionAsBinary(outputFilePath, tree, i);
            regionCount++;
            if (regionCount>numberOfRegionsPerSubDirectory) {
                partitionCount++;
                partitionOutputDirectory.resolve(getPartitionSubDirectoryString(partitionCount)).toFile().mkdir();
                regionCount=0;
            }
        }
        //
        writeTreeLeavesAsPolyData(partitionOutputDirectory.resolve(getPartitionVtkFileName(partitionCount)), tree);
    }

    void writeTreeLeavesAsPolyData(Path outputFilePath, olaTree tree) {
        vtkPolyData polyData=new vtkPolyData();
        tree.GenerateRepresentation(tree.GetLevel(), polyData);
        vtkPolyDataWriter writer=new vtkPolyDataWriter();
        writer.SetInputData(polyData);
        writer.SetFileTypeToBinary();
        writer.SetFileName(outputFilePath.toString());
        writer.Write();
    }

    void writeRegionAsBinary(Path outputFilePath, olaTree tree, int region) {
        try
        {
            DataOutputStream stream=new DataOutputStream(new FileOutputStream(outputFilePath.toFile()));
            olaVtkPoints points=(olaVtkPoints)tree.getPoints();
            vtkIdTypeArray ids=tree.GetPointsInRegion(region);
            stream.writeInt(ids.GetNumberOfTuples());
            for (int i=0; i<ids.GetNumberOfTuples(); i++) {
                OlaPoint pt=points.getRawData(i);
                stream.writeDouble(pt.tgpos.getX());
                stream.writeDouble(pt.tgpos.getY());
                stream.writeDouble(pt.tgpos.getZ());
                stream.writeDouble(pt.scpos.getX());
                stream.writeDouble(pt.scpos.getY());
                stream.writeDouble(pt.scpos.getZ());
                stream.writeDouble(pt.time);
                stream.writeDouble(pt.intensity);
            }
            stream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class olaVtkPoints extends vtkPoints {  // this allows one to construct vtkPoints from target positions in an OlaPointList while keeping references to the full original data points

        OlaPointList pointList;

        public OlaPoint getRawData(int i) {
            return pointList.getPoint(i);
        }

        public olaVtkPoints(OlaPointList pointList)
        {
            this.pointList=pointList;
            for (int i=0; i<pointList.getNumberOfPoints(); i++) {
                Vector3D tgpos=pointList.getPoint(i).tgpos;
                InsertNextPoint(tgpos.getX(), tgpos.getY(), tgpos.getZ());
            }
        }
    }

    class olaTree extends vtkKdTree {   // expose vtkPoints used to create the tree

        vtkPoints points;

        public void BuildLocatorFromPoints(vtkPoints id0) {
            points=id0;
        };

        vtkPoints getPoints() {
            return points;
        }
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        Path inputDirectory=Paths.get("/Volumes/dumbledore/sbmt/OLA");
        OlaTreeGenerator generator=new OlaTreeGenerator();
        generator.partitionRawDataFiles(inputDirectory);
    }

}
