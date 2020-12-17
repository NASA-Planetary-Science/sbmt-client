package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryDisplayItemsPanel;

/**
 * Controllers that governs the view which contains controls for which items to display in the renderer
 * @author steelrj1
 *
 */
public class StateHistoryDisplayItemsController implements ItemListener
{
	/**
	 * The view that this controller governs
	 */
	StateHistoryDisplayItemsPanel view;

	/**
	 * Constructor.  Sets state properties and initializes view control panel
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryDisplayItemsController(StateHistoryRendererManager rendererManager)
	{
		initializeViewControlPanel(rendererManager);
	}

	/**
	 * Initializes the view control panel, and sets up action listeners, etc
	 */
	private void initializeViewControlPanel(StateHistoryRendererManager rendererManager)
	{
		view = new StateHistoryDisplayItemsPanel();
		view.setStateHistoryCollection(rendererManager);
//		String[] distanceChoices =
//		{ "Distance to Center", "Distance to Surface" };
//		DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
	}

	@Override
	public void itemStateChanged(ItemEvent e) throws NullPointerException
	{
//		Object source = e.getItemSelectable();
//		StateHistory currentRun = runs.getCurrentRun();
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryDisplayItemsPanel getView()
	{
		return view;
	}
}
