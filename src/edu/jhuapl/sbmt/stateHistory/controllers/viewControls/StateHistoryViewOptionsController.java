package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import javax.swing.DefaultComboBoxModel;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.StateHistoryViewOptionsModel;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.viewOptions.StateHistoryViewOptionsPanel;

import glum.item.ItemEventType;

/**
 * Controls the UI for the view options (look direction, lighting)
 * @author steelrj1
 *
 */
public class StateHistoryViewOptionsController
{
	/**
	 * The view governed by this controller
	 */
	private StateHistoryViewOptionsPanel view;

	/**
	 * Constructor.  Sets properties and initializes the view control panel
	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 */
	public StateHistoryViewOptionsController(StateHistoryRendererManager rendererManager)
	{
		initializeViewControlPanel(rendererManager);
	}

	/**
	 * Initializes the view control panel, sets action listeners, etc
	 *
 	 * @param runs			The collection of state history items
	 * @param renderer		The renderer being manipulated
	 */
	private void initializeViewControlPanel(StateHistoryRendererManager rendererManager)
	{
		DefaultComboBoxModel<RendererLookDirection> comboModelView = new DefaultComboBoxModel<RendererLookDirection>(
				RendererLookDirection.values());
		view = new StateHistoryViewOptionsPanel();
		StateHistoryViewOptionsModel model = new StateHistoryViewOptionsModel();
		StateHistoryCollection runs = rendererManager.getHistoryCollection();

		//set the model for the view options, and set the action listener
		view.getViewOptions().setModel(comboModelView);

		view.getViewOptions().addActionListener(e ->
		{
			rendererManager.updateLookDirection((RendererLookDirection) view.getViewOptions().getSelectedItem());
		});

        //Adds a property change listener for the StateHistoryCollection object to respond to POSITION_CHANGED events
        //This updates the lighting as well as the look direction
        rendererManager.addPropertyChangeListener(evt -> {

			if (!evt.getPropertyName().equals("POSITION_CHANGED")) return;
			RendererLookDirection selectedItem = (RendererLookDirection) view.getViewOptions().getSelectedItem();
			model.setRendererLookDirectionForStateHistory(selectedItem, runs.getCurrentRun());
		});

        //on a change in selection in the table, reset the time fraction and update the state history's look angle
        //and look direction
        rendererManager.addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
			rendererManager.setTimeFraction(0.0, runs.getCurrentRun());
			RendererLookDirection lookDir = model.getRendererLookDirectionForStateHistory(runs.getCurrentRun());
			if (lookDir == null) lookDir = RendererLookDirection.FREE_VIEW;
			view.getViewOptions().setSelectedItem(lookDir);
		});

		view.getViewOptions().setSelectedItem(RendererLookDirection.FREE_VIEW);
		rendererManager.setRendererLookDirection(RendererLookDirection.FREE_VIEW);
	}

	/**
	 * @return the view
	 */
	public StateHistoryViewOptionsPanel getView()
	{
		return view;
	}
}