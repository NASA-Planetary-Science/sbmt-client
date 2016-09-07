package edu.jhuapl.near.pick;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.DefaultPicker;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.util.Preferences;

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