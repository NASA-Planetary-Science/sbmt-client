package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import javax.swing.JSpinner;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerDateModel;

import org.joda.time.DateTime;

import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.ui.version2.DateTimeSpinner;
import edu.jhuapl.sbmt.stateHistory.ui.version2.StateHistoryIntervalGenerationPanel;

public class StateHistoryIntervalGenerationController
{
	private StateHistoryIntervalGenerationPanel view;
    private Date newStart;
    private Date newEnd;
    private StateHistoryModel historyModel;
    private SimpleDateFormat dateFormatter;
    private SpinnerDateModel spinnerDateModel;
    private JSpinner.DateEditor dateEditor;
    private ProgressMonitor progressMonitor;

	public StateHistoryIntervalGenerationController(StateHistoryModel historyModel, DateTime newStart, DateTime newEnd)
	{
		this.historyModel = historyModel;
		this.newStart = newStart.toDate();
		this.newEnd = newEnd.toDate();
		dateFormatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
		initializeIntervalGenerationPanel();
	}

	private void initializeIntervalGenerationPanel()
    {
		view = new StateHistoryIntervalGenerationPanel();
        view.getAvailableTimeLabel().setEditable(false);
        view.getAvailableTimeLabel().setBackground(null);
        view.getAvailableTimeLabel().setBorder(null);
        view.getAvailableTimeLabel().setText(dateFormatter.format(newStart)+ " to\n " + dateFormatter.format(newEnd));

        spinnerDateModel = new SpinnerDateModel(newStart, null, null, Calendar.DAY_OF_MONTH);
        view.getStartTimeSpinner().setModel(spinnerDateModel);
		dateEditor = new JSpinner.DateEditor(view.getStartTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS");
        view.getStartTimeSpinner().setEditor(dateEditor);

        spinnerDateModel = new SpinnerDateModel(newEnd, null, null, Calendar.DAY_OF_MONTH);
        view.getStopTimeSpinner().setModel(spinnerDateModel);
		dateEditor = new JSpinner.DateEditor(view.getStopTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS");
        view.getStopTimeSpinner().setEditor(dateEditor);

        view.getGetIntervalButton().addActionListener(e -> {
            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            double total = DateTimeSpinner.getTimeSpanBetween(view.getStartTimeSpinner(), view.getStopTimeSpinner());
            DateTime dtStart2 = view.getStartTimeSpinner().getISOFormattedTime();
            DateTime dtEnd2 = view.getStopTimeSpinner().getISOFormattedTime();
            historyModel.setStartTime(dtStart2);
            historyModel.setEndTime(dtEnd2);

            // TODO check key generation
            // generate random stateHistoryKey to use for this interval
            StateHistoryKey key = new StateHistoryKey(historyModel.getRuns());

            progressMonitor = new ProgressMonitor(null, "Calculating History...", "", 0, 100);
    		progressMonitor.setProgress(0);
        	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
    		{
    			@Override
    			protected Void doInBackground() throws Exception
    			{
    				int success = historyModel.createNewTimeInterval(key, total, "", new Function<Double, Void>()
					{
						@Override
						public Void apply(Double t)
						{
							progressMonitor.setProgress(t.intValue());
							return null;
						}
					});
    		        return null;
    			}
    		};
    		task.execute();


            view.setCursor(Cursor.getDefaultCursor());
        });
    }



	public StateHistoryIntervalGenerationPanel getView()
	{
		return view;
	}
}
