package edu.jhuapl.near.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkObject;
import vtk.vtkPolyData;

import edu.jhuapl.near.app.SmallBodyModel;
import edu.jhuapl.near.app.SmallBodyViewConfig;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;

public class NisDatabaseGeneratorSql
{
    static private final String NisSpectraTable = "nisspectra_beta2";
    static private final String NisCubesTable = "niscubes_beta2";

    static private SqlManager db = null;
    static private PreparedStatement nisInsert = null;
    static private PreparedStatement nisInsert2 = null;
    static private SmallBodyModel erosModel;
    //static private vtkPolyDataReader footprintReader;
    static private vtkPolyData footprintPolyData;
    //static private double[] meanPlateSizes;

    private static void createNISTables()
    {
        try {

            //make a table
            try
            {
                db.dropTable(NisSpectraTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + NisSpectraTable + "(" +
                    "id int PRIMARY KEY, " +
                    "year smallint, " +
                    "day smallint, " +
                    "midtime bigint, " +
                    "minincidence double," +
                    "maxincidence double," +
                    "minemission double," +
                    "maxemission double," +
                    "minphase double," +
                    "maxphase double," +
                    "range double, " +
                    "polygon_type_flag smallint)"
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

    private static void createNISTablesCubes()
    {
        try {

            //make a table
            try
            {
                db.dropTable(NisCubesTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + NisCubesTable + "(" +
                    "id int PRIMARY KEY, " +
                    "nisspectrumid int, " +
                    "cubeid int)"
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

    private static void populateNISTables(ArrayList<String> nisFiles) throws SQLException, IOException
    {
        int count = 0;
        for (String filename : nisFiles)
        {
            // Don't check if all Nis files exist here, since we want to allow searches on spectra
            // that don't intersect the asteroid

            System.out.println("starting nis " + count++ + "  " + filename);

            String dayOfYearStr = "";
            String yearStr = "";

            File origFile = new File(filename);
            File f = origFile;

            f = f.getParentFile();
            dayOfYearStr = f.getName();

            f = f.getParentFile();
            yearStr = f.getName();


            NISSpectrum nisSpectrum = new NISSpectrum(origFile, erosModel);

            if (nisInsert == null)
            {
                nisInsert = db.preparedStatement(
                        "insert into " + NisSpectraTable + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime midtime = new DateTime(nisSpectrum.getDateTime().toString(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //time = time.substring(0, 10) + " " + time.substring(11, time.length());

            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
            System.out.println("year: " + yearStr);
            System.out.println("dayofyear: " + dayOfYearStr);
            System.out.println("midtime: " + midtime);
            System.out.println("minIncidence: " + nisSpectrum.getMinIncidence());
            System.out.println("maxIncidence: " + nisSpectrum.getMaxIncidence());
            System.out.println("minEmission: " + nisSpectrum.getMinEmission());
            System.out.println("maxEmission: " + nisSpectrum.getMaxEmission());
            System.out.println("minPhase: " + nisSpectrum.getMinPhase());
            System.out.println("maxPhase: " + nisSpectrum.getMaxPhase());
            System.out.println("range: " + nisSpectrum.getRange());
            System.out.println("polygon type: " + nisSpectrum.getPolygonTypeFlag());
            System.out.println(" ");


            nisInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            nisInsert.setShort(2, Short.parseShort(yearStr));
            nisInsert.setShort(3, Short.parseShort(dayOfYearStr));
            nisInsert.setLong(4, midtime.getMillis());
            nisInsert.setDouble(5, nisSpectrum.getMinIncidence());
            nisInsert.setDouble(6, nisSpectrum.getMaxIncidence());
            nisInsert.setDouble(7, nisSpectrum.getMinEmission());
            nisInsert.setDouble(8, nisSpectrum.getMaxEmission());
            nisInsert.setDouble(9, nisSpectrum.getMinPhase());
            nisInsert.setDouble(10, nisSpectrum.getMaxPhase());
            nisInsert.setDouble(11, nisSpectrum.getRange());
            nisInsert.setShort(12, nisSpectrum.getPolygonTypeFlag());

            nisInsert.executeUpdate();
        }
    }

    private static void populateNISTablesCubes(ArrayList<String> nisFiles) throws SQLException, IOException
    {
        int count = 0;
        int filecount = 0;
        for (String filename : nisFiles)
        {
            boolean filesExist = checkIfAllNisFilesExist(filename);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting nis " + filename + " " + filecount++ + "/" + nisFiles.size());

//            String dayOfYearStr = "";
//            String yearStr = "";

            File origFile = new File(filename);
//            File f = origFile;

//            f = f.getParentFile();
//            dayOfYearStr = f.getName();

//            f = f.getParentFile();
//            yearStr = f.getName();

            NISSpectrum nisSpectrum = new NISSpectrum(origFile, erosModel);

            nisSpectrum.generateFootprint();

            if (footprintPolyData == null)
                footprintPolyData = new vtkPolyData();
            footprintPolyData.DeepCopy(nisSpectrum.getUnshiftedFootprint());
            footprintPolyData.ComputeBounds();


            if (nisInsert2 == null)
            {
                nisInsert2 = db.preparedStatement(
                        "insert into " + NisCubesTable + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = erosModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                nisInsert2.setInt(1, count);
                nisInsert2.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
                nisInsert2.setInt(3, i);

                nisInsert2.executeUpdate();

                ++count;
            }

            nisSpectrum.Delete();
            //System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    static boolean checkIfAllNisFilesExist(String line)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

//        String name = line.substring(0, line.length()-4) + "_FOOTPRINT.VTK";
//        file = new File(name);
//        if (!file.exists())
//            return false;

        return true;
    }
/*
    private static void computeMeanPlateSizeAtAllResolutions() throws IOException
    {
        int numRes = erosModel.getNumberResolutionLevels();

        meanPlateSizes = new double[numRes];

        for (int i=0; i<numRes; ++i)
        {
            erosModel.setModelResolution(i);

            meanPlateSizes[i] = erosModel.computeLargestSmallestMeanEdgeLength()[2];
        }
    }

    private static int findOptimalResolution(MSIImage image)
    {
        // First get the pixel size.
        double horiz = image.getMinimumHorizontalPixelScale();
        double vert = image.getMinimumVerticalPixelScale();
        double pixelSize = Math.min(horiz, vert);

        System.out.println("pixel size " + pixelSize);
        int numRes = erosModel.getNumberResolutionLevels();
        for (int i=0; i<numRes; ++i)
        {
            if (pixelSize >= meanPlateSizes[i])
                return i;
        }

        return numRes - 1;
    }
*/


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        erosModel = new Eros(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL));

//        computeMeanPlateSizeAtAllResolutions();

        String nisFileList=args[0];
        int mode = Integer.parseInt(args[1]);

        ArrayList<String> nisFiles = null;
        try {
            nisFiles = FileUtil.getFileLinesAsStringList(nisFileList);
        } catch (IOException e2) {
            e2.printStackTrace();
            return;
        }

        try
        {
            db = new SqlManager(null);
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }

        if (mode == 5 || mode == 0)
            createNISTables();
        else if (mode == 6 || mode == 0)
            createNISTablesCubes();


        try
        {
            if (mode == 5 || mode == 0)
                populateNISTables(nisFiles);
            else if (mode == 6 || mode == 0)
                populateNISTablesCubes(nisFiles);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }


        try
        {
            db.shutdown();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
