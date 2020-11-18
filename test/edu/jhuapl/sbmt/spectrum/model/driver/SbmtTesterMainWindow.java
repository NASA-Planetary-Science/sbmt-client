package edu.jhuapl.sbmt.spectrum.model.driver;

import javax.swing.ImageIcon;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.status.StatusNotifier;



/**
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public class SbmtTesterMainWindow extends MainWindow
{
    public SbmtTesterMainWindow(String tempCustomShapeModelPath)
    {
        super(tempCustomShapeModelPath);
    }

    @Override
    protected FileMenu createFileMenu(ViewManager rootPanel)
    {

        FileMenu menu=super.createFileMenu(rootPanel);
//        JMenu saveImagesMenu=new JMenu("Save mapped images to...");
//        saveImagesMenu.add(new JMenuItem(new SaveImagesAsSTLAction()));
//        menu.add(new JSeparator());
//        menu.add(saveImagesMenu);
        return menu;

    }

    @Override
    protected ViewManager createViewManager(StatusNotifier aStatusNotifier, String tempCustomShapeModelPath)
    {
        return new SbmtTesterViewManager(aStatusNotifier, this, tempCustomShapeModelPath);
    }

    @Override
    protected ImageIcon createImageIcon()
    {
        return new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
    }

}
