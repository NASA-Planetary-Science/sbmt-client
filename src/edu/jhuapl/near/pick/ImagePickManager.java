package edu.jhuapl.near.pick;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.Preferences;

public class ImagePickManager extends LidarPickManager
{
    public ImagePickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        super(renderer, statusBar, modelManager, popupManager);
        DefaultPicker defaultPicker = new ImageDefaultPicker(renderer, statusBar, modelManager, popupManager);
        setDefaultPicker(defaultPicker);

        setPickTolerance(Preferences.getInstance().getAsDouble(
                Preferences.PICK_TOLERANCE, Picker.DEFAULT_PICK_TOLERANCE));

        addPicker(defaultPicker);
    }
}