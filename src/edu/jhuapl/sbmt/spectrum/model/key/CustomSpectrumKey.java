package edu.jhuapl.sbmt.spectrum.model.key;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Key defining custom spectra - used to help load/save the custom spectra data
 * to file
 *
 * @author steelrj1
 *
 */
public class CustomSpectrumKey implements CustomSpectrumKeyInterface
{
	public final String name;

	public final FileType fileType;

	public final BasicSpectrumInstrument instrument;

	public ISpectraType spectrumType;

	public String spectrumFilename;

	public String pointingFilename;

	public SpectrumSearchSpec searchSpec;

	/**
	 * @param name
	 * @param fileType
	 * @param instrument
	 * @param spectrumType
	 * @param spectrumFilename
	 * @param pointingFilename
	 */
	public CustomSpectrumKey(String name, FileType fileType, BasicSpectrumInstrument instrument,
			ISpectraType spectrumType, String spectrumFilename, String pointingFilename, SpectrumSearchSpec searchSpec)
	{
		this.name = name;
		this.fileType = fileType;
		this.instrument = instrument;
		this.spectrumType = spectrumType;
		this.spectrumFilename = spectrumFilename;
		this.pointingFilename = pointingFilename;
		this.searchSpec = searchSpec;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public FileType getFileType()
	{
		return fileType;
	}

	@Override
	public BasicSpectrumInstrument getInstrument()
	{
		return instrument;
	}

	@Override
	public ISpectraType getSpectrumType()
	{
		return spectrumType;
	}

	@Override
	public void setSpectrumFilename(String spectrumFilename)
	{
		this.spectrumFilename = spectrumFilename;
	}

	@Override
	public String getSpectrumFilename()
	{
		return spectrumFilename;
	}

	@Override
	public void setSpectraType(ISpectraType type)
	{
		this.spectrumType = type;
	}

	@Override
	public String getPointingFilename()
	{
		return pointingFilename;
	}

	public void setPointingFilename(String pointingFilename)
	{
		this.pointingFilename = pointingFilename;
	}

	@Override
	public SpectrumSearchSpec getSpectraSpec()
	{
		return searchSpec;
	}

	@Override
	public String toString()
	{
		return "CustomSpectrumKey [name=" + name + ", fileType=" + fileType + ", instrument=" + instrument
				+ ", imageType=" + spectrumType + "]";
	}

	final static Key<String> nameKey = Key.of("name");
	final static Key<String> pointingFileTypeKey = Key.of("pointingFileType");
	final static Key<String> spectrumFileNameKey = Key.of("spectrumfilename");
	final static Key<String> spectraTypeKey = Key.of("spectratype");
	final static Key<String> pointingFilenameKey = Key.of("pointingFilename");
	final static Key<String> instrumentKey = Key.of("instrument");
	final static Key<SpectrumSearchSpec> searchSpecKey = Key.of("searchSpec");

	private static final Key<CustomSpectrumKey> CUSTOM_SPECTRUM_KEY = Key.of("customSpectrum");

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(CUSTOM_SPECTRUM_KEY, (metadata) ->
		{

			String name = metadata.get(nameKey);
			String spectrumFilename = metadata.get(spectrumFileNameKey);
			String displayName = metadata.get(spectraTypeKey);
			if (displayName.contains("_"))
				displayName = displayName.split("_")[0];
			ISpectraType spectrumType = SpectraTypeFactory.findSpectraTypeForDisplayName(displayName);
			FileType fileType = FileType.valueOf(metadata.get(pointingFileTypeKey));
			BasicSpectrumInstrument instrument = SpectrumInstrumentFactory
					.getInstrumentForName(spectrumType.getDisplayName());
			String pointingFilename = metadata.get(pointingFilenameKey);
			SpectrumSearchSpec searchSpec = null;
			if (metadata.hasKey(searchSpecKey))
				searchSpec = metadata.get(searchSpecKey);
			CustomSpectrumKey result = new CustomSpectrumKey(name, fileType, instrument, spectrumType, spectrumFilename,
					pointingFilename, searchSpec);
			return result;

		}, CustomSpectrumKey.class, (key) ->
		{
			SettableMetadata result = SettableMetadata.of(Version.of(1, 1));
			if (key.getSpectraSpec() != null)
			{
				result = SettableMetadata.of(Version.of(1, 1));
				result.put(searchSpecKey, key.getSpectraSpec());
			}
			result.put(nameKey, key.getName());
			result.put(spectrumFileNameKey, key.getSpectrumFilename());
			result.put(instrumentKey, key.getInstrument().toString());
			result.put(spectraTypeKey, key.getSpectrumType().toString());
			result.put(pointingFileTypeKey, key.getFileType().toString());
			result.put(pointingFilenameKey, key.getPointingFilename());

			return result;
		});
	}

}
