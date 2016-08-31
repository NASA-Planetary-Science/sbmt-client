package edu.jhuapl.near.gui;

import javax.swing.JOptionPane;

import edu.jhuapl.near.model.ModelManager;

public class CustomFitsPlateDataDialog extends CustomPlateDataDialog
{
    public CustomFitsPlateDataDialog(ModelManager modelManager)
    {
        super(modelManager);
    }

    protected CustomPlateDataImporterDialog getPlateImporterDialog()
    {
        return new CustomFitsPlateDataImporterDialog(JOptionPane.getFrameForComponent(this), false);
    }
}
