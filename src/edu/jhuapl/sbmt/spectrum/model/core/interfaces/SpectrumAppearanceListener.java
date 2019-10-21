package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Listener for monitoring visibility changes for spectra
 * @author steelrj1
 *
 */
public interface SpectrumAppearanceListener<S extends BasicSpectrum>
{
	/**
	 * Signifies whether spectrum visibility has changed
	 * @param renderer
	 * @param isVisible
	 */
	public void spectrumFootprintVisibilityChanged(S renderer, boolean isVisible);

	/**
	 * Signifies whether spectrum boundary visibility has changed
	 * @param renderer
	 * @param isVisible
	 */
	public void spectrumBoundaryVisibilityChanged(S renderer, boolean isVisible);
}
