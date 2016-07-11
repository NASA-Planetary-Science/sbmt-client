package edu.jhuapl.near.lidar.hyperoctree;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Sets;

import edu.jhuapl.near.util.NativeLibraryLoader;

public class OlaFSHyperTreeSkeleton
{

    Node rootNode;
    int idCount=0;
    TreeMap<Integer, Node> nodeMap; // unfortunately this extra level of indirection is required by the "LidarSearchDataCollection" class

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
            this.id=id;
        }

        public boolean intersects(double[] bbox)
        {
            return bbox[0]<=bounds[1] && bbox[1]>=bounds[0] && bbox[2]<=bounds[3] && bbox[3]>=bounds[2] && bbox[4]<=bounds[5] && bbox[5]>=bounds[4];
        }

        public Path getPath()
        {
            return path;
        }
    }

    public OlaFSHyperTreeSkeleton read(Path inputFile)  // cf. OlaFSHyperTreeCondenser for code to write the skeleton file
    {
        OlaFSHyperTreeSkeleton skeleton=new OlaFSHyperTreeSkeleton();
        Path rootPath=inputFile.getParent();    // inputFile is expected to be in the root path of the tree
        double[] rootBounds=OlaFSHyperTreeNode.readBoundsFile(rootPath.resolve("bounds"), 4);
        skeleton.rootNode=new Node(rootBounds,rootPath,false,idCount++); // false -> root is not a leaf
        //
        try
        {
            Scanner scanner=new Scanner(inputFile.toFile());
            readChildren(scanner, skeleton.rootNode);
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return skeleton;
    }

    private void readChildren(Scanner scanner, Node node)   // cf. OlaFSHyperTreeCondenser for code to write the skeleton
    {
        for (int i=0; i<16; i++)
        {
            String line=scanner.nextLine();
            String[] tokens=line.replace("\n", "").replace("\r", "").split(" ");
            Path childPath=Paths.get(tokens[0]);
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
                node.children[i]=new Node(bounds, childPath, false, idCount++);
            else if (childInfo.equals("d")) // child exists and is a leaf (i.e. does have data)
                node.children[i]=new Node(bounds, childPath, true, idCount++);
        }
        for (int i=0; i<16; i++)
            if (node.children[i]!=null && !node.children[i].isLeaf)
                readChildren(scanner, node.children[i]);
    }

    public TreeSet<Integer> getLeavesIntersectingBoundingBox(double[] searchBounds)
    {
        TreeSet<Integer> pathList=Sets.newTreeSet();
        getLeavesIntersectingBoundingBox(rootNode, searchBounds, pathList);
        return pathList;
    }

    private void getLeavesIntersectingBoundingBox(Node node, double[] searchBounds, TreeSet<Integer> pathList)
    {
        if (node.intersects(searchBounds))
            pathList.add(node.id);
        for (int i=0; i<16; i++)
            if (node.children[i]!=null)
                getLeavesIntersectingBoundingBox(node.children[i],searchBounds,pathList);
    }

    public Node getNodeById(int id)
    {
        return nodeMap.get(id);
    }

    public static void main(String[] args)
    {
        NativeLibraryLoader.loadVtkLibraries();
        OlaFSHyperTreeSkeleton skeleton=new OlaFSHyperTreeSkeleton();
        skeleton.read(Paths.get(args[0]));
    }

}
