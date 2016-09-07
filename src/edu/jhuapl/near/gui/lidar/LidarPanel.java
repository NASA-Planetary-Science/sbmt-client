package edu.jhuapl.near.gui.lidar;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.app.SmallBodyViewConfig;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;


public class LidarPanel extends JTabbedPane
{
    public LidarPanel(
            SmallBodyViewConfig smallBodyConfig,
            ModelManager modelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        setBorder(BorderFactory.createEmptyBorder());

        LidarBrowsePanel lidarBrowsePanel = new LidarBrowsePanel(modelManager);
        LidarSearchPanel lidarSearchPanel;
        if (smallBodyConfig.hasHypertreeBasedLidarSearch)
            lidarSearchPanel=new OLALidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
        else
            lidarSearchPanel=new LidarSearchPanel(smallBodyConfig, modelManager, pickManager, renderer);

        addTab("Browse", lidarBrowsePanel);
        addTab("Search", lidarSearchPanel);

    }
}
