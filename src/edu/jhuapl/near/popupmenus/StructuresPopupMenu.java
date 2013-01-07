package edu.jhuapl.near.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.ChangeLatLonDialog;
import edu.jhuapl.near.gui.ColorChooser;
import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.model.StructureModel;

abstract public class StructuresPopupMenu extends PopupMenu
{
    private StructureModel model;
    private JMenuItem changeLatLonAction;
    private JMenuItem exportPlateDataAction;
    private JMenuItem editAction;

    public StructuresPopupMenu(
            StructureModel model,
            boolean showChangeLatLon,
            boolean showExportPlateDataInsidePolygon)
    {
        this.model = model;

        editAction = new JMenuItem(new EditAction());
        editAction.setText("Edit");
        //this.add(mi); // don't show for now

        JMenuItem changeColorAction = new JMenuItem(new ChangeColorAction());
        changeColorAction.setText("Change Color...");
        this.add(changeColorAction);

        JMenuItem deleteAction = new JMenuItem(new DeleteAction());
        deleteAction.setText("Delete");
        this.add(deleteAction);

        if (showChangeLatLon)
        {
            changeLatLonAction = new JMenuItem(new ChangeLatLonAction());
            changeLatLonAction.setText("Change Latitude/Longitude...");
            this.add(changeLatLonAction);
        }

        if (showExportPlateDataInsidePolygon)
        {
            exportPlateDataAction = new JMenuItem(new ExportPlateDataInsidePolygon());
            exportPlateDataAction.setText("Save plate data inside polygon...");
            this.add(exportPlateDataAction);
        }
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        // Disable certain items if more than one structure is selected
        boolean exactlyOne = model.getSelectedStructures().length == 1;

        if (editAction != null)
            editAction.setEnabled(exactlyOne);

        if (changeLatLonAction != null)
            changeLatLonAction.setEnabled(exactlyOne);

        if (exportPlateDataAction != null)
            exportPlateDataAction.setEnabled(exactlyOne);

        super.show(invoker, x, y);
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        show(e.getComponent(), e.getX(), e.getY());
    }

    protected class EditAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length == 1)
                model.activateStructure(selectedStructures[0]);
        }
    }

    protected class ChangeColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent actionEvent)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length == 0)
                return;

            // Use the color of the first item as the default to show
            Color color = ColorChooser.showColorChooser(
                    getInvoker(),
                    model.getStructure(selectedStructures[0]).getColor());

            if (color == null)
                return;

            int[] c = new int[4];
            c[0] = color.getRed();
            c[1] = color.getGreen();
            c[2] = color.getBlue();
            c[3] = color.getAlpha();

            for (int idx : selectedStructures)
                model.setStructureColor(idx, c);
        }
    }

    protected class DeleteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            for (int i=selectedStructures.length-1; i>=0; --i)
                model.removeStructure(selectedStructures[i]);
        }
    }

    protected class ChangeLatLonAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent actionEvent)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length == 1)
            {
                ChangeLatLonDialog dialog = new ChangeLatLonDialog(model, selectedStructures[0]);
                dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(getInvoker()));
                dialog.setVisible(true);
            }
        }
    }

    protected class ExportPlateDataInsidePolygon extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Plate Data", "platedata.txt");
            if (file != null)
            {
                try
                {
                    int[] selectedStructures = model.getSelectedStructures();
                    if (selectedStructures.length == 1)
                        model.savePlateDataInsideStructure(selectedStructures[0], file);
                }
                catch (IOException e1)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getInvoker()),
                            "Unable to save file to " + file.getAbsolutePath(),
                            "Error Saving File",
                            JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        }
    }
}
