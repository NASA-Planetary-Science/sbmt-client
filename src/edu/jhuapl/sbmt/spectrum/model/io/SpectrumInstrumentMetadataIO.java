package edu.jhuapl.sbmt.spectrum.model.io;

import java.io.FileNotFoundException;
import java.util.List;

import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.InstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

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

    public static void main(String[] args) throws FileNotFoundException
    {
//        OREXSpectrumInstrumentMetadataIO test2 = new OREXSpectrumInstrumentMetadataIO("OTES");
//        List<OREXSpectrumInstrumentMetadata> metadata = test2.readMetadata(new File("/Users/steelrj1/Desktop/metadata.json"));
//        for (OREXSpectrumInstrumentMetadata data : metadata)
//        {
//            System.out.println("MetadataTest2: test: data is " + data);
//            List<OREXSearchSpec> searchSpecs = data.getSpecs();
//            for (OREXSearchSpec spec : searchSpecs)
//            {
//                System.out.println("MetadataTest2: test: " + spec);
//            }
//        }

//        ArrayList<OREXSearchSpec> specs = new ArrayList<OREXSearchSpec>();
//        ArrayList<OREXSpectrumInstrumentMetadata<OREXSearchSpec>> infos = new ArrayList<OREXSpectrumInstrumentMetadata<OREXSearchSpec>>();
//        OREXSpectrumInstrumentMetadata<OREXSearchSpec> otesInfo = new OREXSpectrumInstrumentMetadata<OREXSearchSpec>("OTES");
//        otesInfo.setQueryType("file");
//        specs.add(new OREXSearchSpec("OTES L2 Calibrated Radiance", "/earth/osirisrex/otes/spectra/l2", "spectra", "spectrumlist.txt", ImageSource.CORRECTED_SPICE,
//                                  "Wavenumber (1/cm)", "Radiance", "OTES L2 Calibrated Radiance"));
//        specs.add(new OREXSearchSpec("OTES L3 Spot Emissivity", "/earth/osirisrex/otes/spectra/l3", "spectra", "spectrumlist.txt", ImageSource.CORRECTED_SPICE,
//              "Wavenumber (1/cm)", "Emissivity", "OTES L3 Spot Emissivity"));
//        otesInfo.addSearchSpecs(specs);
//        infos.add(otesInfo);
//
//
//        OREXSpectrumInstrumentMetadata<OREXSearchSpec> ovirsInfo = new OREXSpectrumInstrumentMetadata<OREXSearchSpec>("OVIRS");
//        ovirsInfo.setQueryType("file");
//        specs = new ArrayList<OREXSearchSpec>();
//        specs.add(new OREXSearchSpec("OVIRS L3 I/F Spectra", "/earth/osirisrex/ovirs/spectra/l3/if", "spectra", "spectrumlist.txt", ImageSource.CORRECTED_SPICE,
//              "Wavenumber (1/cm)", "I/F", "OVIRS L3 I/F Spectra"));
//        specs.add(new OREXSearchSpec("OVIRS L3 REFF", "/earth/osirisrex/ovirs/spectra/l3/reff", "spectra", "spectrumlist.txt", ImageSource.CORRECTED_SPICE,
//                "Wavenumber (1/cm)", "REFF", "OVIRS L3 REFF"));
//        ovirsInfo.addSearchSpecs(specs);
//        infos.add(ovirsInfo);
//
//        String je = gson.toJson(infos);
//        System.out.println("MetadataTest2: test: je " + je);
//        Type collectionType = new TypeToken<List<OREXSpectrumInstrumentMetadata<OREXSearchSpec>>>(){}.getType();
//        List<OREXSpectrumInstrumentMetadata<OREXSearchSpec>> info2 = gson.fromJson(je, collectionType);
//        for (InstrumentMetadata<OREXSearchSpec> data : info2)
//        {
//            System.out.println("MetadataTest2: test: data is " + data);
//            List<OREXSearchSpec> searchSpecs = data.getSpecs();
//            for (SearchSpec spec : searchSpecs)
//            {
//                System.out.println("MetadataTest2: test: " + spec);
//            }
//        }
//        try
//        {
//            test2.writeJSON(new File("/Users/steelrj1/Desktop/metadata.json"), je);
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

}