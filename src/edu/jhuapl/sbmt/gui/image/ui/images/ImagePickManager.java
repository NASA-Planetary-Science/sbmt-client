package edu.jhuapl.sbmt.gui.image.ui.images;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.sbmt.gui.lidar.LidarPickManager;

public class ImagePickManager extends LidarPickManager
{
    public ImagePickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        super(renderer, popupManager, modelManager,
               new ImageDefaultPicker(renderer, statusBar, modelManager, popupManager)
               );

    }
}