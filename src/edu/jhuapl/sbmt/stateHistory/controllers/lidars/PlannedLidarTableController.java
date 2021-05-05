package edu.jhuapl.sbmt.stateHistory.controllers.lidars;

import edu.jhuapl.sbmt.stateHistory.controllers.PlannedDataTableController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.ui.lidars.PlannedLidarTrackView;

/**
 * Controls the UI for an individual planned lidar schedule
 * @author steelrj1
 *
 */
public class PlannedLidarTableController extends PlannedDataTableController<PlannedLidarTrackView, PlannedLidarTrack>
{
	IStateHistoryMetadata historyMetadata = null;

	public PlannedLidarTableController(PlannedLidarTrackCollection collection)
	{
		this.collection = collection;
		view = new PlannedLidarTrackView(collection);

		view.getTable().getShowPlannedLidarTrackButton().addActionListener(e -> {
			collection.getSelectedItems().forEach(item -> collection.setDataShowing(item, true));
			refreshView();
		});

		view.getTable().getHidePlannedLidarTrackButton().addActionListener(e -> {
			collection.getSelectedItems().forEach(item -> collection.setDataShowing(item, false));
			refreshView();
		});

		collection.addListener((aSource, aEventType) ->
		{
			refreshView();
		});
	}

	@Override
	protected void updateButtonState()
	{
		super.updateButtonState();
		int selectedSize = collection.getSelectedItems().size();
		view.getTable().getHidePlannedLidarTrackButton().setEnabled((selectedSize > 0) && allMapped);
		view.getTable().getShowPlannedLidarTrackButton().setEnabled((selectedSize > 0) && !allMapped);
	}
}