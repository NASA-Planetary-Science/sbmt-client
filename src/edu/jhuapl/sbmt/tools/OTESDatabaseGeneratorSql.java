package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkObject;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;

public class OTESDatabaseGeneratorSql
{
    static private final String OTESSpectraTable = "otesspectra";
    static private final String OTESCubesTable = "otescubes";

    static private SqlManager db = null;
    static private PreparedStatement otesInsert = null;
    static private PreparedStatement otesInsert2 = null;
    static private ISmallBodyModel bodyModel;
    //static private vtkPolyDataReader footprintReader;
    static private vtkPolyData footprintPolyData;
    //static private double[] meanPlateSizes;

    static private OTES otes=new OTES();

    private static void createOTESTables()
    {
        try {

            //make a table
            try
            {
                db.dropTable(OTESSpectraTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + OTESSpectraTable + "(" +
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
                    "range double)"
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

    private static void createOTESTablesCubes()
    {
        try {

            //make a table
            try
            {
                db.dropTable(OTESCubesTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + OTESCubesTable + "(" +
                    "id int PRIMARY KEY, " +
                    "otesspectrumid int, " +
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

    private static void populateOTESTables(List<String> otesFiles) throws SQLException, IOException
    {
        int count = 0;
        for (String filename : otesFiles)
        {
            // Don't check if all OTES files exist here, since we want to allow searches on spectra
            // that don't intersect the asteroid

            System.out.println("starting otes " + count++ + "  " + filename);

            String dayOfYearStr = "";
            String yearStr = "";

            File origFile = new File(filename);
            File f = origFile;

            f = f.getParentFile();
            dayOfYearStr = f.getName();

            f = f.getParentFile();
            yearStr = f.getName();


            BasicSpectrum otesSpectrum = SbmtSpectrumModelFactory.createSpectrum(origFile.getAbsolutePath(), otes);


            if (otesInsert == null)
            {
                otesInsert = db.preparedStatement(
                        "insert into " + OTESSpectraTable + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime midtime = new DateTime(otesSpectrum.getDateTime().toString(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //time = time.substring(0, 10) + " " + time.substring(11, time.length());

            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
            System.out.println("year: " + yearStr);
            System.out.println("dayofyear: " + dayOfYearStr);
            System.out.println("midtime: " + midtime);
            System.out.println("minIncidence: " + otesSpectrum.getMinIncidence());
            System.out.println("maxIncidence: " + otesSpectrum.getMaxIncidence());
            System.out.println("minEmission: " + otesSpectrum.getMinEmission());
            System.out.println("maxEmission: " + otesSpectrum.getMaxEmission());
            System.out.println("minPhase: " + otesSpectrum.getMinPhase());
            System.out.println("maxPhase: " + otesSpectrum.getMaxPhase());
            System.out.println("range: " + otesSpectrum.getRange());
//            System.out.println("polygon type: " + otesSpectrum.getPolygonTypeFlag());
            System.out.println(" ");


            otesInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            otesInsert.setShort(2, Short.parseShort(yearStr));
            otesInsert.setShort(3, Short.parseShort(dayOfYearStr));
            otesInsert.setLong(4, midtime.getMillis());
            otesInsert.setDouble(5, otesSpectrum.getMinIncidence());
            otesInsert.setDouble(6, otesSpectrum.getMaxIncidence());
            otesInsert.setDouble(7, otesSpectrum.getMinEmission());
            otesInsert.setDouble(8, otesSpectrum.getMaxEmission());
            otesInsert.setDouble(9, otesSpectrum.getMinPhase());
            otesInsert.setDouble(10, otesSpectrum.getMaxPhase());
            otesInsert.setDouble(11, otesSpectrum.getRange());
//            otesInsert.setShort(12, otesSpectrum.getPolygonTypeFlag());

            otesInsert.executeUpdate();
        }
    }

    private static void populateOTESTablesCubes(List<String> otesFiles) throws SQLException, IOException
    {
        int count = 0;
        int filecount = 0;
        for (String filename : otesFiles)
        {
            boolean filesExist = checkIfAllOTESFilesExist(filename);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting otes " + filename + " " + filecount++ + "/" + otesFiles.size());

            File origFile = new File(filename);

            OTESSpectrum otesSpectrum = (OTESSpectrum)SbmtSpectrumModelFactory.createSpectrum(origFile.getAbsolutePath(), otes);
            BasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer = new BasicSpectrumRenderer<OTESSpectrum>(otesSpectrum, bodyModel, true);
            otesSpectrumRenderer.generateFootprint();

            if (footprintPolyData == null)
                footprintPolyData = new vtkPolyData();
            footprintPolyData.DeepCopy(otesSpectrumRenderer.getUnshiftedFootprint());
            footprintPolyData.ComputeBounds();


            if (otesInsert2 == null)
            {
                otesInsert2 = db.preparedStatement(
                        "insert into " + OTESCubesTable + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                otesInsert2.setInt(1, count);
                otesInsert2.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
                otesInsert2.setInt(3, i);

                otesInsert2.executeUpdate();

                ++count;
            }

            otesSpectrumRenderer.Delete();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    static boolean checkIfAllOTESFilesExist(String line)
    {
        File file = new File(line);
        if (!file.exists())
            return false;

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

        String bodyName = args[0];
        String authorName = args[1];
        String versionString = null;

        bodyModel = SbmtModelFactory.createSmallBodyModel(SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName), ShapeModelType.provide(authorName), versionString));

        String otesFileList=args[0];
        int mode = Integer.parseInt(args[1]);

        List<String> otesFiles = null;
        try {
            otesFiles = FileUtil.getFileLinesAsStringList(otesFileList);
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
            createOTESTables();
        else if (mode == 6 || mode == 0)
            createOTESTablesCubes();


        try
        {
            if (mode == 5 || mode == 0)
                populateOTESTables(otesFiles);
            else if (mode == 6 || mode == 0)
                populateOTESTablesCubes(otesFiles);
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
