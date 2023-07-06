package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.time.TimeWindow;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.displayedInterval.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.stateHistory.ui.state.displayedInterval.StateHistoryPercentIntervalChanger;

import glum.item.ItemEventType;

/**
 * Controller that displays the "Displayed Interval" panel in the state history tab
 * @author steelrj1
 *
 */
public class StateHistoryDisplayedIntervalController
{
	/**
	 * JPanel for displaying the displayed interval controls
	 */
	private StateHistoryDisplayedIntervalPanel view;

	/**
	 * State History time model object
	 */
	private StateHistoryTimeModel timeModel;

	/**
	 * Constructor.
	 *
	 * Adds listeners to the <pre>intervalSet</pre> object as well as the
	 * <pre>StateHistoryDisplayedIntervalPanel</pre>'s time interval changer object
	 *
	 * @param interval			The current set of StateHistory intervals
	 */
	public StateHistoryDisplayedIntervalController(StateHistoryRendererManager rendererManager, StateHistoryTimeModel timeModel)
	{
		this.timeModel = timeModel;
		view = new StateHistoryDisplayedIntervalPanel();
		StateHistoryCollection intervalSet = rendererManager.getHistoryCollection();

		//If the selected item is changed, update the current run, reset the time range, and the time interval label
		rendererManager.addListener((aSource, aEventType) -> {

			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getHistoryCollection().getCurrentRun() == null) return;
			if (rendererManager.getSelectedItems().size() > 0)
			{
				IStateHistoryMetadata metadata = rendererManager.getSelectedItems().asList().get(0).getMetadata();
				intervalSet.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
				IStateHistoryTrajectoryMetadata trajectoryMetadata = intervalSet.getCurrentRun().getTrajectoryMetadata();

				timeModel.setTimeWindow(new TimeWindow(metadata.getStartTime(), metadata.getEndTime()));
				timeModel.setFractionDisplayed((trajectoryMetadata.getTrajectory().getMinDisplayFraction()), (trajectoryMetadata.getTrajectory().getMaxDisplayFraction()));

				updateDisplayedTimeRange();
				view.getTimeIntervalChanger().getSlider().setLowValue((int)(trajectoryMetadata.getTrajectory().getMinDisplayFraction()*255));
				view.getTimeIntervalChanger().getSlider().setHighValue((int)(trajectoryMetadata.getTrajectory().getMaxDisplayFraction()*255));
			}
			view.getTimeIntervalChanger().setEnabled(rendererManager.getSelectedItems().size() > 0);

		});

		//The action listener for the time interval changer; takes values from the changer
		//and passes them onto the current run so the display can properly update
		view.getTimeIntervalChanger().addActionListener(e -> {

			IStateHistoryTrajectoryMetadata trajectoryMetadata = intervalSet.getCurrentRun().getTrajectoryMetadata();
			StateHistoryPercentIntervalChanger changer = view.getTimeIntervalChanger();
			double minValue = changer.getLowValue();
			double maxValue = changer.getHighValue();
			timeModel.setFractionDisplayed(minValue, maxValue);
			updateDisplayedTimeRange();
			rendererManager.setTrajectoryMinMax(intervalSet.getCurrentRun(), minValue, maxValue);
			rendererManager.setTimeFraction(minValue, intervalSet.getCurrentRun());
			trajectoryMetadata.getTrajectory().setMinDisplayFraction(minValue);
			trajectoryMetadata.getTrajectory().setMaxDisplayFraction(maxValue);
			rendererManager.notify(intervalSet, ItemEventType.ItemsMutated);
			rendererManager.refreshColoring();
		});
	}

	/**
	 * Updates the displayed start/stop time label
	 */
	private void updateDisplayedTimeRange()
	{
		TimeWindow window = timeModel.getDisplayedTimeWindow();
		String minTime = TimeUtil.et2str(window.getStartTime());
		String maxTime = TimeUtil.et2str(window.getStopTime());
		view.getDisplayedStartTimeLabel().setText(minTime.substring(0, minTime.length()-3));
		view.getDisplayedStopTimeLabel().setText(maxTime.substring(0, maxTime.length()-3));

	}

	/**
	 * @return the view
	 */
	public StateHistoryDisplayedIntervalPanel getView()
	{
		return view;
	}
}
