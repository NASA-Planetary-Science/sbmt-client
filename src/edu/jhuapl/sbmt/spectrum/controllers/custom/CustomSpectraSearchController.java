package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.controllers.standard.SpectrumColoringController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.instruments.SpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.search.SpectrumSearchPanel;

public class CustomSpectraSearchController
{
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
            PickManager pickManager, Renderer renderer, BasicSpectrumInstrument instrument)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        this.spectrumSearchModel =  new CustomSpectraSearchModel(hasHierarchicalSpectraSearch, hasHypertreeBasedSpectraSearch, hierarchicalSpectraSearchSpecification, modelManager, instrument);
        this.spectrumSearchModel.setCustomDataFolder(modelManager.getPolyhedralModel().getCustomDataFolder());

        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());
        SpectrumBoundaryCollection boundaries = (SpectrumBoundaryCollection)modelManager.getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());

        this.spectrumResultsTableController = new CustomSpectrumResultsTableController(instrument, spectrumCollection, modelManager, boundaries, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumSearchModel.removeAllResultsChangedListeners();
        this.spectrumSearchModel.addResultsChangedListener(new CustomSpectraResultsListener()
        {

            @Override
            public void resultsChanged(List<CustomSpectrumKeyInterface> results)
            {
                List<BasicSpectrum> formattedResults = new ArrayList<BasicSpectrum>();
                for (CustomSpectrumKeyInterface info : results)
                {
                	IBasicSpectrumRenderer renderer = null;
        			try
        			{
        				renderer = SbmtSpectrumModelFactory.createSpectrumRenderer(modelManager.getPolyhedralModel().getCustomDataFolder() + File.separator + info.getSpectrumFilename(), SpectrumInstrumentFactory.getInstrumentForName(instrument.getDisplayName()));
        			}
        			catch (IOException e)
        			{
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}

                    formattedResults.add(renderer.getSpectrum());
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

        this.coloringController = new SpectrumColoringController(spectrumSearchModel, spectrumCollection);
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
