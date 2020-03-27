package edu.jhuapl.sbmt.stateHistory.ui.version2;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.RendererLookDirection;
import edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls.StateHistoryColoringOptionsController;
import edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls.StateHistoryDisplayItemsController;
import edu.jhuapl.sbmt.stateHistory.ui.version2.viewControls.StateHistoryViewOptionsController;

/**
 * @author steelrj1
 *
 */
public class StateHistoryViewControlsPanel extends JPanel
{
	private StateHistoryDisplayItemsController displayItemsControls;
	private StateHistoryColoringOptionsController coloringControls;
	private StateHistoryViewOptionsController viewControls;

	/**
	 *
	 */
	public StateHistoryViewControlsPanel(StateHistoryModel historyModel, Renderer renderer)
	{
		initUI(historyModel, renderer);
	}

	/**
	 *
	 */
	private void initUI(StateHistoryModel historyModel, Renderer renderer)
	{
		setBorder(new TitledBorder(null, "View Controls",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        viewControls =
        		new StateHistoryViewOptionsController(historyModel, renderer);

        coloringControls =
        		new StateHistoryColoringOptionsController(historyModel);
        displayItemsControls =
        		new StateHistoryDisplayItemsController(historyModel, renderer);

        //this is a cross panel listener action, so set it up here, above the 3 controllers
        viewControls.getView().getViewOptions().addActionListener(e ->
        {
        	// toggle the ability to show the spacecraft depending on what
			// mode we're in
        	RendererLookDirection selectedView = (RendererLookDirection) viewControls.getView().getViewOptions().getSelectedItem();
			boolean scSelected = (selectedView == RendererLookDirection.SPACECRAFT);
			displayItemsControls.getView().getShowSpacecraftPanel().getShowSpacecraft().setEnabled(!scSelected);
        });

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(viewControls.getView());
        rightPanel.add(coloringControls.getView());

        add(displayItemsControls.getView());
        add(rightPanel);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		viewControls.getView().setEnabled(enabled);
		coloringControls.getView().setEnabled(enabled);
		displayItemsControls.getView().setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/**
	 *
	 */
	public void updateUI()
	{
//		displayItemsControls.getView().getShowSpacecraftPanel().updateGui();
	}
}
