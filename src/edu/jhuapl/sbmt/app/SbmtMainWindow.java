package edu.jhuapl.sbmt.app;

import javax.swing.ImageIcon;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;
import edu.jhuapl.saavtk.gui.menu.HelpMenu;



/**
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public class SbmtMainWindow extends MainWindow
{
    public SbmtMainWindow(String tempCustomShapeModelPath)
    {
        super(tempCustomShapeModelPath);
   }

    protected ViewManager createViewManager(StatusBar statusBar, MainWindow mainWindow, String tempCustomShapeModelPath)
    {
        return new SbmtViewManager(statusBar, this, tempCustomShapeModelPath);
    }

    protected ViewMenu createViewMenu(ViewManager rootPanel, RecentlyViewed recentsMenu)
    {
        return new SbmtViewMenu(rootPanel, recentsMenu);
    }

    protected ImageIcon createImageIcon()
    {
        return new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
    }

    protected HelpMenu createHelpMenu(ViewManager rootPanel)
    {
        return new SbmtHelpMenu(rootPanel);
    }
}
