package edu.jhuapl.sbmt.client;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.ViewManager;
import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.gui.menu.FileMenu;
import edu.jhuapl.saavtk.main.MainWinCfg;
import edu.jhuapl.saavtk.main.io.MainWinConfigUtil;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;

import glum.gui.info.WindowCfg;
import glum.task.SilentTask;



/**
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 */
public class SbmtMainWindow extends MainWindow
{
    public SbmtMainWindow(String tempCustomShapeModelPath)
    {
        super(tempCustomShapeModelPath);
        addComponentListener(new ComponentAdapter()
		{
        	@Override
        	public void componentResized(ComponentEvent e)
        	{
        		MainWinCfg mainAppCfg = MainWindow.getMainWindow().getMainAppCfg();
        		var tmpMainAppCfg = new MainWinCfg(new WindowCfg(MainWindow.getMainWindow()), mainAppCfg.mainSplitSize());
        		MainWinConfigUtil.saveConfiguration(new SilentTask(), tmpMainAppCfg);
        		super.componentResized(e);
        	}
		});
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
        return new SbmtViewManager(aStatusNotifier, this, tempCustomShapeModelPath);
    }

    @Override
    protected ImageIcon createImageIcon()
    {
        return new ImageIcon(getClass().getResource("/edu/jhuapl/sbmt/data/eros.png"));
    }

    @SuppressWarnings("unused")
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
                System.out.println(
                        "SbmtMainWindow.SaveImagesAsSTLAction: actionPerformed: file " + file);
                if (file != null)
                {
//                    ImageCollection collection = (ImageCollection)rootPanel.getCurrentView().getModelManager().getModel(ModelNames.IMAGES);
                    @SuppressWarnings("rawtypes")
					PerspectiveImageCollection collection = (PerspectiveImageCollection)rootPanel.getCurrentView().getModelManager().getModel(ModelNames.IMAGES_V2);
//                    System.out.println(
//                            "SbmtMainWindow.SaveImagesAsSTLAction: actionPerformed: number of images " + collection.getImages().size());
                    @SuppressWarnings("unchecked")
					List<PerspectiveImageMetadata> images = collection.getAllItems();
                    for (PerspectiveImageMetadata image : images)
                    {
                        System.out.println(image.getName());
                        //TODO FIX THIS
//                        image.outputToOBJ(file.toPath().resolve(image.getName()+".obj").toAbsolutePath().toString());
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
