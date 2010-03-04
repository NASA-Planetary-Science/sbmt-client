package edu.jhuapl.near.gui;

import javax.swing.*;

import edu.jhuapl.near.gui.pick.PickManager;
import edu.jhuapl.near.model.*;


public class ControlPanel extends JTabbedPane
{
	private ErosControlPanel erosPanel;
	private MSISearchPanel msiSearchPanel;
	private NISSearchPanel nisSearchPanel;
	private NLRSearchPanel nlrSearchPanel;
	private LineamentControlPanel lineamentPanel;
	private StructureMapperControlPanel lineStructuresMapperPanel;
	private StructureMapperControlPanel circleStructuresMapperPanel;
	private StructureMapperControlPanel pointsStructuresMapperPanel;
		
	
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

		StructureModel structureModel = 
			(StructureModel)modelManager.getModel(ModelManager.LINE_STRUCTURES);
		lineStructuresMapperPanel = new StructureMapperControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.LINE_DRAW);

		structureModel = 
			(StructureModel)modelManager.getModel(ModelManager.CIRCLE_STRUCTURES);
		circleStructuresMapperPanel = new StructureMapperControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.CIRCLE_DRAW);
		
		structureModel = 
			(StructureModel)modelManager.getModel(ModelManager.POINT_STRUCTURES);
		pointsStructuresMapperPanel = new StructureMapperControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.POINT_DRAW);
		
		addTab("Eros", erosPanel);
		addTab("MSI", msiSearchPanel);
		addTab("NIS", nisSearchPanel);
		addTab("NLR", nlrSearchPanel);
		addTab("Lineament", lineamentPanel);
		addTab("Paths", lineStructuresMapperPanel);
		addTab("Circles", circleStructuresMapperPanel);
	}
}
