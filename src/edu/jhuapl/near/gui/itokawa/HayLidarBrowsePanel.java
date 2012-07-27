package edu.jhuapl.near.gui.itokawa;

import edu.jhuapl.near.gui.LidarBrowsePanel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;


public class HayLidarBrowsePanel extends LidarBrowsePanel
{
    public HayLidarBrowsePanel(ModelManager modelManager)
    {
        super(modelManager);
    }

    protected String getModelName()
    {
        return ModelNames.LIDAR_BROWSE;
    }
}
