package edu.jhuapl.sbmt.spectrum.controllers.standard;

public interface SearchProgressListener
{
	public void searchStarted();

	public void searchProgressChanged(int percentComplete);

	public void searchEnded();

	public void searchIndeterminate();
}
