package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.jhuapl.sbmt.gui.lidar.PercentIntervalChanger;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryDisplayedIntervalPanel;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

public class StateHistoryDisplayedIntervalController
{
	private StateHistoryDisplayedIntervalPanel view;
	private StateHistory currentHistory;
	private StateHistoryModel historyModel;

	public StateHistoryDisplayedIntervalController(StateHistoryModel historyModel)
	{
		view = new StateHistoryDisplayedIntervalPanel();
		this.historyModel = historyModel;

		historyModel.getRuns().addListener(new ItemEventListener()
		{

			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				if (aEventType == ItemEventType.ItemsSelected)
				{
					System.out.println(
							"StateHistoryDisplayedIntervalController.StateHistoryDisplayedIntervalController(...).new ItemEventListener() {...}: handleItemEvent: item selected");
					if (historyModel.getRuns().getSelectedItems().size() > 0)
						historyModel.getRuns().setCurrentRun(historyModel.getRuns().getSelectedItems().asList().get(0));
					view.getTimeIntervalChanger().setEnabled(historyModel.getRuns().getSelectedItems().size() > 0);
//					runs.setTimeFraction(runs.getCurrentRun(), 0.0);
//					updateLookDirection();
				}
			}
		});

		view.getTimeIntervalChanger().addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				PercentIntervalChanger changer = view.getTimeIntervalChanger();
				double min = changer.getLowValue();
				double max = changer.getHighValue();
//				System.out.println(
//						"StateHistoryDisplayedIntervalController.StateHistoryDisplayedIntervalController(...).new ActionListener() {...}: actionPerformed: min max " + min + " " + max);
				double timeFraction = historyModel.getRuns().getCurrentRun().getTimeFraction();
//				System.out.println(
//						"StateHistoryDisplayedIntervalController.StateHistoryDisplayedIntervalController(...).new ActionListener() {...}: actionPerformed: time fraction " + timeFraction);
				historyModel.getRuns().setTrajectoryMinMax(historyModel.getRuns().getCurrentRun(), min, max);
				historyModel.getRuns().getCurrentRun().setMinDisplayFraction(min);
				historyModel.getRuns().getCurrentRun().setMaxDisplayFraction(max);
				historyModel.getRuns().getCurrentRun().setTimeFraction(historyModel.getCurrentFlybyStateHistory(), timeFraction);

			}
		});
	}

	public void setCurrentHistory(StateHistory currentHistory)
	{
		this.currentHistory = currentHistory;
	}

	public StateHistoryDisplayedIntervalPanel getView()
	{
		return view;
	}

}
