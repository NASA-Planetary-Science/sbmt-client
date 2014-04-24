package edu.jhuapl.near.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.LidarBrowseDataCollection;
import edu.jhuapl.near.model.LidarBrowseDataCollection.LidarDataFileSpec;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;


/**
 * This program does the following as explained in this email exchange with Olivier:
 *
 * Olivier wrote:
 *
 * More importantly - can you run a quick analysis using all the Gaskell images
 * registered to the Eros model to determine the range to the surface of Eros at
 * pixel sample 260, line 220. And then, can you compare this range measured
 * from the images to the range measured by NLR, for NLR data collected as close
 * as possible in time to when the image was obtained. I think you have tools to
 * do the first task quickly. If you can't do the second one easily, I certainly
 * can - I would simply need the time when each image was taken (in the info
 * file) and the range to the surface you compute from the pixel I mention
 * above. I can search through all the NLR data times and finding the
 * corresponding range and assess the differences in range (hopefully small!!!)
 *
 * Eli wrote:
 *
 * And I think I should be able to do the analysis of comparing the range of the
 * gaskell images to the closest NLR. I guess I could generate a file with the
 * following columns:
 *
 * 1. Gaskell image ID
 * 2. Distance of spacecraft to pixel 260/220 on surface
 * 3. Distance of spacecraft to lidar point on surface closest in time to when
 *    image was acquired
 * 4. Time image was acquired (UTC)
 * 5. Time lidar point was acquired (UTC)
 * 6. difference in range between image and lidar
 * 7. difference in time (in seconds) between image and lidar
 *
 * Eli wrote:
 *
 * Another question: When you mentioned pixel 260/220, is that the same as the
 * boresight direction or different from it? Assuming the boresight would simplify things.
 *
 * Olivier wrote:
 *
 * Its different. NLR is not boresighted with MSI, but comes out of pixel 260/220.
 * So no - boresight will not work.
 *
 * Olivier wrote asking to run it using 4 neighboring pixels to pixel 260/220:
 *
 * In the meanwhile take four pixels neighboring the 260-220 pixel. And send the files.
 * Should give me an idea if the largish offset I'm measuring is just because of where
 * we think the lidar is in Msi.
 *
 */
public class CompareGaskellAndNLR
{
    static class LidarPoint implements Comparable<LidarPoint>
    {
        long time;
        double range;
        double[] point;

        @Override
        public int compareTo(LidarPoint o)
        {
            return Long.valueOf(this.time).compareTo(o.time);
        }

        String getFormattedTime()
        {
            DateTime dt = new DateTime(time, DateTimeZone.UTC);
            return fmt.print(dt);
        }
    }

    static List<LidarPoint> points = new ArrayList<LidarPoint>();
    static DateTimeFormatter fmt = ISODateTimeFormat.dateTime();


    static void loadPoints(String path, SmallBodyConfig smallBodyConfig) throws IOException
    {
        int[] xyzIndices = smallBodyConfig.lidarBrowseXYZIndices;
        int[] scXyzIndices = smallBodyConfig.lidarBrowseSpacecraftIndices;
        boolean isSpacecraftInSphericalCoordinates = smallBodyConfig.lidarBrowseIsSpacecraftInSphericalCoordinates;
        int timeindex = smallBodyConfig.lidarBrowseTimeIndex;
        int numberHeaderLines = smallBodyConfig.lidarBrowseNumberHeaderLines;
        boolean isInMeters = smallBodyConfig.lidarBrowseIsInMeters;
        int noiseindex = smallBodyConfig.lidarBrowseNoiseIndex;

        int xindex = xyzIndices[0];
        int yindex = xyzIndices[1];
        int zindex = xyzIndices[2];
        int scxindex = scXyzIndices[0];
        int scyindex = scXyzIndices[1];
        int sczindex = scXyzIndices[2];

        File file = FileCache.getFileFromServer(path);

        if (file == null)
            throw new IOException(path + " could not be loaded");

        FileInputStream fs = new FileInputStream(file.getAbsolutePath());
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        for (int i=0; i<numberHeaderLines; ++i)
            in.readLine();

        String line;

        while ((line = in.readLine()) != null)
        {
            String[] vals = line.trim().split("\\s+");

            // Don't include noise
            if (noiseindex >=0 && vals[noiseindex].equals("1"))
                continue;

            double x = Double.parseDouble(vals[xindex]);
            double y = Double.parseDouble(vals[yindex]);
            double z = Double.parseDouble(vals[zindex]);
            double scx = Double.parseDouble(vals[scxindex]);
            double scy = Double.parseDouble(vals[scyindex]);
            double scz = Double.parseDouble(vals[sczindex]);

            // If spacecraft position is in spherical coordinates,
            // do the conversion here.
            if (isSpacecraftInSphericalCoordinates)
            {
                double[] xyz = MathUtil.latrec(new LatLon(scy*Math.PI/180.0, scx*Math.PI/180.0, scz));
                scx = xyz[0];
                scy = xyz[1];
                scz = xyz[2];
            }

            if (isInMeters)
            {
                x /= 1000.0;
                y /= 1000.0;
                z /= 1000.0;
                scx /= 1000.0;
                scy /= 1000.0;
                scz /= 1000.0;
            }

            double[] pt1 = {x ,y, z};
            double[] pt2 = {scx, scy, scz};

            double range = MathUtil.distanceBetween(pt1, pt2);

            LidarPoint point = new LidarPoint();
            long time = new DateTime(vals[timeindex], DateTimeZone.UTC).getMillis();

            point.time = time;
            point.range = range;
            point.point = pt1;

            points.add(point);
        }

        in.close();

        // sort the points
        Collections.sort(points);
    }

    static LidarPoint getClosestLidarPoint(long time)
    {
        LidarPoint dummyPoint = new LidarPoint();
        dummyPoint.time = time;

        int idx = Collections.binarySearch(points, dummyPoint);

        if (idx >= 0)
        {
            return points.get(idx);
        }
        else
        {
            idx = -(idx + 1);
            // Look at the points before and after the insertion point and return
            // the closest to time
            if (idx == 0)
                return points.get(0);
            else if (idx == points.size())
                return points.get(points.size()-1);
            else
            {
                long beforeTime = points.get(idx-1).time;
                long afterTime = points.get(idx).time;

                // test to make sure signs are as expected
                if (time-beforeTime <= 0 || afterTime-time <= 0)
                {
                    System.out.println("Uh oh. times seem to bring wrong!");
                    System.exit(1);
                }

                if (time-beforeTime < afterTime-time)
                    return points.get(idx-1);
                else
                    return points.get(idx);
            }
        }
    }

    static LidarPoint[] get2ClosestLidarPoints(long time)
    {
        LidarPoint dummyPoint = new LidarPoint();
        dummyPoint.time = time;

        int idx = Collections.binarySearch(points, dummyPoint);

        if (idx >= 0)
        {
            return new LidarPoint[]{points.get(idx)};
        }
        else
        {
            idx = -(idx + 1);
            // Look at the points before and after the insertion point and return
            // the closest to time
            if (idx == 0)
                return new LidarPoint[]{points.get(0)};
            else if (idx == points.size())
                return new LidarPoint[]{points.get(points.size()-1)};
            else
            {
                long beforeTime = points.get(idx-1).time;
                long afterTime = points.get(idx).time;

                // test to make sure signs are as expected
                if (time-beforeTime <= 0 || afterTime-time <= 0)
                {
                    System.out.println("Uh oh. times seem to bring wrong!");
                    System.exit(1);
                }

                return new LidarPoint[]{points.get(idx-1), points.get(idx)};
            }
        }
    }

    static void doComparison(
            ArrayList<String> msiFiles,
            SmallBodyModel smallBodyModel,
            String resultsFilename,
            int sampleOffset,
            int lineOffset
            ) throws IOException, FitsException
    {
        FileWriter fstream = new FileWriter(resultsFilename);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("Image ID,Image Range (km),Lidar Range (km),Image Time (UTC), Lidar Time (UTC), Range Diff (km), Time Diff (sec), X-image (km), Y-image (km), Z-image (km), X-lidar (km), Y-lidar (km), Z-lidar (km)\n");

        int count = 1;
        for (String filename : msiFiles)
        {
            System.out.println("starting msi " + (count++) + " / " + msiFiles.size() + " " + filename + "\n");

            File origFile = new File(filename);

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1153 bytes long or less
            if (origFile.length() <= 1153)
                continue;

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            ImageKey key = new ImageKey(keyName, ImageSource.GASKELL);
            MSIImage image = new MSIImage(key, smallBodyModel, true, rootFolder);

            String startTimeStr = image.getStartTime();
            String stopTimeStr = image.getStopTime();

            double startTime = new DateTime(startTimeStr, DateTimeZone.UTC).getMillis();
            double stopTime = new DateTime(stopTimeStr, DateTimeZone.UTC).getMillis();

            long time = (long) (startTime + ((stopTime - startTime) / 2.0));

            DateTime dt = new DateTime(time, DateTimeZone.UTC);
            String imageTime = fmt.print(dt);

            LidarPoint lidarPoint = getClosestLidarPoint(time);
            LidarPoint[] lidarPoints = get2ClosestLidarPoints(time);

            String imageId = new File(key.name).getName();
            imageId = imageId.substring(0, imageId.length()-4);

            double[] scPos = image.getSpacecraftPosition();
            double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259 + sampleOffset, 411 - (219 + lineOffset));

            double lidarRange = lidarPoint.range;
            if (lidarPoints.length == 2)
            {
                lidarRange = MathUtil.linearInterpolate2Points(
                        lidarPoints[0].time, lidarPoints[0].range,
                        lidarPoints[1].time, lidarPoints[1].range,
                        time);
            }

            if (imageSurfacePoint != null)
            {
                double imageRange = MathUtil.distanceBetween(scPos, imageSurfacePoint);

                out.write(imageId + "," + imageRange + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
                        + "," + (Math.abs(imageRange-lidarRange)) + "," + ((double)Math.abs(time - lidarPoint.time)/1000.0) + ","
                        + imageSurfacePoint[0] + "," + imageSurfacePoint[1] + "," + imageSurfacePoint[2] + ","
                        + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + "\n");
            }
            else
            {
                out.write(imageId + "," + "NA" + "," + lidarRange + "," + imageTime + "," + lidarPoint.getFormattedTime()
                        + "," + "NA" + "," + ((double)Math.abs(time - lidarPoint.time)/1000.0) + ",NA,NA,NA,"
                + lidarPoint.point[0] + "," + lidarPoint.point[1] + "," + lidarPoint.point[2] + "\n");
            }
        }

        out.close();
    }

    public static void main(String[] args) throws IOException, FitsException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();


        SmallBodyConfig config = SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL);
        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(config);
        smallBodyModel.setModelResolution(3);

        // Load lidar data
        LidarBrowseDataCollection lidarModel = (LidarBrowseDataCollection) ModelFactory.
                createLidarModels(smallBodyModel).get(ModelNames.LIDAR_BROWSE);

        ArrayList<LidarDataFileSpec> lidarPaths = lidarModel.getAllLidarPaths();
        int count = 1;
        for (LidarDataFileSpec spec : lidarPaths)
        {
            loadPoints(spec.path, config);

            System.out.println("Loaded " + spec + " " + count + "/" + lidarPaths.size());
            ++count;
        }

        // Get list of gaskell files
        String msiFileList=args[0];
        ArrayList<String> msiFiles = null;
        try {
            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        doComparison(msiFiles, smallBodyModel, "results-260-220.csv", 0, 0);
        doComparison(msiFiles, smallBodyModel, "results-260-221.csv", 0, 1);
        doComparison(msiFiles, smallBodyModel, "results-260-219.csv", 0, -1);
        doComparison(msiFiles, smallBodyModel, "results-259-220.csv", 1, 0);
        doComparison(msiFiles, smallBodyModel, "results-261-220.csv", -1, 0);
    }
}
