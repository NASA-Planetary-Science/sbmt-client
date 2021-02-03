package edu.jhuapl.sbmt.client;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.LookUtils;

import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.gui.Console;
import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.Polygon;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Configuration.ReleaseType;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.ServerSettingsManager;
import edu.jhuapl.saavtk.util.ServerSettingsManager.ServerSettings;
import edu.jhuapl.saavtk.util.UrlStatus;
import edu.jhuapl.sbmt.dtm.model.DEMKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.gui.image.model.custom.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.image.ImageFactory;
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.tools.SbmtRunnable;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and other initialization. The main
 * function may take one optional argument. If there are no arguments specified,
 * then the tool starts up as usual showing Eros by default. If one argument is
 * specified, it is assumed to be a path to a temporary shape model which is
 * then loaded as a custom view though it is not retained the next time the tool
 * starts.
 */
public class SbmtMultiMissionTool
{
    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    public enum Mission
	{
    	APL_INTERNAL_NIGHTLY("b1bc7ec", false),
		APL_INTERNAL("b1bc7ed", false),
		PUBLIC_RELEASE("3ee38f0", true),
		TEST_APL_INTERNAL("fb404a7", false),
		TEST_PUBLIC_RELEASE("a1a32b4", true),
		HAYABUSA2_DEV("133314b", false),
//		HAYABUSA2_STAGE("244425c", false),
		HAYABUSA2_DEPLOY("355536d", false),
		OSIRIS_REX("7cd84586", false),
//		OSIRIS_REX_STAGE("7cd84587", false),
        OSIRIS_REX_DEPLOY("7cd84588", false),
        OSIRIS_REX_TEST("h887aa63", false),
		OSIRIS_REX_MIRROR_DEPLOY("7cd84589", false),
		NH_DEPLOY("8ff86312", false),
		DART_DEV("9da75292", false),
		DART_DEPLOY("9da75293", false),
        DART_TEST("8f449edc", false),
        DART_STAGE("afac11cb", false),
		STAGE_APL_INTERNAL("f7e441b", false),
		STAGE_PUBLIC_RELEASE("8cc8e12", true),
		MEGANE_DEV("9da85292", false),
		MEGANE_DEPLOY("9da85293", false),
        MEGANE_TEST("8f549edc", false),
        MEGANE_STAGE("afad11cb", false),;

		private final String hashedName;
        private final boolean publishedDataOnly;

		Mission(String hashedName, boolean publishedDataOnly)
		{
			this.hashedName = hashedName;
            this.publishedDataOnly = publishedDataOnly;
		}

		public String getHashedName()
		{
			return hashedName;
		}

		public boolean isPublishedDataOnly()
		{
		    return publishedDataOnly;
		}

		public static Mission getMissionForName(String name)
		{
		    for (Mission msn : values())
		    {
		        if (name.equals(msn.hashedName))
		            return msn;
		    }
		    return null;
		}
	}

	private static final String OUTPUT_FILE_NAME = "sbmtLogFile.txt";
	private static final PrintStream SAVED_OUT = System.out;
	private static final PrintStream SAVED_ERR = System.err;
	private static PrintStream outputStream = null;

	// DO NOT change anything about this without also confirming the script set-released-mission.sh still works correctly!
	// This field is used during the build process to "hard-wire" a release to point to a specific server.
//	private static final Mission RELEASED_MISSION = Mission.APL_INTERNAL;	//for generating the allBodies.json metadata file, for example
	private static final Mission RELEASED_MISSION = null;	//normal ops

	private static Mission mission = RELEASED_MISSION;
	private static boolean missionConfigured = false;
    private static volatile JDialog offlinePopup = null;

	private static boolean enableAuthentication;

	public static Date compileDate = null;
	public static String versionString = "\n";

	static
	{
		if (Configuration.isMac())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			ImageIcon erosIcon = new ImageIcon(SbmtMultiMissionTool.class.getResource("/edu/jhuapl/sbmt/data/erosMacDock.png"));
			if (!Configuration.isHeadless())
			{
			    OSXAdapter.setDockIconImage(erosIcon.getImage());
			}
		}


		// Initialize serialization proxies

		// Structures.
		LatLon.initializeSerializationProxy();
		EllipsePolygon.initializeSerializationProxy();
		Polygon.initializeSerializationProxy();
		Line.initializeSerializationProxy();

		// Images.
		CustomCylindricalImageKey.initializeSerializationProxy();
		CustomPerspectiveImageKey.initializeSerializationProxy();
//		SpectrumKey.initializeSerializationProxy();
		CustomSpectrumKey.initializeSerializationProxy();
		DEMKey.initializeSerializationProxy();
//		BasicSpectrumInstrument.initializeSerializationProxy();
//		OTESSpectrum.initializeSerializationProxy();
		SpectrumInstrumentMetadataIO.initializeSerializationProxy();
		SpectrumInstrumentMetadata.initializeSerializationProxy();
		SpectrumSearchSpec.initializeSerializationProxy();
		OTES.initializeSerializationProxy();
		OVIRS.initializeSerializationProxy();
		NIS.initializeSerializationProxy();
		NIRS3.initializeSerializationProxy();
		MEGANE.initializeSerializationProxy();
		ImageFactory.initializeSerializationProxy();
		StandardStateHistory.initializeSerializationProxy();
		SpiceStateHistory.initializeSerializationProxy();
		StateHistoryKey.initializeSerializationProxy();
		SpiceInfo.initializeSerializationProxy();
	}

	public static void setEnableAuthentication(boolean enableAuthentication)
	{
		SbmtMultiMissionTool.enableAuthentication = enableAuthentication;
	}

	public static Mission getMission()
	{
		if (mission == null)
		{
			// Note that System.getProperty is inconsistent with regard to whether it includes quote marks.
			// To be sure the mission identifier is processed consistently, exclude all non-word characters.
			String missionIdentifier = System.getProperty("edu.jhuapl.sbmt.mission").replaceAll("\\W+", "");
			if (missionIdentifier == null)
			{
				throw new IllegalArgumentException("Mission was not specified at build time or run time");
			}
			try
			{
				// First see if provided mission identifier matches the enumeration
				// name.
				mission = Mission.valueOf(missionIdentifier);
			}
			catch (IllegalArgumentException e)
			{
				// No mission identifier with that natural enumeration name,
				// so see if instead this is a hashed mission identifier.
				for (Mission each : Mission.values())
				{
					if (each.getHashedName().equalsIgnoreCase(missionIdentifier))
					{
						mission = each;
						break;
					}
				}
				if (mission == null)
				{
					throw new IllegalArgumentException("Invalid mission identifier specified at run time: " + missionIdentifier, e);
				}
			}
		}

		return mission;
	}

	private static void generateCompileDateAndVersionString(Object sbmtClass) {


		if (compileDate != null && !versionString.equals("\n")) return;
        try
        {
        	Class<?> classToLoad = sbmtClass.getClass();
        	ClassLoader classLoader = classToLoad.getClassLoader();
        	String classNameToLoad = sbmtClass.getClass().getCanonicalName().replace('.', '/').substring(0, sbmtClass.getClass().getCanonicalName().length()-8);
        	URL classURL = classLoader.getResource(classNameToLoad + ".class");
        	File file = new File(classURL.toURI());
            compileDate = new Date(file.lastModified());
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            try {
                String rn = sbmtClass.getClass().getName().replace('.', '/') + ".class";
                JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
                long time =  j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
                compileDate = new Date(time);
            } catch (@SuppressWarnings("unused") Exception e1) {
            }
        }

        try
        {
            InputStream is = sbmtClass.getClass().getResourceAsStream("/edu/jhuapl/sbmt/data/release.version");
            byte[] data = new byte[256];
            is.read(data, 0, data.length);
            versionString = new String(data).trim();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
	    	System.out.println("exception = " + e.toString());
        }
	}

    public static Mission configureMission()
    {
        if (missionConfigured)
        {
            return mission;
        }

        Mission mission = getMission();

        String rootUrl = "http://sbmt.jhuapl.edu/sbmt/prod";

        switch (mission)
        {
        case DART_DEPLOY:
            // TODO: set rootUrl when this is determined.
            break;
        case STAGE_APL_INTERNAL:
        case STAGE_PUBLIC_RELEASE:
        case DART_STAGE:
            rootUrl = "http://sbmt-web.jhuapl.edu/internal/multi-mission/stage";
            break;
        case TEST_APL_INTERNAL:
        case TEST_PUBLIC_RELEASE:
        case DART_TEST:
        case OSIRIS_REX_TEST:
            rootUrl = "http://sbmt-web.jhuapl.edu/internal/multi-mission/test";
            break;
        case HAYABUSA2_DEPLOY:
            rootUrl = "http://hyb2sbmt.u-aizu.ac.jp/sbmt";
            break;
        case OSIRIS_REX_DEPLOY:
            rootUrl = "https://uasbmt.lpl.arizona.edu/sbmt";
            break;
        default:
            break;
        }

        return configureMission(rootUrl);
    }

    public static Mission configureMission(String rootUrl)
	{
		if (missionConfigured)
		{
			return mission;
		}

		Mission mission = getMission();
        generateCompileDateAndVersionString(mission);
	    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

	    String compileDateString = DATE_FORMAT.format(compileDate);
	    String fullTitle = "SBMT " + versionString + " (" + compileDateString + ")";
	    String abbrTitle = "SBMT " + "(" + compileDateString + ")";
	    fullTitle = "SBMT";
	    abbrTitle = "SBMT";

        Configuration.setRootURL(rootUrl);

        // DO NOT SET ROOT URL IN THE SWITCH BELOW ANYMORE!!!
		switch (mission)
		{
		case APL_INTERNAL_NIGHTLY:
			Configuration.setAppName("sbmt-internal-nightly");
			Configuration.setCacheVersion("2");
			Configuration.setAppTitle(abbrTitle);
			Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
			break;
		case APL_INTERNAL:
            Configuration.setAppName("sbmt-apl");
            Configuration.setCacheVersion("2");
            if (!versionString.contentEquals(""))
                Configuration.setAppTitle(fullTitle);
            else
                Configuration.setAppTitle(abbrTitle);
            break;
		case PUBLIC_RELEASE:
			Configuration.setAppName("sbmt");
			Configuration.setCacheVersion("2");
			if (!versionString.contentEquals(""))
				Configuration.setAppTitle(fullTitle);
			else
				Configuration.setAppTitle(abbrTitle);
			break;
		case STAGE_APL_INTERNAL:
            Configuration.setAppName("sbmt-stage-apl");
            Configuration.setCacheVersion("2");
            Configuration.setAppTitle(fullTitle);
            break;
		case STAGE_PUBLIC_RELEASE:
			Configuration.setAppName("sbmt-stage");
			Configuration.setCacheVersion("2");
			Configuration.setAppTitle(fullTitle);
			break;
		case TEST_APL_INTERNAL:
            Configuration.setAppName("sbmt-test-apl");
            Configuration.setCacheVersion("2");
            Configuration.setAppTitle(fullTitle);
            // Configuration.setDatabaseSuffix("_test");
            break;
		case TEST_PUBLIC_RELEASE:
			Configuration.setAppName("sbmt-test");
			Configuration.setCacheVersion("2");
			Configuration.setAppTitle(fullTitle);
            // Configuration.setDatabaseSuffix("_test");
			break;
		case HAYABUSA2_DEV:
			Configuration.setAppName("sbmthyb2-dev");
			Configuration.setCacheVersion("");
			Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
			Configuration.setAppTitle("SBMT/Hayabusa2-Dev"/* + "(" + compileDateString + ")"*/);
            // Configuration.setDatabaseSuffix("_test");
			break;
//		case HAYABUSA2_STAGE:
//			Configuration.setAppName("sbmthyb2-stage");
//			Configuration.setCacheVersion("");
//			Configuration.setAppTitle("SBMT/Hayabusa2-Stage");
//			break;
		case HAYABUSA2_DEPLOY:
			Configuration.setAppName("sbmthyb2");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/Hayabusa2" /*+ versionString + " (" + compileDateString + ")"*/);
			break;
        case OSIRIS_REX:
            Configuration.setAppName("sbmt1orex-dev");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/OSIRIS REx-Dev" /*+ "(" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
        case OSIRIS_REX_TEST:
            Configuration.setAppName("sbmt1orex-test");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/OSIRIS REx-Test" /*+ versionString + " (" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
//		case OSIRIS_REX_STAGE:
//			Configuration.setAppName("sbmt1orex-stage");
//			Configuration.setCacheVersion("");
//			Configuration.setAppTitle("SBMT/OSIRIS REx-Stage");
//			break;
		case OSIRIS_REX_MIRROR_DEPLOY:
			Configuration.setAppName("sbmt1orex-mirror");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/OSIRIS REx APL Mirror" /*+ versionString + " (" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
		case OSIRIS_REX_DEPLOY:
			Configuration.setAppName("sbmt1orex");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/OSIRIS REx"/* + versionString + " (" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
		case NH_DEPLOY:
			Configuration.setAppName("sbmtnh");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/New Horizons" /*+ versionString + " (" + compileDateString + ")"*/);
			break;
		case DART_DEV:
			Configuration.setAppName("sbmt1dart-dev");
			Configuration.setCacheVersion("");
			Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
			Configuration.setAppTitle("SBMT/DART (Development Version)" /*+ "(" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
		case DART_DEPLOY:
			Configuration.setAppName("sbmt1dart");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/DART" /*+ versionString + " (" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
        case DART_STAGE:
            Configuration.setAppName("sbmt1dart-stage");
            Configuration.setCacheVersion("");
            Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
            Configuration.setAppTitle("SBMT/DART (Stage Version)" /*+ "(" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
        case DART_TEST:
            Configuration.setAppName("sbmt1dart-test");
            Configuration.setCacheVersion("");
            Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
            Configuration.setAppTitle("SBMT/DART (Test Version)" /*+ "(" + compileDateString + ")"*/);
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
        case MEGANE_DEV:
			Configuration.setAppName("sbmt1megane-dev");
			Configuration.setCacheVersion("");
			Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
			Configuration.setAppTitle("SBMT/MEGANE (Development Version)");
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
		case MEGANE_DEPLOY:
			Configuration.setAppName("sbmt1megane");
			Configuration.setCacheVersion("");
			Configuration.setAppTitle("SBMT/MEGANE");
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
			break;
        case MEGANE_STAGE:
            Configuration.setAppName("sbmt1megane-stage");
            Configuration.setCacheVersion("");
            Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
            Configuration.setAppTitle("SBMT/MEGANE (Stage Version)");
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
        case MEGANE_TEST:
            Configuration.setAppName("sbmt1megane-test");
            Configuration.setCacheVersion("");
            Configuration.setReleaseType(ReleaseType.DEVELOPMENT);
            Configuration.setAppTitle("SBMT/MEGANE (Test Version)" );
            Colormaps.setDefaultColormapName("Spectral_lowBlue");
            break;
		default:
            throw new AssertionError("Unhandled case for setting up launch configuration " + mission);
		}

		missionConfigured = true;

		return mission;
	}

	public static void shutDown()
	{
		boolean showConsole = Console.isConfigured();
		if (showConsole)
		{
			System.err.println("Close this console window to exit.");
			Console.showStandaloneConsole();
		}

		restoreStreams();

		if (!showConsole)
		{
			System.exit(1);
		}
	}

	protected static void setupLookAndFeel()
	{
		if (!Configuration.isMac())
		{
			try
			{
				UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
				//                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
				// uncomment for cross-platform LAF
				//            else
				//                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

			}
			catch (Exception e)
			{
				e.printStackTrace();
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1)
				{
					throw new RuntimeException(e1);
				}
			}
		}
	}

	protected static void displaySplash(Mission mission)
	{
	    if (Configuration.isHeadless())
	    {
	        return;
	    }

	    try
        {
            Configuration.runAndWaitOnEDT(() -> {

                SbmtSplash splash = null;
                switch (mission)
                {
                case APL_INTERNAL_NIGHTLY:
                case APL_INTERNAL:
                case PUBLIC_RELEASE:
                case STAGE_APL_INTERNAL:
                case STAGE_PUBLIC_RELEASE:
                case TEST_APL_INTERNAL:
                case TEST_PUBLIC_RELEASE:
                case NH_DEPLOY:
                case DART_DEV:
                case DART_DEPLOY:
                case DART_STAGE:
                case DART_TEST:
                case MEGANE_DEV:
                case MEGANE_DEPLOY:
                case MEGANE_STAGE:
                case MEGANE_TEST:
                    splash = new SbmtSplash("resources", "splashLogo.png");
                    break;
                case HAYABUSA2_DEV:
                    splash = new SbmtSplash("resources", "splashLogoHb2Dev.png");
                    break;
//                case HAYABUSA2_STAGE:
//                    splash = new SbmtSplash("resources", "splashLogoHb2Stage.png");
//                    break;
                case HAYABUSA2_DEPLOY:
                    splash = new SbmtSplash("resources", "splashLogoHb2.png");
                    break;
                case OSIRIS_REX:
                case OSIRIS_REX_TEST:
                case OSIRIS_REX_DEPLOY:
                case OSIRIS_REX_MIRROR_DEPLOY:
//                case OSIRIS_REX_STAGE:
                    splash = new SbmtSplash("resources", "splashLogoOrex.png");
                    break;
                default:
                    throw new AssertionError("Unhandled splash screen case for launch configuration " + mission);
                }

                splash.validate();
                splash.setVisible(true);
                if (Console.isEnabled())
                {
                    Console.showStandaloneConsole();
                }
                splash.toFront();

                if (offlinePopup != null)
                {
                    offlinePopup.toFront();
                }

                final SbmtSplash finalSplash = splash;

                SwingWorker<Void, Void> timeOut = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        // Kill the splash screen after a suitable pause.
                        try
                        {
                            Thread.sleep(4000);
                        }
                        catch (InterruptedException e)
                        {
                            // Ignore this one.
                        }
                        finally
                        {
                            EventQueue.invokeLater(() -> {
                                finalSplash.setVisible(false);
                            });
                        }

                        return null;
                    }

                };
                timeOut.execute();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

	protected static String getOption(String[] args, String option)
	{
		for (String arg : args)
		{
			arg = arg.toLowerCase();
			option = option.toLowerCase();
			if (arg.startsWith(option + "="))
			{
				return arg.substring(option.length() + 1);
			}
			else if (arg.startsWith(option))
			{
				return arg.substring(option.length());
			}
		}
		return null;
	}

	protected static void restoreStreams()
	{
		if (outputStream != null)
		{
			System.setErr(SAVED_ERR);
			System.setOut(SAVED_OUT);
			outputStream.close();
			outputStream = null;
		}
	}

	private boolean clearCache;
	private boolean redirectStreams;
	private String initialShapeModelPath;

	protected SbmtMultiMissionTool()
	{
		this.clearCache = false;
		this.redirectStreams = true;
		this.initialShapeModelPath = null;
	}

	public void run(String[] args) throws IOException, InterruptedException, InvocationTargetException
	{
		processArguments(args);

		setUpStreams();

		// Display splash screen.
		displaySplash(mission);

		setUpAuthentication();

		clearCache();

		// Start up the client.
		new SbmtRunnable(initialShapeModelPath).run();

	}

	protected void processArguments(String[] args)
	{
		// Get options.
		redirectStreams = getOption(args, "--no-stream-redirect") == null;
		clearCache = getOption(args, "--auto-clear-cache") != null;
		SmallBodyViewConfig.betaMode = getOption(args, "--beta") != null;
        if (getOption(args, "--debug") != null)
        {
            Debug.setEnabled(true);
        }

        // Use --debug-cache to control both the debug and informational messages.
        boolean debugCache = getOption(args, "--debug-cache") != null;
        FileCache.enableDebug(debugCache);

		// Get other arguments.
		initialShapeModelPath = null;
		for (String arg : args)
		{
			if (!arg.startsWith("-"))
			{
				// First non-option is an optional shape model path.
				initialShapeModelPath = arg;
				// No other non-option arguments.
				break;
			}
		}
	}

	protected void setUpStreams() throws IOException, InvocationTargetException, InterruptedException
	{
		if (outputStream != null)
		{
			throw new IllegalStateException("Cannot call setUpStreams more than once");
		}

		if (redirectStreams)
		{
			Path outputFilePath = SAFE_URL_PATHS.get(Configuration.getApplicationDataDir(), OUTPUT_FILE_NAME);
			outputStream = new PrintStream(Files.newOutputStream(outputFilePath));
			System.setOut(outputStream);
			System.setErr(outputStream);
			Console.configure(true, outputStream);
		}
	}

	protected void clearCache()
	{
		if (clearCache)
		{
			Configuration.clearCache();
		}
	}

    protected void setUpAuthentication()
    {
        if (enableAuthentication)
        {
            ServerSettings serverSettings = ServerSettingsManager.instance().get();

            if (serverSettings.isServerAccessible())
            {
                Configuration.getSwingAuthorizor().setUpAuthorization();
            }
            else
            {
                FileCache.setOfflineMode(true, Configuration.getCacheDir());
                String message = "Unable to connect to server " + Configuration.getDataRootURL() + ". Starting in offline mode. See console log for more information.";
                if (Configuration.isHeadless())
                {
                    System.err.println(message);
                }
                else
                {
                    Configuration.runOnEDT(() -> {
                        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
                        offlinePopup = pane.createDialog("No internet access");
                        offlinePopup.setVisible(true);
                        offlinePopup.dispose();
                    });
                }
            }
            FileCache.instance().queryAllInBackground(true);

            FileCache.addServerUrlPropertyChangeListener(e -> {
                if (e.getPropertyName().equals(DownloadableFileState.STATE_PROPERTY))
                {
                    DownloadableFileState rootState = (DownloadableFileState) e.getNewValue();
                    if (rootState.getUrlState().getStatus() == UrlStatus.NOT_AUTHORIZED)
                    {
                        if (Configuration.getSwingAuthorizor().setUpAuthorization())
                        {
                            FileCache.instance().queryAllInBackground(true);
                        }
                    }
                }
            });
        }
    }

	public static void main(String[] args)
	{
		SbmtMultiMissionTool tool = null;
		try
		{
			// Global (static) initializations.
			setupLookAndFeel();

			// The following line appears to be needed on some systems to prevent server redirect errors.
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

			configureMission();

			tool = new SbmtMultiMissionTool();
			tool.run(args);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
			System.err.println("\nFatal error during launch. Please review the information above.");
			shutDown();
		}
	}

}
