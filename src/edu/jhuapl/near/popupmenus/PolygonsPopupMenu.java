package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PolygonModel;

public class PolygonsPopupMenu extends StructuresPopupMenu
{
    private int structureIdLastClicked = -1;
    private PolygonModel model = null;

    public PolygonsPopupMenu(ModelManager modelManager)
    {
        this.model = (PolygonModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES);

        super.addMenuItems(model);

        JMenuItem mi = new JMenuItem(new DeleteAction());
        mi.setText("Delete");
        this.add(mi);
    }

    private class DeleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            model.removeStructure(structureIdLastClicked);
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (model.getBoundaryActor() == pickedProp)
        {
            structureIdLastClicked = model.getPolygonIdFromBoundaryCellId(pickedCellId);
            getChangeColorAction().setInvoker(e.getComponent());
            getChangeColorAction().setStructureIndex(structureIdLastClicked);
            show(e.getComponent(), e.getX(), e.getY());
        }
        else if (model.getInteriorActor() == pickedProp)
        {
            structureIdLastClicked = model.getPolygonIdFromInteriorCellId(pickedCellId);
            getChangeColorAction().setInvoker(e.getComponent());
            getChangeColorAction().setStructureIndex(structureIdLastClicked);
            show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
