package edu.jhuapl.near.gui.deimos;

import javax.swing.*;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.GenericPopupManager;

import java.awt.*;
import java.util.HashMap;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class DeimosViewer extends Viewer
{
	public static final String NAME = "Deimos";

	private JSplitPane splitPane;
	private Renderer renderer;
	private JTabbedPane controlPanel;
	private ModelManager modelManager;
	private PickManager pickManager;
	private GenericPopupManager popupManager;
	private StatusBar statusBar;
	private boolean initialized = false;
		
	public DeimosViewer(StatusBar statusBar)
	{
		super(new BorderLayout());
		this.statusBar = statusBar;
	}
	
	public void initialize()
	{
		if (initialized)
			return;
		
		setupModelManager();

		renderer = new Renderer(modelManager);

		popupManager = new GenericPopupManager(modelManager);

		pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

		controlPanel = new JTabbedPane();
		controlPanel.setBorder(BorderFactory.createEmptyBorder());
		controlPanel.addTab("Deimos", new SmallBodyControlPanel(modelManager, "Deimos"));
		controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
		splitPane.setOneTouchExpandable(true);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(800, 800));
        controlPanel.setMinimumSize(new Dimension(320, 100));
        controlPanel.setPreferredSize(new Dimension(320, 800));

		this.add(splitPane, BorderLayout.CENTER);

		initialized = true;
	}

	private void setupModelManager()
	{
		modelManager = new ModelManager();

		SmallBodyModel deimosModel = ModelFactory.createDeimosBodyModel();
    	Graticule graticule = ModelFactory.createDeimosGraticuleModel(deimosModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, deimosModel);
    	allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(deimosModel));
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(deimosModel));
    	allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(deimosModel));
    	allModels.put(ModelNames.CIRCLE_SELECTION, new RegularPolygonModel(deimosModel,20,false,"Selection",ModelNames.CIRCLE_SELECTION));
    	allModels.put(ModelNames.GRATICULE, graticule);

    	modelManager.setModels(allModels);

	}
	
	public Renderer getRenderer()
	{
		return renderer;
	}

	public String getName()
	{
		return NAME;
	}
}
