package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog.SpectrumInfo;
import edu.jhuapl.sbmt.gui.spectrum.model.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.gui.spectrum.model.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.model.ISpectrumSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.ui.SpectrumSearchPanel;
import edu.jhuapl.sbmt.model.spectrum.SpectraCollection;
import edu.jhuapl.sbmt.model.spectrum.instruments.SpectralInstrument;

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


    public CustomSpectraSearchController(ISmallBodyViewConfig smallBodyConfig, ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, SpectralInstrument instrument, CustomSpectraSearchModel model)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        this.spectrumSearchModel = model;
        this.spectrumSearchModel.loadSearchSpecMetadata();

        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());

        this.spectrumResultsTableController = new CustomSpectrumResultsTableController(instrument, spectrumCollection, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumSearchModel.removeAllResultsChangedListeners();
        this.spectrumSearchModel.addResultsChangedListener(new CustomSpectraResultsListener()
        {

            @Override
            public void resultsChanged(List<SpectrumInfo> results)
            {
                List<List<String>> formattedResults = new ArrayList<List<String>>();
                for (SpectrumInfo info : results)
                {
                    List<String> res = new ArrayList<String>();
                    res.add(info.spectrumfilename);
                    res.add(""+0); //TODO need time here
                    res.add(info.name);
                    formattedResults.add(res);
                }

                spectrumResultsTableController.setSpectrumResults(formattedResults);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                spectrumResultsTableController.getPanel().getResultsLabel().setText(spectrumSearchModel.getSpectrumRawResults().size() + " Spectra Found");
            }
        });
        this.spectrumResultsTableController.setSpectrumResultsPanel();


        this.searchParametersController = new CustomSpectraControlController(model);
//        this.searchParametersController.setupSearchParametersPanel();

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
