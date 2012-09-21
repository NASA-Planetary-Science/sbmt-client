package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

public class CirclesPopupMenu extends StructuresPopupMenu
{
    private int cellIdLastClicked = -1;
    private CircleModel model = null;
    private ChangeLatLonAction changeLatLonAction;
    private Component invoker;

    public CirclesPopupMenu(ModelManager modelManager, Component invoker)
    {
        this.model = (CircleModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
        this.invoker = invoker;

        super.addMenuItems(model);

        JMenuItem mi;
        changeLatLonAction = new ChangeLatLonAction(model);
        mi = new JMenuItem(changeLatLonAction);
        mi.setText("Change Latitude/Longitude...");
        this.add(mi);

        mi = new JMenuItem(new DeleteAction());
        mi.setText("Delete");

        mi = new JMenuItem(new ExportPlateDataInsidePolygon());
        mi.setText("Save plate data inside circle...");

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

    private class ExportPlateDataInsidePolygon extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            File file = CustomFileChooser.showSaveDialog(invoker, "Save Plate Data", "platedata.txt");
            if (file != null)
            {
                try
                {
                    int idx = model.getPolygonIdFromBoundaryCellId(cellIdLastClicked);
                    model.savePlateDataInsideStructure(idx, file);
                }
                catch (IOException e1)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(invoker),
                            "Unable to save file to " + file.getAbsolutePath(),
                            "Error Saving File",
                            JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
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
