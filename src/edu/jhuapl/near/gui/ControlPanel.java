package edu.jhuapl.near.gui;

import javax.swing.*;
import edu.jhuapl.near.model.ModelManager;


public class ControlPanel extends JTabbedPane
{
	private SearchPanel searchPanel;
	private ErosControlPanel erosPanel;
	//private MSIControlPanel msiPanel;
	private LineamentControlPanel lineamentPanel;
		
	
	public ControlPanel(
			ErosRenderer viewer, 
			ModelManager modelManager, 
			MSIImageInfoPanelManager infoPanelManager)
	{
		searchPanel = new SearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		erosPanel = new ErosControlPanel(modelManager);
		//msiPanel = new MSIControlPanel(modelManager);
		lineamentPanel = new LineamentControlPanel(modelManager);
		
		addTab("Search", searchPanel);
		addTab("Eros", erosPanel);
		//addTab("MSI", msiPanel);
		addTab("Lineament", lineamentPanel);
	}
}
