package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.spectrum.controllers.standard.SpectrumResultsTableController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;

/**
 * Controller for the Custom Spectrum Results Table Controller.  This needs to get updated with the new table UI from Nobes.
 * @author steelrj1
 *
 */
public class CustomSpectrumResultsTableController<S extends BasicSpectrum>
        extends SpectrumResultsTableController<S>
{
    private List<CustomSpectrumKeyInterface> results;
    private CustomSpectraSearchModel<S> model;
    private String customDataFolder;

    public CustomSpectrumResultsTableController(BasicSpectrumInstrument instrument,
            SpectraCollection<S> spectrumCollection, ModelManager modelManager, SpectrumBoundaryCollection boundaries, CustomSpectraSearchModel<S> model,
            Renderer renderer, SbmtInfoWindowManager infoPanelManager)
    {
        super(instrument, spectrumCollection, modelManager, boundaries, model, renderer,
                infoPanelManager);
        this.model = model;
        this.results = model.getCustomSpectra();
        this.customDataFolder = modelManager.getPolyhedralModel().getCustomDataFolder();
    }

    @Override
    public void setSpectrumResultsPanel()
    {
        super.setSpectrumResultsPanel();

//        super.setSpectrumResultsPanel();
//        panel.getResultList().getModel().removeTableModelListener(tableModelListener);
//        panel.getResultList().getModel().addTableModelListener(tableModelListener);

//        panel.getRemoveSpectraButton().removeActionListener(panel.getRemoveSpectraButton().getActionListeners()[0]);
//        panel.getRemoveSpectraButton().addActionListener(e -> removeFootprintsForAllInstrumentsButtonActionPerformed());

        try
        {
            model.initializeSpecList();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    private CustomSpectrumKeyInterface getConvertedKey(CustomSpectrumKeyInterface key)
//    {
//        CustomSpectrumKeyInterface info;
//		String expandedNameString = SafeURLPaths.instance().getString(getCustomDataFolder() + File.separator + key.getSpectrumFilename());
//        info = new CustomSpectrumKey(expandedNameString, key.getFileType(), instrument, key.getSpectrumType(), SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getSpectrumFilename()), key.getPointingFilename());
//        return info;
//    }

    public String getCustomDataFolder()
    {
        return customDataFolder;
    }

}
