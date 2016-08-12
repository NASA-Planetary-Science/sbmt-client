package edu.jhuapl.near.popupmenus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkCamera;
import vtk.vtkProp;
import vtk.rendering.vtkAbstractComponent;

import edu.jhuapl.near.gui.ChangeLatLonDialog;
import edu.jhuapl.near.gui.ColorChooser;
import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.util.MathUtil;

abstract public class StructuresPopupMenu extends PopupMenu
{
    private StructureModel model;
    private SmallBodyModel smallBodyModel;
    private Renderer renderer;
    private JMenuItem changeLatLonAction;
    private JMenuItem exportPlateDataAction;
    private JMenuItem editAction;
    private JMenuItem centerStructureMenuItem;
    private JMenuItem centerStructurePreserveDistanceMenuItem;
    private JMenuItem displayInteriorMenuItem;
    private JMenuItem setLabelButton;
    private JMenuItem showLabelButton;

    private JCheckBoxMenuItem hideMenuItem;

    public StructuresPopupMenu(
            StructureModel model,
            SmallBodyModel smallBodyModel,
            Renderer renderer,
            boolean showChangeLatLon,
            boolean showExportPlateDataInsidePolygon,
            boolean showDisplayInterior)
    {
        this.model = model;
        this.smallBodyModel = smallBodyModel;
        this.renderer = renderer;

        editAction = new JMenuItem(new EditAction());
        editAction.setText("Edit");
        //this.add(mi); // don't show for now

        JMenuItem changeColorAction = new JMenuItem(new ChangeColorAction());
        changeColorAction.setText("Change Color...");
        this.add(changeColorAction);

        hideMenuItem = new JCheckBoxMenuItem(new ShowHideAction());
        hideMenuItem.setText("Hide");
        this.add(hideMenuItem);

        setLabelButton = new JMenuItem(new SetLabelAction());
        setLabelButton.setText("Set Label");
        this.add(setLabelButton);

        showLabelButton = new JMenuItem(new ShowLabelAction());
        showLabelButton.setText("Show Label");
        this.add(showLabelButton);

        JMenuItem deleteAction = new JMenuItem(new DeleteAction());
        deleteAction.setText("Delete");
        this.add(deleteAction);

        centerStructureMenuItem = new JMenuItem(new CenterStructureAction(false));
        centerStructureMenuItem.setText("Center in Window (Close Up)");
        this.add(centerStructureMenuItem);

        centerStructurePreserveDistanceMenuItem = new JMenuItem(new CenterStructureAction(true));
        centerStructurePreserveDistanceMenuItem.setText("Center in Window (Preserve Distance)");
        this.add(centerStructurePreserveDistanceMenuItem);

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

        if (showDisplayInterior)
        {
            displayInteriorMenuItem = new JCheckBoxMenuItem(new DisplayInteriorAction());
            displayInteriorMenuItem.setText("Display Interior");
            this.add(displayInteriorMenuItem);
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

        if (centerStructureMenuItem != null)
            centerStructureMenuItem.setEnabled(exactlyOne);

        if (centerStructurePreserveDistanceMenuItem != null)
            centerStructurePreserveDistanceMenuItem.setEnabled(exactlyOne);

        // If any of the selected structures are not hidden then show
        // the hide menu item as unchecked. Otherwise show it checked.
        hideMenuItem.setSelected(true);
        int[] selectedStructures = model.getSelectedStructures();
        for (int i=0; i<selectedStructures.length; ++i)
        {
            if (!model.isStructureHidden(selectedStructures[i]))
            {
                hideMenuItem.setSelected(false);
                break;
            }
        }

        // If any of the selected structures are displaying interior then show
        // the display interior menu item as unchecked. Otherwise show it checked.
        if (displayInteriorMenuItem != null)
        {
            displayInteriorMenuItem.setSelected(true);
            selectedStructures = model.getSelectedStructures();
            for (int i=0; i<selectedStructures.length; ++i)
            {
                if (!model.isShowStructureInterior(selectedStructures[i]))
                {
                    displayInteriorMenuItem.setSelected(false);
                    break;
                }
            }
        }

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

    protected class ShowHideAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            model.setStructuresHidden(selectedStructures, hideMenuItem.isSelected());
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

    private class CenterStructureAction extends AbstractAction
    {
        private boolean preserveCurrentDistance = false;

        public CenterStructureAction(boolean preserveCurrentDistance)
        {
            this.preserveCurrentDistance = preserveCurrentDistance;
        }

        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length != 1)
                return;

            double viewAngle = renderer.getCameraViewAngle();
            double[] focalPoint = model.getStructureCenter(selectedStructures[0]);
            double[] normal = model.getStructureNormal(selectedStructures[0]);
            vtkAbstractComponent renWin = renderer.getRenderWindowPanel();

            double distanceToStructure = 0.0;
            if (preserveCurrentDistance)
            {
                vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
                double[] pos = activeCamera.GetPosition();
                double[] closestPoint = smallBodyModel.findClosestPoint(pos);
                distanceToStructure = MathUtil.distanceBetween(pos, closestPoint);
            }
            else
            {
                double size = model.getStructureSize(selectedStructures[0]);
                distanceToStructure = size / Math.tan(Math.toRadians(viewAngle)/2.0);
            }

            double[] newPos = {focalPoint[0] + distanceToStructure*normal[0],
                    focalPoint[1] + distanceToStructure*normal[1],
                    focalPoint[2] + distanceToStructure*normal[2]
            };

            // compute up vector
            double[] dir = {focalPoint[0]-newPos[0],
                    focalPoint[1]-newPos[1],
                    focalPoint[2]-newPos[2]
            };
            MathUtil.vhat(dir, dir);
            double[] zAxis = {0.0, 0.0, 1.0};
            double[] upVector = new double[3];
            MathUtil.vcrss(dir, zAxis, upVector);

            if (upVector[0] != 0.0 || upVector[1] != 0.0 || upVector[2] != 0.0)
                MathUtil.vcrss(upVector, dir, upVector);
            else
                upVector = new double[]{1.0, 0.0, 0.0};

            renderer.setCameraOrientation(newPos, focalPoint, upVector, viewAngle);
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

    public void updateLabel(String label, int row)
    {
        model.setStructureLabel(row, label);
    }

    protected class SetLabelAction extends AbstractAction
    {
        public SetLabelAction()
        {
            super("Set Label");
        }
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures[0] == -1)
                return;
            String option = JOptionPane.showInputDialog("How do you wish to name the structure? Leave blank for default.");
            for (int idx : selectedStructures)
                model.setStructureLabel(idx, option);

        }
    }

    protected class ShowLabelAction extends AbstractAction
    {
        public ShowLabelAction()
        {
            super("Show Label");
        }
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            for (int idx : selectedStructures)
                model.showLabel(idx);
            //showLabelButton.
        }
    }

    protected class DisplayInteriorAction extends AbstractAction
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            model.setShowStructuresInterior(selectedStructures, displayInteriorMenuItem.isSelected());
        }
    }
}
