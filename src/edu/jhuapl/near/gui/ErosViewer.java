package edu.jhuapl.near.gui;

import javax.swing.*;

import vtk.vtkRenderWindowPanel;

import edu.jhuapl.near.gui.ControlPanel;
import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.PopupManager;

import java.awt.*;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program. 
 * @author kahneg1
 *
 */
public class ErosViewer extends JPanel 
{
	private JSplitPane splitPane;
	private ErosRenderer renderer;
	private ControlPanel controlPanel;
	private ModelManager modelManager;
	private PickManager pickManager;
	private PopupManager popupManager;
	private ModelInfoWindowManager infoPanelManager;
	
	public ErosViewer(StatusBar statusBar)
	{
		modelManager = new ModelManager();
	
		infoPanelManager = new ModelInfoWindowManager(modelManager);
		
		renderer = new ErosRenderer(modelManager);

		popupManager = new PopupManager(renderer, modelManager, infoPanelManager);

		pickManager = new PickManager(renderer, statusBar, modelManager, infoPanelManager, popupManager);

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
}
