package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.GregorianCalendar;

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
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class NewHorizonsConfigs extends SmallBodyViewConfig
{

	public NewHorizonsConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}

	private static void setupFeatures(NewHorizonsConfigs c)
	{
		c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));
        c.addFeatureConfig(SpectrumInstrumentConfig.class, new SpectrumInstrumentConfig(c));
        c.addFeatureConfig(LidarInstrumentConfig.class, new LidarInstrumentConfig(c));
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.addFeatureConfig(StateHistoryConfig.class, new StateHistoryConfig(c));

        FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig(c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig(c);
	}

	private static Mission[] presentMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
			Mission.NH_DEPLOY};


	public static void initialize(ConfigArrayList<IBodyViewConfig> configArray)
    {
        NewHorizonsConfigs c = new NewHorizonsConfigs();


        /* Older unused configs

        buildAmaltheaConfig(configArray);
        buildCallistoConfig(configArray);
        buildEuropaConfig(configArray);
        buildGanymedeConfig(configArray);
        buildIOConfig(configArray);
        buildJupiterConfig(configArray);
        */

        buildPlutoConfig(configArray);

        buildPlutoTestConfig(configArray);

        buildCharonConfig(configArray);

        buildHydraConfig(configArray);

        buildKerberosConfig(configArray);

        buildNyxConfig(configArray);

        buildStyxConfig(configArray);

    	buildMU69Config(configArray);

    }

	private static void configurePlutoEncounterImaging(ImagingInstrumentConfig imagingConfig, DataQuerySourcesMetadata lorriMetadata,
													   DataQuerySourcesMetadata mvicMetadata, DataQuerySourcesMetadata leisaMetadata, PointingSource[] lorriPointingSource)
	{
		ImagingInstrument lorriInstrument = null;
		ImagingInstrument mvicInstrument = null;
		ImagingInstrument leisaInstrument = null;	//currently unused but will be eventually
		imagingConfig.imagingInstruments = Lists.newArrayList();
		if (lorriMetadata != null)
		{
			 lorriInstrument = new ImagingInstrument( //
                     SpectralImageMode.MONO, //
                     new ImageDataQuery(lorriMetadata),
                     ImageType.LORRI_IMAGE, //
                     lorriPointingSource, //
                     Instrument.LORRI //
                     );
			 imagingConfig.imagingInstruments.add(lorriInstrument);
		}
		if (mvicMetadata != null)
		{
			 mvicInstrument = new ImagingInstrument( //
                     SpectralImageMode.MULTI, //
                     new ImageDataQuery(mvicMetadata),
                     ImageType.MVIC_JUPITER_IMAGE, //
                     new PointingSource[]{PointingSource.SPICE}, //
                     Instrument.MVIC
                     );
			 imagingConfig.imagingInstruments.add(mvicInstrument);
		}
		if (leisaMetadata != null)
		{
//			 leisaInstrument = new ImagingInstrument( //
//                     SpectralImageMode.MONO, //
//                     new FixedListDataQuery(lorriMetadata),
//                     ImageType.LORRI_IMAGE, //
//                     new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED, PointingSource.CORRECTED_SPICE}, //
//                     Instrument.LORRI //
//                     );
//			imagingConfig.imagingInstruments.add(leisaInstrument);
		}

		imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
	}

	private static void buildPlutoConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.PLUTO;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.NIMMO;
        c.modelLabel = "Nimmo et al. (2017)";
        c.rootDirOnServer = "/NEWHORIZONS/PLUTO";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
        c.hasColoringData = false;
        setupFeatures(c);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/IMAGING", "", null, null, null);
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/MVIC", "", null, null, null);
        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
        								new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED, PointingSource.CORRECTED_SPICE});

        c.setResolution(ImmutableList.of(128880));

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), "/project/nearsdc/data/NEWHORIZONS/PLUTO/IMAGING/imagelist-fullpath.txt", ShapeModelBody.PLUTO.toString().toLowerCase()),
        };

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildPlutoTestConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.PLUTO;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.provide("pluto-test");
        c.modelLabel = "Pluto (Test)";
        c.rootDirOnServer = "/pluto/pluto-test";
        c.shapeModelFileNames = null;
        c.shapeModelFileExtension = ".obj";

        c.hasColoringData = false;
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        DataQuerySourcesMetadata lorriMetadata = DataQuerySourcesMetadata.of(c.rootDirOnServer + "/lorri", "/new-horizons/lorri/pluto-test/images", "pluto_pluto_test_lorri", null, null);
//        mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/PLUTO/MVIC", "", null, null, null);

        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, null, null, new PointingSource[]{PointingSource.GASKELL});

        c.setResolution(ImmutableList.of(128880));

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), c.rootDirOnServer + "/lorri" + "/imagelist-fullpath-sum.txt", ShapeModelBody.PLUTO.toString().toLowerCase()),
        };

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildCharonConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.CHARON;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.NIMMO;
        c.modelLabel = "Nimmo et al. (2017)";
        c.rootDirOnServer = "/NEWHORIZONS/CHARON";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
        c.hasColoringData = false;
        setupFeatures(c);
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/CHARON/IMAGING", "", null, null);
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/CHARON/MVIC", "", null, null, null);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

        c.setResolution(ImmutableList.of(128880));

        c.databaseRunInfos = new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-sum.txt", "charon_nimmo2017_lorri"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-info.txt", "charon_nimmo2017_lorri"),
        };

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildHydraConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.HYDRA;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.WEAVER;
        c.modelLabel = "Weaver et al. (2016)";
//        c.pathOnServer = "/NEWHORIZONS/HYDRA/shape_res0.vtk.gz";
        c.rootDirOnServer = "/NEWHORIZONS/HYDRA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
        c.hasColoringData = false;
        setupFeatures(c);
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/IMAGING", "", null, null, null);
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/MVIC", "", null, null, null);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

        c.setResolution(ImmutableList.of(128880));

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildKerberosConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		 c.body = ShapeModelBody.KERBEROS;
         c.type = BodyType.KBO;
         c.population = ShapeModelPopulation.PLUTO;
         c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
         c.author = ShapeModelType.WEAVER;
         c.modelLabel = "Weaver et al. (2016)";
         c.rootDirOnServer = "/NEWHORIZONS/KERBEROS";
         c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
         c.hasColoringData = false;
         c.setResolution(ImmutableList.of(128880));

         c.presentInMissions = presentMissions;
         c.defaultForMissions = new Mission[] {};

         configArray.add(c);
	}

	private static void buildNyxConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.NIX;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.WEAVER;
        c.modelLabel = "Weaver et al. (2016)";
        c.rootDirOnServer = "/NEWHORIZONS/NIX";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.obj.gz");
        c.hasColoringData = false;
        setupFeatures(c);
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/IMAGING", "", null, null, null);
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/MVIC", "", null, null, null);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

        c.setResolution(ImmutableList.of(128880));

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildStyxConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
		c.body = ShapeModelBody.STYX;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.PLUTO;
        c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
        c.author = ShapeModelType.WEAVER;
        c.modelLabel = "Weaver et al. (2016)";
        c.rootDirOnServer = "/NEWHORIZONS/STYX";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(128880));

        c.presentInMissions = presentMissions;
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);
	}

	private static void buildMU69Config(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.MU69;
        c.type = BodyType.KBO;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED;
        c.rootDirOnServer = "/mu69/mu69-test5h-1-final-oriented";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of("Very Low (25708 plates)"), ImmutableList.of(25708));
        setupFeatures(c);

        c.density = Double.NaN;
        c.useMinimumReferencePotential = true;
        c.rotationRate = Double.NaN;

        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/lorri", "", null, null, c.rootDirOnServer + "/lorri/gallery");
    	ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
    	configurePlutoEncounterImaging(imagingConfig, lorriMetadata, null, null,
				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

    	//override base settings with MU69 values
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2018, 11, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2019, 0, 2, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e4;

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        spectrumConfig.hasSpectralData = false;
        spectrumConfig.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();


        c.hasStateHistory = false;

        c.hasMapmaker = false;
        spectrumConfig.hasHierarchicalSpectraSearch = false;
        spectrumConfig.hasHypertreeBasedSpectraSearch = false;

        lidarConfig.hasLidarData = false;
        lidarConfig.hasHypertreeBasedLidarSearch = false;

        c.presentInMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY};
        c.defaultForMissions = new Mission[] {Mission.NH_DEPLOY};

        configArray.add(c);
    }

	private static void buildJupiterConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.JUPITER;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = null;
        c.author = null;
        c.rootDirOnServer = "/NEWHORIZONS/JUPITER";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
        c.hasColoringData = false;
//        c.hasImageMap = false;
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        // imaging instruments

        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/JUPITER/IMAGING", "", "JUPITER", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery");
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/JUPITER/MVIC", "", null, null, null);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument(SpectralImageMode.MONO,
                		new ImageDataQuery(lorriMetadata),
                		ImageType.LORRI_IMAGE,
                		new PointingSource[] { PointingSource.SPICE },
                		Instrument.LORRI),

                new ImagingInstrument( //
                        SpectralImageMode.MULTI, //
                        new FixedListDataQuery(mvicMetadata),
//                        new FixedListQuery("/NEWHORIZONS/JUPITER/MVIC"), //
                        ImageType.MVIC_JUPITER_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.MVIC //
//                        ), //
//
//                new ImagingInstrument( //
//                        SpectralMode.HYPER, //
//                        new FixedListQuery("/NEWHORIZONS/JUPITER/LEISA"), //
//                        ImageType.LEISA_JUPITER_IMAGE, //
//                        new ImageSource[]{ImageSource.SPICE}, //
//                        Instrument.LEISA //
                        ) //
                ); //

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
        configArray.add(c);
    }

	private static void buildAmaltheaConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.AMALTHEA;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        // 2017-12-20: this name will be correct when "the new model" has been brought
        // in.
        // c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/AMALTHEA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "j5amalthea.llr.gz");
        configArray.add(c);
	}

	private static void buildCallistoConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.CALLISTO;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = null;
        c.author = null;
        c.rootDirOnServer = "/NEWHORIZONS/CALLISTO";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//        c.hasImageMap = true;
        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        setupFeatures(c);
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/CALLISTO/IMAGING", "", "CALLISTO", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery");

        // imaging instruments
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(lorriMetadata),
//                        new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery"), //
                        ImageType.LORRI_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.LORRI //
                        ) //
        );

        configArray.add(c);
    }

	private static void buildEuropaConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.EUROPA;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = null;
        c.author = null;
        c.rootDirOnServer = "/NEWHORIZONS/EUROPA";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");

        c.hasFlybyData = true;
        setupFeatures(c);
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/EUROPA/IMAGING", "", "EUROPA", "EUROPA", "/NEWHORIZONS/EUROPA/IMAGING/images/gallery");
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/EUROPA/MVIC", "", null, null, null);

        // imaging instruments
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(lorriMetadata),
                        ImageType.LORRI_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.LORRI //
                        ), //

                new ImagingInstrument( //
                        SpectralImageMode.MULTI, //
                        new FixedListDataQuery(mvicMetadata),
//                        new FixedListQuery("/NEWHORIZONS/EUROPA/MVIC"), //
                        ImageType.MVIC_JUPITER_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.MVIC //
                        ) //
                );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
        configArray.add(c);
    }

	private static void buildGanymedeConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.GANYMEDE;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = null;
        c.author = null;
        c.rootDirOnServer = "/NEWHORIZONS/GANYMEDE";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//        c.hasImageMap = true;
        setupFeatures(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/GANYMEDE/IMAGING", "", "GANYMEDE", "GANYMEDE", "/NEWHORIZONS/GANYMEDE/IMAGING/images/gallery");
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/GANYMEDE/MVIC", "", null, null, null);

        // imaging instruments
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(lorriMetadata),
                        ImageType.LORRI_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.LORRI //
                        ), //

                new ImagingInstrument( //
                        SpectralImageMode.MULTI, //
                        new FixedListDataQuery(mvicMetadata),
                        ImageType.MVIC_JUPITER_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.MVIC //
                        ) //
                ); //

        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
        configArray.add(c);
    }

	private static void buildIOConfig(ConfigArrayList<IBodyViewConfig> configArray)
	{
		NewHorizonsConfigs c = new NewHorizonsConfigs();
        c.body = ShapeModelBody.IO;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = null;
        c.author = null;
        c.rootDirOnServer = "/NEWHORIZONS/IO";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "shape_res0.vtk.gz");
//        c.hasImageMap = true;
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

        // imaging instruments
        DataQuerySourcesMetadata lorriMetadata =
        		DataQuerySourcesMetadata.of("/NEWHORIZONS/IO/IMAGING", "", "IO", "IO", "/NEWHORIZONS/IO/IMAGING/images/gallery");
        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/IO/MVIC", "", null, null, null);

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new ImageDataQuery(lorriMetadata),
                        ImageType.LORRI_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.LORRI //
                        ), //

                new ImagingInstrument( //
                        SpectralImageMode.MULTI, //
                        new FixedListDataQuery(mvicMetadata),
                        ImageType.MVIC_JUPITER_IMAGE, //
                        new PointingSource[]{PointingSource.SPICE}, //
                        Instrument.MVIC //
                        ) //
                );

        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
        imagingConfig.imageSearchDefaultMaxResolution = 1.0e6;
        configArray.add(c);
    }

    public NewHorizonsConfigs clone() // throws CloneNotSupportedException
    {
        NewHorizonsConfigs c = (NewHorizonsConfigs) super.clone();

        return c;
    }

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }
}
