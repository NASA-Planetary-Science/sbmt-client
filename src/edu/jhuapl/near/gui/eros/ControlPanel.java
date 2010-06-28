package edu.jhuapl.near.gui.eros;

import javax.swing.*;

import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.pick.PickManager;


public class ControlPanel extends JTabbedPane
{
	private SmallBodyControlPanel erosPanel;
	private MSISearchPanel msiSearchPanel;
	private NISSearchPanel nisSearchPanel;
    //private NLRSearchPanel nlrSearchPanel;
    private NLR2SearchPanel nlr2SearchPanel;
	private LineamentControlPanel lineamentPanel;
	private StructuresControlPanel structuresPanel;
		
	
	public ControlPanel(
			Renderer renderer, 
			ErosModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager,
			PickManager pickManager)
	{
        setBorder(BorderFactory.createEmptyBorder());
        
		erosPanel = new SmallBodyControlPanel(modelManager, "Eros");
		msiSearchPanel = new MSISearchPanel(modelManager, infoPanelManager, pickManager, renderer);
		nisSearchPanel = new NISSearchPanel(modelManager, infoPanelManager, pickManager);
        //nlrSearchPanel = new NLRSearchPanel(modelManager, infoPanelManager);
        nlr2SearchPanel = new NLR2SearchPanel(modelManager, infoPanelManager, pickManager);
		lineamentPanel = new LineamentControlPanel(modelManager);
		structuresPanel = new StructuresControlPanel(modelManager, pickManager);
		
		addTab("Eros", erosPanel);
		addTab("MSI", msiSearchPanel);
		addTab("NIS", nisSearchPanel);
        //addTab("NLR", nlrSearchPanel);
        addTab("NLR", nlr2SearchPanel);
		addTab("Lineament", lineamentPanel);
		addTab("Structures", structuresPanel);
	}
}
