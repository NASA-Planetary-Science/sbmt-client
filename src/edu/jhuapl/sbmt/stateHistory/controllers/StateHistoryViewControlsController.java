package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;

/**
 * Controller that governs the "View Controls" panel in the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsController
{
	/**
	 * The <pre>StateHistoryViewControlsPanel</pre> panel associated with this controller
	 */
	private StateHistoryViewControlsPanel view;

	/**
	 * Constructor.  Takes in a <pre>historyModel</pre> and <pre>renderer</pre> object to
	 * help populate the user interface
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.view = new StateHistoryViewControlsPanel(historyModel, renderer);
	}

	/**
	 * The panel associated with this controller
	 * @return
	 */
	public StateHistoryViewControlsPanel getView()
	{
		return view;
	}

}
