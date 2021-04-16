package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.time.TimeWindow;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.stateHistory.ui.state.StateHistoryPercentIntervalChanger;
import edu.jhuapl.sbmt.stateHistory.ui.state.version2.StateHistoryDisplayedIntervalPanel;
import edu.jhuapl.sbmt.util.TimeUtil;

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
	 *
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
		StateHistoryCollection intervalSet = rendererManager.getRuns();


		//If the selected item is changed, update the current run, reset the time range, and the time interval label
		rendererManager.addListener((aSource, aEventType) -> {

			if (aEventType != ItemEventType.ItemsSelected) return;
			if (rendererManager.getRuns().getCurrentRun() == null) return;
//			if (intervalSet.getCurrentRun() == null) return;
			if (rendererManager.getSelectedItems().size() > 0)
			{
				IStateHistoryMetadata metadata = intervalSet.getCurrentRun().getMetadata();
				intervalSet.setCurrentRun(rendererManager.getSelectedItems().asList().get(0));
				timeModel.setTimeWindow(new TimeWindow(metadata.getStartTime(), metadata.getEndTime()));
				updateDisplayedTimeRange(0.0, 1.0);
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
			updateDisplayedTimeRange(minValue, maxValue);

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
	 * @param minValue the minimum fraction (between 0 and 1) of the entire interval being displayed
	 * @param maxValue the maximum fraction (between 0 and 1) of the entire interval being displayed
	 */
	private void updateDisplayedTimeRange(double minValue, double maxValue)
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
