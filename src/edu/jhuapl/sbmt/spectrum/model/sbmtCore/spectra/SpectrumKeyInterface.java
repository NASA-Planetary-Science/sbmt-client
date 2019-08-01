package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import edu.jhuapl.saavtk.model.FileType;

public interface SpectrumKeyInterface
{

	String getName();

	FileType getFileType();

	ISpectralInstrument getInstrument();

	ISpectraType getSpectrumType();

}