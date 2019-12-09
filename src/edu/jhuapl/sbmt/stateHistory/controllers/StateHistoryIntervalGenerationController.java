package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

public class StateHistoryIntervalGenerationController
{

	public StateHistoryIntervalGenerationController()
	{
		// TODO Auto-generated constructor stub
	}

	private void initializeIntervalGenerationPanel()
    {
        view.getAvailableTimeLabel().setEditable(false); // as before
        view.getAvailableTimeLabel().setBackground(null); // this is the same as a JLabel
        view.getAvailableTimeLabel().setBorder(null);
        view.getAvailableTimeLabel().setText(new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newStart)+ " to\n " + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS").format(newEnd));


        view.getStartTimeSpinner().setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newStart.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStartTimeSpinner().setEditor(new javax.swing.JSpinner.DateEditor(view.getStartTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));

        view.getStopTimeSpinner().setModel(new javax.swing.SpinnerDateModel(new java.util.Date(newEnd.getTime()), null, null, java.util.Calendar.DAY_OF_MONTH));
        view.getStopTimeSpinner().setEditor(new javax.swing.JSpinner.DateEditor(view.getStopTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS"));


        view.getGetIntervalButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                Date beginTime = (Date) view.getStartTimeSpinner().getModel()
                        .getValue();
                Date stopTime = (Date) view.getStopTimeSpinner().getModel()
                        .getValue();
                double total = (stopTime.getTime() - beginTime.getTime())
                        / (24.0 * 60.0 * 60.0 * 1000.0);
                DateTime dtStart = new DateTime(beginTime);
                DateTime dtEnd = new DateTime(stopTime);
                DateTime dtStart2 = ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(dtStart.toString());
                DateTime dtEnd2 = ISODateTimeFormat.dateTimeParser()
                        .parseDateTime(dtEnd.toString());
                // TODO check key generation
                // generate random stateHistoryKey to use for this interval
                StateHistoryKey key = new StateHistoryKey(runs);
                StateHistoryModel newInterval = new StateHistoryModel(
                        dtStart2, dtEnd2, bodyModel, renderer);
                if (newInterval.createNewTimeInterval(
                        StateHistoryController.this, total, "") > 0)
                {
                    view.getTable().addInterval(newInterval, renderer);
                    //                    timeTablePanel.addIntervalToTable(newInterval, renderer);
                }
                // once we add an interval, enable the view options
                //                view.getViewControlPanel().setEnabled(true);
                setViewControlPanelEnabled(true);
                String currentView = (String)view.getViewOptions().getSelectedItem();
                if (currentView.equals(viewChoices.SPACECRAFT.toString()))
                    view.getShowSpacecraft().setEnabled(false); // show spacecraft should be disabled if spacecraft view
                view.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

}
