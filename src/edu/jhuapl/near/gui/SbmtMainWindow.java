package edu.jhuapl.near.gui;

import javax.swing.ImageIcon;



/**
 * This class contains the "main" function called at the start of the program.
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
        return new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
    }

    protected HelpMenu createHelpMenu(ViewManager rootPanel)
    {
        return new ExampleHelpMenu(rootPanel);
    }
}
