package edu.jhuapl.sbmt.client.configs;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SpectralMode;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.SessionConfiguration;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.image.BasicImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.Hayabusa2SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;

public class RyuguConfigs extends SmallBodyViewConfig
{

	public RyuguConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(List<ViewConfig> configArray)
    {
        RyuguConfigs c = new RyuguConfigs();
        c = new RyuguConfigs();

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, Hayabusa2 WGS84 version
            // :

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();


            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("hayabusa2", ShapeModelDataUsed.WGS84).build();
//            BasicImagingInstrument mapCam;
//            {
//                // Set up images.
//                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
//                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
//                        Instrument.MAPCAM,
//                        SpectralMode.MONO,
//                        queryBase,
//                        new ImageSource[] { ImageSource.SPICE },
//                        fileLocator,
//                        ImageType.MAPCAM_IMAGE);
//
//                // Put it all together in a session.
//                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
//                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
//                mapCam = BasicImagingInstrument.of(builder.build());
//            }
//            BasicImagingInstrument polyCam;
//            {
//                // Set up images.
//                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
//                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
//                        Instrument.POLYCAM,
//                        SpectralMode.MONO,
//                        queryBase,
//                        new ImageSource[] { ImageSource.SPICE },
//                        fileLocator,
//                        ImageType.POLYCAM_IMAGE);
//
//                // Put it all together in a session.
//                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
//                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
//                polyCam = BasicImagingInstrument.of(builder.build());
//            }
    //TODO handle SAMCAM sbmt1dev-style. Add and handle the ImageType for it, then uncomment this block and the line below.
    //        BasicImagingInstrument samCam;
    //        {
    //            // Set up images.
    //            SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
    //            QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
    //            Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
    //                    Instrument.SAMCAM,
    //                    SpectralMode.MONO,
    //                    queryBase,
    //                    new ImageSource[] { ImageSource.SPICE },
    //                    fileLocator,
    //                    ImageType.SAMCAM_IMAGE);
    //
    //            // Put it all together in a session.
    //            Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
    //            builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
    //            samCam = BasicImagingInstrument.of(builder.build());
    //        }

          BasicImagingInstrument tir;
          {
              // Set up images.
              SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
              QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
              Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                      Instrument.TIR,
                      SpectralMode.MONO,
                      queryBase,
                      new ImageSource[] { ImageSource.SPICE },
                      fileLocator,
                      ImageType.TIR_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                tir = BasicImagingInstrument.of(builder.build());
            }

            c = new RyuguConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.JAXA_SFM_v20180627;
            c.modelLabel = "Haybusa2-testing";
            c.rootDirOnServer = "/earth/hayabusa2";
//            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasImageMap = true;
            c.hasColoringData = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    //                       // new Vis(ShapeModelBody.PHOBOS)
                    //                        mapCam,
                    //                        polyCam,
                    // TODO when samCam is handled for sbmt1dev (see above), uncomment the next line
                    // to add it to the panel.
                    // samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

//                c.hasSpectralData=true;
//                c.spectralInstruments=new SpectralInstrument[] {
//                        new OTES()
//                };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, 11, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime();
            // TODO make hierarchical search work sbmt1dev-style.
//            c.imageSearchFilterNames = new String[]{
//                    EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
//            };
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                    EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
//            };
//            c.hasHierarchicalImageSearch = true;
//            c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            configArray.add(c);

            c.hasLidarData = false;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//          c.lidarSearchDataSourceMap.put("Hayabusa2","/ryugu/shared/lidar/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/earth/hayabusa2/laser/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/earth/hayabusa2/laser/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Truth", ShapeModelDataUsed.IMAGE_BASED).build();

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.TRUTH;
            c.modelLabel = "H2 Simulated Truth";
            c.rootDirOnServer = "/ryugu/truth";
            c.shapeModelFileExtension = ".obj";

            c.setResolution(ImmutableList.of("Low (54504 plates)", "High (5450420 plates)"), ImmutableList.of(54504, 5450420));

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/truth/history/timeHistory.bth";

            // This version would enable image search but this seems to hang, possibly
            // because of the very high resolution of the model.
            // Re-enable this if/when that issue is addressed.
//            QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/imaging", "ryugu", "/ryugu/truth/imaging/images/gallery");
//            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.IMAGING_DATA, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_TRUTH_IMAGE);
            SBMTFileLocator fileLocatorTir = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
//            QueryBase queryBaseTir = new FixedListQuery(fileLocatorTir.get(SBMTFileLocator.TOP_PATH).getLocation("") + "/simulated", fileLocatorTir.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            QueryBase queryBaseTir = new GenericPhpQuery("/ryugu/truth/tir", "ryugu_nasa002_tir", "/ryugu/truth/tir/gallery");
            ImagingInstrument tir = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, queryBaseTir, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/onc", "ryugu_sim", "/ryugu/truth/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_TRUTH_IMAGE);

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
//                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Gaskell", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/gaskell/onc", "ryugu_sim", "/ryugu/gaskell/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL }, ImageType.ONC_IMAGE);

            SBMTFileLocator fileLocatorTir = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
//            QueryBase queryBaseTir = new FixedListQuery(fileLocatorTir.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocatorTir.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            QueryBase queryBaseTir = new GenericPhpQuery("/ryugu/gaskell/tir", "ryugu_nasa002_tir", "/ryugu/gaskell/tir/gallery");
            ImagingInstrument tir = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, queryBaseTir, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "H2 Simulated SPC";
            c.rootDirOnServer = "/ryugu/gaskell";
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/gaskell/history/timeHistory.bth"; // TODO move this to shared/timeHistory.bth

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
//                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/laser/search/test_hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180627", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180627/onc", "jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180627/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180627/tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180627/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180627;
            c.modelLabel = "JAXA-SFM-v20180627";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180627";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180627/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180714", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180714/onc", "ryugu_jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180714/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180627/tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180714/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180714/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180714;
            c.modelLabel = "JAXA-SFM-v20180714";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180714";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180714/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180725_2", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_jaxasfmv201807252", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180725-2/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180725-2/tir", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180725-2/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180725_2;
            c.modelLabel = "JAXA-SFM-v20180725_2";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180725-2";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180725-2/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180804", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180804/onc", "ryugu_jaxasfmv20180804", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180804/onc/gallery");
            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180804/onc",
            // "/ryugu/jaxa-sfm-v20180804/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180804/tir", "/ryugu/jaxa-sfm-v20180804/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180804/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180804/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180804;
            c.modelLabel = "JAXA-SFM-v20180804";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180804";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180804/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180705", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180705/onc", "ryugu_jaxaspcv20180705", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180705/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180705/tir", "/ryugu/jaxa-spc-v20180705/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180705/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180705/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180705;
            c.modelLabel = "JAXA-SPC-v20180705";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180705";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180705/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180717", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180717/onc", "ryugu_jaxaspcv20180717", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180717/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180717/tir", "/ryugu/jaxa-spc-v20180717/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180717/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180717/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180717;
            c.modelLabel = "JAXA-SPC-v20180717";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180717";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180717/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180719_2", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_jaxaspcv201807192", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180719-2/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180719-2/tir", "/ryugu/jaxa-spc-v20180719-2/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180719-2/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180719-2/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180719_2;
            c.modelLabel = "JAXA-SPC-v20180719_2";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180719-2";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180719-2/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180731", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180731/onc",
            // "/ryugu/jaxa-spc-v20180731/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180731/onc", "ryugu_jaxaspcv20180731", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180731/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180731/tir", "/ryugu/jaxa-spc-v20180731/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180731/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180731/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180731;
            c.modelLabel = "JAXA-SPC-v20180731";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180731";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180731/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180810", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180810/onc",
            // "/ryugu/jaxa-spc-v20180810/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180810/onc", "ryugu_jaxaspcv20180810", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180810/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180810/tir", "/ryugu/jaxa-spc-v20180810/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180810/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180810/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180810;
            c.modelLabel = "JAXA-SPC-v20180810";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180810";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180810/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180816", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180816/onc",
            // "/ryugu/jaxa-spc-v20180816/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180816/onc", "ryugu_jaxaspcv20180816", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180816/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180816/tir", "/ryugu/jaxa-spc-v20180816/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180816/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180816/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180816;
            c.modelLabel = "JAXA-SPC-v20180816";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180816";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180816/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180829", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180829/onc",
            // "/ryugu/jaxa-spc-v20180829/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180829/onc", "ryugu_jaxaspcv20180829", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180829/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180829/tir", "/ryugu/jaxa-spc-v20180829/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180829/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180829/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180829;
            c.modelLabel = "JAXA-SPC-v20180829";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180829";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180829/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20181014", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20181014/onc",
            // "/ryugu/jaxa-spc-v20181014/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20181014/onc", "ryugu_jaxaspcv20181014", "ryugu_nasa005", "/ryugu/jaxa-spc-v20181014/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20181014/tir", "/ryugu/jaxa-spc-v20181014/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20181014/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20181014/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20181014;
            c.modelLabel = "JAXA-SPC-v20181014";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20181014";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20181014/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("NIRS3", c.rootDirOnServer + "/nirs3/l2c/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            Hayabusa2SpectrumInstrumentMetadataIO specIO = new Hayabusa2SpectrumInstrumentMetadataIO("HAYABUSA2");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-001", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/nasa-001/onc", "ryugu_flight", "/ryugu/nasa-001/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL }, ImageType.ONC_IMAGE);

            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-001/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-001/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_001;
            c.modelLabel = "NASA-001";
            c.rootDirOnServer = "/ryugu/nasa-001";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            // c.hasStateHistory = true;
//            c.timeHistoryFile = "/ryugu/nasa-001/history/timeHistory.bth"; // TODO move this to shared/timeHistory.bth

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam // , tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/laser/search/test_hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-002", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-002/onc", "ryugu_nasa002", "ryugu_nasa002", "/ryugu/nasa-002/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-002/tir", "/ryugu/nasa-002/tir/gallery", false);            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-001/tir", "", "ryugu_nasa003_tir", "/ryugu/nasa-001/tir/gallery");
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-002/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-002/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_002;
            c.modelLabel = "NASA-002";
            c.rootDirOnServer = "/ryugu/nasa-002";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-002/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-003", ShapeModelDataUsed.IMAGE_BASED).build();

            // NOTE THE FOLLOWING LINE IS NOT A TYPO: THIRD ARGUMENT SHOULD BE ryugu_nasa002, not ryugu_nasa003.
            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-003/onc", "ryugu_nasa003", "ryugu_nasa002", "/ryugu/nasa-003/onc/gallery");
            // QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-003/onc",
            // "/ryugu/nasa-003/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-003/tir", "/ryugu/nasa-003/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-003/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-003/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_003;
            c.modelLabel = "NASA-003";
            c.rootDirOnServer = "/ryugu/nasa-003";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-003/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-004", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-004/onc", "ryugu_nasa004", "ryugu_nasa005", "/ryugu/nasa-004/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-004/onc", "/ryugu/nasa-004/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-004/tir", "/ryugu/nasa-004/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-004/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-004/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_004;
            c.modelLabel = "NASA-004";
            c.rootDirOnServer = "/ryugu/nasa-004";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-004/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-005", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-005/onc", "ryugu_nasa005", "ryugu_nasa005", "/ryugu/nasa-005/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-005/onc", "/ryugu/nasa-005/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-005/tir", "/ryugu/nasa-005/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-005/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-005/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_005;
            c.modelLabel = "NASA-005";
            c.rootDirOnServer = "/ryugu/nasa-005";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-005/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-006", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-006/onc", "ryugu_nasa006", "ryugu_nasa005", "/ryugu/nasa-006/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-006/onc", "/ryugu/nasa-006/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-006/tir", "/ryugu/nasa-006/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-006/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-006/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new RyuguConfigs();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_006;
            c.modelLabel = "NASA-006";
            c.rootDirOnServer = "/ryugu/nasa-006";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-006/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022867; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            configArray.add(c);
        }
    }

	 private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, ImageSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, QueryBase queryBase, ImageSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTFileLocator fileLocator, SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, QueryBase queryBase, ImageSource[] imageSources, ImageType imageType)
    {
        Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(instrument, SpectralMode.MONO, queryBase, imageSources, fileLocator, imageType);

        // Put it all together in a session.
        Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
        builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
        return BasicImagingInstrument.of(builder.build());
    }


	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }

    @Override
    public Instrument getLidarInstrument()
    {
        // TODO Auto-generated method stub
        return lidarInstrumentName;
    }

    public boolean hasHypertreeLidarSearch()
    {
        return hasHypertreeBasedLidarSearch;
    }

    public SpectraHierarchicalSearchSpecification<?> getHierarchicalSpectraSearchSpecification()
    {
        return hierarchicalSpectraSearchSpecification;
    }

}
