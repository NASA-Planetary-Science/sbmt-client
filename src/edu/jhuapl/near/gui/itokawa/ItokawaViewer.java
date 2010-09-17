package edu.jhuapl.near.gui.itokawa;

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
public class ItokawaViewer extends View 
{
	public static final String NAME = "Itokawa";

	private JSplitPane splitPane;
	private Renderer renderer;
	private JTabbedPane controlPanel;
	private ModelManager modelManager;
	private PickManager pickManager;
	private GenericPopupManager popupManager;
	private StatusBar statusBar;
		
	public ItokawaViewer(StatusBar statusBar)
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
		controlPanel.addTab("Itokawa", new SmallBodyControlPanel(modelManager, "Itokawa"));
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
			"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver64q",
			"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver128q",
			"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver256q",
			"HAY_A_AMICA_5_ITOKAWASHAPE_V1_0 ver512q"};

		final String[] modelFiles = {
			"/ITOKAWA/ver64q.vtk.gz",
			"/ITOKAWA/ver128q.vtk.gz",
			"/ITOKAWA/ver256q.vtk.gz",
			"/ITOKAWA/ver512q.vtk.gz"};

		final String[] coloringFiles = null;

		final String[] gridFiles = {
			"/ITOKAWA/coordinate_grid_res0.vtk.gz",
			"/ITOKAWA/coordinate_grid_res1.vtk.gz",
			"/ITOKAWA/coordinate_grid_res2.vtk.gz",
			"/ITOKAWA/coordinate_grid_res3.vtk.gz"};

		SmallBodyModel itokawaModel = new SmallBodyModel(modelNames, modelFiles, coloringFiles, null, false, ModelNames.ITOKAWA);
    	Graticule graticule = new Graticule(itokawaModel, gridFiles);
    	graticule.setShiftAmount(0.0005);
    	
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.ITOKAWA, itokawaModel);
    	allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(itokawaModel));
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(itokawaModel));
    	allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(itokawaModel));
    	allModels.put(ModelNames.CIRCLE_SELECTION, new RegularPolygonModel(itokawaModel,20,false,"Selection",ModelNames.CIRCLE_SELECTION));
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
