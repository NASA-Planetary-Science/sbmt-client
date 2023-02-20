package edu.jhuapl.sbmt.tools;

import java.awt.HeadlessException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import org.gdal.gdal.gdal;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.TSConsole;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Configuration.ReleaseType;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.DownloadableFileManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.client2.SbmtMainWindow;
import edu.jhuapl.sbmt.client2.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.common.client.Mission;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;



public class SbmtRunnable2 implements Runnable
{
	private final String initialShapeModelPath;

	public SbmtRunnable2(String initialShapeModelPath)
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
			NativeLibraryLoader.loadGDALLibraries();
			gdal.AllRegister();

			SmallBodyViewConfig.initialize();
			//            new SmallBodyViewConfigMetadataIO(SmallBodyViewConfig.getBuiltInConfigs()).write(new File("/Users/steelrj1/Desktop/test.json"), "Test");

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

                // Set this before starting the access monitor, whether or not
                // profiling will be enabled.
			    DownloadableFileManager.setProfileAreaPrefix("baseline");

                // Uncomment this line to enable profiling. Because this clears
                // out the profiling results area, it will naturally sync up all
                // clients running on the same host to start profiling after the
                // last copy of the client starts. Need to call this before
                // starting access monitor to profile all transitions.
                // FileCache.instance().enableProfiling();

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

                            TSConsole.hideConsole();
                            TSConsole.setDefaultLocation(frame);
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
		Date compileDate = SbmtMultiMissionTool.compileDate;
		String version = (Configuration.getReleaseType() == ReleaseType.DEVELOPMENT) ? "" : SbmtMultiMissionTool.versionString;

		System.out.println("Welcome to the Small Body Mapping Tool (SBMT) " + version);
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
				System.out.println("You may update your user name and password on the Body -> Update Password menu.");
			}
		}
		System.out.println("\nStoring application data in " + Configuration.getApplicationDataDir());

		if (TSConsole.isConfigured())
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

}
