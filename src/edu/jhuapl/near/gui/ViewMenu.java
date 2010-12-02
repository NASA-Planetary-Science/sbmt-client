package edu.jhuapl.near.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class ViewMenu extends JMenu 
{
	private ViewerManager rootPanel;
	
    public ViewMenu(ViewerManager rootPanel)
    {
        super("View");
        
        this.rootPanel = rootPanel;

        ButtonGroup group = new ButtonGroup();

        for (int i=0; i < rootPanel.getNumberOfViewers(); ++i)
        {
        	Viewer viewer = rootPanel.getViewer(i);
        	JMenuItem mi = new JRadioButtonMenuItem(new ShowBodyAction(viewer));
        	if (i==0)
        		mi.setSelected(true);
        	group.add(mi);
        	this.add(mi);
        }
    }
    
    private class ShowBodyAction extends AbstractAction
    {
    	private Viewer viewer;
        
    	public ShowBodyAction(Viewer viewer)
        {
            super(viewer.getName());
            this.viewer = viewer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	rootPanel.setCurrentViewer(viewer);
        }
    }
}
