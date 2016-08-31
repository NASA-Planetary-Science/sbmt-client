package edu.jhuapl.near.gui;

import edu.jhuapl.near.model.ModelManager;

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
