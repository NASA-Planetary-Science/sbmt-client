package edu.jhuapl.sbmt.stateHistory.controllers;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import org.joda.time.DateTime;

import com.jidesoft.utils.SwingWorker;

import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice.SpiceStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.ui.DateTimeSpinner;
import edu.jhuapl.sbmt.stateHistory.ui.state.intervalGeneration.StateHistoryIntervalGenerationPanel;

import crucible.core.mechanics.providers.lockable.LockableEphemerisLinkEvaluationException;

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
	private void initializeIntervalGenerationPanel(StateHistoryModel historyModel, Date newStart, Date newEnd)// throws Exception
    {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
		view = new StateHistoryIntervalGenerationPanel(historyModel);

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
            double totalDays = DateTimeSpinner.getDaysBetween(view.getStartTimeSpinner(), view.getStopTimeSpinner());

    		// check length of interval - if more than 10, warn the user before proceeding
    		if (view.getStateHistorySourceType() == StateHistorySourceType.PREGEN && totalDays > 10.0)
    		{
    			int result = JOptionPane.showConfirmDialog(getView(),
    					"The interval you selected is longer than 10 days and may take a while to generate. \nAre you sure you want to create it?");
    			if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
    				return;
    		}

            DateTime startTime = view.getStartTimeSpinner().getISOFormattedTime();
            DateTime endTime = view.getStopTimeSpinner().getISOFormattedTime();

            // TODO check key generation
            // generate random stateHistoryKey to use for this interval
            StateHistoryKey key = new StateHistoryKey(historyModel.getHistoryCollection());

            historyModel.setIntervalGenerator(view.getStateHistorySourceType());

            if (view.getStateHistorySourceType() == StateHistorySourceType.SPICE)
        	{
            	SpiceInfo spice = historyModel.getViewConfig().getSpiceInfo();
            	if (spice == null)
            	{
          			throw new RuntimeException("ERROR: SpiceInfo is not available for this model; please contact SBMT support.");
            	}
            	((SpiceStateHistoryIntervalGenerator)historyModel.getActiveIntervalGenerator()).setMetaKernelFile(view.getMetakernelToLoad(), spice);
        	}

            //Setup a progress monitor to display status to the user while generating the state history
            //for the given interval
            ProgressMonitor progressMonitor = new ProgressMonitor(null, "Calculating History...", "", 0, 100);
    		progressMonitor.setProgress(0);
        	SwingWorker<Void, Void> task = new SwingWorker<Void, Void>()
    		{
    			@Override
    			protected Void doInBackground() throws Exception
    			{
    				try
    				{
	    				historyModel.createNewTimeInterval(key, startTime, endTime, totalDays, "", new Function<Double, Void>()
						{
							@Override
							public Void apply(Double t)
							{
								progressMonitor.setProgress(t.intValue());
								return null;
							}
						});
    				}
    				catch (LockableEphemerisLinkEvaluationException lelee)
    				{
    					JOptionPane.showMessageDialog(null, "This time is not valid for this kernel set; please check the ranges and try again", "Error",
    		                    JOptionPane.ERROR_MESSAGE);
    				}
    				catch (StateHistoryInputException shie)
    				{
    					JOptionPane.showMessageDialog(null, shie.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    				}
    				catch (InvocationTargetException | InterruptedException ie)
    				{
    					if (ie.getCause().getClass() == LockableEphemerisLinkEvaluationException.class)
    					{
    						JOptionPane.showMessageDialog(null, "This time is not valid for this kernel set; please check the ranges and try again", "Error",
        		                    JOptionPane.ERROR_MESSAGE);
    					}
    					else {
	    					JOptionPane.showMessageDialog(null, "Error adding state history; see console for details", "Error", JOptionPane.ERROR_MESSAGE);
	    					ie.printStackTrace();
    					}
    				}

    				catch (Exception e)
    				{
    					e.printStackTrace();
    				}
    		        return null;
    			}
    		};
    		task.execute();

            view.setCursor(Cursor.getDefaultCursor());
            ((JFrame) SwingUtilities.getRoot(view)).dispose();
        });


        view.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				view.repaint();
			}
		});
    }

	/**
	 * @return the view
	 */
	public StateHistoryIntervalGenerationPanel getView()
	{
		return view;
	}
}