package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.controllers.StateHistoryViewControlsController;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import glum.item.ItemEventType;

public class ObservationPlanningViewControlsController
{
    /**
     * Controller for the view controls panel
     */
    private StateHistoryViewControlsController viewControlsController;

	public ObservationPlanningViewControlsController(StateHistoryModel historyModel, final ModelManager modelManager, StateHistoryRendererManager rendererManager, ColoringDataManager coloringDataManager)
	{
		this.viewControlsController = new StateHistoryViewControlsController(historyModel, rendererManager, coloringDataManager);
		viewControlsController.getView().setEnabled(false);

		rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			viewControlsController.setEnabled(rendererManager.getSelectedItems().size() > 0);
		});
	}

	/**
     * Returns a JPanel made of the child views that comprise this parent view
     * @return
     */
    public JPanel getView()
    {
    	return viewControlsController.getView();
    }
}
