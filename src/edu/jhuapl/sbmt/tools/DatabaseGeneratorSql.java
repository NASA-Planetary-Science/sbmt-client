package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkObject;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.app.SbmtModelFactory;
import edu.jhuapl.sbmt.app.SmallBodyModel;
import edu.jhuapl.sbmt.app.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;
import edu.jhuapl.sbmt.model.image.Image.ImageKey;

public class DatabaseGeneratorSql
{
    private SqlManager db = null;
    private SmallBodyModel smallBodyModel;
    private SmallBodyViewConfig smallBodyConfig;
    private String betaSuffix = "_beta";
    private String databasePrefix;
    private boolean appendTables;
    private boolean modifyMain;


    public DatabaseGeneratorSql(SmallBodyViewConfig smallBodyConfig, String databasePrefix, boolean appendTables, boolean modifyMain)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.databasePrefix = databasePrefix;
        this.appendTables = appendTables;
        this.modifyMain = modifyMain;
    }

    private void createTables(String tableName)
    {
        try {
            // Check to see if we should append to existing or not
            if(!appendTables){
                // Not appending, drop existing (if applicable) table first
                try
                {
                    db.dropTable(tableName);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Then create a new table if one does not already exist
            db.update(
                "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
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
            // Check to see if we should append to existing or not
            if(!appendTables){
                // Not appending, drop existing (if applicable) table first
                try
                {
                    db.dropTable(tableName);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Then create a new table if one does not already exist
            db.update(
                "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
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
            List<String> imageFiles,
            String tableName,
            String cubesTableName,
            ImageSource imageSource) throws IOException, SQLException, FitsException
    {
        smallBodyModel.setModelResolution(0);
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();

        PreparedStatement insertStatement = db.preparedStatement(
                "insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement insertStatement2 = db.preparedStatement(
                "insert into " + cubesTableName + " values (?, ?, ?)");

        int count = 0;
        int primaryKey = 0;
        int cubeTablePrimaryKey = 0;

        // If appending, search table for next consecutive key to use, if no entries exist then start at 0
        List<List<Object>> queryResult;
        if(appendTables){
            queryResult = db.query("SELECT MAX(id) FROM `" + tableName + "`");
            if(queryResult != null && !queryResult.isEmpty() &&
                    queryResult.get(0) != null && !queryResult.get(0).isEmpty() &&
                    queryResult.get(0).get(0) != null){
                primaryKey = (int)queryResult.get(0).get(0) + 1;
            }
            queryResult = db.query("SELECT MAX(id) FROM `" + cubesTableName + "`");
            if(queryResult != null && !queryResult.isEmpty() &&
                    queryResult.get(0) != null && !queryResult.get(0).isEmpty() &&
                    queryResult.get(0).get(0) != null){
                cubeTablePrimaryKey = (int)queryResult.get(0).get(0) + 1;
            }
        }

        for (String filename : imageFiles)
        {
            // Increment image count (for status message purposes only)
            count++;

            // If appending and there is already an entry for the filename then skip
            if(appendTables){
                queryResult = db.query("SELECT * FROM `" + tableName + "` WHERE `filename` = \"" +
                    new File(filename).getName() + "\"");
                if(queryResult != null && !queryResult.isEmpty() &&
                    queryResult.get(0) != null && !queryResult.get(0).isEmpty() &&
                    queryResult.get(0).get(0) != null){

                    System.out.println("\n\nskipping image " + count + "  " + filename + ", already in table");
                    continue;
                }
            }

            // If we got to this point, the image is not already in the table so we need to create a new entry
            System.out.println("\n\nstarting image " + count + "  " + filename);

            String keyName = filename;
            keyName = keyName.replace(".FIT", "");
            keyName = keyName.replace(".fit", "");
            ImageKey key = new ImageKey(keyName, imageSource, config.imagingInstruments[0]);
            PerspectiveImage image = null;

            try
            {
                image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
                boolean filesExist = checkIfAllFilesExist(image, imageSource);
                if (filesExist == false)
                {
                    System.out.println("file not found, skipping image " + filename);
                    image.Delete();
                    System.gc();
                    System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                    continue;
                }
            }
            catch (Exception e)
            {
                System.out.println("exception, skipping image " + filename);
                e.printStackTrace();
                continue;
            }

            image.loadFootprint();
            if (image.getUnshiftedFootprint() == null)
            {
                // In this case if image.loadFootprint() finds no frustum intersection
                System.out.println("skipping this image since no frustum intersection with body");
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }
            else if (image.getUnshiftedFootprint().GetNumberOfCells() == 0)
            {
                System.out.println("skipping this image since no intersecting cells");
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
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
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    boolean checkIfAllFilesExist(PerspectiveImage image, ImageSource source)
    {
        File fitfile = new File(image.getFitFileFullPath());
        System.out.println("Fit file full path: " + fitfile.getAbsolutePath());
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
            // are 1296 bytes long or less. If it's very small in size though it is probably
            // a sumfile that was generated via another method so keep it.
            //if (sumfile.length() <= 1296 && sumfile.length() >= 500)
            //    return false;
        }
        else
        {
            File infofile = new File(image.getInfoFileFullPath());
            System.out.println("Infofile full path: " + infofile.getAbsolutePath());
            if (!infofile.exists())
                return false;
        }

        return true;
    }

    String getImagesGaskellTableNames()
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "images_gaskell";
        }else{
            return databasePrefix.toLowerCase() + "images_gaskell" + betaSuffix;
        }
    }

    String getCubesGaskellTableNames()
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "cubes_gaskell";
        }else{
            return databasePrefix.toLowerCase() + "cubes_gaskell" + betaSuffix;
        }
    }

    String getImagesPdsTableNames()
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "images_pds";
        }else{
            return databasePrefix.toLowerCase() + "images_pds" + betaSuffix;
        }
    }

    String getCubesPdsTableNames()
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "cubes_pds";
        }else{
            return databasePrefix.toLowerCase() + "cubes_pds" + betaSuffix;
        }
    }

    public void run(String fileList, int mode) throws IOException
    {
        smallBodyModel = SbmtModelFactory.createSmallBodyModel(smallBodyConfig);

        List<String> files = null;
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
                populateTables(files, imagesGaskellTable, cubesGaskellTable, ImageSource.GASKELL);
            }
            if (mode == 2 || mode == 0)
            {
                populateTables(files, imagesPdsTable, cubesPdsTable, ImageSource.SPICE);
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
        EROS(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/EROS/MSI/msiImageList.txt"),
        ITOKAWA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.ITOKAWA, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/ITOKAWA/AMICA/imagelist.txt", "amica"),
        VESTA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.VESTA, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/VESTA/FC/uniqFcFiles.txt", "fc"),
        CERES(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.CERES, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt", "ceres"),
        DEIMOSEXPERIMENTAL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.DEIMOS, ShapeModelAuthor.THOMAS),
                "/project/nearsdc/data/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/imagelist-fullpath.txt", "deimos"),
        PHOBOS(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelAuthor.GASKELL),
                "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt"),
        PHOBOSEXPERIMENTAL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelAuthor.EXPERIMENTAL),
                "/project/nearsdc/data/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/imagelist.txt", "phobosexp"),
        _67P(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelAuthor.GASKELL, "SHAP5 V0.3"),
                "/project/nearsdc/data/GASKELL/67P/IMAGING/imagelist-fullpath.txt", "67p"),
        _67P_DLR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelAuthor.DLR, "SHAP4S"),
                "/project/nearsdc/data/DLR/67P/IMAGING/imagelist-fullpath.txt", "67p_dlr"),
        _67P_V2(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelAuthor.GASKELL, "V2"),
                "/project/nearsdc/data/GASKELL/67P_V2/IMAGING/imagelist-fullpath.txt", "67p_v2"),
        _67P_V3(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelAuthor.GASKELL, "V3"),
                "/project/nearsdc/data/GASKELL/67P_V3/IMAGING/imagelist-fullpath.txt", "67p_v3"),
        JUPITER(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.JUPITER, null),
                "/project/nearsdc/data/NEWHORIZONS/JUPITER/IMAGING/imagelist-fullpath.txt"),
        CALLISTO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.CALLISTO, null),
                "/project/nearsdc/data/NEWHORIZONS/CALLISTO/IMAGING/imagelist-fullpath.txt"),
        EUROPA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EUROPA, null),
                "/project/nearsdc/data/NEWHORIZONS/EUROPA/IMAGING/imagelist-fullpath.txt"),
        GANYMEDE(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.GANYMEDE, null),
                "/project/nearsdc/data/NEWHORIZONS/GANYMEDE/IMAGING/imagelist-fullpath.txt"),
        IO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.IO, null),
                "/project/nearsdc/data/NEWHORIZONS/IO/IMAGING/imagelist-fullpath.txt"),
        RQ36_MAP(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"),
                "/project/nearsdc/data/GASKELL/RQ36_V3/MAPCAM/imagelist-fullpath.txt", "RQ36_MAP"),
        RQ36_POLY(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V3 Image"),
                "/project/nearsdc/data/GASKELL/RQ36_V3/POLYCAM/imagelist-fullpath.txt", "RQ36_POLY"),
//        RQ36V4_MAP(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V4 Image"),
//               "/project/nearsdc/data/GASKELL/RQ36_V4/MAPCAM/imagelist-fullpath.txt", "RQ36V4_MAP"),
//        RQ36V4_POLY(SmallBodyConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelAuthor.GASKELL, "V4 Image"),
//                "/project/nearsdc/data/GASKELL/RQ36_V4/POLYCAM/imagelist-fullpath.txt", "RQ36V4_POLY"),
        PLUTO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PLUTO, null),
                "/project/nearsdc/data/NEWHORIZONS/PLUTO/IMAGING/imagelist-fullpath.txt");

        public final SmallBodyViewConfig config;
        public final String pathToFileList;
        public final String databasePrefix;

        private RunInfo(SmallBodyViewConfig config, String pathToFileList)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = config.body.toString().toLowerCase();
        }

        private RunInfo(SmallBodyViewConfig config, String pathToFileList, String databasePrefix)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = databasePrefix;
        }
    }

    private static void usage()
    {
        String o = "This program generates tables in the MySQL database for a given body.\n\n"
                + "Usage: DatabaseGeneratorSql [options] <mode> <shapemodel>\n\n"
                + "Where:\n"
                + "  <mode>\n"
                + "          Must be either 0, 1, or 2. If 0, then both Gaskell and PDS (SPICE) tables\n"
                + "          are generated. If 1, then only Gaskell tables are generated. If 2, then\n"
                + "          only PDS (SPICE) tables are generated.\n"
                + "  <shapemodel>\n"
                + "          shape model to process. Must be one of the values in the RunInfo enumeration\n"
                + "          such as EROS or ITOKAWA. If ALL is specified then the entire database is\n"
                + "          regenerated.\n"
                + "Options:\n"
                + "  --append-tables\n"
                + "          If specified, will check to see if database tables of the shape+mode already\n"
                + "          exist, create one if necessary, and append entries to that table as opposed\n"
                + "          to deleting existing tables and creating a new one from scratch.\n"
                + "  --modify-main\n"
                + "          If specified, will modify main tables directly instead of those with the\n"
                + "          _beta suffix.  Be very careful when enabling this option.\n"
                + "  --root-url <url>\n"
                + "          Root URL from which to get data from. Should begin with file:// to load\n"
                + "          data directly from file system rather than web server. Default value\n"
                + "          is file:///disks/d0180/htdocs-sbmt/internal/sbmt.\n\n";
        System.out.println(o);
        System.exit(1);
    }


    private static void authenticate()
    {
        Configuration.setAPLVersion(true);

        String username = null;
        String password = null;

        try
        {
            // First try to see if there's a password.txt file in ~/.neartool. Then try the folder
            // containing the runsbmt script.
            String jarLocation = SmallBodyMappingToolAPL.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String parent = new File(jarLocation).getParentFile().getParent();
            String[] passwordFilesToTry = {
                    Configuration.getApplicationDataDir() + File.separator + "password.txt",
                    parent + File.separator + "password.txt"
            };

            for (String passwordFile : passwordFilesToTry)
            {
                if (new File(passwordFile).exists())
                {
                    List<String> credentials = FileUtil.getFileLinesAsStringList(passwordFile);
                    if (credentials.size() >= 2)
                    {
                        String user = credentials.get(0);
                        String pass = credentials.get(1);

                        if (user != null && user.trim().length() > 0 && !user.trim().toLowerCase().contains("replace-with-") &&
                            pass != null && pass.trim().length() > 0)
                        {
                            username = user.trim();
                            password = pass.trim();
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }

        if (username != null && password != null)
        {
            Configuration.setupPasswordAuthentication(username, password);
        }
        else
        {
            System.out.println("Warning: no correctly formatted password file found. "
                    + "Continuing without password. Certain functionality may not work.");
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");
        Configuration.setAPLVersion(true);
        String rootURL = "file:///disks/d0180/htdocs-sbmt/internal/sbmt";
        boolean appendTables = false;
        boolean modifyMain = false;

        int i = 0;
        for (; i < args.length; ++i) {
            if (args[i].equals("--root-url")) {
                rootURL = args[++i];
            }
            else if (args[i].equals("--append-tables")){
                appendTables = true;
            }
            else if (args[i].equals("--modify-main")){
                modifyMain = true;
            }
            else {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 2;
        if (args.length - i != numberRequiredArgs)
            usage();

        int mode = Integer.parseInt(args[i++]);
        String body = args[i++];

        RunInfo[] runInfos = null;
        if (body.toUpperCase().equals("ALL"))
            runInfos = RunInfo.values();
        else
            runInfos = new RunInfo[]{RunInfo.valueOf(body.toUpperCase())};

        Configuration.setRootURL(rootURL);
        NativeLibraryLoader.loadVtkLibrariesHeadless();

        // authenticate
        authenticate();



        for (RunInfo ri : runInfos)
        {
            DatabaseGeneratorSql generator = new DatabaseGeneratorSql(ri.config, ri.databasePrefix, appendTables, modifyMain);
            generator.run(ri.pathToFileList, mode);
        }
    }
}
