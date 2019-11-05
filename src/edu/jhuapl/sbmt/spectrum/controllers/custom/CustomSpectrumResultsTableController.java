package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.spectrum.controllers.standard.SpectrumResultsTableController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

/**
 * Controller for the Custom Spectrum Results Table Controller.
 * @author steelrj1
 *
 */
public class CustomSpectrumResultsTableController<S extends BasicSpectrum>
        extends SpectrumResultsTableController<S>
{
    private CustomSpectraSearchModel<S> model;
    private String customDataFolder;

    /**
     * Controller for the custom spectrum table
     *
     * @param instrument			The spectrum instrument
     * @param spectrumCollection	The Custom spectrum collection
     * @param modelManager			The system model manager
     * @param boundaries			The custom spectrum boundaries collection
     * @param model					The custom spectrum model
     * @param renderer				The renderer
     * @param infoPanelManager
     */
    public CustomSpectrumResultsTableController(BasicSpectrumInstrument instrument,
            SpectraCollection<S> spectrumCollection, ModelManager modelManager, SpectrumBoundaryCollection<S> boundaries, CustomSpectraSearchModel<S> model,
            Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        super(instrument, spectrumCollection, modelManager, boundaries, model, renderer,
                infoPanelManager);
        this.model = model;
        this.customDataFolder = modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    /**
     *	Initializes the spectrum results panel
     */
    @Override
    public void setSpectrumResultsPanel()
    {
        super.setSpectrumResultsPanel();

        try
        {
            model.initializeSpecList();
        }
        catch (IOException e)
        {
        	JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error importing the spectra list from the cache.  See the console for details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Returns the custom data folder for the model
     * @return
     */
    public String getCustomDataFolder()
    {
        return customDataFolder;
    }

    /**
     * Sets the list of spectra to display on the table
     * @param results
     */
    @Override
    public void setSpectrumResults(List<S> results)
    {
        panel.getResultsLabel().setText(results.size() + " spectra found");
        spectrumRawResults = results;
        spectrumCollection.setAllItems(spectrumRawResults);
    }
}