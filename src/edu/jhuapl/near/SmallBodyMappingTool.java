package edu.jhuapl.near;

import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.jgoodies.looks.LookUtils;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.MainWindow;
import edu.jhuapl.near.gui.OSXAdapter;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.NativeLibraryLoader;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and other initialization.
 * @author kahneg1
 *
 */
public class SmallBodyMappingTool
{
    private static vtkJavaGarbageCollector garbageCollector;

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
            if (Configuration.isMac())
            {
                System.setProperty("Quaqua.tabLayoutPolicy","wrap");

                // Use reflection to load the quaqua look and feel
                Class quaquaManagerClass = Class.forName("ch.randelshofer.quaqua.QuaquaManager");
                Method getLookAndFeelMethod = quaquaManagerClass.getDeclaredMethod("getLookAndFeel", new Class[] { });
                LookAndFeel lookAndFeel = (LookAndFeel)getLookAndFeelMethod.invoke(null, new Object[] { });
                UIManager.setLookAndFeel(lookAndFeel);
            }
            else
            {
                UIManager.put("ClassLoader", LookUtils.class.getClassLoader());
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
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

    public static void main(String[] args)
    {
        // The following line appears to be needed on some systems to prevent server redirect errors.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        try
        {
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    NativeLibraryLoader.loadVtkLibraries();

                    garbageCollector = new vtkJavaGarbageCollector();
                    //garbageCollector.SetDebug(true);
                    garbageCollector.SetScheduleTime(5, TimeUnit.SECONDS);
                    garbageCollector.SetAutoGarbageCollection(true);

                    setupLookAndFeel();

                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
                    ToolTipManager.sharedInstance().setDismissDelay(600000); // 10 minutes

                    MainWindow frame = new MainWindow();
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
