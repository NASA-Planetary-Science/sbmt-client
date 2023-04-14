package edu.jhuapl.sbmt.tools2.toBeUpdated;
//package edu.jhuapl.sbmt.tools2;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//
//import vtk.vtkObject;
//import vtk.vtkPolyData;
//
//import edu.jhuapl.saavtk.model.ShapeModelBody;
//import edu.jhuapl.saavtk.model.ShapeModelType;
//import edu.jhuapl.saavtk.util.FileCache;
//import edu.jhuapl.saavtk.util.FileUtil;
//import edu.jhuapl.saavtk.util.LatLon;
//import edu.jhuapl.saavtk.util.MathUtil;
//import edu.jhuapl.saavtk.util.NativeLibraryLoader;
//import edu.jhuapl.saavtk.util.PolyDataUtil;
//import edu.jhuapl.sbmt.client.SbmtModelFactory;
//import edu.jhuapl.sbmt.common.client.BodyViewConfig;
//import edu.jhuapl.sbmt.common.client.SmallBodyModel;
//import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
//import edu.jhuapl.sbmt.core.image.ImageSource;
////import edu.jhuapl.sbmt.image.model.bodies.eros.MSIImage;
////import edu.jhuapl.sbmt.image.model.keys.ImageKey;
//import edu.jhuapl.sbmt.lidar.LidarFileSpec;
//import edu.jhuapl.sbmt.lidar.util.LidarBrowseUtil;
//import edu.jhuapl.sbmt.util.TimeUtil;
//
//import nom.tam.fits.FitsException;
//
//
///**
// * This program does the following as explained in this email exchange with Olivier:
// *
// * Olivier wrote:
// *
// * More importantly - can you run a quick analysis using all the Gaskell images
// * registered to the Eros model to determine the range to the surface of Eros at
// * pixel sample 260, line 220. And then, can you compare this range measured
// * from the images to the range measured by NLR, for NLR data collected as close
// * as possible in time to when the image was obtained. I think you have tools to
// * do the first task quickly. If you can't do the second one easily, I certainly
// * can - I would simply need the time when each image was taken (in the info
// * file) and the range to the surface you compute from the pixel I mention
// * above. I can search through all the NLR data times and finding the
// * corresponding range and assess the differences in range (hopefully small!!!)
// *
// * Eli wrote:
// *
// * And I think I should be able to do the analysis of comparing the range of the
// * gaskell images to the closest NLR. I guess I could generate a file with the
// * following columns:
// *
// * 1. Gaskell image ID
// * 2. Distance of spacecraft to pixel 260/220 on surface
// * 3. Distance of spacecraft to lidar point on surface closest in time to when
// *    image was acquired
// * 4. Time image was acquired (UTC)
// * 5. Time lidar point was acquired (UTC)
// * 6. difference in range between image and lidar
// * 7. difference in time (in seconds) between image and lidar
// *
// * Eli wrote:
// *
// * Another question: When you mentioned pixel 260/220, is that the same as the
// * boresight direction or different from it? Assuming the boresight would simplify things.
// *
// * Olivier wrote:
// *
// * Its different. NLR is not boresighted with MSI, but comes out of pixel 260/220.
// * So no - boresight will not work.
// *
// * Olivier wrote asking to run it using 4 neighboring pixels to pixel 260/220:
// *
// * In the meanwhile take four pixels neighboring the 260-220 pixel. And send the files.
// * Should give me an idea if the largish offset I'm measuring is just because of where
// * we think the lidar is in Msi.
// *
// * In August, 2014, Olivier requestes that the comparison be done agains the maps (as
// * generated by Mapmaker) rather than against the global shape model since the former
// * is at higher resolution.
// *
// */
//public class CompareGaskellAndNLR
//{
//    static class LidarPoint implements Comparable<LidarPoint>
//    {
//        double time;
//        double range;
//        double[] point;
//
//        @Override
//        public int compareTo(LidarPoint o)
//        {
//            return Double.valueOf(this.time).compareTo(o.time);
//        }
//
//        String getFormattedTime()
//        {
//            return TimeUtil.et2str(time);
//        }
//    }
//
//    static List<LidarPoint> points = new ArrayList<LidarPoint>();
//
//
//    static void loadPoints(String path, SmallBodyViewConfig smallBodyConfig) throws IOException
//    {
//        // Uncomment to save out all lidar data to a single file
//        //FileWriter fstream = new FileWriter("/tmp/nlr-all.txt", true);
//        //BufferedWriter out = new BufferedWriter(fstream);
//
//        int[] xyzIndices = smallBodyConfig.lidarBrowseXYZIndices;
//        int[] scXyzIndices = smallBodyConfig.lidarBrowseSpacecraftIndices;
//        boolean isSpacecraftInSphericalCoordinates = smallBodyConfig.lidarBrowseIsSpacecraftInSphericalCoordinates;
//        int timeindex = smallBodyConfig.lidarBrowseTimeIndex;
//        int numberHeaderLines = smallBodyConfig.lidarBrowseNumberHeaderLines;
//        boolean isInMeters = smallBodyConfig.lidarBrowseIsInMeters;
//        int noiseindex = smallBodyConfig.lidarBrowseNoiseIndex;
//
//        int xindex = xyzIndices[0];
//        int yindex = xyzIndices[1];
//        int zindex = xyzIndices[2];
//        int scxindex = scXyzIndices[0];
//        int scyindex = scXyzIndices[1];
//        int sczindex = scXyzIndices[2];
//
//        File file = FileCache.getFileFromServer(path);
//
//        if (!file.exists())
//            throw new IOException(path + " could not be loaded");
//
//        FileInputStream fs = new FileInputStream(file.getAbsolutePath());
//        InputStreamReader isr = new InputStreamReader(fs);
//        BufferedReader in = new BufferedReader(isr);
//
//        for (int i=0; i<numberHeaderLines; ++i)
//            in.readLine();
//
//        String line;
//
//        while ((line = in.readLine()) != null)
//        {
//            String[] vals = line.trim().split("\\s+");
//
//            // Don't include noise
//            if (noiseindex >=0 && vals[noiseindex].equals("1"))
//                continue;
//
//            double x = Double.parseDouble(vals[xindex]);
//            double y = Double.parseDouble(vals[yindex]);
//            double z = Double.parseDouble(vals[zindex]);
//            double scx = Double.parseDouble(vals[scxindex]);
//            double scy = Double.parseDouble(vals[scyindex]);
//            double scz = Double.parseDouble(vals[sczindex]);
//
//            // If spacecraft position is in spherical coordinates,
//            // do the conversion here.
//            if (isSpacecraftInSphericalCoordinates)
//            {
//                double[] xyz = MathUtil.latrec(new LatLon(scy*Math.PI/180.0, scx*Math.PI/180.0, scz));
//                scx = xyz[0];
//                scy = xyz[1];
//                scz = xyz[2];
//            }
//
//            if (isInMeters)
//            {
//                x /= 1000.0;
//                y /= 1000.0;
//                z /= 1000.0;
//                scx /= 1000.0;
//                scy /= 1000.0;
//                scz /= 1000.0;
//            }
//
//            double[] pt1 = {x ,y, z};
//            double[] pt2 = {scx, scy, scz};
//
//            double range = MathUtil.distanceBetween(pt1, pt2);
//
//            LidarPoint point = new LidarPoint();
//            double time = TimeUtil.str2et(vals[timeindex]);
//
//            point.time = time;
//            point.range = range;
//            point.point = pt1;
//
//            points.add(point);
//
//            //out.write(vals[timeindex]+" "+x+" "+y+" "+z+" "+scx+" "+scy+" "+scz+"\n");
//        }
//
//        in.close();
//
//        //out.close();
//
//        // sort the points
//        Collections.sort(points);
//    }
//
//    static LidarPoint getClosestLidarPoint(double time)
//    {
//        LidarPoint dummyPoint = new LidarPoint();
//        dummyPoint.time = time;
//
//        int idx = Collections.binarySearch(points, dummyPoint);
//
//        if (idx >= 0)
//        {
//            return points.get(idx);
//        }
//        else
//        {
//            idx = -(idx + 1);
//            // Look at the points before and after the insertion point and return
//            // the closest to time
//            if (idx == 0)
//                return points.get(0);
//            else if (idx == points.size())
//                return points.get(points.size()-1);
//            else
//            {
//                double beforeTime = points.get(idx-1).time;
//                double afterTime = points.get(idx).time;
//
//                // test to make sure signs are as expected
//                if (time-beforeTime <= 0 || afterTime-time <= 0)
//                {
//                    System.out.println("Uh oh. times seem to bring wrong!");
//                    System.exit(1);
//                }
//
//                if (time-beforeTime < afterTime-time)
//                    return points.get(idx-1);
//                else
//                    return points.get(idx);
//            }
//        }
//    }
//
//    static LidarPoint[] get2ClosestLidarPoints(double time)
//    {
//        LidarPoint dummyPoint = new LidarPoint();
//        dummyPoint.time = time;
//
//        int idx = Collections.binarySearch(points, dummyPoint);
//
//        if (idx >= 0)
//        {
//            return new LidarPoint[]{points.get(idx)};
//        }
//        else
//        {
//            idx = -(idx + 1);
//            // Look at the points before and after the insertion point and return
//            // the closest to time
//            if (idx == 0)
//                return new LidarPoint[]{points.get(0)};
//            else if (idx == points.size())
//                return new LidarPoint[]{points.get(points.size()-1)};
//            else
//            {
//                double beforeTime = points.get(idx-1).time;
//                double afterTime = points.get(idx).time;
//
//                // test to make sure signs are as expected
//                if (time-beforeTime <= 0 || afterTime-time <= 0)
//                {
//                    System.out.println("Uh oh. times seem to bring wrong!");
//                    System.exit(1);
//                }
//
//                return new LidarPoint[]{points.get(idx-1), points.get(idx)};
//            }
//        }
//    }
//
//    static void flipNormalIfInwardFacing(double[] pt, double[] normal)
//    {
//        double[] dir = pt.clone();
//        MathUtil.vhat(dir, dir);
//        MathUtil.vhat(normal, normal);
//        if (MathUtil.vdot(dir, normal) < 0.0)
//        {
//            MathUtil.vscl(-1.0,  normal, normal);
//        }
//    }
//
//    static class PlateStatistics
//    {
//        List<Double> rangeDiffs = new ArrayList<Double>();
//        List<Double> incidences = new ArrayList<Double>();
//        List<Double> emissions = new ArrayList<Double>();
//    }
//
//    static void writePlateStatisticsMap(HashMap<Integer, PlateStatistics> plateMap,
//            SmallBodyModel lowResSmallBodyModel,
//            String filename) throws IOException
//    {
//        FileWriter fstream = new FileWriter(filename);
//        BufferedWriter out = new BufferedWriter(fstream);
//        out.write("Plate,Mean Range Diff (km),Stdev Range Diff (km),Mean Incidence (km),Stdev Incidence (km),Mean Emission (km),Stdev Emission (km),Number Intersects\n");
//
//        int numPlates = lowResSmallBodyModel.getSmallBodyPolyData().GetNumberOfCells();
//        for (int i=0; i<numPlates; ++i)
//        {
//            if (plateMap.containsKey(i) &&
//                    !plateMap.get(i).rangeDiffs.isEmpty())
//            {
//                DescriptiveStatistics rangeDiffStatistics = new DescriptiveStatistics();
//                for (Double v : plateMap.get(i).rangeDiffs)
//                    rangeDiffStatistics.addValue(v);
//                DescriptiveStatistics incidenceStatistics = new DescriptiveStatistics();
//                for (Double v : plateMap.get(i).incidences)
//                    incidenceStatistics.addValue(v);
//                DescriptiveStatistics emissionStatistics = new DescriptiveStatistics();
//                for (Double v : plateMap.get(i).emissions)
//                    emissionStatistics.addValue(v);
//                out.write(i + "," + rangeDiffStatistics.getMean() + "," + rangeDiffStatistics.getStandardDeviation() + ","
//                        + incidenceStatistics.getMean() + "," + incidenceStatistics.getStandardDeviation() + ","
//                        + emissionStatistics.getMean() + "," + emissionStatistics.getStandardDeviation() + ","
//                        + plateMap.get(i).rangeDiffs.size() + "\n");
//            }
//            else
//            {
//                out.write(i + ",-1.0e32,-1.0e32,-1.0e32,-1.0e32,-1.0e32,-1.0e32,0\n");
//            }
//        }
//
//        out.close();
//    }
//
//    // This does the comparison with the global high res shape model
//    static void doComparisonGlobalShapeModel(
//            List<String> msiFiles,
//            SmallBodyModel smallBodyModel,
//            String resultsFilename,
//            int sampleOffset,
//            int lineOffset
//            ) throws IOException, FitsException
//    {
//        FileWriter fstream = new FileWriter(resultsFilename);
//        BufferedWriter out = new BufferedWriter(fstream);
//
//        out.write("Image ID,Image Range (km),Lidar Range (km),Image Time (UTC), Lidar Time (UTC), Range Diff (km), Time Diff (sec), X-image (km), Y-image (km), Z-image (km), X-lidar (km), Y-lidar (km), Z-lidar (km)\n");
//
//        int count = 1;
//        for (String keyName : msiFiles)
//        {
//            System.out.println("starting msi " + (count++) + " / " + msiFiles.size() + " " + keyName + "\n");
//
//            keyName = keyName.replace(".FIT", "");
//            ImageKey key = new ImageKey(keyName, ImageSource.GASKELL);
//            MSIImage image = new MSIImage(key, smallBodyModel, true);
//
//            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
//            // are 1153 bytes long or less
//            File sumfile = new File(image.getSumfileFullPath());
//            if (!sumfile.exists() || sumfile.length() <= 1153)
//                continue;
//
//            String startTimeStr = image.getStartTime();
//            String stopTimeStr = image.getStopTime();
//
//            double startTime = TimeUtil.str2et(startTimeStr);
//            double stopTime = TimeUtil.str2et(stopTimeStr);
//
//            double time = startTime + ((stopTime - startTime) / 2.0);
//
//            String imageTime = TimeUtil.et2str(time);
//
//            LidarPoint lidarPoint = getClosestLidarPoint(time);
//            LidarPoint[] lidarPoints = get2ClosestLidarPoints(time);
//
//            String imageId = new File(key.name).getName();
//            imageId = imageId.substring(0, imageId.length()-4);
//
//            double[] scPos = image.getSpacecraftPosition();
//            double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259 + sampleOffset, 411 - (219 + lineOffset));
//
//            double lidarRange = lidarPoint.range;
//            if (lidarPoints.length == 2)
//            {
//                lidarRange = MathUtil.linearInterpolate2Points(
//                        lidarPoints[0].time, lidarPoints[0].range,
//                        lidarPoints[1].time, lidarPoints[1].range,
//                        time);
//            }
//
//            if (imageSurfacePoint != null)
//            {
//                double imageRange = MathUtil.distanceBetween(scPos, imageSurfacePoint);
//
//                out.write(imageId + "," + imageRange + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
//                        + "," + (imageRange-lidarRange) + "," + Math.abs(time - lidarPoint.time) + ","
//                        + imageSurfacePoint[0] + "," + imageSurfacePoint[1] + "," + imageSurfacePoint[2] + ","
//                        + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + "\n");
//            }
//            else
//            {
//                out.write(imageId + "," + "NA" + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
//                        + "," + "NA" + "," + Math.abs(time - lidarPoint.time) + ",NA,NA,NA,"
//                + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + "\n");
//            }
//        }
//
//        out.close();
//    }
//
//    // This does the comparison with individual maps generated by mapmaker. The program
//    // CompareGaskellAndNLRGenerateMapletsForImages must be run first to generate the
//    // maplets and covert them to vtk format.
//    static void doComparisonMaplets(
//            List<String> msiFiles,
//            String mapletDir,
//            String resultsFilename,
//            int sampleOffset,
//            int lineOffset
//            ) throws Exception
//    {
//        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
//        SmallBodyModel lowResSmallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
//        lowResSmallBodyModel.setModelResolution(0);
//
//        HashMap<Integer, PlateStatistics> plateMap = new LinkedHashMap<Integer, PlateStatistics>();
//
//        FileWriter fstream = new FileWriter(resultsFilename);
//        BufferedWriter out = new BufferedWriter(fstream);
//
//        out.write("Image ID,Image Range (km),Lidar Range (km),Image Time (UTC), Lidar Time (UTC), Range Diff (km), Time Diff (sec), X-image (km), Y-image (km), Z-image (km), X-lidar (km), Y-lidar (km), Z-lidar (km), Incidence (deg), Emission (deg)\n");
//
//        int count = 1;
//        for (String keyName : msiFiles)
//        {
//            System.out.println("starting msi " + (count++) + " / " + msiFiles.size() + " " + keyName);
//
//            keyName = keyName.replace(".FIT", "");
//            ImageKey key = new ImageKey(keyName, ImageSource.GASKELL);
//
//            String imageId = new File(key.name).getName();
//            imageId = imageId.substring(0, imageId.length()-4);
//
//            String smallBodyFile = mapletDir + File.separator + imageId + ".vtk";
//            if (!(new File(smallBodyFile).exists()))
//            {
//                System.out.println("Skipping -- no maplet at " + smallBodyFile);
//                continue;
//            }
//            vtkPolyData smallBodyPolyData = PolyDataUtil.loadShapeModel(smallBodyFile);
//            SmallBodyModel smallBodyModel = new SmallBodyModel(key.name, smallBodyPolyData);
//
//            MSIImage image = new MSIImage(key, smallBodyModel, true);
//
//            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
//            // are 1153 bytes long or less
//            File sumfile = new File(image.getSumfileFullPath());
//            if (!sumfile.exists() || sumfile.length() <= 1153)
//            {
//                smallBodyModel.delete();
//                System.gc();
//                vtkObject.JAVA_OBJECT_MANAGER.gc(true);
//                System.out.println("Skipping -- no landmarks");
//                continue;
//            }
//
//            String startTimeStr = image.getStartTime();
//            String stopTimeStr = image.getStopTime();
//
//            double startTime = TimeUtil.str2et(startTimeStr);
//            double stopTime = TimeUtil.str2et(stopTimeStr);
//
//            double time = startTime + ((stopTime - startTime) / 2.0);
//
//            String imageTime = TimeUtil.et2str(time);
//
//            LidarPoint lidarPoint = getClosestLidarPoint(time);
//            LidarPoint[] lidarPoints = get2ClosestLidarPoints(time);
//
//            double[] scPos = image.getSpacecraftPosition();
//            double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259 + sampleOffset, 411 - (219 + lineOffset));
//
//            double lidarRange = lidarPoint.range;
//            if (lidarPoints.length == 2)
//            {
//                lidarRange = MathUtil.linearInterpolate2Points(
//                        lidarPoints[0].time, lidarPoints[0].range,
//                        lidarPoints[1].time, lidarPoints[1].range,
//                        time);
//            }
//
//            if (imageSurfacePoint != null)
//            {
//                double imageRange = MathUtil.distanceBetween(scPos, imageSurfacePoint);
//
//                double[] imageSurfaceNormal = PolyDataUtil.computePolyDataNormal(smallBodyPolyData);
//                flipNormalIfInwardFacing(imageSurfacePoint, imageSurfaceNormal);
//                double[] illumAngles = image.computeIlluminationAnglesAtPoint(imageSurfacePoint, imageSurfaceNormal);
//                double incidence = illumAngles[0];
//                double emission = illumAngles[1];
//                double rangediff = imageRange-lidarRange;
//                double timediff = Math.abs(time - lidarPoint.time);
//
//                out.write(imageId + "," + imageRange + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
//                        + "," + rangediff + "," + timediff + ","
//                        + imageSurfacePoint[0] + "," + imageSurfacePoint[1] + "," + imageSurfacePoint[2] + ","
//                        + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + "," + incidence + "," + emission + "\n");
//
//                // Only consider situations where offset is less than 300 meters and time difference less than 1 second
//                if (rangediff <= 0.3 && timediff <= 1.0)
//                {
//                    int cellId = lowResSmallBodyModel.findClosestCell(imageSurfacePoint);
//                    if (!plateMap.containsKey(cellId))
//                        plateMap.put(cellId, new PlateStatistics());
//                    plateMap.get(cellId).rangeDiffs.add(rangediff);
//                    plateMap.get(cellId).incidences.add(incidence);
//                    plateMap.get(cellId).emissions.add(emission);
//                }
//            }
//            else
//            {
//                System.out.println("Error: no intercept to maplet! We should not reach here!");
//                out.write(imageId + "," + "NA" + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
//                        + "," + "NA" + "," + Math.abs(time - lidarPoint.time) + ",NA,NA,NA,"
//                + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + ",NA,NA\n");
//            }
//
//            smallBodyModel.delete();
//            System.gc();
//            vtkObject.JAVA_OBJECT_MANAGER.gc(true);
//        }
//
//        out.close();
//
//        writePlateStatisticsMap(plateMap, lowResSmallBodyModel, "results-per-plate.csv");
//    }
//
//    public static void main(String[] args) throws Exception
//    {
//        System.setProperty("java.awt.headless", "true");
//        NativeLibraryLoader.loadVtkLibraries();
//
//
//        SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
//        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
//        smallBodyModel.setModelResolution(3);
//
//        // Load lidar data
//        BodyViewConfig tmpBodyViewConfig = (BodyViewConfig)smallBodyModel.getSmallBodyConfig();
//        List<LidarFileSpec> lidarPaths = LidarBrowseUtil.loadLidarFileSpecListFor(tmpBodyViewConfig);
//        int count = 1;
//        for (LidarFileSpec spec : lidarPaths)
//        {
//            loadPoints(spec.getPath(), config);
//
//            System.out.println("Loaded " + spec + " " + count + "/" + lidarPaths.size());
//            ++count;
//        }
//
//        // Get list of gaskell files
//        String msiFileList=args[0];
//        List<String> msiFiles = null;
//        try {
//            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // If 2 arguments are provided, then assume the second argument specifies the folder
//        // containing the maps and do the comparison against these maps rather than against the
//        // global shape model.
//        if (args.length > 1)
//        {
//            String mapletDir = args[1];
//            int sample = 260;
//            int line = 220;
//            doComparisonMaplets(msiFiles, mapletDir, "results-" + sample + "-" + line + ".csv", 0, 0);
//        }
//        else
//        {
//            int size = 4;
//            for (int i=-size; i<=size; ++i)
//                for (int j=-size; j<=size; ++j)
//                {
//                    int sample = 260 + i;
//                    int line = 220 + j;
//                    doComparisonGlobalShapeModel(msiFiles, smallBodyModel, "results-" + sample + "-" + line + ".csv", -i, j);
//                }
//        }
//    }
//}
