package edu.jhuapl.near.gui;

import edu.jhuapl.saavtk.gui.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.ModelManager;

public class SbmtPolyhedralModelControlPanel extends PolyhedralModelControlPanel
{
    public SbmtPolyhedralModelControlPanel(ModelManager modelManager, String bodyName)
    {
        super(modelManager, bodyName);
    }

    protected CustomPlateDataDialog getPlateDataDialog(ModelManager modelManager)
    {
        return new CustomFitsPlateDataDialog(modelManager);
    }
}
