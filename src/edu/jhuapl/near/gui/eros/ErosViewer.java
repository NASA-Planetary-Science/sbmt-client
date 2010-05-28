package edu.jhuapl.near.gui.eros;

import javax.swing.*;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.View;
import edu.jhuapl.near.model.eros.ErosModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.eros.ErosPopupManager;

import java.awt.*;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program. 
 * @author kahneg1
 *
 */
public class ErosViewer extends View
{
	public static final String NAME = "Eros";

	private JSplitPane splitPane;
	private Renderer renderer;
	private ControlPanel controlPanel;
	private ErosModelManager modelManager;
	private PickManager pickManager;
	private ErosPopupManager popupManager;
	private ModelInfoWindowManager infoPanelManager;
	
	public ErosViewer(StatusBar statusBar)
	{
		super(new BorderLayout());

		modelManager = new ErosModelManager();
	
		infoPanelManager = new ModelInfoWindowManager(modelManager);
		
		renderer = new Renderer(modelManager);

		popupManager = new ErosPopupManager(renderer, modelManager, infoPanelManager);

		pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new ControlPanel(renderer, modelManager, infoPanelManager, pickManager);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
		splitPane.setOneTouchExpandable(true);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(800, 800));
        controlPanel.setMinimumSize(new Dimension(320, 100));
        controlPanel.setPreferredSize(new Dimension(320, 800));

		this.add(splitPane, BorderLayout.CENTER);
	}
	
	public vtkRenderWindowPanel getRenderWindowPanel()
	{
		return renderer.getRenderWindowPanel();
	}

	public String getName()
	{
		return NAME;
	}
}
