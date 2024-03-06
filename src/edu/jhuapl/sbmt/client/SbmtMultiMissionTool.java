package edu.jhuapl.sbmt.client;

import java.awt.EventQueue;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.looks.LookUtils;

import edu.jhuapl.saavtk.gui.TSConsole;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.DownloadableFileState;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.ServerSettingsManager;
import edu.jhuapl.saavtk.util.ServerSettingsManager.ServerSettings;
import edu.jhuapl.saavtk.util.UrlStatus;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.dem.legacy.DEMKey;
import edu.jhuapl.sbmt.image.config.BasemapImageConfig;
import edu.jhuapl.sbmt.image.config.BasemapImageConfigIO;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfigIO;
import edu.jhuapl.sbmt.image.keys.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.image.keys.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.image.model.BasemapImage;
import edu.jhuapl.sbmt.image.model.BinExtents;
import edu.jhuapl.sbmt.image.model.BinSpacings;
import edu.jhuapl.sbmt.image.model.BinTranslations;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.OrientationFactory;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfigIO;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfigIO;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKey;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfigIO;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard.StandardStateHistory;

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
			    Taskbar.getTaskbar().setIconImage(erosIcon.getImage());
			}
		}


		// Initialize serialization proxies
		ImageBinPadding.initializeSerializationProxy();
		BinExtents.initializeSerializationProxy();
		BinTranslations.initializeSerializationProxy();
		BinSpacings.initializeSerializationProxy();


		// Structures.
		LatLon.initializeSerializationProxy();

		// Images.
		CylindricalBounds.initializeSerializationProxy();
		PerspectiveImageMetadata.initializeSerializationProxy();
		CustomCylindricalImageKey.initializeSerializationProxy();
		CustomPerspectiveImageKey.initializeSerializationProxy();
		CompositePerspectiveImage.initializeSerializationProxy();
		CustomSpectrumKey.initializeSerializationProxy();
		DEMKey.initializeSerializationProxy();
		SpectrumInstrumentMetadataIO.initializeSerializationProxy();
		SpectrumInstrumentMetadata.initializeSerializationProxy();
		SpectrumSearchSpec.initializeSerializationProxy();
		OTES.initializeSerializationProxy();
		OVIRS.initializeSerializationProxy();
		NIS.initializeSerializationProxy();
		NIRS3.initializeSerializationProxy();
		MEGANE.initializeSerializationProxy();
		StandardStateHistory.initializeSerializationProxy();
		SpiceStateHistory.initializeSerializationProxy();
		StateHistoryKey.initializeSerializationProxy();
		SpiceInfo.initializeSerializationProxy();
		OrientationFactory.initializeSerializationProxy();
		BasemapImage.initializeSerializationProxy();
		ImageDataQuery.initializeSerializationProxy();
		ImagingInstrument.initializeSerializationProxy();
		DataQuerySourcesMetadata.initializeSerializationProxy();
		FixedListDataQuery.initializeSerializationProxy();

		FeatureConfigIOFactory.registerFeatureConfigIO(BasemapImageConfig.class.getSimpleName(), new BasemapImageConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(ImagingInstrumentConfig.class.getSimpleName(), new ImagingInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(LidarInstrumentConfig.class.getSimpleName(), new LidarInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(SpectrumInstrumentConfig.class.getSimpleName(), new SpectrumInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(StateHistoryConfig.class.getSimpleName(), new StateHistoryConfigIO());
	}

	public static void setEnableAuthentication(boolean enableAuthentication)
	{
		SbmtMultiMissionTool.enableAuthentication = enableAuthentication;
	}

	public static Mission getMission()
	{
		return Mission.getMission();
	}

    public static Mission configureMission(String rootUrl)
	{
    	return Mission.configureMission();
	}

	public static void shutDown()
	{
		boolean showConsole = TSConsole.isConfigured();
		if (showConsole)
		{
			System.err.println("Close this console window to exit.");
			TSConsole.showStandaloneConsole();
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
                String splashFileName = Mission.getDisplaySplashName(mission);
                splash = new SbmtSplash("resources", splashFileName);
                splash.validate();
                splash.setVisible(true);
                if (TSConsole.isEnabled())
                {
                    TSConsole.showStandaloneConsole();
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
		displaySplash(Mission.getMission());

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
//		SmallBodyViewConfig.betaMode = getOption(args, "--beta") != null;
        if (getOption(args, "--debug") != null)
        {
            Debug.setEnabled(true);
        }

//        if (getOption(args, "--info") == null)
//        {
//            FileCacheMessageUtil.enableInfoMessages(false);
//        }

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
			TSConsole.configure(true, "Message Console", outputStream, outputStream);
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

			Mission.configureMission();

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
