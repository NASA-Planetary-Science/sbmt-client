package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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

import crucible.crust.metadata.impl.InstanceGetter;

class DartConfigsTest
{

	private static final Mission[] APLClients = new Mission[] { //
            Mission.APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
            Mission.STAGE_APL_INTERNAL, //
    };

    private static final Mission[] DartClients = new Mission[] { //
            Mission.DART_DEV, //
            Mission.DART_DEPLOY, //
            Mission.DART_TEST, //
            Mission.DART_STAGE, //
    };

    private static final Mission[] InternalClientsWithDartModels = new Mission[] {
    		 Mission.APL_INTERNAL, //
             Mission.TEST_APL_INTERNAL, //
             Mission.STAGE_APL_INTERNAL, //
             Mission.DART_DEV, //
             Mission.DART_DEPLOY, //
             Mission.DART_TEST, //
             Mission.DART_STAGE, //
    };

    private static final Mission[] ClientsWithDartModels = new Mission[] { //
            Mission.APL_INTERNAL, //
            Mission.TEST_APL_INTERNAL, //
            Mission.STAGE_APL_INTERNAL, //
            Mission.DART_DEV, //
            Mission.DART_DEPLOY, //
            Mission.DART_TEST, //
            Mission.DART_STAGE, //
            Mission.PUBLIC_RELEASE,
            Mission.STAGE_PUBLIC_RELEASE,
            Mission.TEST_PUBLIC_RELEASE
    };

    private static final String MissionPrefix = "DART";

	// Months (only) are 0-based: SEPTEMBER 20 is 8, 20, not 9, 20.
    private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2022, 8, 20, 0, 0, 0).getTime();
    // Months (only) are 0-based: OCTOBER 5 is 9, 5 not 10, 5.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2022, 9, 5, 0, 0, 0).getTime();

//    // Months (only) are 0-based: JULY 1 is 6, 1, not 7, 1.
//    private static final Date JupiterSearchStartDate = new GregorianCalendar(2022, 6, 1, 0, 0, 0).getTime();
//    // Months (only) are 0-based: JULY 2 is 6, 2 not 7, 2.
//    private static final Date JupiterSearchEndDate = new GregorianCalendar(2022, 6, 2, 0, 0, 0).getTime();

    // Months (only) are 0-based: SEPTEMBER 26 is 8, 26.
    // These values were specified in an update to Redmine issue #2472.
    private static final Date DimorphosImpactSearchStartDate = new GregorianCalendar(2022, 8, 26, 22, 0, 0).getTime();
    private static final Date DimorphosImpactSearchEndDate = new GregorianCalendar(2022, 8, 26, 23, 14, 25).getTime();
    private static final double DimorphosImpactMaxScDistance = 10000.0; // km
    private static final double DimorphosImpactResolution = 300.0; // mpp

    // These values were specified in an update to Redmine issue #2496.
    private static final Date DidymosImpactSearchStartDate = new GregorianCalendar(2022, 8, 26, 23, 10, 39).getTime();
    private static final Date DidymosImpactSearchEndDate = new GregorianCalendar(2022, 8, 26, 23, 12, 57).getTime();
    private static final double DidymosImpactMaxScDistance = 1500.0; // km
    private static final double DidymosImpactResolution = 7.0; // mpp

    // These values were specified in an update to Redmine issue #2555.
    private static final Date DidymosLiciaSearchStartDate = DidymosImpactSearchStartDate;
    private static final Date DidymosLiciaSearchEndDate = new GregorianCalendar(2022, 8, 26, 23, 17, 20).getTime();

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
		DartConfigs.instance().initialize(builtInConfigs);

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

	@Test
	void testDidymosIdealImpact1_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact2_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact3_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact4_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact4RA_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact5_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact6_20200629_v01()
	{
		//NOT USED
	}

	@Test
	void testDidymosIdealImpact9_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact1_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact1-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact1-20200629-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 1 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact1-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact1-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3072 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3072));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact1-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact1-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact1-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact1-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact1-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact1-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact1-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact1-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact1-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact1-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact1_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact1-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact1_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact1-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact1_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact1-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact1_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact2_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact2-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact2-20200629-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 2 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact2-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact2-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact2-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact2-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact2-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact2-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact2-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact2-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact2-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact2-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact2-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact2-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact2_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact2-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact2_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact2-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact2_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact2-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact2_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact3_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact3-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact3-20200629-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 3 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact3-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact3-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3366134 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3366134));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact3-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact3-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact3-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact3-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact3-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact3-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact3-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact3-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact3-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact3-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact3_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact3-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact3_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact3-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact3_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact3-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact3_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact4_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact4-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact4-20200629-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 4 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact4-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact4-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact4RA_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact4-ra-20210211-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact4-ra-20210211-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 4 RA 20210211 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact4-ra-20210211-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact4-ra-20210211-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-ra-20210211-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-ra-20210211-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact4-ra-20210211-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact4-ra-20210211-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact4-ra-20210211-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact4-ra-20210211-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact4-ra-20210211-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), false);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

//        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-ra-20210211-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_ra_20210211_v01_draco"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-ra-20210211-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_ra_20210211_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact4-ra-20210211-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact4_ra_20210211_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact5_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact5-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact5-20200629-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 5 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact5-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact5-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3366134 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3366134));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact5-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact5-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact5-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact5-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact5_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact5-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact5_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact5-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact5_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact5-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact5_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosIdealImpact6_20200629_v01()
	{
		//NOT USED
//		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact6-20200629-v01")));
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//
//        String label = "Dimorphos-v004";
//        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
//        assertEquals(c.author, ShapeModelType.provide("ideal-impact6-20200629-v01"));
//        assertEquals(c.modelLabel, "Ideal Impact 6 20200629 v01");
//        assertEquals(c.type, BodyType.ASTEROID);
//        assertEquals(c.population, ShapeModelPopulation.NEO);
//        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
//        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
//        assertEquals(c.density, 0.0);
//        assertEquals(c.rotationRate, 0.0);
//        assertEquals(c.hasSystemBodies, false);
//        assertEquals(c.systemConfigs, Lists.newArrayList());
//        assertEquals(c.hasDTMs, true);
//        assertEquals(c.hasLineamentData, false);
//        assertEquals(c.hasMapmaker, false);
//        assertEquals(c.hasColoringData, true);
//        assertEquals(c.hasCustomBodyCubeSize, false);
//        assertEquals(c.customBodyCubeSize, 0.0);
//        assertEquals(c.useMinimumReferencePotential, false);
//        assertEquals(c.bodyReferencePotential, 0.0);
//        assertEquals(c.getBaseMapConfigName(), "config.txt");
//        assertEquals(c.bodyLowestResModelName, "");
//        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact6-20200629-v01");
//        assertEquals(c.getShapeModelFileExtension(), ".obj");
//        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
//        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact6-20200629-v01/shape/", "shape0.obj.gz"));
//        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
//        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));
//
//        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
//        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
//        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
//        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);
//
//        assertEquals(imagingConfig.imagingInstruments.size(), 3);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact6-20200629-v01" + "/draco");
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact6-20200629-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact6-20200629-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
//        Arrays.sort(fillValues);
//        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});
//
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact6-20200629-v01" + "/leia");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact6-20200629-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact6-20200629-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
//        Arrays.sort(fillValues2);
//        assertArrayEquals(fillValues2, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});
//
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact6-20200629-v01" + "/luke");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact6-20200629-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact6-20200629-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
//        Arrays.sort(fillValues3);
//        assertArrayEquals(fillValues3, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});
//
//
//        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
//        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
//        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);
//
//        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
//        assertEquals(spectrumConfig.hasSpectralData, false);
//
//        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
//        assertEquals(lidarConfig.hasLidarData, false);
//
//        testStateHistoryParameters(c, ShapeModelBody.DIDYMOS.name());
//
//        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
//        assertArrayEquals(c.defaultForMissions, new Mission[] {});
//
//        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
//            {
//                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/ideal-impact6-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact6_20200629_v01_draco"),
//                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/ideal-impact6-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact6_20200629_v01_leia"),
//                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/ideal-impact6-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact6_20200629_v01_leia"),
//                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/ideal-impact6-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact6_20200629_v01_luke"),
//    		}
//        );
	}

	@Test
	void testDimorphosIdealImpact9_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("ideal-impact9-20210630-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("ideal-impact9-20210630-v01"));
        assertEquals(c.modelLabel, "Ideal Impact 9 20210630 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/ideal-impact9-20210630-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/ideal-impact9-20210630-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3072 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3072));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact9-20210630-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/ideal-impact9-20210630-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/ideal-impact9-20210630-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact9-20210630-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/ideal-impact9-20210630-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/ideal-impact9-20210630-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/ideal-impact9-20210630-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/ideal-impact9-20210630-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/ideal-impact9-20210630-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), false);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact9-20210630-v01/draco/imagelist-fullpath-info.txt", "dimorphos_ideal_impact9_20210630_v01_draco"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact9-20210630-v01/leia/imagelist-fullpath-info.txt", "dimorphos_ideal_impact9_20210630_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/ideal-impact9-20210630-v01/luke/imagelist-fullpath-info.txt", "dimorphos_ideal_impact9_20210630_v01_luke"),
    		}
        );
	}

	@Test
	void testDidymosErrorImpact1_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact2_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact3_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact4_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact4RA_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact5_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosErrorImpact6_20200629_v01()
	{
		//NOT USED
	}

	@Test
	void testDidymosErrorImpact9_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact1_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact2_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact3_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact4_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("errors-impact4-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("errors-impact4-20200629-v01"));
        assertEquals(c.modelLabel, "Errors Impact 4 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/errors-impact4-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/errors-impact4-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/errors-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/errors-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/errors-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/errors-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/errors-impact4-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/errors-impact4-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact4-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact4-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact4-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact4-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosErrorImpact4RA_20200629_v01()
	{
		//DOESN'T EXIST
//		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("errors-impact4-ra-20210211-v01")));
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//
//        String label = "Dimorphos-v004";
//        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
//        assertEquals(c.author, ShapeModelType.provide("errors-impact4-ra-20210211-v01"));
//        assertEquals(c.modelLabel, "Errors Impact 4 RA 20210211 v01");
//        assertEquals(c.type, BodyType.ASTEROID);
//        assertEquals(c.population, ShapeModelPopulation.NEO);
//        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
//        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
//        assertEquals(c.density, 0.0);
//        assertEquals(c.rotationRate, 0.0);
//        assertEquals(c.hasSystemBodies, false);
//        assertEquals(c.systemConfigs, Lists.newArrayList());
//        assertEquals(c.hasDTMs, true);
//        assertEquals(c.hasLineamentData, false);
//        assertEquals(c.hasMapmaker, false);
//        assertEquals(c.hasColoringData, true);
//        assertEquals(c.hasCustomBodyCubeSize, false);
//        assertEquals(c.customBodyCubeSize, 0.0);
//        assertEquals(c.useMinimumReferencePotential, false);
//        assertEquals(c.bodyReferencePotential, 0.0);
//        assertEquals(c.getBaseMapConfigName(), "config.txt");
//        assertEquals(c.bodyLowestResModelName, "");
//        assertEquals(c.rootDirOnServer, "/dimorphos/errors-impact4-ra-20210211-v01");
//        assertEquals(c.getShapeModelFileExtension(), ".obj");
//        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
//        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/errors-impact4-ra-20210211-v01/shape/", "shape0.obj.gz"));
//        assertEquals(c.getResolutionLabels(), ImmutableList.of("3145728 plates"));
//        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3145728));
//
//        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
//        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
//        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
//        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);
//
//        assertEquals(imagingConfig.imagingInstruments.size(), 3);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-ra-20210211-v01" + "/draco");
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/errors-impact4-20200629-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/errors-impact4-20200629-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
//        Arrays.sort(fillValues);
//        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});
//
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-ra-20210211-v01" + "/leia");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/errors-impact4-ra-20210211-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/errors-impact4-ra-20210211-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
//        Arrays.sort(fillValues2);
//        assertArrayEquals(fillValues2, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});
//
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/errors-impact4-ra-20210211-v01" + "/luke");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/errors-impact4-ra-20210211-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/errors-impact4-ra-20210211-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), false);
//        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
//        Arrays.sort(fillValues3);
//        assertArrayEquals(fillValues3, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});
//
//
//        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
//        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
//        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);
//
//        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
//        assertEquals(spectrumConfig.hasSpectralData, false);
//
//        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
//        assertEquals(lidarConfig.hasLidarData, false);
//
////        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());
//
//        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
//        assertArrayEquals(c.defaultForMissions, new Mission[] {});
//
//        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
//            {
//                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/errors-impact4-ra-20210211-v01/draco/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_ra_20210211_v01_draco"),
//                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/errors-impact4-ra-20210211-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_ra_20210211_v01_leia"),
//                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
//                		"/dimorphos/errors-impact4-ra-20210211-v01/luke/imagelist-fullpath-info.txt", "dimorphos_errors_impact4_ra_20210211_v01_luke"),
//    		}
//        );
	}

	@Test
	void testDimorphoserrorsImpact5_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("errors-impact5-20200629-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("errors-impact5-20200629-v01"));
        assertEquals(c.modelLabel, "Errors Impact 5 20200629 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/errors-impact5-20200629-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/errors-impact5-20200629-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3366134 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3366134));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 3);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/errors-impact5-20200629-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/errors-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/errors-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/errors-impact5-20200629-v01" + "/leia");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/errors-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/errors-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.CORRECTED_SPICE, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.CORRECTED_SPICE).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/errors-impact5-20200629-v01" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/errors-impact5-20200629-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/errors-impact5-20200629-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 270.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
        Arrays.sort(fillValues3);
        assertArrayEquals(fillValues3, new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact5-20200629-v01/draco/imagelist-fullpath-info.txt", "dimorphos_errors_impact5_20200629_v01_draco"),
                new DBRunInfo(PointingSource.CORRECTED_SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact5-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact5_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact5-20200629-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact5_20200629_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact5-20200629-v01/luke/imagelist-fullpath-info.txt", "dimorphos_errors_impact5_20200629_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphoserrorsImpact6_20200629_v01()
	{
		//NOT USED
	}

	@Test
	void testDimorphoserrorsImpact9_20200629_v01()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("errors-impact9-20210630-v01")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("errors-impact9-20210630-v01"));
        assertEquals(c.modelLabel, "Errors Impact 9 20210630 v01");
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
        assertEquals(c.density, 0.0);
        assertEquals(c.rotationRate, 0.0);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/errors-impact9-20210630-v01");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/errors-impact9-20210630-v01/shape/", "shape0.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of("3072 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(3072));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 10000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1000.0);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/errors-impact9-20210630-v01" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart/draco/errors-impact9-20210630-v01//images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart/draco/errors-impact9-20210630-v01/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-32768.0, -32767.0, 4095.0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/dimorphos/errors-impact9-20210630-v01" + "/leia");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart/leia/errors-impact9-20210630-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart/leia/errors-impact9-20210630-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LEIA_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LEIA);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
//        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).isTranspose(), true);
//        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
//        Arrays.sort(fillValues2);
//        assertArrayEquals(fillValues2, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});
//
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), "/dimorphos/errors-impact9-20210630-v01" + "/luke");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), "/dart/luke/errors-impact9-20210630-v01//images");
//        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), "/dart/luke/errors-impact9-20210630-v01/gallery");
//        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("LUKE_IMAGE"));
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, new PointingSource[] { PointingSource.SPICE });
//        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.LUKE);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
//        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.SPICE).isTranspose(), false);
//        double[] fillValues3 = imagingConfig.imagingInstruments.get(2).getFillValues();
//        Arrays.sort(fillValues3);
//        assertArrayEquals(fillValues3, new double[] {});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getLinearInterpolationDims(), new int[] {0,0,0,0});
//        assertArrayEquals(imagingConfig.imagingInstruments.get(2).getMaskValues(), new int[] {0,0,0,0});


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, InternalClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact9-20210630-v01/draco/imagelist-fullpath-info.txt", "dimorphos_errors_impact9_20210630_v01_draco"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LEIA, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact9-20210630-v01/leia/imagelist-fullpath-info.txt", "dimorphos_errors_impact9_20210630_v01_leia"),
                new DBRunInfo(PointingSource.SPICE, Instrument.LUKE, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/errors-impact9-20210630-v01/luke/imagelist-fullpath-info.txt", "dimorphos_errors_impact9_20210630_v01_luke"),
    		}
        );
	}

	@Test
	void testDimorphosv002()
	{

	}

	@Test
	void testDimorphosv003()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("dart-dimorphos-v003")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("dart-dimorphos-v003"));
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.density, 2300);
        assertEquals(c.rotationRate, 1.464E-4);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/dart-dimorphos-v003");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/dart-dimorphos-v003/shape/", "shape0.obj.gz", "shape1.obj.gz", "shape2.obj.gz", "shape3.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, DimorphosImpactSearchStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, DimorphosImpactSearchEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, DimorphosImpactMaxScDistance);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, DimorphosImpactResolution);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/dart-dimorphos-v003" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart" + "/draco/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart" + "/draco/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 180.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-1e10f, -1e09f, 4095f, 1e09f, 1e10f});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testUpdatedStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, ClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/dart-dimorphos-v003/draco/imagelist-fullpath-sum.txt", "dimorphos_dart_dimorphos_v003_draco"),
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/dart-dimorphos-v003/draco/imagelist-fullpath-info.txt", "dimorphos_dart_dimorphos_v003_draco"),
    		}
        );
	}

	@Test
	void testDimorphosv004()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIMORPHOS, ShapeModelType.provide("dart-dimorphos-v004")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Dimorphos-v004";
        assertEquals(c.body, ShapeModelBody.DIMORPHOS);
        assertEquals(c.author, ShapeModelType.provide("dart-dimorphos-v004"));
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.density, 2400);
        assertEquals(c.rotationRate, 1.464E-4);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/dimorphos/dart-dimorphos-v004");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/dimorphos/dart-dimorphos-v004/shape/", "shape0.obj.gz", "shape1.obj.gz", "shape2.obj.gz", "shape3.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, DimorphosImpactSearchStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, DimorphosImpactSearchEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, DimorphosImpactMaxScDistance);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, DimorphosImpactResolution);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/dimorphos/dart-dimorphos-v004" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart" + "/draco/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart" + "/draco/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.GASKELL, PointingSource.SPICE });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 180.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-1e10f, -1e09f, 4095f, 1e09f, 1e10f});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testUpdatedStateHistoryParameters(c, ShapeModelBody.DIMORPHOS.name());

        assertArrayEquals(c.presentInMissions, ClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, DartClients);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/dart-dimorphos-v004/draco/imagelist-fullpath-sum.txt", "dimorphos_dart_dimorphos_v004_draco"),
                new DBRunInfo(PointingSource.SPICE, Instrument.DRACO, ShapeModelBody.DIMORPHOS.toString(),
                		"/dimorphos/dart-dimorphos-v004/draco/imagelist-fullpath-info.txt", "dimorphos_dart_dimorphos_v004_draco"),
    		}
        );
	}

	@Test
	void testDidymosv001()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIDYMOS, ShapeModelType.provide("dart-didymos-v001")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Didymos-v003";
        assertEquals(c.body, ShapeModelBody.DIDYMOS);
        assertEquals(c.author, ShapeModelType.provide("dart-didymos-v001"));
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.density, 2400);
        assertEquals(c.rotationRate, 7.72E-4);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/didymos/dart-didymos-v001");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/didymos/dart-didymos-v001/shape/", "shape0.obj.gz", "shape1.obj.gz", "shape2.obj.gz", "shape3.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, DidymosImpactSearchStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, DidymosImpactSearchEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, DidymosImpactMaxScDistance);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, DidymosImpactResolution);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/didymos/dart-didymos-v001" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart" + "/draco/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart" + "/draco/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.GASKELL });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 180.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-1e10f, -1e09f, 4095f, 1e09f, 1e10f});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testUpdatedStateHistoryParameters(c, ShapeModelBody.DIDYMOS.name());

        assertArrayEquals(c.presentInMissions, ClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.DRACO, ShapeModelBody.DIDYMOS.toString(),
                		"/didymos/dart-didymos-v001/draco/imagelist-fullpath-sum.txt", "didymos_dart_didymos_v001_draco"),
    		}
        );
	}

	@Test
	void testDidymosv003()
	{
		SmallBodyViewConfig c = (SmallBodyViewConfig)(SmallBodyViewConfig.getConfig(ShapeModelBody.DIDYMOS, ShapeModelType.provide("dart-didymos-v003")));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        String label = "Didymos-v003";
        assertEquals(c.body, ShapeModelBody.DIDYMOS);
        assertEquals(c.author, ShapeModelType.provide("dart-didymos-v003"));
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.system, ShapeModelBody.DIDYMOS_SYSTEM);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.density, 2834);
        assertEquals(c.rotationRate, 7.7227E-4);
        assertEquals(c.hasSystemBodies, false);
        assertEquals(c.systemConfigs, Lists.newArrayList());
        assertEquals(c.hasDTMs, true);
        assertEquals(c.hasLineamentData, false);
        assertEquals(c.hasMapmaker, false);
        assertEquals(c.hasColoringData, true);
        assertEquals(c.hasCustomBodyCubeSize, false);
        assertEquals(c.customBodyCubeSize, 0.0);
        assertEquals(c.useMinimumReferencePotential, false);
        assertEquals(c.bodyReferencePotential, 0.0);
        assertEquals(c.getBaseMapConfigName(), "config.txt");
        assertEquals(c.bodyLowestResModelName, "");
        assertEquals(c.rootDirOnServer, "/didymos/dart-didymos-v003");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getShapeModelFileBaseName(), "shape/shape");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/didymos/dart-didymos-v003/shape/", "shape0.obj.gz", "shape1.obj.gz", "shape2.obj.gz", "shape3.obj.gz"));
        assertEquals(c.getResolutionLabels(), ImmutableList.of(
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2],
        			BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, DidymosLiciaSearchStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, DidymosLiciaSearchEndDate);
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, DidymosImpactMaxScDistance);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, DidymosImpactResolution);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/didymos/dart-didymos-v003" + "/draco");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/dart" + "/draco/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/dart" + "/draco/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("DRACO_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[] { PointingSource.GASKELL });
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.DRACO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).isTranspose(), true);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 180.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).isTranspose(), true);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-1e10f, -1e09f, 4095f, 1e09f, 1e10f});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {0,0,0,0});

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/didymos/dart-didymos-v003" + "/luke");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/dart" + "/luke/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), "/dart" + "/luke/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MULTI);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("LUKE_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[] { PointingSource.GASKELL });
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.LUKE);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).isTranspose(), false);
        double[] fillValues2 = imagingConfig.imagingInstruments.get(1).getFillValues();
        Arrays.sort(fillValues2);
        assertArrayEquals(fillValues2, new double[] {-1.0E30f, 1.0E30f, 1.0E32f});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getLinearInterpolationDims(), new int[] {0,0,0,0});
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).getMaskValues(), new int[] {0,0,0,0});

        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.hasHierarchicalImageSearch(), false);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, false);

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, false);

        testUpdatedStateHistoryParameters(c, ShapeModelBody.DIDYMOS.name());

        assertArrayEquals(c.presentInMissions, ClientsWithDartModels);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
            {
                new DBRunInfo(PointingSource.GASKELL, Instrument.DRACO, ShapeModelBody.DIDYMOS.toString(),
                		"/didymos/dart-didymos-v003/draco/imagelist-fullpath-sum.txt", "didymos_dart_didymos_v003_draco"),
                new DBRunInfo(PointingSource.GASKELL, Instrument.LUKE, ShapeModelBody.DIDYMOS.toString(),
                		"/didymos/dart-didymos-v003/luke/imagelist-fullpath-sum.txt", "didymos_dart_didymos_v003_luke"),
    		}
        );

	}

	@Test
	void testDimorphosv003Didymosv001()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosv004Didymosv003()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosv001Dimorphosv003()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosv003Dimorphosv004()
	{
		fail("Not yet implemented");
	}

    private static void testStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
    {
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.stateHistoryStartDate, new GregorianCalendar(2022, 9, 1, 10, 25, 8).getTime());
        assertEquals(stateHistoryConfig.stateHistoryEndDate, new GregorianCalendar(2022, 9, 1, 10, 28, 36).getTime());
        SpiceInfo spiceInfo1 = new SpiceInfo(MissionPrefix, "920065803_FIXED", "DART_SPACECRAFT", "DIDYMOS", new String[] { "EARTH", "SUN", "DIMORPHOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIMORPHOS"}, new String[] { "DART_DRACO_2X2", "120065803_FIXED" }, new String[] {});
        SpiceInfo spiceInfo2 = new SpiceInfo(MissionPrefix, "120065803_FIXED", "DART_SPACECRAFT", "DIMORPHOS", new String[] { "EARTH", "SUN", "DIDYMOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIDYMOS"}, new String[] { "DART_DRACO_2X2", "920065803_FIXED" }, new String[] {});
        SpiceInfo[] spiceInfos = new SpiceInfo[] { spiceInfo1, spiceInfo2 };
        stateHistoryConfig.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);

		assertEquals(stateHistoryConfig.spiceInfo.getScId(), MissionPrefix);
		assertEquals(stateHistoryConfig.spiceInfo.getScFrameName(), "DART_SPACECRAFT");

		if (stateHistoryConfig.spiceInfo.getBodyName().equals("DIDYMOS"))
		{
			assertEquals(stateHistoryConfig.spiceInfo.getBodyFrameName(), "920065803_FIXED");
			assertEquals(stateHistoryConfig.spiceInfo.getBodyName(), "DIDYMOS");
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyNamesToBind(), new String[] {"EARTH", "SUN", "DIMORPHOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyFramesToBind(), new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_DIMORPHOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentNamesToBind(), new String[] {"DART_DRACO_2X2", "120065803_FIXED"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentFrameNamesToBind(), new String[] {});
		}
		else
		{
			assertEquals(stateHistoryConfig.spiceInfo.getBodyFrameName(), "120065803_FIXED");
			assertEquals(stateHistoryConfig.spiceInfo.getBodyName(), "DIMORPHOS");
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyNamesToBind(), new String[] {"EARTH" , "SUN", "DIDYMOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyFramesToBind(), new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_DIDYMOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentNamesToBind(), new String[] {"DART_DRACO_2X2", "920065803_FIXED"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentFrameNamesToBind(), new String[] {});
		}

    }

    private static void testUpdatedStateHistoryParameters(SmallBodyViewConfig c, String centerBodyName)
    {
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.stateHistoryStartDate, new GregorianCalendar(2022, 8, 26, 23, 10, 18).getTime());
//        assertEquals(stateHistoryConfig.stateHistoryEndDate, new GregorianCalendar(2022, 8, 26, 23, 14, 24).getTime());
        assertEquals(stateHistoryConfig.stateHistoryEndDate.getTime(), new GregorianCalendar(2022, 8, 26, 23, 14, 24).getTime().getTime() + 193);
        SpiceInfo spiceInfo1 = new SpiceInfo(MissionPrefix, "IAU_DIDYMOS", "DART_SPACECRAFT", "DIDYMOS", new String[] { "EARTH", "SUN", "DIMORPHOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIMORPHOS"}, new String[] { "DART_DRACO_2X2" }, new String[] {});
        SpiceInfo spiceInfo2 = new SpiceInfo(MissionPrefix, "IAU_DIMORPHOS", "DART_SPACECRAFT", "DIMORPHOS", new String[] { "EARTH", "SUN", "DIDYMOS" }, new String[] {"IAU_EARTH", "IAU_SUN", "IAU_DIDYMOS"}, new String[] { "DART_DRACO_2X2" }, new String[] {});
        SpiceInfo[] spiceInfos = new SpiceInfo[] { spiceInfo1, spiceInfo2 };
        stateHistoryConfig.spiceInfo = List.of(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).toList().get(0);


        assertEquals(stateHistoryConfig.spiceInfo.getScId(), MissionPrefix);
		assertEquals(stateHistoryConfig.spiceInfo.getScFrameName(), "DART_SPACECRAFT");

		if (stateHistoryConfig.spiceInfo.getBodyName().equals("DIDYMOS"))
		{
			assertEquals(stateHistoryConfig.spiceInfo.getBodyFrameName(), "IAU_DIDYMOS");
			assertEquals(stateHistoryConfig.spiceInfo.getBodyName(), "DIDYMOS");
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyNamesToBind(), new String[] {"EARTH", "SUN", "DIMORPHOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyFramesToBind(), new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_DIMORPHOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentNamesToBind(), new String[] {"DART_DRACO_2X2"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentFrameNamesToBind(), new String[] {});
		}
		else
		{
			assertEquals(stateHistoryConfig.spiceInfo.getBodyFrameName(), "IAU_DIMORPHOS");
			assertEquals(stateHistoryConfig.spiceInfo.getBodyName(), "DIMORPHOS");
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyNamesToBind(), new String[] {"EARTH" , "SUN", "DIDYMOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getBodyFramesToBind(), new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_DIDYMOS"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentNamesToBind(), new String[] {"DART_DRACO_2X2"});
			assertArrayEquals(stateHistoryConfig.spiceInfo.getInstrumentFrameNamesToBind(), new String[] {});
		}

    }

}
