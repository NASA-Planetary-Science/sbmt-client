package edu.jhuapl.sbmt.stateHistory.controllers;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryDisplayedIntervalPanel;

import glum.item.ItemEventType;

public class StateHistoryDisplayedIntervalController
{
	private StateHistoryDisplayedIntervalPanel view;
	private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
	private StateHistoryModel historyModel;

	public StateHistoryDisplayedIntervalController(StateHistoryModel historyModel)
	{
		this.historyModel = historyModel;
		view = new StateHistoryDisplayedIntervalPanel();

		historyModel.getRuns().addListener((aSource, aEventType) -> {

			if (aEventType != ItemEventType.ItemsSelected) return;
			if (historyModel.getRuns().getSelectedItems().size() > 0)
				historyModel.getRuns().setCurrentRun(historyModel.getRuns().getSelectedItems().asList().get(0));
			view.getTimeIntervalChanger().setEnabled(historyModel.getRuns().getSelectedItems().size() > 0);
//					runs.setTimeFraction(runs.getCurrentRun(), 0.0);
//					updateLookDirection();
		});

		view.getTimeIntervalChanger().addActionListener(e -> {
			PercentIntervalChanger changer = view.getTimeIntervalChanger();
			double minValue = changer.getLowValue();
			double maxValue = changer.getHighValue();
			double timeFraction = historyModel.getRuns().getCurrentRun().getTimeFraction();
			historyModel.getRuns().setTrajectoryMinMax(historyModel.getRuns().getCurrentRun(), minValue, maxValue);
			historyModel.getRuns().getCurrentRun().setMinDisplayFraction(minValue);
			historyModel.getRuns().getCurrentRun().setMaxDisplayFraction(maxValue);
			historyModel.getRuns().getCurrentRun().setTimeFraction(historyModel.getCurrentFlybyStateHistory(), timeFraction);

			updateDisplayedTimeRange(minValue, maxValue);
		});

		historyModel.getRuns().addListener((aSource, aEventType) ->
		{
			if (aEventType != ItemEventType.ItemsSelected) return;
			if (historyModel.getRuns().getSelectedItems().size() > 0) updateDisplayedTimeRange(0.0, 1.0);
		});
	}

	private void updateDisplayedTimeRange(double minValue, double maxValue)
	{
		Double period = historyModel.getRuns().getCurrentRun().getPeriod();
		Date start = new Date(historyModel.getStartTime().toDate().getTime() + new Double(1000*minValue * period).longValue());
		Date end = new Date(historyModel.getStartTime().toDate().getTime() + new Double(1000*maxValue * period).longValue());
		view.getDisplayedStartTimeLabel().setText(new DateTime(start).toString(fmt));
		view.getDisplayedStopTimeLabel().setText(new DateTime(end).toString(fmt));
	}

	public StateHistoryDisplayedIntervalPanel getView()
	{
		return view;
	}
}
