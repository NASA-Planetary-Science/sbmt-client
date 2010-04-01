package edu.jhuapl.near.gui;

import javax.swing.*;

import edu.jhuapl.near.model.*;
import edu.jhuapl.near.pick.PickManager;


public class ControlPanel extends JTabbedPane
{
	private ErosControlPanel erosPanel;
	private MSISearchPanel msiSearchPanel;
	private NISSearchPanel nisSearchPanel;
	private NLRSearchPanel nlrSearchPanel;
	private LineamentControlPanel lineamentPanel;
	private StructuresControlPanel structuresPanel;
		
	
	public ControlPanel(
			ErosRenderer viewer, 
			ModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager,
			PickManager pickManager)
	{
		erosPanel = new ErosControlPanel(modelManager);
		msiSearchPanel = new MSISearchPanel(modelManager, infoPanelManager, pickManager, viewer.getRenderWindowPanel());
		nisSearchPanel = new NISSearchPanel(modelManager, infoPanelManager, pickManager, viewer.getRenderWindowPanel());
		nlrSearchPanel = new NLRSearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		lineamentPanel = new LineamentControlPanel(modelManager);
		structuresPanel = new StructuresControlPanel(modelManager, pickManager);
		
		addTab("Eros", erosPanel);
		addTab("MSI", msiSearchPanel);
		addTab("NIS", nisSearchPanel);
		addTab("NLR", nlrSearchPanel);
		addTab("Lineament", lineamentPanel);
		addTab("Structures", structuresPanel);
	}
}
