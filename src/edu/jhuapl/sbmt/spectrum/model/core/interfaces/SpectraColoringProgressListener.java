package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

public interface SpectraColoringProgressListener
{
	/**
	 * Signifies that a coloring update has changed
	 */
	public void coloringUpdateStarted();

	/**
	 * Signifies that the coloring update is <pre>percentComplete</pre>
	 * @param percentComplete
	 *
	 */
	public void coloringUpdateProgressChanged(int percentComplete);

	/**
	 * Signifies that the coloring update has ended.
	 */
	public void coloringUpdateEnded();
}
