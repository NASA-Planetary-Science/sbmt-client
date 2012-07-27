package edu.jhuapl.near.gui.eros;

import edu.jhuapl.near.gui.LidarBrowsePanel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;


public class NLRBrowsePanel extends LidarBrowsePanel
{
    public NLRBrowsePanel(ModelManager modelManager)
    {
        super(modelManager);
    }

    protected String getModelName()
    {
        return ModelNames.LIDAR_BROWSE;
    }
}
