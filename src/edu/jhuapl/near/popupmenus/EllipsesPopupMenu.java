package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;

import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class EllipsesPopupMenu extends StructuresPopupMenu
{
    private int cellIdLastClicked = -1;
    private EllipseModel model = null;
    private ChangeLatLonAction changeLatLonAction;

    public EllipsesPopupMenu(ModelManager modelManager)
    {
        this.model = (EllipseModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);

        super.addMenuItems(model);

        JMenuItem mi;
        changeLatLonAction = new ChangeLatLonAction(model);
        mi = new JMenuItem(changeLatLonAction);
        mi.setText("Change Latitude/Longitude...");
        this.add(mi);

        mi = new JMenuItem(new DeleteAction());
        mi.setText("Delete");
        this.add(mi);
    }

    private class DeleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int idx = model.getPolygonIdFromBoundaryCellId(cellIdLastClicked);
            model.removeStructure(idx);
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        this.cellIdLastClicked = pickedCellId;

        getChangeColorAction().setInvoker(e.getComponent());
        int idx = model.getPolygonIdFromBoundaryCellId(cellIdLastClicked);
        getChangeColorAction().setStructureIndex(idx);

        changeLatLonAction.setStructureIndex(idx);
        changeLatLonAction.setInvoker(e.getComponent());

        show(e.getComponent(), e.getX(), e.getY());
    }
}
