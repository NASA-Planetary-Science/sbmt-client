package edu.jhuapl.sbmt.stateHistory.controllers.spectrometers;

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
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.spectrometers.PlannedSpectrumView;

public class PlannedSpectrumTableController
{
	PlannedSpectrumView view;
	PlannedSpectrumCollection collection;
	StateHistoryRendererManager rendererManager;

	public PlannedSpectrumTableController(final ModelManager modelManager, StateHistoryRendererManager rendererManager, PlannedSpectrumCollection collection, SmallBodyViewConfig config)
	{
		view = new PlannedSpectrumView(collection, config);
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
						collection.loadPlannedSpectraFromFileWithName(file.getAbsolutePath(), new ProgressStatusListener()
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
			rendererManager.setSyncSpectra(view.getTable().getSyncWithTimelineButton().isSelected());
			collection.updateFootprints();
			rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
			refreshView();
		});

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