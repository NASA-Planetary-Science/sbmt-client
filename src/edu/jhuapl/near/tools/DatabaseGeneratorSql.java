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

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class DatabaseGeneratorSql
{
    private SqlManager db = null;
    private SmallBodyModel smallBodyModel;
    private SmallBodyConfig smallBodyConfig;
    private String betaSuffix = "_beta";
    private String databasePrefix;


    public DatabaseGeneratorSql(SmallBodyConfig smallBodyConfig, String databasePrefix)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.databasePrefix = databasePrefix;
    }

    private void createTables(String tableName)
    {
        System.out.println("creating table " + tableName);
        try {

            //make a table
            try
            {
                db.dropTable(tableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + tableName + "(" +
                    "id int PRIMARY KEY, " +
                    "filename char(128), " +
                    "starttime bigint, " +
                    "stoptime bigint, " +
                    "filter tinyint, " +
                    "camera tinyint, " +
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

    private void createTablesCubes(String tableName)
    {
        try {

            //make a table
            try
            {
                db.dropTable(tableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + tableName + "(" +
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

    private void populateTables(
            ArrayList<String> imageFiles,
            String tableName,
            String cubesTableName,
            PerspectiveImage.ImageSource imageSource) throws IOException, SQLException, FitsException
    {
        smallBodyModel.setModelResolution(0);

        PreparedStatement insertStatement = db.preparedStatement(
                "insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement insertStatement2 = db.preparedStatement(
                "insert into " + cubesTableName + " values (?, ?, ?)");

        int count = 0;
        int cubeTablePrimaryKey = 0;
        int primaryKey = 0;

        for (String filename : imageFiles)
        {

            System.out.println("\n\nstarting image " + count++ + "  " + filename);

            String keyName = filename;
            keyName = keyName.replace(".FIT", "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, imageSource);
            PerspectiveImage image = null;

            try
            {
                image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false);
                boolean filesExist = checkIfAllFilesExist(image, imageSource);
                if (filesExist == false)
                {
                    System.out.println("skipping image " + filename);
                    image.Delete();
                    System.gc();
                    System.out.println("deleted " + vtkGlobalJavaHash.GC());
                    continue;
                }
            }
            catch (Exception e)
            {
                System.out.println("skipping image " + filename);
                continue;
            }

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

            DateTime startTime = new DateTime(image.getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());

            System.out.println("id: " + primaryKey);
            System.out.println("filename: " + new File(filename).getName());
            System.out.println("starttime: " + startTime);
            System.out.println("stoptime: " + stopTime);
            System.out.println("filter: " + image.getFilter());
            System.out.println("camera: " + image.getCamera());
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

            insertStatement.setInt(1, primaryKey);
            insertStatement.setString(2, new File(filename).getName());
            insertStatement.setLong(3, startTime.getMillis());
            insertStatement.setLong(4, stopTime.getMillis());
            insertStatement.setByte(5, (byte)image.getFilter());
            insertStatement.setByte(6, (byte)image.getCamera());
            insertStatement.setDouble(7, image.getSpacecraftDistance());
            insertStatement.setDouble(8, image.getMinimumHorizontalPixelScale());
            insertStatement.setDouble(9, image.getMaximumHorizontalPixelScale());
            insertStatement.setDouble(10, image.getMinimumVerticalPixelScale());
            insertStatement.setDouble(11, image.getMaximumVerticalPixelScale());
            insertStatement.setBoolean(12, image.containsLimb());
            insertStatement.setDouble(13, image.getMinIncidence());
            insertStatement.setDouble(14, image.getMaxIncidence());
            insertStatement.setDouble(15, image.getMinEmission());
            insertStatement.setDouble(16, image.getMaxEmission());
            insertStatement.setDouble(17, image.getMinPhase());
            insertStatement.setDouble(18, image.getMaxPhase());

            insertStatement.executeUpdate();


            // Now populate cubes table
            vtkPolyData footprintPolyData = image.getUnshiftedFootprint();
            TreeSet<Integer> cubeIds = smallBodyModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + cubeTablePrimaryKey);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());
            for (Integer cubeid : cubeIds)
            {
                insertStatement2.setInt(1, cubeTablePrimaryKey);
                insertStatement2.setInt(2, primaryKey);
                insertStatement2.setInt(3, cubeid);

                insertStatement2.executeUpdate();

                ++cubeTablePrimaryKey;
            }

            ++primaryKey;

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    boolean checkIfAllFilesExist(PerspectiveImage image, PerspectiveImage.ImageSource source)
    {
        File fitfile = new File(image.getFitFileFullPath());
        if (!fitfile.exists())
            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File sumfile = new File(image.getSumfileFullPath());
            System.out.println(sumfile);
            if (!sumfile.exists())
                return false;

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1296 bytes long or less
            if (sumfile.length() <= 1296)
                return false;
        }
        else
        {
            File infofile = new File(image.getInfoFileFullPath());
            if (!infofile.exists())
                return false;
        }

        return true;
    }

    String getImagesGaskellTableNames()
    {
        return databasePrefix.toLowerCase() + "images_gaskell" + betaSuffix;
    }

    String getCubesGaskellTableNames()
    {
        return databasePrefix.toLowerCase() + "cubes_gaskell" + betaSuffix;
    }

    String getImagesPdsTableNames()
    {
        return databasePrefix.toLowerCase() + "images_pds" + betaSuffix;
    }

    String getCubesPdsTableNames()
    {
        return databasePrefix.toLowerCase() + "cubes_pds" + betaSuffix;
    }

    public void run(String fileList, int mode) throws IOException
    {
        smallBodyModel = ModelFactory.createSmallBodyModel(smallBodyConfig);

        ArrayList<String> files = null;
        try {
            files = FileUtil.getFileLinesAsStringList(fileList);
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

        String imagesGaskellTable = getImagesGaskellTableNames();
        String cubesGaskellTable = getCubesGaskellTableNames();
        String imagesPdsTable = getImagesPdsTableNames();
        String cubesPdsTable = getCubesPdsTableNames();

        if (mode == 1 || mode == 0)
        {
            createTables(imagesGaskellTable);
            createTablesCubes(cubesGaskellTable);
        }
        if (mode == 2 || mode == 0)
        {
            createTables(imagesPdsTable);
            createTablesCubes(cubesPdsTable);
        }

        try
        {
            if (mode == 1 || mode == 0)
            {
                populateTables(files, imagesGaskellTable, cubesGaskellTable, Image.ImageSource.GASKELL);
            }
            if (mode == 2 || mode == 0)
            {
                populateTables(files, imagesPdsTable, cubesPdsTable, Image.ImageSource.PDS);
            }
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

    private enum RunInfo
    {
        EROS(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/EROS/MSI/msiImageList.txt"),
        ITOKAWA(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.ITOKAWA, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/"),
        VESTA(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.VESTA, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/"),
        DEIMOS(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.DEIMOS, ShapeModelAuthor.THOMAS),
                "/project/nearsdc/data/"),
        PHOBOS(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt"),
        PHOBOSEXPERIMENTAL(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelAuthor.EXPERIMENTAL),
                "/project/nearsdc/data/", "phobosexp"),
        JUPITER(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.JUPITER, null),
                "/project/nearsdc/data/NEWHORIZONS/JUPITER/IMAGING/imagelist-fullpath.txt"),
        CALLISTO(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.CALLISTO, null),
                "/project/nearsdc/data/NEWHORIZONS/CALLISTO/IMAGING/imagelist-fullpath.txt"),
        EUROPA(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.EUROPA, null),
                "/project/nearsdc/data/NEWHORIZONS/EUROPA/IMAGING/imagelist-fullpath.txt"),
        GANYMEDE(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.GANYMEDE, null),
                "/project/nearsdc/data/NEWHORIZONS/GANYMEDE/IMAGING/imagelist-fullpath.txt"),
        IO(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.IO, null),
                "/project/nearsdc/data/NEWHORIZONS/IO/IMAGING/imagelist-fullpath.txt");

        public final SmallBodyConfig config;
        public final String pathToFileList;
        public final String databasePrefix;

        private RunInfo(SmallBodyConfig config, String pathToFileList)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = config.body.toString().toLowerCase();
        }

        private RunInfo(SmallBodyConfig config, String pathToFileList, String databasePrefix)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = databasePrefix;
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        //System.setProperty("java.awt.headless", "true");
        Configuration.setAPLVersion(true);
        NativeLibraryLoader.loadVtkLibraries();

        int mode = Integer.parseInt(args[0]);

        for (int i=1; i<args.length; ++i)
        {
            String body = args[i];
            RunInfo ri = RunInfo.valueOf(body.toUpperCase());
            DatabaseGeneratorSql generator = new DatabaseGeneratorSql(ri.config, ri.databasePrefix);
            generator.run(ri.pathToFileList, mode);
        }
    }
}
