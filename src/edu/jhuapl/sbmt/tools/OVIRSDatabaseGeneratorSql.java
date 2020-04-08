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
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;

public class OVIRSDatabaseGeneratorSql
{
    static private final String OVIRSSpectraTable = "ovirsspectra";
    static private final String OVIRSCubesTable = "ovirscubes";

    static private SqlManager db = null;
    static private PreparedStatement ovirsInsert = null;
    static private PreparedStatement ovirsInsert2 = null;
    static private ISmallBodyModel bodyModel;
    static private vtkPolyData footprintPolyData;

    static private OVIRS ovirs=new OVIRS();

    private static void createOVIRSTables()
    {
        try {

            //make a table
            try
            {
                db.dropTable(OVIRSSpectraTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + OVIRSSpectraTable + "(" +
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

    private static void createOVIRSTablesCubes()
    {
        try {

            //make a table
            try
            {
                db.dropTable(OVIRSCubesTable);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            db.update(
                    "create table " + OVIRSCubesTable + "(" +
                    "id int PRIMARY KEY, " +
                    "ovirsspectrumid int, " +
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

    private static void populateOVIRSTables(List<String> ovirsFiles) throws SQLException, IOException
    {
        int count = 0;
        for (String filename : ovirsFiles)
        {
            // Don't check if all OVIRS files exist here, since we want to allow searches on spectra
            // that don't intersect the asteroid

            System.out.println("starting ovirs " + count++ + "  " + filename);

            String dayOfYearStr = "";
            String yearStr = "";

            File origFile = new File(filename);
            File f = origFile;

            f = f.getParentFile();
            dayOfYearStr = f.getName();

            f = f.getParentFile();
            yearStr = f.getName();


            BasicSpectrum ovirsSpectrum = SbmtSpectrumModelFactory.createSpectrum(origFile.getAbsolutePath(), ovirs);


            if (ovirsInsert == null)
            {
                ovirsInsert = db.preparedStatement(
                        "insert into " + OVIRSSpectraTable + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime midtime = new DateTime(ovirsSpectrum.getDateTime().toString(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //time = time.substring(0, 10) + " " + time.substring(11, time.length());

            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
            System.out.println("year: " + yearStr);
            System.out.println("dayofyear: " + dayOfYearStr);
            System.out.println("midtime: " + midtime);
            System.out.println("minIncidence: " + ovirsSpectrum.getMinIncidence());
            System.out.println("maxIncidence: " + ovirsSpectrum.getMaxIncidence());
            System.out.println("minEmission: " + ovirsSpectrum.getMinEmission());
            System.out.println("maxEmission: " + ovirsSpectrum.getMaxEmission());
            System.out.println("minPhase: " + ovirsSpectrum.getMinPhase());
            System.out.println("maxPhase: " + ovirsSpectrum.getMaxPhase());
            System.out.println("range: " + ovirsSpectrum.getRange());
//            System.out.println("polygon type: " + ovirsSpectrum.getPolygonTypeFlag());
            System.out.println(" ");


            ovirsInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
            ovirsInsert.setShort(2, Short.parseShort(yearStr));
            ovirsInsert.setShort(3, Short.parseShort(dayOfYearStr));
            ovirsInsert.setLong(4, midtime.getMillis());
            ovirsInsert.setDouble(5, ovirsSpectrum.getMinIncidence());
            ovirsInsert.setDouble(6, ovirsSpectrum.getMaxIncidence());
            ovirsInsert.setDouble(7, ovirsSpectrum.getMinEmission());
            ovirsInsert.setDouble(8, ovirsSpectrum.getMaxEmission());
            ovirsInsert.setDouble(9, ovirsSpectrum.getMinPhase());
            ovirsInsert.setDouble(10, ovirsSpectrum.getMaxPhase());
            ovirsInsert.setDouble(11, ovirsSpectrum.getRange());
//            ovirsInsert.setShort(12, ovirsSpectrum.getPolygonTypeFlag());

            ovirsInsert.executeUpdate();
        }
    }

    private static void populateOVIRSTablesCubes(List<String> ovirsFiles) throws SQLException, IOException
    {
        int count = 0;
        int filecount = 0;
        for (String filename : ovirsFiles)
        {
            boolean filesExist = checkIfAllOVIRSFilesExist(filename);
            if (filesExist == false)
                continue;

            System.out.println("\n\nstarting ovirs " + filename + " " + filecount++ + "/" + ovirsFiles.size());

            File origFile = new File(filename);

            OVIRSSpectrum ovirsSpectrum = (OVIRSSpectrum)SbmtSpectrumModelFactory.createSpectrum(origFile.getAbsolutePath(), ovirs);
            BasicSpectrumRenderer<OVIRSSpectrum> ovirsSpectrumRenderer = new BasicSpectrumRenderer<OVIRSSpectrum>(ovirsSpectrum, bodyModel, true);
            ovirsSpectrumRenderer.generateFootprint();

            if (footprintPolyData == null)
                footprintPolyData = new vtkPolyData();
            footprintPolyData.DeepCopy(ovirsSpectrumRenderer.getUnshiftedFootprint());
            footprintPolyData.ComputeBounds();


            if (ovirsInsert2 == null)
            {
                ovirsInsert2 = db.preparedStatement(
                        "insert into " + OVIRSCubesTable + " values (?, ?, ?)");
            }

            TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
            System.out.println("id: " + count);
            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                ovirsInsert2.setInt(1, count);
                ovirsInsert2.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
                ovirsInsert2.setInt(3, i);

                ovirsInsert2.executeUpdate();

                ++count;
            }

            ovirsSpectrumRenderer.Delete();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
            System.out.println(" ");
            System.out.println(" ");
        }
    }

    static boolean checkIfAllOVIRSFilesExist(String line)
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

        String ovirsFileList=args[0];
        int mode = Integer.parseInt(args[1]);

        List<String> ovirsFiles = null;
        try {
            ovirsFiles = FileUtil.getFileLinesAsStringList(ovirsFileList);
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
            createOVIRSTables();
        else if (mode == 6 || mode == 0)
            createOVIRSTablesCubes();


        try
        {
            if (mode == 5 || mode == 0)
                populateOVIRSTables(ovirsFiles);
            else if (mode == 6 || mode == 0)
                populateOVIRSTablesCubes(ovirsFiles);
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
