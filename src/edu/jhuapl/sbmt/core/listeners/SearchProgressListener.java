package edu.jhuapl.sbmt.core.listeners;

/**
 * Interface for sending back information on the progress of a search.  It doesn't necessarily need to be used with a UI elements, but that was where it was originally used.
 * @author steelrj1
 *
 */
public interface SearchProgressListener
{
	/**
	 * The search has started
	 */
	public void searchStarted();

	/**
	 * The search is now <pre>percentComplete</pre> of 100%.
	 * @param percentComplete
	 */
	public void searchProgressChanged(int percentComplete);

	/**
	 * The search has ended.
	 */
	public void searchEnded();

	/**
	 * This search is indeterminate; setup something like a progress bar with an indeterminate state to show this.
	 */
	public void searchIndeterminate();

	/**
	 * Updates the note displayed in the progress dialog
	 * @param note
	 */
	public void searchNoteUpdated(String note);
}
