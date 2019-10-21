package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public interface SpectrumCollectionChangedListener<S extends BasicSpectrum>
{
	public void spectraRendered(IBasicSpectrumRenderer<S> renderer);
}
