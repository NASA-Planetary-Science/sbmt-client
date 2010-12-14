package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import vtk.vtkProp;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class LinesPopupMenu extends StructuresPopupMenu
{
    private int cellIdLastClicked = -1;
    private LineModel model = null;

    public LinesPopupMenu(ModelManager modelManager)
    {
        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);

        JMenuItem mi;
        mi = new JMenuItem(new EditAction());
        mi.setText("Edit");
        //this.add(mi); // don't show for now

        super.addMenuItems(model);

        mi = new JMenuItem(new DeleteAction());
        mi.setText("Delete");
        this.add(mi);
    }

    private class EditAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            model.selectStructure(cellIdLastClicked);
        }
    }

    private class DeleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            model.removeStructure(cellIdLastClicked);
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (model.getLineActor() == pickedProp)
        {
            this.cellIdLastClicked = pickedCellId;
            getChangeColorAction().setInvoker(e.getComponent());
            getChangeColorAction().setStructureIndex(cellIdLastClicked);
            show(e.getComponent(), e.getX(), e.getY());
        }
        else if (model.getLineSelectionActor() == pickedProp)
        {
            this.cellIdLastClicked = model.getSelectedStructureIndex();
            show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
