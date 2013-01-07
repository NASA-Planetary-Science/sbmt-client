package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class LinesPopupMenu extends StructuresPopupMenu
{
    private LineModel model = null;
    private SmallBodyModel smallBodyModel;
    private JMenuItem saveProfileAction;

    public LinesPopupMenu(ModelManager modelManager)
    {
        super((LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES), false, false);

        this.model = (LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
        this.smallBodyModel = modelManager.getSmallBodyModel();

        saveProfileAction = new JMenuItem(new SaveProfileAction());
        saveProfileAction.setText("Save Profile...");
        this.add(saveProfileAction);
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        // Disable certain items if more than one structure is selected
        boolean exactlyOne = model.getSelectedStructures().length == 1;
        saveProfileAction.setEnabled(exactlyOne);

        super.show(invoker, x, y);
    }

    private class SaveProfileAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            int[] selectedStructures = model.getSelectedStructures();
            if (selectedStructures.length != 1)
                return;

            try
            {
                boolean hasElevation = smallBodyModel.getElevationDataColoringIndex() >= 0;
                if (!hasElevation)
                {
                    int option = JOptionPane.showConfirmDialog(getInvoker(),
                            "No elevation data is available with this shape model. Calculate elevation" +
                            " as distance to center of shape model instead?",
                            "Confirm Elevation Calculation", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.NO_OPTION)
                        return;
                }

                File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
                if (file != null)
                    model.saveProfile(selectedStructures[0], file, hasElevation);
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
