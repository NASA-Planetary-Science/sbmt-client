package edu.jhuapl.near.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 * This abstract class simplifies running long tasks in the background while
 * showing a popup showing the progress. To use it, subclass this class and define
 * the doInBackground method
 * @author eli
 *
 */
abstract public class ProgressMonitorSwingWorker extends SwingWorker<Void, Void>
	implements PropertyChangeListener
{
	private ProgressMonitor progressMonitor;
	
	public ProgressMonitorSwingWorker(Component c)
	{
		progressMonitor = new ProgressMonitor(c,
				"Downloading file",
				"", 0, 100);
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(1);
        progressMonitor.setMillisToPopup(1);
        
        addPropertyChangeListener(this);
	}
	
	public void propertyChange(PropertyChangeEvent evt)
	{
        if ("progress".equals(evt.getPropertyName()))
        {
        	int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message = String.format("Completed %d%%\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || isDone())
            {
                if (progressMonitor.isCanceled())
                    cancel(true);
            }
        }
    }
	
}
