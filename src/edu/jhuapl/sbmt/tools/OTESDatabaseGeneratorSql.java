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
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class OTESDatabaseGeneratorSql
{
	static private final String OTESSpectraTable = "otesspectra";
	static private final String OTESCubesTable = "otescubes";

	static private SqlManager db = null;
	static private PreparedStatement otesInsert = null;
	static private PreparedStatement otesInsert2 = null;
	static private ISmallBodyModel bodyModel;
	static private vtkPolyData footprintPolyData;
	static private boolean writeToDB = true;
	static private boolean appendTables;

	static private OTES otes = new OTES();

	static Logger logger = Logger.getAnonymousLogger();

	private static void dropTable(String tableName)
	{
		try
		{
			db.dropTable(tableName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void createOTESTables(String modelName, String dataType, boolean appendTables) throws SQLException
	{
		String tableName = modelName + "_" + OTESSpectraTable + "_" + dataType;
		if (appendTables == false) dropTable(tableName);

		// make a table
		db.update("CREATE TABLE IF NOT EXISTS " + tableName + "(" + "id int PRIMARY KEY AUTO_INCREMENT, "
				+ "year smallint, " + "day smallint, " + "midtime bigint, " + "minincidence double,"
				+ "maxincidence double," + "minemission double," + "maxemission double," + "minphase double,"
				+ "maxphase double," + "minrange double," + "maxrange double, " + "filename char(128))");
	}

	private static void createOTESTablesCubes(String modelName, String dataType, boolean appendTables) throws SQLException
	{
		String tableName = modelName + "_" + OTESCubesTable + "_" + dataType;
		if (appendTables == false) dropTable(tableName);

		// make a table
		db.update("CREATE TABLE IF NOT EXISTS " + tableName + "(" + "id int PRIMARY KEY AUTO_INCREMENT, "
				+ "otesspectrumid int, " + "cubeid char(128))");
	}

	private static void populateOTESTables(String modelName, String dataType, List<String> otesFiles)
			throws SQLException, IOException
	{
		String tableName = modelName + "_" + OTESSpectraTable + "_" + dataType;
		int count = 0;
		for (String filename : otesFiles)
		{
			if (count % 100 == 0)
				logger.log(Level.INFO,
						"Processing OTES index:" + count + " of " + otesFiles.size() + ", filename: " + filename);
			String dayOfYearStr = "";
			String yearStr = "";
			String name = filename.substring(filename.lastIndexOf("/") + 1);

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

			IBasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer = SbmtSpectrumModelFactory
					.createSpectrumRenderer(filename, otes, true);
			otesSpectrumRenderer.generateFootprint();

			//if no intersection took place, skip this loop
			if (otesSpectrumRenderer.getShiftedFootprint() == null) continue;

			DateTime midtime = new DateTime(new DateTime(date).toString(), DateTimeZone.UTC);
			String filenamePlusParent = filename.substring(filename.lastIndexOf("otes/") + 5);

			if (writeToDB == true)
			{
				List<List<Object>> checkQuery;
				// If appending and there is already an entry for the filename then skip
	            if(appendTables){
	            	checkQuery = db.query("SELECT * FROM `" + tableName + "` WHERE `filename` = \"" +
	                    new File(filename).getName() + "\"");
	                if(checkQuery != null && !checkQuery.isEmpty() &&
	                		checkQuery.get(0) != null && !checkQuery.get(0).isEmpty() &&
	                				checkQuery.get(0).get(0) != null){
	                	int id = Integer.parseInt((String)checkQuery.get(0).get(0));
	                    System.out.println("\n\nskipping image insertion for " + filename + ", already in table, updating cubes for id " + id);
//	                    populateOTESCubeTableForFile(modelName, dataType, otesSpectrumRenderer, id);
	                    continue;
	                }
	            }


				if (otesInsert == null)
				{
					// the index auto increments, so start with the year column
					otesInsert = db.preparedStatement("insert into " + tableName
							+ " (year, day, midtime, minincidence, maxincidence, minemission, maxemission, minphase, maxphase, minrange, maxrange, filename) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				}

				// The index autoincrements, so start adding with the year
				// string
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
				otesInsert.setString(12, filenamePlusParent);
//				logger.log(Level.INFO, "insert statement for spectra populated");
				otesInsert.executeUpdate();
//				logger.log(Level.INFO, "insert statement updated completed");
				ResultSet rs = otesInsert.getGeneratedKeys();
				rs.next();
				populateOTESCubeTableForFile(modelName, dataType, otesSpectrumRenderer, rs.getInt(1));
			}
			else
				populateOTESCubeTableForFile(modelName, dataType, otesSpectrumRenderer, count);

			count++;
		}
	}

	private static void populateOTESCubeTableForFile(String modelName, String dataType,
			IBasicSpectrumRenderer<OTESSpectrum> otesSpectrumRenderer, int spectrumIndex)
			throws SQLException, IOException
	{
		logger.log(Level.FINE, "Populating cube table for " + spectrumIndex);

		String tableName = modelName + "_" + OTESCubesTable + "_" + dataType;

		if (footprintPolyData == null)
			footprintPolyData = new vtkPolyData();
		footprintPolyData.DeepCopy(otesSpectrumRenderer.getShiftedFootprint());
		footprintPolyData.ComputeBounds();
		logger.log(Level.FINE, "Footprint bounds calculated");

		TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
		logger.log(Level.FINE, "Intersected cubes calculated");

		// System.out.println("cubeIds: " + cubeIds);
		// System.out.println("number of cubes: " + cubeIds.size());
		// System.out.println("id: " + count);
		// System.out.println("number of cells in polydata " +
		// footprintPolyData.GetNumberOfCells());

		if (writeToDB == true)
		{
			//TODO add check to see if spectrumID currently exists and call update
			//instead of insert
			if (otesInsert2 == null)
			{
				// index autoincrements, so start with the otesspectrum id column
				otesInsert2 = db
						.preparedStatement("insert into " + tableName + " (otesspectrumid, cubeid) values (?, ?)");
			}

			for (Integer i : cubeIds)
			{
				otesInsert2.setInt(1, spectrumIndex);
				otesInsert2.setInt(2, i);
				otesInsert2.executeUpdate();
			}
			logger.log(Level.FINE, "All cube rows inserted");

		}
		otesSpectrumRenderer.Delete();
		vtkObject.JAVA_OBJECT_MANAGER.gc(true);
		logger.log(Level.FINE, "Done inserting cube rows");
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
	 * @throws SQLException
	 */

	public static void main(String[] args) throws IOException, SQLException
	{
//		logger.setLevel(Level.OFF);
		logger.log(Level.INFO, "Starting main method");
		final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
		// default configuration parameters
		boolean aplVersion = true;
		String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/sbmt");

		// Important: set the mission before changing things in the
		// Configuration. Otherwise,
		// setting the mission will undo those changes.
		SbmtMultiMissionTool.configureMission();

		// basic default configuration, most of these will be overwritten by the
		// configureMission() method
		Configuration.setAPLVersion(aplVersion);
		Configuration.setRootURL(rootURL);

//		// authentication
//		Authenticator.authenticate();

		// initialize view config
		SmallBodyViewConfig.fromServer = true;

		SmallBodyViewConfig.initialize();

		System.setProperty("java.awt.headless", "true");
		NativeLibraryLoader.loadVtkLibraries();

		boolean appendTables = false;
		boolean modifyMain = false;
		boolean remote = false;
		boolean localRun = false;
		String bodyName = "";
		String authorName = "";
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
				logger.setLevel(Level.INFO);
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
			else if (args[i].equals("--dryRun"))
			{
				writeToDB = false;
			}
			else if (args[i].equals("--localRun"))
			{
				localRun = true;
			}
			else
			{
				// We've encountered something that is not an option, must be at
				// the args
				break;
			}
		}
		logger.log(Level.INFO, "Parsed arguments, initializing body model");
		SmallBodyViewConfig config = null;
		if (versionString != null)
			config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName),
					ShapeModelType.provide(authorName), versionString);
		else
			config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.valueOf(bodyName),
					ShapeModelType.provide(authorName));

		bodyModel = SbmtModelFactory.createSmallBodyModel(config);

		logger.log(Level.INFO, "Body Model initialized");

		OREXSpectraFactory.initializeModels(bodyModel);

		String otesFileList = rootURL + File.separator + "data/bennu/shared/otes/" + dataType + "/spectrumlist.txt";

		List<String> otesFiles = null;
		List<String> updatedFilenames = new ArrayList<String>();
		try
		{

			if (localRun == true)
			{
				File otesFileFromServer = FileCache.getFileFromServer(otesFileList);
				otesFiles = FileUtil.getFileLinesAsStringList(otesFileFromServer.getAbsolutePath());
			}
			else
			{
				otesFiles = FileUtil.getFileLinesAsStringList(otesFileList.substring(5));
			}

			if (endIndex > otesFiles.size())
				endIndex = otesFiles.size();
			for (int j = startIndex; j < endIndex; j++)
			{
				String otesFile = otesFiles.get(j);
				String actualName = (rootURL + File.separator + "data/bennu/shared/otes/" + dataType + "/spectra/"
						+ otesFile.split(" ")[0]);
				if (localRun == true) actualName = ("bennu/shared/otes/" + dataType + "/spectra/" + otesFile.split(" ")[0]);
				updatedFilenames.add(actualName);
			}
		}
		catch (IOException e2)
		{
			e2.printStackTrace();
			return;
		}
		logger.log(Level.INFO, "Filename list built, number of entries " + (endIndex - startIndex));

		String modelName = "bennu_" + authorName.toLowerCase().replace("-", "");

		if (writeToDB == true)
		{
			try
			{
				db = new SqlManager(null);
			}
			catch (Exception ex1)
			{
				ex1.printStackTrace();
				return;
			}

			createOTESTables(modelName, dataType, appendTables);
			createOTESTablesCubes(modelName, dataType, appendTables);
		}
		logger.log(Level.INFO, "Database tables created.  ");
		try
		{
			populateOTESTables(modelName, dataType, updatedFilenames);
			if (writeToDB == true)
				db.shutdown();

		}
		catch (SQLException | IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
