package edu.jhuapl.sbmt.spectrum.model.io;

import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Class to handle spectrum instrument metadata IO, allowing you to read the associated metadata
 * @author steelrj1
 *
 */
public class SpectrumInstrumentMetadataIO extends SpectraHierarchicalSearchSpecification<SpectrumSearchSpec>
{
    List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> info = null;

    public SpectrumInstrumentMetadataIO(String scName)
    {
        super(scName);
    }

    public SpectrumInstrumentMetadataIO(String scName, List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> info)
	{
		super(scName);
		this.info = info;
	}

    @Override
    public SpectraHierarchicalSearchSpecification<SpectrumSearchSpec> clone()
    {
		SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO(rootName, info);
		return specIO;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadataIO#getInstrumentMetadata(java.lang.String)
     */
    @Override
    public InstrumentMetadata<SpectrumSearchSpec> getInstrumentMetadata(String instrumentName)
    {
        for (SpectrumInstrumentMetadata<SpectrumSearchSpec> instInfo : info)
        {
            if (instInfo.getInstrumentName().equals(instrumentName))
            {
                return instInfo;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.jhuapl.sbmt.model.bennu.InstrumentMetadataIO#readHierarchyForInstrument(java.lang.String)
     */
    @Override
    public void readHierarchyForInstrument(String instrumentName)
    {
        InstrumentMetadata<SpectrumSearchSpec> instrumentMetadata = getInstrumentMetadata(instrumentName);
        for (SpectrumSearchSpec spec : instrumentMetadata.getSpecs())
        {
            addHierarchicalSearchPath(new String[] {spec.getDataName()}, instrumentMetadata.getSpecs().indexOf(spec),-1);
        }
    }

    private static final Key<SpectrumInstrumentMetadataIO> SPECTRUMINSTRUMENTMETADATAIO_KEY = Key.of("spectrumInstrumentMetadataIO");
	private static final Key<String> SCNAME_KEY = Key.of("scName");
	private static final Key<List<SpectrumInstrumentMetadata<SpectrumSearchSpec>>> INSTRUMENTMETADATA_KEY = Key.of("instrumentMetadata");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(SPECTRUMINSTRUMENTMETADATAIO_KEY, (source) -> {
    		String instrumentName = source.get(SCNAME_KEY);
    		List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> info = source.get(INSTRUMENTMETADATA_KEY);
    		SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO(instrumentName, info);
    		return specIO;

    	}, SpectrumInstrumentMetadataIO.class, spec -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(SCNAME_KEY, spec.rootName);
    		SpectrumInstrumentMetadata<SpectrumSearchSpec>[] infos = new SpectrumInstrumentMetadata[spec.info.size()];
    		spec.info.toArray(infos);
    		result.put(INSTRUMENTMETADATA_KEY, spec.info);
    		return result;
    	});

	}
}