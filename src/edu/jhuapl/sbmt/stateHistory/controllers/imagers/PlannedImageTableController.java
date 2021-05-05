package edu.jhuapl.sbmt.stateHistory.controllers.imagers;

import edu.jhuapl.sbmt.stateHistory.controllers.PlannedDataTableController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.PlannedImageView;

/**
 * Class that controls the UI for an individual planned image schedule
 * @author steelrj1
 *
 */
public class PlannedImageTableController extends PlannedDataTableController<PlannedImageView, PlannedImage>
{
	IStateHistoryMetadata historyMetadata = null;

	public PlannedImageTableController(PlannedImageCollection collection)
	{
		this.collection = collection;
		view = new PlannedImageView(collection);

		view.getTable().getShowPlannedImageButton().addActionListener(e -> {
			collection.getSelectedItems().forEach(item -> collection.setDataShowing(item, true));
			refreshView();
		});

		view.getTable().getHidePlannedImageButton().addActionListener(e -> {
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
		view.getTable().getHidePlannedImageButton().setEnabled((selectedSize > 0) && allMapped);
		view.getTable().getShowPlannedImageButton().setEnabled((selectedSize > 0) && !allMapped);
	}
}