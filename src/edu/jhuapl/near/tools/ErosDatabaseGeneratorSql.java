package edu.jhuapl.near.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeSet;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkGlobalJavaHash;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ShapeModelAuthor;
import edu.jhuapl.near.model.ModelFactory.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ErosDatabaseGeneratorSql
{
    static private final String MsiImagesPdsTable = "msiimages_beta2";
    static private final String MsiImagesGaskellTable = "msiimages_gaskell_beta3";
    static private final String MsiCubesPdsTable = "msicubes_beta2";
    static private final String MsiCubesGaskellTable = "msicubes_gaskell_beta3";
    static private final String NisSpectraTable = "nisspectra_beta2";
    static private final String NisCubesTable = "niscubes_beta2";

    static private SqlManager db = null;
    static private PreparedStatement msiInsert = null;
    static private PreparedStatement msiInsert2 = null;
    static private PreparedStatement nisInsert = null;
    static private PreparedStatement nisInsert2 = null;
    static private SmallBodyModel erosModel;
    //static private vtkPolyDataReader footprintReader;
    static private vtkPolyData footprintPolyData;
    //static private double[] meanPlateSizes;

    private static void createMSITables(String msiTableName)
    {
        System.out.println("creating msi");
        try {

            //make a table
            try
            {
                db.dropTable(msiTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + msiTableName + "(" +
                    "id int PRIMARY KEY, " +
                    "year smallint, " +
                    "day smallint, " +
                    "starttime bigint, " +
                    "stoptime bigint, " +
                    "filter tinyint, " +
                    "iofcif tinyint," +
                    "target_center_distance double," +
                    "min_horizontal_pixel_scale double," +
                    "max_horizontal_pixel_scale double," +
                    "min_vertical_pixel_scale double," +
                    "max_vertical_pixel_scale double," +
                    "has_limb boolean," +
                    "minincidence double," +
                    "maxincidence double," +
                    "minemission double," +
                    "maxemission double," +
                    "minphase double," +
                    "maxphase double" +
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

    private static void createMSITablesCubes(String msiTableName)
    {
        try {

            //make a table
            try
            {
                db.dropTable(msiTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + msiTableName + "(" +
                    "id int PRIMARY KEY, " +
                    "imageid int, " +
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

    private static void populateMSITables(
            ArrayList<String> msiFiles,
            String msiTableName,
            MSIImage.ImageSource msiSource) throws IOException, SQLException, FitsException
    {
        erosModel.setModelResolution(3);
        MSIImage.setGenerateFootprint(true);
        MSIImage.setFootprintIsOnLocalDisk(true);

        int count = 0;

        for (String filename : msiFiles)
        {
            boolean filesExist = checkIfAllMsiFilesExist(filename, msiSource);
            if (filesExist == false)
                continue;

            System.out.println("starting msi " + count++ + "  " + filename);

            byte iof_or_cif = -1;
            String dayOfYearStr = "";
            String yearStr = "";

            File origFile = new File(filename);
            File f = origFile;

            f = f.getParentFile();
            if (f.getName().equals("iofdbl"))
                iof_or_cif = 0;
            else if (f.getName().equals("cifdbl"))
                iof_or_cif = 1;

            f = f.getParentFile();
            dayOfYearStr = f.getName();

            f = f.getParentFile();
            yearStr = f.getName();

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, msiSource);
            MSIImage image = new MSIImage(key, erosModel, false, rootFolder);

            // Calling this forces the calculation of incidence, emission, phase, and pixel scale
            image.getProperties();

            /*
            int res = findOptimalResolution(image);

            System.out.println("Optimal resolution " + res);

            if (res != erosModel.getModelResolution())
            {
                System.out.println("Changing resolution to " + res);
                erosModel.setModelResolution(res);
                image.Delete();
                image = new MSIImage(origFile, erosModel, msiSource);
                image.getProperties();
            }
            */

            if (msiInsert == null)
            {
                msiInsert = db.preparedStatement(
//                    "insert into msiimages values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    "insert into " + msiTableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime startTime = new DateTime(image.getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());


            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
            System.out.println("year: " + yearStr);
            System.out.println("dayofyear: " + dayOfYearStr);
            System.out.println("iof_or_cif: " + iof_or_cif);
            System.out.println("starttime: " + startTime);
            System.out.println("stoptime: " + stopTime);
            System.out.println("filter: " + Integer.parseInt(origFile.getName().substring(12, 13)));
            System.out.println("iof_or_cif: " + iof_or_cif);
            System.out.println("TARGET_CENTER_DISTANCE: " + image.getSpacecraftDistance());
            System.out.println("Min HORIZONTAL_PIXEL_SCALE: " + image.getMinimumHorizontalPixelScale());
            System.out.println("Max HORIZONTAL_PIXEL_SCALE: " + image.getMaximumHorizontalPixelScale());
            System.out.println("Min VERTICAL_PIXEL_SCALE: " + image.getMinimumVerticalPixelScale());
            System.out.println("Max VERTICAL_PIXEL_SCALE: " + image.getMaximumVerticalPixelScale());
            System.out.println("hasLimb: " /*+ image.containsLimb()*/);
            System.out.println("minIncidence: " + image.getMinIncidence());
            System.out.println("maxIncidence: " + image.getMaxIncidence());
            System.out.println("minEmission: " + image.getMinEmission());
            System.out.println("maxEmission: " + image.getMaxEmission());
            System.out.println("minPhase: " + image.getMinPhase());
            System.out.println("maxPhase: " + image.getMaxPhase());

            msiInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            msiInsert.setShort(2, Short.parseShort(yearStr));
            msiInsert.setShort(3, Short.parseShort(dayOfYearStr));
            msiInsert.setLong(4, startTime.getMillis());
            msiInsert.setLong(5, stopTime.getMillis());
            msiInsert.setByte(6, Byte.parseByte(origFile.getName().substring(12, 13)));
            msiInsert.setByte(7, iof_or_cif);
            msiInsert.setDouble(8, image.getSpacecraftDistance());
            msiInsert.setDouble(9, image.getMinimumHorizontalPixelScale());
            msiInsert.setDouble(10, image.getMaximumHorizontalPixelScale());
            msiInsert.setDouble(11, image.getMinimumVerticalPixelScale());
            msiInsert.setDouble(12, image.getMaximumVerticalPixelScale());
            msiInsert.setBoolean(13, image.containsLimb());
            msiInsert.setDouble(14, image.getMinIncidence());
            msiInsert.setDouble(15, image.getMaxIncidence());
            msiInsert.setDouble(16, image.getMinEmission());
            msiInsert.setDouble(17, image.getMaxEmission());
            msiInsert.setDouble(18, image.getMinPhase());
            msiInsert.setDouble(19, image.getMaxPhase());

            msiInsert.executeUpdate();


            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
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

    private static void populateMSITablesCubes(
            ArrayList<String> msiFiles,
            String msiTableName,
            MSIImage.ImageSource msiSource) throws SQLException, IOException, FitsException
    {
        erosModel.setModelResolution(0);
        MSIImage.setGenerateFootprint(true);
        MSIImage.setFootprintIsOnLocalDisk(true);

        int count = 0;
        for (String filename : msiFiles)
        {
            boolean filesExist = checkIfAllMsiFilesExist(filename, msiSource);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting msi " + filename);

//            String dayOfYearStr = "";
//            String yearStr = "";

            File origFile = new File(filename);
//            File f = origFile;

//            f = f.getParentFile();
//            dayOfYearStr = f.getName();

//            f = f.getParentFile();
//            yearStr = f.getName();

//            String vtkfile = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
//
//            if (footprintReader == null)
//                footprintReader = new vtkPolyDataReader();
//            footprintReader.SetFileName(vtkfile);
//            footprintReader.Update();
//
            if (footprintPolyData == null)
                footprintPolyData = new vtkPolyData();
//            footprintPolyData.DeepCopy(footprintReader.GetOutput());
//            footprintPolyData.ComputeBounds();

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, msiSource);
            MSIImage image = new MSIImage(key, erosModel, false, rootFolder);

            image.loadFootprint();
            footprintPolyData.DeepCopy(image.getUnshiftedFootprint());

            if (msiInsert2 == null)
            {
                msiInsert2 = db.preparedStatement(
                        "insert into " + msiTableName + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = erosModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                msiInsert2.setInt(1, count);
                msiInsert2.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
                msiInsert2.setInt(3, i);

                msiInsert2.executeUpdate();

                ++count;
            }

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
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
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    static boolean checkIfAllMsiFilesExist(String line, MSIImage.ImageSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

        String name = line.substring(0, line.length()-4) + ".LBL";
        file = new File(name);
        if (!file.exists())
            return false;

        name = line.substring(0, line.length()-4) + "_DDR.LBL";
        file = new File(name);
        if (!file.exists())
            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File msirootdir = (new File(line)).getParentFile().getParentFile().getParentFile().getParentFile();
            String msiId = (new File(line)).getName().substring(0, 11);
            name = msirootdir.getAbsolutePath() + "/sumfiles/" + msiId + ".SUM";
            file = new File(name);
            if (!file.exists())
                return false;
        }

        //name = line.substring(0, line.length()-4) + "_DDR.IMG.gz";
        //file = new File(name);
        //if (!file.exists())
        //    return false;

        //name = line.substring(0, line.length()-4) + "_BOUNDARY.VTK";
        //file = new File(name);
        //if (!file.exists())
        //    return false;

        //name = line.substring(0, line.length()-4) + "_FOOTPRINT.VTK";
        //file = new File(name);
        //if (!file.exists())
        //    return false;

        return true;
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

        erosModel = new Eros(ModelFactory.getModelConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL));

//        computeMeanPlateSizeAtAllResolutions();

        String msiFileList=args[0];
        String nisFileList=args[1];
        int mode = Integer.parseInt(args[2]);

        ArrayList<String> msiFiles = null;
        ArrayList<String> nisFiles = null;
        try {
            msiFiles = FileUtil.getFileLinesAsStringList(msiFileList);
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

        if (mode == 1 || mode == 0)
            createMSITables(MsiImagesPdsTable);
        else if (mode == 2 || mode == 0)
            createMSITables(MsiImagesGaskellTable);
        else if (mode == 3 || mode == 0)
            createMSITablesCubes(MsiCubesPdsTable);
        else if (mode == 4 || mode == 0)
            createMSITablesCubes(MsiCubesGaskellTable);
        else if (mode == 5 || mode == 0)
            createNISTables();
        else if (mode == 6 || mode == 0)
            createNISTablesCubes();


        try
        {
            if (mode == 1 || mode == 0)
                populateMSITables(msiFiles, MsiImagesPdsTable, MSIImage.ImageSource.PDS);
            else if (mode == 2 || mode == 0)
                populateMSITables(msiFiles, MsiImagesGaskellTable, MSIImage.ImageSource.GASKELL);
            else if (mode == 3 || mode == 0)
                populateMSITablesCubes(msiFiles, MsiCubesPdsTable, MSIImage.ImageSource.PDS);
            else if (mode == 4 || mode == 0)
                populateMSITablesCubes(msiFiles, MsiCubesGaskellTable, MSIImage.ImageSource.GASKELL);
            else if (mode == 5 || mode == 0)
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
