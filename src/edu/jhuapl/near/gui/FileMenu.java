package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.near.gui.actions.SaveImageAction;



public class FileMenu extends JMenu
{
    public FileMenu(ViewerManager rootPanel)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(rootPanel.getCurrentViewer().getRenderer()));
        this.add(mi);

        // On macs the exit action is in the Application menu not the file menu
        if (!System.getProperty("os.name").toLowerCase().startsWith("mac"))
        {
            this.addSeparator();

            mi = new JMenuItem(new ExitAction());
            this.add(mi);
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
            System.exit(0);
        }
    }
}
