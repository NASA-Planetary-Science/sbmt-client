package edu.jhuapl.near.gui;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;


public class FileMenu extends JMenu 
{
    private JPanel rootPanel;

    public FileMenu(JPanel rootPanel)
    {
        super("File");

        this.rootPanel = rootPanel;
        
        JMenuItem mi = new JMenuItem(new SaveImageAction());
        this.add(mi);

        this.addSeparator();

        mi = new JMenuItem(new ExitAction());
        this.add(mi);
    }
    
    private View getCurrentView()
    {
        // The following is adapted from http://www.rgagnon.com/javadetails/java-0423.html
        Component[] comps = rootPanel.getComponents();
        int i = 0;
        while(i < comps.length && !comps[i].isVisible())
           ++i;

        return (i == comps.length) ? null : (View)comps[i];
    }

    private class SaveImageAction extends AbstractAction
    {
    	public SaveImageAction()
        {
            super("Export to Image...");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            getCurrentView().getRenderer().saveToFile();
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
