package edu.jhuapl.near.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JTabbedPane;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.pick.PickManager;

public class StructuresControlPanel extends JTabbedPane
{
    private AbstractStructureMappingControlPanel lineStructuresMapperPanel;
    private AbstractStructureMappingControlPanel circleStructuresMapperPanel;
    private AbstractStructureMappingControlPanel ellipseStructuresMapperPanel;
    private AbstractStructureMappingControlPanel pointsStructuresMapperPanel;

    public StructuresControlPanel(
            ModelManager modelManager,
            PickManager pickManager)
    {
        StructureModel structureModel =
            (StructureModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
        lineStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                modelManager,
                structureModel,
                pickManager,
                PickManager.PickMode.LINE_DRAW,
                false) {});

        structureModel =
            (StructureModel)modelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
        circleStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                modelManager,
                structureModel,
                pickManager,
                PickManager.PickMode.CIRCLE_DRAW,
                false) {});

        structureModel =
            (StructureModel)modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
        ellipseStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
                modelManager,
                structureModel,
                pickManager,
                PickManager.PickMode.ELLIPSE_DRAW,
                false) {});

        pointsStructuresMapperPanel = new PointsMappingControlPanel(
                modelManager,
                pickManager);

        addTab("Paths", lineStructuresMapperPanel);
        addTab("Circles", circleStructuresMapperPanel);
        addTab("Ellipses", ellipseStructuresMapperPanel);
        addTab("Points", pointsStructuresMapperPanel);

        this.addComponentListener(new ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                lineStructuresMapperPanel.setEditingEnabled(false);
                circleStructuresMapperPanel.setEditingEnabled(false);
                ellipseStructuresMapperPanel.setEditingEnabled(false);
                pointsStructuresMapperPanel.setEditingEnabled(false);
            }
        });
    }
}
