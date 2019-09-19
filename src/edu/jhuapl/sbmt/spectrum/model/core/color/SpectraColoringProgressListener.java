package edu.jhuapl.sbmt.spectrum.model.core.color;

public interface SpectraColoringProgressListener
{
	public void coloringUpdateStarted();

	public void coloringUpdateProgressChanged(int percentComplete);

	public void coloringUpdateEnded();
}
