package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.StateHistoryFOVPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.viewControls.viewOptions.table.ViewOptionsTableView;

import glum.item.ItemEventType;

/**
 * @author steelrj1
 *
 */
public class StateHistoryFOVController
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryFOVPanel view;

	/**
	 * The collection of state history items
	 */
	private StateHistoryCollection runs;

	/**
	 * Constructor.  Sets properties and initializes the view control panel
	 * @param runs
	 * @param renderer
	 */
	public StateHistoryFOVController(StateHistoryRendererManager rendererManager)
	{
		this.runs = rendererManager.getRuns();
		initializeViewControlPanel(rendererManager);
	}

	/**
	 * Initializes the view control panel, sets action listeners, etc
	 */
	private void initializeViewControlPanel(StateHistoryRendererManager rendererManager)
	{
		view = new StateHistoryFOVPanel();
		view.setAvailableFOVs(runs.getAvailableFOVs());

        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsChanged) return;
			view.setAvailableFOVs(runs.getAvailableFOVs());
		});

		ViewOptionsTableView tableView = new ViewOptionsTableView(rendererManager);
		tableView.setup();
		view.setTableView(tableView);
	}

	/**
	 * @return the view
	 */
	public StateHistoryFOVPanel getView()
	{
		return view;
	}
}