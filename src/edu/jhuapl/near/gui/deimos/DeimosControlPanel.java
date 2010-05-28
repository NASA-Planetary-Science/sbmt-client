package edu.jhuapl.near.gui.deimos;

import javax.swing.*;

import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;


public class DeimosControlPanel extends JTabbedPane
{
	private SmallBodyControlPanel deimosPanel;
	private StructuresControlPanel structuresPanel;
		
	
	public DeimosControlPanel(
			ModelManager modelManager, 
			PickManager pickManager)
	{
        setBorder(BorderFactory.createEmptyBorder());
        
		deimosPanel = new SmallBodyControlPanel(modelManager, "Deimos");
		structuresPanel = new StructuresControlPanel(modelManager, pickManager);
		
		addTab("Deimos", deimosPanel);
		addTab("Structures", structuresPanel);
	}
}
