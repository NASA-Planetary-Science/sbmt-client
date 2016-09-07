package edu.jhuapl.near.pick;

import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.StructuresPickManager;
import edu.jhuapl.saavtk.popup.PopupManager;

public class LidarPickManager extends StructuresPickManager
{
    public LidarPickManager(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        super(renderer, statusBar, modelManager, popupManager);

        LidarSearchDataCollection lidarModel = (LidarSearchDataCollection) modelManager.getModel(ModelNames.LIDAR_SEARCH);
        if (lidarModel != null)
            getNonDefaultPickers().put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(renderer, modelManager, lidarModel));
        lidarModel = (LidarSearchDataCollection) modelManager.getModel(ModelNames.TRACKS);
        if (lidarModel != null)
            getNonDefaultPickers().put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(renderer, modelManager, lidarModel));

        // added by Mike Z 2016-Jul-22
        lidarModel = (LidarSearchDataCollection) modelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
        if (lidarModel != null)
            getNonDefaultPickers().put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(renderer, modelManager, lidarModel));
    }
}