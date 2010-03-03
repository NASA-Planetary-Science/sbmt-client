package edu.jhuapl.near.gui;

import javax.swing.*;

import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.model.ModelManager;


public class ControlPanel extends JTabbedPane
{
	private ErosControlPanel erosPanel;
	private MSISearchPanel msiSearchPanel;
	private NISSearchPanel nisSearchPanel;
	private NLRSearchPanel nlrSearchPanel;
	private LineamentControlPanel lineamentPanel;
	private StructureMapperControlPanel structureMapperPanel;
		
	
	public ControlPanel(
			ErosRenderer viewer, 
			ModelManager modelManager, 
			ModelInfoWindowManager infoPanelManager,
			PickManager pickManager)
	{
		erosPanel = new ErosControlPanel(modelManager);
		msiSearchPanel = new MSISearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		nisSearchPanel = new NISSearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		nlrSearchPanel = new NLRSearchPanel(modelManager, infoPanelManager, viewer.getRenderWindowPanel());
		lineamentPanel = new LineamentControlPanel(modelManager);
		structureMapperPanel = new StructureMapperControlPanel(modelManager, pickManager);
		
		addTab("Eros", erosPanel);
		addTab("MSI", msiSearchPanel);
		addTab("NIS", nisSearchPanel);
		addTab("NLR", nlrSearchPanel);
		addTab("Lineament", lineamentPanel);
		addTab("Structures", structureMapperPanel);
	}
}
