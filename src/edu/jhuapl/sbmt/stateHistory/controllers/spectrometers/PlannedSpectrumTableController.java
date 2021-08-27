package edu.jhuapl.sbmt.stateHistory.controllers.spectrometers;

import edu.jhuapl.sbmt.stateHistory.controllers.PlannedDataTableController;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.ui.spectrometers.PlannedSpectrumView;

/**
 * Class that controls the UI for an individual planned spectrum schedule
 * @author steelrj1
 *
 */
public class PlannedSpectrumTableController extends PlannedDataTableController<PlannedSpectrumView, PlannedSpectrum>
{
	public PlannedSpectrumTableController(PlannedSpectrumCollection collection)
	{
		this.collection = collection;
		view = new PlannedSpectrumView(collection);

		view.getTable().getShowPlannedSpectrumButton().addActionListener(e -> {
			collection.getSelectedItems().forEach(item -> collection.setDataShowing(item, true));
			refreshView();
		});

		view.getTable().getHidePlannedSpectrumButton().addActionListener(e -> {
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
		view.getTable().getHidePlannedSpectrumButton().setEnabled((selectedSize > 0) && allMapped);
		view.getTable().getShowPlannedSpectrumButton().setEnabled((selectedSize > 0) && !allMapped);
	}
}