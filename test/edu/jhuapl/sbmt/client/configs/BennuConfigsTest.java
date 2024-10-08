package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.body.BodyType;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.io.DBRunInfo;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.BasemapImageConfig;
import edu.jhuapl.sbmt.image.config.BasemapImageConfigIO;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfigIO;
import edu.jhuapl.sbmt.image.keys.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.image.keys.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.image.model.BasemapImage;
import edu.jhuapl.sbmt.image.model.BinExtents;
import edu.jhuapl.sbmt.image.model.BinSpacings;
import edu.jhuapl.sbmt.image.model.BinTranslations;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.model.ImageFlip;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfig;
import edu.jhuapl.sbmt.lidar.config.LidarInstrumentConfigIO;
import edu.jhuapl.sbmt.model.bennu.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrumMath;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSQuery;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrumMath;
import edu.jhuapl.sbmt.model.eros.nis.NIS;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrumMath;
import edu.jhuapl.sbmt.model.eros.nis.NisQuery;
import edu.jhuapl.sbmt.model.phobos.MEGANE;
import edu.jhuapl.sbmt.model.phobos.MEGANEQuery;
import edu.jhuapl.sbmt.model.phobos.MEGANESpectrumMath;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3Query;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SpectrumMath;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfig;
import edu.jhuapl.sbmt.spectrum.config.SpectrumInstrumentConfigIO;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfigIO;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;

class BennuConfigsTest
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

    @SuppressWarnings("unused")
	private static final Mission[] PublicOnly = new Mission[] {
    		Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE
    };


	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		System.setProperty("edu.jhuapl.sbmt.mission", "TEST_APL_INTERNAL");
		Configuration.setAPLVersion(true);
		Mission.configureMission();
		Configuration.authenticate();

		SpectraTypeFactory.registerSpectraType("OTES", OTESQuery.getInstance(), OTESSpectrumMath.getInstance(), "cm^-1", new OTES().getBandCenters());
		SpectraTypeFactory.registerSpectraType("OVIRS", OVIRSQuery.getInstance(), OVIRSSpectrumMath.getInstance(), "um", new OVIRS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIS", NisQuery.getInstance(), NISSpectrumMath.getSpectrumMath(), "nm", new NIS().getBandCenters());
		SpectraTypeFactory.registerSpectraType("NIRS3", NIRS3Query.getInstance(), NIRS3SpectrumMath.getInstance(), "nm", new NIRS3().getBandCenters());
		SpectraTypeFactory.registerSpectraType("MEGANE", MEGANEQuery.getInstance(), MEGANESpectrumMath.getInstance(), "cm^-1", new MEGANE().getBandCenters());

		try {
			InstanceGetter.defaultInstanceGetter().getKeyForType(ImageBinPadding.class);
		} catch (IllegalArgumentException iae) {


//		if (InstanceGetter.defaultInstanceGetter().getKeyForType(ImageBinPadding.class) == null)
//		{
			ImageBinPadding.initializeSerializationProxy();
			BinExtents.initializeSerializationProxy();
			BinTranslations.initializeSerializationProxy();
			BinSpacings.initializeSerializationProxy();
			BasemapImage.initializeSerializationProxy();
			ImageDataQuery.initializeSerializationProxy();
			FixedListDataQuery.initializeSerializationProxy();
			CylindricalBounds.initializeSerializationProxy();
			PerspectiveImageMetadata.initializeSerializationProxy();
			CustomCylindricalImageKey.initializeSerializationProxy();
			CustomPerspectiveImageKey.initializeSerializationProxy();
			CompositePerspectiveImage.initializeSerializationProxy();
			ImagingInstrument.initializeSerializationProxy();

			MEGANE.initializeSerializationProxy();
			NIS.initializeSerializationProxy();
			NIRS3.initializeSerializationProxy();
			OTES.initializeSerializationProxy();
			OVIRS.initializeSerializationProxy();
			SpectrumInstrumentMetadata.initializeSerializationProxy();
			SpectrumInstrumentMetadataIO.initializeSerializationProxy();
			DataQuerySourcesMetadata.initializeSerializationProxy();
			SpectrumSearchSpec.initializeSerializationProxy();

			SpiceInfo.initializeSerializationProxy();
		}

		FeatureConfigIOFactory.registerFeatureConfigIO(BasemapImageConfig.class.getSimpleName(), new BasemapImageConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(ImagingInstrumentConfig.class.getSimpleName(), new ImagingInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(LidarInstrumentConfig.class.getSimpleName(), new LidarInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(SpectrumInstrumentConfig.class.getSimpleName(), new SpectrumInstrumentConfigIO());
		FeatureConfigIOFactory.registerFeatureConfigIO(StateHistoryConfig.class.getSimpleName(), new StateHistoryConfigIO());

		ConfigArrayList<IBodyViewConfig> builtInConfigs = SmallBodyViewConfig.getBuiltInConfigs();
		BennuConfigs.initialize(builtInConfigs, false);

//		SmallBodyViewConfig.initializeWithStaticConfigs(publishedDataOnly);
		for (IBodyViewConfig each : SmallBodyViewConfig.getBuiltInConfigs())
		{
			each.enable(true);
		}
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception
	{
	}

    protected static String[] prepend(String prefix, String... strings)
    {
        String[] result = new String[strings.length];
        int index = 0;
        for (String string : strings)
        {
            result[index++] = SafeURLPaths.instance().getString(prefix, string);
        }

        return result;
    }

	@Test void testEarthBlenderConfig()
	{
	}

	@Test void testEarthWGS84Config()
	{
	}

	@Test void testBennuNolanConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.NOLAN);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

		assertEquals(c.body, ShapeModelBody.RQ36);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.RADAR_BASED);
		assertEquals(c.author, ShapeModelType.NOLAN);
		assertEquals(c.modelLabel, "Nolan et al. (2013)");
		assertEquals( c.rootDirOnServer, "/bennu/nolan");
		assertEquals(c.getShapeModelFileExtension(), ".obj");

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/bennu/nolan/history/timeHistory.bth");

        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(2692));
        assertEquals(c.density, 1.260);
        assertEquals(c.useMinimumReferencePotential, true);
        assertEquals(c.rotationRate, 0.00040613);

        assertArrayEquals(c.presentInMissions, AllBennuClients);
	}

	@Test void testBennuV3GaskellConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V3");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "OREX Simulated");
        assertEquals(c.version, "V3");
        assertEquals(c.rootDirOnServer, "/GASKELL/RQ36_V3");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz"));
        assertEquals(c.density, 1.0);
        assertEquals(c.rotationRate, 0.000407026411379);
        assertArrayEquals(c.presentInMissions, AllBennuClients);

        assertEquals(c.body,  ShapeModelBody.RQ36);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.useMinimumReferencePotential, false);

        assertEquals(c.hasMapmaker, true);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

//        testPolycamParameters(imagingConfig, c.rootDirOnServer, "", "RQ36_POLY", true, true, false);

        PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		imageSourceArray.add(PointingSource.GASKELL);
		imageSourceArray.add(PointingSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/POLYCAM");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), c.rootDirOnServer + "/POLYCAM/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.POLYCAM_V3_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.POLYCAM);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
    	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

//		testMapcamParameters(imagingConfig, c.rootDirOnServer, "", "RQ36_MAP", true, true, false);
		assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), c.rootDirOnServer + "/MAPCAM");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), c.rootDirOnServer + "/MAPCAM/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MAPCAM_V3_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MAPCAM);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
    	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

//        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, true);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");

        // default ideal data

        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
        // noisy data
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Noise"), "/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Noise"), "/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

        assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
        assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
        assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
        assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
        assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarBrowseIsBinary, true);
        assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
        assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/RQ36_V3/history/timeHistory.bth");

        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
    		new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/MAPCAM/imagelist-fullpath.txt", "RQ36_MAP"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/POLYCAM/imagelist-fullpath.txt", "RQ36_POLY"),
        });


        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuV4GaskellConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.GASKELL, "V4");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "OREX Simulated");
        assertEquals(c.version, "V4");
        assertEquals(c.rootDirOnServer, "/bennu/bennu-simulated-v4");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer + "/shape", "shape0.obj.gz", "shape1.vtk.gz", "shape2.vtk.gz", "shape3.vtk.gz", "shape4.vtk.gz"));
        assertEquals(c.density, 1.26);
        assertEquals(c.rotationRate, 0.0004061303295118512);
        assertArrayEquals(c.presentInMissions, AllBennuClients);

        assertEquals(c.body,  ShapeModelBody.RQ36);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.useMinimumReferencePotential, true);

        assertEquals(c.hasMapmaker, false);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

//        testPolycamParameters(imagingConfig, c.rootDirOnServer, "", "RQ36_POLY", true, true, false);

        PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		imageSourceArray.add(PointingSource.GASKELL);
		imageSources = imageSourceArray.toArray(imageSources);

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/polycam");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), c.rootDirOnServer + "/polycam/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), c.rootDirOnServer + "/polycam/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.POLYCAM_V4_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.POLYCAM);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);

//		testMapcamParameters(imagingConfig, c.rootDirOnServer, "", "RQ36_MAP", true, true, false);
		assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), c.rootDirOnServer + "/mapcam");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), c.rootDirOnServer + "/mapcam/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), c.rootDirOnServer + "/mapcam/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MAPCAM_V4_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MAPCAM);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt");

        // default ideal data

        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/GASKELL/RQ36_V4/OLA/trees/default/tree/dataSource.lidar");
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt");
//        // noisy data
//        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Noise"), "/GASKELL/RQ36_V4/OLA/trees/noise/tree/dataSource.lidar");
//        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Noise"), "/GASKELL/RQ36_V4/OLA/browse/noise/fileList.txt");

        assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
        assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
        assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
        assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
        assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarBrowseIsBinary, true);
        assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
        assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, c.rootDirOnServer + "/history/timeHistory.bth");

        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
    		new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/mapcam/imagelist-fullpath.txt", "RQ36V4_MAP"),
         	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/polycam/imagelist-fullpath.txt", "RQ36V4_POLY"),
        });


        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181109bConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181109b"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181109b"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181109b");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181109b");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181115Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181115"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181115"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181115");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181115");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
        		"Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181115_polycam", "bennu_altwgspcv20181115_polycam", false, true, false);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181115_mapcam", "bennu_altwgspcv20181115_mapcam", false, true, false);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181116Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181116"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181116"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181116");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181116");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", true, true, false);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", false, true, false);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181123bConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181123b"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181123b"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181123b");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181123b");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", true, true, false);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", false, true, false);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181202Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181202"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181202"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181202");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181202");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", true, true, false);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", true, true, false);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181206Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181206"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181206"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20181206");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181206");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", true, true, false);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", false, true, false);
//		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181217Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181217"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181217"));
        assertEquals(c.modelLabel, "SPC v13");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181217");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20181227Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20181227"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20181227"));
        assertEquals(c.modelLabel, "SPC v14");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20181227");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20181227_navcam", "bennu_altwgspcv20181227_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190105Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190105"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190105"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20190105");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190105");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);
        assertEquals(c.bodyReferencePotential, -0.02654811544296466);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190105_polycam", "bennu_altwgspcv20190105_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190105_mapcam", "bennu_altwgspcv20190105_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190105_navcam", "bennu_altwgspcv20190105_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190114Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190114"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190114"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20190114");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190114");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.260);
        assertEquals(c.rotationRate, 4.0613E-4);
        assertEquals(c.bodyReferencePotential, -0.02637307554771602);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190114_polycam", "bennu_altwgspcv20190114_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190114_mapcam", "bennu_altwgspcv20190114_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190114_navcam", "bennu_altwgspcv20190114_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190117Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190117"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190117"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20190117");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190117");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02530442113463265);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190117_polycam", "bennu_altwgspcv20190117_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190117_mapcam", "bennu_altwgspcv20190117_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190117_navcam", "bennu_altwgspcv20190117_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190121Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190121"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190121"));
        assertEquals(c.modelLabel, "SPC v20");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190121");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02513575424405747);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190121_navcam", "bennu_altwgspcv20190121_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190207aConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190207a"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190207a"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20190207a");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190207a");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.0253033332766406);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207a_polycam", "bennu_altwgspcv20190207a_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207a_mapcam", "bennu_altwgspcv20190207a_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207a_navcam", "bennu_altwgspcv20190207a_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190207bConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190207b"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190207b"));
        assertEquals(c.modelLabel, "ALTWG-SPC-v20190207b");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190207b");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02528907231151947);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207b_polycam", "bennu_altwgspcv20190207b_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207b_mapcam", "bennu_altwgspcv20190207b_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190207b_navcam", "bennu_altwgspcv20190207b_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, false);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190414Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190414"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190414"));
        assertEquals(c.modelLabel, "SPC v28");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190414");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02520767997203304);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190414_navcam", "bennu_altwgspcv20190414_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, InternalOnly);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190612Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPO-v20190612"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPO-v20190612"));
        assertEquals(c.modelLabel, "SPO v34");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spo-v20190612");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02517871436774813);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspov20190612_polycam", "bennu_altwgspov20190612_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspov20190612_mapcam", "bennu_altwgspov20190612_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspov20190612_navcam", "bennu_altwgspov20190612_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/navcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20190828Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20190828"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20190828"));
        assertEquals(c.modelLabel, "SPC v42");
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20190828");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02517940647257273);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190828_polycam", "bennu_altwgspcv20190828_polycam", true, false, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190828_mapcam", "bennu_altwgspcv20190828_mapcam", true, false, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20190828_navcam", "bennu_altwgspcv20190828_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_mapcam"),
        	new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_polycam"),

        	new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_mapcam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_polycam"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_navcam")
        });

        assertEquals(c.getBaseMapConfigName(), "config.txt");
	}

	@Test void testBennuSPC20191027Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("ALTWG-SPC-v20191027"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ALTWG-SPC-v20191027"));
        assertEquals(c.modelLabel, c.author.name());
        assertEquals(c.rootDirOnServer, "/bennu/altwg-spc-v20191027");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.186);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02517940647257273);
        assertEquals(c.hasCustomBodyCubeSize, true);
        assertEquals(c.customBodyCubeSize, 0.02); //km

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20191027_polycam", "bennu_altwgspcv20191027_polycam", true, true, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20191027_mapcam", "bennu_altwgspcv20191027_mapcam", true, true, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_altwgspcv20191027_navcam", "bennu_altwgspcv20191027_navcam", false);
		testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/browse/fileListv2.txt");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	 new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_mapcam"),
             new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_polycam"),

             new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_mapcam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_polycam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_navcam")
        });
	}

	@Test void testBennuOLAV20Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("OLA-v20"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.author, ShapeModelType.provide("OLA-v20"));
        assertEquals(c.modelLabel, "OLA v20");
        assertEquals(c.rootDirOnServer, "/bennu/ola-v20-spc");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.194);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02527683882517149);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20_polycam", "bennu_olav20_polycam", true, true, true, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20_mapcam", "bennu_olav20_mapcam", true, true, true, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20_navcam", "bennu_olav20_navcam", true);

        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

//        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/l2a/fileListL2A.txt");

        String lidarBrowseList = "/ola/l2a/fileListL2A.txt";
        assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
		assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
		assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseFileListResourcePath,  c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);

		assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
		assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
		assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
		assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
		assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
		assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
		assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
		assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
		assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
		assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
		assertEquals(lidarConfig.lidarBrowseIsBinary, true);
		assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
		assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        ArrayList<Date> startStop = new ArrayList<Date>();
        startStop = new ArrayList<Date>();
        startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
        startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

        assertEquals(lidarConfig.orexSearchTimeMap.get("OLAv20"), startStop);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("OLAv20"), c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	 new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20_mapcam"),
             new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav20_polycam"),

             new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav20_mapcam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav20_polycam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav20_navcam")
        });
	}

	@Test void testBennuOLAV20PTMConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("OLA-v20-PTM"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.author, ShapeModelType.provide("OLA-v20-PTM"));
        assertEquals(c.modelLabel, "OLA v20 PTM");
        assertEquals(c.rootDirOnServer, "/bennu/ola-v20-ptm");
        assertEquals(c.getResolutionLabels(), ImmutableList.of("Medium (217032 plates)", "High (886904 plates)", "Very High (3366134 plates)"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(217032, 886904, 3366134));
        assertEquals(c.density, 1.194);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02527683882517149);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20ptm_polycam", "bennu_olav20ptm_polycam", true, true, true, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20ptm_mapcam", "bennu_olav20ptm_mapcam", true, true, true, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav20ptm_navcam", "bennu_olav20ptm_navcam", true);

        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

//        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/l2a/fileListL2A.txt");

        String lidarBrowseList = "/ola/l2a/fileListL2A.txt";
        assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
		assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
		assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseFileListResourcePath,  c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);

		assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
		assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
		assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
		assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
		assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
		assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
		assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
		assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
		assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
		assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
		assertEquals(lidarConfig.lidarBrowseIsBinary, true);
		assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
		assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        ArrayList<Date> startStop = new ArrayList<Date>();
        startStop = new ArrayList<Date>();
        startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
        startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

        assertEquals(lidarConfig.orexSearchTimeMap.get("OLAv20"), startStop);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("OLAv20"), c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	 new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_mapcam"),
             new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_polycam"),

             new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_mapcam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-info.txt", "bennu_olav20ptm_polycam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/navcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_navcam")
        });
	}

	@Test void testBennuOLAV21Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("OLA-v21"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.author, ShapeModelType.provide("OLA-v21"));
        assertEquals(c.modelLabel, "OLA v21");
        assertEquals(c.rootDirOnServer, "/bennu/ola-v21-spc");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.1953);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02524469206484981);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21_polycam", "bennu_olav21_polycam", true, true, true, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21_mapcam", "bennu_olav21_mapcam", true, true, true, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21_navcam", "bennu_olav21_navcam", true);

        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

//        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/l2a/fileListL2A.txt");

        String lidarBrowseList = "/ola/l2a/fileListL2A_OLAv21.txt";
        assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
		assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
		assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseFileListResourcePath,  c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);

		assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
		assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
		assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
		assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
		assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
		assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
		assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
		assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
		assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
		assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
		assertEquals(lidarConfig.lidarBrowseIsBinary, true);
		assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
		assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        ArrayList<Date> startStop = new ArrayList<Date>();
        startStop = new ArrayList<Date>();
        startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
        startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

        assertEquals(lidarConfig.orexSearchTimeMap.get("OLAv21"), startStop);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("OLAv21"), c.rootDirOnServer + "/ola/search/olav21/dataSource.lidar");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, OREXClients);
        assertEquals(c.getBaseMapConfigName(), "config.txt");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	 new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav21_mapcam"),
             new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav21_polycam"),

             new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav21_mapcam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav21_polycam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav21_navcam")
        });
	}

	@Test void testBennuOLAV21PTMConfig()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("OLA-v21-PTM"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.author, ShapeModelType.provide("OLA-v21-PTM"));
        assertEquals(c.modelLabel, "OLA v21 PTM");
        assertEquals(c.rootDirOnServer, "/bennu/ola-v21-ptm");
        assertEquals(c.getResolutionLabels(), ImmutableList.of("Low (231870 plates)", "Medium (886400 plates)", "High (3365938 plates)", "Very High (17866836 plates)"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(231870, 886400, 3365938, 17866836));
        assertEquals(c.density, 1.1953);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02524469206484981);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21ptm_polycam", "bennu_olav21ptm_polycam", true, true, true, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21ptm_mapcam", "bennu_olav21ptm_mapcam", true, true, true, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_olav21ptm_navcam", "bennu_olav21ptm_navcam", true);

        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

//        testLidarParameters(lidarConfig, false, c.rootDirOnServer, "/ola/l2a/fileListL2A.txt");

        String lidarBrowseList = "/ola/l2a/fileListL2A_OLAv21.txt";
        assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
		assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
		assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseFileListResourcePath,  c.rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), c.rootDirOnServer + lidarBrowseList);

		assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
		assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
		assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
		assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
		assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
		assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
		assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
		assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
		assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
		assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
		assertEquals(lidarConfig.lidarBrowseIsBinary, true);
		assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
		assertEquals(lidarConfig.lidarOffsetScale, 0.0005);

        ArrayList<Date> startStop = new ArrayList<Date>();
        startStop = new ArrayList<Date>();
        startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
        startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());

        assertEquals(lidarConfig.orexSearchTimeMap.get("OLAv21PTM"), startStop);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("OLAv21PTM"), c.rootDirOnServer + "/ola/search/olav21ptm/dataSource.lidar");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
        assertEquals(c.getBaseMapConfigName(), "config.txt");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	 new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/mapcam/imagelist-fullpath-sum.txt", "bennu_olav21ptm_mapcam"),
             new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/polycam/imagelist-fullpath-sum.txt", "bennu_olav21ptm_polycam"),

             new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/mapcam/imagelist-fullpath-info.txt", "bennu_olav21ptm_mapcam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/polycam/imagelist-fullpath-info.txt", "bennu_olav21ptm_polycam"),
             new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-ptm/navcam/imagelist-fullpath-info.txt", "bennu_olav21ptm_navcam")
        });
	}

	@Test void testBennuSPOV54Config()
	{
		BennuConfigs c = (BennuConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RQ36, ShapeModelType.provide("SPO-v54"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("SPO-v54"));
        assertEquals(c.modelLabel, "SPO v54");
        assertEquals(c.rootDirOnServer, "/bennu/spo-v54-spc");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(),
                ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
        assertEquals(c.density, 1.194);
        assertEquals(c.rotationRate, 4.0626E-4);
        assertEquals(c.bodyReferencePotential, -0.02538555084803482);

        testBodyParameters(c);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

//        imagingConfig.imagingInstruments = Lists.newArrayList(c.generatePolycamInstrument("bennu_spov54_polycam", "bennu_spov54_polycam", true, true),
//                                                         c.generateMapcamInstrument("bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true),
//                                                         c.generateNavcamInstrument("bennu_spov54_navcam", "bennu_spov54_navcam")
//        );

		assertEquals(imagingConfig.imagingInstruments.size(), 3);

		testPolycamParameters(imagingConfig, c.rootDirOnServer, "bennu_spov54_polycam", "bennu_spov54_polycam", true, true, false, ImageFlip.X, ImageFlip.X);
		testMapcamParameters(imagingConfig, c.rootDirOnServer, "bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true, false, ImageFlip.X, ImageFlip.X);
		testNavcamParameters(imagingConfig, c.rootDirOnServer, "bennu_spov54_navcam", "bennu_spov54_navcam", false);

        System.out.println("BennuConfigsTest: testBennuSPOV54Config: spec config " + spectrumConfig.hasSpectralData);
        testSpectrumParameters(spectrumConfig, c.rootDirOnServer, true);
        testStateHistoryParameters(c, stateHistoryConfig);
        assertEquals(c.hasMapmaker, false);

        testLidarParameters(lidarConfig, true, c.rootDirOnServer, "/ola/l2a/fileListL2A.txt");

//        ArrayList<Date> startStop = new ArrayList<Date>();
//        startStop = new ArrayList<Date>();
//        assertEquals(startStop.get(0), new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
//        assertEquals(startStop.get(1), new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
//        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
//        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
//        assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

        assertArrayEquals(c.presentInMissions, ClientsWithOREXModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
            new DBRunInfo(PointingSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_spov54_mapcam"),
            new DBRunInfo(PointingSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-sum.txt", "bennu_spov54_polycam"),

            new DBRunInfo(PointingSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-info.txt", "bennu_spov54_mapcam"),
            new DBRunInfo(PointingSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-info.txt", "bennu_spov54_polycam"),
            new DBRunInfo(PointingSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/navcam/imagelist-fullpath-info.txt", "bennu_spov54_navcam")
        });
	}

	private static void testPolycamParameters(ImagingInstrumentConfig imagingConfig, String rootDirOnServer, String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		testPolycamParameters(imagingConfig, rootDirOnServer, spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, publicOnly, ImageFlip.NONE, ImageFlip.NONE);
	}

	private static void testPolycamParameters(ImagingInstrumentConfig imagingConfig, String rootDirOnServer, String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly, ImageFlip spcFlip, ImageFlip spiceFlip)
	{
		PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		if (includeSPC) imageSourceArray.add(PointingSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(PointingSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), rootDirOnServer + "/polycam");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), rootDirOnServer + "/polycam/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), rootDirOnServer + "/polycam/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.POLYCAM_FLIGHT_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.POLYCAM);
        if (includeSPC)
        {
	        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
	        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), spcFlip);
        }
        if (includeSPICE)
        {
        	assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
	        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), spiceFlip);
        }
	}

	private static void testMapcamParameters(ImagingInstrumentConfig imagingConfig, String rootDirOnServer, String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		testMapcamParameters(imagingConfig, rootDirOnServer, spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, publicOnly, ImageFlip.NONE, ImageFlip.NONE);
	}

	private static void testMapcamParameters(ImagingInstrumentConfig imagingConfig, String rootDirOnServer, String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly, ImageFlip spcFlip, ImageFlip spiceFlip)
	{
		PointingSource[] imageSources = {};
		ArrayList<PointingSource> imageSourceArray = new ArrayList<PointingSource>();
		if (includeSPC) imageSourceArray.add(PointingSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(PointingSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);

		assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), rootDirOnServer + "/mapcam");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), rootDirOnServer + "/mapcam/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), rootDirOnServer + "/mapcam/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MAPCAM_FLIGHT_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, imageSources);
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MAPCAM);
        if (includeSPC)
        {
        	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getFlip(), spcFlip);
        }
        if (includeSPICE)
        {
        	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        	assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), spiceFlip);
        }
	}

	private static void testNavcamParameters(ImagingInstrumentConfig imagingConfig, String rootDirOnServer, String spcNamePrefix, String spiceNamePrefix, boolean publicOnly)
	{
		assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), rootDirOnServer + "/navcam");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), rootDirOnServer + "/navcam/images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), rootDirOnServer + "/navcam/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.NAVCAM_FLIGHT_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] {PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.NAVCAM);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
	}

	private static void testBodyParameters(BennuConfigs c)
	{
		assertEquals(c.body,  ShapeModelBody.RQ36);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.useMinimumReferencePotential, true);

	}

	private static void testStateHistoryParameters(BennuConfigs c, StateHistoryConfig stateHistoryConfig)
	{
		assertEquals(c.hasStateHistory, true);
		assertEquals(stateHistoryConfig.hasStateHistory, true);
		assertEquals(stateHistoryConfig.timeHistoryFile, c.rootDirOnServer + "/history/timeHistory.bth");
		assertEquals(stateHistoryConfig.stateHistoryStartDate, new GregorianCalendar(2018, 10, 25, 0, 0, 0).getTime());
		assertEquals(stateHistoryConfig.stateHistoryEndDate, new GregorianCalendar(2025, 1, 1, 0, 0, 0).getTime());
		assertEquals(stateHistoryConfig.spiceInfo.getScId(), "ORX");
		assertEquals(stateHistoryConfig.spiceInfo.getBodyFrameName(), "IAU_BENNU");
		assertEquals(stateHistoryConfig.spiceInfo.getScFrameName(), "ORX_SPACECRAFT");
		assertEquals(stateHistoryConfig.spiceInfo.getBodyName(), "BENNU");
		assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyNamesToBind(), new String[] {"EARTH" , "SUN"});
		assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyFramesToBind(), new String[] {"IAU_EARTH" , "IAU_SUN"});
		assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentNamesToBind(), new String[] {});
		assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentFrameNamesToBind(), new String[] {"ORX_OCAMS_POLYCAM", "ORX_OCAMS_MAPCAM",
				"ORX_OCAMS_SAMCAM", "ORX_NAVCAM1", "ORX_NAVCAM2",
//				"ORX_OTES", "ORX_OVIRS",
				"ORX_OLA_LOW", "ORX_OLA_HIGH"});
	}

	@SuppressWarnings("unused")
	private static void testImagingParameters(ImagingInstrumentConfig imagingConfig)
	{
		assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e3);
		assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e3);
	}

	private static void testSpectrumParameters(SpectrumInstrumentConfig spectrumConfig, String rootDirOnServer, boolean hasHypertree)
	{
		assertEquals(spectrumConfig.hasSpectralData, true);
		assertEquals(spectrumConfig.spectralInstruments.get(0), new OTES());
		assertEquals(spectrumConfig.spectralInstruments.get(1), new OVIRS());

		assertEquals(spectrumConfig.hasHierarchicalSpectraSearch, true);
		assertEquals(spectrumConfig.hasHypertreeBasedSpectraSearch, hasHypertree);
		assertEquals(spectrumConfig.spectraSearchDataSourceMap.get("OTES_L2"), rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
		assertEquals(spectrumConfig.spectraSearchDataSourceMap.get("OTES_L3"), rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
		assertEquals(spectrumConfig.spectraSearchDataSourceMap.get("OVIRS_IF"), rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
		assertEquals(spectrumConfig.spectraSearchDataSourceMap.get("OVIRS_REF"), rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");

//        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification. .size(), 2);

	}

	private static void testLidarParameters(LidarInstrumentConfig lidarConfig, boolean hasHypertree, String rootDirOnServer, String lidarBrowseList)
	{
		assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, hasHypertree); // enable tree-based lidar searching
		assertEquals(lidarConfig.lidarInstrumentName, Instrument.OLA);
		assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime());
		assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Default"), rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseFileListResourcePath,  rootDirOnServer + lidarBrowseList);
		assertEquals(lidarConfig.lidarBrowseWithPointsDataSourceMap.get("Default"), rootDirOnServer + lidarBrowseList);

         if (hasHypertree)
         {
	         /*
	          * search times split into phases
	          */
	         ArrayList<Date> startStop = new ArrayList<Date>();
	         startStop.add(lidarConfig.lidarSearchDefaultStartDate);
	         startStop.add(lidarConfig.lidarSearchDefaultEndDate);
	         assertEquals(lidarConfig.orexSearchTimeMap.get("Default"), startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(lidarConfig.lidarSearchDefaultStartDate);
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         assertEquals(lidarConfig.orexSearchTimeMap.get("Preliminary"), startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         assertEquals(lidarConfig.orexSearchTimeMap.get("Detailed"), startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         assertEquals(lidarConfig.orexSearchTimeMap.get("OrbB"), startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         startStop.add(lidarConfig.lidarSearchDefaultEndDate);
	         assertEquals(lidarConfig.orexSearchTimeMap.get("Recon"), startStop);

	         assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Preliminary"), rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
	         assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Detailed"), rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
	         assertEquals(lidarConfig.lidarSearchDataSourceMap.get("OrbB"), rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
	         assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Recon"), rootDirOnServer + "/ola/search/recon/dataSource.lidar");
         }

         assertEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
         assertEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
         assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
         assertEquals(lidarConfig.lidarBrowseTimeIndex, 26);
         assertEquals(lidarConfig.lidarBrowseNoiseIndex, 62);
         assertEquals(lidarConfig.lidarBrowseOutgoingIntensityIndex, 98);
         assertEquals(lidarConfig.lidarBrowseReceivedIntensityIndex, 106);
         assertEquals(lidarConfig.lidarBrowseIntensityEnabled, true);
         assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
         assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
         assertEquals(lidarConfig.lidarBrowseIsBinary, true);
         assertEquals(lidarConfig.lidarBrowseBinaryRecordSize, 186);
         assertEquals(lidarConfig.lidarOffsetScale, 0.0005);
	}

}
