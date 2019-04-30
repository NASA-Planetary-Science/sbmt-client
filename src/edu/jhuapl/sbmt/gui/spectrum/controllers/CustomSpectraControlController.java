package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog;
import edu.jhuapl.sbmt.gui.spectrum.model.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.ui.CustomSpectraControlPanel;
import edu.jhuapl.sbmt.model.spectrum.CustomSpectrumKeyInterface;


public class CustomSpectraControlController
{
    CustomSpectraControlPanel panel;
    CustomSpectraSearchModel model;
    List<CustomSpectrumKeyInterface> customSpectra;

    public CustomSpectraControlController(CustomSpectraSearchModel model)
    {
        panel = new CustomSpectraControlPanel();
        this.model = model;
        this.customSpectra = model.getcustomSpectra();
        init();
    }

    public CustomSpectraControlPanel getPanel()
    {
        return panel;
    }

    private void init()
    {
        panel.getNewButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                newButtonActionPerformed(e);
            }
        });

        panel.getEditButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editButtonActionPerformed(e);
            }
        });
    }

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(
                null, false, model.getInstrument());
        dialog.setSpectrumInfo(null,
                model.getModelManager().getPolyhedralModel().isEllipsoid());
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

    private void editButtonActionPerformed(ActionEvent evt)
    {
        model.editButtonActionPerformed();
    }

    private void saveSpectrum(int index, CustomSpectrumKeyInterface oldImageInfo,
            CustomSpectrumKeyInterface newImageInfo) throws IOException
    {
        model.saveSpectrum(index, oldImageInfo, newImageInfo);
    }
}