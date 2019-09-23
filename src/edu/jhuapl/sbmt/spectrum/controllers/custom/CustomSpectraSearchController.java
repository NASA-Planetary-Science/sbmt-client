package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.spectrum.controllers.standard.SpectrumColoringController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.CustomSpectraResultsListener;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.search.SpectrumSearchPanel;

/**
 * Controller class for the Custom Search UI.  In charge of starting up the appropriate models and sub panels required for the UI.
 * @author steelrj1
 *
 */
public class CustomSpectraSearchController
{
    private SpectrumSearchPanel panel;
    private CustomSpectrumResultsTableController spectrumResultsTableController;
    private CustomSpectraControlController searchParametersController;
    private SpectrumColoringController coloringController;
    private CustomSpectraSearchModel spectrumSearchModel;


    public CustomSpectraSearchController(ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            PickManager pickManager, Renderer renderer, BasicSpectrumInstrument instrument)
    {
        this.spectrumSearchModel =  new CustomSpectraSearchModel(modelManager, instrument);
        this.spectrumSearchModel.setCustomDataFolder(modelManager.getPolyhedralModel().getCustomDataFolder());

        SpectraCollection spectrumCollection = (SpectraCollection)modelManager.getModel(spectrumSearchModel.getSpectrumCollectionModelName());
        SpectrumBoundaryCollection boundaries = (SpectrumBoundaryCollection)modelManager.getModel(spectrumSearchModel.getSpectrumBoundaryCollectionModelName());

        this.spectrumResultsTableController = new CustomSpectrumResultsTableController(instrument, spectrumCollection, modelManager, boundaries, spectrumSearchModel, renderer, infoPanelManager);
        this.spectrumSearchModel.removeAllResultsChangedListeners();
        this.spectrumSearchModel.addResultsChangedListener(new CustomSpectraResultsListener()
        {

            @Override
            public void resultsChanged(List<BasicSpectrum> results)
            {
                spectrumResultsTableController.setSpectrumResults(results);
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
