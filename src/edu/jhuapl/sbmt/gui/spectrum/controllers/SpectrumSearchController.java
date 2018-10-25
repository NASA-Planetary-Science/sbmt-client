package edu.jhuapl.sbmt.gui.spectrum.controllers;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.model.SpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.ui.SpectrumSearchPanel;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

public class SpectrumSearchController
{
    private ISpectrumSearchModel model;
    private SpectrumSearchPanel panel;
    protected SpectralInstrument instrument;
    protected ModelManager modelManager;
    protected Renderer renderer;
    private SpectrumResultsTableController spectrumResultsTableController;
    private SpectrumSearchParametersController searchParametersController;
    private SpectrumColoringController coloringController;
    private SpectrumSearchModel spectrumSearchModel;


    public SpectrumSearchController(SmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, SpectralInstrument instrument, SpectrumSearchModel model)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        this.spectrumSearchModel = model;
        this.spectrumSearchModel.loadSearchSpecMetadata();

        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());

        this.spectrumResultsTableController = new SpectrumResultsTableController(instrument, spectrumCollection, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumResultsTableController.setSpectrumResultsPanel();

        this.searchParametersController = new SpectrumSearchParametersController(spectrumSearchModel, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        this.coloringController = new SpectrumColoringController(model);


//        if (spectraSpec.getInstrumentMetadata(instrument.getDisplayName()).getQueryType().equals("file"))
//        {
//            searchParametersController.getPanel().setVisible(false);
//        }

        init();
    }

    public void init()
    {
        panel = new SpectrumSearchPanel();
        panel.addSubPanel(searchParametersController.getPanel());
        panel.addSubPanel(spectrumResultsTableController.getPanel());
        panel.addSubPanel(coloringController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
