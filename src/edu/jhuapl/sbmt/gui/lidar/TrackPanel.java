package edu.jhuapl.sbmt.gui.lidar;

import edu.jhuapl.saavtk.gui.renderer.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.BodyViewConfig;

/**
 * Panel used for showing custom tracks. It is essentially the LidarSearchPanel but with the search
 * controls removed.
 */
public class TrackPanel extends LidarSearchPanel
{

    public TrackPanel(BodyViewConfig polyhedralModelConfig,
            ModelManager modelManager, PickManager pickManager,
            Renderer renderer)
    {
        super(polyhedralModelConfig, modelManager, pickManager, renderer);
        hideSearchControls();

        if (polyhedralModelConfig.getUniqueName().equals("Gaskell/Bennu (V3 Image)"));
            fileTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OLA Level 2", "Text" }));

    }

    @Override
    protected ModelNames getLidarModelName()
    {
        return ModelNames.TRACKS;
    }
}
