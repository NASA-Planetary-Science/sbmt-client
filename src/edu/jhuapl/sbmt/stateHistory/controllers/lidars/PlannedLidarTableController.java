package edu.jhuapl.sbmt.stateHistory.controllers.lidars;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.lidars.PlannedLidarTrackView;

public class PlannedLidarTableController
{
	PlannedLidarTrackView view;
	final PlannedLidarTrackCollection collection;
	StateHistoryRendererManager rendererManager;

	public PlannedLidarTableController(final ModelManager modelManager, StateHistoryRendererManager rendererManager, PlannedLidarTrackCollection collection, SmallBodyViewConfig config)
	{
		this.collection = collection;
		this.rendererManager = rendererManager;
		view = new PlannedLidarTrackView(collection, config);
		view.getTable().getLoadPlannedLidarTrackButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
    		Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						collection.loadPlannedLidarTracksFromFileWithName(file.getAbsolutePath(), new ProgressStatusListener()
						{

							@Override
							public void setProgressStatus(String status, int progress)
							{
								if (progress == 0) view.getTable().getProcessingLabel().setText(status);
								else view.getTable().getProcessingLabel().setText(status + " (" + progress +"%)");
								view.getTable().repaint();
								view.getTable().validate();
							}
						},
						() -> {
							SwingUtilities.invokeLater(new Runnable()
							{
								@Override
								public void run()
								{
									collection.setPercentageShown(0);
								}
							});
						});
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			Thread thread = new Thread(runner);
			thread.start();
		});

		view.getTable().getSyncWithTimelineButton().addActionListener(e -> {
			rendererManager.setSyncLidar(view.getTable().getSyncWithTimelineButton().isSelected());
			collection.updateFootprints();
			rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
			refreshView();
		});

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

	private void refreshView()
	{
		updateButtonState(rendererManager);
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

	private void updateButtonState(StateHistoryRendererManager rendererManager)
	{
		ImmutableSet<PlannedLidarTrack> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedLidarTrack history : selectedItems)
		{
			if (history.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedLidarTrackButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedLidarTrackButton().setEnabled((selectedItems.size() > 0) && !allMapped);
	}

	/**
	 * @return the view
	 */
	public PlannedLidarTrackView getView()
	{
		return view;
	}
}