package edu.jhuapl.sbmt.spectrum.model.key;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.CustomSpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class CustomSpectrumKey implements CustomSpectrumKeyInterface
{
	public final String name;

    public final FileType fileType;

    public final BasicSpectrumInstrument instrument;

    public ISpectraType spectrumType;

    public String spectrumFilename;

    public String pointingFilename;

    final static Key<String> nameKey = Key.of("name");
    final static Key<String> pointingFileTypeKey = Key.of("pointingFileType");
    final static Key<String> spectrumFileNameKey = Key.of("spectrumfilename");
    final static Key<String> spectraTypeKey = Key.of("spectratype");
    final static Key<String> pointingFilenameKey = Key.of("pointingFilename");
    final static Key<String> instrumentKey = Key.of("instrument");

    private static final Key<CustomSpectrumKey> CUSTOM_SPECTRUM_KEY = Key.of("customSpectrum");


	public CustomSpectrumKey(String name, FileType fileType, BasicSpectrumInstrument instrument, ISpectraType spectrumType, String spectrumFilename, String pointingFilename)
	{
		this.name = name;
		this.fileType = fileType;
		this.instrument = instrument;
		this.spectrumType = spectrumType;
		this.spectrumFilename = spectrumFilename;
		this.pointingFilename = pointingFilename;
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

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(CUSTOM_SPECTRUM_KEY,
			(metadata) -> {

				String name = metadata.get(nameKey);
		        String spectrumFilename = metadata.get(spectrumFileNameKey);
		        ISpectraType spectrumType = SpectraTypeFactory.findSpectraTypeForDisplayName(metadata.get(spectraTypeKey));
		        FileType fileType = FileType.valueOf(metadata.get(pointingFileTypeKey));
		        BasicSpectrumInstrument instrument = SpectrumInstrumentFactory.getInstrumentForName(name);
		        String pointingFilename = metadata.get(pointingFilenameKey);
		        CustomSpectrumKey result = new CustomSpectrumKey(name, fileType, instrument, spectrumType, spectrumFilename, pointingFilename);
				return result;

			},
			CustomSpectrumKey.class,
			(key) -> {
				SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
		        result.put(nameKey, key.getName());
		        result.put(spectrumFileNameKey, key.getSpectrumFilename());
		        result.put(instrumentKey, key.getInstrument().toString());
		        result.put(spectraTypeKey, key.getSpectrumType().toString());
		        result.put(pointingFileTypeKey, key.getFileType().toString());
		        result.put(pointingFilenameKey, key.getPointingFilename());
		        return result;
			}
		);
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
    public String toString()
    {
        return "CustomSpectrumKey [name=" + name
                + ", fileType=" + fileType + ", instrument=" + instrument
                + ", imageType=" + spectrumType + "]";
    }

}
