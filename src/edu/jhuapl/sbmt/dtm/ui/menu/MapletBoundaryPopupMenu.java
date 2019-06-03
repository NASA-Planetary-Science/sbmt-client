package edu.jhuapl.sbmt.dtm.ui.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection.DEMBoundary;

public class MapletBoundaryPopupMenu extends PopupMenu
{
    private DEMBoundaryCollection boundaryCollection;
    private DEMBoundary boundary;
    private Component invoker;
    private JMenuItem colorMenuItem;
    private JMenuItem thicknessMenuItem;

    public MapletBoundaryPopupMenu(ModelManager modelManager,
            Component invoker)
    {
        this.boundaryCollection = (DEMBoundaryCollection)modelManager.getModel(ModelNames.DEM_BOUNDARY);
        this.invoker = invoker;

        colorMenuItem = new JMenuItem(new ChangeColorAction());
        colorMenuItem.setText("Change Color...");
        this.add(colorMenuItem);

        thicknessMenuItem = new JMenuItem(new ChangeThicknessAction());
        thicknessMenuItem.setText("Change Line Width...");
        this.add(thicknessMenuItem);
    }

    public class ChangeColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent arg0)
        {
            Color color = boundary.getColor();
            int[] initialColor = {color.getRed(), color.getGreen(), color.getBlue()};

            color = ColorChooser.showColorChooser(invoker, initialColor);

            if (color == null)
                return;

            boundary.setBoundaryColor(color);
        }
    }

    public class ChangeThicknessAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent arg0)
        {
            SpinnerNumberModel sModel = new SpinnerNumberModel(boundary.getLineWidth(), 1.0, 100.0, 1.0);
            JSpinner spinner = new JSpinner(sModel);

            int option = JOptionPane.showOptionDialog(
                    invoker,
                    spinner,
                    "Enter valid number",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);

            if (option == JOptionPane.OK_OPTION)
            {
                boundary.setLineWidth((Double)spinner.getValue());
            }
        }
    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        this.boundary = boundaryCollection.getBoundary(pickedProp);
        show(e.getComponent(), e.getX(), e.getY());
    }
}
