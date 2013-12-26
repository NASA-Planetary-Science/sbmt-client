package edu.jhuapl.near.server;

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
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;


abstract public class DatabaseGeneratorBaseSql
{
    private SqlManager db = null;
    private PreparedStatement insertStatement = null;
    private PreparedStatement insertStatement2 = null;
    private SmallBodyModel smallBodyModel;
    private vtkPolyData footprintPolyData;

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
                    "id bigint PRIMARY KEY, " +
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

    private void populateTables(
            ArrayList<String> imageFiles,
            String tableName,
            PerspectiveImage.ImageSource imageSource) throws IOException, SQLException, FitsException
    {
        smallBodyModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;

        for (String filename : imageFiles)
        {
            boolean filesExist = checkIfAllFilesExist(filename, imageSource);
            if (filesExist == false)
            {
                System.out.println("skipping image " + filename);
                continue;
            }

            System.out.println("starting image " + count++ + "  " + filename);

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, imageSource);
            PerspectiveImage image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false, rootFolder);

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

            if (insertStatement == null)
            {
                insertStatement = db.preparedStatement(
                    "insert into " + tableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime startTime = new DateTime(image.getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());

            System.out.println("id: " + getIdFromImageName(origFile.getName()));
            System.out.println("filename: " + origFile.getName());
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

            insertStatement.setLong(1, getIdFromImageName(origFile.getName()));
            insertStatement.setString(2, origFile.getName());
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


            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    private void populateTablesCubes(
            ArrayList<String> imageFiles,
            String tableName,
            PerspectiveImage.ImageSource imageSource) throws SQLException, IOException, FitsException
    {
        smallBodyModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;
        for (String filename : imageFiles)
        {
            boolean filesExist = checkIfAllFilesExist(filename, imageSource);
            if (filesExist == false)
            {
                System.out.println("skipping image " + filename);
                continue;
            }

            System.out.println("\n\nstarting image " + filename);

            File origFile = new File(filename);

            if (footprintPolyData == null)
                footprintPolyData = new vtkPolyData();

            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, imageSource);
            PerspectiveImage image = (PerspectiveImage)ModelFactory.createImage(key, smallBodyModel, false, rootFolder);

            image.loadFootprint();
            footprintPolyData.DeepCopy(image.getUnshiftedFootprint());

            if (insertStatement2 == null)
            {
                insertStatement2 = db.preparedStatement(
                        "insert into " + tableName + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = smallBodyModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                insertStatement2.setInt(1, count);
                insertStatement2.setLong(2, getIdFromImageName(origFile.getName()));
                insertStatement2.setInt(3, i);

                insertStatement2.executeUpdate();

                ++count;
            }

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
        }
    }

    boolean checkIfAllFilesExist(String line, PerspectiveImage.ImageSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File rootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String id = (new File(line)).getName();
            id = id.substring(0, id.length()-4);
            String name = rootdir.getAbsolutePath() + "/sumfiles/" + id + ".SUM";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1296 bytes long or less
            if (ignoreSumfilesWithNoLandmarks(id))
            {
                if (file.length() <= 1296)
                    return false;
            }
        }
        else
        {
            File rootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String id = (new File(line)).getName();
            id = id.substring(0, id.length()-4);
            String name = rootdir.getAbsolutePath() + "/infofiles/" + id + ".INFO";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;
        }

        return true;
    }

    /**
     * Subclasses may redefine this to filter out bad files from the list.
     * By default original file list is returned unchanged.
     * @param files
     * @return
     */
    private ArrayList<String> removeBadFiles(ArrayList<String> files)
    {
        return files;
    }

    /**
     * Subclasses may redefine this to include images even if they do not
     * have landmarks
     */
    protected boolean ignoreSumfilesWithNoLandmarks(String filename)
    {
        return true;
    }


    abstract String getImagesGaskellTableNames();
    abstract String getCubesGaskellTableNames();
    abstract String getImagesPdsTableNames();
    abstract String getCubesPdsTableNames();
    abstract SmallBodyModel createSmallBodyModel();
    abstract long getIdFromImageName(String filename);

    /**
     * @param args
     * @throws IOException
     */
    public void doMain(String[] args) throws IOException
    {
        System.setProperty("java.awt.headless", "true");

        Configuration.setAPLVersion(true);

        NativeLibraryLoader.loadVtkLibraries();

        smallBodyModel = createSmallBodyModel();

        String fileList=args[0];
        int mode = Integer.parseInt(args[1]);

        ArrayList<String> files = null;
        try {
            files = FileUtil.getFileLinesAsStringList(fileList);
            files = removeBadFiles(files);
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

        String ImagesGaskellTable = getImagesGaskellTableNames();
        String CubesGaskellTable = getCubesGaskellTableNames();
        String ImagesPdsTable = getImagesPdsTableNames();
        String CubesPdsTable = getCubesPdsTableNames();

        if (mode == 1 || mode == 0)
            createTables(ImagesGaskellTable);
        if (mode == 1 || mode == 0)
            createTablesCubes(CubesGaskellTable);
        if (mode == 2 || mode == 0)
            createTables(ImagesPdsTable);
        if (mode == 2 || mode == 0)
            createTablesCubes(CubesPdsTable);

        try
        {
            if (mode == 1 || mode == 0)
                populateTables(files, ImagesGaskellTable, Image.ImageSource.GASKELL);
            if (mode == 1 || mode == 0)
                populateTablesCubes(files, CubesGaskellTable, Image.ImageSource.GASKELL);
            if (mode == 2 || mode == 0)
                populateTables(files, ImagesPdsTable, Image.ImageSource.PDS);
            if (mode == 2 || mode == 0)
                populateTablesCubes(files, CubesPdsTable, Image.ImageSource.PDS);
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
