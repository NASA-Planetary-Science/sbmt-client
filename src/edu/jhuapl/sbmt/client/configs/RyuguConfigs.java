package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
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
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.query.v2.IDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class RyuguConfigs extends SmallBodyViewConfig
{
	List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> instrumentSearchSpecs = new ArrayList<SpectrumInstrumentMetadata<SpectrumSearchSpec>>();


	public RyuguConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

		SpectrumSearchSpec nirs3 = new SpectrumSearchSpec("NIRS3", "/ryugu/shared/nirs3", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "nm", "Radiance", "NIRS3");
		List<SpectrumSearchSpec> nirs3Specs = new ArrayList<SpectrumSearchSpec>();
		nirs3Specs.add(nirs3);

		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("NIRS3", nirs3Specs));
	}

	private static void setupFeatures(RyuguConfigs c)
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


	public static void initialize(ConfigArrayList<IBodyViewConfig> configArray)
    {
        configureEarth(configArray);
        configureTruth(configArray);
        configureGaskell(configArray);
        configureJAXASFM20180627(configArray);
        configureJAXASFM20180714(configArray);
        configureJAXASFM201807252(configArray);
        configureJAXASFM20180804(configArray);
        configureJAXASPC20180705(configArray);
        configureJAXASPC20180717(configArray);
        configureJAXASPC201807192(configArray);
        configureJAXASPC20180731(configArray);
        configureJAXASPC20180810(configArray);
        configureJAXASPC20180816(configArray);
        configureJAXASPC20180829(configArray);
        configureJAXASPC20181014(configArray);
        configureNASA001(configArray);
        configureNASA002(configArray);
        configureNASA003(configArray);
        configureNASA004(configArray);
        configureNASA005(configArray);
        configureNASA006(configArray);
    }

	private static void configureEarth(ConfigArrayList<IBodyViewConfig> configArray)
	{
		//
		// Earth, Hayabusa2 WGS84 version
		// :

		// Set up body -- one will suffice.
		SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.EARTH.name(),
				BodyType.PLANETS_AND_SATELLITES.name(), ShapeModelPopulation.EARTH.name()).build();

		// Set up shape model -- one will suffice.
		ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("hayabusa2", ShapeModelDataUsed.WGS84)
				.build();
		ImagingInstrument tir;

		// Set up images.
		SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO",
				null, ".jpeg");
//            QueryBase queryBase = new FixedListQuery<>(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

		DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(
				fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null,
				fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
		FixedListDataQuery tirQuery = new FixedListDataQuery(metadata);

		Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration
				.builder(Instrument.TIR, SpectralImageMode.MONO, tirQuery, new PointingSource[]
				{ PointingSource.SPICE }, fileLocator, ImageType.TIR_IMAGE);

		imagingInstBuilder.put(ImagingInstrumentConfiguration.TRANSPOSE, Boolean.FALSE);

		// Put it all together in a session.
		Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
		builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
		tir = BasicImagingInstrument.of(builder.build());

		RyuguConfigs c = new RyuguConfigs();
		c.body = ShapeModelBody.EARTH;
		c.type = BodyType.PLANETS_AND_SATELLITES;
		c.population = ShapeModelPopulation.EARTH;
		c.dataUsed = ShapeModelDataUsed.WGS84;
		c.author = ShapeModelType.JAXA_SFM_v20180627;
		c.modelLabel = "Haybusa2-testing";
		c.rootDirOnServer = "/earth/hayabusa2";
//          c.shapeModelFileExtension = ".obj";
		c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]),
				ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
//          c.hasImageMap = true;
		c.hasColoringData = false;

		setupFeatures(c);
		ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig) c
				.getConfigForClass(ImagingInstrumentConfig.class);
		SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig) c
				.getConfigForClass(SpectrumInstrumentConfig.class);
		LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig) c.getConfigForClass(LidarInstrumentConfig.class);

		imagingConfig.imagingInstruments = Lists.newArrayList(tir);

		imagingConfig.imageSearchFilterNames = new String[]
		{};
		imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[]
		{};
		imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 0;
		imagingConfig.imageSearchDefaultMaxResolution = 0;

		c.hasMapmaker = false;
		imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2015, 11, 1, 0, 0, 0).getTime();
		imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime();
		// TODO make hierarchical search work sbmt1dev-style.
//          c.imageSearchFilterNames = new String[]{
//                  EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
//          };
//          c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                  EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
//          };
//          c.hasHierarchicalImageSearch = true;
//          c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
		imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
		imagingConfig.imageSearchDefaultMaxResolution = 300.0;

		spectrumConfig.hasSpectralData = true;
		spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
		spectrumConfig.spectralInstruments.add(new NIRS3());

		c.presentInMissions = new Mission[]
		{};
		c.defaultForMissions = new Mission[]
		{};

		configArray.add(c);

		lidarConfig.hasLidarData = false;
	}

	private static void configureTruth(ConfigArrayList<IBodyViewConfig> configArray)
	{
        // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Truth", ShapeModelDataUsed.IMAGE_BASED).build();

        RyuguConfigs c = new RyuguConfigs();
        c.body = ShapeModelBody.RYUGU;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = ShapeModelType.TRUTH;
        c.modelLabel = "H2 Simulated Truth";
        c.rootDirOnServer = "/ryugu/truth";
        c.shapeModelFileExtension = ".obj";

        c.setResolution(ImmutableList.of("Low (54504 plates)", "High (5450420 plates)"), ImmutableList.of(54504, 5450420));

        setupFeatures(c);

        // This version would enable image search but this seems to hang, possibly
        // because of the very high resolution of the model.
        // Re-enable this if/when that issue is addressed.
//        QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/imaging", "ryugu", "/ryugu/truth/imaging/images/gallery");
//        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.IMAGING_DATA, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_TRUTH_IMAGE);
        SBMTFileLocator fileLocatorTir = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
//        QueryBase queryBaseTir = new FixedListQuery(fileLocatorTir.get(SBMTFileLocator.TOP_PATH).getLocation("") + "/simulated", fileLocatorTir.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
//        QueryBase queryBaseTir = new GenericPhpQuery("/ryugu/truth/tir", "ryugu_nasa002_tir", "/ryugu/truth/tir/gallery");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/truth/tir", "", null, "ryugu_nasa002_tir", "/ryugu/truth/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tir = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

//        QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/onc", "ryugu_sim", "/ryugu/truth/onc/gallery");

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/truth/onc", "", null, "ryugu_sim", "/ryugu/truth/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_TRUTH_IMAGE, 90.0, "None");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam
        );
        c.hasMapmaker = false;
        configureImages(imagingConfig);
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime();

        configureStateHistory(stateHistoryConfig, "/ryugu/truth/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureGaskell(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Gaskell", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/gaskell/onc", "", "ryugu_sim", "ryugu_sim", "/ryugu/gaskell/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL }, ImageType.ONC_IMAGE, 0.0, "None");

        RyuguConfigs c = new RyuguConfigs();
        c.body = ShapeModelBody.RYUGU;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.SIMULATED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "H2 Simulated SPC";
        c.rootDirOnServer = "/ryugu/gaskell";
        c.shapeModelFileExtension = ".obj";

        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/gaskell/history/timeHistory.bth");

        configureLidar(lidarConfig);

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASFM20180627(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180627", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180627/onc", "", "jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180627/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180627/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SFM_v20180627;
        c.modelLabel = "JAXA-SFM-v20180627";
        c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180627";
        c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));

        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180627/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/onc", "jaxasfmv20180627", "ryugu/jaxa-sfm-v20180627/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180627/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180627/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASFM20180714(ConfigArrayList<IBodyViewConfig> configArray)
	{
        // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180714", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180714/onc", "", "ryugu_jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180714/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180714/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180714/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SFM_v20180714;
        c.modelLabel = "JAXA-SFM-v20180714";
        c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180714";

        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180714/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180714/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180714/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASFM201807252(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180725_2", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180725-2/onc", "", "ryugu_jaxasfmv201807252", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180725-2/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180725-2/tir", "",  "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SFM_v20180725_2;
        c.modelLabel = "JAXA-SFM-v20180725_2";
        c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180725-2";

        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );

        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180725-2/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180725-2/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180725-2/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASFM20180804(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180804", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180804/onc", "", "ryugu_jaxasfmv20180804", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180804/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-sfm-v20180804/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180804/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SFM_v20180804;
        c.modelLabel = "JAXA-SFM-v20180804";
        c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180804";

        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180804/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180804/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180804/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180705(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180705", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180705/onc", "", "ryugu_jaxaspcv20180705", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180705/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180705/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180705/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SPC_v20180705;
        c.modelLabel = "JAXA-SPC-v20180705";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180705";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180705/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
            	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180705/onc"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180705/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180717(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180717", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180717/onc", "", "ryugu_jaxaspcv20180717", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180717/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");
        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180717/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180717/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SPC_v20180717;
        c.modelLabel = "JAXA-SPC-v20180717";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180717";
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180717/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180717/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180717/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC201807192(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180719_2", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180719-2/onc", "", "ryugu_jaxaspcv201807192", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180719-2/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.ONC_IMAGE, 90.0, "None");

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180719-2/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180719-2/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SPC_v20180719_2;
        c.modelLabel = "JAXA-SPC-v20180719_2";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180719-2";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180719-2/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180719-2/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180719-2/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180731(ConfigArrayList<IBodyViewConfig> configArray)
	{
		 // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180731", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180731/onc", "", "ryugu_jaxaspcv20180731", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180731/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180731/tir", "", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180731/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.JAXA_SPC_v20180731;
        c.modelLabel = "JAXA-SPC-v20180731";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180731";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180731/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180731/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180731/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180810(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180810", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180810/onc", "", "ryugu_jaxaspcv20180810", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180810/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180810/tir", "", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180810/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)

        c.author = ShapeModelType.JAXA_SPC_v20180810;
        c.modelLabel = "JAXA-SPC-v20180810";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180810";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180810/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180810/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180810/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180816(ConfigArrayList<IBodyViewConfig> configArray)
	{
        // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180816", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180816/onc", "", "ryugu_jaxaspcv20180816", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180816/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180816/tir", "", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180816/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)

        c.author = ShapeModelType.JAXA_SPC_v20180816;
        c.modelLabel = "JAXA-SPC-v20180816";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180816";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );

        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180816/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180816/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180816/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20180829(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180829", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180829/onc", "", "ryugu_jaxaspcv20180829", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180829/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20180829/tir", "", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180829/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)

        c.author = ShapeModelType.JAXA_SPC_v20180829;
        c.modelLabel = "JAXA-SPC-v20180829";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20180829";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180829/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180829/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180829/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureJAXASPC20181014(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20181014", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20181014/onc", "", "ryugu_jaxaspcv20181014", "ryugu_nasa005", "/ryugu/jaxa-spc-v20181014/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/jaxa-spc-v20181014/tir", "", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20181014/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)

        c.author = ShapeModelType.JAXA_SPC_v20181014;
        c.modelLabel = "JAXA-SPC-v20181014";
        c.rootDirOnServer = "/ryugu/jaxa-spc-v20181014";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);
        configureSpectra(spectrumConfig, c.rootDirOnServer, c.instrumentSearchSpecs);
        configureStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20181014/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20181014/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20181014/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20181014/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20181014/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        configArray.add(c);
	}

	private static void configureNASA001(ConfigArrayList<IBodyViewConfig> configArray)
	{
        // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-001", ShapeModelDataUsed.IMAGE_BASED).build();

//        QueryBase queryBase = new GenericPhpQuery("/ryugu/nasa-001/onc", "ryugu_flight", "/ryugu/nasa-001/onc/gallery");

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-001/onc", "", null, "ryugu_flight", "/ryugu/nasa-001/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL }, ImageType.ONC_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.NASA_001;
        c.modelLabel = "NASA-001";
        c.rootDirOnServer = "/ryugu/nasa-001";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam
        );

        c.hasMapmaker = false;
        configureImages(imagingConfig);
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-001/onc", "ryugu_flight", "ryugu/nasa-001/onc"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureNASA002(ConfigArrayList<IBodyViewConfig> configArray)
	{
		  // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-002", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-002/onc", "", "ryugu_nasa002", "ryugu_nasa002", "/ryugu/nasa-002/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-002/tir", "", "", "ryugu_nasa002_tir", "/ryugu/nasa-002/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);

        c.author = ShapeModelType.NASA_002;
        c.modelLabel = "NASA-002";
        c.rootDirOnServer = "/ryugu/nasa-002";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;
        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/nasa-002/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
            	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/onc", "ryugu_nasa002", "ryugu/nasa-002/onc"),
            	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/tir", "ryugu_nasa002_tir", "ryugu/nasa-002/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureNASA003(ConfigArrayList<IBodyViewConfig> configArray)
	{
        // Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-003", ShapeModelDataUsed.IMAGE_BASED).build();

        // NOTE THE FOLLOWING LINE IS NOT A TYPO: THIRD ARGUMENT SHOULD BE ryugu_nasa002, not ryugu_nasa003.
        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-003/onc", "", "ryugu_nasa003", "ryugu_nasa002", "/ryugu/nasa-003/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-003/tir", "", "", "ryugu_nasa002_tir", "/ryugu/nasa-003/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.author = ShapeModelType.NASA_003;
        c.modelLabel = "NASA-003";
        c.rootDirOnServer = "/ryugu/nasa-003";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );

        c.hasMapmaker = false;
        configureImages(imagingConfig);

        configureStateHistory(stateHistoryConfig, "/ryugu/nasa-003/history/timeHistory.bth");
        configureLidar(lidarConfig);


        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/onc", "ryugu_nasa002", "ryugu/nasa-003/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/tir", "ryugu_nasa002_tir", "ryugu/nasa-003/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureNASA004(ConfigArrayList<IBodyViewConfig> configArray)
	{

		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-004", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-004/onc", "", "ryugu_nasa004", "ryugu_nasa005", "/ryugu/nasa-004/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-004/tir", "", "", "ryugu_nasa005_tir", "/ryugu/nasa-004/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)
        c.author = ShapeModelType.NASA_004;
        c.modelLabel = "NASA-004";
        c.rootDirOnServer = "/ryugu/nasa-004";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );
        c.hasMapmaker = false;

        configureImages(imagingConfig);

        configureStateHistory(stateHistoryConfig, "/ryugu/nasa-004/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/onc", "ryugu_nasa005", "ryugu/nasa-004/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/tir", "ryugu_nasa005_tir", "ryugu/nasa-004/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureNASA005(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-005", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-005/onc", "", "ryugu_nasa005", "ryugu_nasa005", "/ryugu/nasa-005/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-005/tir", "", "", "ryugu_nasa005_tir", "/ryugu/nasa-005/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)
        c.author = ShapeModelType.NASA_005;
        c.modelLabel = "NASA-005";
        c.rootDirOnServer = "/ryugu/nasa-005";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );

        c.hasMapmaker = false;
        configureImages(imagingConfig);

        configureStateHistory(stateHistoryConfig, "/ryugu/nasa-005/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/onc", "ryugu_nasa005", "ryugu/nasa-005/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/tir", "ryugu_nasa005_tir", "ryugu/nasa-005/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureNASA006(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Set up body -- one will suffice.
        SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                ShapeModelBody.RYUGU.name(),
                BodyType.ASTEROID.name(),
                ShapeModelPopulation.NEO.name()).build();

        // Set up shape model -- one will suffice.
        ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-006", ShapeModelDataUsed.IMAGE_BASED).build();

        DataQuerySourcesMetadata oncCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-006/onc", "", "ryugu_nasa006", "ryugu_nasa005", "/ryugu/nasa-006/onc/gallery");
        ImageDataQuery oncQuery = new ImageDataQuery(oncCamMetadata);

        DataQuerySourcesMetadata tirCamMetadata = DataQuerySourcesMetadata.of("/ryugu/nasa-006/tir", "", "", "ryugu_nasa005_tir", "/ryugu/nasa-006/tir/gallery");
        ImageDataQuery tirQuery = new ImageDataQuery(tirCamMetadata);

        ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQuery, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE }, ImageType.ONC_IMAGE);
        ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQuery, new PointingSource[] { PointingSource.SPICE }, ImageType.TIR_IMAGE);

        RyuguConfigs c = new RyuguConfigs();
        configureBody(c);
        c.density = 1.200; // (g/cm^3)
        c.rotationRate = 0.00022867; // (rad/sec)
        c.author = ShapeModelType.NASA_006;
        c.modelLabel = "NASA-006";
        c.rootDirOnServer = "/ryugu/nasa-006";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                oncCam, tirCam
        );

        c.hasMapmaker = false;
        configureImages(imagingConfig);
        configureStateHistory(stateHistoryConfig, "/ryugu/nasa-006/history/timeHistory.bth");
        configureLidar(lidarConfig);

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-006/onc", "ryugu_nasa005", "ryugu/nasa-006/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-006/tir", "ryugu_nasa005_tir", "ryugu/nasa-006/tir"),
        };

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureBody(RyuguConfigs c)
	{
		 c.body = ShapeModelBody.RYUGU;
	     c.type = BodyType.ASTEROID;
	     c.population = ShapeModelPopulation.NEO;
	     c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
	     c.setResolution(ImmutableList.of(
	                "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
	                ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
	     c.shapeModelFileExtension = ".obj";
	     c.density = 1.500; // (g/cm^3)
	     c.rotationRate = 0.00022871; // (rad/sec)
	}

	private static void configureImages(ImagingInstrumentConfig imagingConfig)
	{
		imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 0;
        imagingConfig.imageSearchDefaultMaxResolution = 0;

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 300.0;
	}

	private static void configureSpectra(SpectrumInstrumentConfig spectrumConfig, String rootOnServer, List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> searchSpecs)
	{
		spectrumConfig.hasSpectralData = true;
        spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
        spectrumConfig.spectralInstruments.add(new NIRS3());

        spectrumConfig.hasHierarchicalSpectraSearch = true;
        spectrumConfig.hasHypertreeBasedSpectraSearch = false;
        spectrumConfig.spectraSearchDataSourceMap = new LinkedHashMap<>();
        spectrumConfig.spectraSearchDataSourceMap.put("NIRS3", rootOnServer + "/nirs3/l2c/hypertree/dataSource.spectra");
        spectrumConfig.spectrumMetadataFile = rootOnServer + "/spectraMetadata.json";

        SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO("HAYABUSA2", searchSpecs);
//        specIO.setPathString(c.spectrumMetadataFile);
        spectrumConfig.hierarchicalSpectraSearchSpecification = specIO;
	}

	private static void configureStateHistory(StateHistoryConfig stateHistoryConfig, String path)
	{
		stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = path;
	}

	private static void configureLidar(LidarInstrumentConfig lidarConfig)
	{
        lidarConfig.hasLidarData = true;
        lidarConfig.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
        lidarConfig.lidarInstrumentName = Instrument.LASER;
        lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarBrowseDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
        lidarConfig.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
        lidarConfig.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

	private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, PointingSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
//        QueryBase queryBase = new FixedListQuery<>(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        FixedListDataQuery query = new FixedListDataQuery(metadata);

        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, query, imageSources, imageType, 0.0, "None");
    }

	private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType, 0.0, "None");
    }

    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument,IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType, double rotation, String flip)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType, rotation, flip);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTFileLocator fileLocator, SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType, double rotation, String flip)
    {
        Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(instrument, SpectralImageMode.MONO, queryBase, imageSources, fileLocator, imageType, rotation, flip);

        boolean isTranspose = !ImageType.TIR_IMAGE.equals(imageType);
        imagingInstBuilder.put(ImagingInstrumentConfiguration.TRANSPOSE, Boolean.valueOf(isTranspose));

        //TODO add flip and rotation here

        // Put it all together in a session.
        Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
        builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
        return BasicImagingInstrument.of(builder.build());
    }
}
