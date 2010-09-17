package edu.jhuapl.near.gui.deimos;

import javax.swing.*;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.View;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
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
public class DeimosViewer extends View 
{
	public static final String NAME = "Deimos";

	private JSplitPane splitPane;
	private Renderer renderer;
	private JTabbedPane controlPanel;
	private ModelManager modelManager;
	private PickManager pickManager;
	private GenericPopupManager popupManager;
	private StatusBar statusBar;
		
	public DeimosViewer(StatusBar statusBar)
	{
		super(new BorderLayout());
		this.statusBar = statusBar;
	}
	
	public void initialize()
	{
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
	}

	private void setupModelManager()
	{
		modelManager = new ModelManager();

		final String[] modelNames = {
		"DEIMOS"};

		final String[] modelFiles = {
		"/DEIMOS/DEIMOS.vtk.gz"};

		final String[] coloringFiles = {
				"/DEIMOS/DEIMOS_Elevation.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalAcceleration.txt.gz",
				"/DEIMOS/DEIMOS_GravitationalPotential.txt.gz",
		"/DEIMOS/DEIMOS_Slope.txt.gz"};

		final String[] gridFiles = {
		"/DEIMOS/coordinate_grid_res0.vtk.gz"};

		final String imageMap = "/DEIMOS/deimos_image_map.png";

		SmallBodyModel deimosModel = new SmallBodyModel(modelNames, modelFiles, coloringFiles, imageMap, false, ModelNames.DEIMOS);
    	Graticule graticule = new Graticule(deimosModel, gridFiles);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.DEIMOS, deimosModel);
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
