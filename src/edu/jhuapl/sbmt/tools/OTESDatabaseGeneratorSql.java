package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                    "minrange double," +
                    "maxrange double, " +
                    "filename char(128))"
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
            System.out.println("Processing OTES index:" + count + "  filename: " + filename);

            String dayOfYearStr = "";
            String yearStr = "";
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

       	 	IBasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(filename, otes, true);
       	 	otesSpectrumRenderer.generateFootprint();

            if (otesInsert == null)
            {
            	//the index auto increments, so start with the year column
                otesInsert = db.preparedStatement(
                        "insert into " + tableName + " (year, day, midtime, minincidence, maxincidence, minemission, maxemission, minphase, maxphase, minrange, maxrange, filename) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }

            DateTime midtime = new DateTime(new DateTime(date).toString(), DateTimeZone.UTC);

//            System.out.println("id: " + count);
//            System.out.println("year: " + yearStr);
//            System.out.println("dayofyear: " + dayOfYearStr);
//            System.out.println("midtime: " + midtime);
//            System.out.println("minIncidence: " + otesSpectrumRenderer.getMinIncidence());
//            System.out.println("maxIncidence: " + otesSpectrumRenderer.getMaxIncidence());
//            System.out.println("minEmission: " + otesSpectrumRenderer.getMinEmission());
//            System.out.println("maxEmission: " + otesSpectrumRenderer.getMaxEmission());
//            System.out.println("minPhase: " + otesSpectrumRenderer.getMinPhase());
//            System.out.println("maxPhase: " + otesSpectrumRenderer.getMaxPhase());
//            System.out.println("minrange: " + otesSpectrumRenderer.getMinRange());
//            System.out.println("maxrange: " + otesSpectrumRenderer.getMaxRange());
            System.out.println(" ");

            //The index autoincrements, so start adding with the year string
            otesInsert.setShort(1, Short.parseShort(yearStr));
            otesInsert.setShort(2, Short.parseShort(dayOfYearStr));
            otesInsert.setLong(3, midtime.getMillis());
            otesInsert.setDouble(4, otesSpectrumRenderer.getMinIncidence());
            otesInsert.setDouble(5, otesSpectrumRenderer.getMaxIncidence());
            otesInsert.setDouble(6, otesSpectrumRenderer.getMinEmission());
            otesInsert.setDouble(7, otesSpectrumRenderer.getMaxEmission());
            otesInsert.setDouble(8, otesSpectrumRenderer.getMinPhase());
            otesInsert.setDouble(9, otesSpectrumRenderer.getMaxPhase());
            otesInsert.setDouble(10, otesSpectrumRenderer.getMinRange());
            otesInsert.setDouble(11, otesSpectrumRenderer.getMinRange());
            String filenamePlusParent = filename.substring(filename.lastIndexOf("otes/")+5);
            otesInsert.setString(12, filenamePlusParent);
//            otesInsert.setShort(12, otesSpectrum.getPolygonTypeFlag());

            otesInsert.executeUpdate();
            ResultSet rs = otesInsert.getGeneratedKeys();
            rs.next();
            populateOTESCubeTableForFile(modelName, dataType, otesSpectrumRenderer, rs.getInt(1));
            count++;
        }
    }

    private static void populateOTESCubeTableForFile(String modelName, String dataType, IBasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer, int spectrumIndex) throws SQLException, IOException
    {
    	 String tableName = modelName + "_" + OTESCubesTable + "_" + dataType;

         if (footprintPolyData == null)
             footprintPolyData = new vtkPolyData();
         footprintPolyData.DeepCopy(otesSpectrumRenderer.getUnshiftedFootprint());
         footprintPolyData.ComputeBounds();


         if (otesInsert2 == null)
         {
        	//index autoincrements, so start with the otesspectrum id column
             otesInsert2 = db.preparedStatement(
                     "insert into " + tableName + " (otesspectrumid, cubeid) values (?, ?)");
         }

         TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
//         System.out.println("cubeIds:  " + cubeIds);
         System.out.println("number of cubes: " + cubeIds.size());
//         System.out.println("id: " + count);
//         System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

         for (Integer i : cubeIds)
         {
        	 //index autoincrements, so start with the otesspectrum id column
//             otesInsert2.setInt(1, count);
             otesInsert2.setInt(1, spectrumIndex);
             otesInsert2.setInt(2, i);

             otesInsert2.executeUpdate();

//             ++count;
         }

         otesSpectrumRenderer.Delete();
         System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
//         System.out.println(" ");
//         System.out.println(" ");
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

            System.out.println("\n\nProcessing otes " + filename + " " + filecount++ + "/" + otesFiles.size());

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
//            System.out.println("cubeIds:  " + cubeIds);
            System.out.println("number of cubes: " + cubeIds.size());
//            System.out.println("id: " + count);
//            System.out.println("number of cells in polydata " + footprintPolyData.GetNumberOfCells());

            for (Integer i : cubeIds)
            {
                otesInsert2.setInt(1, count);
                otesInsert2.setInt(2, Integer.parseInt(origFile.getName().substring(2, 11)));
                otesInsert2.setInt(3, i);

                otesInsert2.executeUpdate();

                ++count;
            }

            otesSpectrumRenderer.Delete();
//            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
//            System.out.println(" ");
//            System.out.println(" ");
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
        int startIndex = 0;
        int endIndex = 0;

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
            else if (args[i].equals("--startIndex"))
            {
            	startIndex = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals("--endIndex"))
            {
            	endIndex = Integer.parseInt(args[++i]);
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

        List<String> otesFiles = null;
        List<String> updatedFilenames = new ArrayList<String>();
        try {
            otesFiles = FileUtil.getFileLinesAsStringList(otesFileList.substring(5));
//            for (String otesFile : otesFiles)
            if (endIndex > otesFiles.size()) endIndex = otesFiles.size();
            for (int j=startIndex; j<endIndex; j++)
            {
            	String otesFile = otesFiles.get(j);
            	String actualName = (rootURL + File.separator + "data/bennu/shared/otes/" + dataType + "/spectra/" + otesFile.split(" ")[0]);
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
        createOTESTables(modelName, dataType, appendTables);
        createOTESTablesCubes(modelName, dataType, appendTables);

        try
		{
			populateOTESTables(modelName, dataType, updatedFilenames);
//	        populateOTESTablesCubes(otesFiles);
			db.shutdown();

		}
		catch (SQLException | IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

}
