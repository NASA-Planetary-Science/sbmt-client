package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.BasemapImageConfig;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.model.BasicImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.image.model.SessionConfiguration;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.model.bennu.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class BennuConfigs extends SmallBodyViewConfig
{
    private static final Mission[] OREXClients = new Mission[] { //
            Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST, Mission.OSIRIS_REX_DEPLOY, //
            Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final Mission[] ClientsWithOREXModels = new Mission[] { //
            Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, //
            Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST, Mission.OSIRIS_REX_DEPLOY, //
            Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final Mission[] AllBennuClients = new Mission[] { //
            Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, //
            Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, //
            Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST, Mission.OSIRIS_REX_DEPLOY, //
            Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final Mission[] InternalOnly = new Mission[] {
    		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL
    };

    private static final Mission[] PublicOnly = new Mission[] {
    		Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE
    };

	private static void setupFeatures(BennuConfigs c)
	{
		c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));
        c.addFeatureConfig(SpectrumInstrumentConfig.class, new SpectrumInstrumentConfig(c));
        c.addFeatureConfig(LidarInstrumentConfig.class, new LidarInstrumentConfig(c));
        c.addFeatureConfig(StateHistoryConfig.class, new StateHistoryConfig(c));
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));


        FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig(c);
	}


	List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> instrumentSearchSpecs = new ArrayList<SpectrumInstrumentMetadata<SpectrumSearchSpec>>();

	public BennuConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

		this.defaultForMissions = DefaultForNoMissions;

		SpectrumSearchSpec otesL2 = new SpectrumSearchSpec("OTES L2 Calibrated Radiance", "/bennu/shared/otes/l2", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wave Number (1/cm)", "Radiance", "OTES L2 Calibrated Radiance");
		SpectrumSearchSpec otesL3 = new SpectrumSearchSpec("OTES L3 Spot Emissivity", "/bennu/shared/otes/l3", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wave Number (1/cm)", "Emissivity", "OTES L3 Spot Emissivity");
		List<SpectrumSearchSpec> otesSpecs = new ArrayList<SpectrumSearchSpec>();
		otesSpecs.add(otesL2);
		otesSpecs.add(otesL3);

		SpectrumSearchSpec ovirsSA16 = new SpectrumSearchSpec("OVIRS SA-16 Photometrically Corrected SPOT Reflectance Factor (REFF)", "/bennu/shared/ovirs/l3/SA16l3escireff", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "REFF", "OVIRS L3 SA-16 Photometrically Corrected SPOT Reflectance Factor (REFF)");
		SpectrumSearchSpec ovirsSA27 = new SpectrumSearchSpec("OVIRS SA-27 SPOT I/F", "/bennu/shared/ovirs/l3/SA27l3csci", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "I/F", "OVIRS L3 SA-27 SPOT I/F");
		SpectrumSearchSpec ovirsSA29 = new SpectrumSearchSpec("OVIRS SA-29 Photometrically corrected SPOT I/F, aka RADF", "/bennu/shared/ovirs/l3/SA29l3esciradf", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "RADF", "OVIRS L3 SA-29 Photometrically corrected SPOT I/F, aka RADF");
		List<SpectrumSearchSpec> ovirsSpecs = new ArrayList<SpectrumSearchSpec>();
		ovirsSpecs.add(ovirsSA16);
		ovirsSpecs.add(ovirsSA27);
		ovirsSpecs.add(ovirsSA29);

		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("OTES", otesSpecs));
		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("OVIRS", ovirsSpecs));

        presentInMissions = ClientsWithOREXModels;
	}


	public static void initialize(List<IBodyViewConfig> configArray, boolean publicOnly)
    {
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
            ImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

                DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                FixedListDataQuery query = new FixedListDataQuery(metadata);

                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralImageMode.MONO,
                        query,
                        new PointingSource[] { PointingSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            ImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

                DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                FixedListDataQuery query = new FixedListDataQuery(metadata);

                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralImageMode.MONO,
                        query,
                        new PointingSource[] { PointingSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            ImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

                DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                FixedListDataQuery query = new FixedListDataQuery(metadata);

                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralImageMode.MONO,
                        query,
                        new PointingSource[] { PointingSource.SPICE },
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
//            c.hasImageMap = true;
//            c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
            setupFeatures(c);

            StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
            stateHistoryConfig.hasStateHistory = true;
            stateHistoryConfig.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    mapCam,
                    polyCam,
                    samCam
            );

            spectrumConfig.hasSpectralData = true;
            spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
            spectrumConfig.spectralInstruments.add(new OTES());
            spectrumConfig.spectralInstruments.add(new OVIRS());

            c.hasMapmaker = false;
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            imagingConfig.imageSearchDefaultMaxResolution = 300.0;
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

//            // Set up shape model -- one will suffice. Note the "orex" here must be kept
//            // exactly as it is; that is what the directory is named in the data area.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("orex", ShapeModelDataUsed.WGS84).build();

            c = new BennuConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.OREX;
            c.rootDirOnServer = "/earth/orex";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
//            c.hasImageMap = true;
//            c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
            setupFeatures(c);
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);


            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            imagingConfig.imageSearchDefaultMaxResolution = 300.0;

            SBMTFileLocator polyCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fits", ".INFO", null, ".jpeg");
//            QueryBase polyCamQueryBase = new FixedListQuery(polyCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), polyCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

            DataQuerySourcesMetadata polyCamMetadata = DataQuerySourcesMetadata.of(polyCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, polyCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            FixedListDataQuery polyCamQuery = new FixedListDataQuery(polyCamMetadata);

            SBMTFileLocator mapCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fits", ".INFO", null, ".jpeg");
//            QueryBase mapCamQueryBase = new FixedListQuery(mapCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), mapCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

            DataQuerySourcesMetadata mapCamMetadata = DataQuerySourcesMetadata.of(mapCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, mapCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            FixedListDataQuery mapCamQuery = new FixedListDataQuery(mapCamMetadata);

            SBMTFileLocator samCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
//            QueryBase samCamQueryBase = new FixedListQuery(samCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), samCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

            DataQuerySourcesMetadata samCamMetadata = DataQuerySourcesMetadata.of(samCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, samCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            FixedListDataQuery samCamQuery = new FixedListDataQuery(samCamMetadata);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		new ImagingInstrument(
                            SpectralImageMode.MONO,
                            polyCamQuery,
                            ImageType.POLYCAM_EARTH_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL,PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            mapCamQuery,
                            ImageType.MAPCAM_EARTH_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL,PointingSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            samCamQuery,
                            ImageType.SAMCAM_EARTH_IMAGE,
                            new PointingSource[]{PointingSource.SPICE},
                            Instrument.SAMCAM

                    		)
                    );

            StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
            stateHistoryConfig.hasStateHistory = true;
            stateHistoryConfig.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.hasMapmaker = false;
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();

            c.setSpectrumParameters();
            //override
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            spectrumConfig.spectraSearchDataSourceMap = new LinkedHashMap<>();
            spectrumConfig.spectraSearchDataSourceMap.put("OTES_L2", "/earth/osirisrex/otes/l2/hypertree/dataSource.spectra");
            spectrumConfig.spectraSearchDataSourceMap.put("OTES_L3", "/earth/osirisrex/otes/l3/hypertree/dataSource.spectra");
            spectrumConfig.spectraSearchDataSourceMap.put("OVIRS_IF", "/earth/osirisrex/ovirs/l3/if/hypertree/dataSource.spectra");
            spectrumConfig.spectraSearchDataSourceMap.put("OVIRS_REF", "/earth/osirisrex/ovirs/l3/reff/hypertree/dataSource.spectra");

            c.presentInMissions = new Mission[] {};

//            configArray.add(c);
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

        setupFeatures(c);

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = "/bennu/nolan/history/timeHistory.bth";

        c.setResolution(ImmutableList.of(2692));
        c.density = 1.260;
        c.useMinimumReferencePotential = true;
        c.rotationRate = 0.00040613;

        c.presentInMissions = AllBennuClients;

        configArray.add(c);

        // PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.useMinimumReferencePotential = false;

            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/RQ36_V3";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
            c.density = 1.0;
            c.rotationRate = 0.000407026411379;
            c.presentInMissions = AllBennuClients;

            setupFeatures(c);
            c.setBodyParameters();
            c.hasMapmaker = true;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of("/GASKELL/RQ36_V3/POLYCAM", "", "", "RQ36_POLY", null);
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of("/GASKELL/RQ36_V3/MAPCAM", "", "", "RQ36_MAP", null);

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery("/GASKELL/RQ36_V3/POLYCAM", "RQ36_POLY"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/POLYCAM", true),
                            ImageType.POLYCAM_V3_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery("/GASKELL/RQ36_V3/MAPCAM", "RQ36_MAP"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/MAPCAM"),
                            ImageType.MAPCAM_V3_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );

            lidarConfig.hasLidarData = true;
            lidarConfig.lidarInstrumentName = Instrument.OLA;
            lidarConfig.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
            lidarConfig.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            lidarConfig.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt";

            // default ideal data

            lidarConfig.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
            // noisy data
            lidarConfig.lidarSearchDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

            lidarConfig.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            lidarConfig.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            lidarConfig.lidarBrowseTimeIndex = 26;
            lidarConfig.lidarBrowseNoiseIndex = 62;
            lidarConfig.lidarBrowseOutgoingIntensityIndex = 98;
            lidarConfig.lidarBrowseReceivedIntensityIndex = 106;
            lidarConfig.lidarBrowseIntensityEnabled = true;
            lidarConfig.lidarBrowseNumberHeaderLines = 0;
            lidarConfig.lidarBrowseIsInMeters = true;
            lidarConfig.lidarBrowseIsBinary = true;
            lidarConfig.lidarBrowseBinaryRecordSize = 186;
            lidarConfig.lidarOffsetScale = 0.0005;

            c.databaseRunInfos = new DBRunInfo[]
            {
        		new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/MAPCAM/imagelist-fullpath.txt", "RQ36_MAP"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/POLYCAM/imagelist-fullpath.txt", "RQ36_POLY"),
            };

            configArray.add(c);


            stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
            stateHistoryConfig.hasStateHistory = true;
            stateHistoryConfig.timeHistoryFile = "/GASKELL/RQ36_V3/history/timeHistory.bth";
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.shapeModelFileExtension = ".vtk";
//            c.hasImageMap = false;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V4";
            c.rootDirOnServer = "/bennu/bennu-simulated-v4";
            c.shapeModelFileNames = prepend(c.rootDirOnServer + "/shape", "shape0.obj.gz", "shape1.vtk.gz", "shape2.vtk.gz", "shape3.vtk.gz", "shape4.vtk.gz");
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.26;
            c.rotationRate = 0.0004061303295118512;
            c.presentInMissions = AllBennuClients;
            if(Configuration.isMac()) c.hasBigmap = false;  // Right now bigmap only works on Macs

            c.hasMapmaker = false;
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "RQ36V4_POLY", "RQ36V4_POLY", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "RQ36V4_MAP", "RQ36V4_MAP", c.rootDirOnServer + "/mapcam/gallery");


            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "RQ36V4_POLY", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_V4_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL},
                            Instrument.POLYCAM,
                            0.0,
                            "X"
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "RQ36V4_MAP", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_V4_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL},
                            Instrument.MAPCAM,
                            0.0,
                            "X"
                            )
            );

            lidarConfig.hasLidarData = true;
            lidarConfig.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            lidarConfig.lidarInstrumentName = Instrument.OLA;
            lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
            lidarConfig.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            lidarConfig.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V4/OLA/trees/default/tree/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt";

            lidarConfig.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            lidarConfig.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            lidarConfig.lidarBrowseTimeIndex = 26;
            lidarConfig.lidarBrowseNoiseIndex = 62;
            lidarConfig.lidarBrowseOutgoingIntensityIndex = 98;
            lidarConfig.lidarBrowseReceivedIntensityIndex = 106;
            lidarConfig.lidarBrowseIntensityEnabled = true;
            lidarConfig.lidarBrowseNumberHeaderLines = 0;
            lidarConfig.lidarBrowseIsInMeters = true;
            lidarConfig.lidarBrowseIsBinary = true;
            lidarConfig.lidarBrowseBinaryRecordSize = 186;
            lidarConfig.lidarOffsetScale = 0.0005;

            c.generateStateHistoryParameters();

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/mapcam/imagelist-fullpath.txt", "RQ36V4_MAP"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/polycam/imagelist-fullpath.txt", "RQ36V4_POLY"),
            };


            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181109b;
            c.modelLabel = "ALTWG-SPC-v20181109b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181109b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList( c.generatePolycamInstrument("bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", true, false, true),
            												 c.generateMapcamInstrument("bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", true, false, true)
            );
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setSpectrumParameters();
            c.setLidarParameters(true);
        	c.presentInMissions  = new Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181115;
            c.modelLabel = "ALTWG-SPC-v20181115"; // NOTE: labeled SPC, but this is a Palmer model.
            c.rootDirOnServer = "/bennu/altwg-spc-v20181115";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181115_polycam", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181115_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new PointingSource[] { PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setSpectrumParameters();
            c.setLidarParameters(true);

            c.hasMapmaker = false;
        	c.presentInMissions = new Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181116;
            c.modelLabel = "ALTWG-SPC-v20181116";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181116";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;
            c.setSpectrumParameters();

            c.setLidarParameters(true);
			c.presentInMissions = new Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181123b;
            c.modelLabel = "ALTWG-SPC-v20181123b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181123b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();
            c.hasMapmaker = false;
            c.setLidarParameters(true);
			c.presentInMissions = new Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181202;
            c.modelLabel = "ALTWG-SPC-v20181202";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181202";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();

      		c.hasMapmaker = false;

            c.setLidarParameters(false);
			c.presentInMissions = new Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181206;
            c.modelLabel = "ALTWG-SPC-v20181206";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181206";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            DataQuerySourcesMetadata polycamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/polycam", "", "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery");
            DataQuerySourcesMetadata mapcamMetadata =
            		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/mapcam", "", "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery");

            imagingConfig.imagingInstruments = Lists.newArrayList(
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(polycamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new ImageDataQuery(mapcamMetadata),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new PointingSource[]{PointingSource.SPICE},
                            Instrument.MAPCAM
                            )
            );

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();
      		c.hasMapmaker = false;
            c.setLidarParameters(false);
			c.presentInMissions = new Mission[] {};
            configArray.add(c);
        }


        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181217;
            c.modelLabel = "SPC v13";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181217";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            	 c.generatePolycamInstrument("bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", true, false),
				 c.generateMapcamInstrument("bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", true, false),
				 c.generateNavcamInstrument("bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public1217 = (BennuConfigs)c.clone();
	//            public1217.author = ShapeModelType.provide("ALTWG-SPC-v20181217_SPC_v13_PUBLIC");
	            public1217.modelLabel = "SPC v13";
	            imagingConfig = (ImagingInstrumentConfig)public1217.getConfigForClass(ImagingInstrumentConfig.class);
	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public1217.getConfigForClass(LidarInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		public1217.generatePolycamInstrument("bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", true, false, true),	//false = SPC only
	            															public1217.generateMapcamInstrument("bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", true, false, true)
	            );
	            public1217.disableSpectra();
	//            public1217.hasImageMap = false;
	            public1217.presentInMissions = PublicOnly;
	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public1217.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public1217.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public1217.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.orexSearchTimeMap.remove("Recon");
	            lidarConfig.lidarSearchDataSourceMap.remove("Recon");

	        	configArray.add(public1217);
            }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181227;
            c.modelLabel = "SPC v14";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181227";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;


            if(Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20181227_navcam", "bennu_altwgspcv20181227_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public1227 = (BennuConfigs)c.clone();
	//            public1227.author = ShapeModelType.provide("ALTWG-SPC-v20181227_SPC_v14_PUBLIC");
	            public1227.modelLabel = "SPC v14";
	            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		public1227.generatePolycamInstrument("bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", true, false, true),	//false = SPC only
	            														public1227.generateMapcamInstrument("bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", true, false, true)
	            );
	            public1227.disableSpectra();
	//            public1227.hasImageMap = false;
	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public1227.getConfigForClass(LidarInstrumentConfig.class);

	            public1227.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            public1227.presentInMissions = PublicOnly;
	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public1227.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public1227.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public1227.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.orexSearchTimeMap.remove("Recon");
		        lidarConfig.lidarSearchDataSourceMap.remove("Recon");

	        	configArray.add(public1227);
            }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190105;
            c.modelLabel = "ALTWG-SPC-v20190105";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190105";

            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            c.bodyReferencePotential = -0.02654811544296466;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190105_polycam", "bennu_altwgspcv20190105_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190105_mapcam", "bennu_altwgspcv20190105_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190105_navcam", "bennu_altwgspcv20190105_navcam")
            );

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190114;
            c.modelLabel = "ALTWG-SPC-v20190114";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190114";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            c.bodyReferencePotential = -0.02637307554771602;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190114_polycam", "bennu_altwgspcv20190114_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190114_mapcam", "bennu_altwgspcv20190114_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190114_navcam", "bennu_altwgspcv20190114_navcam")
            );

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190117;
            c.modelLabel = "ALTWG-SPC-v20190117";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190117";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02530442113463265;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190117_polycam", "bennu_altwgspcv20190117_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190117_mapcam", "bennu_altwgspcv20190117_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190117_navcam", "bennu_altwgspcv20190117_navcam")
            );

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190121;
            c.modelLabel = "SPC v20";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190121";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02513575424405747;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190121_navcam", "bennu_altwgspcv20190121_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public0121 = (BennuConfigs)c.clone();
	//            public0121.author = ShapeModelType.provide("ALTWG-SPC-v20190121_SPC_v20_PUBLIC");
	            public0121.modelLabel = "SPC v20";
	            imagingConfig = (ImagingInstrumentConfig)public0121.getConfigForClass(ImagingInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		public0121.generatePolycamInstrument("bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", true, false, true),	//false = SPC only
	            															public0121.generateMapcamInstrument("bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", true, false, true)
	            );
	            public0121.disableSpectra();
	//            public0121.hasImageMap = true;
	            public0121.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            public0121.presentInMissions = PublicOnly;
	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public0121.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public0121.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public0121.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public0121.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.orexSearchTimeMap.remove("Recon");
	            lidarConfig.lidarSearchDataSourceMap.remove("Recon");

	        	configArray.add(public0121);
        	}

        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207a;
            c.modelLabel = "ALTWG-SPC-v20190207a";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207a";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.0253033332766406;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190207a_polycam", "bennu_altwgspcv20190207a_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190207a_mapcam", "bennu_altwgspcv20190207a_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190207a_navcam", "bennu_altwgspcv20190207a_navcam")
            );

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207b;
            c.modelLabel = "ALTWG-SPC-v20190207b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02528907231151947;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190207b_polycam", "bennu_altwgspcv20190207b_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190207b_mapcam", "bennu_altwgspcv20190207b_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190207b_navcam", "bennu_altwgspcv20190207b_navcam")
            );

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190414;
            c.modelLabel = "SPC v28";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190414";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02520767997203304;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190414_navcam", "bennu_altwgspcv20190414_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_mapcam"),
            	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_polycam"),

            	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_mapcam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_polycam"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_navcam")
            };
            c.presentInMissions = InternalOnly;
            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public0414 = (BennuConfigs)c.clone();
	//            public0414.author = ShapeModelType.provide("ALTWG-SPC-v20190414_SPC_v28_PUBLIC");
	            public0414.modelLabel = "SPC v28";
	            setupFeatures(c);
	            imagingConfig = (ImagingInstrumentConfig)public0414.getConfigForClass(ImagingInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList( public0414.generatePolycamInstrument("bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", true, false, true),	//false = SPC only
	            														public0414.generateMapcamInstrument("bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", true, false, true)
	            );
	            public0414.disableSpectra();
	//            public0414.hasImageMap = true;
	            public0414.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            public0414.baseMapConfigName = "config_public.txt";
	            public0414.presentInMissions = PublicOnly;
	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public0414.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public0414.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public0414.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public0414.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.orexSearchTimeMap.remove("Recon");
	            lidarConfig.lidarSearchDataSourceMap.remove("Recon");

	        	configArray.add(public0414);
            }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPO_v20190612;
            c.modelLabel = "SPO v34";
            c.rootDirOnServer = "/bennu/altwg-spo-v20190612";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517871436774813;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspov20190612_polycam", "bennu_altwgspov20190612_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspov20190612_mapcam", "bennu_altwgspov20190612_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspov20190612_navcam", "bennu_altwgspov20190612_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/navcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public0612 = (BennuConfigs)c.clone();
	//            public0612.author = ShapeModelType.provide("ALTWG-SPC-v20190612_SPC_v34_PUBLIC");
	            public0612.modelLabel = "SPO v34";
	            setupFeatures(c);
	            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		public0612.generatePolycamInstrument("bennu_altwgspov20190612_polycam", "bennu_altwgspov20190612_polycam", true, false, true),
	            														public0612.generateMapcamInstrument("bennu_altwgspov20190612_mapcam", "bennu_altwgspov20190612_mapcam", true, false, true)
	            );
	            public0612.disableSpectra();
	//            public0612.hasImageMap = true;
	            public0612.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            public0612.baseMapConfigName = "config_public.txt";
	            public0612.presentInMissions = PublicOnly;

	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public0612.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public0612.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public0612.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public0612.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.orexSearchTimeMap.remove("Recon");
	            lidarConfig.lidarSearchDataSourceMap.remove("Recon");

	        	configArray.add(public0612);
            }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("ALTWG-SPC-v20190828");
            c.modelLabel = "SPC v42";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190828";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517940647257273;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20190828_polycam", "bennu_altwgspcv20190828_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190828_mapcam", "bennu_altwgspcv20190828_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190828_navcam", "bennu_altwgspcv20190828_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
            {
	            BennuConfigs public0828 = (BennuConfigs)c.clone();
	//            public0828.author = ShapeModelType.provide("ALTWG-SPC-v20190828_SPC_v42_PUBLIC");
	            public0828.modelLabel = "SPC v42";
	            setupFeatures(public0828);
	            imagingConfig = (ImagingInstrumentConfig)public0828.getConfigForClass(ImagingInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		public0828.generatePolycamInstrument("bennu_altwgspcv20190828_polycam", "bennu_altwgspcv20190828_polycam", true, false, true),	//false = SPC only
	            														public0828.generateMapcamInstrument("bennu_altwgspcv20190828_mapcam", "bennu_altwgspcv20190828_mapcam", true, false, true)
	            );
	            public0828.disableSpectra();
	//            public0828.hasImageMap = true;
	            public0828.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            public0828.presentInMissions = PublicOnly;
	            public0828.baseMapConfigName = "config_public.txt";

	            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)public0828.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", public0828.rootDirOnServer + "/ola/browse/fileList_public.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = public0828.rootDirOnServer + "/ola/browse/fileList_public.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", public0828.rootDirOnServer + "/ola/browse/fileList_public.txt");
		        lidarConfig.orexSearchTimeMap.remove("Recon");
		        lidarConfig.lidarSearchDataSourceMap.remove("Recon");
	        	configArray.add(public0828);
            }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("ALTWG-SPC-v20191027");
            c.modelLabel = c.author.name();
            c.rootDirOnServer = "/bennu/altwg-spc-v20191027";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517940647257273;
            c.hasCustomBodyCubeSize = true;
            c.customBodyCubeSize = 0.02; //km

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_altwgspcv20191027_polycam", "bennu_altwgspcv20191027_polycam", true, true),
															 c.generateMapcamInstrument("bennu_altwgspcv20191027_mapcam", "bennu_altwgspcv20191027_mapcam", true, true),
															 c.generateNavcamInstrument("bennu_altwgspcv20191027_navcam", "bennu_altwgspcv20191027_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_navcam")
            };

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v20");
            c.modelLabel = "OLA v20";
            c.rootDirOnServer = "/bennu/ola-v20-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02527683882517149;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_olav20_polycam", "bennu_olav20_polycam", true, true),
															 c.generateMapcamInstrument("bennu_olav20_mapcam", "bennu_olav20_mapcam", true, true),
															 c.generateNavcamInstrument("bennu_olav20_navcam", "bennu_olav20_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            lidarConfig.lidarSearchDataSourceMap.clear();
            lidarConfig.orexSearchTimeMap.clear();
            lidarConfig.orexSearchTimeMap.put("OLAv20", startStop);
            lidarConfig.lidarSearchDataSourceMap.put("OLAv20", c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav20_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav20_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav20_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav20_navcam")
            };

            if (!publicOnly)
            	configArray.add(c);

            //public version
            if (publicOnly)
	        {
	            BennuConfigs publicOLA = (BennuConfigs)c.clone();
	//            publicOLA.author = ShapeModelType.provide("OLA-v20_PUBLIC");
	            publicOLA.modelLabel = "OLA v20";
	            publicOLA.disableSpectra();
	//            publicOLA.hasImageMap = true;
	            publicOLA.presentInMissions = PublicOnly;
	            publicOLA.baseMapConfigName = "config_public.txt";
	            setupFeatures(publicOLA);
	            imagingConfig = (ImagingInstrumentConfig)publicOLA.getConfigForClass(ImagingInstrumentConfig.class);
	            spectrumConfig = (SpectrumInstrumentConfig)publicOLA.getConfigForClass(SpectrumInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		publicOLA.generatePolycamInstrument("bennu_olav20_polycam", "bennu_olav20_polycam", true, true, true),
						 publicOLA.generateMapcamInstrument("bennu_olav20_mapcam", "bennu_olav20_mapcam", true, true, true),
						 publicOLA.generateNavcamInstrument("bennu_olav20_navcam", "bennu_olav20_navcam", true)
	            );
	            spectrumConfig.hasHypertreeBasedSpectraSearch = false;

	            lidarConfig = (LidarInstrumentConfig)publicOLA.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarSearchDataSourceMap.clear();
	            lidarConfig.orexSearchTimeMap.clear();
	            lidarConfig.orexSearchTimeMap.put("OLAv20", startStop);
	            lidarConfig.lidarSearchDataSourceMap.put("OLAv20", publicOLA.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");

            	configArray.add(publicOLA);
	        }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v20-PTM");
            c.modelLabel = "OLA v20 PTM";
            c.rootDirOnServer = "/bennu/ola-v20-ptm";
            c.setResolution( //
                    ImmutableList.of("Medium (217032 plates)", "High (886904 plates)", "Very High (3366134 plates)"),
                    ImmutableList.of(217032, 886904, 3366134));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02527683882517149;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            setupFeatures(c);

            c.setBodyParameters();
            System.out.println("BennuConfigs: initialize: c unique " + c.getUniqueName());

            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_olav20ptm_polycam", "bennu_olav20ptm_polycam", true, true),
            												 c.generateMapcamInstrument("bennu_olav20ptm_mapcam", "bennu_olav20ptm_mapcam", true, true),
            												 c.generateNavcamInstrument("bennu_olav20ptm_navcam", "bennu_olav20ptm_navcam")
            );

            c.generateStateHistoryParameters();

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            lidarConfig.lidarSearchDataSourceMap.clear();
            lidarConfig.orexSearchTimeMap.clear();
            lidarConfig.orexSearchTimeMap.put("OLAv20", startStop);
            lidarConfig.lidarSearchDataSourceMap.put("OLAv20", c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.hasMapmaker = false;

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-info.txt", "bennu_olav20ptm_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/navcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);


            //public version
            if (publicOnly)
	        {
	            BennuConfigs publicOLAptm = (BennuConfigs)c.clone();
	//            publicOLAptm.author = ShapeModelType.provide("OLA-v20-PTM_PUBLIC");
	            publicOLAptm.modelLabel = "OLA v20 PTM";
	            publicOLAptm.disableSpectra();
	//            publicOLAptm.hasImageMap = true;
	            publicOLAptm.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            publicOLAptm.baseMapConfigName = "config_public.txt";

	            publicOLAptm.presentInMissions = PublicOnly;
	            setupFeatures(publicOLAptm);
	            imagingConfig = (ImagingInstrumentConfig)publicOLAptm.getConfigForClass(ImagingInstrumentConfig.class);
	            spectrumConfig = (SpectrumInstrumentConfig)publicOLAptm.getConfigForClass(SpectrumInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		publicOLAptm.generatePolycamInstrument("bennu_olav20ptm_polycam", "bennu_olav20ptm_polycam", true, true, true),
	            		publicOLAptm.generateMapcamInstrument("bennu_olav20ptm_mapcam", "bennu_olav20ptm_mapcam", true, true, true),
	            		publicOLAptm.generateNavcamInstrument("bennu_olav20ptm_navcam", "bennu_olav20ptm_navcam", true)
	            );
	            spectrumConfig.hasHypertreeBasedSpectraSearch = false;

	            lidarConfig = (LidarInstrumentConfig)publicOLAptm.getConfigForClass(LidarInstrumentConfig.class);

	            lidarConfig.lidarBrowseDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarSearchDataSourceMap.clear();
	            lidarConfig.orexSearchTimeMap.clear();
	            lidarConfig.orexSearchTimeMap.put("OLAv20", startStop);
	            lidarConfig.lidarSearchDataSourceMap.put("OLAv20", publicOLAptm.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            	configArray.add(publicOLAptm);
	        }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v21");
            c.modelLabel = "OLA v21";
            c.rootDirOnServer = "/bennu/ola-v21-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.1953;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02524469206484981;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(c.generatePolycamInstrument("bennu_olav21_polycam", "bennu_olav21_polycam", true, true),
                                                             c.generateMapcamInstrument("bennu_olav21_mapcam", "bennu_olav21_mapcam", true, true),
                                                             c.generateNavcamInstrument("bennu_olav21_navcam", "bennu_olav21_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
            lidarConfig.lidarSearchDataSourceMap.clear();
            lidarConfig.orexSearchTimeMap.clear();
            lidarConfig.orexSearchTimeMap.put("OLAv21", startStop);
            lidarConfig.lidarSearchDataSourceMap.put("OLAv21", c.rootDirOnServer + "/ola/search/olav21/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt";
            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav21_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav21_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav21_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav21_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav21_navcam")
            };

            c.defaultForMissions = OREXClients;
            if (!publicOnly)
                configArray.add(c);

            //public version
            if (publicOnly)
	        {
	            BennuConfigs publicOLA = (BennuConfigs)c.clone();
	//            publicOLA.author = ShapeModelType.provide("OLA-v21_PUBLIC");
	            publicOLA.modelLabel = "OLA v21";
	            publicOLA.disableSpectra();
	//            publicOLA.hasImageMap = true;
	            publicOLA.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            publicOLA.presentInMissions = PublicOnly;
	            publicOLA.baseMapConfigName = "config_public.txt";
	            setupFeatures(publicOLA);
	            imagingConfig = (ImagingInstrumentConfig)publicOLA.getConfigForClass(ImagingInstrumentConfig.class);
	            spectrumConfig = (SpectrumInstrumentConfig)publicOLA.getConfigForClass(SpectrumInstrumentConfig.class);
	            lidarConfig = (LidarInstrumentConfig)publicOLA.getConfigForClass(LidarInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(
	            		publicOLA.generatePolycamInstrument("bennu_olav21_polycam", "bennu_olav21_polycam", true, true, true),
	                     publicOLA.generateMapcamInstrument("bennu_olav21_mapcam", "bennu_olav21_mapcam", true, true, true),
	                     publicOLA.generateNavcamInstrument("bennu_olav21_navcam", "bennu_olav21_navcam", true)
	            );
	            spectrumConfig.hasHypertreeBasedSpectraSearch = false;
	            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
	            lidarConfig.lidarSearchDataSourceMap.clear();
	            lidarConfig.orexSearchTimeMap.clear();
	            lidarConfig.orexSearchTimeMap.put("OLAv21", startStop);
	            lidarConfig.lidarSearchDataSourceMap.put("OLAv21", c.rootDirOnServer + "/ola/search/olav21/dataSource.lidar");
                configArray.add(publicOLA);
	        }
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v21-PTM");
            c.modelLabel = "OLA v21 PTM";
            c.rootDirOnServer = "/bennu/ola-v21-ptm";
            c.setResolution( //
                    ImmutableList.of("Low (231870 plates)", "Medium (886400 plates)", "High (3365938 plates)", "Very High (17866836 plates)"),
                    ImmutableList.of(231870, 886400, 3365938, 17866836));
            c.density = 1.1953;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02524469206484981;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(
            		c.generatePolycamInstrument("bennu_olav21ptm_polycam", "bennu_olav21ptm_polycam", true, true),
                                                             c.generateMapcamInstrument("bennu_olav21ptm_mapcam", "bennu_olav21ptm_mapcam", true, true),
                                                             c.generateNavcamInstrument("bennu_olav21ptm_navcam", "bennu_olav21ptm_navcam")
            );

            c.generateStateHistoryParameters();

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
            lidarConfig.lidarSearchDataSourceMap.clear();
            lidarConfig.orexSearchTimeMap.clear();
            lidarConfig.orexSearchTimeMap.put("OLAv21PTM", startStop);
            lidarConfig.lidarSearchDataSourceMap.put("OLAv21PTM", c.rootDirOnServer + "/ola/search/olav21ptm/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt";
            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");

            c.hasMapmaker = false;

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/mapcam/imagelist-fullpath-sum.txt", "bennu_olav21ptm_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/polycam/imagelist-fullpath-sum.txt", "bennu_olav21ptm_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/mapcam/imagelist-fullpath-info.txt", "bennu_olav21ptm_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/polycam/imagelist-fullpath-info.txt", "bennu_olav21ptm_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/navcam/imagelist-fullpath-info.txt", "bennu_olav21ptm_navcam")
            };
            if (!publicOnly)
                configArray.add(c);


            //public version
            if (publicOnly)
	        {
	            BennuConfigs publicOLAptm = (BennuConfigs)c.clone();
	//            publicOLAptm.author = ShapeModelType.provide("OLA-v21-PTM_PUBLIC");
	            publicOLAptm.modelLabel = "OLA v21 PTM";
	            publicOLAptm.disableSpectra();
	//            publicOLAptm.hasImageMap = true;
	            publicOLAptm.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            publicOLAptm.baseMapConfigName = "config_public.txt";

	            publicOLAptm.presentInMissions = PublicOnly;
	            setupFeatures(publicOLAptm);
	            imagingConfig = (ImagingInstrumentConfig)publicOLAptm.getConfigForClass(ImagingInstrumentConfig.class);
	            spectrumConfig = (SpectrumInstrumentConfig)publicOLAptm.getConfigForClass(SpectrumInstrumentConfig.class);
	            lidarConfig = (LidarInstrumentConfig)publicOLAptm.getConfigForClass(LidarInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(publicOLAptm.generatePolycamInstrument("bennu_olav21ptm_polycam", "bennu_olav21ptm_polycam", true, true, true),
	                    publicOLAptm.generateMapcamInstrument("bennu_olav21ptm_mapcam", "bennu_olav21ptm_mapcam", true, true, true),
	                    publicOLAptm.generateNavcamInstrument("bennu_olav21ptm_navcam", "bennu_olav21ptm_navcam", true)
	            );
	            spectrumConfig.hasHypertreeBasedSpectraSearch = false;
	            lidarConfig.lidarBrowseDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A_OLAv21.txt");
	            lidarConfig.lidarSearchDataSourceMap.clear();
	            lidarConfig.orexSearchTimeMap.clear();
	            lidarConfig.orexSearchTimeMap.put("OLAv21PTM", startStop);
	            lidarConfig.lidarSearchDataSourceMap.put("OLAv21PTM", publicOLAptm.rootDirOnServer + "/ola/search/olav21ptm/dataSource.lidar");
                configArray.add(publicOLAptm);
	        }
        }

       if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();


            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("SPO-v54");
            c.modelLabel = "SPO v54";
            c.rootDirOnServer = "/bennu/spo-v54-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02538555084803482;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs
            setupFeatures(c);
            c.setBodyParameters();
            ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
            LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

            imagingConfig.imagingInstruments = Lists.newArrayList(c.generatePolycamInstrument("bennu_spov54_polycam", "bennu_spov54_polycam", true, true),
                                                             c.generateMapcamInstrument("bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true),
                                                             c.generateNavcamInstrument("bennu_spov54_navcam", "bennu_spov54_navcam")
            );

            c.setSpectrumParameters();
            spectrumConfig.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.clear();
//            c.orexSearchTimeMap.clear();
//            c.orexSearchTimeMap.put("SPOv54", startStop);
//            c.lidarSearchDataSourceMap.put("SPOv54", c.rootDirOnServer + "/ola/search/olav54/dataSource.lidar");
            lidarConfig.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            lidarConfig.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_spov54_mapcam"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-sum.txt", "bennu_spov54_polycam"),

                new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-info.txt", "bennu_spov54_mapcam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-info.txt", "bennu_spov54_polycam"),
                new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/navcam/imagelist-fullpath-info.txt", "bennu_spov54_navcam")
            };

            if (!publicOnly)
                configArray.add(c);

            //public version
            if (publicOnly)
		    {
	            BennuConfigs publicModel = (BennuConfigs)c.clone();
	//            publicModel.author = ShapeModelType.provide("SPO-v54_PUBLIC");
	            publicModel.modelLabel = "SPO v54";
	            publicModel.disableSpectra();
	//            publicModel.hasImageMap = true;
	            publicModel.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
	            publicModel.presentInMissions = PublicOnly;
	            publicModel.baseMapConfigName = "config_public.txt";
	            setupFeatures(publicModel);
	            imagingConfig = (ImagingInstrumentConfig)publicModel.getConfigForClass(ImagingInstrumentConfig.class);
	            spectrumConfig = (SpectrumInstrumentConfig)publicModel.getConfigForClass(SpectrumInstrumentConfig.class);
	            lidarConfig = (LidarInstrumentConfig)publicModel.getConfigForClass(LidarInstrumentConfig.class);

	            imagingConfig.imagingInstruments = Lists.newArrayList(publicModel.generatePolycamInstrument("bennu_spov54_polycam", "bennu_spov54_polycam", true, true, true),
	                     publicModel.generateMapcamInstrument("bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true, true),
	                     publicModel.generateNavcamInstrument("bennu_spov54_navcam", "bennu_olav54_navcam", true)
	            );
	            spectrumConfig.hasHypertreeBasedSpectraSearch = false;
	            lidarConfig.lidarBrowseDataSourceMap.put("Default", publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarBrowseFileListResourcePath = publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
	            lidarConfig.lidarBrowseWithPointsDataSourceMap.put("Default", publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
	            lidarConfig.lidarSearchDataSourceMap.clear();
	            lidarConfig.orexSearchTimeMap.clear();
	            lidarConfig.orexSearchTimeMap.put("SPOv54", startStop);
	            lidarConfig.lidarSearchDataSourceMap.put("SPOv54", publicModel.rootDirOnServer + "/ola/search/olav54/dataSource.lidar");
	                configArray.add(publicModel);
		    }
        }

    }

	private void generateStateHistoryParameters()
	{
		StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)getConfigForClass(StateHistoryConfig.class);
        hasStateHistory = true;
        timeHistoryFile = rootDirOnServer + "/history/timeHistory.bth";
        stateHistoryConfig.stateHistoryStartDate = new GregorianCalendar(2018, 10, 25, 0, 0, 0).getTime();
        stateHistoryConfig.stateHistoryEndDate = new GregorianCalendar(2025, 1, 1, 0, 0, 0).getTime();
        stateHistoryConfig.spiceInfo = new SpiceInfo("ORX", "IAU_BENNU", "ORX_SPACECRAFT", "BENNU",
    			new String[] {"EARTH" , "SUN"},
    			new String[] {"IAU_EARTH" , "IAU_SUN"},
    			new String[] {},
    			new String[] {"ORX_OCAMS_POLYCAM", "ORX_OCAMS_MAPCAM",
    															"ORX_OCAMS_SAMCAM", "ORX_NAVCAM1", "ORX_NAVCAM2",
//    															"ORX_OTES", "ORX_OVIRS",
    															"ORX_OLA_LOW", "ORX_OLA_HIGH"});
	}

	private ImagingInstrument generatePolycamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE)
	{
		return generatePolycamInstrument(spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, false);
	}

	private ImagingInstrument generatePolycamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		String gallery = "/polycam/gallery";
		PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		if (includeSPC) imageSourceArray.add(PointingSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(PointingSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);
//		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/polycam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
//		phpQuery.setPublicOnly(publicOnly);

		DataQuerySourcesMetadata polyCamMetadata = DataQuerySourcesMetadata.of(rootDirOnServer + "/polycam", "", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
        ImageDataQuery phpQuery = new ImageDataQuery(polyCamMetadata);

		phpQuery.setImageNameTable("bennu_polycam_images");
		ImagingInstrument instrument = new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.POLYCAM_FLIGHT_IMAGE,
                imageSources,
                Instrument.POLYCAM,
                0,
                "X"
                );
		return instrument;
	}

	private ImagingInstrument generateMapcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE)
	{
		return generateMapcamInstrument(spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, false);
	}

	private ImagingInstrument generateMapcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		String gallery = "/mapcam/gallery";
		PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		if (includeSPC) imageSourceArray.add(PointingSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(PointingSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);
//		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/mapcam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);

		DataQuerySourcesMetadata mapCamMetadata = DataQuerySourcesMetadata.of(rootDirOnServer + "/mapcam", "", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
        ImageDataQuery phpQuery = new ImageDataQuery(mapCamMetadata);

		phpQuery.setPublicOnly(publicOnly);
		phpQuery.setImageNameTable("bennu_mapcam_images");



		return new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.MAPCAM_FLIGHT_IMAGE,
                imageSources,
                Instrument.MAPCAM,
                0,
                "X"
                );
	}

	private ImagingInstrument generateNavcamInstrument(String spcNamePrefix, String spiceNamePrefix)
	{
		return generateNavcamInstrument(spcNamePrefix, spiceNamePrefix, false);
	}

	private ImagingInstrument generateNavcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean publicOnly)
	{
		String gallery = "/navcam/gallery";
//		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/navcam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);

		DataQuerySourcesMetadata mapCamMetadata = DataQuerySourcesMetadata.of(rootDirOnServer + "/navcam", "", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
        ImageDataQuery phpQuery = new ImageDataQuery(mapCamMetadata);

		phpQuery.setPublicOnly(publicOnly);
		phpQuery.setImageNameTable("bennu_navcam_images");
		return new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.NAVCAM_FLIGHT_IMAGE,
                new PointingSource[]{PointingSource.SPICE},
                Instrument.NAVCAM,
                0,
                "X"
                );
	}

	private void setBodyParameters()
	{
		body = ShapeModelBody.RQ36;
        type = BodyType.ASTEROID;
        population = ShapeModelPopulation.NEO;
        shapeModelFileExtension = ".obj";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e3;
        useMinimumReferencePotential = true;
//        hasImageMap = true;
        addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(this));

	}

	private void disableSpectra()
	{
		SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)getConfigForClass(SpectrumInstrumentConfig.class);
		spectrumConfig.hasSpectralData = false;
		spectrumConfig.hasHierarchicalSpectraSearch = false;
		spectrumConfig.hasHypertreeBasedSpectraSearch = false;
		spectrumConfig.spectraSearchDataSourceMap.clear();
		spectrumConfig.spectralInstruments.clear();
	}

	private void setSpectrumParameters()
	{
		SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)getConfigForClass(SpectrumInstrumentConfig.class);
		spectrumConfig.hasSpectralData = true;
		spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
		spectrumConfig.spectralInstruments.add(new OTES());
		spectrumConfig.spectralInstruments.add(new OVIRS());

		spectrumConfig.hasHierarchicalSpectraSearch = true;
		spectrumConfig.hasHypertreeBasedSpectraSearch = false;
		spectrumConfig.spectraSearchDataSourceMap = new LinkedHashMap<>();
		spectrumConfig.spectraSearchDataSourceMap.put("OTES_L2", rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
		spectrumConfig.spectraSearchDataSourceMap.put("OTES_L3", rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
		spectrumConfig.spectraSearchDataSourceMap.put("OVIRS_IF", rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
		spectrumConfig.spectraSearchDataSourceMap.put("OVIRS_REF", rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");

        SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO("OREX", instrumentSearchSpecs);
        spectrumConfig.hierarchicalSpectraSearchSpecification = specIO;
	}

	private void setLidarParameters(boolean hasHypertree)
	{
		LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)getConfigForClass(LidarInstrumentConfig.class);
		lidarConfig.hasLidarData = true;
		lidarConfig. hasHypertreeBasedLidarSearch = hasHypertree; // enable tree-based lidar searching
		lidarConfig. lidarInstrumentName = Instrument.OLA;
		lidarConfig. lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
		lidarConfig. lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
		lidarConfig. lidarSearchDataSourceMap = new LinkedHashMap<>();
		lidarConfig. lidarBrowseDataSourceMap = new LinkedHashMap<>();
		lidarConfig. lidarBrowseDataSourceMap.put("Default", rootDirOnServer + "/ola/browse/fileListv2.txt");
		lidarConfig. lidarBrowseFileListResourcePath = rootDirOnServer + "/ola/browse/fileListv2.txt";
		lidarConfig. lidarBrowseWithPointsDataSourceMap.put("Default", rootDirOnServer + "/ola/browse/fileListv2.txt");

         if (hasHypertree)
         {
	         /*
	          * search times split into phases
	          */
	         ArrayList<Date> startStop = new ArrayList<Date>();
	         startStop.add(lidarConfig.lidarSearchDefaultStartDate);
	         startStop.add(lidarConfig.lidarSearchDefaultEndDate);
	         lidarConfig.orexSearchTimeMap.put("Default", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(lidarConfig.lidarSearchDefaultStartDate);
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         lidarConfig.orexSearchTimeMap.put("Preliminary", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         lidarConfig.orexSearchTimeMap.put("Detailed", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         lidarConfig.orexSearchTimeMap.put("OrbB", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         startStop.add(lidarConfig.lidarSearchDefaultEndDate);
	         lidarConfig.orexSearchTimeMap.put("Recon", startStop);

	         lidarConfig.lidarSearchDataSourceMap.put("Preliminary", rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
	         lidarConfig.lidarSearchDataSourceMap.put("Detailed", rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
	         lidarConfig.lidarSearchDataSourceMap.put("OrbB", rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
	         lidarConfig.lidarSearchDataSourceMap.put("Recon", rootDirOnServer + "/ola/search/recon/dataSource.lidar");
         }

         lidarConfig.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
         lidarConfig.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
         lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
         lidarConfig.lidarBrowseTimeIndex = 26;
         lidarConfig.lidarBrowseNoiseIndex = 62;
         lidarConfig.lidarBrowseOutgoingIntensityIndex = 98;
         lidarConfig.lidarBrowseReceivedIntensityIndex = 106;
         lidarConfig.lidarBrowseIntensityEnabled = true;
         lidarConfig.lidarBrowseNumberHeaderLines = 0;
         lidarConfig.lidarBrowseIsInMeters = true;
         lidarConfig.lidarBrowseIsBinary = true;
         lidarConfig.lidarBrowseBinaryRecordSize = 186;
         lidarConfig.lidarOffsetScale = 0.0005;
	}

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }

//    @Override
//    public Instrument getLidarInstrument()
//    {
//        // TODO Auto-generated method stub
//        return lidarInstrumentName;
//    }
//
//    public boolean hasHypertreeLidarSearch()
//    {
//        return hasHypertreeBasedLidarSearch;
//    }
//
//    public SpectraHierarchicalSearchSpecification<?> getHierarchicalSpectraSearchSpecification()
//    {
//        return hierarchicalSpectraSearchSpecification;
//    }
}
