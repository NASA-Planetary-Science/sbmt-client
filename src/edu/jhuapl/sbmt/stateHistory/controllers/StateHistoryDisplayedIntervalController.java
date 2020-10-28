package edu.jhuapl.sbmt.stateHistory.controllers;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.time.BaseStateHistoryTimeModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.time.StateHistoryTimeModel;
import edu.jhuapl.sbmt.stateHistory.model.time.TimeWindow;
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
	public StateHistoryDisplayedIntervalController(StateHistoryCollection intervalSet, StateHistoryTimeModel timeModel)
	{
		this.timeModel = timeModel;
		view = new StateHistoryDisplayedIntervalPanel();

		//If the selected item is changed, update the current run, reset the time range, and the time interval label
		intervalSet.addListener((aSource, aEventType) -> {

			if (aEventType != ItemEventType.ItemsSelected) return;
			if (intervalSet.getSelectedItems().size() > 0)
			{
				intervalSet.setCurrentRun(intervalSet.getSelectedItems().asList().get(0));
				System.out
				.println("StateHistoryDisplayedIntervalController: StateHistoryDisplayedIntervalController: updating dispalyed window");
				timeModel.setTimeWindow(new TimeWindow(intervalSet.getCurrentRun().getStartTime(), intervalSet.getCurrentRun().getEndTime()));
				updateDisplayedTimeRange(0.0, 1.0);
			}
			view.getTimeIntervalChanger().setEnabled(intervalSet.getSelectedItems().size() > 0);

		});

		//The action listener for the time interval changer; takes values from the changer
		//and passes them onto the current run so the display can properly update
		view.getTimeIntervalChanger().addActionListener(e -> {

			StateHistoryPercentIntervalChanger changer = view.getTimeIntervalChanger();
			double minValue = changer.getLowValue();
			double maxValue = changer.getHighValue();
			timeModel.setFractionDisplayed(minValue, maxValue);

			intervalSet.setTrajectoryMinMax(intervalSet.getCurrentRun(), minValue, maxValue);
			intervalSet.getCurrentRun().getTrajectory().setMinDisplayFraction(minValue);
			intervalSet.getCurrentRun().getTrajectory().setMaxDisplayFraction(maxValue);
			intervalSet.notify(intervalSet, ItemEventType.ItemsMutated);
		});

		timeModel.addTimeModelChangeListener(new BaseStateHistoryTimeModelChangedListener() {
			@Override
			public void fractionDisplayedChanged(double minFractionDisplayed, double maxFractionDisplayed)
			{
				super.fractionDisplayedChanged(minFractionDisplayed, maxFractionDisplayed);
				updateDisplayedTimeRange(minFractionDisplayed, minFractionDisplayed);
			}
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
