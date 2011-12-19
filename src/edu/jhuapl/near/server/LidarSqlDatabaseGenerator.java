package edu.jhuapl.near.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;
import edu.jhuapl.near.util.MathUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This program goes through all the Lidar data and creates an sql database
 * containing all the data.
 *
 * The database consists of the following columns
 *
 * 1.  time (as 64-bit long integer)
 * 2.  x closest point
 * 3.  y closest point
 * 4.  z closest point
 * 5.  x target point
 * 6.  y target point
 * 7.  z target point
 * 8.  x sc point
 * 9.  y sc point
 * 10. z sc point
 * 11. potential
 *
 * @author kahneg1
 *
 */
public abstract class LidarSqlDatabaseGenerator
{
    static private SqlManager db = null;

    private void createTable()
    {
        System.out.println("creating database");
        try {

            //make a table
            try
            {
                db.dropTable("lidar");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table lidar(" +
                    "UTC bigint PRIMARY KEY, " +
                    "xclosest real, " +
                    "yclosest real, " +
                    "zclosest real, " +
                    "xtarget real, " +
                    "ytarget real, " +
                    "ztarget real, " +
                    "xsc real, " +
                    "ysc real, " +
                    "zsc real, " +
                    "potential real" +
                    ")"
                );
        } catch (SQLException ex2) {

            //ignore
            ex2.printStackTrace();  // second time we run program
            //  should throw execption since table
            // already there
            //
            // this will have no effect on the db
        }
    }

    abstract protected SmallBodyModel getSmallBodyModel();
    abstract protected int[] getXYZIndices();

    abstract protected int[] getSpacecraftIndices();

    /**
     * For Eros NLR data, the spacecraft position is in spherical coordinates,
     * not Cartesian. Hence we need this function.
     * @return
     */
    abstract protected boolean isSpacecraftInSphericalCoordinates();

    abstract protected int getTimeIndex();

    abstract protected int getNoiseIndex();

    abstract protected String getFileListPath();

    abstract protected int getNumberHeaderLines();

    /**
     * Return whether or not the units of the lidar points are in meters. If false
     * they are assumed to be in kilometers.
     * @return
     */
    abstract protected boolean isInMeters();

    abstract protected int getPotentialIndex();

    abstract protected String getDatabasePath();

    public void run()
    {
        NativeLibraryLoader.loadVtkLibraries();

        SmallBodyModel smallBodyModel = getSmallBodyModel();

        String lidarFileList = getFileListPath();

        ArrayList<String> lidarFiles = null;
        try {
            lidarFiles = FileUtil.getFileLinesAsStringList(lidarFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        PreparedStatement msiInsert = null;

        try
        {
            db = new SqlManager("org.h2.Driver", "jdbc:h2:" + getDatabasePath());

            createTable();

            msiInsert = db.preparedStatement(
            "insert into lidar values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

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

                System.out.println("Begin processing file " + filename + " - " + filecount + " / " + lidarFiles.size());

                InputStream fs = new FileInputStream(filename);
                if (filename.toLowerCase().endsWith(".gz"))
                    fs = new GZIPInputStream(fs);
                InputStreamReader isr = new InputStreamReader(fs);
                BufferedReader in = new BufferedReader(isr);

                for (int i=0; i<getNumberHeaderLines(); ++i)
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

                    long time = new DateTime(vals[timeindex], DateTimeZone.UTC).getMillis();

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

                    msiInsert.setLong(1, time);
                    msiInsert.setFloat(2, (float)closest[0]);
                    msiInsert.setFloat(3, (float)closest[1]);
                    msiInsert.setFloat(4, (float)closest[2]);
                    msiInsert.setFloat(5, (float)x);
                    msiInsert.setFloat(6, (float)y);
                    msiInsert.setFloat(7, (float)z);
                    msiInsert.setFloat(8, (float)scx);
                    msiInsert.setFloat(9, (float)scy);
                    msiInsert.setFloat(10, (float)scz);
                    msiInsert.setFloat(11, (float)potential);

                    msiInsert.executeUpdate();
                }

                ++filecount;
            }

            db.shutdown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
