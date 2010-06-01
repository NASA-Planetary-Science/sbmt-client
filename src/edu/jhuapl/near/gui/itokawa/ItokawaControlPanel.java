package edu.jhuapl.near.gui.itokawa;

import javax.swing.*;

import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;


public class ItokawaControlPanel extends JTabbedPane
{
	private SmallBodyControlPanel itokawaPanel;
	private StructuresControlPanel structuresPanel;
		
	
	public ItokawaControlPanel(
			ModelManager modelManager, 
			PickManager pickManager)
	{
        setBorder(BorderFactory.createEmptyBorder());
        
		itokawaPanel = new SmallBodyControlPanel(modelManager, "Itokawa");
		structuresPanel = new StructuresControlPanel(modelManager, pickManager);
		
		addTab("Itokawa", itokawaPanel);
		addTab("Structures", structuresPanel);
	}
}
