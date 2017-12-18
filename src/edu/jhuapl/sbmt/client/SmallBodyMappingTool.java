package edu.jhuapl.sbmt.client;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

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
public class SmallBodyMappingTool
{

    public enum Mission
    {
        HAYABUSA2("133314b"),
        HAYABUSA2_STAGE("244425c"),
        HAYABUSA2_DEPLOY("355536d"),
        NEARTOOL("b1bc7ed"),
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

    private static Mission mission = null;

    static
    {
        if (Configuration.isMac())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            ImageIcon erosIcon = new ImageIcon(SmallBodyMappingTool.class.getResource("/edu/jhuapl/sbmt/data/erosMacDock.png"));
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
            Mission mission = missionHash == null ? Mission.NEARTOOL : null;
            for (Mission each: Mission.values())
            {
                if (each.getHashedName().equals(missionHash))
                {
                    mission = each;
                    break;
                }
            }
            SmallBodyMappingTool.mission = mission;
        }

        return mission;
    }

    static Mission configureMission()
    {
        Mission mission = getMission();
        if (mission == null)
        {
            throw new RuntimeException("Invalid mission hash supplied at start-up");
        }

        switch (mission)
        {
        case HAYABUSA2:
            Configuration.setRootURL("http://sbmt.jhuapl.edu/sbmt");
            Configuration.setAppName("sbmt1hyb2");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/Hayabusa2-Dev");
            break;
        case HAYABUSA2_STAGE:
            Configuration.setRootURL("http://hyb2sbmt.jhuapl.edu/sbmt");
            Configuration.setAppName("sbmt1hyb2-stage");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/Hayabusa2-Stage");
            break;
        case HAYABUSA2_DEPLOY:
            Configuration.setRootURL("http://sbmt.jhuapl.edu/sbmt");
            Configuration.setAppName("sbmt1hyb2-deploy");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/Hayabusa2-Deploy");
            break;
        case NEARTOOL:
            Configuration.setRootURL("http://sbmt.jhuapl.edu/sbmt");
           Configuration.setAppName("neartool");
            Configuration.setCacheVersion("2");
            Configuration.setAppTitle("SBMT");
            break;
        case OSIRIS_REX:
            Configuration.setRootURL("http://sbmt.jhuapl.edu/sbmt");
            Configuration.setAppName("sbmt1orex");
            Configuration.setCacheVersion("");
            Configuration.setAppTitle("SBMT/OSIRIS REx");
            break;
            default:
                throw new AssertionError();
        }
       return mission;
    }

    static SbmtSplash createSplash(Mission mission)
    {
        SbmtSplash splash = null;
        switch (mission)
        {
        case HAYABUSA2:
            splash = new SbmtSplash("resources", "splashLogoHb2Dev.png");
            break;
        case HAYABUSA2_STAGE:
            splash = new SbmtSplash("resources", "splashLogoHb2Stage.png");
            break;
        case HAYABUSA2_DEPLOY:
            splash = new SbmtSplash("resources", "splashLogoHb2Deploy.png");
            break;
        case NEARTOOL:
            splash = new SbmtSplash("resources", "splashLogo.png");
            break;
        case OSIRIS_REX:
            splash = new SbmtSplash("resources", "splashLogoOrex.png");
            break;
            default:
                throw new AssertionError();
        }
        return splash;
    }

    public static void main(final String[] args)
    {
        if (Configuration.getAppName() == null)
        {
            SmallBodyMappingTool.configureMission();
        }
        Mission mission = getMission();

        setupLookAndFeel();

        // set up splash screen
        SbmtSplash splash = createSplash(mission);
        splash.setVisible(true);
        splash.validate();
        splash.repaint();

        /*if(!startPopup)   INITIALIZES THE START SCREEN
        {
            startPopup=true;
            new StartScreen();
        }*/
        // The following line appears to be needed on some systems to prevent server redirect errors.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        try
        {
            javax.swing.SwingUtilities.invokeLater(new SbmtRunnable(args));
            Thread.sleep(8000);
            splash.setVisible(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
