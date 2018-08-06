package edu.jhuapl.sbmt.client;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.Serializers;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.sbmt.model.image.Instrument;

public class SmallBodyViewConfigMetadataExporter implements MetadataManager
{
    private List<ViewConfig> configs;

    public SmallBodyViewConfigMetadataExporter(List<ViewConfig> configs)
    {
        this.configs = configs;
    }

    public void write(File file) throws IOException
    {
        Serializers.serialize("Test", store(), file);
    }

    @Override
    public Metadata store()
    {
        List<ViewConfig> builtInConfigs = configs;
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        for (ViewConfig config : builtInConfigs)
        {
            SmallBodyViewConfig c = (SmallBodyViewConfig)config;
            Key<SettableMetadata> metadata = Key.of(config.getUniqueName());
            SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
            configMetadata.put(body, c.body);
            configMetadata.put(type, c.type);


            result.put(metadata, configMetadata);
        }
        return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
        // TODO Auto-generated method stub

    }

    final Key<ShapeModelBody> body = Key.of("body");
    final Key<BodyType> type = Key.of("type");
    final Key<ShapeModelPopulation> population = Key.of("population");
    final Key<ShapeModelDataUsed> dataUsed = Key.of("dataUsed");
    final Key<ShapeModelType> author = Key.of("author");
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

    final Key<Date> imageSearchDefaultStartDate = Key.of("imageSearchDefaultStartDate");
    final Key<Date> imageSearchDefaultEndDate = Key.of("imageSearchDefaultEndDate");
    final Key<String[]> imageSearchFilterNames = Key.of("imageSearchFilterNames");
    final Key<String[]> imageSearchUserDefinedCheckBoxesNames = Key.of("imageSearchUserDefinedCheckBoxesNames");
    final Key<Double> imageSearchDefaultMaxSpacecraftDistance = Key.of("imageSearchDefaultMaxSpacecraftDistance");
    final Key<Double> imageSearchDefaultMaxResolution = Key.of("imageSearchDefaultMaxResolution");

    final Key<Date> lidarSearchDefaultStartDate = Key.of("lidarSearchDefaultStartDate");
    final Key<Date> lidarSearchDefaultEndDate = Key.of("lidarSearchDefaultEndDate");

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
    final Key<Instrument> lidarInstrumentName = Key.of("lidarInstrumentName");




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
