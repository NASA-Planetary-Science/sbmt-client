package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

public interface SpectrumAppearanceListener
{
	public void spectrumFootprintVisbilityChanged(BasicSpectrum renderer, boolean isVisible);

	public void spectrumBoundaryVisibilityChanged(BasicSpectrum renderer, boolean isVisible);
}
