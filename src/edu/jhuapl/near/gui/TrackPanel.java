package edu.jhuapl.near.gui;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModelConfig;
import edu.jhuapl.saavtk.pick.PickManager;

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
