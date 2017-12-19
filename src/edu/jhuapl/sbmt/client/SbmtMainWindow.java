package edu.jhuapl.sbmt.client;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.RecentlyViewed;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.ViewMenu;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.gui.menu.HelpMenu;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.model.image.Image;
import edu.jhuapl.sbmt.model.image.ImageCollection;



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

    @Override
    protected FileMenu createFileMenu(ViewManager rootPanel)
    {

        FileMenu menu=super.createFileMenu(rootPanel);
        JMenu saveImagesMenu=new JMenu("Save visible images to...");
        saveImagesMenu.add(new JMenuItem(new SaveImagesAsSTLAction()));
        menu.add(new JSeparator());
        menu.add(saveImagesMenu);
        return menu;

    }

    @Override
    protected ViewManager createViewManager(StatusBar statusBar, MainWindow mainWindow, String tempCustomShapeModelPath)
    {
        return new SbmtViewManager(statusBar, this, tempCustomShapeModelPath);
    }

    @Override
    protected ViewMenu createViewMenu(ViewManager viewManager, RecentlyViewed recentsMenu)
    {
        return new SbmtViewMenu((SbmtViewManager) viewManager, recentsMenu);
    }

    @Override
    protected ImageIcon createImageIcon()
    {
        return new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
    }

    @Override
    protected HelpMenu createHelpMenu(ViewManager rootPanel)
    {
        return new SbmtHelpMenu(rootPanel);
    }

    private class SaveImagesAsSTLAction extends AbstractAction
    {
        public SaveImagesAsSTLAction()
        {
            super("OBJ...");
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent actionEvent)
        {
            File file = DirectoryChooser.showOpenDialog(rootPanel);

            try
            {
                if (file != null)
                {
                    ImageCollection collection = (ImageCollection)rootPanel.getCurrentView().getModelManager().getModel(ModelNames.IMAGES);
                    for (Image image : collection.getImages())
                    {
                        System.out.println(image.getImageName());
                        image.outputToOBJ(file.toPath().resolve(image.getImageName()+".obj").toAbsolutePath().toString());
                    }
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
