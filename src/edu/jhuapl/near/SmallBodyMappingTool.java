package edu.jhuapl.near;

import java.awt.BorderLayout;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import vtk.vtkJavaGarbageCollector;

import edu.jhuapl.near.gui.FileMenu;
import edu.jhuapl.near.gui.HelpMenu;
import edu.jhuapl.near.gui.OSXAdapter;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.ViewMenu;
import edu.jhuapl.near.gui.ViewerManager;
import edu.jhuapl.near.util.NativeLibraryLoader;


/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class SmallBodyMappingTool extends JFrame
{
    private StatusBar statusBar;
    private FileMenu fileMenu;
    private ViewMenu viewMenu;
    private HelpMenu helpMenu;
    private ViewerManager rootPanel;
    private static vtkJavaGarbageCollector garbageCollector;

    static
    {
        if (System.getProperty("os.name").toLowerCase().startsWith("mac"))
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            ImageIcon erosIcon = new ImageIcon(SmallBodyMappingTool.class.getResource("/edu/jhuapl/near/data/eros.png"));
            OSXAdapter.setDockIconImage(erosIcon.getImage());
        }
    }

    public SmallBodyMappingTool()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createStatusBar();

        rootPanel = new ViewerManager(statusBar);

        createMenus();

        this.add(rootPanel, BorderLayout.CENTER);

//        // Center the application on the screen.
//        Dimension prefSize = this.getPreferredSize();
//        Dimension parentSize;
//        java.awt.Point parentLocation = new java.awt.Point(0, 0);
//        parentSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
//        int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
//        this.setLocation(x, y);
//        this.setResizable(true);
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        fileMenu = new FileMenu(rootPanel);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        viewMenu = new ViewMenu(rootPanel);
        viewMenu.setMnemonic('V');
        menuBar.add(viewMenu);

        helpMenu = new HelpMenu(rootPanel);
        helpMenu.setMnemonic('H');
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createStatusBar()
    {
        statusBar = new StatusBar();
        this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }

    /*
    private static void setupLookAndFeel()
    {
        try
        {
            if (System.getProperty("os.name").toLowerCase().startsWith("linux"))
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
    */

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

                    try
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
                    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

                    SmallBodyMappingTool frame = new SmallBodyMappingTool();

                    ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
                    frame.setIconImage(erosIcon.getImage());
                    frame.setTitle("Small Body Mapping Tool");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
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
