package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.io.IOException;

import edu.jhuapl.sbmt.spectrum.model.rendering.IBasicSpectrumRenderer;

@FunctionalInterface
public interface SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>
{
	IBasicSpectrumRenderer buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException;
}
