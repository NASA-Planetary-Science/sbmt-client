package edu.jhuapl.sbmt.spectrum.model.core;

import java.io.IOException;

@FunctionalInterface
public interface SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>
{
	Spectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException;
}
