package edu.jhuapl.near.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program goes through all the lidar data and divides all the data
 * up into cubes and saves each cube to a separate file.
 */
abstract public class LidarCubesGenerator
{
    abstract protected SmallBodyModel getSmallBodyModel();
    abstract protected int[] getXYZIndices();

    abstract protected String getFileListPath();
    abstract protected String getOutputFolderPath();

    abstract protected int getNumberHeaderLines();

    abstract protected int[] getSpacecraftIndices();

    /**
     * For Eros NLR data, the spacecraft position is in spherical coordinates,
     * not Cartesian. Hence we need this function.
     * @return
     */
    abstract protected boolean isSpacecraftInSphericalCoordinates();

    abstract protected int getTimeIndex();

    abstract protected int getNoiseIndex();

    abstract protected int getPotentialIndex();


    /**
     * Return whether or not the units of the lidar points are in meters. If false
     * they are assumed to be in kilometers.
     * @return
     */
    abstract protected boolean isInMeters();

    /**
     * First create empty files for all the cubes files
     * @throws IOException
     */
    private void createInitialFiles() throws IOException
    {
        SmallBodyModel smallBodyModel = getSmallBodyModel();
        String outputFolder = getOutputFolderPath();

        TreeSet<Integer> cubes = smallBodyModel.getIntersectingCubes(smallBodyModel.getLowResSmallBodyPolyData());

        for (Integer cubeid : cubes)
        {
            FileWriter fstream = new FileWriter(outputFolder + "/" + cubeid + ".lidarcube");
            BufferedWriter out = new BufferedWriter(fstream);
            out.close();
        }
    }

    public void run()
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        SmallBodyModel smallBodyModel = getSmallBodyModel();

        String lidarFileList = getFileListPath();
        String outputFolder = getOutputFolderPath();

        new File(outputFolder).mkdirs();

        ArrayList<String> lidarFiles = null;
        try
        {
            lidarFiles = FileUtil.getFileLinesAsStringList(lidarFileList);

            createInitialFiles();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }

        try
        {
            int xindex = getXYZIndices()[0];
            int yindex = getXYZIndices()[1];
            int zindex = getXYZIndices()[2];

            int scxindex = getSpacecraftIndices()[0];
            int scyindex = getSpacecraftIndices()[1];
            int sczindex = getSpacecraftIndices()[2];

            int noiseindex = getNoiseIndex();
            int timeindex = getTimeIndex();
            int potentialIndex = getPotentialIndex();

            int filecount = 1;

            for (String filename : lidarFiles)
            {
                System.out.println("Begin processing file " + filename + " - " + filecount++ + " / " + lidarFiles.size());

                InputStream fs = new FileInputStream(filename);
                if (filename.toLowerCase().endsWith(".gz"))
                    fs = new GZIPInputStream(fs);
                InputStreamReader isr = new InputStreamReader(fs);
                BufferedReader in = new BufferedReader(isr);

                for (int i=0; i<getNumberHeaderLines(); ++i)
                {
                    in.readLine();
                }

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
                    if (isSpacecraftInSphericalCoordinates())
                    {
                        double[] xyz = MathUtil.latrec(new LatLon(scy*Math.PI/180.0, scx*Math.PI/180.0, scz));
                        scx = xyz[0];
                        scy = xyz[1];
                        scz = xyz[2];
                    }

                    if (isInMeters())
                    {
                        x /= 1000.0;
                        y /= 1000.0;
                        z /= 1000.0;
                        scx /= 1000.0;
                        scy /= 1000.0;
                        scz /= 1000.0;
                    }

                    // Compute closest point on asteroid to target
                    double[] closest = smallBodyModel.findClosestPoint(new double[]{x,y,z});

                    double potential = 0.0;
                    if (potentialIndex >= 0)
                    {
                        potential = Double.parseDouble(vals[potentialIndex]);
                    }
                    else
                    {
                        // If no potential is provided in file, then use potential
                        // of plate of closest point
                        double[] coloringValues = smallBodyModel.getAllColoringValues(closest);
                        potential = coloringValues[3];
                    }

                    int cubeid = smallBodyModel.getCubeId(closest);

                    if (cubeid >= 0)
                    {
                        // Open the file for appending
                        FileWriter fstream = new FileWriter(outputFolder + "/" + cubeid + ".lidarcube", true);
                        BufferedWriter out = new BufferedWriter(fstream);

                        String record =
                            vals[timeindex] + " " +
                            (float)x + " " + (float)y + " " + (float)z + " " +
                            (float)scx + " " + (float)scy + " " + (float)scz + " " +
                            (float)potential + "\n";

                        out.write(record);

                        out.close();
                    }
                }

                in.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
