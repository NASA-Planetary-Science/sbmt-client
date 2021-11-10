package edu.jhuapl.sbmt.stateHistory.controllers.lidars;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.controllers.IPlannedDataController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedLidarTrackIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackScheduleCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.lidars.schedule.PlannedLidarTrackScheduleView;

/**
 * Controls the UI for the planned Lidar schedules
 * @author steelrj1
 *
 */
public class PlannedLidarScheduleTableController implements IPlannedDataController<PlannedLidarTrackScheduleView>
{
	PlannedLidarTrackScheduleView view;
	PlannedLidarTrackScheduleCollection collection;
	StateHistoryRendererManager rendererManager;
	IStateHistoryMetadata historyMetadata = null;
	ArrayList<PropertyChangeListener> pcls = new ArrayList<PropertyChangeListener>();

	public PlannedLidarScheduleTableController(ModelManager modelManager, StateHistoryRendererManager rendererManager, SmallBodyModel smallBodyModel, StatusNotifier statusNotifier)
	{
		this.rendererManager = rendererManager;
		collection = new PlannedLidarTrackScheduleCollection();

		this.view = new PlannedLidarTrackScheduleView(collection);

		//creates a new lidar collection for each schedule and loads it into the tool
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
						PlannedLidarTrackCollection lidarTrackCollection = new PlannedLidarTrackCollection(file.getAbsolutePath(), modelManager, smallBodyModel, rendererManager.getRenderer(), statusNotifier);
			        	if (rendererManager.getSelectedItems().size() > 0) historyMetadata = rendererManager.getSelectedItems().asList().get(0).getMetadata();
			        		lidarTrackCollection.setStateHistoryMetadata(historyMetadata);
			        	lidarTrackCollection.updateStateHistorySource(rendererManager.getHistoryCollection().getCurrentRun());
			        	lidarTrackCollection.setPercentageShown(0);
			        	pcls.forEach(listener -> lidarTrackCollection.addPropertyChangeListener(listener));
			        	lidarTrackCollection.addListener((aSource, aEventType) -> { refreshView(); });
			        	collection.addCollection(lidarTrackCollection);

						PlannedLidarTrackIOHelper.loadPlannedLidarTracksFromFileWithName(file.getAbsolutePath(), historyMetadata, lidarTrackCollection, new ProgressStatusListener()
						{

							@Override
							public void setProgressStatus(String status, int progress)
							{
								if (progress == 0) view.getTable().getProcessingLabel().setText(status);
								else view.getTable().getProcessingLabel().setText(status);
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
									view.getTable().getProcessingLabel().setText("Ready.");
									refreshView();
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
			updateSyncState();
		});

		view.getTable().getShowPlannedLidarTrackButton().addActionListener(e -> {

			collection.getSelectedItems().forEach( coll -> coll.setShowing(true));
			refreshView();
		});

		view.getTable().getHidePlannedLidarTrackButton().addActionListener(e -> {
			collection.getSelectedItems().forEach( coll -> coll.setShowing(false));
			refreshView();
		});

		view.getTable().getDeleteScheduleButton().addActionListener(e -> {
			collection.getSelectedItems().forEach( coll -> collection.removeCollection(coll));
			refreshView();
		});

		collection.addListener((aSource, aEventType) ->
		{
			refreshView();
		});
	}

	private void updateSyncState()
	{
		rendererManager.setSyncImages(view.getTable().getSyncWithTimelineButton().isSelected());
		collection.getAllItems().forEach( coll -> coll.updateFootprints() );
		rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
		refreshView();
	}

	/**
	 * Adds a property change listener to this controller
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcls.add(listener);
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
		ImmutableSet<PlannedLidarTrackCollection> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedLidarTrackCollection LidarTrackCollection : selectedItems)
		{
			if (LidarTrackCollection.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedLidarTrackButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedLidarTrackButton().setEnabled((selectedItems.size() > 0) && !allMapped);
		view.getTable().getDeleteScheduleButton().setEnabled(selectedItems.size() > 0);
	}

	@Override
	public PlannedLidarTrackScheduleView getView()
	{
		return view;
	}

	/**
	 * Returns the p
	 * @return the collection
	 */
	public PlannedLidarTrackScheduleCollection getCollection()
	{
		return collection;
	}

}
