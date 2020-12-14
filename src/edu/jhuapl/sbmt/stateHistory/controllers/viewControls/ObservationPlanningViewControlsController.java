package edu.jhuapl.sbmt.stateHistory.controllers.viewControls;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.sbmt.stateHistory.controllers.StateHistoryViewControlsController;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
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
		StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
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
    	System.out.println("ObservationPlanningViewControlsController: getView: getting controls");
    	return viewControlsController.getView();
//    	JPanel viewControlsPanel = viewControlsController.getView();
//    	JPanel panel = new JPanel();
//    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//    	panel.add(viewControlsPanel);
//    	return panel;
    }
}
