package edu.jhuapl.near.popupmenus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.near.gui.CustomFileChooser;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;

public class LinesPopupMenu extends StructuresPopupMenu
{
    private LineModel model = null;
    private SmallBodyModel smallBodyModel;
    private JMenuItem saveProfileAction;

    public LinesPopupMenu(ModelManager modelManager, Renderer renderer)
    {
        super((LineModel)modelManager.getModel(ModelNames.LINE_STRUCTURES), renderer, false, false);

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
                if (smallBodyModel.getNumberOfColors() == 0)
                {
                    JOptionPane.showMessageDialog(getInvoker(),
                            "No data is associated with the plates of the current shape model. At least\n" +
                            "one set of plate data must be associated with the shape model in order to\n" +
                            "save out a profile. Plate data can be added to the shape model by clicking\n" +
                            "'Customize Plate Coloring...' in the leftmost tab.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    return;
                }

                File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
                if (file != null)
                    model.saveProfile(selectedStructures[0], file);
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
