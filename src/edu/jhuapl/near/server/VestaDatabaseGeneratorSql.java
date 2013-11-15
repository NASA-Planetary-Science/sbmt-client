package edu.jhuapl.near.server;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeSet;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkGlobalJavaHash;
import vtk.vtkPolyData;

import edu.jhuapl.near.model.Image;
import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.PerspectiveImage;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.NativeLibraryLoader;

public class VestaDatabaseGeneratorSql
{
    static private final String FcImagesGaskellTable = "fcimages_gaskell_beta";
    static private final String FcCubesGaskellTable = "fccubes_gaskell_beta";
    static private final String FcImagesPdsTable = "fcimages_pds_beta";
    static private final String FcCubesPdsTable = "fccubes_pds_beta";

    static private SqlManager db = null;
    static private PreparedStatement fcInsert = null;
    static private PreparedStatement fcInsert2 = null;
    static private SmallBodyModel vestaModel;
    //static private vtkPolyDataReader footprintReader;
    static private vtkPolyData footprintPolyData;
    //static private double[] meanPlateSizes;

    // sumfiles listed in Gaskell's list. Only these are processed.
    //private static ArrayList<String> sumfileList = new ArrayList<String>();

    private static void createFcTables(String fcTableName)
    {
        System.out.println("creating fc");
        try {

            //make a table
            try
            {
                db.dropTable(fcTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + fcTableName + "(" +
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

    private static void createFcTablesCubes(String fcTableName)
    {
        try {

            //make a table
            try
            {
                db.dropTable(fcTableName);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + fcTableName + "(" +
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

    private static void populateFcTables(
            ArrayList<String> fcFiles,
            String fcTableName,
            PerspectiveImage.ImageSource fcSource) throws IOException, SQLException, FitsException
    {
        vestaModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;

        for (String filename : fcFiles)
        {
            boolean filesExist = checkIfAllFcFilesExist(filename, fcSource);
            if (filesExist == false)
                continue;

            System.out.println("starting fc " + count++ + "  " + filename);

            File origFile = new File(filename);
            File rootFolder = origFile.getParentFile().getParentFile().getParentFile().getParentFile();
            String keyName = origFile.getAbsolutePath().replace(rootFolder.getAbsolutePath(), "");
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, fcSource);
            FcImage image = new FcImage(key, vestaModel, false, rootFolder);

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

            if (fcInsert == null)
            {
                fcInsert = db.preparedStatement(
                    "insert into " + fcTableName + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime startTime = new DateTime(image.getStartTime(), DateTimeZone.UTC);
            DateTime stopTime = new DateTime(image.getStopTime(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //startTime = startTime.substring(0, 10) + " " + startTime.substring(11, startTime.length());
            //stopTime = stopTime.substring(0, 10) + " " + stopTime.substring(11, stopTime.length());

            System.out.println("id: " + Long.parseLong(origFile.getName().substring(5, 12)));
            System.out.println("filename: " + origFile.getName());
            System.out.println("starttime: " + startTime);
            System.out.println("stoptime: " + stopTime);
            System.out.println("filter: " + image.getFilter());
            System.out.println("fc: " + image.getCamera());
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

            fcInsert.setLong(1, Long.parseLong(origFile.getName().substring(5, 12), 10));
            fcInsert.setString(2, origFile.getName());
            fcInsert.setLong(3, startTime.getMillis());
            fcInsert.setLong(4, stopTime.getMillis());
            fcInsert.setByte(5, (byte)image.getFilter());
            fcInsert.setByte(6, (byte)image.getCamera());
            fcInsert.setDouble(7, image.getSpacecraftDistance());
            fcInsert.setDouble(8, image.getMinimumHorizontalPixelScale());
            fcInsert.setDouble(9, image.getMaximumHorizontalPixelScale());
            fcInsert.setDouble(10, image.getMinimumVerticalPixelScale());
            fcInsert.setDouble(11, image.getMaximumVerticalPixelScale());
            fcInsert.setBoolean(12, image.containsLimb());
            fcInsert.setDouble(13, image.getMinIncidence());
            fcInsert.setDouble(14, image.getMaxIncidence());
            fcInsert.setDouble(15, image.getMinEmission());
            fcInsert.setDouble(16, image.getMaxEmission());
            fcInsert.setDouble(17, image.getMinPhase());
            fcInsert.setDouble(18, image.getMaxPhase());

            fcInsert.executeUpdate();


            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    private static void populateFcTablesCubes(
            ArrayList<String> fcFiles,
            String fcTableName,
            PerspectiveImage.ImageSource fcSource) throws SQLException, IOException, FitsException
    {
        vestaModel.setModelResolution(0);
        PerspectiveImage.setGenerateFootprint(true);
        PerspectiveImage.setFootprintIsOnLocalDisk(true);

        int count = 0;
        for (String filename : fcFiles)
        {
            boolean filesExist = checkIfAllFcFilesExist(filename, fcSource);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting fc " + filename);

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
            keyName = keyName.replace(".FIT", "");
            ImageKey key = new ImageKey(keyName, fcSource);
            FcImage image = new FcImage(key, vestaModel, false, rootFolder);

            image.loadFootprint();
            footprintPolyData.DeepCopy(image.getUnshiftedFootprint());

            if (fcInsert2 == null)
            {
                fcInsert2 = db.preparedStatement(
                        "insert into " + fcTableName + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = vestaModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                fcInsert2.setInt(1, count);
                fcInsert2.setLong(2, Long.parseLong(origFile.getName().substring(5, 12), 10));
                fcInsert2.setInt(3, i);

                fcInsert2.executeUpdate();

                ++count;
            }

            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkGlobalJavaHash.GC());
        }
    }

    /**
     * Gaskell sent out a list specifying that only these files should be used.
     * Load it.
     * @param listFilename
     * @throws IOException
     */
    /*
    private static void loadGaskellList(String sumfilelistFilename) throws IOException
    {
        InputStream fs = new FileInputStream(sumfilelistFilename);
        InputStreamReader isr = new InputStreamReader(fs);
        BufferedReader in = new BufferedReader(isr);

        String line;

        while ((line = in.readLine()) != null)
        {
            if (line.trim().startsWith("FC"))
            {
                sumfileList.add(line.trim());
            }
        }

        System.out.println("number of sumfiles to process: " + sumfileList.size());

        in.close();
    }
     */

    static boolean checkIfAllFcFilesExist(String line, PerspectiveImage.ImageSource source)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

//        String name = line.substring(0, line.length()-4) + ".lbl";
//        file = new File(name);
//        if (!file.exists())
//            return false;

        // Check for the sumfile if source is Gaskell
        if (source.equals(ImageSource.GASKELL))
        {
            File fcrootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String fcId = (new File(line)).getName().substring(0, 12).replace('B', 'A');
            String name = fcrootdir.getAbsolutePath() + "/sumfiles/" + fcId + ".SUM";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;

            // If the sumfile has no landmarks, then ignore it. Sumfiles that have no landmarks
            // are 1296 bytes long or less
            if (file.length() <= 1296)
                return false;

            // Only process files that are listed in Gaskell's sumfile list
            //if (!sumfileList.contains(fcId))
            //{
            //    System.out.println(fcId + " not in sumfile list");
            //    return false;
            //}
        }
        else
        {
            File fcrootdir = (new File(line)).getParentFile().getParentFile();
            System.out.println(line);
            String fcId = (new File(line)).getName();
            fcId = fcId.substring(0, fcId.length()-4);
            String name = fcrootdir.getAbsolutePath() + "/infofiles/" + fcId + ".INFO";
            System.out.println(name);
            file = new File(name);
            if (!file.exists())
                return false;
        }

        return true;
    }

    /**
     * Only keep 1A files if there's no corresponding 1B file.
     * Also remove files that are not exactly 1024x1024 pixels.
     * @param fcFiles
     * @return
     */
    private static ArrayList<String> removeRedundantAndWrongSizedFiles(
            ArrayList<String> fcFiles)
    {
        ArrayList<String> filesToKeep = new ArrayList<String>();
        for (String path : fcFiles)
        {
            // First ignore files that are not exactly 1024x1024 pixels.
            try
            {
                Fits f = new Fits(path);
                BasicHDU h = f.getHDU(0);

                int x = h.getHeader().getIntValue("NAXIS1");
                int y = h.getHeader().getIntValue("NAXIS2");
                System.out.println("Size: " + x + " " + y + "  " + path);
                f.getStream().close();
                if (x != 1024 || y != 1024)
                    continue;
            }
            catch (FitsException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // If two files have the same exact name preceding the first underscore,
            // but the part after the underscore is different, then only keep the
            // newer file. The newer file should have a higher letter
            // before the .FIT extension.
            {
                char version = path.charAt(path.length()-5);
                String name = new File(path).getName();
                name = name.substring(0, name.indexOf('_'));

                boolean foundNewerVersion = false;
                for (String path2 : fcFiles)
                {
                    char version2 = path2.charAt(path.length()-5);
                    String name2 = new File(path2).getName();
                    name2 = name2.substring(0, name2.indexOf('_'));

                    if (name.equals(name2) && version2 > version)
                    {
                        System.out.println("found newer version " + version + " " + version2 + " " + path + " " + path2);
                        foundNewerVersion = true;
                        break;
                    }
                }

                if (foundNewerVersion)
                    continue;
            }


            // The following checks to make sure that we only keep a 1A file
            // if there is no 1B file.
            if (path.contains("FC11B") || path.contains("FC21B"))
            {
                filesToKeep.add(path);
            }
            else if (path.contains("FC11A") || path.contains("FC21A"))
            {
                // Only compare the part of the filename preceding the underscore
                // as the remaining part of the filename can differ.
                String name = new File(path).getName();
                name = name.substring(0, name.indexOf('_'));
                if (path.contains("FC11A"))
                    name = name.replaceAll("FC11A", "FC11B");
                else
                    name = name.replaceAll("FC21A", "FC21B");

                boolean found1B = false;

                for (String path2 : fcFiles)
                {
                    String name2 = new File(path2).getName();
                    name2 = name2.substring(0, name2.indexOf('_'));

                    if (name.equals(name2))
                        found1B = true;
                }

                if (!found1B)
                    filesToKeep.add(path);
            }
        }

	System.out.println("before redundant " + fcFiles.size());
	System.out.println("before redundant " + filesToKeep.size());
	System.out.println(filesToKeep);

        return filesToKeep;
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        Configuration.setAPLVersion(true);

        NativeLibraryLoader.loadVtkLibraries();

        ModelConfig modelConfig = ModelFactory.getModelConfig(ModelFactory.VESTA, ModelFactory.GASKELL);
        vestaModel = ModelFactory.createSmallBodyModel(modelConfig);

        String fcFileList=args[0];
        //String sumfilelistFilename=args[1];
        int mode = Integer.parseInt(args[2]);

        ArrayList<String> fcFiles = null;
        try {
            fcFiles = FileUtil.getFileLinesAsStringList(fcFileList);
            fcFiles = removeRedundantAndWrongSizedFiles(fcFiles);
            //loadGaskellList(sumfilelistFilename);
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
            createFcTables(FcImagesGaskellTable);
        else if (mode == 2 || mode == 0)
            createFcTablesCubes(FcCubesGaskellTable);
        else if (mode == 3 || mode == 0)
            createFcTables(FcImagesPdsTable);
        else if (mode == 4 || mode == 0)
            createFcTablesCubes(FcCubesPdsTable);

        try
        {
            if (mode == 1 || mode == 0)
                populateFcTables(fcFiles, FcImagesGaskellTable, Image.ImageSource.GASKELL);
            else if (mode == 2 || mode == 0)
                populateFcTablesCubes(fcFiles, FcCubesGaskellTable, Image.ImageSource.GASKELL);
            else if (mode == 3 || mode == 0)
                populateFcTables(fcFiles, FcImagesPdsTable, Image.ImageSource.PDS);
            else if (mode == 4 || mode == 0)
                populateFcTablesCubes(fcFiles, FcCubesPdsTable, Image.ImageSource.PDS);
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
