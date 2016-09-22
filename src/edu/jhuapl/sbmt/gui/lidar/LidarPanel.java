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

        LidarBrowsePanel lidarBrowsePanel;
        LidarSearchPanel lidarSearchPanel;
        if (smallBodyConfig.hasHypertreeBasedLidarSearch && smallBodyConfig.lidarInstrumentName.equals(Instrument.MOLA))
        {
            lidarSearchPanel=new MolaLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
            lidarBrowsePanel = new LidarBrowsePanel(modelManager);
        }
        else if (smallBodyConfig.lidarInstrumentName.equals(Instrument.OLA))
        {
            lidarBrowsePanel = new OlaLidarBrowsePanel(modelManager, smallBodyConfig);
            lidarSearchPanel=new OlaLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer,(OlaLidarBrowsePanel)lidarBrowsePanel);
        }
        else
        {
            lidarSearchPanel=new LidarSearchPanel(smallBodyConfig, modelManager, pickManager, renderer);
            lidarBrowsePanel = new LidarBrowsePanel(modelManager);
        }

        addTab("Browse", lidarBrowsePanel);
        addTab("Search", lidarSearchPanel);

    }
}
