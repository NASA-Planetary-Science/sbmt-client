package edu.jhuapl.near.lidar.hyperoctree;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.jhuapl.near.util.FileCache;

public class OlaFSHyperTreeSkeleton
{

    Node rootNode;
    int idCount=0;
    TreeMap<Integer, Node> nodeMap=Maps.newTreeMap(); // unfortunately this extra level of indirection is required by the "LidarSearchDataCollection" class
    Path basePath;

    public static class Node
    {
        double[] bounds;
        Path path;
        boolean isLeaf;
        Node[] children;
        int id;

        public Node(double[] bounds, Path path, boolean isLeaf, int id)
        {
            this.bounds=bounds;
            this.path=path;
            this.isLeaf=isLeaf;
            children=new Node[16];
            for (int i=0; i<16; i++)
                children[i]=null;
            this.id=id;
        }

        public boolean intersects(double[] bbox)
        {
            return bbox[0]<=bounds[1] && bbox[1]>=bounds[0] && bbox[2]<=bounds[3] && bbox[3]>=bounds[2] && bbox[4]<=bounds[5] && bbox[5]>=bounds[4] && bbox[6]<=bounds[7] && bbox[7]>=bounds[6];
        }

        public Path getPath()
        {
            return path;
        }
    }

    public OlaFSHyperTreeSkeleton(Path basePath)
    {
        this.basePath=basePath;
    }

    private double[] readBoundsFile(Path path)
    {
        File f=FileCache.getFileFromServer(path.toString());
        if (f.exists())
            return OlaFSHyperTreeNode.readBoundsFile(Paths.get(f.getAbsolutePath()), 4);
        else
            return null;
    }

    public void read()  // cf. OlaFSHyperTreeCondenser for code to write the skeleton file
    {
        Path inputFile=basePath.resolve("skeleton.txt");    // inputFile is expected to be in the root path of the tree
        File f=FileCache.getFileFromServer(inputFile.toString());
        double[] rootBounds=readBoundsFile(basePath.resolve("bounds"));
        rootNode=new Node(rootBounds,basePath,false,idCount); // false -> root is not a leaf
        nodeMap.put(rootNode.id, rootNode);
        idCount++;
        //
        try
        {
            Scanner scanner=new Scanner(f);
            readChildren(scanner, rootNode);
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readChildren(Scanner scanner, Node node)   // cf. OlaFSHyperTreeCondenser for code to write the skeleton
    {
        for (int i=0; i<16; i++)
        {
            String line=scanner.nextLine();
            String[] tokens=line.replace("\n", "").replace("\r", "").split(" ");
            Path childPath=basePath.resolve(tokens[0]);
            String childInfo=tokens[1];
            //
            if (childInfo.equals("*"))   // child does not exist
                continue;
            //
            double[] bounds=new double[8];
            for (int j=0; j<8; j++)
                bounds[j]=Double.valueOf(tokens[2+j]);
            //
            if(childInfo.equals(">"))  // child exists but is not a leaf (i.e. does not have data)
                node.children[i]=new Node(bounds, childPath, false, idCount);
            else if (childInfo.equals("d")) // child exists and is a leaf (i.e. does have data)
                node.children[i]=new Node(bounds, childPath, true, idCount);
            idCount++;
            nodeMap.put(node.children[i].id, node.children[i]);
        }
        for (int i=0; i<16; i++)
            if (node.children[i]!=null && !node.children[i].isLeaf)
            {
                readChildren(scanner, node.children[i]);
            }
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(double[] searchBounds)
    {
        TreeSet<Integer> pathList=Sets.newTreeSet();
        getLeavesIntersectingBoundingBox(rootNode, searchBounds, pathList);
        return pathList;
    }

    private void getLeavesIntersectingBoundingBox(Node node, double[] searchBounds, TreeSet<Integer> pathList)
    {
        if (node.intersects(searchBounds) && node.isLeaf)
            pathList.add(node.id);
        for (int i=0; i<16; i++)
            if (node.children[i]!=null)
                getLeavesIntersectingBoundingBox(node.children[i],searchBounds,pathList);
    }

    public Node getNodeById(int id)
    {
        return nodeMap.get(id);
    }


}
