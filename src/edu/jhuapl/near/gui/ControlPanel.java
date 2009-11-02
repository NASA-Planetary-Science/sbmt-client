package edu.jhuapl.near.gui;

import javax.swing.*;
import edu.jhuapl.near.model.ModelManager;


public class ControlPanel extends JTabbedPane
{
    private ErosRenderer viewer;
	private SearchPanel searchPanel;
	private ErosControlPanel erosPanel;
	private MSIControlPanel msiPanel;
	private LineamentControlPanel lineamentPanel;
    private ModelManager modelManager;
	
	public ControlPanel(ErosRenderer viewer, ModelManager modelManager)
	{
		this.viewer = viewer;
		this.modelManager = modelManager;
		
		searchPanel = new SearchPanel(modelManager);
		erosPanel = new ErosControlPanel(modelManager);
		msiPanel = new MSIControlPanel(modelManager);
		lineamentPanel = new LineamentControlPanel(modelManager);
		
		addTab("Search", searchPanel);
		addTab("Eros", erosPanel);
		addTab("MSI", msiPanel);
		addTab("Lineament", lineamentPanel);
	}
}
