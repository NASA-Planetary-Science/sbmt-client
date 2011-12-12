package edu.jhuapl.near.gui.itokawa;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.pick.PickManager;

public class HayLidarUnfilteredSearchPanel extends HayLidarSearchPanel
{
    public HayLidarUnfilteredSearchPanel(ModelManager modelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        super(modelManager, pickManager, renderer);
    }

    @Override
    protected String getLidarModelName()
    {
        return ModelNames.HAYLIDAR_SEARCH_UNFILTERED;
    }
}
