package edu.jhuapl.sbmt.gui.lidar;

import java.util.Map;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.DefaultPicker;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;

public class LidarPickManager extends PickManager
{
    public LidarPickManager(
            Renderer renderer,
            PopupManager popupManager,
            ModelManager modelManager,
            DefaultPicker aDefaultPicker)
    {
        super(renderer, popupManager,
                formNonDefaultPickers(renderer, modelManager), aDefaultPicker);
    }

    /**
     * Utility helper method to form the relevant collection of non-default
     * pickers
     */
    public static Map<PickMode, Picker> formNonDefaultPickers(Renderer aRenderer, ModelManager aModelManager)
    {
        Map<PickMode, Picker> retMap = PickUtil.formNonDefaultPickerMap(aRenderer, aModelManager);

        LidarSearchDataCollection lidarModel = (LidarSearchDataCollection) aModelManager.getModel(ModelNames.LIDAR_SEARCH);
        if (lidarModel != null)
            retMap.put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(aRenderer, aModelManager, lidarModel));
        lidarModel = (LidarSearchDataCollection) aModelManager.getModel(ModelNames.TRACKS);
        if (lidarModel != null)
            retMap.put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(aRenderer, aModelManager, lidarModel));

        // added by Mike Z 2016-Jul-22
        lidarModel = (LidarSearchDataCollection) aModelManager.getModel(ModelNames.LIDAR_HYPERTREE_SEARCH);
        if (lidarModel != null)
            retMap.put(PickMode.LIDAR_SHIFT, new LidarShiftPicker(aRenderer, aModelManager, lidarModel));

        return retMap;

    }
}