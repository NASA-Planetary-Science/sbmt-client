package edu.jhuapl.near.gui;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.pick.PickManager;

public class TrackPanel extends LidarSearchPanel
{

    public TrackPanel(SmallBodyConfig smallBodyConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(smallBodyConfig, modelManager, pickManager, renderer);
        enableTrackMode();
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.TRACKS;
    }
}
