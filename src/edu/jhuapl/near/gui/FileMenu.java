package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.near.util.Configuration;


public class FileMenu extends JMenu
{
    private PreferencesDialog preferencesDialog;
    private ViewerManager rootPanel;

    public FileMenu(ViewerManager rootPanel)
    {
        super("File");
        this.rootPanel = rootPanel;

        JMenuItem mi = new JMenuItem(new SaveImageAction());
        this.add(mi);

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
            preferencesDialog.setViewerManager(rootPanel);
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
            rootPanel.getCurrentViewer().getRenderer().saveToFile();
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
