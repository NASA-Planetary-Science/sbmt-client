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
import edu.jhuapl.sbmt.client.SbmtModelFactory;
import edu.jhuapl.sbmt.client2.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.common.client.ISmallBodyModel;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectraFactory;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;

public class OVIRSDatabaseGeneratorSql
{
    static private final String OVIRSSpectraTable = "ovirsspectra";
    static private final String OVIRSCubesTable = "ovirscubes";

    static private SqlManager db = null;
    static private PreparedStatement ovirsInsert = null;
    static private PreparedStatement ovirsInsert2 = null;
    static private ISmallBodyModel bodyModel;
    static private vtkPolyData footprintPolyData;
    static private boolean writeToDB = true;

	static private OVIRS ovirs = new OVIRS();

	static Logger logger = Logger.getAnonymousLogger();

	private static void createOVIRSTables(String modelName, String dataType, boolean appendTables)
	{
		String tableName = modelName + "_" + OVIRSSpectraTable + "_" + dataType;
		try
		{

			// make a table
			if (appendTables == false)
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

			db.update("CREATE TABLE IF NOT EXISTS " + tableName + "(" + "id int PRIMARY KEY AUTO_INCREMENT, "
					+ "year smallint, " + "day smallint, " + "midtime bigint, " + "minincidence double,"
					+ "maxincidence double," + "minemission double," + "maxemission double," + "minphase double,"
					+ "maxphase double," + "minrange double," + "maxrange double, " + "filename char(128))");
		}
		catch (SQLException ex2)
		{
			ex2.printStackTrace();
		}
	}

	private static void createOVIRSTablesCubes(String modelName, String dataType, boolean appendTables)
	{
		String tableName = modelName + "_" + OVIRSCubesTable + "_" + dataType;
		try
		{

			// make a table
			if (appendTables == false)
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

			db.update("CREATE TABLE IF NOT EXISTS " + tableName + "(" + "id int PRIMARY KEY AUTO_INCREMENT, "
					+ "ovirsspectrumid int, " + "cubeid char(128))");
		}
		catch (SQLException ex2)
		{
			ex2.printStackTrace();
		}
	}

	private static void populateOVIRSTables(String modelName, String dataType, List<String> ovirsFiles)
			throws SQLException, IOException
	{
		String tableName = modelName + "_" + OVIRSSpectraTable + "_" + dataType;
		int count = 0;
		for (String filename : ovirsFiles)
		{
			if (count % 100 == 0)
				logger.log(Level.INFO,
						"Processing OVIRS index:" + count + "of " + ovirsFiles.size() + ", filename: " + filename);
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

			IBasicSpectrumRenderer<OVIRSSpectrum> ovirsSpectrumRenderer = SbmtSpectrumModelFactory
					.createSpectrumRenderer(filename, ovirs, true);
			ovirsSpectrumRenderer.generateFootprint();

			//if no intersection took place, skip this loop
			if (ovirsSpectrumRenderer.getShiftedFootprint() == null) continue;

			DateTime midtime = new DateTime(new DateTime(date).toString(), DateTimeZone.UTC);
			String filenamePlusParent = filename.substring(filename.lastIndexOf("ovirs/") + 5);

			if (writeToDB == true)
			{
				if (ovirsInsert == null)
				{
					// the index auto increments, so start with the year column
					ovirsInsert = db.preparedStatement("insert into " + tableName
							+ " (year, day, midtime, minincidence, maxincidence, minemission, maxemission, minphase, maxphase, minrange, maxrange, filename) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				}

				// The index autoincrements, so start adding with the year
				// string
				ovirsInsert.setShort(1, Short.parseShort(yearStr));
				ovirsInsert.setShort(2, Short.parseShort(dayOfYearStr));
				ovirsInsert.setLong(3, midtime.getMillis());
				ovirsInsert.setDouble(4, ovirsSpectrumRenderer.getMinIncidence());
				ovirsInsert.setDouble(5, ovirsSpectrumRenderer.getMaxIncidence());
				ovirsInsert.setDouble(6, ovirsSpectrumRenderer.getMinEmission());
				ovirsInsert.setDouble(7, ovirsSpectrumRenderer.getMaxEmission());
				ovirsInsert.setDouble(8, ovirsSpectrumRenderer.getMinPhase());
				ovirsInsert.setDouble(9, ovirsSpectrumRenderer.getMaxPhase());
				ovirsInsert.setDouble(10, ovirsSpectrumRenderer.getMinRange());
				ovirsInsert.setDouble(11, ovirsSpectrumRenderer.getMinRange());
				ovirsInsert.setString(12, filenamePlusParent);
//				logger.log(Level.INFO, "insert statement for spectra populated");
				ovirsInsert.executeUpdate();
//				logger.log(Level.INFO, "insert statement updated completed");
				ResultSet rs = ovirsInsert.getGeneratedKeys();
				rs.next();
				populateOVIRSCubeTableForFile(modelName, dataType, ovirsSpectrumRenderer, rs.getInt(1));
			}
			else
				populateOVIRSCubeTableForFile(modelName, dataType, ovirsSpectrumRenderer, count);

			count++;
		}
	}

	private static void populateOVIRSCubeTableForFile(String modelName, String dataType,
			IBasicSpectrumRenderer<OVIRSSpectrum> ovirsSpectrumRenderer, int spectrumIndex)
			throws SQLException, IOException
	{
		logger.log(Level.INFO, "Populating cube table for " + spectrumIndex);

		String tableName = modelName + "_" + OVIRSCubesTable + "_" + dataType;

		if (footprintPolyData == null)
			footprintPolyData = new vtkPolyData();
		footprintPolyData.DeepCopy(ovirsSpectrumRenderer.getShiftedFootprint());
		footprintPolyData.ComputeBounds();
//		logger.log(Level.INFO, "Footprint bounds calculated");

		TreeSet<Integer> cubeIds = bodyModel.getIntersectingCubes(footprintPolyData);
//		logger.log(Level.INFO, "Intersected cubes calculated");

		// System.out.println("cubeIds: " + cubeIds);
		// System.out.println("number of cubes: " + cubeIds.size());
		// System.out.println("id: " + count);
		// System.out.println("number of cells in polydata " +
		// footprintPolyData.GetNumberOfCells());

		if (writeToDB == true)
		{
			if (ovirsInsert2 == null)
			{
				// index autoincrements, so start with the ovirsspectrum id column
				ovirsInsert2 = db
						.preparedStatement("insert into " + tableName + " (ovirsspectrumid, cubeid) values (?, ?)");
			}

			for (Integer i : cubeIds)
			{
				ovirsInsert2.setInt(1, spectrumIndex);
				ovirsInsert2.setInt(2, i);
				ovirsInsert2.executeUpdate();
			}
			logger.log(Level.INFO, "All cube rows inserted");

		}
		ovirsSpectrumRenderer.Delete();
		vtkObject.JAVA_OBJECT_MANAGER.gc(true);
//		logger.log(Level.INFO, "Done inserting cube rows");
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
		logger.setLevel(Level.OFF);
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

		// authentication
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

		String ovirsFileList = rootURL + File.separator + "data/bennu/shared/ovirs/" + dataType + "/spectrumlist.txt";

		List<String> ovirsFiles = null;
		List<String> updatedFilenames = new ArrayList<String>();
		try
		{
			if (localRun == true)
			{
				File ovirsFileFromServer = FileCache.getFileFromServer(ovirsFileList);
				ovirsFiles = FileUtil.getFileLinesAsStringList(ovirsFileFromServer.getAbsolutePath());
			}
			else
			{
				ovirsFiles = FileUtil.getFileLinesAsStringList(ovirsFileList.substring(5));
			}

			if (endIndex > ovirsFiles.size())
				endIndex = ovirsFiles.size();
			for (int j = startIndex; j < endIndex; j++)
			{
				String ovirsFile = ovirsFiles.get(j);
				String actualName = (rootURL + File.separator + "data/bennu/shared/ovirs/" + dataType + "/spectra/"
						+ ovirsFile.split(" ")[0]);
				if (localRun == true) actualName = ("bennu/shared/ovirs/" + dataType + "/spectra/" + ovirsFile.split(" ")[0]);
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

			createOVIRSTables(modelName, dataType, appendTables);
			createOVIRSTablesCubes(modelName, dataType, appendTables);
		}
		logger.log(Level.INFO, "Database tables created.  ");
		try
		{
			populateOVIRSTables(modelName, dataType, updatedFilenames);
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
