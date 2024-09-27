package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.FileCache;
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
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.phobos.PhobosExperimentalSearchSpecification;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class MarsConfigs extends SmallBodyViewConfig
{

	private static void setupFeatures(MarsConfigs c)
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

	public MarsConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}


	public static void initialize(ConfigArrayList<IBodyViewConfig> configArray, boolean publicOnly)
    {
        configurePhobosGaskell(configArray);
        configurePhobosThomas(configArray);
        //configurePhobosErnstHierarchical(configArray);
        configurePhobosErnst2018(configArray);
        configureDeimosThomas(configArray);
//        configureDeimosExperimental(configArray);
        configureDeimosErnst2018(configArray);
        configurePhobosErnst2018Megane(configArray, publicOnly);
        configurePhobosErnst2023(configArray);
        configureDeimosErnst2023(configArray);
    }

	private static void configurePhobosGaskell(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Gaskell Phobos
        MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2011)";
        c.density = 1.876;
        c.rotationRate = 0.00022803304110600688;
        c.rootDirOnServer = "/GASKELL/PHOBOS";
        setupFeatures(c);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        DataQuerySourcesMetadata phobosMetadata = DataQuerySourcesMetadata.of("/GASKELL/PHOBOS/imaging", "", null, null, "/GASKELL/PHOBOS/imaging/images/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(phobosMetadata),
//                        new FixedListQuery<>("/GASKELL/PHOBOS/imaging", "/GASKELL/PHOBOS/imaging/images/gallery"), //
//                        new GenericPhpQuery("/GASKELL/PHOBOS/IMAGING", "PHOBOS", "/GASKELL/PHOBOS/IMAGING/images/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA, //
                        0.0,
                        "X"
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
//        c.imageSearchFilterNames = new String[] {
//                "VSK, Channel 1",
//                "VSK, Channel 2",
//                "VSK, Channel 3",
//                "VIS, Blue",
//                "VIS, Minus Blue",
//                "VIS, Violet",
//                "VIS, Clear",
//                "VIS, Green",
//                "VIS, Red",
//        };
//        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "Phobos 2", "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 300.0;


        lidarConfig.hasLidarData = true;
        lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        lidarConfig.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
        lidarConfig.lidarBrowseIsLidarInSphericalCoordinates = true;
        lidarConfig.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
        lidarConfig.lidarBrowseIsTimeInET = true;
        lidarConfig.lidarBrowseTimeIndex = 5;
        lidarConfig.lidarBrowseNoiseIndex = -1;
        lidarConfig.lidarBrowseIsRangeExplicitInData = true;
        lidarConfig.lidarBrowseRangeIndex = 3;
        lidarConfig.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
        lidarConfig.lidarBrowseNumberHeaderLines = 1;
        lidarConfig.lidarBrowseIsInMeters = true;
        lidarConfig.lidarOffsetScale = 0.025;
        lidarConfig.lidarInstrumentName = Instrument.MOLA;

        // MOLA search is disabled for now. See LidarPanel class.
        lidarConfig.hasHypertreeBasedLidarSearch = true;
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt", ShapeModelBody.PHOBOS.toString().toLowerCase()),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};



        configArray.add(c);
	}

	private static void configurePhobosThomas(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// Thomas Phobos
        MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/PHOBOS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "m1phobos.llr.gz");
        c.setResolution(ImmutableList.of(32040));
        setupFeatures(c);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        lidarConfig.lidarSearchDataSourceMap = Maps.newHashMap(); // this must be instantiated, but can be empty

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};


        configArray.add(c);
	}

	@SuppressWarnings("unused")
	private static void configurePhobosErnstHierarchical(ConfigArrayList<IBodyViewConfig> configArray)
	{
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.provide("Ernst-hierarchical");
        c.modelLabel = "Ernst et al. (hierarchical)";
        c.rootDirOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";

        DataQuerySourcesMetadata phobosMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "", "PHOBOSEXP", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery");

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);


        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(phobosMetadata),
//                        new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );

        c.hasMapmaker = true;
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "VSK, Channel 1",
                "VSK, Channel 2",
                "VSK, Channel 3",
                "VIS, Blue",
                "VIS, Minus Blue",
                "VIS, Violet",
                "VIS, Clear",
                "VIS, Green",
                "VIS, Red",
        };
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {
                "Phobos 2",
                "Viking Orbiter 1-A",
                "Viking Orbiter 1-B",
                "Viking Orbiter 2-A",
                "Viking Orbiter 2-B",
                "MEX HRSC",
                "MRO HiRISE",
                "MGS MOC"
        };
        imagingConfig.hasHierarchicalImageSearch = true;
        imagingConfig.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
        imagingConfig.hierarchicalImageSearchSpecification.setSelectionModel(new CheckBoxTree(imagingConfig.hierarchicalImageSearchSpecification.getTreeModel()).getSelectionModel());
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 300.0;
        lidarConfig.lidarSearchDataSourceMap = Maps.newHashMap();
        // This was causing the "cfg does not equal config" type errors when the generator runs.
        // Just commenting it out for now.
//        c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/nearsdc/data/GASKELL/PHOBOSEXPERIMENTAL/IMAGING/imagelist.txt", "phobosexp"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

//        configArray.add(c);
	}

	private static void configurePhobosErnst2018(ConfigArrayList<IBodyViewConfig> configArray)
	{
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.EXPERIMENTAL;
        c.modelLabel = "Ernst et al. (in progress)";
        c.rootDirOnServer = "/phobos/ernst2018";
        c.shapeModelFileExtension = ".obj";

        DataQuerySourcesMetadata phobosMetadata = DataQuerySourcesMetadata.of("/phobos/ernst2018/imaging", "", null, null, "/phobos/ernst2018/imaging/gallery");
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);


        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(phobosMetadata),
//                        new GenericPhpQuery("/phobos/ernst2018/imaging", "PHOBOS_ERNST_2018", "/phobos/ernst2018/imaging/gallery"), //
//                        new FixedListQuery<>("/phobos/ernst2018/imaging", "/phobos/ernst2018/imaging/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new PointingSource[]{ PointingSource.GASKELL }, //
                        Instrument.IMAGING_DATA, //
                        0., //
                        "X" // Note: this means "flip along X axis". Don't know why, but this flip is needed as of this delivery.
                        ) //
        );

        c.hasMapmaker = true;
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime();
//        c.imageSearchFilterNames = new String[]{
//                "VSK, Channel 1",
//                "VSK, Channel 2",
//                "VSK, Channel 3",
//                "VIS, Blue",
//                "VIS, Minus Blue",
//                "VIS, Violet",
//                "VIS, Clear",
//                "VIS, Green",
//                "VIS, Red",
//        };
//        c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                "Phobos 2",
//                "Viking Orbiter 1-A",
//                "Viking Orbiter 1-B",
//                "Viking Orbiter 2-A",
//                "Viking Orbiter 2-B",
//                "MEX HRSC",
//                "MRO HiRISE",
//                "MGS MOC"
//        };
//        c.hasHierarchicalImageSearch = true;
//        c.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 300.0;

        lidarConfig.hasLidarData = true;
        lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        lidarConfig.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
        lidarConfig.lidarBrowseIsLidarInSphericalCoordinates = true;
        lidarConfig.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
        lidarConfig.lidarBrowseIsTimeInET = true;
        lidarConfig.lidarBrowseTimeIndex = 5;
        lidarConfig.lidarBrowseNoiseIndex = -1;
        lidarConfig.lidarBrowseIsRangeExplicitInData = true;
        lidarConfig.lidarBrowseRangeIndex = 3;
        lidarConfig.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
        lidarConfig.lidarBrowseNumberHeaderLines = 1;
        lidarConfig.lidarBrowseIsInMeters = true;
        lidarConfig.lidarOffsetScale = 0.025;
        lidarConfig.lidarInstrumentName = Instrument.MOLA;

        // MOLA search is disabled for now. See LidarPanel class.
        lidarConfig.hasHypertreeBasedLidarSearch = true;
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/sbmt2/sbmt/data/bodies/phobos/ernst2018/imaging/imagelist-fullpath.txt", "phobos_ernst_2018"),
        };



        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void configureDeimosThomas(ConfigArrayList<IBodyViewConfig> configArray)
	{
		MarsConfigs c = new MarsConfigs();
		c.body = ShapeModelBody.DEIMOS;
		c.type = BodyType.PLANETS_AND_SATELLITES;
		c.population = ShapeModelPopulation.MARS;
		c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
		c.author = ShapeModelType.THOMAS;
		c.modelLabel = "Thomas (2000)";
		c.rootDirOnServer = "/THOMAS/DEIMOS";
		c.shapeModelFileNames = prepend(c.rootDirOnServer, "DEIMOS.vtk.gz");
//	        c.hasStateHistory = true;
//	        c.timeHistoryFile = "/DEIMOS/history/TimeHistory.bth";

//	        c.hasImageMap = true;
		c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
		c.setResolution(ImmutableList.of(49152));
		c.presentInMissions = new Mission[]
		{ Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL,
				Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
		c.defaultForMissions = new Mission[]
		{};

		configArray.add(c);
	}

	@SuppressWarnings("unused")
	private static void configureDeimosExperimental(ConfigArrayList<IBodyViewConfig> configArray)
    {
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.DEIMOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.BLENDER;
        c.modelLabel = "OLD Ernst et al. (in progress)";
        c.rootDirOnServer = "/THOMAS/DEIMOSEXPERIMENTAL";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "DEIMOS.vtk.gz");
//        c.hasImageMap = true;
//        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));

        DataQuerySourcesMetadata deimosMetadata =
        		DataQuerySourcesMetadata.of("/THOMAS/DEIMOSEXPERIMENTAL/IMAGING", "", "DEIMOS", "DEIMOS", "/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/viking/gallery");
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(deimosMetadata),
//                        new GenericPhpQuery("/THOMAS/DEIMOSEXPERIMENTAL/IMAGING", "DEIMOS", "/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/viking/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "VIS, Blue",
                "VIS, Minus Blue",
                "VIS, Violet",
                "VIS, Clear",
                "VIS, Green",
                "VIS, Red",
        };

        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] { "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 800.0;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.DEIMOS.toString(), "/project/nearsdc/data/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/imagelist-fullpath.txt", "deimos"),
        };

//        configArray.add(c);
    }

	private static void configureDeimosErnst2018(ConfigArrayList<IBodyViewConfig> configArray)
    {
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.DEIMOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.EXPERIMENTAL;
        c.modelLabel = "Ernst et al. (in progress)";
        c.rootDirOnServer = "/deimos/ernst2018";
        c.shapeModelFileExtension = ".obj";
//        c.hasImageMap = true;
//        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));

        DataQuerySourcesMetadata deimosMetadata = DataQuerySourcesMetadata.of("/deimos/ernst2018/imaging", "", null, null, "/deimos/ernst2018/imaging/gallery");
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(deimosMetadata),
//                        new GenericPhpQuery("/deimos/ernst2018/imaging", "DEIMOS_ERNST_2018", "/deimos/ernst2018/imaging/gallery"), //
//                        new FixedListQuery<>("/deimos/ernst2018/imaging", "/deimos/ernst2018/imaging/gallery"), //
                        ImageType.valueOf("MARS_MOON_IMAGE"), //
                        new PointingSource[]{ PointingSource.GASKELL }, //
                        Instrument.IMAGING_DATA, //
                        0., //
                        "X" // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                        ) //
        );
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
//        c.imageSearchFilterNames = new String[]{
//                "VIS, Blue",
//                "VIS, Minus Blue",
//                "VIS, Violet",
//                "VIS, Clear",
//                "VIS, Green",
//                "VIS, Red",
//        };
//
//        c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 800.0;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.DEIMOS.toString(), "/project/sbmt2/sbmt/data/bodies/deimos/ernst2018/imaging/imagelist-fullpath.txt", "deimos_ernst_2018"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
    }

	private static void configurePhobosErnst2018Megane(ConfigArrayList<IBodyViewConfig> configArray, boolean publicOnly)
    {
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.EXPERIMENTAL;
        c.modelLabel = "Ernst et al. (in progress)";
        c.version = "with MEGANE";
        c.rootDirOnServer = "/phobos/ernst2018-megane";
        c.shapeModelFileExtension = ".obj";

        setupFeatures(c);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = c.rootDirOnServer + "/shared/history/timeHistory.bth";


        DataQuerySourcesMetadata phobosMetadata = DataQuerySourcesMetadata.of("/phobos/ernst2018-megane/imaging", "", null, null, "/phobos/ernst2018-megane/imaging/gallery");
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument(
                        SpectralImageMode.MONO,
                        new FixedListDataQuery(phobosMetadata),
//                        new FixedListQuery<>("/phobos/ernst2018-megane/imaging", "/phobos/ernst2018-megane/imaging/gallery"),
                        ImageType.valueOf("MARS_MOON_IMAGE"),
                        new PointingSource[]{ PointingSource.GASKELL },
                        Instrument.IMAGING_DATA,
                        0.,
                        "X" // Note: this means "flip along X axis". Don't know why, but this flip is needed as of this delivery.
                        )
        );

        spectrumConfig.hasSpectralData=true;
        spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
        spectrumConfig.spectralInstruments.add(new MEGANE());

        c.hasMapmaker = true;
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime();

        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 300.0;

        lidarConfig.hasLidarData = true;
        lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        lidarConfig.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
        lidarConfig.lidarBrowseIsLidarInSphericalCoordinates = true;
        lidarConfig.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
        lidarConfig.lidarBrowseIsTimeInET = true;
        lidarConfig.lidarBrowseTimeIndex = 5;
        lidarConfig.lidarBrowseNoiseIndex = -1;
        lidarConfig.lidarBrowseIsRangeExplicitInData = true;
        lidarConfig.lidarBrowseRangeIndex = 3;
        lidarConfig.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
        lidarConfig.lidarBrowseNumberHeaderLines = 1;
        lidarConfig.lidarBrowseIsInMeters = true;
        lidarConfig.lidarOffsetScale = 0.025;
        lidarConfig.lidarInstrumentName = Instrument.MOLA;

        // MOLA search is disabled for now. See LidarPanel class.
        lidarConfig.hasHypertreeBasedLidarSearch = true;
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");


        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
        stateHistoryConfig.stateHistoryStartDate = new GregorianCalendar(2026, 1, 1, 0, 0, 0).getTime();
        stateHistoryConfig.stateHistoryEndDate = new GregorianCalendar(2026, 9, 30, 0, 0, 0).getTime();
        stateHistoryConfig.spiceInfo = new SpiceInfo("MMX", "IAU_PHOBOS", "MMX_SPACECRAFT", "PHOBOS",
        				new String[] {"EARTH" , "SUN", "MARS"},
        				new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_MARS"},
        				new String[] {"MMX_MEGANE"},
        				new String[] {});

        c.presentInMissions = new Mission[] {Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
        														  Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
        														  Mission.MEGANE_TEST};
        c.defaultForMissions = new Mission[] {Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
				  													Mission.MEGANE_TEST};

        if (!publicOnly)
        	configArray.add(c);
    }

	private static void configurePhobosErnst2023(ConfigArrayList<IBodyViewConfig> configArray)
    {
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.provide("ernst-et-al-2023");
        c.modelLabel = "Ernst et al. 2023";
        c.rootDirOnServer = "/phobos/ernst-et-al-2023";
        c.shapeModelFileExtension = ".obj";
//        c.hasImageMap = false;
        c.presentInMissions = new Mission[] { //
                Mission.APL_INTERNAL, //
                Mission.TEST_APL_INTERNAL, //
                Mission.STAGE_APL_INTERNAL, //
                Mission.PUBLIC_RELEASE, //
                Mission.TEST_PUBLIC_RELEASE, //
                Mission.STAGE_PUBLIC_RELEASE //
        };
        c.setResolution( //
                ImmutableList.of( //
                        DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], //
                        DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], //
                        DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], //
                        DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3], //
                		"Extremely High (12582912 plates)"),
                ImmutableList.of( //
                        DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], //
                        DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], //
                        DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], //
                        DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3], //
                        12582912));
        configArray.add(c);
	}

	private static void configureDeimosErnst2023(ConfigArrayList<IBodyViewConfig> configArray)
    {
		MarsConfigs c = new MarsConfigs();
        c.body = ShapeModelBody.DEIMOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.provide("ernst-et-al-2023");
        c.modelLabel = "Ernst et al. 2023";
        c.rootDirOnServer = "/deimos/ernst-et-al-2023";
        c.shapeModelFileExtension = ".obj";
//        c.hasImageMap = false;
        c.presentInMissions = new Mission[] { //
                Mission.APL_INTERNAL, //
                Mission.TEST_APL_INTERNAL, //
                Mission.STAGE_APL_INTERNAL, //
                Mission.PUBLIC_RELEASE, //
                Mission.TEST_PUBLIC_RELEASE, //
                Mission.STAGE_PUBLIC_RELEASE //
        };
        configArray.add(c);
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