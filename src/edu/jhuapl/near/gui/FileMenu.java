package edu.jhuapl.near.gui;

import java.awt.event.*;

import javax.swing.*;

import edu.jhuapl.near.gui.actions.SaveImageAction;



public class FileMenu extends JMenu 
{
    public FileMenu(ViewerManager rootPanel)
    {
        super("File");

        JMenuItem mi = new JMenuItem(new SaveImageAction(rootPanel.getCurrentViewer().getRenderer()));
        this.add(mi);

        this.addSeparator();

        mi = new JMenuItem(new ExitAction());
        this.add(mi);
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
