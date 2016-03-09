package edu.jhuapl.near.lidar.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import vtk.vtkCellArray;
import vtk.vtkHexahedron;
import vtk.vtkPoints;
import vtk.vtkUnstructuredGrid;
import vtk.vtkUnstructuredGridWriter;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.bennu.Bennu;
import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class FileSystemOctreeGenerator
{

    final Path outputDirectory;
    final int maxNumberOfPointsPerLeaf;
    final BoundingBox boundingBox;
    final int maxNumberOfOpenFiles;
    FileSystemOctreeNode root;
    final DataOutputStreamPool streamManager;
    long totalPointsWritten=0;

    public FileSystemOctreeGenerator(Path outputDirectory, int maxNumberOfPointsPerLeaf, BoundingBox bbox, int maxNumFiles) throws IOException
    {
        this.outputDirectory=outputDirectory;
        this.maxNumberOfPointsPerLeaf=maxNumberOfPointsPerLeaf;
        boundingBox=bbox;
        maxNumberOfOpenFiles=maxNumFiles;
        streamManager=new DataOutputStreamPool(maxNumberOfOpenFiles);
        root=new FileSystemOctreeNode(outputDirectory, bbox, maxNumberOfPointsPerLeaf, streamManager);
    }


    public void addPointsFromFile(Path inputFilePath) throws IOException {
        addPointsFromFile(inputFilePath, Integer.MAX_VALUE);
    }

    public void addPointsFromFile(Path inputFilePath, int nmax) throws IOException {
        OlaPointList pointList=new OlaPointList();
        pointList.appendFromL2File(inputFilePath);
        int limit=Math.min(pointList.getNumberOfPoints(),nmax);
        for (int i=0; i<limit; i++) {
            if ((i%200000)==0)
                System.out.println((int)((double)i/(double)pointList.getNumberOfPoints()*100)+"% complete : "+i+"/"+limit);
            root.addPoint(new OlaOctreePoint(pointList.getPoint(i)));
            totalPointsWritten++;
        }
    }



    public int getNumberOfNodes() {
        return getAllNodes().size();
    }

    public List<FileSystemOctreeNode> getAllNodes() {
        List<FileSystemOctreeNode> nodeList=Lists.newArrayList();
        getAllNodes(root, nodeList);
        return nodeList;
    }

    void getAllNodes(FileSystemOctreeNode node, List<FileSystemOctreeNode> nodeList) {
        nodeList.add(node);
        for (int i=0; i<8; i++)
            if (node.children[i]!=null)
                nodeList.add(node.children[i]);
    }

    public vtkUnstructuredGrid getAllNonEmptyLeavesAsUnstructuredGrid() {
        List<FileSystemOctreeNode> nodeList=getAllNonEmptyLeafNodes();
        vtkPoints points=new vtkPoints();
        vtkCellArray cells=new vtkCellArray();
        for (FileSystemOctreeNode node : nodeList) {
            vtkHexahedron hex=new vtkHexahedron();
            for (int i=0; i<8; i++) {
                Vector3D crn=node.getCorner(i);
                int id=points.InsertNextPoint(crn.getX(),crn.getY(),crn.getZ());
                hex.GetPointIds().SetId(i,id);
            }
            cells.InsertNextCell(hex);
        }
        //
        vtkUnstructuredGrid grid=new vtkUnstructuredGrid();
        grid.SetPoints(points);
        grid.SetCells(new vtkHexahedron().GetCellType(), cells);
        return grid;
    }

    public List<FileSystemOctreeNode> getAllNonEmptyLeafNodes() {
        List<FileSystemOctreeNode> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(root, nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(FileSystemOctreeNode node, List<FileSystemOctreeNode> nodeList) {
        if (!node.isLeaf)
            for (int i=0; i<8; i++)
                getAllNonEmptyLeafNodes(node.children[i], nodeList);
        else if (node.numPoints>0)
            nodeList.add(node);
    }

    public void finalCommit() throws IOException {
        streamManager.closeAllStreams();// close any files that are still open
        finalCommit(root);
    }

    void finalCommit(FileSystemOctreeNode node) throws IOException {
        if (!node.isLeaf)
            for (int i=0; i<8; i++)
                finalCommit(node.children[i]);
        else {
            File dataFile=node.getDataFilePath().toFile();  // clean up any data files with zero points
            if (dataFile.length()==0l)
                dataFile.delete();
        }
    }

    interface OctreePoint {
        public Vector3D getPosition();
        public void writeToStream(DataOutputStream stream) throws IOException;
        public void readFromStream(DataInputStream stream) throws IOException;
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length!=3) {
            System.out.println("Arguments:");
            System.out.println("   OLA L2 file directory");
            System.out.println("   Desired root directory for tree");
            System.out.println("   Desired max file size (MB)");
            return;
        }

        Path olaL2FileDirectory=Paths.get(args[0]);
        Path treeRootDirectory=Paths.get(args[1]);  //Paths.get("/Volumes/dumbledore/sbmt/tree")
        int dataFileMBLimit=Integer.valueOf(args[2]);
        System.out.println("OLA L2 file directory = "+olaL2FileDirectory);
        System.out.println("Tree root directory = "+treeRootDirectory);
        System.out.println("Desired max file size = "+dataFileMBLimit+" MB");
        System.out.println();

        //
        NativeLibraryLoader.loadVtkLibrariesHeadless();
        int megaByte=1024*1024;
        int dataFileByteLimit=dataFileMBLimit*megaByte;
        int maxPointsPerLeaf=dataFileByteLimit/(8*4);   // three doubles for scpos, three doubles for tgpos, one double for time, and one double for intensity
        int maxNumFiles=512;
        //
        Configuration.setAPLVersion(true);
        ShapeModelBody body=ShapeModelBody.RQ36;
        ShapeModelAuthor author=ShapeModelAuthor.GASKELL;
        String version="V3 Image";
        Bennu bennu = new Bennu(SmallBodyConfig.getSmallBodyConfig(body,author,version));
        BoundingBox bbox=new BoundingBox(bennu.getBoundingBox().getBounds());
        System.out.println("Shape model info:");
        System.out.println("  Body = "+body);
        System.out.println("  Author = "+author);
        System.out.println("  Version = \""+version+"\"");
        System.out.println("Original bounding box = "+bbox);
        double bboxSizeIncrease=0.05;
        bbox.increaseSize(bboxSizeIncrease);
        System.out.println("Bounding box diagonal length increase = "+bboxSizeIncrease);
        System.out.println("Rescaled bounding box = "+bbox);
        System.out.println();
        FileSystemOctreeGenerator tree=new FileSystemOctreeGenerator(treeRootDirectory, maxPointsPerLeaf, bbox, maxNumFiles);
        //
        List<File> fileList=Lists.newArrayList();
        Collection<File> fileCollection=FileUtils.listFiles(olaL2FileDirectory.toFile(), new WildcardFileFilter("OBJLIST*.l2"), null);
        for (File f : fileCollection)
            fileList.add(f);
        //
        Stopwatch sw=new Stopwatch();
        int numFiles=fileList.size();
        for (int i=0; i<numFiles; i++) {
            sw.start();
            Path inputPath=Paths.get(fileList.get(i).toString());
            System.out.println("File "+(i+1)+"/"+numFiles+": "+inputPath);
            tree.addPointsFromFile(inputPath);
            System.out.println("Elapsed time = "+sw.elapsedTime(TimeUnit.SECONDS)+" s");
            System.out.println(tree.totalPointsWritten+" total points written to disk so far");// TODO: close down all DataOutputStreams
            System.out.println();
            sw.reset();
        }
        tree.finalCommit(); // clean up any empty or open data files

        //
        vtkUnstructuredGrid grid=tree.getAllNonEmptyLeavesAsUnstructuredGrid();
        vtkUnstructuredGridWriter writer=new vtkUnstructuredGridWriter();
        writer.SetFileName(tree.outputDirectory.resolve("tree.vtk").toString());
        writer.SetFileTypeToBinary();
        writer.SetInputData(grid);
        writer.Write();

        System.out.println("Total # of leaves="+grid.GetNumberOfCells());
    }

}
