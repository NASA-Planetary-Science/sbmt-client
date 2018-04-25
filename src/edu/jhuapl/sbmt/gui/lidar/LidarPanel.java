package edu.jhuapl.sbmt.gui.lidar;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.lidar.v2.LidarSearchController;
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
        LidarSearchController lidarSearchController;
        if (smallBodyConfig.lidarInstrumentName.equals(Instrument.MOLA))
        {
            // Search isn't working, so disable it for now. Leave remaining stanzas intact to make it
            // easy to re-enable by just removing this top clause.
//            lidarSearchPanel=new MolaLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
            lidarBrowsePanel = new LidarBrowsePanel(modelManager);
            addTab("Browse", lidarBrowsePanel);
//            addTab("Search", lidarSearchPanel);
        }
        else if (smallBodyConfig.hasHypertreeBasedLidarSearch && smallBodyConfig.lidarInstrumentName.equals(Instrument.MOLA))
        {
            lidarSearchController=new MolaLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
            lidarBrowsePanel = new LidarBrowsePanel(modelManager);
            addTab("Browse", lidarBrowsePanel);
            addTab("Search", lidarSearchController.getView());
        }
        else if (smallBodyConfig.hasHypertreeBasedLidarSearch && smallBodyConfig.lidarInstrumentName.equals(Instrument.LASER))
        {
//            lidarSearchPanel=new LaserLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer);
            lidarBrowsePanel = new LaserLidarBrowsePanel(modelManager, smallBodyConfig);
            addTab("Browse", lidarBrowsePanel);
//            addTab("Search", lidarSearchPanel);
        }
        else if (smallBodyConfig.lidarInstrumentName.equals(Instrument.OLA))
        {
            lidarBrowsePanel = new OlaLidarBrowsePanel(modelManager, smallBodyConfig);
            lidarSearchController=new OlaLidarHyperTreeSearchPanel(smallBodyConfig,modelManager,pickManager,renderer,(OlaLidarBrowsePanel)lidarBrowsePanel);
            addTab("Browse", lidarBrowsePanel);
            addTab("Search", lidarSearchController.getView());
        }
        else
        {
//            lidarSearchPanel=new LidarSearchPanel(smallBodyConfig, modelManager, pickManager, renderer);
            lidarSearchController=new LidarSearchController(smallBodyConfig, modelManager, pickManager, renderer);
            lidarBrowsePanel = new LidarBrowsePanel(modelManager);
            addTab("Browse", lidarBrowsePanel);
            addTab("Search", lidarSearchController.getView());
        }


    }
}
