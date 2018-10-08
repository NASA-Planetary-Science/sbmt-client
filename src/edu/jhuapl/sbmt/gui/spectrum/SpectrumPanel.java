package edu.jhuapl.sbmt.gui.spectrum;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.spectrum.model.OTESSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.model.OVIRSSearchModel;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSearchPanel;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSearchPanel;
import edu.jhuapl.sbmt.model.spectrum.SpectraType;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

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

        SpectrumSearchController spectrumSearchController;
//        SpectrumSearchController spectrumBrowseController;
        if (instrument.getDisplayName().equals(SpectraType.OTES_SPECTRA.getDisplayName())) {
            spectrumSearchController = new OTESSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, true);

            OTESSearchModel model = new OTESSearchModel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument);

            JComponent component = new edu.jhuapl.sbmt.gui.spectrum.controllers.SpectrumSearchController(
                    smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, model).getPanel();



//            spectrumBrowseController = new OTESSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, false);
            addTab("Browse", component);
            addTab("Search", spectrumSearchController.getView());
        }
        else if (instrument.getDisplayName().equals(SpectraType.OVIRS_SPECTRA.getDisplayName())) {
            spectrumSearchController = new OVIRSSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, true);
//            spectrumBrowseController = new OVIRSSearchPanel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, false);
            OVIRSSearchModel model = new OVIRSSearchModel(smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument);

            JComponent component = new edu.jhuapl.sbmt.gui.spectrum.controllers.SpectrumSearchController(
                    smallBodyConfig, modelManager, sbmtInfoWindowManager, pickManager, renderer, instrument, model).getPanel();
            addTab("Browse", component);
            addTab("Search", spectrumSearchController.getView());
        }


    }
}