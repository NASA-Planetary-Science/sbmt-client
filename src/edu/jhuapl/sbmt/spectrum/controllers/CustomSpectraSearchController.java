package edu.jhuapl.sbmt.spectrum.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.spectrum.model.core.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.model.instruments.SpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.key.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumSearchPanel;

public class CustomSpectraSearchController
{
    private ISpectrumSearchModel model;
    private SpectrumSearchPanel panel;
    protected SpectralInstrument instrument;
    protected ModelManager modelManager;
    protected Renderer renderer;
    private CustomSpectrumResultsTableController spectrumResultsTableController;
    private CustomSpectraControlController searchParametersController;
    private SpectrumColoringController coloringController;
    private CustomSpectraSearchModel spectrumSearchModel;


    public CustomSpectraSearchController(boolean hasHierarchicalSpectraSearch,
    		boolean hasHypertreeBasedSpectraSearch,
    		SpectraHierarchicalSearchSpecification<?> hierarchicalSpectraSearchSpecification, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, ISpectralInstrument instrument)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        this.spectrumSearchModel =  new CustomSpectraSearchModel(hasHierarchicalSpectraSearch, hasHypertreeBasedSpectraSearch, hierarchicalSpectraSearchSpecification, modelManager, pickManager, renderer, instrument);

        this.spectrumSearchModel.loadSearchSpecMetadata();

        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());

        this.spectrumResultsTableController = new CustomSpectrumResultsTableController(instrument, spectrumCollection, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumSearchModel.removeAllResultsChangedListeners();
        this.spectrumSearchModel.addResultsChangedListener(new CustomSpectraResultsListener()
        {

            @Override
            public void resultsChanged(List<CustomSpectrumKeyInterface> results)
            {
                List<List<String>> formattedResults = new ArrayList<List<String>>();
                for (CustomSpectrumKeyInterface info : results)
                {
                    List<String> res = new ArrayList<String>();
                    res.add(info.getSpectrumFilename());
                    res.add(""+0); //TODO need time here
                    res.add(info.getName());
                    formattedResults.add(res);
                }

                spectrumResultsTableController.setSpectrumResults(formattedResults);
                spectrumSearchModel.updateColoring();
            }

            @Override
            public void resultsCountChanged(int count)
            {
                spectrumResultsTableController.getPanel().getResultsLabel().setText(spectrumSearchModel.getSpectrumRawResults().size() + " Spectra Found");
            }
        });
        this.spectrumResultsTableController.setSpectrumResultsPanel();


        this.searchParametersController = new CustomSpectraControlController(spectrumSearchModel);
//        this.searchParametersController.setupSearchParametersPanel();

        this.coloringController = new SpectrumColoringController(spectrumSearchModel);

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
