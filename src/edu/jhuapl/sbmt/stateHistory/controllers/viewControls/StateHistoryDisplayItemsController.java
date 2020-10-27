package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
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
	 * The collection of state history elements
	 */
	private StateHistoryCollection runs;

	/**
	 * The renderer that is being updated by the elements governed by this controller
	 */
	private Renderer renderer;

	/**
	 * Constructor.  Sets state properties and initializes view control panel
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryDisplayItemsController(StateHistoryCollection runs, Renderer renderer)
	{
		this.runs = runs;
		this.renderer = renderer;
		initializeViewControlPanel();
	}

	/**
	 * Initializes the view control panel, and sets up action listeners, etc
	 */
	private void initializeViewControlPanel()
	{
		view = new StateHistoryDisplayItemsPanel();
		view.setStateHistoryCollection(runs);

		String[] distanceChoices =
		{ "Distance to Center", "Distance to Surface" };
		DefaultComboBoxModel<String> comboModelDistance = new DefaultComboBoxModel<String>(distanceChoices);
	}

	@Override
	public void itemStateChanged(ItemEvent e) throws NullPointerException
	{
		Object source = e.getItemSelectable();
		StateHistory currentRun = runs.getCurrentRun();
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
