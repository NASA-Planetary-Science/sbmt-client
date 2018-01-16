
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

        for (Integer cubeid : cubeList)
        {
            String filename = "Users/osheacm1/Documets/SAA/testHypertree/" + cubeid + ".imagecube";
            System.out.println(filename);

//            int fileId;
//            if (!localFileMap.containsValue(file.toString()))
//            {
//                fileId=localFileMap.size();
//                localFileMap.put(fileId, file.toString());
//            }
//            else
//                fileId=localFileMap.inverse().get(file.toString());
//
//            if (file == null)
//                continue;
//
//            InputStream fs = new FileInputStream(file.getAbsolutePath());
//            InputStreamReader isr = new InputStreamReader(fs);
//            BufferedReader in = new BufferedReader(isr);
//
//            String lineRead;
//            while ((lineRead = in.readLine()) != null)
//            {
//                String[] vals = lineRead.trim().split("\\s+");
//
//                double time = TimeUtil.str2et(vals[timeindex]);
//                if (time < start || time > stop)
//                    continue;
//
//                double[] scpos = new double[3];
//                double[] target = new double[3];
//                target[0] = Double.parseDouble(vals[xindex]);
//                target[1] = Double.parseDouble(vals[yindex]);
//                target[2] = Double.parseDouble(vals[zindex]);
//                scpos[0] = Double.parseDouble(vals[scxindex]);
//                scpos[1] = Double.parseDouble(vals[scyindex]);
//                scpos[2] = Double.parseDouble(vals[sczindex]);
//
//                if (pointInRegionChecker==null) // if this part of the code has been reached and the point-checker is null then this is a time-only search, and the time criterion has already been met (cf. continue statement a few lines above)
//                {
//                    LidarPoint p=new BasicLidarPoint(target, scpos, time, 0);
//                    originalPoints.add(p);
//                    originalPointsSourceFiles.put(p, fileId);
//                    continue;
//                }
//
//
//                if (pointInRegionChecker.checkPointIsInRegion(target))  // here, the point is known to be within the specified time bounds, and since the point checker exists the target coordinates are filtered against
//                {
//                    LidarPoint p=new BasicLidarPoint(target, scpos, time, 0);
//                    originalPoints.add(p);
//                    originalPointsSourceFiles.put(p, fileId);
//                    continue;
//                }
//            }
//
//            in.close();
        }
        }

}
