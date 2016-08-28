package edu.jhuapl.near.pick;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.popupmenus.PopupManager;

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