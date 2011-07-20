package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.near.util.Configuration;



public class FileMenu extends JMenu
{
    public FileMenu(ViewerManager rootPanel)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(rootPanel));
        this.add(mi);

        // On macs the exit action is in the Application menu not the file menu
        if (!Configuration.isMac())
        {
            this.addSeparator();

            mi = new JMenuItem(new ExitAction());
            this.add(mi);
        }
        else
        {
            try
            {
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

    public void exitSBMT()
    {
        System.exit(0);
    }

    private class SaveImageAction extends AbstractAction
    {
        private ViewerManager rootPanel;

        public SaveImageAction(ViewerManager rootPanel)
        {
            super("Export to Image...");
            this.rootPanel = rootPanel;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            rootPanel.getCurrentViewer().getRenderer().saveToFile();
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
