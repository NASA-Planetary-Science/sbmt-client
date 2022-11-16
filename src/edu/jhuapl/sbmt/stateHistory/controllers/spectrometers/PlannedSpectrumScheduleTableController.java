package edu.jhuapl.sbmt.stateHistory.controllers.spectrometers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.controllers.IPlannedDataController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedSpectrumIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumScheduleCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.spectrometers.schedule.PlannedSpectrumScheduleView;

/**
 * Controls the UI forthe planned spectrum schedules
 * @author steelrj1
 *
 */
public class PlannedSpectrumScheduleTableController implements IPlannedDataController<PlannedSpectrumScheduleView>
{
	PlannedSpectrumScheduleView view;
	PlannedSpectrumScheduleCollection collection;
	StateHistoryRendererManager rendererManager;
	IStateHistoryMetadata historyMetadata = null;
	ArrayList<PropertyChangeListener> pcls = new ArrayList<PropertyChangeListener>();

	public PlannedSpectrumScheduleTableController(StateHistoryRendererManager rendererManager, SmallBodyModel smallBodyModel)
	{
		this.rendererManager = rendererManager;
		collection = new PlannedSpectrumScheduleCollection();

		this.view = new PlannedSpectrumScheduleView(collection);

		//creates a new spectrum collection for each schedule and loads it into the tool
		view.getTable().getLoadPlannedSpectrumButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;

        	Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						PlannedSpectrumCollection spectrumCollection = new PlannedSpectrumCollection(file.getAbsolutePath(), smallBodyModel);
			        	if (rendererManager.getSelectedItems().size() > 0) historyMetadata = rendererManager.getSelectedItems().asList().get(0).getMetadata();
			        		spectrumCollection.setStateHistoryMetadata(historyMetadata);
			        	spectrumCollection.updateStateHistorySource(rendererManager.getHistoryCollection().getCurrentRun());
			        	pcls.forEach(listener -> spectrumCollection.addPropertyChangeListener(listener));
			        	spectrumCollection.addListener((aSource, aEventType) -> { refreshView(); });
			        	collection.addCollection(spectrumCollection);

						PlannedSpectrumIOHelper.loadPlannedSpectraFromFileWithName(file.getAbsolutePath(), historyMetadata, spectrumCollection, new ProgressStatusListener()
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

		view.getTable().getShowPlannedSpectrumButton().addActionListener(e -> {

			collection.getSelectedItems().forEach( coll -> coll.setShowing(true));
			refreshView();
		});

		view.getTable().getHidePlannedSpectrumButton().addActionListener(e -> {
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
		ImmutableSet<PlannedSpectrumCollection> selectedItems = collection.getSelectedItems();
		boolean allMapped = true;
		for (PlannedSpectrumCollection spectrumCollection : selectedItems)
		{
			if (spectrumCollection.isShowing() == false) allMapped = false;
		}
		view.getTable().getHidePlannedSpectrumButton().setEnabled((selectedItems.size() > 0) && allMapped);
		view.getTable().getShowPlannedSpectrumButton().setEnabled((selectedItems.size() > 0) && !allMapped);
		view.getTable().getDeleteScheduleButton().setEnabled(selectedItems.size() > 0);
	}

	@Override
	public PlannedSpectrumScheduleView getView()
	{
		return view;
	}

	/**
	 * Returns the collection for this controller
	 * @return the collection
	 */
	public PlannedSpectrumScheduleCollection getCollection()
	{
		return collection;
	}
}