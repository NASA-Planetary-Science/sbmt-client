package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
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
            SpectraCollection<S> spectrumCollection, ModelManager modelManager, SpectrumBoundaryCollection<S> boundaries, CustomSpectraSearchModel<S> model,
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

    public String getCustomDataFolder()
    {
        return customDataFolder;
    }


    //Handles the load and saving of custom spectra.  This is different from the normal model since custom spectra are tracked via an underlying metadata file
    /**
     * Handles action for Load Spectrum button. On an exception, displays an error dialog to the user
     */
    @Override
    protected void loadSpectrumListButtonActionPerformed()
    {
    	 try
         {
    		 File file = CustomFileChooser.showOpenDialog(null, "Select File");
    	     if (file == null) return;
             model.loadSpectrumListFromFile(file);
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                     "There was an error reading the file.",
                     "Error",
                     JOptionPane.ERROR_MESSAGE);

             e.printStackTrace();
         }
    }

    /**
     * Handles action for Save Spectrum button. On an exception, displays an error dialog to the user
     */
    @Override
    protected void saveSpectrumListButtonActionPerformed()
    {
        try
        {
        	File file = CustomFileChooser.showSaveDialog(panel, "Select File", "spectrumlist.txt");
            model.saveSpectrumListToFile(file);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error saving the file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
    }

    /**
     * Handles action for Save Selected Spectrum button. On an exception, displays an error dialog to the user
     */
    @Override
    protected void saveSelectedSpectrumListButtonActionPerformed()
    {
        try
        {
        	File file = CustomFileChooser.showSaveDialog(panel, "Select File", "spectrumlist.txt");
            model.saveSelectedSpectrumListToFile(file, panel.getResultList().getSelectedRows());
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error saving the file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
    }


}
