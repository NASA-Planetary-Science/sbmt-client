package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public interface ISpectrumColorer<S extends BasicSpectrum>
{
	public double[] getColorForSpectrum(IBasicSpectrumRenderer<S> spectrumRenderer);
}
