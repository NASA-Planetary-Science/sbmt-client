package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.Date;
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
import edu.jhuapl.sbmt.client.BodyViewConfig;
import edu.jhuapl.sbmt.client.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SpectralMode;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.SessionConfiguration;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.OREXSpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.model.bennu.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.image.BasicImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;

public class BennuConfigs extends BodyViewConfig implements ISmallBodyViewConfig
{

	public BennuConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize()
    {
        List<ViewConfig> configArray = new ArrayList<ViewConfig>();
        BennuConfigs c = new BennuConfigs();

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, test spherical version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("osirisrex", ShapeModelDataUsed.IMAGE_BASED).build();
            BasicImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.SAMCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                samCam = BasicImagingInstrument.of(builder.build());
            }

            c = new BennuConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.BLENDER;
            c.rootDirOnServer = "/earth/osirisrex";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
            c.hasImageMap = true;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    // new Vis(ShapeModelBody.PHOBOS)
                    mapCam,
                    polyCam,
                    samCam,
// TODO when samCam is handled for sbmt1dev (see above), uncomment the next line to add it to the panel.
//                        samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {

                    new OTES(),
                    new OVIRS()
            };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
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
            // 2017-12-21: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, OREX WGS84 version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();

            // Set up shape model -- one will suffice. Note the "orex" here must be kept
            // exactly as it is; that is what the directory is named in the data area.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("orex", ShapeModelDataUsed.WGS84).build();
            BasicImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.SAMCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                samCam = BasicImagingInstrument.of(builder.build());
            }

            c = new BennuConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.OREX;
            c.rootDirOnServer = "/earth/orex";
//            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    // new Vis(ShapeModelBody.PHOBOS)
                    mapCam,
                    polyCam,
                    samCam,
                    // TODO when samCam is handled for sbmt1dev (see above), uncomment the next line
                    // to add it to the panel.
                    // samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
            // TODO make hierarchical search work sbmt1dev-style.
            // c.imageSearchFilterNames = new String[]{
            // EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
            // };
            // c.imageSearchUserDefinedCheckBoxesNames = new String[]{
            // EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
            // };
//            c.hasHierarchicalImageSearch = true;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = true;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", "/earth/osirisrex/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", "/earth/osirisrex/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", "/earth/osirisrex/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", "/earth/osirisrex/ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = "/earth/osirisrex/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            configArray.add(c);
        }


        c = new BennuConfigs();
        c.body = ShapeModelBody.RQ36;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.NOLAN;
        c.modelLabel = "Nolan et al. (2013)";
        c.rootDirOnServer = "/bennu/nolan";
        c.shapeModelFileExtension = ".obj";

        c.hasStateHistory = true;
        c.timeHistoryFile = "/bennu/nolan/history/timeHistory.bth";

        c.setResolution(ImmutableList.of(2692));
        c.density = 1260;
        c.useMinimumReferencePotential = true;
        c.rotationRate = 0.00040613;
        configArray.add(c);

//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = ShapeModelType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.ENHANCED;
//            c.author = ShapeModelAuthor.GASKELL;
//            c.version = "V2";
//            c.rootDirOnServer = "/GASKELL/RQ36";
//            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
//            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
//            c.hasLidarData = true;
//            c.hasMapmaker = true;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
//            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36/OLA/cubes");
//            c.lidarBrowseXYZIndices = new int[]{96, 104, 112};
//            c.lidarBrowseSpacecraftIndices = new int[]{144, 152, 160};
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 18;
//            c.lidarBrowseNoiseIndex = -1;
//            c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/OlaLidarFiles.txt";
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 168;
//            c.lidarOffsetScale = 0.0005;
//            c.lidarInstrumentName = Instrument.OLA;
//            configArray.add(c);
//        }

        // PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/RQ36_V3";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.hasMapmaker = true;
            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/POLYCAM", "RQ36_POLY"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/POLYCAM", true),
                            ImageType.POLYCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/MAPCAM", "RQ36_MAP"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/MAPCAM"),
                            ImageType.MAPCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

//            c.hasSpectralData = true;
//            c.spectralInstruments=new SpectralInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };

            c.density = 1.0;
            c.useMinimumReferencePotential = false;
            c.rotationRate = 0.000407026411379;
            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            // c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/cubes");
            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;
            c.lidarInstrumentName = Instrument.OLA;
            configArray.add(c);

            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            // default ideal data

//            c.lidarSearchDataSourceMap.put("Default","/GASKELL/RQ36_V3/OLA/trees/with_range2/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", "/bennu/bennu-simulated-v4/ola/search/hypertree/dataSource.lidar");

            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
            // noisy data
            c.lidarSearchDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

            c.hasStateHistory = true;
            c.timeHistoryFile = "/GASKELL/RQ36_V3/history/timeHistory.bth";
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V4";
            c.rootDirOnServer = "/bennu/bennu-simulated-v4";
            c.shapeModelFileNames = prepend(c.rootDirOnServer + "/shape", "shape0.obj.gz", "shape1.vtk.gz", "shape2.vtk.gz", "shape3.vtk.gz", "shape4.vtk.gz");
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = false;
            }
            c.hasMapmaker = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "RQ36V4_POLY", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "RQ36V4_MAP", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.MAPCAM
                            )
            };

//            c.hasSpectralData = true;
//            c.spectralInstruments=new SpectralInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
            c.density = 1.26;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.0004061303295118512;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V4/OLA/trees/default/tree/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/hypertree/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseFileListResourcePath =  c.rootDirOnServer + "/ola/browse/default/fileList.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//            c.dtmSearchDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/search/hypertree/dataSource.lidar");

//            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
//                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
//            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//            }
            configArray.add(c);
        }

//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181109b;
//            c.modelLabel = "ALTWG-SPC-v20181109b";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181109b";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = false;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181115;
//            c.modelLabel = "ALTWG-SPC-v20181115"; // NOTE: labeled SPC, but this is a Palmer model.
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181115";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = false;
//            }
//
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[] { ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181116;
//            c.modelLabel = "ALTWG-SPC-v20181116";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181116";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
////            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181123b;
//            c.modelLabel = "ALTWG-SPC-v20181123b";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181123b";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181202;
//            c.modelLabel = "ALTWG-SPC-v20181202";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181202";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=false; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181206;
//            c.modelLabel = "ALTWG-SPC-v20181206";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181206";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=false; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }


//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181217;
//            c.modelLabel = "ALTWG-SPC-v20181217";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181217";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//
//            c.hasImageMap = true;
//            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };
//
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }

//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181217_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181217_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", c.rootDirOnServer + "/navcam/gallery"),
//                            ImageType.NAVCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.NAVCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//
//            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//            specIO.setPathString(c.spectrumMetadataFile);
//            c.hierarchicalSpectraSearchSpecification = specIO;
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new BennuConfigs();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181227;
//            c.modelLabel = "ALTWG-SPC-v20181227";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181227";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//
//            c.hasImageMap = true;
//            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };
//
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181227_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20181227_navcam", "bennu_altwgspcv20181227_navcam", c.rootDirOnServer + "/navcam/gallery"),
//                            ImageType.NAVCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.NAVCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//
//            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//            specIO.setPathString(c.spectrumMetadataFile);
//            c.hierarchicalSpectraSearchSpecification = specIO;
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190105;
            c.modelLabel = "ALTWG-SPC-v20190105";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190105";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190105_polycam", "bennu_altwgspcv20190105_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190105_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190105_mapcam", "bennu_altwgspcv20190105_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190105_navcam", "bennu_altwgspcv20190105_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190114;
            c.modelLabel = "ALTWG-SPC-v20190114";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190114";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190114_polycam", "bennu_altwgspcv20190114_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190114_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190114_mapcam", "bennu_altwgspcv20190114_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190114_navcam", "bennu_altwgspcv20190114_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190114/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190117;
            c.modelLabel = "ALTWG-SPC-v20190117";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190117";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190117_polycam", "bennu_altwgspcv20190117_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190117_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190117_mapcam", "bennu_altwgspcv20190117_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190117_navcam", "bennu_altwgspcv20190117_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190117/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190121;
            c.modelLabel = "ALTWG-SPC-v20190121";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190121";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190121_navcam", "bennu_altwgspcv20190121_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190121/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207a;
            c.modelLabel = "ALTWG-SPC-v20190207a";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207a";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207a_polycam", "bennu_altwgspcv20190207a_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207a_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190207a_mapcam", "bennu_altwgspcv20190207a_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190207a_navcam", "bennu_altwgspcv20190207a_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207a/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207b;
            c.modelLabel = "ALTWG-SPC-v20190207b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207b";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207b_polycam", "bennu_altwgspcv20190207b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207b_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190207b_mapcam", "bennu_altwgspcv20190207b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190207b_navcam", "bennu_altwgspcv20190207b_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207b/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190414;
            c.modelLabel = "ALTWG-SPC-v20190414";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190414";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190414_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190414_navcam", "bennu_altwgspcv20190414_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190414/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

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
