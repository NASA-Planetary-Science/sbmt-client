package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryViewControlsPanel;

/**
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsController
{
	public boolean earthEnabled = true;
	private StateHistoryViewControlsPanel view;

	/**
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer)
	{
		this.view = new StateHistoryViewControlsPanel(historyModel, renderer);
		initializeViewControlPanel();
	}

	/**
	 *
	 */
	private void initializeViewControlPanel()
    {
        view.updateUI();
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
