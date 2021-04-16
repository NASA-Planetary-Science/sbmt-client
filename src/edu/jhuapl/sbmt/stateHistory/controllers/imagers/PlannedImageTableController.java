package edu.jhuapl.sbmt.stateHistory.controllers.imagers;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.stateHistory.controllers.IPlannedDataController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.PlannedImageView;

public class PlannedImageTableController implements IPlannedDataController<PlannedImageView>
{
	PlannedImageView view;
	PlannedImageCollection collection;
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

	private void refreshView()
	{
		updateButtonState();
		SwingUtilities.invokeLater(new Runnable()
		{

			@Override
			public void run()
			{
				view.repaint();
	            view.validate();
			}
		});
	}

	private void updateButtonState()
	{
		ImmutableSet<PlannedImage> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedImage history : selectedItems)
		{
			if (history.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedImageButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedImageButton().setEnabled((selectedItems.size() > 0) && !allMapped);
	}

	/**
	 * @return the view
	 */
	public PlannedImageView getView()
	{
		return view;
	}
}
