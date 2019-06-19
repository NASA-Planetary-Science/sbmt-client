package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool.Mission;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.image.model.ImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.PerspectiveImage;

import nom.tam.fits.FitsException;

public class DatabaseGeneratorSql
{
    private SqlManager db = null;
    private SmallBodyModel smallBodyModel;
    private SmallBodyViewConfig smallBodyConfig;
    private String databasePrefix;
    private String databaseSuffix = "";
    private boolean appendTables;
    private boolean modifyMain;
    private int cameraIndex;


    public DatabaseGeneratorSql(SmallBodyViewConfig smallBodyConfig, String databasePrefix, boolean appendTables, boolean modifyMain, int cameraIndex)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.databasePrefix = databasePrefix;
        this.appendTables = appendTables;
        this.modifyMain = modifyMain;
        this.cameraIndex = cameraIndex;
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
            ImageKey key = new ImageKey(keyName, imageSource, config.imagingInstruments[cameraIndex]);
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
                System.out.println("skipping image " + filename + " since no frustum intersection with body");
                image.Delete();
                System.gc();
                System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
                System.out.println(" ");
                System.out.println(" ");
                continue;
            }
            else if (image.getUnshiftedFootprint().GetNumberOfCells() == 0)
            {
                System.out.println("skipping image " + filename + " since no intersecting cells");
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

            System.out.println("statement: " + insertStatement.toString());

            insertStatement.executeUpdate();


            // Now populate cubes table
            vtkPolyData footprintPolyData = image.getUnshiftedFootprint();
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
        if (source.equals(ImageSource.GASKELL) || source.equals(ImageSource.GASKELL_UPDATED))
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

    public void run(String fileList, ImageSource source, String diffFileList) throws IOException
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
        if (SbmtMultiMissionTool.getMission() == Mission.HAYABUSA2_STAGE)
            dburl = "hyb2sbmt.jhuapl.edu";
        else if (SbmtMultiMissionTool.getMission() == Mission.HAYABUSA2_DEPLOY)
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

        String imagesTable = getImagesTableNames(source);
        String cubesTable = getCubesTableNames(source);

        createTables(imagesTable);
        createTablesCubes(cubesTable);

        try
        {
            populateTables(lines, imagesTable, cubesTable, source);
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
        EROS(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL),
                "/project/nearsdc/data/GASKELL/EROS/MSI/msiImageList.txt", "eros"),
        ITOKAWA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.ITOKAWA, ShapeModelType.GASKELL),
                "/project/nearsdc/data/GASKELL/ITOKAWA/AMICA/imagelist.txt", "amica"),
        VESTA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.VESTA, ShapeModelType.GASKELL),
                "/project/nearsdc/data/GASKELL/VESTA/FC/uniqFcFiles.txt", "fc"),
        CERES(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.CERES, ShapeModelType.GASKELL),
                "/project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt", "ceres"),
        DEIMOSEXPERIMENTAL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.DEIMOS, ShapeModelType.THOMAS),
                "/project/nearsdc/data/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/imagelist-fullpath.txt", "deimos"),
        PHOBOS(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelType.GASKELL),
                "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt"),
        PHOBOSEXPERIMENTAL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelType.EXPERIMENTAL),
                "/project/nearsdc/data/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/imagelist.txt", "phobosexp"),
        _67P(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "SHAP5 V0.3"),
                "/project/nearsdc/data/GASKELL/67P/IMAGING/imagelist-fullpath.txt", "67p"),
        _67P_DLR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelType.DLR, "SHAP4S"),
                "/project/nearsdc/data/DLR/67P/IMAGING/imagelist-fullpath.txt", "67p_dlr"),
        _67P_V2(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "V2"),
                "/project/nearsdc/data/GASKELL/67P_V2/IMAGING/imagelist-fullpath.txt", "67p_v2"),
        _67P_V3(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "V3"),
                "/project/nearsdc/data/GASKELL/67P_V3/IMAGING/imagelist-fullpath.txt", "67p_v3"),
//        JUPITER(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.JUPITER, null),
//                "/project/nearsdc/data/NEWHORIZONS/JUPITER/IMAGING/imagelist-fullpath.txt"),
//        CALLISTO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.CALLISTO, null),
//                "/project/nearsdc/data/NEWHORIZONS/CALLISTO/IMAGING/imagelist-fullpath.txt"),
//        EUROPA(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EUROPA, null),
//                "/project/nearsdc/data/NEWHORIZONS/EUROPA/IMAGING/imagelist-fullpath.txt"),
//        GANYMEDE(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.GANYMEDE, null),
//                "/project/nearsdc/data/NEWHORIZONS/GANYMEDE/IMAGING/imagelist-fullpath.txt"),
//        IO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.IO, null),
//                "/project/nearsdc/data/NEWHORIZONS/IO/IMAGING/imagelist-fullpath.txt"),
        RQ36_MAP(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V3"),
                "nearsdc/data/GASKELL/RQ36_V3/MAPCAM/imagelist-fullpath.txt", "RQ36_MAP"),
        RQ36_POLY(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V3"),
                "/project/nearsdc/data/GASKELL/RQ36_V3/POLYCAM/imagelist-fullpath.txt", "RQ36_POLY"),
        RQ36V4_MAP(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V4"),
                "/project/sbmt2/sbmt/nearsdc/data/bennu/bennu-simulated-v4/mapcam/imagelist-fullpath.txt", "RQ36V4_MAP"),
        RQ36V4_POLY(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V4"),
                "/project/sbmt2/sbmt/nearsdc/data/bennu/bennu-simulated-v4/polycam/imagelist-fullpath.txt", "RQ36V4_POLY"),
        PLUTO(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PLUTO, null),
                "/project/nearsdc/data/NEWHORIZONS/PLUTO/IMAGING/imagelist-fullpath.txt"),

        //
        // Ryugu
        //
		RYUGU_TIR_SIMULATED_SPICE(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.GASKELL),
                "/project/sbmt2/data/ryugu/truth/simulated/tir/imagelist-fullpath.txt", "ryugu"),

        // Ryugu Simulated SPC Model (on staging and deployed servers)
        RYUGU_SIM_SPC(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.GASKELL),
                "/var/www/sbmt/sbmt/data/ryugu/gaskell/onc", "ryugu_sim",
                "data/ryugu/gaskell/onc"),
        // Ryugu Simulated Truth Model (on staging and deployed servers)
        RYUGU_SIM_TRUTH(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.TRUTH),
                "/var/www/sbmt/sbmt/data/ryugu/truth/onc/", "ryugu_sim",
                "data/ryugu/truth/onc"),

        // Ryugu Simulated SPC Model SUMFILE Images (on APL server)
        RYUGU_SIM_SPC_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.GASKELL),
                "/project/sbmt2/sbmt/data/bodies/ryugu/gaskell/onc", "ryugu_sim",
                "data/ryugu/gaskell/onc"),
        // Ryugu Simulated SPC Model INFOFILE Images (on APL server)
        RYUGU_SIM_TRUTH_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.TRUTH),
                "/project/sbmt2/sbmt/data/bodies/ryugu/truth/onc", "ryugu_sim",
                "data/ryugu/truth/onc"),

        // Ryugu Shared Flight INFOFILE Images (on APL server)
//        RYUGU_SHARED_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/shared/onc", "ryugu_shared",
//                "ryugu/shared/onc"),

        /********************************
         * Hayabusa 2 - ONC images at Aizu
         ********************************/

        // Ryugu Model-specific Flight Images (on Aizu server) - ONC
        JAXA_SFM_V20180627(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180627),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180627/onc", "ryugu_jaxasfmv20180627",
                "ryugu/jaxa-sfm-v20180627/onc"),

        JAXA_SPC_V20180705(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180705),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180705/onc", "ryugu_jaxaspcv20180705",
                "ryugu/jaxa-spc-v20180705/onc"),

        JAXA_SFM_V20180714(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180714),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180714/onc", "ryugu_jaxasfmv20180714",
                "ryugu/jaxa-sfm-v20180714/onc"),

        JAXA_SPC_V20180717(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180717),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180717/onc", "ryugu_jaxaspcv20180717",
                "ryugu/jaxa-spc-v20180717/onc"),

        JAXA_SPC_V20180719_2(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180719_2),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_jaxaspcv201807192",
                "ryugu/jaxa-spc-v20180719-2/onc"),

        JAXA_SFM_V20180725_2(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180725_2),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_jaxasfmv201807252",
                "ryugu/jaxa-sfm-v20180725-2/onc"),

        JAXA_SPC_V20180731(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180731),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180731/onc", "ryugu_jaxaspcv20180731",
                "ryugu/jaxa-spc-v20180731/onc"),

        JAXA_SPC_V20180810(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180810),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180810/onc", "ryugu_jaxaspcv20180810",
                "ryugu/jaxa-spc-v20180810/onc"),

        JAXA_SPC_V20180816(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180816),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180816/onc", "ryugu_jaxaspcv20180816",
                "ryugu/jaxa-spc-v20180816/onc"),

        JAXA_SPC_V20180829(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180829/onc", "ryugu_jaxaspcv20180829",
                "ryugu/jaxa-spc-v20180829/onc"),

        JAXA_SPC_V20181014(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20181014),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20181014/onc", "ryugu_jaxaspcv20181014",
                "ryugu/jaxa-spc-v20181014/onc"),

       JAXA_SFM_V20180804(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180804),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180804/onc", "ryugu_jaxasfmv20180804",
                "ryugu/jaxa-sfm-v20180804/onc"),


        RYUGU_NASA_001(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_001),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-001/onc", "ryugu_nasa001",
                "ryugu/nasa-001/onc"),
        RYUGU_NASA_002(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-002/onc", "ryugu_nasa002",
                "ryugu/nasa-002/onc"),
        RYUGU_NASA_003(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_003),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-003/onc", "ryugu_nasa003",
                "ryugu/nasa-002/onc"),
        RYUGU_NASA_004(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_004),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-004/onc", "ryugu_nasa004",
                "ryugu/nasa-004/onc"),
        RYUGU_NASA_005(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-005/onc", "ryugu_nasa005",
                "ryugu/nasa-005/onc"),
        RYUGU_NASA_006(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_006),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-006/onc", "ryugu_nasa006",
                "ryugu/nasa-006/onc"),

        RYUGU_SHARED(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180829/onc", "ryugu_jaxaspcv20180829",
                "ryugu/jaxa-spc-v20180829/onc"),

        /********************************
         * Hayabusa 2 - ONC images at APL
         ********************************/


        // Ryugu Model-specific Flight Images (on APL server)
        JAXA_SFM_V20180627_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180627),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/onc", "ryugu_jaxasfmv20180627",
                "ryugu/jaxa-sfm-v20180627/onc"),

        JAXA_SPC_V20180705_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180705),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/onc", "ryugu_jaxaspcv20180705",
                "ryugu/jaxa-spc-v20180705/onc"),

        JAXA_SFM_V20180714_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180714),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/onc", "ryugu_jaxasfmv20180714",
                "ryugu/jaxa-sfm-v20180714/onc"),

        JAXA_SPC_V20180717_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180717),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/onc", "ryugu_jaxaspcv20180717",
                "ryugu/jaxa-spc-v20180717/onc"),

        JAXA_SPC_V20180719_2_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180719_2),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_jaxaspcv201807192",
                "ryugu/jaxa-spc-v20180719-2/onc"),

        JAXA_SFM_V20180725_2_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180725_2),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_jaxasfmv201807252",
                "ryugu/jaxa-sfm-v20180725-2/onc"),

        JAXA_SPC_V20180731_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180731),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/onc", "ryugu_jaxaspcv20180731",
                "ryugu/jaxa-spc-v20180731/onc"),

        JAXA_SPC_V20180810_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180810),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/onc", "ryugu_jaxaspcv20180810",
                "ryugu/jaxa-spc-v20180810/onc"),

        JAXA_SPC_V20180816_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180816),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/onc", "ryugu_jaxaspcv20180816",
                "ryugu/jaxa-spc-v20180816/onc"),

        JAXA_SPC_V20180829_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/onc", "ryugu_jaxaspcv20180829",
                "ryugu/jaxa-spc-v20180829/onc"),

        JAXA_SFM_V20180804_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180804),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/onc", "ryugu_jaxasfmv20180804",
                "ryugu/jaxa-sfm-v20180804/onc"),

        JAXA_SPC_V20181014_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20181014),
                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20181014/onc", "ryugu_jaxaspcv20181014",
                "ryugu/jaxa-spc-v20181014/onc"),



        RYUGU_NASA_001_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_001),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-001/onc", "ryugu_nasa001",
                "ryugu/nasa-001/onc"),
        RYUGU_NASA_002_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/onc", "ryugu_nasa002",
                "ryugu/nasa-002/onc"),
        RYUGU_NASA_003_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_003),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/onc", "ryugu_nasa003",
                "ryugu/nasa-003/onc"),
        RYUGU_NASA_004_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_004),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/onc", "ryugu_nasa004",
                "ryugu/nasa-004/onc"),
        RYUGU_NASA_005_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/onc", "ryugu_nasa005",
                "ryugu/nasa-005/onc"),
        RYUGU_NASA_006_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_006),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-006/onc", "ryugu_nasa006",
                "ryugu/nasa-006/onc"),

        RYUGU_SHARED_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/onc", "ryugu_nasa005",
                "ryugu/nasa-005/onc"),


        /********************************
         * Hayabusa 2 - TIR images at Aizu
         ********************************/

//        // Ryugu Model-specific Flight Images (on Aizu server) - TIR
//        JAXA_SFM_V20180627_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180627),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180627/tir", "ryugu_jaxasfmv20180627_tir",
//                "ryugu/jaxa-sfm-v20180627/tir"),
//
//        JAXA_SPC_V20180705_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180705),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180705/tir", "ryugu_jaxaspcv20180705_tir",
//                "ryugu/jaxa-spc-v20180705/tir"),
//
//        JAXA_SFM_V20180714_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180714),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180714/tir", "ryugu_jaxasfmv20180714_tir",
//                "ryugu/jaxa-sfm-v20180714/tir"),
//
//        JAXA_SPC_V20180717_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180717),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180717/tir", "ryugu_jaxaspcv20180717_tir",
//                "ryugu/jaxa-spc-v20180717/tir"),
//
//        JAXA_SPC_V20180719_2_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180719_2),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180719-2/tir", "ryugu_jaxaspcv201807192_tir",
//                "ryugu/jaxa-spc-v20180719-2/tir"),
//
//        JAXA_SFM_V20180725_2_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180725_2),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180725-2/tir", "ryugu_jaxasfmv201807252_tir",
//                "ryugu/jaxa-sfm-v20180725-2/tir"),
//
//        JAXA_SPC_V20180731_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180731),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180731/tir", "ryugu_jaxaspcv20180731_tir",
//                "ryugu/jaxa-spc-v20180731/tir"),
//
//        JAXA_SPC_V20180810_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180810),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180810/tir", "ryugu_jaxaspcv20180810_tir",
//                "ryugu/jaxa-spc-v20180810/tir"),
//
//        JAXA_SPC_V20180816_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180816),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180816/tir", "ryugu_jaxaspcv20180816_tir",
//                "ryugu/jaxa-spc-v20180816/tir"),
//
//        JAXA_SPC_V20180829_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180829/tir", "ryugu_jaxaspcv20180829_tir",
//                "ryugu/jaxa-spc-v20180829/tir"),
//
//        JAXA_SFM_V20180804_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180804),
//                "/var/www/sbmt/sbmt/data/ryugu/jaxa-sfm-v20180804/tir", "ryugu_jaxasfmv20180804_tir",
//                "ryugu/jaxa-sfm-v20180804/tir"),
//
//
//        RYUGU_NASA_001_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_001),
//                "/var/www/sbmt/sbmt/data/ryugu/nasa-001/tir", "ryugu_nasa001_tir",
//                "ryugu/nasa-001/tir"),
//        RYUGU_NASA_002_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002),
//                "/var/www/sbmt/sbmt/data/ryugu/nasa-002/tir", "ryugu_nasa002_tir",
//                "ryugu/nasa-002/tir"),
//        RYUGU_NASA_003_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_003),
//                "/var/www/sbmt/sbmt/data/ryugu/nasa-003/tir", "ryugu_nasa003_tir",
//                "ryugu/nasa-002/tir"),
//        RYUGU_NASA_004_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_004),
//                "/var/www/sbmt/sbmt/data/ryugu/nasa-004/tir", "ryugu_nasa004_tir",
//                "ryugu/nasa-004/tir"),
        RYUGU_NASA_005_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
                "/var/www/sbmt/sbmt/data/ryugu/nasa-005/tir", "ryugu_nasa005_tir",
                "ryugu/nasa-005/tir"),

        RYUGU_SHARED_TIR(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
                "/var/www/sbmt/sbmt/data/ryugu/jaxa-spc-v20180829/tir", "ryugu_jaxaspcv20180829_tir",
                "ryugu/jaxa-spc-v20180829/tir"),


        /********************************
         * Hayabusa 2 - TIR images at APL
         ********************************/
//        // Ryugu Simulated SPC Model SUMFILE Images (on APL server)
//        RYUGU_SIM_SPC_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.GASKELL),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/gaskell/tir", "ryugu_sim_tir",
//                "ryugu/gaskell/tir"),
//        // Ryugu Simulated SPC Model INFOFILE Images (on APL server)
//        RYUGU_SIM_TRUTH_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.TRUTH),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/truth/tir", "ryugu_sim_tir",
//                "ryugu/truth/tir"),
//
//
//     // Ryugu Model-specific Flight Images (on APL server)
//        JAXA_SFM_V20180627_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180627),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/tir", "ryugu_jaxasfmv20180627_tir",
//                "ryugu/jaxa-sfm-v20180627/tir"),
//
//        JAXA_SPC_V20180705_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180705),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/tir", "ryugu_jaxaspcv20180705_tir",
//                "ryugu/jaxa-spc-v20180705/tir"),
//
//        JAXA_SFM_V20180714_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180714),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/tir", "ryugu_jaxasfmv20180714_tir",
//                "ryugu/jaxa-sfm-v20180714/tir"),
//
//        JAXA_SPC_V20180717_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180717),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/tir", "ryugu_jaxaspcv20180717_tir",
//                "ryugu/jaxa-spc-v20180717/tir"),
//
//        JAXA_SPC_V20180719_2_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180719_2),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/tir", "ryugu_jaxaspcv201807192_tir",
//                "ryugu/jaxa-spc-v20180719-2/tir"),
//
//        JAXA_SFM_V20180725_2_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180725_2),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/tir", "ryugu_jaxasfmv201807252_tir",
//                "ryugu/jaxa-sfm-v20180725-2/tir"),
//
//        JAXA_SPC_V20180731_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180731),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/tir", "ryugu_jaxaspcv20180731_tir",
//                "ryugu/jaxa-spc-v20180731/tir"),
//
//        JAXA_SPC_V20180810_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180810),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/tir", "ryugu_jaxaspcv20180810_tir",
//                "ryugu/jaxa-spc-v20180810/tir"),
//
//        JAXA_SPC_V20180816_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180816),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/tir", "ryugu_jaxaspcv20180816_tir",
//                "ryugu/jaxa-spc-v20180816/tir"),
//
//        JAXA_SPC_V20180829_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/tir", "ryugu_jaxaspcv20180829_tir",
//                "ryugu/jaxa-spc-v20180829/tir"),
//
//        JAXA_SFM_V20180804_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180804),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/tir", "ryugu_jaxasfmv20180804_tir",
//                "ryugu/jaxa-sfm-v20180804/tir"),
//
//
//        RYUGU_NASA_001_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_001),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-001/tir", "ryugu_nasa001_tir",
//                "ryugu/nasa-001/tir"),
//        RYUGU_NASA_002_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/tir", "ryugu_nasa002_tir",
//                "ryugu/nasa-002/tir"),
//        RYUGU_NASA_003_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_003),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/tir", "ryugu_nasa003_tir",
//                "ryugu/nasa-002/tir"),
//        RYUGU_NASA_004_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_004),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/tir", "ryugu_nasa004_tir",
//                "ryugu/nasa-004/tir"),
//        RYUGU_NASA_005_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
//                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/tir", "ryugu_nasa005_tir",
//                "ryugu/nasa-005/tir"),

        RYUGU_SHARED_TIR_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005),
                "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/tir/imagelist-info.txt", "ryugu_nasa005_tir",
                "ryugu/nasa-005/tir"),



        ATLAS(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.ATLAS, ShapeModelType.GASKELL),
                "/project/sbmt2/data/atlas/gaskell/imaging/imagelist-fullpath.txt", "atlas"),
        PHOBOS_ERNST_2018(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.PHOBOS, ShapeModelType.EXPERIMENTAL),
                "/project/sbmt2/sbmt/data/bodies/phobos/ernst2018/imaging/imagelist-fullpath.txt", "phobos_ernst_2018"),
        DEIMOS_ERNST_2018(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.DEIMOS, ShapeModelType.EXPERIMENTAL),
                "/project/sbmt2/sbmt/data/bodies/deimos/ernst2018/imaging/imagelist-fullpath.txt", "deimos_ernst_2018"),


        /*
         * Osiris REx flight models below here.
         */
        // 1109B SUMFILES
        BENNU_ALTWG_SPC_V20181109B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181109b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181109b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181109b_mapcam"),
        BENNU_ALTWG_SPC_V20181109B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181109b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181109b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181109b_polycam"),

        // 1109B INFOFILES
        BENNU_ALTWG_SPICE_V20181109B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181109b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181109b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181109b_mapcam"),
        BENNU_ALTWG_SPICE_V20181109B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181109b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181109b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181109b_polycam"),

        // 1115 INFOFILES
        BENNU_ALTWG_SPICE_V20181115_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181115),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181115/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181115_mapcam"),
        BENNU_ALTWG_SPICE_V20181115_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181115),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181115/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181115_polycam"),

        // 1116 SUMFILES
        BENNU_ALTWG_SPC_V20181116_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181116),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181116/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181116_mapcam"),
        BENNU_ALTWG_SPC_V20181116_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181116),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181116/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181116_polycam"),

        // 1116 INFOFILES
        BENNU_ALTWG_SPICE_V20181116_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181116),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181116/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181116_mapcam"),
        BENNU_ALTWG_SPICE_V20181116_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181116),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181116/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181116_polycam"),

        // 1123B SUMFILES
        BENNU_ALTWG_SPC_V20181123B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181123b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181123b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181123b_mapcam"),
        BENNU_ALTWG_SPC_V20181123B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181123b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181123b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181123b_polycam"),

        // 1123B INFOFILES
        BENNU_ALTWG_SPICE_V20181123B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181123b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181123b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181123b_mapcam"),
        BENNU_ALTWG_SPICE_V20181123B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181123b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181123b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181123b_polycam"),

        // 1202 SUMFILES
        BENNU_ALTWG_SPC_V20181202_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181202),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181202/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181202_mapcam"),
        BENNU_ALTWG_SPC_V20181202_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181202),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181202/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181202_polycam"),

        // 1202 INFOFILES
        BENNU_ALTWG_SPICE_V20181202_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181202),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181202/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181202_mapcam"),
        BENNU_ALTWG_SPICE_V20181202_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181202),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181202/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181202_polycam"),

        // 1206 SUMFILES
        BENNU_ALTWG_SPC_V20181206_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181206),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181206/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181206_mapcam"),
        BENNU_ALTWG_SPC_V20181206_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181206),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181206/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181206_polycam"),

        // 1206 INFOFILES
        BENNU_ALTWG_SPICE_V20181206_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181206),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181206/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181206_mapcam"),
        BENNU_ALTWG_SPICE_V20181206_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181206),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181206/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181206_polycam"),

        // 1217 SUMFILES
        BENNU_ALTWG_SPC_V20181217_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181217),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181217/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181217_mapcam"),
        BENNU_ALTWG_SPC_V20181217_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181217),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181217/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181217_polycam"),

        // 1217 INFOFILES
        BENNU_ALTWG_SPICE_V20181217_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181217),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181217/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181217_mapcam"),
        BENNU_ALTWG_SPICE_V20181217_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181217),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181217/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181217_polycam"),
        BENNU_ALTWG_SPICE_V20181217_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181217),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181217/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181217_navcam"),

        // 1227 SUMFILES
        BENNU_ALTWG_SPC_V20181227_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181227),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181227/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181227_mapcam"),
        BENNU_ALTWG_SPC_V20181227_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181227),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181227/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20181227_polycam"),

        // 1227 INFOFILES
        BENNU_ALTWG_SPICE_V20181227_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181227),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181227/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181227_mapcam"),
        BENNU_ALTWG_SPICE_V20181227_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181227),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181227/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181227_polycam"),
        BENNU_ALTWG_SPICE_V20181227_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20181227),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20181227/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20181227_navcam"),

        // 20190105 SUMFILES
        BENNU_ALTWG_SPC_V20190105_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190105),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_mapcam"),
        BENNU_ALTWG_SPC_V20190105_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190105),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_polycam"),

        // 20190105 INFOFILES
        BENNU_ALTWG_SPICE_V20190105_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190105),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_mapcam"),
        BENNU_ALTWG_SPICE_V20190105_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190105),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_polycam"),
        BENNU_ALTWG_SPICE_V20190105_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190105),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190105/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_navcam"),

        // 20190114 SUMFILES
        BENNU_ALTWG_SPC_V20190114_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190114),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_mapcam"),
        BENNU_ALTWG_SPC_V20190114_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190114),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_polycam"),

        // 20190114 INFOFILES
        BENNU_ALTWG_SPICE_V20190114_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190114),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_mapcam"),
        BENNU_ALTWG_SPICE_V20190114_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190114),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_polycam"),
        BENNU_ALTWG_SPICE_V20190114_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190114),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190114/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_navcam"),

        // 20190117 SUMFILES
        BENNU_ALTWG_SPC_V20190117_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190117),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_mapcam"),
        BENNU_ALTWG_SPC_V20190117_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190117),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_polycam"),

        // 20190117 INFOFILES
        BENNU_ALTWG_SPICE_V20190117_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190117),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_mapcam"),
        BENNU_ALTWG_SPICE_V20190117_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190117),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_polycam"),
        BENNU_ALTWG_SPICE_V20190117_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190117),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190117/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_navcam"),

        // 20190121 SUMFILES
        BENNU_ALTWG_SPC_V20190121_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190121),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_mapcam"),
        BENNU_ALTWG_SPC_V20190121_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190121),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_polycam"),

        // 20190121 INFOFILES
        BENNU_ALTWG_SPICE_V20190121_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190121),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_mapcam"),
        BENNU_ALTWG_SPICE_V20190121_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190121),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_polycam"),
        BENNU_ALTWG_SPICE_V20190121_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190121),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190121/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_navcam"),

        // 0207A SUMFILES
        BENNU_ALTWG_SPC_V20190207A_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207a),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_mapcam"),
        BENNU_ALTWG_SPC_V20190207A_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207a),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_polycam"),

        // 0207A INFOFILES
        BENNU_ALTWG_SPICE_V20190207A_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207a),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_mapcam"),
        BENNU_ALTWG_SPICE_V20190207A_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207a),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_polycam"),
        BENNU_ALTWG_SPICE_V20190207A_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207a),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207a/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_navcam"),

        // 0207B SUMFILES
        BENNU_ALTWG_SPC_V20190207B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_mapcam"),
        BENNU_ALTWG_SPC_V20190207B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_polycam"),

        // 0207B INFOFILES
        BENNU_ALTWG_SPICE_V20190207B_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_mapcam"),
        BENNU_ALTWG_SPICE_V20190207B_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_polycam"),
        BENNU_ALTWG_SPICE_V20190207B_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190207b),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190207b/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_navcam"),

        // 0414 SUMFILES
        BENNU_ALTWG_SPC_V20190414_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_mapcam"),
        BENNU_ALTWG_SPC_V20190414_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_polycam"),

        // 0414 INFOFILES
        BENNU_ALTWG_SPICE_V20190414_MAPCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_mapcam"),
        BENNU_ALTWG_SPICE_V20190414_POLYCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_polycam"),
        BENNU_ALTWG_SPICE_V20190414_NAVCAM_APL(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.RQ36, ShapeModelType.ALTWG_SPC_v20190414),
                "/project/sbmt2/sbmt/data/bodies/bennu/altwg-spc-v20190414/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_navcam"),

        ;

        public final SmallBodyViewConfig config;
        public final String pathToFileList;
        public final String databasePrefix;
        public final String remotePathToFileList;

        private RunInfo(SmallBodyViewConfig config, String pathToFileList)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = config.body.toString().toLowerCase();
            this.remotePathToFileList = null;
        }

        private RunInfo(SmallBodyViewConfig config, String pathToFileList, String databasePrefix)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = databasePrefix;
            this.remotePathToFileList = null;
        }

        private RunInfo(SmallBodyViewConfig config, String pathToFileList, String databasePrefix, String remotePathToFileList)
        {
            this.config = config;
            this.pathToFileList = pathToFileList;
            this.databasePrefix = databasePrefix;
            this.remotePathToFileList = remotePathToFileList;
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
     */
    public static void main(String[] args) throws IOException
    {
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        // default configuration parameters
//        String appName = "neartool";
//        String cacheVersion = "2";
        boolean aplVersion = true;
        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/sbmt");

        boolean appendTables = false;
        boolean modifyMain = false;
        boolean remote = false;
        String diffFileList = null;

        int cameraIndex = 0;

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
            }
            else if (args[i].equals("--remote"))
            {
                remote = true;
            }
            else if (args[i].equals("--cameraIndex"))
            {
                cameraIndex = Integer.parseInt(args[++i]);
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
        int numberRequiredArgs = 2;
        if (args.length - i < numberRequiredArgs)
            usage();

        // basic default configuration, most of these will be overwritten by the configureMission() method
//        Configuration.setAppName(appName);
//        Configuration.setCacheVersion(cacheVersion);
        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        SbmtMultiMissionTool.configureMission();

        // authentication
        Authenticator.authenticate();

        // initialize view config
        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibrariesHeadless();

        ImageSource mode = ImageSource.valueOf(args[i++].toUpperCase());
        String body = args[i++];

        RunInfo[] runInfos = null;
        if (body.toUpperCase().equals("ALL"))
            runInfos = RunInfo.values();
        else
            runInfos = new RunInfo[]{RunInfo.valueOf(body.toUpperCase())};

        Mission mission = SbmtMultiMissionTool.getMission();
        System.out.println("Mission: " + mission);

        for (RunInfo ri : runInfos)
        {
            DatabaseGeneratorSql generator = new DatabaseGeneratorSql(ri.config, ri.databasePrefix, appendTables, modifyMain, cameraIndex);

            String pathToFileList = ri.pathToFileList;
            if (remote)
            {
                if (ri.remotePathToFileList != null)
                    pathToFileList = ri.remotePathToFileList;
            }

            System.out.println("Generating: " + pathToFileList + ", mode=" + mode);
            generator.run(pathToFileList, mode, diffFileList);
        }
    }
}
