package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectraFactory;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.BasicSpectrumRenderer;

public class OTESDatabaseGeneratorSql
{
    static private final String OTESSpectraTable = "otesspectra";
    static private final String OTESCubesTable = "otescubes";

    static private SqlManager db = null;
    static private PreparedStatement otesInsert = null;
    static private PreparedStatement otesInsert2 = null;
    static private ISmallBodyModel bodyModel;
    static private vtkPolyData footprintPolyData;

    static private OTES otes=new OTES();

    private static void createOTESTables(String modelName, String dataType, boolean appendTables)
    {
    	String tableName = modelName + "_" + OTESSpectraTable + "_" + dataType;
    	System.out.println("OTESDatabaseGeneratorSql: createOTESTables: tablename " + tableName);
        try {

            //make a table
        	if (appendTables == false)
        	{
	            try
	            {
	                db.dropTable(tableName);
	            }
	            catch(Exception e)
	            {
	                e.printStackTrace();
	            }
        	}

            db.update(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
                    "id int PRIMARY KEY AUTO_INCREMENT, " +
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

    private static void createOTESTablesCubes(String modelName, String dataType, boolean appendTables)
    {
    	String tableName = modelName + "_" + OTESCubesTable + "_" + dataType;
        try {

            //make a table
        	if (appendTables == false)
        	{
	            try
	            {
	                db.dropTable(tableName);
	            }
	            catch(Exception e)
	            {
	                e.printStackTrace();
	            }
        	}

            db.update(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
                    "id int PRIMARY KEY AUTO_INCREMENT, " +
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

    private static void populateOTESTables(String modelName, String dataType, List<String> otesFiles) throws SQLException, IOException
    {
    	String tableName = modelName + "_" + OTESSpectraTable + "_" + dataType;
        int count = 0;
        for (String filename : otesFiles)
        {
            // Don't check if all OTES files exist here, since we want to allow searches on spectra
            // that don't intersect the asteroid

        	//20181102t040042
            System.out.println("starting otes " + count + "  " + filename);

            String dayOfYearStr = "";
            String yearStr = "";

//            String[] parts = filename.split("/");
//            String[] parts2 = parts[1].split(" ");
//            String[] parts3 = parts2[0].split("_");
//            String name = parts3[0];
            String name = filename.substring(filename.lastIndexOf("/")+1);

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd't'hhmmss's'SSS");
            Date date = null;
			try
			{
				date = format.parse(name);
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            SimpleDateFormat yearFormat = new SimpleDateFormat("y");
            SimpleDateFormat doyFormat = new SimpleDateFormat("D");

            dayOfYearStr = doyFormat.format(date);
            yearStr = yearFormat.format(date);

            File origFile = new File(filename);
//            File f = origFile;
//
//            f = f.getParentFile();
//            dayOfYearStr = f.getName();
//
//            f = f.getParentFile();
//            yearStr = f.getName();


            BasicSpectrum otesSpectrum = SbmtSpectrumModelFactory.createSpectrum(filename, otes);
//            otesSpectrum.isCustomSpectra = true;

            if (otesInsert == null)
            {
                otesInsert = db.preparedStatement(
                        "insert into " + tableName + " (year, day, midtime, minincidence, maxincidence, minemission, maxemission, minphase, maxphase, range) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime midtime = new DateTime(new DateTime(date).toString(), DateTimeZone.UTC);
            // Replace the "T" with a space
            //time = time.substring(0, 10) + " " + time.substring(11, time.length());

//            System.out.println("id: " + Integer.parseInt(origFile.getName().substring(2, 11)));
            System.out.println("id: " + count);
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


//            otesInsert.setInt(1, Integer.parseInt(origFile.getName().substring(2, 11)));
//            otesInsert.setInt(1, count);
            otesInsert.setShort(1, Short.parseShort(yearStr));
            otesInsert.setShort(2, Short.parseShort(dayOfYearStr));
            otesInsert.setLong(3, midtime.getMillis());
            otesInsert.setDouble(4, otesSpectrum.getMinIncidence());
            otesInsert.setDouble(5, otesSpectrum.getMaxIncidence());
            otesInsert.setDouble(6, otesSpectrum.getMinEmission());
            otesInsert.setDouble(7, otesSpectrum.getMaxEmission());
            otesInsert.setDouble(8, otesSpectrum.getMinPhase());
            otesInsert.setDouble(9, otesSpectrum.getMaxPhase());
            otesInsert.setDouble(10, otesSpectrum.getRange());
//            otesInsert.setShort(12, otesSpectrum.getPolygonTypeFlag());

            int rowCount = otesInsert.executeUpdate();

            populateOTESCubeTableForFile(modelName, dataType, filename, rowCount-1);
            count++;
        }
    }

    private static void populateOTESCubeTableForFile(String modelName, String dataType, String otesFile, int spectrumIndex) throws SQLException, IOException
    {
    	 File origFile = new File(otesFile);
    	 String tableName = modelName + "_" + OTESCubesTable + "_" + dataType;

    	 IBasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(otesFile, otes, true);
//    	 otesSpectrumRenderer.getSpectrum().isCustomSpectra = true;
//         OTESSpectrum otesSpectrum = (OTESSpectrum)SbmtSpectrumModelFactory.createSpectrum(origFile.getAbsolutePath(), otes);
//         BasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer = new BasicSpectrumRenderer<OTESSpectrum>(otesSpectrum, bodyModel, true);
         otesSpectrumRenderer.generateFootprint();


         if (footprintPolyData == null)
             footprintPolyData = new vtkPolyData();
         footprintPolyData.DeepCopy(otesSpectrumRenderer.getUnshiftedFootprint());
         footprintPolyData.ComputeBounds();


         if (otesInsert2 == null)
         {
             otesInsert2 = db.preparedStatement(
                     "insert into " + tableName + " (otesspectrumid, cubeid) values (?, ?)");
         }

         TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
         System.out.println("cubeIds:  " + cubeIds);
         System.out.println("number of cubes: " + cubeIds.size());
//         System.out.println("id: " + count);
         System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

         for (Integer i : cubeIds)
         {
//             otesInsert2.setInt(1, count);
             otesInsert2.setInt(1, spectrumIndex);
             otesInsert2.setInt(2, i);

             otesInsert2.executeUpdate();

//             ++count;
         }

         otesSpectrumRenderer.Delete();
         System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
         System.out.println(" ");
         System.out.println(" ");
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
        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        // default configuration parameters
        boolean aplVersion = true;
        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/sbmt");

    	// Important: set the mission before changing things in the Configuration. Otherwise,
        // setting the mission will undo those changes.
        SbmtMultiMissionTool.configureMission();

        // basic default configuration, most of these will be overwritten by the configureMission() method
        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        // authentication
        Authenticator.authenticate();

        // initialize view config
        SmallBodyViewConfig.fromServer = true;

        SmallBodyViewConfig.initialize();

        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadVtkLibraries();



        boolean appendTables = false;
        boolean modifyMain = false;
        boolean remote = false;
        String bodyName="";
        String authorName="";
        String versionString = null;
        String diffFileList = null;
        String dataType = null;

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
            else if (args[i].equals("--dataType"))
            {
            	dataType = args[++i];
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

        SmallBodyViewConfig config = null;
        if (versionString != null)
        	config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName), ShapeModelType.provide(authorName), versionString);
        else
        	config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName), ShapeModelType.provide(authorName));

        bodyModel = SbmtModelFactory.createSmallBodyModel(config);

        OREXSpectraFactory.initializeModels(bodyModel);

        String otesFileList = rootURL + File.separator + "data/bennu/shared/otes/" + dataType + "/spectrumlist.txt";
//        int mode = Integer.parseInt(args[1]);

        List<String> otesFiles = null;
        List<String> updatedFilenames = new ArrayList<String>();
        try {
            otesFiles = FileUtil.getFileLinesAsStringList(otesFileList.substring(5));
//            for (String otesFile : otesFiles)
            for (int j=0; j<10; j++)
            {
            	String otesFile = otesFiles.get(j);
            	String actualName = (rootURL + File.separator + "data/bennu/shared/otes/" + dataType + "/spectra/" + otesFile.split(" ")[0])/*.substring(7)*/;
            	System.out.println("OTESDatabaseGeneratorSql: main: actual name " + actualName);
            	updatedFilenames.add(actualName);
            }
        }
        catch (IOException e2) {
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

        String modelName = "bennu_" + authorName.toLowerCase().replace("-", "");
        System.out.println("OTESDatabaseGeneratorSql: main: model name " + modelName);
        createOTESTables(modelName, dataType, appendTables);
        createOTESTablesCubes(modelName, dataType, appendTables);

        try
		{
			populateOTESTables(modelName, dataType, updatedFilenames);
//	        populateOTESTablesCubes(otesFiles);

		}
		catch (SQLException | IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//        if (mode == 5 || mode == 0)
//            createOTESTables();
//        else if (mode == 6 || mode == 0)
//            createOTESTablesCubes();
//
//
//        try
//        {
//            if (mode == 5 || mode == 0)
//                populateOTESTables(otesFiles);
//            else if (mode == 6 || mode == 0)
//                populateOTESTablesCubes(otesFiles);
//        }
//        catch (Exception e1) {
//            e1.printStackTrace();
//        }


        try
        {
            db.shutdown();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
