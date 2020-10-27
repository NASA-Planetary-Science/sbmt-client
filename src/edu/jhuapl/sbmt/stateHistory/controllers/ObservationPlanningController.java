package edu.jhuapl.sbmt.stateHistory.controllers;

import java.beans.PropertyChangeEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.lidars.PlannedLidarTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.spectrometers.PlannedSpectrumTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.viewControls.ObservationPlanningViewControlsController;
import edu.jhuapl.sbmt.stateHistory.model.DefaultStateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedDataActorRegister;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.ui.ObservationPlanningView;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.StateHistoryIntervalPlaybackPanel;

import glum.item.ItemEventType;

public class ObservationPlanningController
{
	StateHistoryController stateHistoryController;
	ObservationPlanningView view = new ObservationPlanningView();
	PlannedImageTableController imageTableController;
	PlannedSpectrumTableController spectrumTableController;
	PlannedLidarTableController lidarTableController;
	PlannedDataActorRegister registrar;
	ObservationPlanningViewControlsController viewControlsController;
	StateHistoryIntervalPlaybackPanel intervalPlaybackPanel;
	PlannedImageCollection imageCollection;
	PlannedSpectrumCollection spectrumCollection;
	PlannedLidarTrackCollection lidarTrackCollection;
	StateHistoryTimeModel timeModel;

    /**
     * Controller for the interval playback panel
     */
    private StateHistoryIntervalPlaybackController intervalPlaybackController;

	public ObservationPlanningController(final ModelManager modelManager, SmallBodyModel smallBodyModel, Renderer renderer, SmallBodyViewConfig config, ColoringDataManager coloringDataManager)
	{
		timeModel = new StateHistoryTimeModel();
		stateHistoryController = new StateHistoryController(modelManager, renderer, timeModel);
		viewControlsController = new ObservationPlanningViewControlsController(stateHistoryController.getHistoryModel(), modelManager, renderer, coloringDataManager);
		this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(stateHistoryController.getHistoryModel(), renderer, timeModel);


		view.addTab("S/C Trajectory", stateHistoryController.getView());
		intervalPlaybackController.getView().setEnabled(false);
		registrar = new PlannedDataActorRegister();

		stateHistoryController.getHistoryModel().addStateHistoryModelChangedListener(new DefaultStateHistoryModelChangedListener() {

			@Override
			public void timeChanged(Double t)
			{
				// TODO Auto-generated method stub
				super.timeChanged(t);
				imageCollection.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, t));
				lidarTrackCollection.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, t));
			}
		});

		imageCollection = new PlannedImageCollection(smallBodyModel);
		imageTableController = new PlannedImageTableController(modelManager, renderer, imageCollection, config);
		if (config.imagingInstruments.length > 0) view.addTab("Imagery", imageTableController.getView());

		spectrumCollection = new PlannedSpectrumCollection(smallBodyModel);
		spectrumTableController = new PlannedSpectrumTableController(modelManager, renderer, spectrumCollection, config);
		if (config.hasSpectralData) view.addTab("Spectra", spectrumTableController.getView());

		lidarTrackCollection = new PlannedLidarTrackCollection(modelManager, smallBodyModel, renderer);
		lidarTableController = new PlannedLidarTableController(modelManager, renderer, lidarTrackCollection, config);
		if (config.hasLidarData) view.addTab("LIDAR", lidarTableController.getView());
		view.addTab("View Controls", viewControlsController.getView());

		StateHistoryCollection runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
        runs.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			intervalPlaybackController.getView().setEnabled(runs.getSelectedItems().size() > 0);
			imageCollection.updateStateHistorySource(runs.getCurrentRun());
			lidarTrackCollection.updateStateHistorySource(runs.getCurrentRun());
			runs.clearPlannedScience();
			runs.addPlannedScience(imageCollection.getProps());
			runs.addPlannedScience(lidarTrackCollection.getProps());
		});

        intervalPlaybackPanel = intervalPlaybackController.getView();

	}

	public JPanel getView()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(view);
		panel.add(intervalPlaybackPanel);
		return panel;
	}
}