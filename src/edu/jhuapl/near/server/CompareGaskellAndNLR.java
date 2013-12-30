package edu.jhuapl.near.server;

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
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
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
 * Its different. NLR is not boresighted with MSI, but comes out of  pixel 260/220.
 * So no - boresight will not work.
 *
 */
public class CompareGaskellAndNLR
{
    static class LidarPoint implements Comparable<LidarPoint>
    {
        long time;
        double range;

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


    static void loadPoints(String path, ModelConfig modelConfig) throws IOException
    {
        int[] xyzIndices = modelConfig.lidarBrowseXYZIndices;
        int[] scXyzIndices = modelConfig.lidarBrowseSpacecraftIndices;
        boolean isSpacecraftInSphericalCoordinates = modelConfig.lidarBrowseIsSpacecraftInSphericalCoordinates;
        int timeindex = modelConfig.lidarBrowseTimeIndex;
        int numberHeaderLines = modelConfig.lidarBrowseNumberHeaderLines;
        boolean isInMeters = modelConfig.lidarBrowseIsInMeters;
        //int noiseindex = modelConfig.lidarBrowseNoiseIndex;

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

            // // Don't include noise
            // if (noiseindex >=0 && vals[noiseindex].equals("1"))
            //     continue;

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

    public static void main(String[] args) throws IOException, FitsException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();


        ModelConfig config = ModelFactory.getModelConfig(ModelFactory.EROS, ModelFactory.GASKELL);
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

        FileWriter fstream = new FileWriter("results.csv");
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("Image ID,Image Range (km),Lidar Range (km),Image Time (UTC), Lidar Time (UTC), Range Diff (km), Time Diff (sec)\n");

        count = 1;
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

            String imageId = new File(key.name).getName();
            imageId = imageId.substring(0, imageId.length()-4);

            double[] scPos = image.getSpacecraftPosition();
            double[] imageSurfacePoint = image.getPixelSurfaceIntercept(259, 411-219);

            if (imageSurfacePoint != null)
            {
                double imageRange = MathUtil.distanceBetween(scPos, imageSurfacePoint);

                out.write(imageId + "," + imageRange + "," + lidarPoint.range + "," + imageTime + "," + lidarPoint.getFormattedTime()
                        + "," + (Math.abs(imageRange-lidarPoint.range)) + "," + ((double)Math.abs(time - lidarPoint.time)/1000.0) + "\n");
            }
            else
            {
                out.write(imageId + "," + "NA" + "," + lidarPoint.range + "," + imageTime + "," + lidarPoint.getFormattedTime()
                        + "," + "NA" + "," + ((double)Math.abs(time - lidarPoint.time)/1000.0) + "\n");
            }
        }

        out.close();
    }
}
