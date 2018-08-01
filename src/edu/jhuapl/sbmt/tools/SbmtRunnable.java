package edu.jhuapl.sbmt.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.JarURLConnection;
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
import edu.jhuapl.saavtk.gui.Console;
import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafePaths;
import edu.jhuapl.sbmt.client.SbmtMainWindow;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool.Mission;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
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
        boolean exitOnError = true;
        try
        {
            // Parse options that come first
            int i = 0;
            // Default settings are correct for released version of the tool.
            boolean clearCache = false;
            boolean redirectStreams = true;
            for (; i < args.length; ++i) {
                if (args[i].equals("--beta")) {
                    SmallBodyViewConfig.betaMode = true;
                } else if (args[i].equals("--auto-clear-cache")) {
                    clearCache = true;
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
                Console.configure(true, outputFile);
                Console.showStandaloneConsole();
                exitOnError = false;
            }
            Mission mission = SbmtMultiMissionTool.getMission();
            writeStartupMessage(mission, redirectStreams);
            if (clearCache)
            {
                Configuration.clearCache();
            }
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














///////////////////////// temporary ///////////

//
//SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(ShapeModelBody.EARTH, ShapeModelType.OREX);
//SmallBodyModel earth = SbmtModelFactory.createSmallBodyModel(config);
//BoundingBox bb = earth.getBoundingBox();
//
//
//
//String filename = "/Users/osheacm1/Documents/SAA/SBMT/testSpectrum/ovirs_l3reff_files.txt";
//FileWriter fw;
//try
//{
//fw = new FileWriter(filename);
//
//BufferedWriter bw = new BufferedWriter(fw);
//
//String inputDir  = "/Users/osheacm1/Documents/SAA/SBMT/testSpectrum/ovirs_l3reff/infofiles/";
//File[] infoFiles = new File(inputDir).listFiles();
//
//int iFile = 0;
//for (File infoFile : infoFiles) {
//    // get filename
//    String thisFileName = infoFile.getAbsolutePath();
//    String specFileName = thisFileName.replaceAll(".INFO", ".spect").replaceAll("infofiles/", "spectrum/");
//
//
//
//    // get x, y, z bounds and time, emission, incidence, phase, s/c distance
//    InfoFileReader reader = new InfoFileReader(thisFileName);
//    reader.read();
//
//    Vector3D origin = new Vector3D(reader.getSpacecraftPosition());
//    //          .scalarMultiply(1e-3);
//    Vector3D fovUnit = new Vector3D(reader.getFrustum2()).normalize(); // for whatever reason, frustum2 contains the vector along the field of view cone
//    Vector3D boresightUnit = new Vector3D(reader.getBoresightDirection()).normalize();
//    Vector3D lookTarget = origin
//            .add(boresightUnit.scalarMultiply(origin.getNorm()));
//
//
//    double fovDeg = Math
//            .toDegrees(Vector3D.angle(fovUnit, boresightUnit) * 2.);
//    Vector3D toSunUnitVector = new Vector3D(reader.getSunPosition()).normalize();
//    Frustum frustum = new Frustum(origin.toArray(), lookTarget.toArray(),
//            boresightUnit.orthogonal().toArray(), fovDeg, fovDeg);
//    double[] frustum1 = frustum.ul;
//    double[] frustum2 = frustum.ur;
//    double[] frustum3 = frustum.lr;
//    double[] frustum4 = frustum.ll;
//    double[] spacecraftPosition = frustum.origin;
//
//
////    double[] intersectPoint = new double[3];
////      int boresightInterceptFaceID = earth.computeRayIntersection(origin.toArray(), boresightUnit.toArray(), intersectPoint);
////      double[] interceptNormal = earth.getCellNormals().GetTuple3(boresightInterceptFaceID);
////      vtkCellCenters centers = new vtkCellCenters();
////      centers.SetInputData(earth.getSmallBodyPolyData());
////      centers.VertexCellsOn();
////      centers.Update();
////      double[] center = centers.GetOutput().GetPoint(boresightInterceptFaceID);
////
//////      double[] center = earth.getSmallBodyPolyData().GetPoint(boresightInterceptFaceID);
////
////      System.out.println("OTESSpectrum: readPointingFromInfoFile: intercept normal " + interceptNormal[0] + " " + interceptNormal[1] + " " + interceptNormal[2]);
////      System.out.println("OTESSpectrum: readPointingFromInfoFile: center " + intersectPoint[0] + " " + intersectPoint[1] + " " + intersectPoint[2]);
////
////      Vector3D nmlVec=new Vector3D(interceptNormal).normalize();
////      Vector3D ctrVec=new Vector3D(intersectPoint).normalize();
////      Vector3D toScVec=new Vector3D(spacecraftPosition).subtract(ctrVec);
////      double emissionAngle = Math.toDegrees(Math.acos(nmlVec.dotProduct(toScVec.normalize())));
////      System.out.println("emission angle: " + emissionAngle);
//
//
//    vtkPolyData tmp = earth.computeFrustumIntersection(
//            spacecraftPosition, frustum1, frustum2, frustum3, frustum4);
//    if (tmp != null) {
//        double[] bbox = tmp.GetBounds();
//        System.out.println("file " + iFile++ + ": " + bbox[0] + ", " + bbox[1] + ", " + bbox[2] + ", " + bbox[3]);
//        bw.write(specFileName + " " + bbox[0] + " " + bbox[1] + " " + bbox[2] + " " + bbox[3] + " " + bbox[4] + " " + bbox[5] +" " + reader.getStartTime() + " " + reader.getStopTime()+", 0, 0, 0, 0\n");
//    }
//
//
//}
//bw.close();
//}
//catch (IOException e)
//{
//// TODO Auto-generated catch block
//e.printStackTrace();
//}
//









/////////////////////////////////////////////





            MainWindow frame = new SbmtMainWindow(tempShapeModelPath);
            MainWindow.setMainWindow(frame);
            FileCache.showDotsForFiles(false);
            System.out.println("\nSBMT Ready");

            frame.setVisible(true);
            Console.hideConsole();
            exitOnError = true;
            Console.setDefaultLocation(frame);
        }
        catch (Throwable throwable)
        {
            // Something went tragically wrong, so report the error, close the output file and
            // move it to a more prominent location.
            throwable.printStackTrace();
            System.err.println("\nThe SBMT had a fatal error during launch. Please view this console window for more information.");
            System.err.println("Note that the SBMT requires an internet connection the first time it is launched.");
            System.err.println("Close the console window to exit the SBMT.");
            if (outputFile != null)
            {
                restoreStreams();
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
            if (exitOnError)
            {
                System.exit(1);
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
    protected void writeStartupMessage(Mission mission, boolean redirectStreams)
    {
        Date compileDate = null;
        try
        {
            compileDate = new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified());
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            try {
                String rn = getClass().getName().replace('.', '/') + ".class";
                JarURLConnection j = (JarURLConnection) ClassLoader.getSystemResource(rn).openConnection();
                long time =  j.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
                compileDate = new Date(time);
            } catch (@SuppressWarnings("unused") Exception e1) {
            }
        }

        FileCache.showDotsForFiles(true);
        System.out.println("Welcome to the Small Body Mapping Tool (SBMT)");
        System.out.println(mission + " edition" + (compileDate != null ? " built " + DATE_FORMAT.format(compileDate) : ""));
        if (Debug.isEnabled())
        {
            System.out.println("Tool started in debug mode; diagnostic output is enabled.");
        }
        System.out.println("Using server at " + Configuration.getDataRootURL());
        if (Configuration.wasUserPasswordAccepted())
        {
            System.out.println("\nValid user name and password entered. Access may be granted to some restricted models.");
        }
        else
        {
            System.out.println("\nNo user name and password entered. Some models may not be available.");
            System.out.println("You may update your user name and pasword on the Body -> Update Password menu.");
        }
        if (redirectStreams)
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
        case APL_INTERNAL:
        case STAGE_APL_INTERNAL:
        case TEST_APL_INTERNAL:
            config.enable(true);
            break;
        case PUBLIC_RELEASE:
        case STAGE_PUBLIC_RELEASE:
        case TEST_PUBLIC_RELEASE:
            if (
                    !ShapeModelType.JAXA_001.equals(config.author) &&
                    !ShapeModelType.NASA_001.equals(config.author) &&
                    !ShapeModelType.NASA_002.equals(config.author) &&
                    !ShapeModelType.OREX.equals(config.author) &&
                    !(ShapeModelBody.RQ36.equals(config.body) && ShapeModelType.GASKELL.equals(config.author)) &&
                    !ShapeModelBody.RYUGU.equals(config.body) &&
                    !ShapeModelPopulation.PLUTO.equals(config.population)
               )
            {
                config.enable(true);
            }
            break;
        case HAYABUSA2_DEV:
            if (
                    ShapeModelBody.EROS.equals(config.body) ||
                    ShapeModelBody.ITOKAWA.equals(config.body) ||
                    ShapeModelType.JAXA_001.equals(config.author) ||
                    ShapeModelType.NASA_001.equals(config.author) ||
                    ShapeModelType.NASA_002.equals(config.author) ||
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
                    ShapeModelType.NASA_001.equals(config.author) ||
                    ShapeModelType.NASA_002.equals(config.author) ||
                    ShapeModelType.JAXA_001.equals(config.author) ||
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
                    ShapeModelType.NASA_001.equals(config.author) ||
                    ShapeModelType.NASA_002.equals(config.author) ||
                    ShapeModelType.JAXA_001.equals(config.author) ||
                    ShapeModelBody.RYUGU.equals(config.body)
               )
            {
                config.enable(true);
            }
            break;
        case OSIRIS_REX:
        case OSIRIS_REX_DEPLOY:
        case OSIRIS_REX_MIRROR_DEPLOY:
        case OSIRIS_REX_STAGE:
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
