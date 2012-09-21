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
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PolygonModel;

public class PolygonsPopupMenu extends StructuresPopupMenu
{
    private int structureIdLastClicked = -1;
    private PolygonModel model = null;
    private Component invoker;

    public PolygonsPopupMenu(ModelManager modelManager, Component invoker)
    {
        this.model = (PolygonModel)modelManager.getModel(ModelNames.POLYGON_STRUCTURES);
        this.invoker = invoker;

        super.addMenuItems(model);

        JMenuItem mi = new JMenuItem(new DeleteAction());
        mi.setText("Delete");

        mi = new JMenuItem(new ExportPlateDataInsidePolygon());
        mi.setText("Save plate data inside polygon...");

        this.add(mi);
    }

    private class DeleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            model.removeStructure(structureIdLastClicked);
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
                    model.savePlateDataInsideStructure(structureIdLastClicked, file);
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
