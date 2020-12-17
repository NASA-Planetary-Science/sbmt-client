package edu.jhuapl.sbmt.stateHistory.controllers.imagers;

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
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.imagers.PlannedImageView;

public class PlannedImageTableController
{
	PlannedImageView view;
	PlannedImageCollection collection;
	StateHistoryRendererManager rendererManager;

	public PlannedImageTableController(final ModelManager modelManager, StateHistoryRendererManager rendererManager, PlannedImageCollection collection, SmallBodyViewConfig config)
	{
		this.collection = collection;
		this.rendererManager = rendererManager;
		view = new PlannedImageView(collection, config);
		view.getTable().getLoadPlannedImageButton().addActionListener(e -> {

			File file = CustomFileChooser.showOpenDialog(view, "Select File");
        	if (file == null) return;
        	Runnable runner = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						collection.loadPlannedImagesFromFileWithName(file.getAbsolutePath(), new ProgressStatusListener()
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
			rendererManager.setSyncImages(view.getTable().getSyncWithTimelineButton().isSelected());
			collection.updateFootprints();
			rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
			refreshView();
		});

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
