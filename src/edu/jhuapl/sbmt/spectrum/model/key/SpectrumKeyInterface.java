package edu.jhuapl.sbmt.spectrum.model.key;

import crucible.crust.metadata.api.Metadata;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectraType;
import edu.jhuapl.sbmt.spectrum.model.core.ISpectralInstrument;

public interface SpectrumKeyInterface
{

	String getName();

	FileType getFileType();

	ISpectralInstrument getInstrument();

	ISpectraType getSpectrumType();
	
	public Metadata store();

}