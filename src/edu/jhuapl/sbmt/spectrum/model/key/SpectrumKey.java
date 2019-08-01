package edu.jhuapl.sbmt.spectrum.model.key;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectraType;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class SpectrumKey implements SpectrumKeyInterface
{

	// The path of the image as passed into the constructor. This is not the
    // same as fullpath but instead corresponds to the name needed to download
    // the file from the server (excluding the hostname and extension).
    public String name;

    private String pointingFilename;

    public FileType fileType;

    public ISpectralInstrument instrument;

    public ISpectraType spectrumType;

    public String band;

    public int slice;


    public SpectrumKey(String name)
    {
        this(name, null, null, null, "");
    }

    public SpectrumKey(String name, ISpectralInstrument instrument)
    {
        this(name, null, null, instrument, "");
    }

    public SpectrumKey(String name, FileType fileType, ISpectraType spectrumType, ISpectralInstrument instrument, String pointingFilename)
    {
        this.name = name;
        this.fileType = fileType;
        this.spectrumType = spectrumType;
        this.instrument = instrument;
        this.setPointingFilename(pointingFilename);
    }

    @Override
    public boolean equals(Object obj)
    {
        return name.equals(((SpectrumKeyInterface)obj).getName());
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    /* (non-Javadoc)
	 * @see edu.jhuapl.sbmt.model.image.ImageKeyInterface#getName()
	 */
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


    public ISpectraType getSpectrumType()
	{
		return spectrumType;
	}

	public ISpectralInstrument getInstrument()
	{
		return instrument;
	}

	@Override
    public String toString()
    {
        return "SpectrumKey [name=" + name
                + ", fileType=" + fileType + ", instrument=" + instrument
                + ", imageType=" + spectrumType + ", band=" + band + ", slice="
                + slice + "]";
    }

    private static final Key<String> nameKey = Key.of("name");
    private static final Key<String> fileTypeKey = Key.of("fileTypeKey");
    private static final Key<String> spectrumTypeKey = Key.of("spectrumType");
    private static final Key<Metadata> instrumentKey = Key.of("spectrumInstrument");
    private static final Key<String> pointingFilenameKey = Key.of("pointingfilename");

    private static final Key<SpectrumKey> SPECTRUM_KEY = Key.of("spectrum");

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(SPECTRUM_KEY,
			(metadata) -> {

		        String name = metadata.get(nameKey);
		        ISpectraType spectrumType = SpectraTypeFactory.findSpectraTypeForDisplayName(metadata.get(spectrumTypeKey));
		        ISpectralInstrument instrument = SpectrumInstrumentFactory.getInstrumentForName(name);

		        FileType fileType = FileType.valueOf(metadata.get(fileTypeKey));
		        String pointingFilename = metadata.get(pointingFilenameKey);

		        SpectrumKey result = new SpectrumKey(name, fileType, spectrumType, instrument, pointingFilename);

				return result;
			},
			SpectrumKey.class,
			(key) -> {
				 SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
			        result.put(Key.of("customimagetype"), SPECTRUM_KEY.toString());
			        result.put(nameKey, key.getName());
			        result.put(fileTypeKey, key.getFileType().toString());
			        result.put(spectrumTypeKey, key.getSpectrumType().toString());

			        result.put(instrumentKey, InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(BasicSpectrumInstrument.class).provide((BasicSpectrumInstrument)key.getInstrument()));
			        result.put(pointingFilenameKey, key.getPointingFilename());
			        return result;
			});

	}


	public String getPointingFilename()
	{
		return pointingFilename;
	}

	public void setPointingFilename(String pointingFilename)
	{
		this.pointingFilename = pointingFilename;
	}

}
