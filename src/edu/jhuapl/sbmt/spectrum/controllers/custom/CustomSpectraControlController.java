package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.ui.custom.CustomSpectraControlPanel;
import edu.jhuapl.sbmt.spectrum.ui.custom.CustomSpectrumImporterDialog;


/**
 * Controller class for the Custom Spectrum Control UI.  Manages interface between the CustomSpectraControlPanel and the CustomSpectraSearchModel
 * @author steelrj1
 *
 */
public class CustomSpectraControlController<S extends BasicSpectrum>
{
    private CustomSpectraControlPanel panel;
    private CustomSpectraSearchModel<S> model;
    private List<CustomSpectrumKeyInterface> customSpectra;
    private SpectraHierarchicalSearchSpecification<SpectrumSearchSpec> spectraSpec;

    /**
     * @param model	The custom spectra model
     */
    public CustomSpectraControlController(CustomSpectraSearchModel<S> model, SpectraHierarchicalSearchSpecification<SpectrumSearchSpec> spectraSpec)
    {
        panel = new CustomSpectraControlPanel();
        this.model = model;
        this.customSpectra = model.getCustomSpectra();
        this.spectraSpec = spectraSpec;
        init();
    }

    /**
     * Returns the panel component so it can be displayed in a parent container
     * @return
     */
    public CustomSpectraControlPanel getPanel()
    {
        return panel;
    }

    /**
     * Private init methods.  Establishes actions for the new and edit buttons
     */
    private void init()
    {
        panel.getNewButton().addActionListener(e -> newButtonActionPerformed());
        panel.getEditButton().addActionListener(e -> editButtonActionPerformed());
        panel.getDeleteButton().addActionListener(e -> deleteButtonActionPerformed());
    }

    /**
     * Handles the press of the "New" button in the dialog.  Prompts the user for information related to a custom spectra via the CustomSpectrumImporterDialog, and if
     * successful, adds the new custom spectrum to the model
     * @param evt
     */
    private void newButtonActionPerformed()
    {
        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(
                null, false, model.getInstrument(), model.getCustomDataFolder(), spectraSpec);
        dialog.setSpectrumInfo(null);
        dialog.setLocationRelativeTo(getPanel());
        dialog.setVisible(true);
        if (!dialog.getOkayPressed()) return;
        // If user clicks okay add to list
        CustomSpectrumKeyInterface spectrumKey = dialog.getSpectrumInfo();
        try
        {
            saveSpectrum(model.getSpectrumRawResults().size(), null, spectrumKey);
        }
        catch (IOException e)
        {
        	JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error importing the spectrum.  See the console for details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    /**
     * Handles the press of the "Edit" button in the dialog.  Displayed information on the currently selected spectra to the user via CustomSpectrumImporterDialog and if updated,
     * updates the model
     * @param evt
     */
    private void editButtonActionPerformed()
    {
    	if (model.getSelectedSpectraIndices() == null || model.getSelectedSpectraIndices().length == 0) return;
    	int selectedItem = model.getSelectedSpectraIndices()[0];
    	if (selectedItem < 0) return;

        CustomSpectrumKeyInterface oldSpectrumInfo = customSpectra.get(selectedItem);

        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, true, model.getInstrument(), model.getCustomDataFolder(), spectraSpec);
        dialog.setSpectrumInfo(oldSpectrumInfo);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (!dialog.getOkayPressed()) return;
        // If user clicks okay replace item in list
        CustomSpectrumKeyInterface newSpectrumInfo = dialog.getSpectrumInfo();
        try
        {
            saveSpectrum(selectedItem, oldSpectrumInfo, newSpectrumInfo);
        }
        catch (IOException e)
        {
        	JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error editingthe spectrum.  See the console for details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Handles the press of the "Delete" button in the dialog.  Prompts to confirm, and deletes the spectrum from the model if the user says yes.
     * @param evt
     */
    private void deleteButtonActionPerformed()
    {
    	int[] selectedSpectra = model.getSelectedSpectraIndices();
    	if (selectedSpectra == null || selectedSpectra.length == 0) return;

		String infoMsg = "Are you sure you want to delete this spectra?";
		int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(panel), infoMsg, "Confirm Deletion",
				JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.NO_OPTION)
			return;

    	model.deleteSpectrum(selectedSpectra);
    }

    /**
     * Helper method to save a spectrum to the model.  Allows saving a new spectrum (<pre>newImageInfo</pre> is null), or updating an existing one (</pre>newImageInfo</p> is not null)
     * @param index
     * @param oldImageInfo
     * @param newImageInfo
     * @throws IOException
     */
    private void saveSpectrum(int index, CustomSpectrumKeyInterface oldImageInfo,
            CustomSpectrumKeyInterface newImageInfo) throws IOException
    {
        model.saveSpectrum(index, oldImageInfo, newImageInfo);
    }
}