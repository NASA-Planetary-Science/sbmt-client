package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.near.util.Configuration;


public class FileMenu extends JMenu
{
    private PreferencesDialog preferencesDialog;
    private ViewManager rootPanel;
    public JFrame frame;

    public FileMenu(ViewManager rootPanel)
    {
        super("File");
        this.rootPanel = rootPanel;

        JMenuItem mi = new JMenuItem(new SaveImageAction());
        this.add(mi);
        mi = new JMenuItem(new Save6AxesViewsAction());
        this.add(mi);
        mi = new JMenuItem(new SaveShapeModelAsPLTAction());
        this.add(mi);
        mi = new JMenuItem(new SaveShapeModelAsOBJAction());
        this.add(mi);
        mi = new JMenuItem(new ShowCameraOrientationAction());
        this.add(mi);
        mi = new JMenuItem(new CopyToClipboardAction());
        //this.add(mi);
        mi = new JCheckBoxMenuItem(new ShowSimpleCylindricalProjectionAction());
        //this.add(mi);

        // On macs the exit action is in the Application menu not the file menu
        if (!Configuration.isMac())
        {
            mi = new JMenuItem(new PreferencesAction());
            this.add(mi);

            this.addSeparator();

            mi = new JMenuItem(new ExitAction());
            this.add(mi);
        }
        else
        {
            try
            {
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[])null));
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("exitSBMT", (Class[])null));
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void showPreferences()
    {
        if (preferencesDialog == null)
        {
            preferencesDialog = new PreferencesDialog(null, false);
            preferencesDialog.setViewManager(rootPanel);
        }

        preferencesDialog.setLocationRelativeTo(rootPanel);
        preferencesDialog.setVisible(true);
    }

    public void exitSBMT()
    {
        System.exit(0);
    }

    private class SaveImageAction extends AbstractAction
    {
        public SaveImageAction()
        {
            super("Export to Image...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.getCurrentView().getRenderer().saveToFile();
        }
    }

    private class Save6AxesViewsAction extends AbstractAction
    {
        public Save6AxesViewsAction()
        {
            super("Export Six Views along Axes to Images...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.getCurrentView().getRenderer().save6ViewsToFile();
        }
    }

    private class SaveShapeModelAsPLTAction extends AbstractAction
    {
        public SaveShapeModelAsPLTAction()
        {
            super("Export Shape Model as PLT...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model as PLT", "model.plt");

            try
            {
                if (file != null)
                    rootPanel.getCurrentView().getModelManager().getSmallBodyModel().saveAsPLT(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SaveShapeModelAsOBJAction extends AbstractAction
    {
        public SaveShapeModelAsOBJAction()
        {
            super("Export Shape Model as OBJ...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            File file = CustomFileChooser.showSaveDialog(null, "Export Shape Model as OBJ", "model.obj");

            try
            {
                if (file != null)
                    rootPanel.getCurrentView().getModelManager().getSmallBodyModel().saveAsOBJ(file);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An error occurred exporting the shape model.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class ShowCameraOrientationAction extends AbstractAction
    {
        public ShowCameraOrientationAction()
        {
            super("Camera...");
        }

        public void actionPerformed(ActionEvent e)
        {
            new CameraDialog(rootPanel.getCurrentView().getRenderer()).setVisible(true);
        }
    }

    private class CopyToClipboardAction extends AbstractAction
    {
        public CopyToClipboardAction()
        {
            super("Copy to Clipboard...");
        }

        public void actionPerformed(ActionEvent e)
        {

        }
    }

    private class ShowSimpleCylindricalProjectionAction extends AbstractAction
    {
        public ShowSimpleCylindricalProjectionAction()
        {
            super("Render using Simple Cylindrical Projection (Experimental)");
        }

        public void actionPerformed(ActionEvent e)
        {
            JCheckBoxMenuItem mi = (JCheckBoxMenuItem)e.getSource();
            rootPanel.getCurrentView().getRenderer().set2DMode(mi.isSelected());
        }
    }

    private class PreferencesAction extends AbstractAction
    {
        public PreferencesAction()
        {
            super("Preferences...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            showPreferences();
        }
    }

    private class ExitAction extends AbstractAction
    {
        public ExitAction()
        {
            super("Exit");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            exitSBMT();
        }
    }
}
