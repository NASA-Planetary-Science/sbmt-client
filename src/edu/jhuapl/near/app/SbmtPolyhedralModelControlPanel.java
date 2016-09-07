package edu.jhuapl.near.app;

import edu.jhuapl.near.gui.fits.CustomFitsPlateDataDialog;
import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
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
