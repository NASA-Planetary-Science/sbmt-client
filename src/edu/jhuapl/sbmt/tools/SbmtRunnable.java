package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import vtk.vtkJavaGarbageCollector;
import vtk.vtkNativeLibrary;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafePaths;
import edu.jhuapl.sbmt.client.SbmtMainWindow;
import edu.jhuapl.sbmt.client.SmallBodyMappingTool;
import edu.jhuapl.sbmt.client.SmallBodyMappingTool.Mission;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;

public class SbmtRunnable implements Runnable
{
    private static final Path OUTPUT_FILE_PATH = SafePaths.get(Configuration.getApplicationDataDir(), "sbmtLogFile.txt");
    private static final Path SAVED_OUTPUT_FILE_PATH = SafePaths.get(System.getProperty("user.home"), "sbmtLogFile.txt");

    private final String[] args;
    private final PrintStream savedOut;
    private final PrintStream savedErr;

    public SbmtRunnable(String[] args)
    {
        this.args = args;
        this.savedOut = System.out;
        this.savedErr = System.err;
    }

    @Override
    public void run()
    {
        // Don't use try-with-resources form -- it suppresses reporting the actual exception!
        PrintStream outputFile = null;
        try
        {
            // Parse options that come first
            int i = 0;
            boolean redirectStreams = true; // Proper default behavior for released code.
            for (; i < args.length; ++i) {
                if (args[i].equals("--beta")) {
                    SmallBodyViewConfig.betaMode = true;
                } else if (args[i].equals("--no-stream-redirect")) {
                    redirectStreams = false;
                } else if (!args[i].startsWith("-")) {
                    // We've encountered something that is not an option, must be at the args
                    break;
                }
            }
            if (redirectStreams)
            {
                outputFile = new PrintStream(Files.newOutputStream(OUTPUT_FILE_PATH));
                redirectStreams(outputFile);
            }
            Mission mission = SmallBodyMappingTool.getMission();
            writeStartupMessage(mission);
            SmallBodyViewConfig.initialize();
            configureMissionBodies(mission);


            // After options comes the args
            String tempShapeModelPath = null;
            if (Configuration.isAPLVersion() && args.length - i >= 1){
                tempShapeModelPath = args[i++];
            }

            //  NativeLibraryLoader.loadVtkLibraries();

            vtkNativeLibrary.LoadAllNativeLibraries();

            vtkJavaGarbageCollector garbageCollector = new vtkJavaGarbageCollector();
            //garbageCollector.SetDebug(true);
            garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
            garbageCollector.SetAutoGarbageCollection(true);

            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

            MainWindow frame = new SbmtMainWindow(tempShapeModelPath);
            frame.setVisible(true);
            FileCache.showDotsForFiles(false);
            System.out.println("\nSBMT Ready");

        }
        catch (Exception e)
        {
            // Something went tragically wrong, so report the error, close the output file and
            // move it to a more prominent location.
            e.printStackTrace();
            restoreStreams();
            if (outputFile != null)
            {
                outputFile.close();
                try
                {
                    Files.move(OUTPUT_FILE_PATH, SAVED_OUTPUT_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    protected void redirectStreams(final PrintStream outputFile)
    {
        System.setOut(outputFile);
        System.setErr(outputFile);
    }

    protected void restoreStreams()
    {
        System.setErr(savedErr);
        System.setOut(savedOut);
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    protected void writeStartupMessage(Mission mission)
    {
        Date compileDate = null;
        try
        {
            compileDate = new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified());
        }
        catch (@SuppressWarnings("unused") URISyntaxException e)
        {
        }
        System.out.println("Welcome to the Small Body Mapping Tool (SBMT)");
        System.out.println(mission + " edition" + (compileDate != null ? " released " + DATE_FORMAT.format(compileDate) : ""));
        if (Debug.isEnabled())
        {
            System.out.println("Tool started in debug mode; diagnostic output is enabled.");
        }
        System.out.println("Using server at " + Configuration.getDataRootURL());
        if (!Configuration.isPasswordAuthenticationSetup())
        {
            System.out.println("Warning: no correctly formatted password file found. "
                    + "Continuing without password. Some models may not be available.");
        }
        System.out.println("Please be patient while the SBMT starts up");

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
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            if (each instanceof SmallBodyViewConfig)
            {
                SmallBodyViewConfig config = (SmallBodyViewConfig) each;
                setBodyEnableState(mission, config);
            }
        }

    }

    protected void setBodyEnableState(Mission mission, SmallBodyViewConfig config)
    {
        switch (mission)
        {
        case HAYABUSA2:
            if (
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelType.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case HAYABUSA2_STAGE:
            if (
//                    ShapeModelBody.EROS.equals(config.body) ||
//                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelType.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case HAYABUSA2_DEPLOY:
            if (
//                    ShapeModelBody.EROS.equals(config.body) ||
//                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelType.HAYABUSA2.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case NEARTOOL:
            config.enable(true);
            break;
        case OSIRIS_REX:
            if (
                    ShapeModelBody.RQ36.equals(config.body) ||
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelType.OREX.equals(config.author)
               )
            {
                config.enable(true);
            }
            break;
            default:
                throw new AssertionError();
        }
    }
}
