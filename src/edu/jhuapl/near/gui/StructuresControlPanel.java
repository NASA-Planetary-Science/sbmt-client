package edu.jhuapl.near.gui;

import javax.swing.JTabbedPane;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.pick.PickManager;

public class StructuresControlPanel extends JTabbedPane
{
	private AbstractStructureMappingControlPanel lineStructuresMapperPanel;
	private AbstractStructureMappingControlPanel circleStructuresMapperPanel;
	private AbstractStructureMappingControlPanel pointsStructuresMapperPanel;

	public StructuresControlPanel(
			ModelManager modelManager,
			PickManager pickManager)
	{
		StructureModel structureModel = 
			(StructureModel)modelManager.getModel(ModelManager.LINE_STRUCTURES);
		lineStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.LINE_DRAW,
				true) {});

		structureModel = 
			(StructureModel)modelManager.getModel(ModelManager.CIRCLE_STRUCTURES);
		circleStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.CIRCLE_DRAW,
				true) {});
				
		pointsStructuresMapperPanel = new PointsMappingControlPanel(
				modelManager,
				pickManager);
				
		addTab("Paths", lineStructuresMapperPanel);
		addTab("Circles", circleStructuresMapperPanel);
		addTab("Points", pointsStructuresMapperPanel);
	}

}
