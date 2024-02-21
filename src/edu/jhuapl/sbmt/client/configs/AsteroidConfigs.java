package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
import edu.jhuapl.sbmt.image.model.BinExtents;
import edu.jhuapl.sbmt.image.model.BinSpacings;
import edu.jhuapl.sbmt.image.model.BinTranslations;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.model.ImageFlip;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.OrientationFactory;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class AsteroidConfigs extends SmallBodyViewConfig
{


	public AsteroidConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}

	private static void setupFeatures(AsteroidConfigs c)
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
        buildErosGaskellConfig(configArray);
       	buildErosThomasConfig(configArray);
       	buildErosNLRConfig(configArray);
       	buildErosNAVConfig(configArray);
		buildItokawaGaskellConfig(configArray);
		buildItokawaHudsonConfig(configArray);
		buildToutatisHudsonConfig(configArray);
		buildCeresGaskellConfig(configArray);
		buildVestaGaskellConfig(configArray);
		buildVestaThomasConfig(configArray);
		buildLutetiaGaskellConfig(configArray);
		buildLutetiaJordaConfig(configArray);
		buildIdaThomasConfig(configArray);
		buildIdaStookeConfig(configArray);
		buildMathildeThomasConfig(configArray);
		buildMathildeStookeConfig(configArray);
		buildGaspraThomasConfig(configArray);
		buildGaspraStookeConfig(configArray);
		buildSteinsJordaConfig(configArray);
		buildPsycheHanusConfig(configArray);
		buildPsycheViikinjoskiConfig(configArray);

    }

	private static void configureErosLidar(LidarInstrumentConfig lidarConfig)
	{
		lidarConfig.hasLidarData = true;
		lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarBrowseDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
        lidarConfig.lidarBrowseXYZIndices = new int[] { 14, 15, 16 };
        lidarConfig.lidarBrowseSpacecraftIndices = new int[] { 8, 9, 10 };
        lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
        lidarConfig.lidarBrowseTimeIndex = 4;
        lidarConfig.lidarBrowseNoiseIndex = 7;
        lidarConfig.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/NlrFiles.txt";
        lidarConfig.lidarBrowseNumberHeaderLines = 2;
        lidarConfig.lidarBrowseIsInMeters = true;
        lidarConfig.lidarOffsetScale = 0.025;
        lidarConfig.lidarInstrumentName = Instrument.NLR;
	}

	private static void configureErosSpectrum(SpectrumInstrumentConfig spectrumConfig)
	{
		spectrumConfig.hasSpectralData = true;
        spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
        spectrumConfig.spectralInstruments.add(new NIS());

    	List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> instrumentSearchSpecs = new ArrayList<SpectrumInstrumentMetadata<SpectrumSearchSpec>>();
        SpectrumSearchSpec nisSpec = new SpectrumSearchSpec("NIS Calibrated Spectrum", "/GASKELL/EROS/shared/nis", "spectra", "spectrumlist.txt", PointingSource.valueFor("Corrected SPICE Derived"), "Wavelength (nm)", "Reflectance", "NIS");
		List<SpectrumSearchSpec> nisSpecs = new ArrayList<SpectrumSearchSpec>();
		nisSpecs.add(nisSpec);

		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("NIS", nisSpecs));
        spectrumConfig.hierarchicalSpectraSearchSpecification = new SpectrumInstrumentMetadataIO("NEAR", instrumentSearchSpecs);
	}

	private static void configureErosImaging(ImagingInstrumentConfig imagingConfig)
	{
		DataQuerySourcesMetadata msiMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/EROS/MSI", "", "EROS", "EROS", "/GASKELL/EROS/MSI/gallery");
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(msiMetadata),
                        ImageType.MSI_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL_UPDATED, PointingSource.SPICE}, //
                        Instrument.MSI,
                        0.0,
                        "None",
                        null,
                        new int[] {537, 412},
                        new int[] {16, 16, 16, 16}
                        )
        );
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] { "iofdbl", "cifdbl" };
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 50.0;
	}

	private static void buildErosGaskellConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2008)";
        c.rootDirOnServer = "/GASKELL/EROS";

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        stateHistoryConfig.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth";
//        c.hasImageMap = true;

        stateHistoryConfig.hasStateHistory = true;
        c.shapeModelFileNames = prepend("/EROS", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");

        configureErosImaging(imagingConfig);
        configureErosSpectrum(spectrumConfig);
        configureErosLidar(lidarConfig);

        c.hasMapmaker = true;
        c.hasRemoteMapmaker = false;
        c.density = 2.67;
        c.rotationRate = 0.000331165761670640;
        c.bodyReferencePotential = -53.765039959572114;
        c.bodyLowestResModelName = "EROS/shape/shape0.obj";
        c.hasLineamentData = true;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MSI, ShapeModelBody.EROS.toString(), "/project/nearsdc/data/GASKELL/EROS/MSI/msiImageList.txt", "eros"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
        															Mission.STAGE_PUBLIC_RELEASE,
        															Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
        															Mission.STAGE_APL_INTERNAL,
        															Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV,
        															Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST,
        															Mission.OSIRIS_REX_DEPLOY, Mission.OSIRIS_REX_MIRROR_DEPLOY,
        															Mission.NH_DEPLOY,
        															Mission.DART_DEV,
        															Mission.DART_DEPLOY,
        															Mission.DART_TEST,
        															Mission.DART_STAGE};

        c.defaultForMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
        														Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL,
        														Mission.TEST_APL_INTERNAL};

        configArray.add(c);

	}

	//bring the above attributes down to the next 3
	private static void buildErosThomasConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
    // Thomas Eros
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
	    c.author = ShapeModelType.THOMAS;
	    c.modelLabel = "Thomas et al. (2001)";
	    c.rootDirOnServer = "/THOMAS/EROS";

	    c.shapeModelFileNames = prepend(c.rootDirOnServer, "eros001708.obj.gz", "eros007790.obj.gz", "eros010152.obj.gz", "eros022540.obj.gz", "eros089398.obj.gz", "eros200700.obj.gz");

	    setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

	    configureErosImaging(imagingConfig);
        configureErosSpectrum(spectrumConfig);
        configureErosLidar(lidarConfig);


	    stateHistoryConfig.hasStateHistory = true;
	    stateHistoryConfig.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory

	    c.setResolution(ImmutableList.of( //
	            "1708 plates", "7790 plates", "10152 plates", //
	            "22540 plates", "89398 plates", "200700 plates" //
	    ), ImmutableList.of( //
	            1708, 7790, 10152, 22540, 89398, 200700 //
	    ));
	    c.hasMapmaker = false;
	    c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
	    															Mission.STAGE_PUBLIC_RELEASE,
	    															Mission.STAGE_APL_INTERNAL,
	    															Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
	    c.defaultForMissions = new Mission[] {};

	    configArray.add(c);
	}

	private static void buildErosNLRConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
     // Eros NLR
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNLR;
        c.modelLabel = "Neumann et al. (2001)";
        c.rootDirOnServer = "/OTHER/EROSNLR";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "nlrshape.llr2.gz");

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

	    configureErosImaging(imagingConfig);
        configureErosSpectrum(spectrumConfig);
        configureErosLidar(lidarConfig);

        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO
        c.setResolution(ImmutableList.of(129600));

        c.defaultForMissions = new Mission[] {};
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
        															Mission.STAGE_PUBLIC_RELEASE,
													        		Mission.STAGE_APL_INTERNAL,
													        		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};

        configArray.add(c);
	}

	private static void buildErosNAVConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
     // Eros NAV
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNAV;
        c.modelLabel = "NAV team (2001)";
        c.rootDirOnServer = "/OTHER/EROSNAV";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "navplate.obj.gz");

        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

	    configureErosImaging(imagingConfig);
        configureErosSpectrum(spectrumConfig);
        configureErosLidar(lidarConfig);

        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.setResolution(ImmutableList.of(56644));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
        																	Mission.STAGE_PUBLIC_RELEASE,
															        		Mission.STAGE_APL_INTERNAL,
															        		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

	}

	private static void buildItokawaGaskellConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        // Gaskell Itokawa

        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell et al. (2008)";
        c.rootDirOnServer = "/GASKELL/ITOKAWA";
        c.shapeModelFileNames = prepend("/ITOKAWA", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
        setupFeatures(c);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        stateHistoryConfig.hasStateHistory = true;
        stateHistoryConfig.timeHistoryFile = "/GASKELL/ITOKAWA/history/TimeHistory.bth";

        ImageBinPadding binPadding = new ImageBinPadding();
        binPadding.binExtents.put(1, new BinExtents(1024, 1024, 1024, 1024));
        binPadding.binTranslations.put(1,  new BinTranslations(0,0));
        binPadding.binSpacings.put(1,  new BinSpacings(1.0, 1.0, 1.0));

        binPadding.binExtents.put(2, new BinExtents(512, 512, 1024, 1024));
        binPadding.binTranslations.put(2,  new BinTranslations(0,0));
        binPadding.binSpacings.put(2,  new BinSpacings(2.0, 2.0, 1.0));

        binPadding.binExtents.put(4, new BinExtents(256, 256, 1024, 1024));
        binPadding.binTranslations.put(4,  new BinTranslations(0, 0));
        binPadding.binSpacings.put(4,  new BinSpacings(4.0, 4.0, 1.0));

        binPadding.binExtents.put(8, new BinExtents(128, 128, 1024, 1024));
        binPadding.binTranslations.put(8,  new BinTranslations(0, 0));
        binPadding.binSpacings.put(8,  new BinSpacings(8.0, 8.0, 1.0));

        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));
        c.addFeatureConfig(SpectrumInstrumentConfig.class, new SpectrumInstrumentConfig(c));
        c.addFeatureConfig(LidarInstrumentConfig.class, new LidarInstrumentConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        DataQuerySourcesMetadata amicaMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/ITOKAWA/AMICA", "", "AMICA", "AMICA", "/GASKELL/ITOKAWA/AMICA/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(amicaMetadata),
//                        new GenericPhpQuery("/GASKELL/ITOKAWA/AMICA", "AMICA", "/GASKELL/ITOKAWA/AMICA/gallery"), //
                        ImageType.AMICA_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE, PointingSource.CORRECTED}, //
                        Instrument.AMICA, //
                        0.0,
                        "None",
                        binPadding
                        ) //
        );

        lidarConfig.hasLidarData = true;
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        };
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 26.0;
        imagingConfig.imageSearchDefaultMaxResolution = 3.0;
        lidarConfig.lidarSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDefaultEndDate = new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime();
        lidarConfig.lidarSearchDataSourceMap = new LinkedHashMap<>();
        lidarConfig.lidarSearchDataSourceMap.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        lidarConfig.lidarSearchDataSourceMap.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        lidarConfig.lidarBrowseXYZIndices = new int[] { 6, 7, 8 };
        lidarConfig.lidarBrowseSpacecraftIndices = new int[] { 3, 4, 5 };
        lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
        lidarConfig.lidarBrowseTimeIndex = 1;
        lidarConfig.lidarBrowseNoiseIndex = -1;
        lidarConfig.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/HayLidarFiles.txt";
        lidarConfig.lidarBrowseNumberHeaderLines = 0;
        lidarConfig.lidarBrowseIsInMeters = false;
        // The following value is the Itokawa diagonal length divided by
        // 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is
        // 0.025 km.
        lidarConfig.lidarOffsetScale = 0.00044228259621279913;
        lidarConfig.lidarInstrumentName = Instrument.LIDAR;

        spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.AMICA, ShapeModelBody.ITOKAWA.toString(), "/project/nearsdc/data/GASKELL/ITOKAWA/AMICA/imagelist.txt", "amica"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
        															Mission.STAGE_PUBLIC_RELEASE,
        															Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
        															Mission.STAGE_APL_INTERNAL,
        															Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV,
        															Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST,
        															Mission.OSIRIS_REX_DEPLOY,
        															Mission.OSIRIS_REX_MIRROR_DEPLOY, Mission.NH_DEPLOY};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildItokawaHudsonConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		 // Ostro Itokawa
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.OSTRO;
        c.modelLabel = "Ostro et al. (2004)";
        c.rootDirOnServer = "/HUDSON/ITOKAWA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "25143itokawa.obj.gz");
        c.setResolution(ImmutableList.of(12192));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

	}

	private static void buildToutatisHudsonConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.TOUTATIS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Hudson et al. (2004)";
        c.rootDirOnServer = "/toutatis/hudson";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of("Low (12796 plates)", "High (39996 plates)"), ImmutableList.of(12796, 39996));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildCeresGaskellConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();

        c.body = ShapeModelBody.CERES;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "SPC";
        c.rootDirOnServer = "/GASKELL/CERES";
        c.hasMapmaker = true;
        setupFeatures(c);
        Map<PointingSource, Orientation> ceresOrientations = new LinkedHashMap<>();
        ceresOrientations.put(PointingSource.GASKELL, new OrientationFactory().of(ImageFlip.X, 0.0, true));
        ceresOrientations.put(PointingSource.SPICE, new OrientationFactory().of(ImageFlip.X, 0.0, true));

        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        DataQuerySourcesMetadata fcMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/CERES/FC", "", "Ceres", "Ceres", "/GASKELL/CERES/FC/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(fcMetadata),
                        ImageType.FCCERES_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE}, //
                        Instrument.FC, //
                        ceresOrientations
                ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2015, GregorianCalendar.APRIL, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, GregorianCalendar.JULY, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.FC, ShapeModelBody.CERES.toString(), "/project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt", "ceres"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
    }

	private static void buildVestaGaskellConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013)";
        c.rootDirOnServer = "/GASKELL/VESTA";
        c.hasMapmaker = true;
        setupFeatures(c);
        Map<PointingSource, Orientation> vestaOrientations = new LinkedHashMap<>();
        vestaOrientations.put(PointingSource.SPICE, new OrientationFactory().of(ImageFlip.X, 0.0, true));

        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        DataQuerySourcesMetadata fcMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/VESTA/FC", "", "FC", "FC", "/GASKELL/VESTA/FC/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(fcMetadata),
                        ImageType.FC_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE}, //
                        Instrument.FC, //
                        0.0,
                        "X"
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.FC, ShapeModelBody.VESTA.toString(), "/project/nearsdc/data/GASKELL/VESTA/FC/uniqFcFiles.txt", "fc"),
        };

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildVestaThomasConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/VESTA_OLD";
        c.shapeModelFileNames = new String[] { "/VESTA_OLD/VESTA.vtk.gz" };
        c.setResolution(ImmutableList.of(49152));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildLutetiaGaskellConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.LUTETIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "SPC";
        c.rootDirOnServer = "/GASKELL/LUTETIA";
        setupFeatures(c);
        ImageBinPadding binPadding = new ImageBinPadding();
        binPadding.binExtents.put(1, new BinExtents(2048, 2048, 2048, 2048));
        binPadding.binTranslations.put(1,  new BinTranslations(559, 575));
        binPadding.binSpacings.put(1,  new BinSpacings(1.0, 1.0, 1.0));

        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        DataQuerySourcesMetadata osirisMetadata = DataQuerySourcesMetadata.of("/GASKELL/LUTETIA/IMAGING", "", "", "/GASKELL/LUTETIA/IMAGING/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(osirisMetadata),
                        ImageType.OSIRIS_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.OSIRIS, //,
                        0.0,
                        "Y",
                        binPadding
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildLutetiaJordaConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.LUTETIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham et al. (2013)";
        c.rootDirOnServer = "/JORDA/LUTETIA";
        c.setResolution(ImmutableList.of( //
                "2962 plates ", "5824 plates ", "11954 plates ", "24526 plates ", //
                "47784 plates ", "98280 plates ", "189724 plates ", "244128 plates ", //
                "382620 plates ", "784510 plates ", "1586194 plates ", "3145728 plates" //
            ), ImmutableList.of( //
                2962, 5824, 11954, 24526, 47784, 98280, 189724, //
                244128, 382620, 784510, 1586194, 3145728 //
            )); //
        c.shapeModelFileNames = prepend(c.rootDirOnServer,
                "shape_res0.vtk.gz", //
                "shape_res1.vtk.gz", //
                "shape_res2.vtk.gz", //
                "shape_res3.vtk.gz", //
                "shape_res4.vtk.gz", //
                "shape_res5.vtk.gz", //
                "shape_res6.vtk.gz", //
                "shape_res7.vtk.gz", //
                "shape_res8.vtk.gz", //
                "shape_res9.vtk.gz", //
                "shape_res10.vtk.gz", //
                "shape_res11.vtk.gz" //
            );
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildIdaThomasConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/IDA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "243ida.llr.gz");
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.setResolution(ImmutableList.of(32040));

        /*c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListQuery("/THOMAS/IDA/SSI", "/THOMAS/IDA/SSI/images/gallery"), //
                        ImageType.SSI_IDA_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.SSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1993, 7, 28, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1993, 7, 29, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        */
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildIdaStookeConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// This model was delivered on 2018-03-08 to replace the previous model of
        // unknown specific origin.
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/ida/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of(5040));
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2600.;
        c.rotationRate = 0.0003766655;
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildMathildeThomasConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/MATHILDE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "253mathilde.llr.gz");
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.setResolution(ImmutableList.of(14160));

        /*c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListQuery("/THOMAS/MATHILDE/MSI", "/THOMAS/MATHILDE/MSI/images/gallery"), //
                        ImageType.MSI_MATHILDE_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.MSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1997, 5, 27, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1997, 5, 28, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        */
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildMathildeStookeConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		// This new model was delivered on 2018-03-08.
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/mathilde/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 1300.;
        c.rotationRate = 0.0000041780;
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.setResolution(ImmutableList.of(5040));

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

	}

	private static void buildGaspraThomasConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/GASPRA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "951gaspra.llr.gz");
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.setResolution(ImmutableList.of(32040));

        /*c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListQuery("/THOMAS/GASPRA/SSI", "/THOMAS/GASPRA/SSI/images/gallery"), //
                        ImageType.SSI_GASPRA_IMAGE, //
                        new ImageSource[]{ImageSource.CORRECTED}, //
                        Instrument.SSI //
                        ) //
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        */

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

	}

	private static void buildGaspraStookeConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		 // This model was delivered on 2018-03-08 to replace the previous model of
        // unknown specific origin.
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/gaspra/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2700.;
        c.rotationRate = 0.0002478;
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.setResolution(ImmutableList.of(5040));

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildSteinsJordaConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.STEINS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham and Jorda (2013)";
        c.rootDirOnServer = "/JORDA/STEINS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "steins_cart.plt.gz");
        c.setResolution(ImmutableList.of(20480));

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildPsycheHanusConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c = new AsteroidConfigs();
        c.body = ShapeModelBody.PSYCHE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.HANUS;
        c.modelLabel = "Hanus et al. (2013)";
        c.rootDirOnServer = "/psyche/hanus";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of(2038));

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}



	private static void buildPsycheViikinjoskiConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
        AsteroidConfigs c = new AsteroidConfigs();
        c.body = ShapeModelBody.PSYCHE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.VIIKINKOSKI;
        c.modelLabel = "Viikinkoski et al. (2018)";
        c.rootDirOnServer = "/psyche/viikinkoski";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of(1352));

        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

    public AsteroidConfigs clone() // throws CloneNotSupportedException
    {
        AsteroidConfigs c = (AsteroidConfigs) super.clone();

        return c;
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