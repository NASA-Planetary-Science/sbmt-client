package edu.jhuapl.near.gui;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PolyhedralModelConfig;
import edu.jhuapl.near.pick.PickManager;

/**
 * Panel used for showing custom tracks. It is essentially the LidarSearchPanel but with the search
 * controls removed.
 */
public class TrackPanel extends LidarSearchPanel
{

    public TrackPanel(PolyhedralModelConfig polyhedralModelConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(polyhedralModelConfig, modelManager, pickManager, renderer);
        hideSearchControls();
    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.TRACKS;
    }
}
