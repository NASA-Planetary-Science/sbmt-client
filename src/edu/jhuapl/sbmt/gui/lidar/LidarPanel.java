package edu.jhuapl.sbmt.gui.lidar;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.image.Instrument;


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
        if (smallBodyConfig.hasHypertreeBasedLidarSearch && smallBodyConfig.lidarInstrumentName.equals(Instrument.MOLA))
            lidarSearchPanel=new MOLALidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
        else if (smallBodyConfig.lidarInstrumentName.equals(Instrument.OLA))
            lidarSearchPanel=new OLALidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
        else
            lidarSearchPanel=new LidarSearchPanel(smallBodyConfig, modelManager, pickManager, renderer);

        addTab("Browse", lidarBrowsePanel);
        addTab("Search", lidarSearchPanel);

    }
}
