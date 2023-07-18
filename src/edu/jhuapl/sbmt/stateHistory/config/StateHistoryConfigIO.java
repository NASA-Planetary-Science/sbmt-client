package edu.jhuapl.sbmt.stateHistory.config;

import java.util.Date;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class StateHistoryConfigIO extends BaseFeatureConfigIO //  BaseInstrumentConfigIO implements MetadataManager
{
	final Key<SpiceInfo> spiceInfoKey = Key.of("spiceInfo");
	final Key<Boolean> hasStateHistoryKey = Key.of("hasStateHistory");
	final Key<Date> stateHistoryStartKey = Key.of("stateHistoryStart");
	final Key<Date> stateHistoryEndKey = Key.of("stateHistoryEnd");
	final Key<String> timeHistoryFileKey = Key.of("timeHistoryFile");

//	private StateHistoryConfig c = new StateHistoryConfig();
	private String metadataVersion = "1.0";
	private ViewConfig viewConfig;

	public StateHistoryConfigIO()
	{
		
	}
	
	public StateHistoryConfigIO(String metadataVersion, ViewConfig viewConfig)
	{
		this.metadataVersion = metadataVersion;
		this.viewConfig = viewConfig;
	}

	@Override
	public void retrieve(Metadata configMetadata)
	{
		featureConfig = new StateHistoryConfig((BodyViewConfig)viewConfig);
		StateHistoryConfig c = (StateHistoryConfig)featureConfig;
		c.hasStateHistory = read(hasStateHistoryKey, configMetadata);
		c.spiceInfo = read(spiceInfoKey, configMetadata);
		c.stateHistoryStartDate = read(stateHistoryStartKey, configMetadata);
		c.stateHistoryEndDate = read(stateHistoryEndKey, configMetadata);
		c.timeHistoryFile = read(timeHistoryFileKey, configMetadata);
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
		StateHistoryConfig c = (StateHistoryConfig)featureConfig;
//		SettableMetadata configMetadata = SettableMetadata.of(Version.of(metadataVersion));

		write(hasStateHistoryKey, c.hasStateHistory, configMetadata);
		write(spiceInfoKey, c.spiceInfo, configMetadata);
		write(stateHistoryStartKey, c.stateHistoryStartDate, configMetadata);
		write(stateHistoryEndKey, c.stateHistoryEndDate, configMetadata);
		write(timeHistoryFileKey, c.timeHistoryFile, configMetadata);

		return configMetadata;
	}

}
