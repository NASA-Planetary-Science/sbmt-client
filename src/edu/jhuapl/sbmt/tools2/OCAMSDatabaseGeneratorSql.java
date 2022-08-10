package edu.jhuapl.sbmt.tools2;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import vtk.vtkObject;

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
import edu.jhuapl.sbmt.image2.pipeline.io.FilenameToRenderableImagePipeline;
import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
//import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;
//import edu.jhuapl.sbmt.image.model.keys.ImageKey;
import edu.jhuapl.sbmt.tools.DBRunInfo;
import edu.jhuapl.sbmt.tools.SqlManager;

public class OCAMSDatabaseGeneratorSql
{
    private SqlManager db = null;
    private SmallBodyModel smallBodyModel;
    private SmallBodyViewConfig smallBodyConfig;
    private String databasePrefix;
    private String databaseSuffix = "";
    private boolean appendTables;
    private boolean modifyMain;
    private Instrument instrument;

	public OCAMSDatabaseGeneratorSql(SmallBodyViewConfig smallBodyConfig, String databasePrefix, boolean appendTables, boolean modifyMain, Instrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.databasePrefix = databasePrefix;
        this.appendTables = appendTables;
        this.modifyMain = modifyMain;
        this.instrument = instrument;
    }

	//Image tables
	private void createImageTable(String tableName) throws SQLException
	{
		// Check to see if we should append to existing or not
        if(!appendTables){
            // Not appending, drop existing (if applicable) table first
            db.dropTable(tableName);
        }

        // Then create a new table if one does not already exist
        db.update(
            "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
            "id int PRIMARY KEY, " +
            "filename char(128), " +
            "privacy char(128) " +
            ")"
        );
	}

	private void populateImageTables(
            List<String> lines,
            String imageNameTableName,
            Date publicCutoffDate) throws SQLException, Exception
    {
//        smallBodyModel.setModelResolution(0);
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();

        PreparedStatement insertStatement = db.preparedStatement(
                "insert into " + imageNameTableName + " values (?, ?, ?)");

        int count = 0;
        int primaryKey = 0;

        // If appending, search table for next consecutive key to use, if no entries exist then start at 0
        List<List<Object>> queryResult;
        if(appendTables){
            queryResult = db.query("SELECT MAX(id) FROM `" + imageNameTableName + "`");
            if(queryResult != null && !queryResult.isEmpty() &&
                    queryResult.get(0) != null && !queryResult.get(0).isEmpty() &&
                    queryResult.get(0).get(0) != null){
                primaryKey = (int)queryResult.get(0).get(0) + 1;
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
                queryResult = db.query("SELECT * FROM `" + imageNameTableName + "` WHERE `filename` = \"" +
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
//            ImageKeyInterface key = new ImageKey(keyName, ImageSource.SPICE, imager);
//
//            PerspectiveImage image = null;

//            Triple<List<List<String>>, ImagingInstrument, List<String>>[] tripleSink = new Triple[1];
//            List<List<String>> fileInputs = List.of(List.of(keyName, "", ImageSource.SPICE.toString()));
//            IPipelineOperator<Pair<List<List<String>>, ImagingInstrument>, Triple<List<List<String>>, ImagingInstrument, List<String>>> searchToPointingFilesOperator
//            		= new SearchResultsToPointingFilesOperator(config);
//            Just.of(Pair.of(fileInputs, imager))
//				.operate(searchToPointingFilesOperator)
//				.subscribe(TripleSink.of(tripleSink))
//				.run();
//            List<String> pointingFilenames = tripleSink[0].getRight();
//            RenderableImagePipeline pipeline = new RenderableImagePipeline(keyName, pointingFilenames.get(0), imager);
//        	List<RenderablePointedImage> images = pipeline.getOutput();

        	FilenameToRenderableImagePipeline pipeline = FilenameToRenderableImagePipeline.of(keyName, ImageSource.SPICE, config, imager);
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
//                    System.gc();
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

            DateTime startTime = new DateTime(images.get(0).getPointing().getStartTime(), DateTimeZone.UTC);

            String privacy = "public";
            if (startTime.toDate().after(publicCutoffDate)) privacy = "private";

            System.out.println("id: " + primaryKey);
            System.out.println("filename: " + new File(filename).getName());
            System.out.println("privacy: " + privacy);


            insertStatement.setInt(1, primaryKey);
            insertStatement.setString(2, new File(filename).getName());
            insertStatement.setString(3, privacy);


            System.out.println("statement: " + insertStatement.toString());

            insertStatement.executeUpdate();

            ++primaryKey;

//            image.Delete();
            System.gc();
            System.out.println("deleted " + vtkObject.JAVA_OBJECT_MANAGER.gc(true));
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

        return true;
    }

    String getImagesTableNames(String body)
    {
    	return body + "_images";
    }

    public void run(String body, String fileList, String publicTimeString) throws SQLException, IOException, Exception
    {
        smallBodyModel = SbmtModelFactory.createSmallBodyModel(smallBodyConfig);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date publicCutoffDate = dateFormatter.parse(publicTimeString);
        String imagesTable = getImagesTableNames(body);
        System.out.println("OCAMSDatabaseGeneratorSql: run: image table name " + imagesTable);

        if (!fileList.endsWith(".txt"))
        {
        	fileList = fileList + File.separator + "imagelist-fullpath-info.txt";
        }

        //Grab the list of the image filenames from the diffFileList, if it exists.
//        List<String> diffFiles = new ArrayList<String>();
//        if (diffFileList != null)
//        {
//        	diffFiles = FileUtil.getFileLinesAsStringList(diffFileList);
//        }


        List<String> lines = null;

        // if the file path starts with "/" then we know we are accessing files from the local file system
        if (fileList.startsWith("/"))
            lines = FileUtil.getFileLinesAsStringList(fileList);
        // otherwise, we try to load the file from the server via HTTP
        else
            lines = FileCache.getFileLinesFromServerAsStringList(fileList);

        //Reduce the lines to only include those that match in the diff list, if it exists
//        if (diffFileList != null)
//        {
//        	List<String> truncatedLines = new ArrayList<String>();
//        	for (String line : lines)
//        	{
//        		for (String diffFile : diffFiles)
//        		{
//        			if (line.endsWith(diffFile))
//        			{
//        				truncatedLines.add(line);
//        				break;
//        			}
//        		}
//        	}
//        	lines = truncatedLines;
//        }

        String dburl = "sd-mysql.jhuapl.edu";

        db = new SqlManager(dburl);
        System.out.println("Connected to database: " + dburl);


        createImageTable(imagesTable);
        populateImageTables(lines, imagesTable, publicCutoffDate);

        db.shutdown();

    }

    private static void usage()
    {
    	//--root-url $dbRootUrl --append-tables --body $body --author $author --instrument $instrumentName $optionalArgs
        String o = "This program generates the image list in the MySQL database for OREX.\n\n"
                + "Usage: DatabaseGeneratorSql [options] <imagesource> <shapemodel>\n\n"
                + "Where:\n"

                + "  <shapemodel>\n"
                + "          shape model to process. Must be one of the values in the RunInfo enumeration\n"
                + "          such as EROS or ITOKAWA. If ALL is specified then the entire database is\n"
                + "          regenerated.\n"
                + "Options:\n"
                + "  --append-tables\n"
                + "          If specified, will check to see if database tables of the shape+mode already\n"
                + "          exist, create one if necessary, and append entries to that table as opposed\n"
                + "          to deleting existing tables and creating a new one from scratch.\n"
                + "  --body\n"
                + "          The ShapeModelBody name to build the table for\n"
                + "  --instrument\n"
                + "          The instrument to process\n"
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
     * --root-url $dbRootUrl --append-tables --body RQ36 --model ALTWG-SPC-v20191027 --instrument MAPCAM
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
    public static void main(String[] args) throws IOException, Exception
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
        String publicTimeString = null;
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
            else if (args[i].equals("--publicTime"))
            {
            	publicTimeString = args[++i];
            }
            else if (args[i].equals("--instrument"))
            {
            	instrumentString = args[++i].toUpperCase();
            }
//            else if (args[i].equals("--diffList"))
//            {
//            	diffFileList = args[++i];
//            }
            else {
                // We've encountered something that is not an option, must be at the args
                break;
            }
        }

        // There must be numRequiredArgs arguments remaining after the options.
        // Otherwise abort.
        int numberRequiredArgs = 0;
        if (args.length - i < numberRequiredArgs)
            usage();

        // Important: set the mission before changing things in the Configuration. Otherwise,
        // setting the mission will undo those changes.
        SbmtMultiMissionTool.configureMission();

        // basic default configuration, most of these will be overwritten by the configureMission() method
        Configuration.setAPLVersion(aplVersion);
        Configuration.setRootURL(rootURL);

        // authentication
        Configuration.authenticate();

        // initialize view config
        SmallBodyViewConfig.fromServer = true;

        SmallBodyViewConfig.initialize();

        // VTK
        System.setProperty("java.awt.headless", "true");
        NativeLibraryLoader.loadHeadlessVtkLibraries();

//        ImageSource mode = ImageSource.valueOf(args[i++].toUpperCase());
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
        	if (!ri.name.equals(ShapeModelBody.valueOf(bodyName).toString()) || (ri.imageSource != ImageSource.SPICE) || (!ri.instrument.toString().equals(instrumentString))) continue;
            System.out.println("DatabaseGeneratorSql: main: writing to " + ri.databasePrefix + " for " + ri.instrument + " with " + ri.imageSource + " remote " + ri.remotePathToFileList);
        	OCAMSDatabaseGeneratorSql generator = new OCAMSDatabaseGeneratorSql(config, ri.databasePrefix, appendTables, modifyMain, ri.instrument);

            String pathToFileList = ri.pathToFileList;
            if (remote)
            {
                if (ri.remotePathToFileList != null)
                    pathToFileList = ri.remotePathToFileList;
            }

            System.out.println("Generating: " + pathToFileList + " and public time string " + publicTimeString);
            generator.run("bennu_" + ri.instrument.toString().toLowerCase(), pathToFileList, publicTimeString);
        }
    }

}
