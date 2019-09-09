package edu.jhuapl.sbmt.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.tools.Authenticator;
import edu.jhuapl.sbmt.tools.DBRunInfo;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class SmallBodyViewConfigMetadataIO implements MetadataManager
{
    public static void main(String[] args) throws IOException
    {
        SettableMetadata allBodiesMetadata = SettableMetadata.of(Version.of(1, 0));
        Configuration.setAPLVersion(true);
        SbmtMultiMissionTool.configureMission();
        Authenticator.authenticate();
        SmallBodyViewConfig.initialize();
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            each.enable(true);
        }

        String rootDir = "/Users/steelrj1/Desktop/configs9/";

        List<ViewConfig> builtInConfigs = SmallBodyViewConfig.getBuiltInConfigs();
        for (ViewConfig config : builtInConfigs)
        {
            try
            {
//            System.out.println("SmallBodyViewConfigMetadataIO: main: body is " + config.body);
                SmallBodyViewConfigMetadataIO io = new SmallBodyViewConfigMetadataIO(config);
                String version = config.version == null ? "" : config.version;

                File file = new File(rootDir + ((SmallBodyViewConfig)config).rootDirOnServer + "/" + config.author +  "_" + config.body.toString().replaceAll(" ", "_") + version.replaceAll(" ", "_") + ".json");
//            if (version != null && (version.length() > 0))
//            	allBodiesMetadata.put(Key.of(config.getUniqueName()), "http://sbmt.jhuapl.edu/sbmt/prod/data" + ((SmallBodyViewConfig)config).rootDirOnServer + "/" + config.author +  "_" + config.body.toString().replaceAll(" ", "_") + version.replaceAll(" ", "_") + ".json");
//            else
//            	allBodiesMetadata.put(Key.of(config.getUniqueName()), "http://sbmt.jhuapl.edu/sbmt/prod/data" + ((SmallBodyViewConfig)config).rootDirOnServer + "/" + config.author +  "_" + config.body.toString().replaceAll(" ", "_") + version.replaceAll(" ", "_") + ".json");

                BasicConfigInfo configInfo = new BasicConfigInfo((BodyViewConfig)config);
                allBodiesMetadata.put(Key.of(config.getUniqueName()), configInfo.store());

//            System.out.println("SmallBodyViewConfigMetadataIO: main: file is " + file);
                if (!file.exists()) file.getParentFile().mkdirs();
                io.write(config.getUniqueName(), file, io.store());
            }
            catch (Exception e)
            {
                System.err.println("WARNING: EXCEPTION! SKIPPING CONFIG " + config.getUniqueName());
                e.printStackTrace();
            }
        }

        Serializers.serialize("AllBodies", allBodiesMetadata, new File(rootDir + "allBodies.json"));


    }

    private List<ViewConfig> configs;
    private String metadataID;

    public SmallBodyViewConfigMetadataIO()
    {
        this.configs = new Vector<ViewConfig>();
    }

    public SmallBodyViewConfigMetadataIO(List<ViewConfig> configs)
    {
        this.configs = configs;
    }

    public SmallBodyViewConfigMetadataIO(ViewConfig config)
    {
        this.configs = new Vector<ViewConfig>();
        this.configs.add(config);
    }

    public void write(File file, String metadataID) throws IOException
    {
        Serializers.serialize(metadataID, store(), file);
    }

    private void write(String metadataID, File file, Metadata metadata) throws IOException
    {
        Serializers.serialize(metadataID, metadata, file);
    }

    public void read(File file, String metadataID, SmallBodyViewConfig config) throws IOException
    {
        FixedMetadata metadata = Serializers.deserialize(file, metadataID);
        this.metadataID = metadataID;
        configs.add(config);
        retrieve(metadata);
    }

    private SettableMetadata storeConfig(ViewConfig config)
    {
        SmallBodyViewConfig c = (SmallBodyViewConfig)config;
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        writeEnum(body, c.body, configMetadata);
        writeEnum(type, c.type, configMetadata);
        write(version, c.version, configMetadata);
        writeEnum(population, c.population, configMetadata);
        writeEnum(dataUsed, c.dataUsed, configMetadata);
        write(author, c.author.name(), configMetadata);
        write(modelLabel, c.modelLabel, configMetadata);
        write(rootDirOnServer, c.rootDirOnServer, configMetadata);
        write(shapeModelFileExtension, c.shapeModelFileExtension, configMetadata);
        write(shapeModelFileBaseName, c.shapeModelFileBaseName, configMetadata);
        write(shapeModelFileNamesKey, c.shapeModelFileNames, configMetadata);
        String[] resolutionsToSave = new String[c.getResolutionLabels().size()];
        Integer[] platesPerResToSave = new Integer[c.getResolutionNumberElements().size()];
        write(resolutions, c.getResolutionLabels().toArray(resolutionsToSave), configMetadata);
        write(platesPerRes, c.getResolutionNumberElements().toArray(platesPerResToSave), configMetadata);
        write(timeHistoryFile, c.timeHistoryFile, configMetadata);
        write(hasImageMap, c.hasImageMap, configMetadata);
        write(hasStateHistory, c.hasStateHistory, configMetadata);

        writeMetadataArray(imagingInstruments, c.imagingInstruments, configMetadata);

        writeMetadataArray(spectralInstruments, c.spectralInstruments, configMetadata);

        write(hasLidarData, c.hasLidarData, configMetadata);
        write(hasHypertreeBasedLidarSearch, c.hasHypertreeBasedLidarSearch, configMetadata);
        write(hasMapmaker, c.hasMapmaker, configMetadata);
        write(hasSpectralData, c.hasSpectralData, configMetadata);
        write(hasLineamentData, c.hasLineamentData, configMetadata);

        writeDate(imageSearchDefaultStartDate, c.imageSearchDefaultStartDate, configMetadata);
        writeDate(imageSearchDefaultEndDate, c.imageSearchDefaultEndDate, configMetadata);
        write(imageSearchFilterNames, c.imageSearchFilterNames, configMetadata);
        write(imageSearchUserDefinedCheckBoxesNames, c.imageSearchUserDefinedCheckBoxesNames, configMetadata);
        write(imageSearchDefaultMaxSpacecraftDistance, c.imageSearchDefaultMaxSpacecraftDistance, configMetadata);
        write(imageSearchDefaultMaxResolution, c.imageSearchDefaultMaxResolution, configMetadata);
        write(hasHierarchicalImageSearch, c.hasHierarchicalImageSearch, configMetadata);
        if (c.hasHierarchicalImageSearch && c.hierarchicalImageSearchSpecification != null)
            write(hierarchicalImageSearchSpecification, c.hierarchicalImageSearchSpecification.getMetadataManager().store(), configMetadata);


        write(hasHierarchicalSpectraSearch, c.hasHierarchicalSpectraSearch, configMetadata);
        write(hasHypertreeBasedSpectraSearch, c.hasHypertreeBasedSpectraSearch, configMetadata);
        write(spectraSearchDataSourceMap, c.spectraSearchDataSourceMap, configMetadata);
        write(spectrumMetadataFile, c.spectrumMetadataFile, configMetadata);

        if (c.hasHierarchicalSpectraSearch && c.hierarchicalSpectraSearchSpecification != null)
        {
        	try
			{
				c.hierarchicalSpectraSearchSpecification.loadMetadata();
			}
        	catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            write(hierarchicalSpectraSearchSpecification, c.hierarchicalSpectraSearchSpecification.getMetadataManager().store(), configMetadata);
        }

        //dtm
        if (c.dtmBrowseDataSourceMap.size() > 0 )
        	write(dtmBrowseDataSourceMap, c.dtmBrowseDataSourceMap, configMetadata);
        if (c.dtmSearchDataSourceMap.size() > 0 )
        	write(dtmSearchDataSourceMap, c.dtmSearchDataSourceMap, configMetadata);

        //lidar
        writeDate(lidarSearchDefaultStartDate, c.lidarSearchDefaultStartDate, configMetadata);
        writeDate(lidarSearchDefaultEndDate, c.lidarSearchDefaultEndDate, configMetadata);
        write(lidarSearchDataSourceMap, c.lidarSearchDataSourceMap, configMetadata);
        write(lidarBrowseDataSourceMap, c.lidarBrowseDataSourceMap, configMetadata);

        write(lidarSearchDataSourceTimeMap, c.lidarSearchDataSourceTimeMap, configMetadata);
        write(orexSearchTimeMap, c.orexSearchTimeMap, configMetadata);

        write(lidarBrowseXYZIndices, c.lidarBrowseXYZIndices, configMetadata);
        write(lidarBrowseSpacecraftIndices, c.lidarBrowseSpacecraftIndices, configMetadata);
        write(lidarBrowseIsSpacecraftInSphericalCoordinates, c.lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
        write(lidarBrowseTimeIndex, c.lidarBrowseTimeIndex, configMetadata);
        write(lidarBrowseNoiseIndex, c.lidarBrowseNoiseIndex, configMetadata);
        write(lidarBrowseOutgoingIntensityIndex, c.lidarBrowseOutgoingIntensityIndex, configMetadata);
        write(lidarBrowseReceivedIntensityIndex, c.lidarBrowseReceivedIntensityIndex, configMetadata);
        write(lidarBrowseFileListResourcePath, c.lidarBrowseFileListResourcePath, configMetadata);
        write(lidarBrowseNumberHeaderLines, c.lidarBrowseNumberHeaderLines, configMetadata);
        write(lidarBrowseIsInMeters, c.lidarBrowseIsInMeters, configMetadata);
        write(lidarBrowseIsBinary, c.lidarBrowseIsBinary, configMetadata);
        write(lidarBrowseBinaryRecordSize, c.lidarBrowseBinaryRecordSize, configMetadata);
        write(lidarOffsetScale, c.lidarOffsetScale, configMetadata);
        writeEnum(lidarInstrumentName, c.lidarInstrumentName, configMetadata);

        if (c.defaultForMissions != null)
        {
	        String[] defaultStrings = new String[c.defaultForMissions.length];
	        int i=0;
	        for (SbmtMultiMissionTool.Mission mission : c.defaultForMissions)
	        {
	        	defaultStrings[i++] = mission.getHashedName();
	        }
	        write(defaultForMissions, defaultStrings, configMetadata);
        }

        String[] presentStrings = new String[c.presentInMissions.length];
        int i=0;
        for (SbmtMultiMissionTool.Mission mission : c.presentInMissions)
        {
        	presentStrings[i++] = mission.getHashedName();
        }
        write(presentInMissions, presentStrings, configMetadata);

        writeMetadataArray(runInfos, c.databaseRunInfos, configMetadata);

        return configMetadata;
    }

    @Override
    public Metadata store()
    {
        List<ViewConfig> builtInConfigs = configs;
        if (builtInConfigs.size() == 1)
        {
            SettableMetadata configMetadata = storeConfig(builtInConfigs.get(0));
            return configMetadata;
        }
        else
        {
            SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
            for (ViewConfig config : builtInConfigs)
            {
                SettableMetadata configMetadata = storeConfig(config);
                Key<SettableMetadata> metadata = Key.of(config.getUniqueName());
                result.put(metadata, configMetadata);
            }
            return result;
        }
    }

    private <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    private <T> void writeEnum(Key<String> key, Enum value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value.name());
        }
    }


    private <T> void writeDate(Key<Long> key, Date value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value.getTime());
        }
    }

    private <T> void writeMetadataArray(Key<Metadata[]> key, MetadataManager[] values, SettableMetadata configMetadata)
    {
        if (values != null)
        {
            Metadata[] data = new Metadata[values.length];
            int i=0;
            for (MetadataManager val : values) data[i++] = val.store();
            configMetadata.put(key, data);
        }
    }

    private Metadata[] readMetadataArray(Key<Metadata[]> key, Metadata configMetadata)
    {
        Metadata[] values = configMetadata.get(key);
        if (values != null)
        {
            return values;
        }
        return null;
    }

    private <T> T read(Key<T> key, Metadata configMetadata)
    {
        if (configMetadata.hasKey(key) == false) return null;
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }

    @Override
    public void retrieve(Metadata configMetadata)
    {
        SmallBodyViewConfig c = (SmallBodyViewConfig)configs.get(0);
        c.body = ShapeModelBody.valueOf(read(body, configMetadata));
        c.type = BodyType.valueOf(read(type, configMetadata));
        c.version = read(version, configMetadata);
        c.population = ShapeModelPopulation.valueOf(read(population, configMetadata));
        c.dataUsed =ShapeModelDataUsed.valueOf(read(dataUsed, configMetadata));
        c.author = ShapeModelType.provide(read(author, configMetadata));
        c.modelLabel = read(modelLabel, configMetadata);
        c.rootDirOnServer = read(rootDirOnServer, configMetadata);
        c.shapeModelFileExtension = read(shapeModelFileExtension, configMetadata);
        c.shapeModelFileBaseName = read(shapeModelFileBaseName, configMetadata);
        String[] resolutionsToAdd = read(resolutions, configMetadata);
        c.shapeModelFileNames = read(shapeModelFileNamesKey, configMetadata);
        Integer[] platesPerResToAdd = read(platesPerRes, configMetadata);
        if (resolutionsToAdd != null && platesPerResToAdd != null)
        	c.setResolution(ImmutableList.copyOf(resolutionsToAdd), ImmutableList.copyOf(platesPerResToAdd));
        c.timeHistoryFile = read(timeHistoryFile, configMetadata);
        c.hasImageMap = read(hasImageMap, configMetadata);
        c.hasStateHistory = read(hasStateHistory, configMetadata);

        Metadata[] imagingMetadata = readMetadataArray(imagingInstruments, configMetadata);
        c.imagingInstruments = new ImagingInstrument[imagingMetadata.length];
        int i=0;
        for (Metadata data : imagingMetadata)
        {
            ImagingInstrument inst = new ImagingInstrument();
            inst.retrieve(data);
            c.imagingInstruments[i++] = inst;
        }

        Metadata[] spectralMetadata = readMetadataArray(spectralInstruments, configMetadata);
        c.spectralInstruments = new BasicSpectrumInstrument[spectralMetadata.length];
        i=0;
        for (Metadata data : spectralMetadata)
        {
            String instrumentName = (String)data.get(Key.of("displayName"));
            BasicSpectrumInstrument inst = SpectrumInstrumentFactory.getInstrumentForName(instrumentName);
            inst.retrieve(data);
            c.spectralInstruments[i++] = inst;
        }

        c.hasLidarData = read(hasLidarData, configMetadata);
        c.hasHypertreeBasedLidarSearch = read(hasHypertreeBasedLidarSearch, configMetadata);
        c.hasMapmaker = read(hasMapmaker, configMetadata);
        c.hasSpectralData = read(hasSpectralData, configMetadata);
        c.hasLineamentData = read(hasLineamentData, configMetadata);

        if (c.imagingInstruments.length > 0)
        {
	        Long imageSearchDefaultStart = read(imageSearchDefaultStartDate, configMetadata);
	        Long imageSearchDefaultEnd = read(imageSearchDefaultEndDate, configMetadata);
	        c.imageSearchDefaultStartDate = new Date(imageSearchDefaultStart);
	        c.imageSearchDefaultEndDate = new Date(imageSearchDefaultEnd);
	        c.imageSearchFilterNames = read(imageSearchFilterNames, configMetadata);
	        c.imageSearchUserDefinedCheckBoxesNames = read(imageSearchUserDefinedCheckBoxesNames, configMetadata);
	        c.imageSearchDefaultMaxSpacecraftDistance = read(imageSearchDefaultMaxSpacecraftDistance, configMetadata);
	        c.imageSearchDefaultMaxResolution = read(imageSearchDefaultMaxResolution, configMetadata);
	        if (configMetadata.hasKey(hasHierarchicalImageSearch))
	        	c.hasHierarchicalImageSearch = read(hasHierarchicalImageSearch, configMetadata);

//        	c.hierarchicalImageSearchSpecification.getMetadataManager().retrieve(read(hierarchicalImageSearchSpecification, configMetadata));


        }

        if (c.hasSpectralData && c.spectralInstruments.length > 0)
        {
        	if (configMetadata.hasKey(hasHierarchicalSpectraSearch))
        		c.hasHierarchicalSpectraSearch = read(hasHierarchicalSpectraSearch, configMetadata);
        	if (configMetadata.hasKey(hasHypertreeBasedSpectraSearch))
        		c.hasHypertreeBasedSpectraSearch = read(hasHypertreeBasedSpectraSearch, configMetadata);
	        c.spectraSearchDataSourceMap = read(spectraSearchDataSourceMap, configMetadata);
	        c.spectrumMetadataFile = read(spectrumMetadataFile, configMetadata);

	        if (c.hasHierarchicalSpectraSearch)
	        {
//	        	c.hierarchicalSpectraSearchSpecification.getMetadataManager().retrieve(read(hierarchicalSpectraSearchSpecification, configMetadata));
	        }
        }

        if (configMetadata.hasKey(dtmSearchDataSourceMap))
        	c.dtmSearchDataSourceMap = read(dtmSearchDataSourceMap, configMetadata);
        if (configMetadata.hasKey(dtmBrowseDataSourceMap))
        	c.dtmBrowseDataSourceMap = read(dtmBrowseDataSourceMap, configMetadata);

        if (c.hasLidarData)
        {
	        Long lidarSearchDefaultStart = read(lidarSearchDefaultStartDate, configMetadata);
	        if (lidarSearchDefaultStart == null) lidarSearchDefaultStart = 0L;
	        c.lidarSearchDefaultStartDate = new Date(lidarSearchDefaultStart);
	        Long lidarSearchDefaultEnd = read(lidarSearchDefaultEndDate, configMetadata);
	        if (lidarSearchDefaultEnd == null) lidarSearchDefaultEnd = 0L;
	        c.lidarSearchDefaultEndDate = new Date(lidarSearchDefaultEnd);
	        c.lidarSearchDataSourceMap = read(lidarSearchDataSourceMap, configMetadata);
	        c.lidarBrowseDataSourceMap = read(lidarBrowseDataSourceMap, configMetadata);

	        c.lidarSearchDataSourceTimeMap = read(lidarSearchDataSourceTimeMap, configMetadata);
	        c.orexSearchTimeMap = read(orexSearchTimeMap, configMetadata);

	        c.lidarBrowseXYZIndices = read(lidarBrowseXYZIndices, configMetadata);
	        c.lidarBrowseSpacecraftIndices = read(lidarBrowseSpacecraftIndices, configMetadata);
	        c.lidarBrowseIsSpacecraftInSphericalCoordinates = read(lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
	        c.lidarBrowseTimeIndex = read(lidarBrowseTimeIndex, configMetadata);
	        c.lidarBrowseNoiseIndex = read(lidarBrowseNoiseIndex, configMetadata);
	        c.lidarBrowseOutgoingIntensityIndex = read(lidarBrowseOutgoingIntensityIndex, configMetadata);
	        c.lidarBrowseReceivedIntensityIndex = read(lidarBrowseReceivedIntensityIndex, configMetadata);
	        c.lidarBrowseFileListResourcePath = read(lidarBrowseFileListResourcePath, configMetadata);
	        c.lidarBrowseNumberHeaderLines = read(lidarBrowseNumberHeaderLines, configMetadata);
	        c.lidarBrowseIsInMeters = read(lidarBrowseIsInMeters, configMetadata);
	        c.lidarBrowseIsBinary = read(lidarBrowseIsBinary, configMetadata);
	        c.lidarBrowseBinaryRecordSize = read(lidarBrowseBinaryRecordSize, configMetadata);
	        c.lidarOffsetScale = read(lidarOffsetScale, configMetadata);
	        c.lidarInstrumentName = Instrument.valueOf(""+read(lidarInstrumentName, configMetadata));

        }

        String[] presentInMissionStrings = read(presentInMissions, configMetadata);
        if (presentInMissionStrings == null)
        {
        	presentInMissionStrings = new String[SbmtMultiMissionTool.Mission.values().length];
        	int ii=0;
        	for (SbmtMultiMissionTool.Mission mission : SbmtMultiMissionTool.Mission.values())
        		presentInMissionStrings[ii++] = mission.getHashedName();
        }
        c.presentInMissions = new SbmtMultiMissionTool.Mission[presentInMissionStrings.length];
        int m=0;
        for (String defStr : presentInMissionStrings)
        {
        	c.presentInMissions[m++] = SbmtMultiMissionTool.Mission.getMissionForName(defStr);
        }

        if (configMetadata.hasKey(defaultForMissions))
        {
	        String[] defaultsForMissionStrings = read(defaultForMissions, configMetadata);
	        c.defaultForMissions = new SbmtMultiMissionTool.Mission[defaultsForMissionStrings.length];
	        int k=0;
	        for (String defStr : defaultsForMissionStrings)
	        {
	        	c.defaultForMissions[k++] = SbmtMultiMissionTool.Mission.getMissionForName(defStr);
	        }
//	        if (missionsToAdd != null)
//	        {
//	            for (String mission : missionsToAdd)
//	            {
//	                SbmtMultiMissionTool.Mission msn = SbmtMultiMissionTool.Mission.getMissionForName(mission);
//	                c.missions.add(msn);
//	                if (SbmtMultiMissionTool.getMission() == msn)
//	                {
//	                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//	                }
//	            }
//	        }
        }

        if (configMetadata.hasKey(runInfos))
        {
	        Metadata[] runInfoMetadata = readMetadataArray(runInfos, configMetadata);
	        c.databaseRunInfos = new DBRunInfo[runInfoMetadata.length];
	        i=0;
	        for (Metadata data : runInfoMetadata)
	        {
	        	DBRunInfo info = new DBRunInfo();
	        	info.retrieve(data);
	        	c.databaseRunInfos[i++] = info;
	        }
        }

        if (c.author == ShapeModelType.CUSTOM)
        {
        	c.modelLabel = metadataID;
        }
    }

    public List<ViewConfig> getConfigs()
    {
        return configs;
    }

    final Key<String> body = Key.of("body");
    final Key<String> type = Key.of("type");
    final Key<String> version = Key.of("version");
    final Key<String> population = Key.of("population");
    final Key<String> dataUsed = Key.of("dataUsed");
    final Key<String> author = Key.of("author");
    final Key<String> modelLabel = Key.of("modelLabel");
    final Key<String> rootDirOnServer = Key.of("rootDirOnServer");
    final Key<String> shapeModelFileExtension = Key.of("shapeModelFileExtension");
    final Key<String> shapeModelFileBaseName = Key.of("shapeModelFileBaseName");
    final Key<String[]> shapeModelFileNamesKey = Key.of("shapeModelFileNames");
    final Key<String[]> resolutions = Key.of("resolutions");
    final Key<Integer[]> platesPerRes = Key.of("platesPerRes");
    final Key<String> timeHistoryFile = Key.of("timeHistoryFile");
    final Key<Boolean> hasImageMap = Key.of("hasImageMap");
    final Key<Boolean> hasStateHistory = Key.of("hasStateHistory");
    final Key<String[]> presentInMissions = Key.of("presentInMissions");
    final Key<String[]> defaultForMissions = Key.of("defaultForMissions");

    //capture imaging instruments here
    final Key<Metadata[]> imagingInstruments = Key.of("imagingInstruments");


    //capture spectral instruments here
    final Key<Metadata[]> spectralInstruments = Key.of("spectralInstruments");

    //DTM
    final Key<Map> dtmSearchDataSourceMap = Key.of("dtmSearchDataSourceMap");
    final Key<Map> dtmBrowseDataSourceMap = Key.of("dtmBrowseDataSourceMap");


    final Key<Boolean> hasLidarData = Key.of("hasLidarData");
    final Key<Boolean> hasHypertreeBasedLidarSearch = Key.of("hasHypertreeBasedLidarSearch");
    final Key<Boolean> hasMapmaker = Key.of("hasMapmaker");
    final Key<Boolean> hasSpectralData = Key.of("hasSpectralData");
    final Key<Boolean> hasLineamentData = Key.of("hasLineamentData");

    final Key<Long> imageSearchDefaultStartDate = Key.of("imageSearchDefaultStartDate");
    final Key<Long> imageSearchDefaultEndDate = Key.of("imageSearchDefaultEndDate");
    final Key<String[]> imageSearchFilterNames = Key.of("imageSearchFilterNames");
    final Key<String[]> imageSearchUserDefinedCheckBoxesNames = Key.of("imageSearchUserDefinedCheckBoxesNames");
    final Key<Double> imageSearchDefaultMaxSpacecraftDistance = Key.of("imageSearchDefaultMaxSpacecraftDistance");
    final Key<Double> imageSearchDefaultMaxResolution = Key.of("imageSearchDefaultMaxResolution");
    final Key<Boolean> hasHierarchicalImageSearch = Key.of("hasHierarchicalImageSearch");
    final Key<Metadata> hierarchicalImageSearchSpecification = Key.of("hierarchicalImageSearchSpecification");


    final Key<Boolean> hasHierarchicalSpectraSearch = Key.of("hasHierarchicalSpectraSearch");
    final Key<Boolean> hasHypertreeBasedSpectraSearch = Key.of("hasHypertreeSpectraSearch");
    final Key<Map> spectraSearchDataSourceMap = Key.of("spectraSearchDataSourceMap");
    final Key<String> spectrumMetadataFile = Key.of("spectrumMetadataFile");
    final Key<Metadata> hierarchicalSpectraSearchSpecification = Key.of("hierarchicalSpectraSearchSpecification");


    final Key<Long> lidarSearchDefaultStartDate = Key.of("lidarSearchDefaultStartDate");
    final Key<Long> lidarSearchDefaultEndDate = Key.of("lidarSearchDefaultEndDate");

    final Key<Map> lidarSearchDataSourceMap = Key.of("lidarSearchDataSourceMap");
    final Key<Map> lidarBrowseDataSourceMap = Key.of("lidarBrowseDataSourceMap");

    final Key<Map> lidarSearchDataSourceTimeMap = Key.of("lidarSearchDataSourceTimeMap");
    final Key<Map> orexSearchTimeMap = Key.of("orexSearchTimeMap");

    final Key<int[]> lidarBrowseXYZIndices = Key.of("lidarBrowseXYZIndices");
    final Key<int[]> lidarBrowseSpacecraftIndices = Key.of("lidarBrowseSpacecraftIndices");

    final Key<Boolean> lidarBrowseIsSpacecraftInSphericalCoordinates = Key.of("lidarBrowseIsSpacecraftInSphericalCoordinates");

    final Key<Integer> lidarBrowseTimeIndex = Key.of("lidarBrowseTimeIndex");
    final Key<Integer> lidarBrowseNoiseIndex = Key.of("lidarBrowseNoiseIndex");
    final Key<Integer> lidarBrowseOutgoingIntensityIndex = Key.of("lidarBrowseOutgoingIntensityIndex");
    final Key<Integer> lidarBrowseReceivedIntensityIndex = Key.of("lidarBrowseReceivedIntensityIndex");
    final Key<String> lidarBrowseFileListResourcePath = Key.of("lidarBrowseFileListResourcePath");
    final Key<Integer> lidarBrowseNumberHeaderLines = Key.of("lidarBrowseNumberHeaderLines");
    final Key<Boolean> lidarBrowseIsInMeters = Key.of("lidarBrowseIsInMeters");
    final Key<Double> lidarOffsetScale = Key.of("lidarOffsetScale");
    final Key<Boolean> lidarBrowseIsBinary = Key.of("lidarBrowseIsBinary");
    final Key<Integer> lidarBrowseBinaryRecordSize = Key.of("lidarBrowseBinaryRecordSize");
    final Key<String> lidarInstrumentName = Key.of("lidarInstrumentName");

    final Key<Metadata[]> runInfos = Key.of("runInfos");

}
