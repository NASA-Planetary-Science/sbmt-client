package edu.jhuapl.sbmt.spectrum.model.core.interfaces;

import java.io.IOException;

import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;

/**
 * Interface for objects that can build spectra and their renders
 * @author steelrj1
 *
 * @param <String>
 * @param <ISmallBodyModel>
 * @param <ISpectralInstrument>
 */
public interface SpectrumBuilder<String, ISmallBodyModel, ISpectralInstrument>
{
	/**
	 * Builds a spectrum without a timestamp; it is assumed that a proper DateTime is set on the spectrum outside this method
	 * @param path
	 * @param smallBodyModel
	 * @param instrument
	 * @return
	 * @throws IOException
	 */
	BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument) throws IOException;

	/**
	 * Builds a spectrum, and also sets the DateTime object based on the passed in timeString
	 * @param path
	 * @param smallBodyModel
	 * @param instrument
	 * @param timeString
	 * @return
	 * @throws IOException
	 */
	BasicSpectrum buildSpectrum(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument, String timeString) throws IOException;

	/**
	 * Builds a spectrum renderer based on the given <pre>path</pre>, <pre>smallBodyModel</pre> and <pre>instrument</pre>
	 * @param path
	 * @param smallBodyModel
	 * @param instrument
	 * @return
	 * @throws IOException
	 */
	IBasicSpectrumRenderer buildSpectrumRenderer(String path, ISmallBodyModel smallBodyModel, ISpectralInstrument instrument, boolean headless) throws IOException;

	/**
	 * Builds a spectrum renderer based on the passed in <pre>spectrum</pre> and <pre>smallBodyModel</pre>
	 * @param spectrum
	 * @param smallBodyModel
	 * @return
	 * @throws IOException
	 */
	IBasicSpectrumRenderer buildSpectrumRenderer(BasicSpectrum spectrum, ISmallBodyModel smallBodyModel, boolean headless) throws IOException;
}
