package edu.jhuapl.sbmt.client;

import java.util.Arrays;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class BasicConfigInfo implements MetadataManager
{

    private static final String configInfoVersion = "9.0";

    // This variable gives the prefix used to locate configuration metadata
    // relative to the top of the model.
    private static final String ConfigPathPrefix = "allBodies-" + configInfoVersion;

    public static String getConfigInfoVersion()
    {
        return configInfoVersion;
    }

    public static String getConfigPathPrefix()
    {
        return ConfigPathPrefix;
    }

    ShapeModelPopulation population;
	String shapeModelName;
	String uniqueName;
	ShapeModelType author;
	BodyType type;
	ShapeModelBody body;
	ShapeModelDataUsed dataUsed;
	private String configURL;
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
			System.out.println("BasicConfigInfo: unique name " + uniqueName);
			for (SbmtMultiMissionTool.Mission presentMission : presentInVersion)
			{
				//allow the body if the "present in Mission" value equals the tool's preset mission value OR if the tool's present mission value is the apl internal nightly
				if ((presentMission == SbmtMultiMissionTool.getMission()) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.APL_INTERNAL_NIGHTLY))
				{
					enabled = true;
					break;
				}
				else
					enabled = false;
			}
            if (SmallBodyViewConfig.getDefaultModelName() == null)
            {
                for (SbmtMultiMissionTool.Mission defaultMission : defaultFor)
                {
                    if (defaultMission == SbmtMultiMissionTool.getMission())
                    {
                        SmallBodyViewConfig.setDefaultModelName(uniqueName);
                        break;
                    }
                }
            }

            // Note: config.version is for when a model intrinsically has a
            // version as part of its name. It has nothing to do with the
            // metadata version. For most models config.version is null, so
            // modelVersion will add nothing.
            String modelVersion = config.version != null ? config.version.replaceAll(" ", "_") : "";

            this.configURL = "/" + ConfigPathPrefix + ((SmallBodyViewConfig) config).rootDirOnServer + //
                    "/" + config.author + "_" + //
                    config.body.toString().replaceAll(" ", "_") + modelVersion + //
                    "_v" + getConfigInfoVersion() + ".json";
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
		author = ShapeModelType.provide(source.get(authorKey)); // creates if necessary.
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
				if ((presentMission == SbmtMultiMissionTool.getMission()) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.APL_INTERNAL_NIGHTLY))
				{
					enabled = true;
					break;
				}
				else
					enabled = false;
			}
            if (SmallBodyViewConfig.getDefaultModelName() == null)
            {
                for (SbmtMultiMissionTool.Mission defaultMission : defaultFor)
                {
                    if (defaultMission == SbmtMultiMissionTool.getMission())
                    {
                        SmallBodyViewConfig.setDefaultModelName(uniqueName);
                        break;
                    }
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

	public String getConfigURL()
	{
		return configURL;
	}

	public ShapeModelType getAuthor()
	{
		return author;
	}

	public ShapeModelBody getBody()
	{
		return body;
	}

	public String getVersion()
	{
		return version;
	}

	public String getShapeModelName()
	{
		return shapeModelName;
	}

	@Override
	public String toString()
	{
		return "BasicConfigInfo [population=" + population + ", shapeModelName=" + shapeModelName + ", uniqueName="
				+ uniqueName + ", author=" + author + ", type=" + type + ", body=" + body + ", dataUsed=" + dataUsed
				+ ", configURL=" + getConfigURL() + ", version=" + version + ", modelLabel=" + modelLabel
				+ ", presentInVersion=" + Arrays.toString(presentInVersion) + ", defaultFor="
				+ Arrays.toString(defaultFor) + ", enabled=" + enabled + "]";
	}
}