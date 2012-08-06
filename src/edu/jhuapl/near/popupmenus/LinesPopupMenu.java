package edu.jhuapl.near.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkProp;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class LinesPopupMenu extends StructuresPopupMenu
{
    private int cellIdLastClicked = -1;
    private LineModel model = null;
    private SmallBodyModel smallBodyModel;

    public LinesPopupMenu(ModelManager modelManager)
    {
        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
        this.smallBodyModel = modelManager.getSmallBodyModel();

        JMenuItem mi;
        mi = new JMenuItem(new EditAction());
        mi.setText("Edit");
        //this.add(mi); // don't show for now

        super.addMenuItems(model);

        mi = new JMenuItem(new SaveProfileAction());
        mi.setText("Save Profile...");
        this.add(mi);

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

    private class SaveProfileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {

            try
            {
                boolean hasElevation = smallBodyModel.getElevationDataColoringIndex() >= 0;
                if (!hasElevation)
                {
                    int option = JOptionPane.showConfirmDialog(null,
                            "No elevation data is available with this shape model. Calculate elevation" +
                            " as distance to center of shape model instead?",
                            "Confirm Elevation Calculation", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION)
                        return;
                }

                File file = CustomFileChooser.showSaveDialog(null, "Save Profile", "profile.csv");
                if (file != null)
                    model.saveProfile(cellIdLastClicked, file, hasElevation);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        e1.getMessage()!=null ? e1.getMessage() : "An error occurred saving the profile.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
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
        if (model.getStructureActor() == pickedProp)
        {
            this.cellIdLastClicked = pickedCellId;
            getChangeColorAction().setInvoker(e.getComponent());
            getChangeColorAction().setStructureIndex(cellIdLastClicked);
            show(e.getComponent(), e.getX(), e.getY());
        }
        else if (model.getSelectionActor() == pickedProp)
        {
            this.cellIdLastClicked = model.getSelectedStructureIndex();
            show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
