package edu.jhuapl.near.gui;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class ViewMenu extends JMenu 
{
	private JPanel rootPanel;
	
    public ViewMenu(JPanel rootPanel, ArrayList<View> views)
    {
        super("View");
        
        this.rootPanel = rootPanel;
        
        for (int i=0; i < views.size(); ++i)
        {
        	JMenuItem mi = new JMenuItem(new ShowBodyAction(views.get(i).getName(), views.get(i)));
        	this.add(mi);
        }
    }
    
    private class ShowBodyAction extends AbstractAction
    {
    	private String name;
    	private View view;
    	private boolean initializedCalled = false;
        
    	public ShowBodyAction(String name, View view)
        {
            super(name);
            this.name = name;
            this.view = view;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
        	if (!initializedCalled)
        	{
        		view.initialize();
        		initializedCalled = true;
        	}
        	
        	CardLayout cardLayout = (CardLayout)(rootPanel.getLayout());
        	cardLayout.show(rootPanel, name);
        }
    }
}
