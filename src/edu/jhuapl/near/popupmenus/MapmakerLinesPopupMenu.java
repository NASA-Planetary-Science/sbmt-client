package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

/**
 * Popup menu used by the mapmaker view for profiles. It is meant to replace LinesPopupMenu
 * which is used in the regular views.
 */
public class MapmakerLinesPopupMenu extends PopupMenu
{
    private LineModel model = null;
    private JMenuItem saveProfileAction;
    private int pickedCellId = -1;
    private SmallBodyModel parentSmallBodyModel;

    public MapmakerLinesPopupMenu(ModelManager modelManager, SmallBodyModel parentSmallBodyModel, Renderer renderer)
    {
        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
        this.parentSmallBodyModel = parentSmallBodyModel;

        saveProfileAction = new JMenuItem(new SaveProfileAction());
        saveProfileAction.setText("Save Profile...");
        saveProfileAction.setEnabled(true);
        this.add(saveProfileAction);
    }

    @Override
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        this.pickedCellId = pickedCellId;
        show(e.getComponent(), e.getX(), e.getY());
    }

    private class SaveProfileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            if (pickedCellId < 0 || pickedCellId >= model.getNumberOfStructures())
                return;

            try
            {
                File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
                if (file != null)
                    model.saveProfileUsingGravityProgram(pickedCellId, file, parentSmallBodyModel);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getInvoker(),
                        e1.getMessage()!=null ? e1.getMessage() : "An error occurred saving the profile.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
