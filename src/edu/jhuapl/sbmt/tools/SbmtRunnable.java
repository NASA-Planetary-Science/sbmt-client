package edu.jhuapl.sbmt.tools;

import java.awt.HeadlessException;
import java.io.File;
import java.net.JarURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.Console;
import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client.SbmtMainWindow;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool.Mission;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

public class SbmtRunnable implements Runnable
{
	private final String initialShapeModelPath;

	public SbmtRunnable(String initialShapeModelPath)
	{
		this.initialShapeModelPath = initialShapeModelPath;
	}

	@Override
	public void run()
	{
		try
		{
			Mission mission = SbmtMultiMissionTool.getMission();
			writeStartupMessage(mission);

			NativeLibraryLoader.loadAllVtkLibraries();

			SmallBodyViewConfig.initialize();
			//            new SmallBodyViewConfigMetadataIO(SmallBodyViewConfig.getBuiltInConfigs()).write(new File("/Users/steelrj1/Desktop/test.json"), "Test");

			configureMissionBodies(mission);

			vtkJavaGarbageCollector garbageCollector = new vtkJavaGarbageCollector();
			//garbageCollector.SetDebug(true);
			garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
			garbageCollector.SetAutoGarbageCollection(true);

			Configuration.runAndWaitOnEDT(() -> {
			    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
			    ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

			    MainWindow frame = new SbmtMainWindow(initialShapeModelPath);
			    MainWindow.setMainWindow(frame);

			    FileCache.instance().startAccessMonitor();

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception
                    {
                        while (!frame.isReady())
                        {
                            try
                            {
                                Thread.sleep(100);
                            }
                            catch (InterruptedException ignored)
                            {
                                break;
                            }
                        }

                        return null;
                    }

                    protected void done()
                    {
                        if (!isCancelled())
                        {
                            frame.pack();
                            frame.setVisible(true);
                            System.out.println("\nSBMT Ready");

                            Console.hideConsole();
                            Console.setDefaultLocation(frame);
                        }
                    }
                };

			    swingWorker.execute();
			});
		}
		catch (HeadlessException e)
		{
		    e.printStackTrace();
		    System.err.println("\nThe SBMT requires a fully functional graphics environment and cannot be run \"headless\"");
		    System.err.println("Unable to launch the SBMT.");
		}
		catch (Throwable throwable)
		{
			// Something went tragically wrong before the tool was displayed, so report the error and exit somewhat gracefully.
			throwable.printStackTrace();
			System.err.println("\nThe SBMT had a serious error during launch. Please review messages above for more information.");
			System.err.println("\nTry restarting the tool. Please report persistent launch problems to sbmt@jhuapl.edu.");
			System.err.println("\nNote that the SBMT requires an internet connection to download standard model data from the server.");
			try
            {
                Configuration.runAndWaitOnEDT(() -> {
                JOptionPane.showMessageDialog(null,
                        "A problem occurred during start-up. Please review messages in the console window.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                });
            }
            catch (Exception ignored)
            {
            }
		}
	}

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

	protected void writeStartupMessage(Mission mission)
	{
		Date compileDate = null;
		try
		{
			compileDate = new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified());
		}
		catch (@SuppressWarnings("unused") Exception e)
		{
			try
			{
				String rn = getClass().getName().replace('.', '/') + ".class";
				JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
				long time =  j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
				compileDate = new Date(time);
			}
			catch (@SuppressWarnings("unused") Exception e1)
			{}
		}

		System.out.println("Welcome to the Small Body Mapping Tool (SBMT)");
		System.out.println(mission + " edition" + (compileDate != null ? " built " + DATE_FORMAT.format(compileDate) : ""));
        Debug.of().out().println("Tool started in debug mode; diagnostic output is enabled.");
        if (FileCache.isEnableDebug())
        {
            System.out.println("Tool started in file cache debug mode; diagnostic output related to file caching/accessibility is enabled.");
        }
        System.out.println("\nUsing server at " + Configuration.getDataRootURL());
		if (!FileCache.instance().isServerAccessEnabled())
		{
			System.out.println("\nTool started in offline mode; skipping password authentication.");
			System.out.println("Only cached models and data will be available.");
		}
		else
		{
			if (Configuration.getAuthorizor().isValidCredentialsLoaded())
			{
				System.out.println("\nValid user name and password entered. Access may be granted to some restricted models.");
			}
			else
			{
				System.out.println("\nNo user name and password entered. Some models may not be available.");
				System.out.println("You may update your user name and pasword on the Body -> Update Password menu.");
			}
		}
		System.out.println("\nStoring application data in " + Configuration.getApplicationDataDir());

		if (Console.isConfigured())
		{
			System.out.println("\nThis is the SBMT console. You can show or hide it on the Console menu.");
			System.out.println("The console shows diagnostic information and other messages.");
			System.out.println("It will be hidden automatically after the SBMT launches.");
			System.out.println("\nPlease be patient while the SBMT starts up.");
		}
		else
		{
			System.out.println("\nStreams were not redirected. Diagnostic information will appear here.");
			System.out.println("The in-app console is disabled.");
		}
		System.out.println();
	}
	protected void configureMissionBodies(Mission mission)
	{
		disableAllBodies();
		enableMissionBodies(mission);
	}

	protected void disableAllBodies()
	{
		for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
		{
			each.enable(false);
		}
	}

	protected void enableMissionBodies(Mission mission)
	{
//		for (BasicConfigInfo info : SmallBodyViewConfig.getConfigIdentifiers().values())
//		{
//			for (SbmtMultiMissionTool.Mission presentMission : info.getPresentInVersion())
//			{
//				if (presentMission == mission)
//					info.enable(true);
//			}
//			for (SbmtMultiMissionTool.Mission defaultMission : info.getDefaultFor())
//			{
//				if (defaultMission == mission)
//					ViewConfig.setFirstTimeDefaultModelName(info.getUniqueName());
//			}
//		}

//		for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
//		{
//			if (each instanceof SmallBodyViewConfig)
//			{
//				SmallBodyViewConfig config = (SmallBodyViewConfig) each;
//				BasicConfigInfo info = new BasicConfigInfo(config);
//				for (SbmtMultiMissionTool.Mission presentMission : info.getPresentInVersion())
//				{
//					if (presentMission == mission)
//					{
//						System.out.println("SbmtRunnable: enableMissionBodies: enabled " + config.getUniqueName());
//						config.enable(true);
//						break;
//					}
//					else
//						config.enable(false);
//				}
//				for (SbmtMultiMissionTool.Mission defaultMission : info.getDefaultFor())
//				{
//					if (defaultMission == mission)
//					{
//						ViewConfig.setFirstTimeDefaultModelName(info.getUniqueName());
//						break;
//					}
//				}
////				setBodyEnableState(mission, config);
//			}
//		}

	}

	protected void setBodyEnableState(Mission mission, SmallBodyViewConfig config)
	{
		switch (mission)
		{
		case APL_INTERNAL_NIGHTLY:
		case APL_INTERNAL:
//		case STAGE_APL_INTERNAL:
		case TEST_APL_INTERNAL:
			config.enable(true);
			break;
		case PUBLIC_RELEASE:
//		case STAGE_PUBLIC_RELEASE:
		case TEST_PUBLIC_RELEASE:
		case DART_DEPLOY:
			if (!ShapeModelBody.EARTH.equals(config.body)
					&& !ShapeModelBody.RQ36.equals(config.body)
					&& !ShapeModelBody.RYUGU.equals(config.body)
					&& !ShapeModelPopulation.PLUTO.equals(config.population)
					&& (!config.getUniqueName().contains("MEGANE")))
            {
                config.enable(true);
            }
			else if (ShapeModelBody.RQ36.equals(config.body) && ShapeModelType.NOLAN.equals(config.author))
			{
				// This is the only public Bennu model.
                config.enable(true);
			}
            break;
        case HAYABUSA2_DEV:
			if (ShapeModelBody.EROS.equals(config.body)
					|| ShapeModelBody.ITOKAWA.equals(config.body)
					|| ShapeModelBody.RYUGU.equals(config.body)
					|| (ShapeModelBody.EARTH.equals(config.body) && ShapeModelType.JAXA_SFM_v20180627.equals(config.author)))
			{
				config.enable(true);
			}
			break;
//		case HAYABUSA2_STAGE:
		case HAYABUSA2_DEPLOY:
			if (ShapeModelBody.RYUGU.equals(config.body))
			{
				config.enable(true);
			}
			break;
		case OSIRIS_REX:
		case OSIRIS_REX_DEPLOY:
		case OSIRIS_REX_MIRROR_DEPLOY:
//		case OSIRIS_REX_STAGE:
			if (ShapeModelBody.RQ36.equals(config.body)
					|| ShapeModelBody.EROS.equals(config.body)
					|| ShapeModelBody.ITOKAWA.equals(config.body)
					|| ShapeModelType.OREX.equals(config.author))
			{
				config.enable(true);
			}
			break;
		case NH_DEPLOY:
			if (ShapeModelBody.MU69.equals(config.body)
					|| ShapeModelBody.EROS.equals(config.body)
					|| ShapeModelBody.ITOKAWA.equals(config.body))
			{
				config.enable(true);
			}
			break;
		default:
			throw new AssertionError();
		}
	}
}
