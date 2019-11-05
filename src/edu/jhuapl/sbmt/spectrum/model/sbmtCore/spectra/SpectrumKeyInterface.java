package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;

/**
 * @author steelrj1
 *
 */
public interface SpectrumKeyInterface
{
	/**
	 * Returns the spectrum name
	 * @return
	 */
	String getName();

	/**
	 * Returns the file type (INFO, SUM, etc)
	 * @return
	 */
	FileType getFileType();

	/**
	 * Returns the spectrum instrument
	 * @return
	 */
	BasicSpectrumInstrument getInstrument();

	/**
	 * Returns the spectra type
	 * @return
	 */
	ISpectraType getSpectrumType();

}