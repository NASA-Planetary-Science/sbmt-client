package edu.jhuapl.sbmt.tools2;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkObject;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.common.client.Mission;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.Instrument;
//import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.image.PointingFileReader;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.ImageIllumination;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.ImagePixelScale;
import edu.jhuapl.sbmt.image2.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.io.FilenameToRenderableImageFootprintPipeline;
import edu.jhuapl.sbmt.image2.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToDerivedMetadataPipeline;
//import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;
//import edu.jhuapl.sbmt.image.model.keys.ImageKey;
import edu.jhuapl.sbmt.tools.DBRunInfo;
import edu.jhuapl.sbmt.tools.SqlManager;

public class DatabaseGeneratorSql
{
    private SqlManager db = null;
    private SmallBodyModel smallBodyModel;
    private SmallBodyViewConfig smallBodyConfig;
    private String databasePrefix;
    private String databaseSuffix = "";
    private boolean appendTables;
    private boolean modifyMain;
//    private int cameraIndex;
    private Instrument instrument;

    public DatabaseGeneratorSql(SmallBodyViewConfig smallBodyConfig, String databasePrefix, boolean appendTables, boolean modifyMain, Instrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.databasePrefix = databasePrefix;
        this.appendTables = appendTables;
        this.modifyMain = modifyMain;
        this.instrument = instrument;
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
            List<String> lines,
            String tableName,
            String cubesTableName,
            ImageSource imageSource) throws Exception
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

        int totalFiles = lines.size();
        for (String line : lines)
        {
            String filename = line.replaceFirst("\\s.*", "");
            // Increment image count (for status message purposes only)
            count++;

            // If appending and there is already an entry for the filename then skip
            if(appendTables){
                queryResult = db.query("SELECT * FROM `" + tableName + "` WHERE `filename` = \"" +
                    new File(filename).getName() + "\"");
                if(queryResult != null && !queryResult.isEmpty() &&
                    queryResult.get(0) != null && !queryResult.get(0).isEmpty() &&
                    queryResult.get(0).get(0) != null){

                    System.out.println("\n\nskipping image " + count + " of " + totalFiles + ": " + filename + ", already in table");
                    continue;
                }
            }

            // If we got to this point, the image is not already in the table so we need to create a new entry
            System.out.println("\n\nstarting image " + count + " of " + totalFiles + ": " + filename);

            String keyName = filename;
            keyName = keyName.replace(".FITS", "");
            keyName = keyName.replace(".fits", "");
            keyName = keyName.replace(".FIT", "");
            keyName = keyName.replace(".fit", "");

            ImagingInstrument imager = null;
            for (ImagingInstrument inst : config.imagingInstruments)
            {
            	if (inst.getInstrumentName() == instrument) imager = inst;
            }
            if (imager == null)
            {
            	throw new Exception("Instrument " + instrument + " not recognized as a valid imager for this configuration");
            }
//            ImageKeyInterface key = new ImageKey(keyName, imageSource, imager);
//
////            ImageKeyInterface key = new ImageKey(keyName, imageSource, config.imagingInstruments[cameraIndex]);
//            PerspectiveImage image = null;

            FilenameToRenderableImageFootprintPipeline pipeline = FilenameToRenderableImageFootprintPipeline.of(keyName, ImageSource.SPICE, List.of(smallBodyModel), imager);
        	List<RenderablePointedImage> images = pipeline.getImages();
        	List<String> pointingFilenames = pipeline.getPointingFilenames();

            try
            {
//                image = (PerspectiveImage)SbmtModelFactory.createImage(key, smallBodyModel, false);
            	boolean filesExist = checkIfAllFilesExist(keyName, pointingFilenames.get(0));
                if (filesExist == false)
                {
                    System.out.println("file not found, skipping image " + filename);
//                    image.Delete();
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

            RenderablePointedImage image = images.get(0);
            PointingFileReader pointing = image.getPointing();
            vtkPolyData footprint = pipeline.getFootprints().get(0);
//            image.loadFootprint();
            if (footprint == null)
            {
                // In this case if image.loadFootprint() finds no frustum intersection
                System.out.println("skipping image " + filename + " since no frustum intersection with body");
                footprint.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }
            else if (footprint.GetNumberOfCells() == 0)
            {
                System.out.println("skipping image " + filename + " since no intersecting cells");
                footprint.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }

            // Calling this forces the calculation of incidence, emission, phase, and pixel scale
//            image.getProperties();

            PerspectiveImageToDerivedMetadataPipeline metadataPipeline =
            		new PerspectiveImageToDerivedMetadataPipeline(image, List.of(smallBodyModel));

    		ImageIllumination imageIllumination = metadataPipeline.getIllumAtts().get(0);
    		ImagePixelScale pixelScale = metadataPipeline.getPixelAtts().get(0);
    		HashMap<String, String> metadata = metadataPipeline.getMetadata();

            DateTime startTime = new DateTime(image.getPointing().getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getPointing().getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());

            //TODO FIX THIS
            boolean containsLimb = false; // was image.containsLimb, which uses the setting on the backplanes, which isn't idea to calculate that first

            System.out.println("id: " + primaryKey);
            System.out.println("filename: " + new File(filename).getName());
            System.out.println("starttime: " + startTime);
            System.out.println("stoptime: " + stopTime);
            System.out.println("filter: " + metadata.get("Filter"));
            System.out.println("camera: " + metadata.get("Camera"));
            System.out.println("TARGET_CENTER_DISTANCE: " + new Vector3D(pointing.getSpacecraftPosition()).getNorm());
            System.out.println("Min HORIZONTAL_PIXEL_SCALE: " + pixelScale.getMinHorizontalPixelScale());
            System.out.println("Max HORIZONTAL_PIXEL_SCALE: " + pixelScale.getMaxHorizontalPixelScale());
            System.out.println("Min VERTICAL_PIXEL_SCALE: " + pixelScale.getMinVerticalPixelScale());
            System.out.println("Max VERTICAL_PIXEL_SCALE: " + pixelScale.getMaxVerticalPixelScale());
            System.out.println("hasLimb: " + containsLimb);
            System.out.println("minIncidence: " + imageIllumination.getMinIncidence());
            System.out.println("maxIncidence: " + imageIllumination.getMaxIncidence());
            System.out.println("minEmission: " + imageIllumination.getMinEmission());
            System.out.println("maxEmission: " + imageIllumination.getMaxEmission());
            System.out.println("minPhase: " + imageIllumination.getMinPhase());
            System.out.println("maxPhase: " + imageIllumination.getMaxPhase());

            insertStatement.setInt(1, primaryKey);
            insertStatement.setString(2, new File(filename).getName());
            insertStatement.setLong(3, startTime.getMillis());
            insertStatement.setLong(4, stopTime.getMillis());
            insertStatement.setByte(5, (byte)Integer.parseInt(metadata.get("Filter")));
            insertStatement.setByte(6, (byte)Integer.parseInt(metadata.get("Camera")));
            insertStatement.setDouble(7, + new Vector3D(pointing.getSpacecraftPosition()).getNorm());
            insertStatement.setDouble(8, pixelScale.getMinHorizontalPixelScale());
            insertStatement.setDouble(9, pixelScale.getMaxHorizontalPixelScale());
            insertStatement.setDouble(10, pixelScale.getMinVerticalPixelScale());
            insertStatement.setDouble(11, pixelScale.getMaxVerticalPixelScale());
            insertStatement.setBoolean(12, containsLimb);
            insertStatement.setDouble(13, imageIllumination.getMinIncidence());
            insertStatement.setDouble(14, imageIllumination.getMaxIncidence());
            insertStatement.setDouble(15, imageIllumination.getMinEmission());
            insertStatement.setDouble(16, imageIllumination.getMaxEmission());
            insertStatement.setDouble(17, imageIllumination.getMinPhase());
            insertStatement.setDouble(18, imageIllumination.getMaxPhase());

            System.out.println("statement: " + insertStatement.toString());

            insertStatement.executeUpdate();


            // Now populate cubes table
            vtkPolyData footprintPolyData = footprint; // image.getUnshiftedFootprint();
            TreeSet<Integer> cubeIds = smallBodyModel.getIntersectingCubes(footprintPolyData);
//            System.out.println("cubeIds:  " + cubeIds);
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

//            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    boolean checkIfAllFilesExist(String imageFile, String pointingFilename)
    {
        File fitfile = new File(imageFile);
        System.out.println("Fit file full path: " + fitfile.getAbsolutePath());
        if (!fitfile.exists())
            return false;
        File pointingFile = new File(pointingFilename);
        System.out.println(pointingFile);
        if (!pointingFile.exists())
            return false;
        return true;
    }


//    boolean checkIfAllFilesExist(PerspectiveImage image, ImageSource source)
//    {
//        File fitfile = new File(image.getFitFileFullPath());
//        System.out.println("Fit file full path: " + fitfile.getAbsolutePath());
//        if (!fitfile.exists())
//            return false;
//
//        // Check for the sumfile if source is Gaskell
//        if (source.equals(ImageSource.GASKELL) || source.equals(ImageSource.GASKELL_UPDATED))
//        {
//            File sumfile = new File(image.getSumfileFullPath());
//            System.out.println(sumfile);
//            if (!sumfile.exists())
//                return false;
//
//            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
//            // are 1296 bytes long or less. If it's very small in size though it is probably
//            // a sumfile that was generated via another method so keep it.
//            //if (sumfile.length() <= 1296 && sumfile.length() >= 500)
//            //    return false;
//        }
//        else
//        {
//            File infofile = new File(image.getInfoFileFullPath());
//            System.out.println("Infofile full path: " + infofile.getAbsolutePath());
//            if (!infofile.exists())
//                return false;
//        }
//
//        return true;
//    }

    String getImagesTableNames(ImageSource source)
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "images_" + source.getDatabaseTableName();
        }else{
            return databasePrefix.toLowerCase() + "images_" + source.getDatabaseTableName() + databaseSuffix;
        }
    }

    String getCubesTableNames(ImageSource source)
    {
        if(modifyMain){
            return databasePrefix.toLowerCase() + "cubes_" + source.getDatabaseTableName();
        }else{
            return databasePrefix.toLowerCase() + "cubes_" + source.getDatabaseTableName() + databaseSuffix;
        }
    }

    public void run(String fileList, ImageSource source, String diffFileList) throws Exception
    {
        smallBodyModel = SbmtModelFactory.createSmallBodyModel(smallBodyConfig);

        if (!fileList.endsWith(".txt"))
        {
            if (source == ImageSource.GASKELL)
                fileList = fileList + File.separator + "imagelist-fullpath-sum.txt";
            else if (source == ImageSource.SPICE)
                fileList = fileList + File.separator + "imagelist-fullpath-info.txt";
            else
                throw new IOException("Image Source is neither type GASKELL or type SPICE");
        }

        //Grab the list of the image filenames from the diffFileList, if it exists.
        List<String> diffFiles = new ArrayList<String>();
        if (diffFileList != null)
        {
        	diffFiles = FileUtil.getFileLinesAsStringList(diffFileList);
        	System.out.println("DatabaseGeneratorSql: run: diff files count " + diffFiles.size());
        }


        List<String> lines = null;
        try {
            // if the file path starts with "/" then we know we are accessing files from the local file system
            if (fileList.startsWith("/"))
                lines = FileUtil.getFileLinesAsStringList(fileList);
            // otherwise, we try to load the file from the server via HTTP
            else
                lines = FileCache.getFileLinesFromServerAsStringList(fileList);
        } catch (IOException e2) {
            e2.printStackTrace();
            return;
        }

        //Reduce the lines to only include those that match in the diff list, if it exists
        if (diffFileList != null)
        {
        	List<String> truncatedLines = new ArrayList<String>();
        	for (String line : lines)
        	{
        		for (String diffFile : diffFiles)
        		{
        			if (line.endsWith(diffFile))
        			{
        				truncatedLines.add(line);
        				break;
        			}
        		}
        	}
        	lines = truncatedLines;
        }

        String dburl = null;
        /*if (SbmtMultiMissionTool.getMission() == Mission.HAYABUSA2_STAGE)
            dburl = "hyb2sbmt.jhuapl.edu";
        else */if (SbmtMultiMissionTool.getMission() == Mission.HAYABUSA2_DEPLOY)
            dburl = "hyb2sbmt.u-aizu.ac.jp";
        else if (SbmtMultiMissionTool.getMission() == Mission.OSIRIS_REX_DEPLOY)
            throw new AssertionError("NEED TO SET UP THE LOCATION OF THE OREX DEPLOYED DATABASE");
        else
            dburl = "sd-mysql.jhuapl.edu";

        try
        {
            db = new SqlManager(dburl);
            System.out.println("Connected to database: " + dburl);
        }
        catch (Exception ex1) {
            ex1.printStackTrace();
            return;
        }

        try
        {
            String imagesTable = getImagesTableNames(source);
            String cubesTable = getCubesTableNames(source);

            createTables(imagesTable);
            createTablesCubes(cubesTable);

            populateTables(lines, imagesTable, cubesTable, source);
        }
        finally
        {
            try
            {
                db.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private static void usage()
    {
        String o = "This program generates tables in the MySQL database for a given body.\n\n"
                + "Usage: DatabaseGeneratorSql [options] <imagesource> <shapemodel>\n\n"
                + "Where:\n"
                + "  <imagesource>\n"
                + "          Must be one of the allowed sources for pointing. The tables for this\n"
                + "          pointing type will be generated. For example, if GASKELL is selected,\n"
                + "          then only the Gaskell tables are generated; if SPICE is selected, then\n"
                + "          only the SPICE (PDS) tables are generated. Allowed values are\n"
                + ImageSource.printSources(16)
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

    /**
     * @param args
     * @throws IOException
     *
     * To call, use arguments like this:
     *
     * --root-url $dbRootUrl --append-tables --body RQ36 --author ALTWG-SPC-v20190414 --instrument MAPCAM $pointingType
     *
     * The old body argument that came after pointing type is no longer needed - that was a lookup into the RunInfos, we which we now get from the configs and use the passed
     * in arguments to filter out the one we want
     *
     * The camera index has also been deprecated, and has been replaced by specifying the actual Instrument to use from that enum, which is then used to find the
     * imaging instrument to use
     *
     * The version argument is optional, since models may not have a version.  The SmallBodyViewConfig handles this automatically (it defaults to null)
     *
     */
    public static void main(String[] args)
    {
        try
        {
            doMain(args);
        } catch (Throwable t)
        {
            t.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    protected static void doMain(String[] args) throws Exception
    {
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        // default configuration parameters
        boolean aplVersion = true;
        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/sbmt");

        boolean appendTables = false;
        boolean modifyMain = false;
        boolean remote = false;
        String bodyName="";
        String authorName="";
        String versionString = null;
        String diffFileList = null;
        String instrumentString = null;

//        int cameraIndex = 0;

        // modify configuration parameters with command line args
        int i = 0;
        for (; i < args.length; ++i)
        {
            if (args[i].equals("--root-url"))
            {
                rootURL = safeUrlPaths.getUrl(args[++i]);
            }
            else if (args[i].equals("--append-tables"))
            {
                appendTables = true;
            }
            else if (args[i].equals("--modify-main"))
            {
                modifyMain = true;
            }
            else if (args[i].equals("--debug"))
            {
                Debug.setEnabled(true);
                FileCache.enableDebug(true);
            }
            else if (args[i].equals("--remote"))
            {
                remote = true;
            }
//            else if (args[i].equals("--cameraIndex"))
//            {
//                cameraIndex = Integer.parseInt(args[++i]);
//            }
            else if (args[i].equals("--body"))
            {
            	bodyName = args[++i];
            }
            else if (args[i].equals("--author"))
            {
            	authorName = args[++i];
            }
            else if (args[i].equals("--version"))
            {
            	versionString = args[++i];
            }
            else if (args[i].equals("--instrument"))
            {
            	instrumentString = args[++i].toUpperCase();
            }
            else if (args[i].equals("--diffList"))
            {
            	diffFileList = args[++i];
            }
            else {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 1;
        if (args.length - i < numberRequiredArgs)
            usage();

        // Important: set the mission before changing things in the Configuration. Otherwise,
        // setting the mission will undo those changes.
        SbmtMultiMissionTool.configureMission(rootURL);

        // basic default configuration, most of these will be overwritten by the configureMission() method
        Configuration.setAPLVersion(aplVersion);

        // authentication
        Configuration.authenticate();

        // initialize view config
        SmallBodyViewConfig.fromServer = true;

        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();

        ImageSource mode = ImageSource.valueOf(args[i++].toUpperCase());
//        String body = args[i++];

        SmallBodyViewConfig config = null;
        if (versionString != null)
        	config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName), ShapeModelType.provide(authorName), versionString);
        else
        	config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName), ShapeModelType.provide(authorName));
        DBRunInfo[] runInfos = config.databaseRunInfos;

        Mission mission = SbmtMultiMissionTool.getMission();
        System.out.println("Mission: " + mission);
//        System.out.println("DatabaseGeneratorSql: main: number of run infos " + runInfos.length);
        for (DBRunInfo ri : runInfos)
        {
//        	System.out.println("DatabaseGeneratorSql: main: body " + bodyName + " image source " + mode + " instrument " + instrumentString);
//        	System.out.println("DatabaseGeneratorSql: main: run info " + ri.toString());
//        	System.out.println("DatabaseGeneratorSql: main: " + !ri.name.equals(ShapeModelBody.valueOf(bodyName).toString()));
//        	System.out.println("DatabaseGeneratorSql: main: " + (ri.imageSource != mode));
//        	System.out.println("DatabaseGeneratorSql: main: " + (!ri.instrument.toString().equals(instrumentString)));
        	if (!ri.name.equals(ShapeModelBody.valueOf(bodyName).toString()) || (ri.imageSource != mode) || (!ri.instrument.toString().equals(instrumentString))) continue;
            System.out.println("DatabaseGeneratorSql: main: writing to " + ri.databasePrefix + " for " + ri.instrument + " with " + ri.imageSource);
        	DatabaseGeneratorSql generator = new DatabaseGeneratorSql(config, ri.databasePrefix, appendTables, modifyMain, ri.instrument);

            String fileListUrl = ri.pathToFileList;
            if (remote)
            {
                if (ri.remotePathToFileList != null)
                    fileListUrl = ri.remotePathToFileList;
            }
            else
            {
                fileListUrl = safeUrlPaths.getUrl(safeUrlPaths.getString(Configuration.getDataRootURL().toString(), fileListUrl));
            }

            System.out.println("Generating: " + fileListUrl + ", mode=" + mode);
            generator.run(fileListUrl, mode, diffFileList);
        }
    }
}