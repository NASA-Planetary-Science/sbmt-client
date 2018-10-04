package edu.jhuapl.sbmt.gui.spectrum.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog;
import edu.jhuapl.sbmt.gui.spectrum.CustomSpectrumImporterDialog.SpectrumInfo;
import edu.jhuapl.sbmt.gui.spectrum.model.CustomSpectraSearchModel;
import edu.jhuapl.sbmt.gui.spectrum.ui.CustomSpectraControlPanel;


public class CustomSpectraControlController
{

    CustomSpectraControlPanel panel;
    CustomSpectraSearchModel model;
    List<SpectrumInfo> customSpectra;

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
        SpectrumInfo imageInfo = new SpectrumInfo();
        CustomSpectrumImporterDialog dialog = new CustomSpectrumImporterDialog(
                null, false, model.getInstrument());
        dialog.setSpectrumInfo(imageInfo,
                model.getModelManager().getPolyhedralModel().isEllipsoid());
        dialog.setLocationRelativeTo(getPanel());
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            imageInfo = dialog.getSpectrumInfo();
            try
            {
                saveSpectrum(model.getSpectrumRawResults().size(), null, imageInfo);
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

    private void saveSpectrum(int index, SpectrumInfo oldImageInfo,
            SpectrumInfo newImageInfo) throws IOException
    {
        model.saveSpectrum(index, oldImageInfo, newImageInfo);
    }
}
