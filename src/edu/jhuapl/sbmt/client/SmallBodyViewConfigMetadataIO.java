package edu.jhuapl.sbmt.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.metadata.FixedMetadata;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.metadata.serialization.Serializers;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.tools.DBRunInfo;

public class SmallBodyViewConfigMetadataIO implements MetadataManager
{
    public static void main(String[] args) throws IOException
    {
        SettableMetadata allBodiesMetadata = SettableMetadata.of(Version.of(1, 0));
        Configuration.setAPLVersion(true);
        SmallBodyViewConfig.initialize();
        for (ViewConfig each: SmallBodyViewConfig.getBuiltInConfigs())
        {
            each.enable(true);
        }
        List<ViewConfig> builtInConfigs = SmallBodyViewConfig.getBuiltInConfigs();
        for (ViewConfig config : builtInConfigs)
        {
            System.out.println("SmallBodyViewConfigMetadataIO: main: body is " + config.body);
            SmallBodyViewConfigMetadataIO io = new SmallBodyViewConfigMetadataIO(config);
            String version = config.version == null ? "" : config.version;
            File file = new File("/Users/steelrj1/Desktop/configs2/" + config.author + "/" + config.author + "_" + config.body.toString().replaceAll(" ", "_") + version + ".json");

            allBodiesMetadata.put(Key.of(config.author + "/" + config.body + version), file.getAbsolutePath());

            System.out.println("SmallBodyViewConfigMetadataIO: main: file is " + file);
            if (!file.exists()) file.getParentFile().mkdirs();
            io.write(config.getUniqueName(), file, io.store());
        }

        Serializers.serialize("AllBodies", allBodiesMetadata, new File("/Users/steelrj1/Desktop/configs2/allBodies.json"));


    }

    private List<ViewConfig> configs;

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
            write(hierarchicalSpectraSearchSpecification, c.hierarchicalSpectraSearchSpecification.getMetadataManager().store(), configMetadata);


        writeDate(lidarSearchDefaultStartDate, c.lidarSearchDefaultStartDate, configMetadata);
        writeDate(lidarSearchDefaultEndDate, c.lidarSearchDefaultEndDate, configMetadata);
        write(lidarSearchDataSourceMap, c.lidarSearchDataSourceMap, configMetadata);
        write(lidarBrowseDataSourceMap, c.lidarBrowseDataSourceMap, configMetadata);

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

        String[] missionsToSave = new String[c.missions.size()];
        write(missions, c.missions.toArray(missionsToSave), configMetadata);

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
        c.author = ShapeModelType.valueOf(read(author, configMetadata));
        c.modelLabel = read(modelLabel, configMetadata);
        c.rootDirOnServer = read(rootDirOnServer, configMetadata);
        c.shapeModelFileExtension = read(shapeModelFileExtension, configMetadata);
        c.shapeModelFileBaseName = read(shapeModelFileBaseName, configMetadata);
        String[] resolutionsToAdd = read(resolutions, configMetadata);
        Integer[] platesPerResToAdd = read(platesPerRes, configMetadata);
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

        Long imageSearchDefaultStart = read(imageSearchDefaultStartDate, configMetadata);
        Long imageSearchDefaultEnd = read(imageSearchDefaultEndDate, configMetadata);
        c.imageSearchDefaultStartDate = new Date(imageSearchDefaultStart);
        c.imageSearchDefaultEndDate = new Date(imageSearchDefaultEnd);
        c.imageSearchFilterNames = read(imageSearchFilterNames, configMetadata);
        c.imageSearchUserDefinedCheckBoxesNames = read(imageSearchUserDefinedCheckBoxesNames, configMetadata);
        c.imageSearchDefaultMaxSpacecraftDistance = read(imageSearchDefaultMaxSpacecraftDistance, configMetadata);
        c.imageSearchDefaultMaxResolution = read(imageSearchDefaultMaxResolution, configMetadata);
        c.hasHierarchicalImageSearch = read(hasHierarchicalImageSearch, configMetadata);

//        c.hierarchicalImageSearchSpecification.getMetadataManager().retrieve(read(hierarchicalImageSearchSpecification, configMetadata));

        c.hasHierarchicalSpectraSearch = read(hasHierarchicalSpectraSearch, configMetadata);
        c.hasHypertreeBasedSpectraSearch = read(hasHypertreeBasedSpectraSearch, configMetadata);
        c.spectraSearchDataSourceMap = read(spectraSearchDataSourceMap, configMetadata);
        c.spectrumMetadataFile = read(spectrumMetadataFile, configMetadata);

//        c.hierarchicalSpectraSearchSpecification.getMetadataManager().retrieve(read(hierarchicalSpectraSearchSpecification, configMetadata));

        Long lidarSearchDefaultStart = read(lidarSearchDefaultStartDate, configMetadata);
        if (lidarSearchDefaultStart == null) lidarSearchDefaultStart = 0L;
        c.lidarSearchDefaultStartDate = new Date(lidarSearchDefaultStart);
        Long lidarSearchDefaultEnd = read(lidarSearchDefaultEndDate, configMetadata);
        if (lidarSearchDefaultEnd == null) lidarSearchDefaultEnd = 0L;
        c.lidarSearchDefaultEndDate = new Date(lidarSearchDefaultEnd);
        c.lidarSearchDataSourceMap = read(lidarSearchDataSourceMap, configMetadata);
        c.lidarBrowseDataSourceMap = read(lidarBrowseDataSourceMap, configMetadata);

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
        String[] missionsToAdd = read(missions, configMetadata);
        c.missions = new ArrayList<>();
        if (missionsToAdd != null)
        {
            for (String mission : missionsToAdd)
            {
                SbmtMultiMissionTool.Mission msn = SbmtMultiMissionTool.Mission.getMissionForName(mission);
                c.missions.add(msn);
                if (SbmtMultiMissionTool.getMission() == msn)
                {
                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
                }
            }
        }

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
    final Key<String[]> resolutions = Key.of("resolutions");
    final Key<Integer[]> platesPerRes = Key.of("platesPerRes");
    final Key<String> timeHistoryFile = Key.of("timeHistoryFile");
    final Key<Boolean> hasImageMap = Key.of("hasImageMap");
    final Key<Boolean> hasStateHistory = Key.of("hasStateHistory");
    final Key<String[]> missions = Key.of("missions");

    //capture imaging instruments here
    final Key<Metadata[]> imagingInstruments = Key.of("imagingInstruments");


    //capture spectral instruments here
    final Key<Metadata[]> spectralInstruments = Key.of("spectralInstruments");

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
