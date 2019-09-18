package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.io.IOException;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

//@FunctionalInterface
public interface SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>
{
	BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException;

	BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument, String timeString) throws IOException;

	IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException;

	IBasicSpectrumRenderer buildSpectrumRenderer(BasicSpectrum spectrum, ISmallBodyModel smallBodyModel) throws IOException;
}
