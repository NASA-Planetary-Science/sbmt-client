package edu.jhuapl.sbmt.client2.configs;

import java.util.GregorianCalendar;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.config.ConfigArrayList;
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
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.query.v2.IDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;

public class SaturnConfigs extends SmallBodyViewConfig
{
	private static final PointingSource[] SumFiles = new PointingSource[] { PointingSource.GASKELL };

    public SaturnConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
	}

	private static void setupFeatures(SaturnConfigs c)
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
        SaturnConfigs c = new SaturnConfigs();

        c = new SaturnConfigs();
        setupFeatures(c);
        c.body = ShapeModelBody.DIONE;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013a)";
        c.rootDirOnServer = "/GASKELL/DIONE";


        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        DataQuerySourcesMetadata imagingMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/DIONE/IMAGING", "", null, null, "/GASKELL/DIONE/IMAGING/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(imagingMetadata),
//                        new FixedListQuery<Object>("/GASKELL/DIONE/IMAGING", "/GASKELL/DIONE/IMAGING/gallery"), //
                        ImageType.SATURN_MOON_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new SaturnConfigs();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/EPIMETHEUS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "s11epimetheus.llr.gz");
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SaturnConfigs();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/epimetheus/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SaturnConfigs();
            c.body = ShapeModelBody.HYPERION;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/HYPERION";
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        c = new SaturnConfigs();
        c.body = ShapeModelBody.HYPERION;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/HYPERION";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "s7hyperion.llr.gz");
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new SaturnConfigs();
        c.body = ShapeModelBody.JANUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/JANUS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "s10janus.llr.gz");
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SaturnConfigs();
        c.body = ShapeModelBody.JANUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/janus/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new SaturnConfigs();
        c.body = ShapeModelBody.MIMAS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013b)";
        c.rootDirOnServer = "/GASKELL/MIMAS";
        setupFeatures(c);
        imagingMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/MIMAS/IMAGING", "", null, null, "/GASKELL/MIMAS/IMAGING/gallery");
        imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(imagingMetadata),
//                        new FixedListQuery<Object>("/GASKELL/MIMAS/IMAGING", "/GASKELL/MIMAS/IMAGING/gallery"), //
                        ImageType.SATURN_MOON_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SaturnConfigs();
        c.body = ShapeModelBody.PANDORA;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/pandora/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new SaturnConfigs();
        c.body = ShapeModelBody.PHOEBE;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013c)";
        c.rootDirOnServer = "/GASKELL/PHOEBE";
        setupFeatures(c);
        imagingMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/PHOEBE/IMAGING", "", null, null, "/GASKELL/PHOEBE/IMAGING/gallery");
        imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(imagingMetadata),
//                        new FixedListQuery<Object>("/GASKELL/PHOEBE/IMAGING", "/GASKELL/PHOEBE/IMAGING/gallery"), //
                        ImageType.SATURN_MOON_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};

        c.defaultForMissions = new Mission[] {};

        configArray.add(c);


        // Model stooke2016 delivered 2018-03-06.
        c = new SaturnConfigs();
        c.body = ShapeModelBody.PROMETHEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/prometheus/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SaturnConfigs();
            c.body = ShapeModelBody.RHEA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/GASKELL/RHEA";
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        c = new SaturnConfigs();
        c.body = ShapeModelBody.TETHYS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013d)";
        c.rootDirOnServer = "/GASKELL/TETHYS";
        c.hasColoringData = false;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);

        c = new SaturnConfigs();
        c.body = ShapeModelBody.TELESTO;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Ernst et al. (in progress)";
        c.rootDirOnServer = "/GASKELL/TELESTO";
        setupFeatures(c);
        imagingMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/TELESTO/IMAGING", "", null, null, "/GASKELL/TELESTO/IMAGING/gallery");
        imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(imagingMetadata),
//                        new FixedListQuery<Object>("/GASKELL/TELESTO/IMAGING", "/GASKELL/TELESTO/IMAGING/gallery"), //
                        ImageType.SATURN_MOON_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA, //
                        0.0,
                        "None",
                        Set.of((float)3.4028234663852886e38, (float)-3.4028234663852886e38)
                        ) //
        );

        imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        imagingConfig.imageSearchFilterNames = new String[] {};
        imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        imagingConfig.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
        c.defaultForMissions = new Mission[] {};

        configArray.add(c);





        // Standard Gaskell shape model may be described once.
        final ShapeModelConfiguration gaskellModelConfig = ShapeModelConfiguration.builder(ShapeModelType.GASKELL.name(), ShapeModelDataUsed.IMAGE_BASED).build();

        // Gaskell images only.
        final PointingSource[] gaskellImagingSource = new PointingSource[] { PointingSource.GASKELL };

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.ATLAS.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
//            QueryBase queryBase = new GenericPhpQuery("/atlas/gaskell/imaging", "atlas", "/atlas/gaskell/imaging/images/gallery");

            DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of("/atlas/gaskell/imaging", "", "atlas", "atlas", "/atlas/gaskell/imaging/images/gallery");
            ImageDataQuery query = new ImageDataQuery(metadata);

            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, query, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);
            imagingInstrument.setFillValues(Set.of((float)3.4028234663852886e38, (float)-3.4028234663852886e38));

            c = new SaturnConfigs();
            c.body = ShapeModelBody.ATLAS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/atlas/gaskell";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 5, 7, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 3, 13, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 400000.0;
            imagingConfig.imageSearchDefaultMaxResolution = 5000.0;
            c.hasColoringData = false;

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.ATLAS.toString(), "/project/sbmt2/data/atlas/gaskell/imaging/imagelist-fullpath.txt", "atlas"),
            };

            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.CALYPSO.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SaturnConfigs();
            c.body = ShapeModelBody.CALYPSO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/calypso/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

//            configArray.add(c);
        }

        {
            // Pair of models that share SUM files. Use this variable for both.
            String issRootDirPrimary;
            // Primary model: the SUM files really belong to this one.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.CALYPSO;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.provide("Daly");
                c.modelLabel = "Daly et al. (in progress)";
                c.rootDirOnServer = "/calypso/daly-2020";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                // This is actaully correct by default for a 4-resolution model:
                // c.setResolution(ImmutableList.copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION),
                // ImmutableList.copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // Set up root directory, where SUM files are located. THIS IS
                // ALSO USED BY THE SECONDARY MODEL.
                issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e3;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }

            // Secondary model: use the SUM files that belong to the Primary
            // model.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.CALYPSO;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.THOMAS;
                c.modelLabel = "Thomas et al. (2018)";
                c.rootDirOnServer = "/calypso/thomas-2018";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                int numberPlates = 28269;
                c.setResolution(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // DO NOT SET THIS VARIABLE: use the SUM files that belong to
                // the Primary model.
                // issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e3;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }
        }

        {
            c = new SaturnConfigs();
            c.body = ShapeModelBody.ENCELADUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/enceladus/gaskell";
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.EPIMETHEUS.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SaturnConfigs();
            c.body = ShapeModelBody.EPIMETHEUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/epimetheus/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            //configArray.add(c);
        }

        {
            // Pair of models that share SUM files. Use this variable for both.
            String issRootDirPrimary;
            // Primary model: the SUM files really belong to this one.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.EPIMETHEUS;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.provide("Daly");
                c.modelLabel = "Daly et al. (in progress)";
                c.rootDirOnServer = "/epimetheus/daly-2020";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                // This is actaully correct by default for a 4-resolution model:
                // c.setResolution(ImmutableList.copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION),
                // ImmutableList.copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // Set up root directory, where SUM files are located. THIS IS
                // ALSO USED BY THE SECONDARY MODEL.
                issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 4, 4, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e7;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e5;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }

            // Secondary model: use the SUM files that belong to the Primary
            // model.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.EPIMETHEUS;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.THOMAS;
                c.modelLabel = "Thomas et al. (2018)";
                c.rootDirOnServer = "/epimetheus/thomas-2018";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                int numberPlates = 49152;
                c.setResolution(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // DO NOT SET THIS VARIABLE: use the SUM files that belong to
                // the Primary model.
                // issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 4, 4, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e7;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e5;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.HELENE.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);
            imagingInstrument.setFillValues(Set.of((float)3.4028234663852886e38, (float)-3.4028234663852886e38));
            c = new SaturnConfigs();
            c.body = ShapeModelBody.HELENE;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/helene/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            c = new SaturnConfigs();
            c.body = ShapeModelBody.IAPETUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/iapetus/gaskell";
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.JANUS.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SaturnConfigs();
            c.body = ShapeModelBody.JANUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/janus/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            //configArray.add(c);
        }

        {
            // Pair of models that share SUM files. Use this variable for both.
            String issRootDirPrimary;
            // Primary model: the SUM files really belong to this one.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.JANUS;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.provide("Daly");
                c.modelLabel = "Daly et al. (in progress)";
                c.rootDirOnServer = "/janus/daly-2020";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                // This is actaully correct by default for a 4-resolution model:
                // c.setResolution(ImmutableList.copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION),
                // ImmutableList.copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // Set up root directory, where SUM files are located. THIS IS
                // ALSO USED BY THE SECONDARY MODEL.
                issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 3, 19, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e7;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e5;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }

            // Secondary model: use the SUM files that belong to the Primary
            // model.
            {
                c = new SaturnConfigs();
                c.body = ShapeModelBody.JANUS;
                c.type = BodyType.PLANETS_AND_SATELLITES;
                c.population = ShapeModelPopulation.SATURN;
                c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
                c.author = ShapeModelType.THOMAS;
                c.modelLabel = "Thomas et al. (2018)";
                c.rootDirOnServer = "/janus/thomas-2018";
                c.shapeModelFileExtension = ".obj";
                c.hasColoringData = false;
                int numberPlates = 49152;
                c.setResolution(ImmutableList.of(numberPlates + " plates"), ImmutableList.of(numberPlates));

                String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

                String issTable = tableBaseName + "iss";

                // DO NOT SET THIS VARIABLE: use the SUM files that belong to
                // the Primary model.
                // issRootDirPrimary = c.rootDirOnServer + "/iss";

                String issDataDir = "/cassini/iss/images";
                String issGalleryDir = "/cassini/iss/gallery";
                setupFeatures(c);
                imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
                imagingMetadata =
                		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

                imagingConfig.imagingInstruments = Lists.newArrayList(
                        new ImagingInstrument( //
                                SpectralImageMode.MONO, //
                                new ImageDataQuery(imagingMetadata),
//                                new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
                                ImageType.valueOf("ISS_IMAGE"), //
                                SumFiles, //
                                Instrument.ISS, //
                                0., //
                                "None" //
                        )
                );
                imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime();
                imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 3, 19, 0, 0, 0).getTime();
                imagingConfig.imageSearchFilterNames = new String[] {};
                imagingConfig.imageSearchUserDefinedCheckBoxesNames = new String[] {};
                imagingConfig.imageSearchDefaultMaxSpacecraftDistance = 1.0e7;
                imagingConfig.imageSearchDefaultMaxResolution = 1.0e5;

                c.databaseRunInfos = new DBRunInfo[] { //
                        new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                                issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
                };

                c.presentInMissions = new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                        Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
                c.defaultForMissions = new Mission[] {};

                configArray.add(c);
            }
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.PAN.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SaturnConfigs();
            c.body = ShapeModelBody.PAN;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pan/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.PANDORA.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SaturnConfigs();
            c.body = ShapeModelBody.PANDORA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pandora/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2005, 4, 20, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2016, 11, 19, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder( //
                    ShapeModelBody.PROMETHEUS.name(), //
                    BodyType.PLANETS_AND_SATELLITES.name(), //
                    ShapeModelPopulation.SATURN.name()).build(); //
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);
            imagingInstrument.setFillValues(Set.of((float)3.4028234663852886e38, (float)-3.4028234663852886e38));
            c = new SaturnConfigs();
            c.body = ShapeModelBody.PROMETHEUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/prometheus/gaskell";
            setupFeatures(c);
            imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
            imagingConfig.imagingInstruments = Lists.newArrayList(
                    imagingInstrument
            );
            imagingConfig.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            imagingConfig.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            c.presentInMissions = new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL};
            c.defaultForMissions = new Mission[] {};
            configArray.add(c);
        }
    }

	 // Imaging instrument helper methods.
    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, PointingSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
//        QueryBase queryBase = new FixedListQuery<Object>(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

        DataQuerySourcesMetadata metadata = DataQuerySourcesMetadata.of(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "", null, null, fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        FixedListDataQuery query = new FixedListDataQuery(metadata);

        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, query, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTFileLocator fileLocator, SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType)
    {
        Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(instrument, SpectralImageMode.MONO, queryBase, imageSources, fileLocator, imageType);

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