package edu.jhuapl.near.gui;

public class FileDownloadSwingWorker extends ProgressBarSwingWorker
{
	private String filename;
	public void downloadFile(String filename)
	{
		this.filename = filename;
		this.execute();
	}
	
	@Override
	protected Object doInBackground() throws Exception
	{
		return null;
	}
}
