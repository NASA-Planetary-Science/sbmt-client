package edu.jhuapl.sbmt.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.util.gravity.Gravity;

/**
 * Popup menu used by the mapmaker view for profiles. It is meant to replace LinesPopupMenu
 * which is used in the regular views.
 */
public class MapmakerLinesPopupMenu extends PopupMenu
{
    private LineModel model = null;
    private JMenuItem saveProfileAction;
    private int pickedCellId = -1;
    private PolyhedralModel parentPolyhedralModel;

    public MapmakerLinesPopupMenu(ModelManager modelManager, PolyhedralModel parentPolyhedralModel, Renderer renderer)
    {
        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
        this.parentPolyhedralModel = parentPolyhedralModel;

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
                {
                    Line lin = model.getLines().get(pickedCellId);
                    Gravity.saveProfileUsingGravityProgram(lin, pickedCellId, file, parentPolyhedralModel, parentPolyhedralModel);
                }
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
