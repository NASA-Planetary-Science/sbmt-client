package edu.jhuapl.near.gui;

import java.io.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.event.*;


public class FileMenu extends JMenu 
{
    private JPanel rootPanel;

    public FileMenu(JPanel rootPanel, boolean showAll)
    {
        super("File");

        this.rootPanel = rootPanel;
        
        JMenuItem mi = new JMenuItem(new SaveImageAction());
        this.add(mi);

        if (showAll)
        {
            this.addSeparator();
            mi = new JMenuItem(new AboutAction());
            this.add(mi);
            
        	this.addSeparator();
        	mi = new JMenuItem(new ExitAction());
        	this.add(mi);
        }
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

    private class AboutAction extends AbstractAction
    {
    	private static final String COPYRIGHT  = "\u00a9";

    	public AboutAction()
        {
            super("About Small Body Mapping Tool");
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	String versionString = "\n";
        	try
        	{
        		InputStream is = this.getClass().getResourceAsStream("/svn.version");
        		byte[] data = new byte[256];
        		is.read(data, 0, data.length);
        		String[] tmp = (new String(data)).trim().split("\\s+");
        		tmp[3] = tmp[3].replace('-', '.');
        		versionString = "Version: " + tmp[3] + "\n\n";
        	}
        	catch (Exception e)
        	{
        	}
        	
			JOptionPane.showMessageDialog(rootPanel,
					"Small Body Mapping Tool\n" + versionString +
				    COPYRIGHT + " 2010 The Johns Hopkins University Applied Physics Laboratory\n",
				    "About Small Body Mapping Tool",
				    JOptionPane.PLAIN_MESSAGE);
        }
    }
}
