
package edu.jhuapl.sbmt.boudedobject.hyperoctree;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton;
import edu.jhuapl.sbmt.lidar.hyperoctree.FSHyperTreeSkeleton.Node;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;
import edu.jhuapl.sbmt.model.image.ImageSearchDataCollection;

public class ImageHyperTreeSearch
{

    public static void main(String[] args) throws HyperDimensionMismatchException, FileNotFoundException {
        DateTime startDateJoda = new DateTime(2000, 2, 21, 10, 00, 00, 00,
                DateTimeZone.UTC);
        DateTime endDateJoda = new DateTime(2000, 2, 21, 12, 00, 00, 00,
                DateTimeZone.UTC);
        double start = (startDateJoda.toDate()).getTime();
        double end = (endDateJoda.toDate()).getTime();

        // read in the skeleton
        ImageSearchDataCollection imageModel = new ImageSearchDataCollection(null);
        imageModel.addDatasourceSkeleton("ErosTest", "/Users/osheacm1/Documents/SAA/testHypertree/dataSource.image");
        imageModel.setCurrentDatasourceSkeleton("ErosTest");
        imageModel.readSkeleton();
        FSHyperTreeSkeleton skeleton = imageModel.getCurrentSkeleton();

        Set<Integer> cubeList = null;

        BoundingBox bb = new BoundingBox(new double[]{-17.6, -3.5, -3, 9, -6, 3});
//        BoundingBox bb = new BoundingBox(new double[]{-5, -3.5, -3, 1, -6, 3});

        cubeList = ((ImageSearchDataCollection)imageModel).getLeavesIntersectingBoundingBox(bb, new double[]{start, end});

        Set<String> files = new HashSet<String>();


        for (Integer cubeid : cubeList)
        {
            System.out.println("cubeId: " + cubeid);
            Node currNode = skeleton.getNodeById(cubeid);
            Path path = currNode.getPath();
            Path dataPath = path.resolve("data");
            DataInputStream instream= new DataInputStream(new BufferedInputStream(new FileInputStream(dataPath.toFile())));
            ArrayList<HyperBoundedObject> images = new ArrayList<HyperBoundedObject>();
            try
            {
                while (instream.available() > 0) {
                    HyperBoundedObject obj = BoundedObjectHyperTreeNode.createNewBoundedObject(instream);
                    images.add(obj);
                    int fileNum = obj.getFileNum();
                    Map<Integer, String> fileMap = skeleton.getFileMap();
                    String file = fileMap.get(fileNum);
//                    System.out.println("file: " + file);
                    files.add(file);
//                    HyperBox box = obj.getBbox();
//                    double[] bounds = box.getBounds();
//                    double[] nodeBounds = currNode.getBounds();
//                    System.out.println("Node Bounds : [" + nodeBounds[1] +"-" + nodeBounds[0] + ", " + nodeBounds[3] +"-" + nodeBounds[2] + "," + nodeBounds[5] +"-" + nodeBounds[4]+"], [" + nodeBounds[0] +"," + nodeBounds[2] + "," + nodeBounds[4] +"]");
//                    System.out.println("Image Bounds: [" + bounds[1] +"-" + bounds[0] + ", " + bounds[3] +"-" + bounds[2] + "," + bounds[5] +"-" + bounds[4]+"], [" + bounds[0] +"," + bounds[2] + "," + bounds[4] +"]");
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        for (String file : files) {
            System.out.println(file);
        }

        /// NOW CHECK WHICH FILES ACTUALLY INTERSECT REGION
    }


}
