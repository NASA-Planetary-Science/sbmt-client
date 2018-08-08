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
import edu.jhuapl.sbmt.model.image.Instrument;

public class SmallBodyViewConfigMetadataExporter implements MetadataManager
{
    private List<ViewConfig> configs;

    public SmallBodyViewConfigMetadataExporter()
    {
        this.configs = new Vector<ViewConfig>();
    }

    public SmallBodyViewConfigMetadataExporter(List<ViewConfig> configs)
    {
        this.configs = configs;
    }

    public void write(File file, String metadataID) throws IOException
    {
        Serializers.serialize(metadataID, store(), file);
    }

    public void read(File file, String metadataID, SmallBodyViewConfig config) throws IOException
    {
        System.out.println("SmallBodyViewConfigMetadataExporter: read: reading " + metadataID + " from " + file.getAbsolutePath());
        FixedMetadata metadata = Serializers.deserialize(file, metadataID);
        System.out.println("SmallBodyViewConfigMetadataExporter: read: " + metadata.get(metadata.getKeys().get(0)).toString());
        configs.add(config);
        retrieve(metadata);
//        retrieve((SettableMetadata)metadata.get(metadata.getKeys().get(0)));
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

        write(hasLidarData, c.hasLidarData, configMetadata);
        write(hasMapmaker, c.hasMapmaker, configMetadata);
        write(hasSpectralData, c.hasSpectralData, configMetadata);
        write(hasLineamentData, c.hasLineamentData, configMetadata);

        writeDate(imageSearchDefaultStartDate, c.imageSearchDefaultStartDate, configMetadata);
        writeDate(imageSearchDefaultEndDate, c.imageSearchDefaultEndDate, configMetadata);
//        write(imageSearchFilterNames, c.imageSearchFilterNames, configMetadata);
//        write(imageSearchUserDefinedCheckBoxesNames, c.imageSearchUserDefinedCheckBoxesNames, configMetadata);
        write(imageSearchDefaultMaxSpacecraftDistance, c.imageSearchDefaultMaxSpacecraftDistance, configMetadata);
        write(imageSearchDefaultMaxResolution, c.imageSearchDefaultMaxResolution, configMetadata);

        writeDate(lidarSearchDefaultStartDate, c.lidarSearchDefaultStartDate, configMetadata);
        writeDate(lidarSearchDefaultEndDate, c.lidarSearchDefaultEndDate, configMetadata);
        write(lidarSearchDataSourceMap, c.lidarSearchDataSourceMap, configMetadata);
        write(lidarBrowseDataSourceMap, c.lidarBrowseDataSourceMap, configMetadata);

//        write(lidarBrowseXYZIndices, c.lidarBrowseXYZIndices, configMetadata);
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

    private <T> T read(Key<T> key, Metadata configMetadata)
    {
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

        c.hasLidarData = read(hasLidarData, configMetadata);
        c.hasMapmaker = read(hasMapmaker, configMetadata);
        c.hasSpectralData = read(hasSpectralData, configMetadata);
        c.hasLineamentData = read(hasLineamentData, configMetadata);

        System.out.println(
                "SmallBodyViewConfigMetadataExporter: retrieve: date " + read(imageSearchDefaultStartDate, configMetadata));
        c.imageSearchDefaultStartDate = new Date(read(imageSearchDefaultStartDate, configMetadata));
        c.imageSearchDefaultEndDate = new Date(read(imageSearchDefaultEndDate, configMetadata));
//        c.imageSearchFilterNames = read(imageSearchFilterNames, configMetadata);
//        c.imageSearchUserDefinedCheckBoxesNames = read(imageSearchUserDefinedCheckBoxesNames, configMetadata);
        c.imageSearchDefaultMaxSpacecraftDistance = read(imageSearchDefaultMaxSpacecraftDistance, configMetadata);
        c.imageSearchDefaultMaxResolution = read(imageSearchDefaultMaxResolution, configMetadata);

        c.lidarSearchDefaultStartDate = new Date(read(lidarSearchDefaultStartDate, configMetadata));
        c.lidarSearchDefaultEndDate = new Date(read(lidarSearchDefaultEndDate, configMetadata));
        c.lidarSearchDataSourceMap = read(lidarSearchDataSourceMap, configMetadata);
        c.lidarBrowseDataSourceMap = read(lidarBrowseDataSourceMap, configMetadata);

//        c.lidarBrowseXYZIndices = read(lidarBrowseXYZIndices, configMetadata);
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = read(lidarBrowseIsSpacecraftInSphericalCoordinates, configMetadata);
        c.lidarBrowseTimeIndex = read(lidarBrowseTimeIndex, configMetadata);
        c.lidarBrowseNoiseIndex = read(lidarBrowseNoiseIndex, configMetadata);
        c.lidarBrowseFileListResourcePath = read(lidarBrowseFileListResourcePath, configMetadata);
        c.lidarBrowseNumberHeaderLines = read(lidarBrowseNumberHeaderLines, configMetadata);
        c.lidarBrowseIsInMeters = read(lidarBrowseIsInMeters, configMetadata);
        c.lidarOffsetScale = read(lidarOffsetScale, configMetadata);
        c.lidarInstrumentName = Instrument.valueOf(""+read(lidarInstrumentName, configMetadata));

//        configs.add(config);
    }

    public List<ViewConfig> getConfigs()
    {
        System.out.println(
                "SmallBodyViewConfigMetadataExporter: getConfigs: configs size is " + configs.size());
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
    //capture spectral instruments here

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




//    c.body = ShapeModelBody.EROS;
//    c.type = BodyType.ASTEROID;
//    c.population = ShapeModelPopulation.NEO;
//    c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//    c.author = ShapeModelType.GASKELL;
//    c.modelLabel = "Gaskell (2008)";
//    c.rootDirOnServer = "/GASKELL/EROS";
//    c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth";
//    c.hasImageMap = true;
//    c.hasStateHistory = true;
//
//    c.imagingInstruments = new ImagingInstrument[] {
//            new ImagingInstrument(
//                    SpectralMode.MONO,
//                    new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS", "/GASKELL/EROS/MSI/gallery"),
//                    ImageType.MSI_IMAGE,
//                    new ImageSource[]{ImageSource.GASKELL_UPDATED, ImageSource.SPICE},
//                    Instrument.MSI
//                    )
//    };
//
//    c.hasLidarData = true;
//    c.hasMapmaker = true;
//
//    c.hasSpectralData = true;
//    c.spectralInstruments=new SpectralInstrument[]{
//            new NIS()
//    };
//
//    c.hasLineamentData = true;
//    c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
//    c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
//    c.imageSearchFilterNames = new String[]{
//            "Filter 1 (550 nm)",
//            "Filter 2 (450 nm)",
//            "Filter 3 (760 nm)",
//            "Filter 4 (950 nm)",
//            "Filter 5 (900 nm)",
//            "Filter 6 (1000 nm)",
//            "Filter 7 (1050 nm)"
//    };
//    c.imageSearchUserDefinedCheckBoxesNames = new String[]{"iofdbl", "cifdbl"};
//    c.imageSearchDefaultMaxSpacecraftDistance = 1000.0;
//    c.imageSearchDefaultMaxResolution = 50.0;
//    c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
//    c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
//    c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//    c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//    c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
//    c.lidarBrowseXYZIndices = new int[]{14, 15, 16};
//    c.lidarBrowseSpacecraftIndices = new int[]{8, 9, 10};
//  c.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
//  c.lidarBrowseTimeIndex = 4;
//  c.lidarBrowseNoiseIndex = 7;
//  c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/NlrFiles.txt";
//  c.lidarBrowseNumberHeaderLines = 2;
//  c.lidarBrowseIsInMeters = true;
//  c.lidarOffsetScale = 0.025;
//  c.lidarInstrumentName = Instrument.NLR;


//
//    @Override
//    public Metadata store()
//    {
//        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
//        result.put(initializedKey, isInitialized());
//        Renderer localRenderer = SbmtView.this.getRenderer();
//        if (localRenderer != null) {
//            RenderPanel panel = (RenderPanel) localRenderer.getRenderWindowPanel();
//            vtkCamera camera = panel.getActiveCamera();
//            result.put(positionKey, camera.GetPosition());
//            result.put(upKey, camera.GetViewUp());
//        }
//        return result;
//    }
//
//    @Override
//    public void retrieve(Metadata state)
//    {
//        if (state.get(initializedKey)) {
//            initialize();
//            Renderer localRenderer = SbmtView.this.getRenderer();
//            if (localRenderer != null)
//            {
//                RenderPanel panel = (RenderPanel) localRenderer.getRenderWindowPanel();
//                vtkCamera camera = panel.getActiveCamera();
//                camera.SetPosition(state.get(positionKey));
//                camera.SetViewUp(state.get(upKey));
//                panel.resetCameraClippingRange();
//                panel.Render();
//            }
//        }
//    }

}
