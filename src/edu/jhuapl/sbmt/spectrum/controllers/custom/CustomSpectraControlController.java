package edu.jhuapl.sbmt.spectrum.controllers.custom;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.search.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.ui.custom.CustomSpectraControlPanel;
import edu.jhuapl.sbmt.spectrum.ui.custom.CustomSpectrumImporterDialog;


/**
 * Controller class for the Custom Spectrum Control UI.  Manages interface between the CustomSpectraControlPanel and the CustomSpectraSearchModel
 * @author steelrj1
 *
 */
public class CustomSpectraControlController
{
    private CustomSpectraControlPanel panel;
    private CustomSpectraSearchModel model;
    private List<CustomSpectrumKeyInterface> customSpectra;

    public CustomSpectraControlController(CustomSpectraSearchModel model)
    {
        panel = new CustomSpectraControlPanel();
        this.model = model;
        this.customSpectra = model.getCustomSpectra();
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
    }

    /**
     * Handles the press of the "New" button in the dialog.  Prompts the user for information related to a custom spectra via the CustomSpectrumImporterDialog, and if
     * successful, adds the new custom spectrum to the model
     * @param evt
     */
    private void newButtonActionPerformed()
    {
        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(
                null, false, model.getInstrument());
        dialog.setSpectrumInfo(null);
        dialog.setLocationRelativeTo(getPanel());
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            CustomSpectrumKeyInterface spectrumKey = dialog.getSpectrumInfo();
            try
            {
                saveSpectrum(model.getSpectrumRawResults().size(), null, spectrumKey);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the press of the "Edit" button in the dialog.  Displayed information on the currently selected spectra to the user via CustomSpectrumImporterDialog and if updated,
     * updates the model
     * @param evt
     */
    private void editButtonActionPerformed()
    {
    	int selectedItem = model.getSelectedImageIndex()[0];
    	if (selectedItem < 0) return;

        CustomSpectrumKeyInterface oldSpectrumInfo = customSpectra.get(selectedItem);

        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(null, true, model.getInstrument());
        dialog.setSpectrumInfo(oldSpectrumInfo);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        // If user clicks okay replace item in list
        if (dialog.getOkayPressed())
        {
            CustomSpectrumKeyInterface newSpectrumInfo = dialog.getSpectrumInfo();
            try
            {
                saveSpectrum(selectedItem, oldSpectrumInfo, newSpectrumInfo);
                //TODO is this needed?
//                    model.remapSpectrumToRenderer(selectedItem);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

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