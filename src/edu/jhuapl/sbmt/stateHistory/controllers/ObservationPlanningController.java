package edu.jhuapl.sbmt.stateHistory.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataManager;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.controllers.imagers.PlannedImageScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.lidars.PlannedLidarScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.controllers.spectrometers.PlannedSpectrumScheduleTableController;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedDataActorRegister;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.BaseStateHistoryTimeModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.playback.StateHistoryIntervalPlaybackPanel;

import glum.item.ItemEventType;

public class ObservationPlanningController implements PropertyChangeListener
{
	StateHistoryController stateHistoryController;
	JTabbedPane view = new JTabbedPane();
	PlannedImageScheduleTableController imageScheduleTableController;
	PlannedSpectrumScheduleTableController spectrumScheduleTableController;
	PlannedLidarScheduleTableController lidarScheduleTableController;
	PlannedDataController plannedDataController;
	PlannedDataActorRegister registrar;
	StateHistoryViewControlsController viewControlsController;
	StateHistoryIntervalPlaybackPanel intervalPlaybackPanel;
	StateHistoryTimeModel timeModel;
	StateHistoryRendererManager rendererManager;

    /**
     * Controller for the interval playback panel
     */
    private StateHistoryIntervalPlaybackController intervalPlaybackController;

	public ObservationPlanningController(final ModelManager modelManager, SmallBodyModel smallBodyModel, StateHistoryRendererManager rendererManager,
										 SmallBodyViewConfig config, ColoringDataManager coloringDataManager, StatusNotifier statusNotifier)
	{
		timeModel = StateHistoryTimeModel.getInstance();
		StateHistoryCollection runs = rendererManager.getHistoryCollection();
		stateHistoryController = new StateHistoryController(modelManager, rendererManager, timeModel);
		viewControlsController = new StateHistoryViewControlsController(stateHistoryController.getHistoryModel(), rendererManager, coloringDataManager);
		stateHistoryController.setViewControlsController(viewControlsController);
		this.intervalPlaybackController = new StateHistoryIntervalPlaybackController(rendererManager, timeModel, statusNotifier);
		this.rendererManager = rendererManager;
		view.addTab("S/C Trajectory", stateHistoryController.getView());
		intervalPlaybackController.getView().setEnabled(false);
		registrar = new PlannedDataActorRegister();
		this.rendererManager = rendererManager;

		timeModel.addTimeModelChangeListener(new BaseStateHistoryTimeModelChangedListener() {

			@Override
			public void timeChanged(double et)
			{
				super.timeChanged(et);
				if (imageScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncImages())
					imageScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));
				if (spectrumScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncSpectra())
					spectrumScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));
				if (lidarScheduleTableController.getCollection().getNumItems() > 0 && rendererManager.isSyncLidar())
					lidarScheduleTableController.getCollection().getAllItems().forEach( coll -> coll.propertyChange(new PropertyChangeEvent(this, PlannedDataProperties.TIME_CHANGED, null, et)));

                try
				{
                	if (runs.getCurrentRun() != null)
                		runs.getCurrentRun().getMetadata().setCurrentTime(et);
				}
				catch (StateHistoryInvalidTimeException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		});

		imageScheduleTableController = new PlannedImageScheduleTableController(rendererManager, smallBodyModel);
		imageScheduleTableController.addPropertyChangeListener(this);

		spectrumScheduleTableController = new PlannedSpectrumScheduleTableController(rendererManager, smallBodyModel);
		spectrumScheduleTableController.addPropertyChangeListener(this);

		lidarScheduleTableController = new PlannedLidarScheduleTableController(modelManager, rendererManager, smallBodyModel);
		lidarScheduleTableController.addPropertyChangeListener(this);

		plannedDataController = new PlannedDataController();
		plannedDataController.addChildController(imageScheduleTableController);
		plannedDataController.addChildController(spectrumScheduleTableController);
		plannedDataController.addChildController(lidarScheduleTableController);
		view.addTab("Planned Data", plannedDataController.getView());

        rendererManager.addListener((aSource, aEventType) -> {
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
			intervalPlaybackController.getView().setEnabled(rendererManager.getSelectedItems().size() > 0);
			intervalPlaybackController.currentOffsetTime = 0.0;
			timeModel.setTime(runs.getCurrentRun().getMetadata().getStartTime());
			imageScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
			spectrumScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
			lidarScheduleTableController.getCollection().getAllItems().forEach(coll -> coll.updateStateHistorySource(runs.getCurrentRun()));
			viewControlsController.setEnabled(rendererManager.getSelectedItems().size() > 0);
			ObservationPlanningController.this.updatePlannedScience();
		});

        intervalPlaybackPanel = intervalPlaybackController.getView();

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("PLANNED_IMAGES_CHANGED") || evt.getPropertyName().equals("PLANNED_SPECTRA_CHANGED") || evt.getPropertyName().equals("PLANNED_LIDAR_CHANGED"))
		{
			updatePlannedScience();
		}
	}

	private void updatePlannedScience()
	{
		rendererManager.clearPlannedScience();
		imageScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
		spectrumScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
		lidarScheduleTableController.getCollection().getAllItems().forEach(coll -> rendererManager.addPlannedScienceActors(coll.getProps()));
		rendererManager.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
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