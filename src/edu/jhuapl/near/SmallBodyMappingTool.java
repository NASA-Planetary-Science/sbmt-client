package edu.jhuapl.near;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.MainWindow;
import edu.jhuapl.near.gui.OSXAdapter;
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
        if (System.getProperty("os.name").toLowerCase().startsWith("mac"))
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
            if (System.getProperty("os.name").toLowerCase().startsWith("mac"))
            {
                System.setProperty("Quaqua.tabLayoutPolicy","wrap");

                try
                {
                    // Use reflection to load the quaqua look and feel
                    Class quaquaManagerClass = Class.forName("ch.randelshofer.quaqua.QuaquaManager");
                    Method getLookAndFeelMethod = quaquaManagerClass.getDeclaredMethod("getLookAndFeel", new Class[] { });
                    LookAndFeel lookAndFeel = (LookAndFeel)getLookAndFeelMethod.invoke(null, new Object[] { });
                    UIManager.setLookAndFeel(lookAndFeel);
                }
                catch(Exception e)
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
            /*
            else if (System.getProperty("os.name").toLowerCase().startsWith("linux"))
            {
                boolean haveNimbus = false;
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                {
                    if ("Nimbus".equals(info.getName()))
                    {
                        UIManager.setLookAndFeel(info.getClassName());
                        haveNimbus = true;
                        break;
                    }
                }

                if (haveNimbus == false)
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            */
            else
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
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
