package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Interface for listeners that can broadcast notifications that a spectrum has changed
 * @author steelrj1
 *
 * @param <S>
 */
public interface SpectrumCollectionChangedListener<S extends BasicSpectrum>
{
	/**
	 * Notifies listeners that a spectrum has been rendered on screen
	 * @param renderer
	 */
	public void spectumRendered(IBasicSpectrumRenderer<S> renderer);
}
