package edu.jhuapl.sbmt.client;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.jgoodies.looks.LookUtils;

import edu.jhuapl.saavtk.gui.OSXAdapter;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.tools.SbmtRunnable;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and other initialization.
 * The main function may take one optional argument. If there are no
 * arguments specified, then the tool starts up as usual showing Eros
 * by default. If one argument is specified, it is assumed to be a path
 * to a temporary shape model which is then loaded as a custom view
 * though it is not retained the next time the tool starts.
 */
public class SbmtMultiMissionTool
{

    public enum Mission
    {
        APL_INTERNAL("b1bc7ed"),
        PUBLIC_RELEASE("3ee38f0"),
        HAYABUSA2("133314b"),
        HAYABUSA2_STAGE("244425c"),
        HAYABUSA2_DEPLOY("355536d"),
        OSIRIS_REX("7cd84586"),
        ;
        private final String hashedName;

        Mission(String hashedName)
        {
            this.hashedName = hashedName;
        }

        String getHashedName()
        {
            return hashedName;
        }
    }

    // DO NOT change anything about this without also confirming the script set-released-mission.sh still works correctly!
    // This field is used during the build process to "hard-wire" a release to point to a specific server.
    private static final Mission RELEASED_MISSION = null;
    private static Mission mission = RELEASED_MISSION;
    private static boolean missionConfigured = false;

    static
    {
        if (Configuration.isMac())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            ImageIcon erosIcon = new ImageIcon(SbmtMultiMissionTool.class.getResource("/edu/jhuapl/sbmt/data/erosMacDock.png"));
            OSXAdapter.setDockIconImage(erosIcon.getImage());
        }
    }

    static void setupLookAndFeel()
    {
        try
        {
            if (!Configuration.isMac())
            {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
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
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    public static Mission getMission()
    {
        if (mission == null)
        {
            String missionHash = System.getProperty("edu.jhuapl.sbmt.mission");
            if (missionHash == null)
            {
                throw new RuntimeException("Invalid launch configuration");
            }
            for (Mission each: Mission.values())
            {
                if (each.getHashedName().equalsIgnoreCase(missionHash))
                {
                    mission = each;
                    break;
                }
            }
        }

        return mission;
    }

    public static Mission configureMission()
    {
        if (missionConfigured)
        {
            return mission;
        }
        Mission mission = getMission();
        switch (mission)
        {
            case APL_INTERNAL:
            case PUBLIC_RELEASE:
                Configuration.setAppName("sbmt");
                Configuration.setCacheVersion("2");
                Configuration.setAppTitle("SBMT");
                break;
            case HAYABUSA2:
                Configuration.setRootURL("http://sbmt.jhuapl.edu/internal/sbmt");
                Configuration.setAppName("sbmthyb2-dev");
                Configuration.setCacheVersion("");
                Configuration.setAppTitle("SBMT/Hayabusa2-Dev");
                break;
            case HAYABUSA2_STAGE:
                Configuration.setRootURL("http://hyb2sbmt.jhuapl.edu/sbmt");
                Configuration.setAppName("sbmthyb2-stage");
                Configuration.setCacheVersion("");
                Configuration.setAppTitle("SBMT/Hayabusa2-Stage");
                break;
            case HAYABUSA2_DEPLOY:
                Configuration.setRootURL("http://hyb2sbmt.u-aizu.ac.jp/sbmt");
                Configuration.setAppName("sbmthyb2");
                Configuration.setCacheVersion("");
                Configuration.setAppTitle("SBMT/Hayabusa2");
                break;
            case OSIRIS_REX:
//                Configuration.setRootURL("http://sbmt.jhuapl.edu/internal/sbmt");
                Configuration.setAppName("sbmt1orex");
                Configuration.setCacheVersion("");
                Configuration.setAppTitle("SBMT/OSIRIS REx");
                break;
            default:
                throw new AssertionError();
        }
        missionConfigured = true;
        return mission;
    }

    static SbmtSplash createSplash(Mission mission)
    {
        SbmtSplash splash = null;
        switch (mission)
        {
            case APL_INTERNAL:
            case PUBLIC_RELEASE:
                splash = new SbmtSplash("resources", "splashLogo.png");
                break;
            case HAYABUSA2:
                splash = new SbmtSplash("resources", "splashLogoHb2Dev.png");
                break;
            case HAYABUSA2_STAGE:
                splash = new SbmtSplash("resources", "splashLogoHb2Stage.png");
                break;
            case HAYABUSA2_DEPLOY:
                splash = new SbmtSplash("resources", "splashLogoHb2.png");
                break;
            case OSIRIS_REX:
                splash = new SbmtSplash("resources", "splashLogoOrex.png");
                break;
            default:
                throw new AssertionError();
        }
        return splash;
    }

    public static String getOption(String[] args, String optionPrefix)
    {
        for (String arg : args)
        {
            arg = arg.toLowerCase();
            optionPrefix = optionPrefix.toLowerCase();
            if (arg.startsWith(optionPrefix + "="))
            {
                return arg.substring(optionPrefix.length() + 1);
            }
            else if (arg.startsWith(optionPrefix))
            {
                return arg.substring(optionPrefix.length());
            }
        }
        return null;
    }

    public static void main(final String[] args)
    {
        Mission mission = SbmtMultiMissionTool.configureMission();

        setupLookAndFeel();


        /*if(!startPopup)   INITIALIZES THE START SCREEN
        {
            startPopup=true;
            new StartScreen();
        }*/
        // The following line appears to be needed on some systems to prevent server redirect errors.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        try
        {
            // set up splash screen
            SbmtSplash splash = createSplash(mission);
            splash.setAlwaysOnTop(true);
            splash.setVisible(true);
            splash.validate();
            splash.repaint();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new SbmtRunnable(args));
            Thread.sleep(8000);
            splash.setVisible(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
