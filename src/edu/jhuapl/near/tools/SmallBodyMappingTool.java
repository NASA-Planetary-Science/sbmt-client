package edu.jhuapl.near.tools;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.jgoodies.looks.LookUtils;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.OSXAdapter;
import edu.jhuapl.near.util.Configuration;

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
    private static vtkJavaGarbageCollector garbageCollector;
    private static boolean startPopup = false;

    static
    {
        if (Configuration.isMac())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            ImageIcon erosIcon = new ImageIcon(SmallBodyMappingTool.class.getResource("/edu/jhuapl/near/data/erosMacDock.png"));
            OSXAdapter.setDockIconImage(erosIcon.getImage());
        }
    }

    private static void setupLookAndFeel()
    {
        try
        {
            if (!Configuration.isMac())
            {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
            else
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

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

    public static void main(final String[] args)
    {
        Configuration.setAppName("neartool");
        Configuration.setCacheVersion("2");

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
            javax.swing.SwingUtilities.invokeLater(new SbmtRunnable(args));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
