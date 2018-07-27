package edu.jhuapl.sbmt.gui.spectrum;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSearchPanel;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSearchPanel;
import edu.jhuapl.sbmt.model.spectrum.SpectralInstrument;

/**
 * A tabbed panel with browse and search options
 * @author osheacm1
 *
 */
public class SpectrumPanel extends JTabbedPane
{
    public SpectrumPanel(
            SmallBodyViewConfig smallBodyConfig,
            ModelManager modelManager,
            SbmtInfoWindowManager sbmtInfoWindowManager, PickManager pickManager,
            Renderer renderer, SpectralInstrument instrument)
    {
        setBorder(BorderFactory.createEmptyBorder());

        SpectrumBrowsePanel spectrumBrowsePanel;
        SpectrumSearchController spectrumSearchController;


        if (instrument instanceof edu.jhuapl.sbmt.model.bennu.otes.OTES) {
            spectrumSearchController = new OTESSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument);
//            spectrumBrowsePanel = new OTESBrowsePanel();
//            addTab("Browse", spectrumBrowsePanel.getView());
            addTab("Search", spectrumSearchController.getView());
        }
        else if (instrument instanceof edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS) {
            spectrumSearchController = new OVIRSSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument);
            spectrumBrowsePanel = new OVIRSBrowsePanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument);
            addTab("Browse", spectrumBrowsePanel.getView());
            addTab("Search", spectrumSearchController.getView());
        }


    }
}