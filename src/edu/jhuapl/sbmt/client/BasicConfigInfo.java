package edu.jhuapl.sbmt.client;

import java.util.Arrays;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class BasicConfigInfo implements MetadataManager
{
	ShapeModelPopulation population;
	String shapeModelName;
	String uniqueName;
	ShapeModelType author;
	BodyType type;
	ShapeModelBody body;
	ShapeModelDataUsed dataUsed;
	String configURL;
	String version;
	String modelLabel;
	SbmtMultiMissionTool.Mission[] presentInVersion = null;
	SbmtMultiMissionTool.Mission[] defaultFor = null;
	boolean enabled;

	public BasicConfigInfo() {}

	public BasicConfigInfo(BodyViewConfig config)
	{
		this.type = config.type;
		this.population = config.population;
		this.body = config.body;
		this.dataUsed = config.dataUsed;
		this.author = config.author;
		this.shapeModelName = config.getShapeModelName();
		this.uniqueName = config.getUniqueName();
		this.version = config.version;
		this.modelLabel = config.modelLabel;
		this.presentInVersion = config.presentInMissions;
		this.defaultFor = config.defaultForMissions;

		if (author != ShapeModelType.CUSTOM)
		{
			System.out.println("BasicConfigInfo: BasicConfigInfo: unique name " + uniqueName);
			for (SbmtMultiMissionTool.Mission presentMission : presentInVersion)
			{
				if (presentMission == SbmtMultiMissionTool.getMission())
				{
					enabled = true;
					break;
				}
				else
					enabled = false;
			}
			for (SbmtMultiMissionTool.Mission defaultMission : defaultFor)
			{
				if (defaultMission == SbmtMultiMissionTool.getMission())
				{
					System.out.println("BasicConfigInfo: BasicConfigInfo: setting unique name");
					ViewConfig.setFirstTimeDefaultModelName(uniqueName);
					break;
				}
			}


			if (config.version != null)
				this.configURL = ((SmallBodyViewConfig)config).rootDirOnServer + "/" + config.author +  "_" + config.body.toString().replaceAll(" ", "_") + config.version.replaceAll(" ", "_") + ".json";
			else
				this.configURL = ((SmallBodyViewConfig)config).rootDirOnServer + "/" + config.author +  "_" + config.body.toString().replaceAll(" ", "_") + ".json";
		}
	}

	Key<String> populationKey = Key.of("population");
	Key<String> typeKey = Key.of("type");
	Key<String> bodyKey = Key.of("body");
	Key<String> dataUsedKey = Key.of("dataUsed");
	Key<String> authorKey = Key.of("author");
	Key<String> shapeModelNameKey = Key.of("shapeModelName");
	Key<String> uniqueNameKey = Key.of("uniqueName");
	Key<String> configURLKey = Key.of("configURL");
	Key<String> versionKey = Key.of("version");
	Key<String> modelLabelKey = Key.of("modelLabel");
	Key<String[]> presentInVersionKey = Key.of("presentInVersion");
	Key<String[]> defaultForKey = Key.of("defaultFor");

	@Override
	public Metadata store()
	{
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        configMetadata.put(populationKey, population.toString());
        configMetadata.put(typeKey, type.toString());
        configMetadata.put(bodyKey, body.toString());
        configMetadata.put(dataUsedKey, dataUsed.toString());
        configMetadata.put(authorKey, author.toString());
        configMetadata.put(shapeModelNameKey, shapeModelName);
        configMetadata.put(uniqueNameKey, uniqueName);
        configMetadata.put(configURLKey, configURL);
        configMetadata.put(versionKey, version);
        configMetadata.put(modelLabelKey, modelLabel);

        if (author != ShapeModelType.CUSTOM)
		{
	        String[] presentStrings = new String[presentInVersion.length];
	        int i=0;
	        for (SbmtMultiMissionTool.Mission mission : presentInVersion) { presentStrings[i++] = mission.getHashedName(); }
	        configMetadata.put(presentInVersionKey, presentStrings);

	        if (defaultFor != null)
	        {
		        String[] defaultStrings = new String[defaultFor.length];
		        i=0;
		        for (SbmtMultiMissionTool.Mission mission : defaultFor) { defaultStrings[i++] = mission.getHashedName(); }
		        configMetadata.put(defaultForKey, defaultStrings);

	        }
		}
        return configMetadata;
	}

	@Override
	public void retrieve(Metadata source)
	{
		type = BodyType.valueFor(source.get(typeKey));
		population = ShapeModelPopulation.valueFor(source.get(populationKey));
		body = ShapeModelBody.valueFor(source.get(bodyKey));
		dataUsed = ShapeModelDataUsed.valueFor(source.get(dataUsedKey));
		String fetchedAuthor = source.get(authorKey);
		if (ShapeModelType.contains(fetchedAuthor) == false) ShapeModelType.create(fetchedAuthor);
		author = ShapeModelType.valueOf(source.get(authorKey));
		shapeModelName = source.get(shapeModelNameKey);
		uniqueName = source.get(uniqueNameKey);
		configURL = source.get(configURLKey);
		version = source.get(versionKey);
		modelLabel = source.get(modelLabelKey);
		if (source.hasKey(presentInVersionKey))
		{
			String[] presentStrings = source.get(presentInVersionKey);
			int i=0;
			presentInVersion = new SbmtMultiMissionTool.Mission[presentStrings.length];
			for (String present : presentStrings)
			{
				presentInVersion[i++] = SbmtMultiMissionTool.Mission.getMissionForName(present);
			}
		}
		if (source.hasKey(defaultForKey))
		{
			String[] defaultStrings = source.get(defaultForKey);
			int i=0;
			defaultFor = new SbmtMultiMissionTool.Mission[defaultStrings.length];
			for (String defaultStr : defaultStrings)
			{
				defaultFor[i++] = SbmtMultiMissionTool.Mission.getMissionForName(defaultStr);
			}

		}

		if (author != ShapeModelType.CUSTOM)
		{
			for (SbmtMultiMissionTool.Mission presentMission : presentInVersion)
			{
				if (presentMission == SbmtMultiMissionTool.getMission())
				{
					enabled = true;
					break;
				}
				else
					enabled = false;
			}
			for (SbmtMultiMissionTool.Mission defaultMission : defaultFor)
			{
				if (defaultMission == SbmtMultiMissionTool.getMission())
				{
					ViewConfig.setFirstTimeDefaultModelName(uniqueName);
					break;
				}
			}
		}
	}

	public SbmtMultiMissionTool.Mission[] getPresentInVersion()
	{
		return presentInVersion;
	}

	public SbmtMultiMissionTool.Mission[] getDefaultFor()
	{
		return defaultFor;
	}

	public String getUniqueName()
	{
		return uniqueName;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public String toString()
	{
		return "BasicConfigInfo [population=" + population + ", shapeModelName=" + shapeModelName + ", uniqueName="
				+ uniqueName + ", author=" + author + ", type=" + type + ", body=" + body + ", dataUsed=" + dataUsed
				+ ", configURL=" + configURL + ", version=" + version + ", modelLabel=" + modelLabel
				+ ", presentInVersion=" + Arrays.toString(presentInVersion) + ", defaultFor="
				+ Arrays.toString(defaultFor) + ", enabled=" + enabled + "]";
	}
}