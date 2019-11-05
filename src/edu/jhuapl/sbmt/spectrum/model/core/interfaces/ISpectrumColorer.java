package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Interface for classes that can color spectra
 * @author steelrj1
 *
 * @param <S>
 */
public interface ISpectrumColorer<S extends BasicSpectrum>
{
	/**
	 * Returns the color for a given spectrum as defined by this model
	 * @param spectrumRenderer
	 * @return
	 */
	public double[] getColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer);
}
