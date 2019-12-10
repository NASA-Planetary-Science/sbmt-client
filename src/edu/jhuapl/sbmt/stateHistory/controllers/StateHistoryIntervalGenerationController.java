package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.ui.version2.DateTimeSpinner;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalGenerationPanel;

public class StateHistoryIntervalGenerationController
{
	StateHistoryIntervalGenerationPanel view;
    Date newStart;
    Date newEnd;
    private StateHistoryModel historyModel;

	public StateHistoryIntervalGenerationController(StateHistoryModel historyModel, DateTime newStart, DateTime newEnd)
	{
		this.historyModel = historyModel;
		this.newStart = newStart.toDate();
		this.newEnd = newEnd.toDate();
		initializeIntervalGenerationPanel();
	}

	private void initializeIntervalGenerationPanel()
    {
		view = new StateHistoryIntervalGenerationPanel();
        view.getAvailableTimeLabel().setEditable(false);
        view.getAvailableTimeLabel().setBackground(null);
        view.getAvailableTimeLabel().setBorder(null);
        view.getAvailableTimeLabel().setText(new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newStart)+ " to\n " + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newEnd));

        view.getStartTimeSpinner().setModel(new SpinnerDateModel(newStart, null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStartTimeSpinner().setEditor(new JSpinner.DateEditor(view.getStartTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getStopTimeSpinner().setModel(new SpinnerDateModel(newEnd, null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStopTimeSpinner().setEditor(new JSpinner.DateEditor(view.getStopTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getGetIntervalButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                double total = DateTimeSpinner.getTimeSpanBetween(view.getStartTimeSpinner(), view.getStopTimeSpinner());
                DateTime dtStart2 = view.getStartTimeSpinner().getISOFormattedTime();
                DateTime dtEnd2 = view.getStopTimeSpinner().getISOFormattedTime();
                historyModel.setStartTime(dtStart2);
                historyModel.setEndTime(dtEnd2);

                // TODO check key generation
                // generate random stateHistoryKey to use for this interval
                StateHistoryKey key = new StateHistoryKey(historyModel.getRuns());
                int success = historyModel.createNewTimeInterval(key, total, "");
                view.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

	public StateHistoryIntervalGenerationPanel getView()
	{
		return view;
	}
}
