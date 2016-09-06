package edu.jhuapl.near.gui;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.CustomPlateDataImporterDialog;
import edu.jhuapl.saavtk.model.ModelManager;

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
