package edu.jhuapl.near.gui.itokawa;

import javax.swing.*;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.View;
import edu.jhuapl.near.model.itokawa.ItokawaModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.GenericPopupManager;

import java.awt.*;

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
	private ItokawaControlPanel controlPanel;
	private ItokawaModelManager modelManager;
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
		modelManager = new ItokawaModelManager();
	
		renderer = new Renderer(modelManager);

		popupManager = new GenericPopupManager(modelManager);

		pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new ItokawaControlPanel(modelManager, pickManager);

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
