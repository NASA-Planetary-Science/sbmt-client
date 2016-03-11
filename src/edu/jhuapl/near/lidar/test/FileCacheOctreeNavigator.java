package edu.jhuapl.near.lidar.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import edu.jhuapl.near.util.BoundingBox;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileCache.FileInfo;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class FileCacheOctreeNavigator
{
    Path rootDirectory;
    SimpleFileSystemOctreeNode root;

    class SimpleFileSystemOctreeNode extends BoundingBox {
        boolean isLeaf=true;
        SimpleFileSystemOctreeNode[] children=new SimpleFileSystemOctreeNode[8];
        Path path;
        Path dataFilePath;

        public SimpleFileSystemOctreeNode(Path path, BoundingBox bbox)    // this constructor is for creating the root node
        {
            super(bbox.getBounds());
            this.path=path;
            isLeaf=true;
            setupDataFilePath();
        }

        public SimpleFileSystemOctreeNode(SimpleFileSystemOctreeNode parent, int whichChild)  // this constructor is for creating child nodes
        {
            super(FileSystemOctreeNode.createBoundingBox(parent, whichChild).getBounds());
            path=parent.path.resolve(String.valueOf(whichChild));
            isLeaf=true;
            setupDataFilePath();
        }

        private void setupDataFilePath() {
            String filePath=path.resolve("data").toString();
            FileInfo info=FileCache.getFileInfoFromServer(filePath);
            if (info.file.exists())
                FileCache.getFileFromServer(filePath);
            else
                dataFilePath=null;
        }

        public Path getChildPath(int whichChild) {
            return path.resolve(String.valueOf(whichChild));
        }

        public boolean childExistsOnDisk(int whichChild) {
            String filePath=path.resolve(String.valueOf(whichChild)).toString();
            FileInfo info=FileCache.getFileInfoFromServer(filePath);
            return info.needToDownload;
        }

    }

    public FileCacheOctreeNavigator(Path rootDirectory, BoundingBox bbox)
    {
        this.rootDirectory=rootDirectory;
        root=new SimpleFileSystemOctreeNode(rootDirectory, bbox);
        buildFromDisk(root);
    }

    void buildFromDisk(SimpleFileSystemOctreeNode node) {
        for (int i=0; i<8; i++)
            if (node.childExistsOnDisk(i)) {
                node.isLeaf=false;
                node.children[i]=new SimpleFileSystemOctreeNode(node, i);
                buildFromDisk(node.children[i]);
            }
    }

    public List<SimpleFileSystemOctreeNode> getAllLeavesIntersectingBoundingBox(BoundingBox bbox) {
        List<SimpleFileSystemOctreeNode> nodeList=Lists.newArrayList();
        getAllLeavesIntersectingBoundingBox(root, bbox, nodeList);
        return nodeList;
    }

    void getAllLeavesIntersectingBoundingBox(SimpleFileSystemOctreeNode node, BoundingBox bbox, List<SimpleFileSystemOctreeNode> nodeList) {
        if (!node.isLeaf) {
            for (int i=0; i<8; i++)
                if (node.children[i]!=null)
                    getAllLeavesIntersectingBoundingBox(node.children[i],bbox,nodeList);
        }
        else {
            if (node.dataFilePath!=null)
                nodeList.add(node);
        }
    }

    OlaPointList getAllPointsInBoundingBox(BoundingBox bbox) {
        OlaPointList pointList=new OlaPointList();
        List<SimpleFileSystemOctreeNode> nodeList=getAllLeavesIntersectingBoundingBox(bbox);
        for (SimpleFileSystemOctreeNode node : nodeList) {
            //System.out.println(node.dataFilePath);
            pointList.appendFromTreeFilePath(node.dataFilePath);
        }
        return pointList;
    }

    public long getFileSizeEstimateForLeavesInMB(List<SimpleFileSystemOctreeNode> nodeList) {
        long totalBytes=0;
        for (SimpleFileSystemOctreeNode node : nodeList)
            totalBytes+=node.dataFilePath.toFile().length();
        return totalBytes/(1024*1024);  // 1024*1024 bytes in a MB
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        BoundingBox bbox=new BoundingBox(new double[]{-1,1,-1,1,-1,1});
        Path rootDirectory=Paths.get("/Volumes/dumbledore/sbmt/tree");
        //
        Stopwatch sw=new Stopwatch();
        sw.start();
        FileCacheOctreeNavigator navigator=new FileCacheOctreeNavigator(rootDirectory, bbox);
        System.out.println("Created tree in "+sw.elapsedMillis()+" ms");
        sw.reset();
        //
        BoundingBox selectionBox=bbox;//new BoundingBox(new double[]{0,0.25,0,0.25,0,0.25});
        sw.start();
        List<SimpleFileSystemOctreeNode> nodeList=navigator.getAllLeavesIntersectingBoundingBox(selectionBox);
        System.out.println("Found "+nodeList.size()+" nodes in "+sw.elapsedMillis()+" ms");
        sw.reset();
        //
        System.out.println("File size estimate is "+navigator.getFileSizeEstimateForLeavesInMB(nodeList)+" MB");
        //
        sw.start();
        OlaPointList pointList=navigator.getAllPointsInBoundingBox(selectionBox);
        System.out.println("Found "+pointList.getNumberOfPoints()+" points in "+sw.elapsedMillis()+" ms");
    }
}
