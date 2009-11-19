package edu.jhuapl.near.gui;

import javax.swing.*;
import edu.jhuapl.near.model.ModelManager;


public class ControlPanel extends JTabbedPane
{
	private MSISearchPanel msiSearchPanel;
	private NISSearchPanel nisSearchPanel;
	private ErosControlPanel erosPanel;
	//private MSIControlPanel msiPanel;
	private LineamentControlPanel lineamentPanel;
		
	
	public ControlPanel(
			ErosRenderer viewer, 
			ModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager)
	{
		JTabbedPane searchPanels = new JTabbedPane();
		msiSearchPanel = new MSISearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		nisSearchPanel = new NISSearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		searchPanels.add("MSI", msiSearchPanel);
		searchPanels.add("NIS", nisSearchPanel);
		
		erosPanel = new ErosControlPanel(modelManager);
		//msiPanel = new MSIControlPanel(modelManager);
		lineamentPanel = new LineamentControlPanel(modelManager);
		
		addTab("Search", searchPanels);
		addTab("Eros", erosPanel);
		//addTab("MSI", msiPanel);
		addTab("Lineament", lineamentPanel);
	}
}
