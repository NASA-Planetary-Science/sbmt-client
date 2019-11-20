package edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra;

import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.ProvidesGenericObjectFromMetadata;
import crucible.crust.metadata.impl.InstanceGetter;

/**
 * Interface for Custom Spectrum Keys
 * @author steelrj1
 *
 */
public interface CustomSpectrumKeyInterface extends SpectrumKeyInterface
{
	/**
	 * Sets the spectrum filename
	 * @param spectrumfilename
	 */
	public void setSpectrumFilename(String spectrumfilename);

	/**
	 * Gets the spectrum filename in the custom data folder
	 * @return
	 */
	public String getSpectrumFilename();

	/**
	 * Gets the pointing filename in the custom data folder
	 * @return
	 */
	public String getPointingFilename();

	/**
	 * Sets the pointing filename in the custom data folder
	 * @param pointingFilename
	 */
	public void setPointingFilename(String pointingFilename);

	/**
	 * Sets the spectra type for this key
	 * @param type
	 */
	public void setSpectraType(ISpectraType type);

	/**
	 * Returns the low level metadata for this spectra
	 */
	public SpectrumSearchSpec getSpectraSpec();

	public void setSpectraSearchSpec(SpectrumSearchSpec spec);

	/**
	 * Method to retreive the custom spectrum key from a saved metadata file
	 * @param objectMetadata
	 * @return
	 */
	static CustomSpectrumKeyInterface retrieve(Metadata objectMetadata)
	{
		final Key<String> key = Key.of("customspectrumtype");
		Key<CustomSpectrumKeyInterface> CUSTOM_SPECTRUM_KEY = Key.of("customSpectrum");
		ProvidesGenericObjectFromMetadata<CustomSpectrumKeyInterface> metadata = InstanceGetter.defaultInstanceGetter().providesGenericObjectFromMetadata(CUSTOM_SPECTRUM_KEY);
		return metadata.provide(objectMetadata);

	}
}
