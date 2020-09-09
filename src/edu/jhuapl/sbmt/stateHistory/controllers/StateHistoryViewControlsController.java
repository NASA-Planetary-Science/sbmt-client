package edu.jhuapl.sbmt.stateHistory.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryColoringOptionsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryDisplayItemsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryViewOptionsController;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.viewOptions.RendererLookDirection;

/**
 * Controller that governs the "View Controls" panel in the StateHistory tab
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsController
{

	/**
	 * The controller that governs the "Display Items" sub panel
	 */
	private StateHistoryDisplayItemsController displayItemsControls;

	/**
	 * The controller that governs the "Coloring Options" sub panel
	 */
	private StateHistoryColoringOptionsController coloringControls;

	/**
	 * The controller that governs the "View Options" sub panel
	 */
	private StateHistoryViewOptionsController viewControls;

	/**
	 * Constructor.  Takes in a <pre>historyModel</pre> and <pre>renderer</pre> object to
	 * help populate the user interface
	 * @param historyModel
	 * @param renderer
	 */
	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer, ColoringDataManager coloringDataManager)
	{
		initUI(historyModel, renderer, coloringDataManager);
	}

	/**
	 * Initializes the user interface using the given <pre>historyModel</pre> and <pre>renderer</pre>
	 */
	private void initUI(StateHistoryModel historyModel, Renderer renderer, ColoringDataManager coloringDataManager)
	{
        viewControls = new StateHistoryViewOptionsController(historyModel.getRuns(), renderer);
        coloringControls = new StateHistoryColoringOptionsController(historyModel.getRuns(), renderer, coloringDataManager);
        displayItemsControls = new StateHistoryDisplayItemsController(historyModel.getRuns(), renderer);

        //this is a cross panel listener action, so set it up here, above the 3 controllers
        viewControls.getView().getViewOptions().addActionListener(e ->
        {
        	// toggle the ability to show the spacecraft depending on what
			// mode we're in
        	RendererLookDirection selectedView = (RendererLookDirection) viewControls.getView().getViewOptions().getSelectedItem();
			boolean scSelected = (selectedView == RendererLookDirection.SPACECRAFT);
			displayItemsControls.getView().getShowSpacecraftPanel().getShowSpacecraft().setEnabled(!scSelected);
        });
	}

	public void setEnabled(boolean enabled)
	{
		viewControls.getView().setEnabled(enabled);
		coloringControls.getView().setEnabled(enabled);
		displayItemsControls.getView().setEnabled(enabled);
	}


	/**
	 * The panel associated with this controller
	 * @return
	 */
	public JPanel getView()
	{
		JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.add(displayItemsControls.getView());
        view.add(viewControls.getView());
        view.add(coloringControls.getView());
		return view;
	}
}
