package edu.jhuapl.sbmt.stateHistory.controllers;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryColoringOptionsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryDisplayItemsController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.StateHistoryViewOptionsController;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;

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
	public StateHistoryViewControlsController(StateHistoryModel historyModel, Renderer renderer)
	{
		initUI(historyModel, renderer);
	}

	/**
	 * Initializes the user interface using the given <pre>historyModel</pre> and <pre>renderer</pre>
	 */
	private void initUI(StateHistoryModel historyModel, Renderer renderer)
	{
        viewControls = new StateHistoryViewOptionsController(historyModel, renderer);
        coloringControls = new StateHistoryColoringOptionsController(historyModel, renderer);
        displayItemsControls = new StateHistoryDisplayItemsController(historyModel, renderer);

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
		view.setBorder(new TitledBorder(null, "View Controls",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        view.setLayout(new BoxLayout(view, BoxLayout.X_AXIS));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(viewControls.getView());
        rightPanel.add(coloringControls.getView());

        view.add(displayItemsControls.getView());
        view.add(rightPanel);
		return view;
	}

}
