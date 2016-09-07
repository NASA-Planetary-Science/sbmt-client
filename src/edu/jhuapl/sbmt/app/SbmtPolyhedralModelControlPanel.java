package edu.jhuapl.sbmt.app;

import edu.jhuapl.saavtk.gui.dialog.CustomPlateDataDialog;
import edu.jhuapl.saavtk.gui.panel.PolyhedralModelControlPanel;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.sbmt.gui.fits.CustomFitsPlateDataDialog;

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
