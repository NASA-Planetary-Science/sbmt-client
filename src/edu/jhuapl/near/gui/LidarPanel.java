package edu.jhuapl.near.gui;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;


public class LidarPanel extends JTabbedPane
{
    public LidarPanel(
            SmallBodyConfig smallBodyConfig,
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
