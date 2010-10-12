package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.jhuapl.near.gui.AbstractStructureMappingControlPanel;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.StructureModel;
import edu.jhuapl.near.model.eros.DEMModel;
import edu.jhuapl.near.model.eros.ProfileLineModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.pick.ProfileLinePicker;
import edu.jhuapl.near.popupmenus.GenericPopupManager;

public class TopoViewer extends JFrame
{
	public TopoViewer(String filename) throws IOException
	{
		ImageIcon erosIcon = new ImageIcon(getClass().getResource("/edu/jhuapl/near/data/eros.png"));
		setIconImage(erosIcon.getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    	StatusBar statusBar = new StatusBar();
    	add(statusBar, BorderLayout.PAGE_END);
    	
		ModelManager modelManager = new ModelManager();
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        DEMModel body = new DEMModel(filename);
        LineModel lineModel = new ProfileLineModel(body);
        lineModel.setMaximumVerticesPerLine(2);
        allModels.put(ModelNames.SMALL_BODY, body);
    	allModels.put(ModelNames.LINE_STRUCTURES, lineModel);
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(body));
    	allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(body));
    	allModels.put(ModelNames.CIRCLE_SELECTION, new RegularPolygonModel(body,20,false,"Selection",ModelNames.CIRCLE_SELECTION));
    	modelManager.setModels(allModels);

		Renderer renderer = new Renderer(modelManager);

		GenericPopupManager popupManager = new GenericPopupManager(modelManager);

		PickManager pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);
		pickManager.setLinePicker(new ProfileLinePicker(renderer, modelManager));
		
        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(400, 400));

        JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(renderer, BorderLayout.CENTER);

		TopoPlot plot = new TopoPlot(lineModel, body);
		plot.setMinimumSize(new Dimension(100, 100));
        plot.setPreferredSize(new Dimension(400, 400));
		
		panel.add(plot, BorderLayout.EAST);
		
		StructureModel structureModel = 
			(StructureModel)modelManager.getModel(ModelNames.LINE_STRUCTURES);
		JPanel lineStructuresMapperPanel = (new AbstractStructureMappingControlPanel(
				modelManager,
				structureModel,
				pickManager,
				PickManager.PickMode.LINE_DRAW,
				true) {});

		panel.add(lineStructuresMapperPanel, BorderLayout.SOUTH);

		add(panel, BorderLayout.CENTER);

        // Finally make the frame visible
        setTitle("Mapmaker View");
        pack();
        setVisible(true);
	}
}
