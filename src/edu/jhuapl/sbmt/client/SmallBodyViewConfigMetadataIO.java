package edu.jhuapl.sbmt.client;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.SpectrumInstrumentFactory;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;

public class SmallBodyViewConfigMetadataIO implements MetadataManager
{
    private List<ViewConfig> configs;

    public SmallBodyViewConfigMetadataIO()
    {
        this.configs = new Vector<ViewConfig>();
    }

    public SmallBodyViewConfigMetadataIO(List<ViewConfig> configs)
    {
        this.configs = configs;
    }

    public void write(File file, String metadataID) throws IOException
    {
        Serializers.serialize(metadataID, store(), file);
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
        writeEnum(population, c.population, configMetadata);
        writeEnum(dataUsed, c.dataUsed, configMetadata);
        writeEnum(author, c.author, configMetadata);
        write(rootDirOnServer, c.rootDirOnServer, configMetadata);
        write(timeHistoryFile, c.timeHistoryFile, configMetadata);
        write(hasImageMap, c.hasImageMap, configMetadata);
        write(hasStateHistory, c.hasStateHistory, configMetadata);

        writeMetadataArray(imagingInstruments, c.imagingInstruments, configMetadata);

        writeMetadataArray(spectralInstruments, c.spectralInstruments, configMetadata);

        write(hasLidarData, c.hasLidarData, configMetadata);
        write(hasMapmaker, c.hasMapmaker, configMetadata);
        write(hasSpectralData, c.hasSpectralData, configMetadata);
        write(hasLineamentData, c.hasLineamentData, configMetadata);

        writeDate(imageSearchDefaultStartDate, c.imageSearchDefaultStartDate, configMetadata);
        writeDate(imageSearchDefaultEndDate, c.imageSearchDefaultEndDate, configMetadata);
        write(imageSearchFilterNames, c.imageSearchFilterNames, configMetadata);
        write(imageSearchUserDefinedCheckBoxesNames, c.imageSearchUserDefinedCheckBoxesNames, configMetadata);
        write(imageSearchDefaultMaxSpacecraftDistance, c.imageSearchDefaultMaxSpacecraftDistance, configMetadata);
        write(imageSearchDefaultMaxResolution, c.imageSearchDefaultMaxResolution, configMetadata);

        writeDate(lidarSearchDefaultStartDate, c.lidarSearchDefaultStartDate, configMetadata);
        writeDate(lidarSearchDefaultEndDate, c.lidarSearchDefaultEndDate, configMetadata);
        write(lidarSearchDataSourceMap, c.lidarSearchDataSourceMap, configMetadata);
        write(lidarBrowseDataSourceMap, c.lidarBrowseDataSourceMap, configMetadata);

        write(lidarBrowseXYZIndices, c.lidarBrowseXYZIndices, configMetadata);
        write(lidarBrowseIsSpacecraftInSphericalCoordinates, c.lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
        write(lidarBrowseTimeIndex, c.lidarBrowseTimeIndex, configMetadata);
        write(lidarBrowseNoiseIndex, c.lidarBrowseNoiseIndex, configMetadata);
        write(lidarBrowseFileListResourcePath, c.lidarBrowseFileListResourcePath, configMetadata);
        write(lidarBrowseNumberHeaderLines, c.lidarBrowseNumberHeaderLines, configMetadata);
        write(lidarBrowseIsInMeters, c.lidarBrowseIsInMeters, configMetadata);
        write(lidarOffsetScale, c.lidarOffsetScale, configMetadata);
        writeEnum(lidarInstrumentName, c.lidarInstrumentName, configMetadata);
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
        c.body = ShapeModelBody.valueOf(""+read(body, configMetadata));
        c.type = BodyType.valueOf(""+read(type, configMetadata));
        c.population = ShapeModelPopulation.valueOf(""+read(population, configMetadata));
        c.dataUsed =ShapeModelDataUsed.valueOf(""+read(dataUsed, configMetadata));
        c.author = ShapeModelType.valueOf(""+read(author, configMetadata));
        c.rootDirOnServer = read(rootDirOnServer, configMetadata);
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
            System.out
                    .println("SmallBodyViewConfigMetadataIO: retrieve: instrument name " + instrumentName);
            BasicSpectrumInstrument inst = SpectrumInstrumentFactory.getInstrumentForName(instrumentName);
            inst.retrieve(data);
            c.spectralInstruments[i++] = inst;
        }

        c.hasLidarData = read(hasLidarData, configMetadata);
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

        Long lidarSearchDefaultStart = read(lidarSearchDefaultStartDate, configMetadata);
        if (lidarSearchDefaultStart == null) lidarSearchDefaultStart = 0L;
        c.lidarSearchDefaultStartDate = new Date(lidarSearchDefaultStart);
        Long lidarSearchDefaultEnd = read(lidarSearchDefaultEndDate, configMetadata);
        if (lidarSearchDefaultEnd == null) lidarSearchDefaultEnd = 0L;
        c.lidarSearchDefaultEndDate = new Date(lidarSearchDefaultEnd);
        c.lidarSearchDataSourceMap = read(lidarSearchDataSourceMap, configMetadata);
        c.lidarBrowseDataSourceMap = read(lidarBrowseDataSourceMap, configMetadata);

        c.lidarBrowseXYZIndices = read(lidarBrowseXYZIndices, configMetadata);
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = read(lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
        c.lidarBrowseTimeIndex = read(lidarBrowseTimeIndex, configMetadata);
        c.lidarBrowseNoiseIndex = read(lidarBrowseNoiseIndex, configMetadata);
        c.lidarBrowseFileListResourcePath = read(lidarBrowseFileListResourcePath, configMetadata);
        c.lidarBrowseNumberHeaderLines = read(lidarBrowseNumberHeaderLines, configMetadata);
        c.lidarBrowseIsInMeters = read(lidarBrowseIsInMeters, configMetadata);
        c.lidarOffsetScale = read(lidarOffsetScale, configMetadata);
        c.lidarInstrumentName = Instrument.valueOf(""+read(lidarInstrumentName, configMetadata));

    }

    public List<ViewConfig> getConfigs()
    {
        return configs;
    }

    final Key<String> body = Key.of("body");
    final Key<String> type = Key.of("type");
    final Key<String> population = Key.of("population");
    final Key<String> dataUsed = Key.of("dataUsed");
    final Key<String> author = Key.of("author");
    final Key<String> modelLabel = Key.of("author");
    final Key<String> rootDirOnServer = Key.of("rootDirOnServer");
    final Key<String> timeHistoryFile = Key.of("timeHistoryFile");
    final Key<Boolean> hasImageMap = Key.of("hasImageMap");
    final Key<Boolean> hasStateHistory = Key.of("hasStateHistory");

    //capture imaging instruments here
    final Key<Metadata[]> imagingInstruments = Key.of("imagingInstruments");


    //capture spectral instruments here
    final Key<Metadata[]> spectralInstruments = Key.of("spectralInstruments");

    final Key<Boolean> hasLidarData = Key.of("hasLidarData");
    final Key<Boolean> hasMapmaker = Key.of("hasMapmaker");
    final Key<Boolean> hasSpectralData = Key.of("hasSpectralData");
    final Key<Boolean> hasLineamentData = Key.of("hasLineamentData");

    final Key<Long> imageSearchDefaultStartDate = Key.of("imageSearchDefaultStartDate");
    final Key<Long> imageSearchDefaultEndDate = Key.of("imageSearchDefaultEndDate");
    final Key<String[]> imageSearchFilterNames = Key.of("imageSearchFilterNames");
    final Key<String[]> imageSearchUserDefinedCheckBoxesNames = Key.of("imageSearchUserDefinedCheckBoxesNames");
    final Key<Double> imageSearchDefaultMaxSpacecraftDistance = Key.of("imageSearchDefaultMaxSpacecraftDistance");
    final Key<Double> imageSearchDefaultMaxResolution = Key.of("imageSearchDefaultMaxResolution");

    final Key<Long> lidarSearchDefaultStartDate = Key.of("lidarSearchDefaultStartDate");
    final Key<Long> lidarSearchDefaultEndDate = Key.of("lidarSearchDefaultEndDate");

    final Key<Map> lidarSearchDataSourceMap = Key.of("lidarSearchDataSourceMap");
    final Key<Map> lidarBrowseDataSourceMap = Key.of("lidarBrowseDataSourceMap");

    final Key<int[]> lidarBrowseXYZIndices = Key.of("lidarBrowseXYZIndices");

    final Key<Boolean> lidarBrowseIsSpacecraftInSphericalCoordinates = Key.of("lidarBrowseIsSpacecraftInSphericalCoordinates");

    final Key<Integer> lidarBrowseTimeIndex = Key.of("lidarBrowseTimeIndex");
    final Key<Integer> lidarBrowseNoiseIndex = Key.of("lidarBrowseNoiseIndex");
    final Key<String> lidarBrowseFileListResourcePath = Key.of("lidarBrowseFileListResourcePath");
    final Key<Integer> lidarBrowseNumberHeaderLines = Key.of("lidarBrowseNumberHeaderLines");
    final Key<Boolean> lidarBrowseIsInMeters = Key.of("lidarBrowseIsInMeters");
    final Key<Double> lidarOffsetScale = Key.of("lidarOffsetScale");
    final Key<String> lidarInstrumentName = Key.of("lidarInstrumentName");

}
