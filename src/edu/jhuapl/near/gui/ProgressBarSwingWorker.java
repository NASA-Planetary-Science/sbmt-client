package edu.jhuapl.near.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

abstract public class ProgressBarSwingWorker implements PropertyChangeListener
{
	private SwingWorker<Object, Object> swingWorker;
	private JProgressBar progressBar;
	
	public ProgressBarSwingWorker()
	{
		progressBar = new JProgressBar(0, 100);
		
	}
	
	public void execute()
	{
		final ProgressBarSwingWorker thisClass = this;
		
		swingWorker = new SwingWorker<Object, Object>()
		{
			protected Object doInBackground() throws Exception
			{
				return thisClass.doInBackground();
			}
		};
		
		swingWorker.addPropertyChangeListener(this);
		
		swingWorker.execute();
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
        if ("progress".equals(evt.getPropertyName()))
        {
            progressBar.setValue((Integer)evt.getNewValue());
        }
    }

	public SwingWorker<Object, Object> getSwingWorker()
	{
		return swingWorker;
	}
	
	abstract protected Object doInBackground() throws Exception;
}
