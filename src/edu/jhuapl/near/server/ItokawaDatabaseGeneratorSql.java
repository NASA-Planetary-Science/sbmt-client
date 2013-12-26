package edu.jhuapl.near.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeSet;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkGlobalJavaHash;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class ItokawaDatabaseGeneratorSql
{
    static private final String AmicaImagesGaskellTable = "amicaimages_gaskell_beta";
    static private final String AmicaCubesGaskellTable = "amicacubes_gaskell_beta";
    static private final String AmicaImagesPdsTable = "amicaimages_pds_beta";
    static private final String AmicaCubesPdsTable = "amicacubes_pds_beta";

    static private SqlManager db = null;
    static private PreparedStatement amicaInsert = null;
    static private PreparedStatement amicaInsert2 = null;
    static private SmallBodyModel itokawaModel;
    //static private vtkPolyDataReader footprintReader;
    static private vtkPolyData footprintPolyData;
    //static private double[] meanPlateSizes;

    // Files listed in Gaskell's INERTIAL.TXT file. Only these are processed.
    private static ArrayList<String> inertialFileList = new ArrayList<String>();

    private static void createAmicaTables(String amicaTableName)
    {
        System.out.println("creating amica");
        try {

            //make a table
            try
            {
                db.dropTable(amicaTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + amicaTableName + "(" +
                    "id bigint PRIMARY KEY, " +
                    "filename char(30), " +
                    "starttime bigint, " +
                    "stoptime bigint, " +
                    "filter tinyint, " +
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

    private static void createAmicaTablesCubes(String amicaTableName)
    {
        try {

            //make a table
            try
            {
                db.dropTable(amicaTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + amicaTableName + "(" +
                    "id int PRIMARY KEY, " +
                    "imageid bigint, " +
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

    private static void populateAmicaTables(
            ArrayList<String> amicaFiles,
            String amicaTableName,
            AmicaImage.ImageSource amicaSource) throws IOException, SQLException, FitsException
    {
        itokawaModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;

        for (String filename : amicaFiles)
        {
            boolean filesExist = checkIfAllAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
                continue;

            System.out.println("starting amica " + count++ + "  " + filename);

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, amicaSource);
            AmicaImage image = new AmicaImage(key, itokawaModel, false, rootFolder);

            image.loadFootprint();
            if (image.getUnshiftedFootprint().GetNumberOfCells() == 0)
            {
                System.out.println("skipping this image since no intersecting cells");
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkGlobalJavaHash.GC());
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }

            // Calling this forces the calculation of incidence, emission, phase, and pixel scale
            image.getProperties();

            if (amicaInsert == null)
            {
                amicaInsert = db.preparedStatement(
                    "insert into " + amicaTableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime startTime = new DateTime(image.getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());

            System.out.println("id: " + Long.parseLong(origFile.getName().substring(3, 13)));
            System.out.println("filename: " + origFile.getName());
            System.out.println("starttime: " + startTime);
            System.out.println("stoptime: " + stopTime);
            System.out.println("filter: " + image.getFilter());
            System.out.println("TARGET_CENTER_DISTANCE: " + image.getSpacecraftDistance());
            System.out.println("Min HORIZONTAL_PIXEL_SCALE: " + image.getMinimumHorizontalPixelScale());
            System.out.println("Max HORIZONTAL_PIXEL_SCALE: " + image.getMaximumHorizontalPixelScale());
            System.out.println("Min VERTICAL_PIXEL_SCALE: " + image.getMinimumVerticalPixelScale());
            System.out.println("Max VERTICAL_PIXEL_SCALE: " + image.getMaximumVerticalPixelScale());
            System.out.println("hasLimb: " + image.containsLimb());
            System.out.println("minIncidence: " + image.getMinIncidence());
            System.out.println("maxIncidence: " + image.getMaxIncidence());
            System.out.println("minEmission: " + image.getMinEmission());
            System.out.println("maxEmission: " + image.getMaxEmission());
            System.out.println("minPhase: " + image.getMinPhase());
            System.out.println("maxPhase: " + image.getMaxPhase());

            amicaInsert.setLong(1, Long.parseLong(origFile.getName().substring(3, 13), 10));
            amicaInsert.setString(2, origFile.getName());
            amicaInsert.setLong(3, startTime.getMillis());
            amicaInsert.setLong(4, stopTime.getMillis());
            amicaInsert.setByte(5, (byte)image.getFilter());
            amicaInsert.setDouble(6, image.getSpacecraftDistance());
            amicaInsert.setDouble(7, image.getMinimumHorizontalPixelScale());
            amicaInsert.setDouble(8, image.getMaximumHorizontalPixelScale());
            amicaInsert.setDouble(9, image.getMinimumVerticalPixelScale());
            amicaInsert.setDouble(10, image.getMaximumVerticalPixelScale());
            amicaInsert.setBoolean(11, image.containsLimb());
            amicaInsert.setDouble(12, image.getMinIncidence());
            amicaInsert.setDouble(13, image.getMaxIncidence());
            amicaInsert.setDouble(14, image.getMinEmission());
            amicaInsert.setDouble(15, image.getMaxEmission());
            amicaInsert.setDouble(16, image.getMinPhase());
            amicaInsert.setDouble(17, image.getMaxPhase());

            amicaInsert.executeUpdate();


            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    private static void populateAmicaTablesCubes(
            ArrayList<String> amicaFiles,
            String amicaTableName,
            PerspectiveImage.ImageSource amicaSource) throws SQLException, IOException, FitsException
    {
        itokawaModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;
        for (String filename : amicaFiles)
        {
            boolean filesExist = checkIfAllAmicaFilesExist(filename, amicaSource);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting amica " + filename);

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

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, amicaSource);
            AmicaImage image = new AmicaImage(key, itokawaModel, false, rootFolder);

            image.loadFootprint();
            footprintPolyData.DeepCopy(image.getUnshiftedFootprint());

            if (amicaInsert2 == null)
            {
                amicaInsert2 = db.preparedStatement(
                        "insert into " + amicaTableName + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = itokawaModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                amicaInsert2.setInt(1, count);
                amicaInsert2.setLong(2, Long.parseLong(origFile.getName().substring(3, 13), 10));
                amicaInsert2.setInt(3, i);

                amicaInsert2.executeUpdate();

                ++count;
            }

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
        }
    }

    private static void loadInertialFile(String inertialFilename) throws IOException
    {
        InputStream fs = new FileInputStream(inertialFilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        while ((line = in.readLine()) != null)
        {
            if (line.startsWith("N"))
            {
                String[] tokens = line.trim().split("\\s+");
                inertialFileList.add(tokens[0]);
            }
        }

        System.out.println("number of inertial files: " + inertialFileList.size());

        in.close();
    }

    static boolean checkIfAllAmicaFilesExist(String line, PerspectiveImage.ImageSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

        String name = line.substring(0, line.length()-4) + ".lbl";
        file = new File(name);
        if (!file.exists())
            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File amicarootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String amicaId = (new File(line)).getName().substring(3, 13);
            name = amicarootdir.getAbsolutePath() + "/sumfiles/N" + amicaId + ".SUM";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;

            // Only process files that are listed in Gaskell's INERTIAL.TXT file.
            if (!inertialFileList.contains("N" + amicaId))
            {
                System.out.println("N" + amicaId + " not in INERTIAL.TXT");
                return false;
            }
        }
        else
        {
            File amicarootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String amicaId = (new File(line)).getName();
            amicaId = amicaId.substring(0, amicaId.length()-4);
            name = amicarootdir.getAbsolutePath() + "/infofiles/" + amicaId + ".INFO";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;
        }

        return true;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();

        itokawaModel = new Itokawa(ModelFactory.getModelConfig(ModelFactory.ITOKAWA, ModelFactory.GASKELL));

        String amicaFileList=args[0];
        String inertialFilename = args[1];
        int mode = Integer.parseInt(args[2]);

        ArrayList<String> amicaFiles = null;
        try {
            amicaFiles = FileUtil.getFileLinesAsStringList(amicaFileList);
            loadInertialFile(inertialFilename);
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
            createAmicaTables(AmicaImagesGaskellTable);
        else if (mode == 2 || mode == 0)
            createAmicaTablesCubes(AmicaCubesGaskellTable);
        else if (mode == 3 || mode == 0)
            createAmicaTables(AmicaImagesPdsTable);
        else if (mode == 4 || mode == 0)
            createAmicaTablesCubes(AmicaCubesPdsTable);

        try
        {
            if (mode == 1 || mode == 0)
                populateAmicaTables(amicaFiles, AmicaImagesGaskellTable, Image.ImageSource.GASKELL);
            else if (mode == 2 || mode == 0)
                populateAmicaTablesCubes(amicaFiles, AmicaCubesGaskellTable, Image.ImageSource.GASKELL);
            else if (mode == 3 || mode == 0)
                populateAmicaTables(amicaFiles, AmicaImagesPdsTable, Image.ImageSource.PDS);
            else if (mode == 4 || mode == 0)
                populateAmicaTablesCubes(amicaFiles, AmicaCubesPdsTable, Image.ImageSource.PDS);
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
