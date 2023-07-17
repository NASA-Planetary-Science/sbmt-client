package edu.jhuapl.sbmt.spectrum.config;

import java.util.List;
import java.util.Map;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class SpectrumInstrumentConfigIO extends BaseFeatureConfigIO
{
	final Key<Boolean> hasSpectralData = Key.of("hasSpectralData");
	final Key<List<BasicSpectrumInstrument>> spectralInstruments = Key.of("spectralInstruments");
//	final Key<Metadata> spectralInstruments = Key.of("imagingInstruments");
	final Key<Boolean> hasHierarchicalSpectraSearch = Key.of("hasHierarchicalSpectraSearch");
    final Key<Boolean> hasHypertreeBasedSpectraSearch = Key.of("hasHypertreeSpectraSearch");
    final Key<Map> spectraSearchDataSourceMap = Key.of("spectraSearchDataSourceMap");
    final Key<String> spectrumMetadataFile = Key.of("spectrumMetadataFile");
    final Key<SpectrumInstrumentMetadataIO> hierarchicalSpectraSearchSpecification = Key.of("hierarchicalSpectraSearchSpecification");

    private String metadataVersion = "1.0";
//	private ViewConfig viewConfig;
//	private SpectrumInstrumentConfig c = new SpectrumInstrumentConfig();
	
	public SpectrumInstrumentConfigIO()
	{
		
	}
	
	public SpectrumInstrumentConfigIO(String metadataVersion, ViewConfig viewConfig)
	{
		this.metadataVersion = metadataVersion;
		this.viewConfig = viewConfig;
	}
    
	@Override
	public void retrieve(Metadata configMetadata)
	{
		featureConfig = new SpectrumInstrumentConfig((BodyViewConfig)viewConfig);
		SpectrumInstrumentConfig c = (SpectrumInstrumentConfig)featureConfig;
        c.hasSpectralData = read(hasSpectralData, configMetadata);

        if (configMetadata.get(hasSpectralData) == true)
        {
        	c.spectralInstruments = configMetadata.get(spectralInstruments);
        	
//    		Metadata spectralMetadata = read(spectralInstruments, configMetadata);
//    		if (spectralMetadata == null) return;
//    		c.spectralInstruments = null; 
//
//    		String instrumentName = (String)configMetadata.get(Key.of("displayName"));
//    		BasicSpectrumInstrument inst = SpectrumInstrumentFactory.getInstrumentForName(instrumentName);
////    		inst.retrieve(spectralMetadata);
//    		c.spectralInstruments = Lists.newArrayList(inst);
        	
//        	try
//        	{
//        		c.spectralInstruments = configMetadata.get(spectralInstruments);
//        	}
//        	catch (ClassCastException cce)	//fall back to the old method
//        	{
//        		final Key<Metadata[]> spectralInstrumentsOldFormat = Key.of("spectralInstruments");
//        		Metadata[] spectralMetadata = readMetadataArray(spectralInstrumentsOldFormat, configMetadata);
//                int i=0;
//                for (Metadata data : spectralMetadata)
//                {
//                    String instrumentName = (String)data.get(Key.of("displayName"));
//                    BasicSpectrumInstrument inst = SpectrumInstrumentFactory.getInstrumentForName(instrumentName);
//                    inst.retrieveOldFormat(data);
//                    c.spectralInstruments.add(inst);
//                }
//        	}
        }

        if (c.hasSpectralData && c.spectralInstruments.size() > 0)
        {
        	if (configMetadata.hasKey(hasHierarchicalSpectraSearch))
        		c.hasHierarchicalSpectraSearch = read(hasHierarchicalSpectraSearch, configMetadata);
        	if (configMetadata.hasKey(hasHypertreeBasedSpectraSearch))
        		c.hasHypertreeBasedSpectraSearch = read(hasHypertreeBasedSpectraSearch, configMetadata);
	        c.spectraSearchDataSourceMap = read(spectraSearchDataSourceMap, configMetadata);
	        c.spectrumMetadataFile = read(spectrumMetadataFile, configMetadata);

	        if (configMetadata.hasKey(hierarchicalSpectraSearchSpecification))
	        {
	        	try
	        	{
	        		c.hierarchicalSpectraSearchSpecification = configMetadata.get(hierarchicalSpectraSearchSpecification);
	        	}
	        	catch (ClassCastException cce)	//fall back to the old method
	        	{
	        	    Key<Metadata> hierarchicalSpectraSearchSpecificationOldFormat = Key.of("hierarchicalSpectraSearchSpecification");

	        		c.hierarchicalSpectraSearchSpecification = new SpectrumInstrumentMetadataIO("");
	        		c.hierarchicalSpectraSearchSpecification.retrieveOldFormat(configMetadata.get(hierarchicalSpectraSearchSpecificationOldFormat));
	        		c.hierarchicalSpectraSearchSpecification.getSelectedDatasets();
	        	}
	        }
        }
	}

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(metadataVersion));
		storeConfig(result);
//		SettableMetadata configMetadata = storeConfig(viewConfig);
//		Key<SettableMetadata> metadata = Key.of(viewConfig.getUniqueName());
//		result.put(metadata, configMetadata);
		return result;
	}

	private SettableMetadata storeConfig(SettableMetadata configMetadata)
	{
//		featureConfig = new SpectrumInstrumentConfig((BodyViewConfig)viewConfig);
//		SpectrumInstrumentConfig c = new SpectrumInstrumentConfig();
		System.out.println("SpectrumInstrumentConfigIO: storeConfig: unique name " + viewConfig.getUniqueName());
		System.out.println("SpectrumInstrumentConfigIO: storeConfig: feature " + ((SpectrumInstrumentConfig)featureConfig).spectralInstruments.size());
		SpectrumInstrumentConfig c = (SpectrumInstrumentConfig)featureConfig;
		if (c.spectralInstruments == null) return configMetadata;
//		SettableMetadata configMetadata = SettableMetadata.of(Version.of(metadataVersion));
//		Metadata[] spectrumInstrumentMetadata = new Metadata[c.spectralInstruments.size()];
		int i = 0;
		for (BasicSpectrumInstrument inst : c.spectralInstruments)
		{
			System.out.println("SpectrumInstrumentConfigIO: storeConfig: name " + inst.getDisplayName());
			// spectrumInstrumentMetadata[i++] =
			// InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(BasicSpectrumInstrument.class).provide(inst);
			//spectrumInstrumentMetadata[i++] = inst.store();
		}
//		Key<Metadata[]> spectralInstrumentsMetadataKey = Key.of("spectralInstruments");
//		configMetadata.put(spectralInstrumentsMetadataKey, spectrumInstrumentMetadata);
		// writeMetadataArray(spectralInstrumentsMetadataKey,
		// spectrumInstrumentMetadata, configMetadata);
		// writeMetadataArray(spectralInstruments, spectrumInstrumentMetadata,
		// configMetadata);
		write(spectralInstruments, c.spectralInstruments, configMetadata);
		write(hasSpectralData, c.hasSpectralData, configMetadata);
//		
        if (c.hasSpectralData && c.spectralInstruments.size() > 0)
        {
        	write(hasHierarchicalSpectraSearch, c.hasHierarchicalSpectraSearch, configMetadata);
        	write(hasHypertreeBasedSpectraSearch, c.hasHypertreeBasedSpectraSearch, configMetadata);
        	write(spectraSearchDataSourceMap, c.spectraSearchDataSourceMap, configMetadata);
        	write(spectrumMetadataFile, c.spectrumMetadataFile, configMetadata);
        }

//        if (c.hasHierarchicalSpectraSearch && c.hierarchicalSpectraSearchSpecification != null)
      	if (c.hierarchicalSpectraSearchSpecification != null)
        {
//        	try
//			{
//				c.hierarchicalSpectraSearchSpecification.loadMetadata();
//			}
//        	catch (FileNotFoundException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            Metadata spectralMetadata = InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(SpectrumInstrumentMetadataIO.class).provide(c.hierarchicalSpectraSearchSpecification);
            configMetadata.put(hierarchicalSpectraSearchSpecification, c.hierarchicalSpectraSearchSpecification);
//            write(hierarchicalSpectraSearchSpecification, spectralMetadata, configMetadata);
        }
      	return configMetadata;
	}

}
