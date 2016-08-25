package edu.jhuapl.near.gui;



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

    protected ViewMenu createViewMenu(ViewManager rootPanel, RecentlyViewed recentsMenu)
    {
        return new SbmtViewMenu(rootPanel, recentsMenu);
    }
}
