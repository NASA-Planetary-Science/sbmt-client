
package edu.jhuapl.sbmt.boudedobject.hyperoctree;

import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.sbmt.model.image.ImageSearchDataCollection;


public class ImageHyperTreeSearch
{
    public static void main(String[] args) {
        DateTime startDateJoda = new DateTime(2000, 2, 16, 11, 00, 00, 00,
                DateTimeZone.UTC);
        DateTime endDateJoda = new DateTime(2000, 2, 16, 12, 00, 00, 00,
                DateTimeZone.UTC);
        double start = (startDateJoda.toDate()).getTime();
        double end = (endDateJoda.toDate()).getTime();

        // read in the skeleton
        ImageSearchDataCollection imageModel = new ImageSearchDataCollection(null);
        imageModel.addDatasourceSkeleton("ErosTest", "/Users/osheacm1/Documents/SAA/testHypertree/dataSource.image");
        imageModel.setCurrentDatasourceSkeleton("ErosTest");
        imageModel.readSkeleton();


        TreeSet<Integer> cubeList = null;
        BoundingBox bb = new BoundingBox(new double[]{-17.6, -3.5, -3, 9, -6, 3});
        cubeList = ((ImageSearchDataCollection)imageModel).getLeavesIntersectingBoundingBox(bb, new double[]{start, end});
        }

}
