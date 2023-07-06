package edu.jhuapl.sbmt.core.config;

import java.util.HashMap;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;

public class FeatureConfigIOFactory
{
	private static HashMap<String, BaseFeatureConfigIO> registeredItems = new HashMap<String, BaseFeatureConfigIO>();

	public static void registerFeatureConfigIO(String classTypeName, BaseFeatureConfigIO configIO)
	{
		registeredItems.put(classTypeName, configIO);
	}

	public static Metadata getMetadataForFeatureConfig(String classTypeName, IFeatureConfig config)
	{
		BaseFeatureConfigIO configIO = registeredItems.get(classTypeName);
		configIO.setFeatureConfig(config);
		return configIO.store();
	}

	public static IFeatureConfig getFeatureConfigForMetadata(String classTypeName, Metadata source)
	{
		BaseFeatureConfigIO configIO = registeredItems.get(classTypeName);
		configIO.retrieve(source);
		return configIO.featureConfig;
	}

	public static Key<Metadata> getKeyForFeature(String configTypeName)
	{
		Key<Metadata> featureConfigKey = Key.of(configTypeName + "Config");
		return featureConfigKey;
	}
}
