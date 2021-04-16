package edu.jhuapl.sbmt.stateHistory.controllers.spectrometers;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.stateHistory.controllers.IPlannedDataController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.ui.spectrometers.PlannedSpectrumView;

public class PlannedSpectrumTableController implements IPlannedDataController<PlannedSpectrumView>
{
	PlannedSpectrumView view;
	PlannedSpectrumCollection collection;
	IStateHistoryMetadata historyMetadata = null;

	public PlannedSpectrumTableController(PlannedSpectrumCollection collection)
	{
		this.collection = collection;

		view = new PlannedSpectrumView(collection);
//		view.getTable().getLoadPlannedSpectrumButton().addActionListener(e -> {
//
//			File file = CustomFileChooser.showOpenDialog(view, "Select File");
//        	if (file == null) return;
//        	Runnable runner = new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					try
//					{
//						if (rendererManager.getSelectedItems().size() > 0) historyMetadata = rendererManager.getSelectedItems().asList().get(0).getMetadata();
//						PlannedSpectrumIOHelper.loadPlannedSpectraFromFileWithName(file.getAbsolutePath(), historyMetadata, collection, new ProgressStatusListener()
//						{
//
//							@Override
//							public void setProgressStatus(String status, int progress)
//							{
//								if (progress == 0) view.getTable().getProcessingLabel().setText(status);
//								else view.getTable().getProcessingLabel().setText(status);
//								view.getTable().repaint();
//								view.getTable().validate();
//							}
//						},
//						() -> {
//							SwingUtilities.invokeLater(new Runnable()
//							{
//								@Override
//								public void run()
//								{
//									view.getTable().getProcessingLabel().setText("Ready.");
//									refreshView();
//								}
//							});
//						});
//					}
//					catch (IOException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			};
//			Thread thread = new Thread(runner);
//			thread.start();
//		});
//
//		view.getTable().getSyncWithTimelineButton().addActionListener(e -> {
//			rendererManager.setSyncSpectra(view.getTable().getSyncWithTimelineButton().isSelected());
//			collection.updateFootprints();
//			rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
//			refreshView();
//		});

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
		ImmutableSet<PlannedSpectrum> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedSpectrum history : selectedItems)
		{
			if (history.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedSpectrumButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedSpectrumButton().setEnabled((selectedItems.size() > 0) && !allMapped);
	}

	/**
	 * @return the view
	 */
	public PlannedSpectrumView getView()
	{
		return view;
	}
}