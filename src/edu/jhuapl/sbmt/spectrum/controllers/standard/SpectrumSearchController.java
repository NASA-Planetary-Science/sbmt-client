package edu.jhuapl.sbmt.spectrum.controllers.standard;

import java.util.Date;

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.search.BaseSpectrumSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.search.SpectrumSearchPanel;

/**
 * Spectrum search controller for OREx.  Contains elements such as the search panel, results table and coloring panel
 * @author steelrj1
 *
 * @param <S>
 */
public class SpectrumSearchController<S extends BasicSpectrum>
{
    private SpectrumSearchPanel panel;
    protected SpectrumResultsTableController spectrumResultsTableController;
    private SpectrumSearchParametersController searchParametersController;
    private SpectrumColoringController coloringController;
    private SpectraCollection spectrumCollection;
    private BasicSpectrumInstrument instrument;

    public SpectrumSearchController(Date imageSearchDefaultStartDate, Date imageSearchDefaultEndDate,
    		boolean hasHierarchicalSpectraSearch, double imageSearchDefaultMaxSpacecraftDistance,
    		SpectraHierarchicalSearchSpecification spectraSpec,
    		ModelManager modelManager,
    		SbmtSpectrumWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, BasicSpectrumInstrument instrument, BaseSpectrumSearchModel model, SpectrumInstrumentConfig spectrumConfig)
    {
    	this.instrument = instrument;
        this.spectrumCollection = (SpectraCollection)modelManager.getModel(model.getSpectrumCollectionModelName());
        SpectrumBoundaryCollection boundaryCollection = (SpectrumBoundaryCollection)modelManager.getModel(model.getSpectrumBoundaryCollectionModelName());

        this.spectrumResultsTableController = new SpectrumResultsTableController(instrument, spectrumCollection, modelManager, boundaryCollection, model, renderer, infoPanelManager, spectrumConfig);
        this.spectrumResultsTableController.setSpectrumResultsPanel();

        this.searchParametersController = new SpectrumSearchParametersController(imageSearchDefaultStartDate, imageSearchDefaultEndDate, new String[] {}, hasHierarchicalSpectraSearch, false, imageSearchDefaultMaxSpacecraftDistance, spectraSpec, model, pickManager, modelManager);
        if (instrument.getQueryBase() instanceof FixedListDataQuery && !(hasHierarchicalSpectraSearch))
        	this.searchParametersController.setFixedListSearch(true);
        this.searchParametersController.setupSearchParametersPanel();

        this.coloringController = new SpectrumColoringController(model, spectrumCollection, instrument.getRGBMaxVals(), instrument.getRGBDefaultIndices());

        init();
    }

    public void init()
    {
        panel = new SpectrumSearchPanel();
        panel.addSubPanel(searchParametersController.getPanel());
        panel.addSubPanel(spectrumResultsTableController.getPanel());
        panel.addSubPanel(coloringController.getPanel());

        panel.addAncestorListener(new AncestorListener()
		{

			@Override
			public void ancestorRemoved(AncestorEvent event)
			{
				spectrumResultsTableController.removeResultListener();

			}

			@Override
			public void ancestorMoved(AncestorEvent event)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void ancestorAdded(AncestorEvent event)
			{
				spectrumResultsTableController.addResultListener();
				spectrumCollection.setActiveInstrument(instrument);
			}
		});

    }

    public JPanel getPanel()
    {
        return panel;
    }
}
