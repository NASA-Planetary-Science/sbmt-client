package edu.jhuapl.near.gui.itokawa;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;


public class HayLidarPanel extends JTabbedPane
{
    public HayLidarPanel(
            final ModelManager modelManager,
            final PickManager pickManager,
            Renderer renderer)
    {
        setBorder(BorderFactory.createEmptyBorder());

        HayLidarBrowsePanel lidarBrowsePanel = new HayLidarBrowsePanel(modelManager);
        HayLidarSearchPanel lidarSearchPanel = new HayLidarSearchPanel(modelManager, pickManager, renderer);
        HayLidarSearchPanel lidarSearchPanelUnfiltered = new HayLidarUnfilteredSearchPanel(modelManager, pickManager, renderer);

        addTab("Browse", lidarBrowsePanel);
        addTab("Search (optimized)", lidarSearchPanel);
        addTab("Search (unfiltered)", lidarSearchPanelUnfiltered);
    }
}
