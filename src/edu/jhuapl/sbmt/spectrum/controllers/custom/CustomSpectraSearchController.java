package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.google.common.collect.ImmutableSet;

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
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.search.SpectrumSearchPanel;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * Controller class for the Custom Search UI.  In charge of starting up the appropriate models and sub panels required for the UI.
 * @author steelrj1
 *
 */
public class CustomSpectraSearchController<S extends BasicSpectrum>
{
    private SpectrumSearchPanel panel;
    private CustomSpectrumResultsTableController<S> spectrumResultsTableController;
    private CustomSpectraControlController<S> searchParametersController;
    private SpectrumColoringController<S> coloringController;
    private CustomSpectraSearchModel<S> spectrumSearchModel;


    public CustomSpectraSearchController(ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, BasicSpectrumInstrument instrument)
    {
        this.spectrumSearchModel =  new CustomSpectraSearchModel<S>(modelManager, instrument);
        this.spectrumSearchModel.setCustomDataFolder(modelManager.getPolyhedralModel().getCustomDataFolder());

        SpectraCollection<S> spectrumCollection = (SpectraCollection<S>)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());
        SpectrumBoundaryCollection<S> boundaries = (SpectrumBoundaryCollection<S>)modelManager.getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());

        this.spectrumResultsTableController = new CustomSpectrumResultsTableController<S>(instrument, spectrumCollection, modelManager, boundaries, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumSearchModel.removeAllResultsChangedListeners();
        this.spectrumSearchModel.addResultsChangedListener(new CustomSpectraResultsListener()
        {

            @Override
            public void resultsChanged(List<CustomSpectrumKeyInterface> results)
            {
            	List<S> spectra = new ArrayList<S>();
            	for (CustomSpectrumKeyInterface info : results)
            	{
    				try
					{
    					S spectrum = (S)SbmtSpectrumModelFactory.createSpectrum(modelManager.getPolyhedralModel().getCustomDataFolder() + File.separator + info.getSpectrumFilename(), SpectrumInstrumentFactory.getInstrumentForName(instrument.getDisplayName()));
    					spectrum.isCustomSpectra = true;
    					spectra.add(spectrum);
					}
    				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
                spectrumResultsTableController.setSpectrumResults(spectra);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                spectrumResultsTableController.getPanel().getResultsLabel().setText(spectrumSearchModel.getSpectrumRawResults().size() + " Spectra Found");
            }

			@Override
			public void resultsLoaded(List<CustomSpectrumKeyInterface> results)
			{
				spectrumResultsTableController.setSpectrumResults(spectrumSearchModel.getSpectrumRawResults());
				spectrumCollection.removeAllSpectra();
				boundaries.removeAllBoundaries();

			}
        });
        this.spectrumResultsTableController.setSpectrumResultsPanel();

        this.searchParametersController = new CustomSpectraControlController<S>(spectrumSearchModel);

        this.coloringController = new SpectrumColoringController(spectrumSearchModel, spectrumCollection, instrument.getRGBMaxVals(), instrument.getRGBDefaultIndices());

        spectrumCollection.addListener(new ItemEventListener()
		{

			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				if (aEventType == ItemEventType.ItemsSelected)
				{
					ImmutableSet<S> selectedItems = spectrumCollection.getSelectedItems();
					spectrumSearchModel.setSelectedSpectra(selectedItems);
				}

			}
		});

        init();
    }

    /**
     * Inits the various sub panels and adds them to a parent for eventual display
     */
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
			}
		});
    }

    /**
     * Returns the panel component so it can be included in a container view
     * @return
     */
    public JPanel getPanel()
    {
        return panel;
    }
}
