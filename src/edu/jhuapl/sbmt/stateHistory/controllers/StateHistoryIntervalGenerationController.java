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

/**
 * Controller that governs the "Interval Generation" panel of the State History tab
 * @author steelrj1
 *
 */
public class StateHistoryIntervalGenerationController
{
	/**
	 * View governed by this controller
	 */
	private StateHistoryIntervalGenerationPanel view;

	/**
	 * Constructor
	 *
	 *
	 * @param historyModel  the underlying state history model
	 * @param newStart		the start date of the range that can be search
	 * @param newEnd		the end date of the range that can be searched
	 */
	public StateHistoryIntervalGenerationController(StateHistoryModel historyModel, DateTime newStart, DateTime newEnd)
	{
		initializeIntervalGenerationPanel(historyModel, newStart.toDate(), newEnd.toDate());
	}

	/**
	 * Initializes the panel.
	 */
	private void initializeIntervalGenerationPanel(StateHistoryModel historyModel, Date newStart, Date newEnd)
    {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
		view = new StateHistoryIntervalGenerationPanel();

        view.getAvailableTimeLabel().setText(dateFormatter.format(newStart)+ " to\n " + dateFormatter.format(newEnd));

        //Initialize and setup the spinner models for the start and end time for the interval generation
        SpinnerDateModel spinnerDateModel = new SpinnerDateModel(newStart, null, null, Calendar.DAY_OF_MONTH);
        view.getStartTimeSpinner().setModel(spinnerDateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(view.getStartTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS");
        view.getStartTimeSpinner().setEditor(dateEditor);

        spinnerDateModel = new SpinnerDateModel(newEnd, null, null, Calendar.DAY_OF_MONTH);
        view.getStopTimeSpinner().setModel(spinnerDateModel);
		dateEditor = new JSpinner.DateEditor(view.getStopTimeSpinner(), "yyyy-MMM-dd HH:mm:ss.SSS");
        view.getStopTimeSpinner().setEditor(dateEditor);

        //Adds an action listener to the "Get interval" button.
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

            //Setup a progress monitor to display status to the user while generating the state history
            //for the given interval
            ProgressMonitor progressMonitor = new ProgressMonitor(null, "Calculating History...", "", 0, 100);
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


	/**
	 * Returns the panel associated with this controller
	 * @return the panel associated with this controller
	 */
	public StateHistoryIntervalGenerationPanel getView()
	{
		return view;
	}
}
