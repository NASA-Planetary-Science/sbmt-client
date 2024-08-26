package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.GregorianCalendar;
import java.util.List;

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

class RyuguConfigsTest
{

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
		RyuguConfigs.initialize(builtInConfigs);

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
	void testEarth()
	{
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EARTH, ShapeModelType.JAXA_SFM_v20180627);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.EARTH);
		assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
		assertEquals(c.population, ShapeModelPopulation.EARTH);
		assertEquals(c.dataUsed, ShapeModelDataUsed.WGS84);
		assertEquals(c.getResolutionLabels(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
		assertEquals(c.getShapeModelFileExtension(), ".vtk");

        assertEquals(c.author, ShapeModelType.JAXA_SFM_v20180627);
        assertEquals(c.modelLabel, "Haybusa2-testing");
        assertEquals(c.rootDirOnServer, "/earth/hayabusa2");
        assertEquals(c.hasColoringData, false);
        assertEquals(c.density, 0.00); // (g/cm^3)
        assertEquals(c.rotationRate, 0.0); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig) c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 1);


		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/earth/hayabusa2/tir/");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/earth/hayabusa2/tir/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.TIR_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.TIR);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);


        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[]{});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[]{});

        assertEquals(c.hasMapmaker, false);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 11, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 120000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 300.0);

		assertEquals(spectrumConfig.hasSpectralData, true);
		assertEquals(spectrumConfig.spectralInstruments.get(0), new NIRS3());


        assertEquals(lidarConfig.hasLidarData, false);

        assertArrayEquals(c.presentInMissions, new Mission[] {});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testTruth()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.TRUTH);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.RYUGU);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
		assertEquals(c.getResolutionLabels(), ImmutableList.of("Low (54504 plates)", "High (5450420 plates)"));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(54504, 5450420));
		assertEquals(c.getShapeModelFileExtension(), ".obj");

        assertEquals(c.author, ShapeModelType.TRUTH);
        assertEquals(c.modelLabel, "H2 Simulated Truth");
        assertEquals(c.rootDirOnServer, "/ryugu/truth");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 0.00); // (g/cm^3)
        assertEquals(c.rotationRate, 0.0); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 1);

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/truth/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/truth/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/truth/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_TRUTH_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);


        assertEquals(c.hasMapmaker, false);

        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
		assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});

		assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime());
		assertEquals(imagingConfig.imageSearchDefaultEndDate, null);
		assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 0);
		assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 0);


        testStateHistory(stateHistoryConfig, "/ryugu/truth/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testGaskell()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.RYUGU);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.SIMULATED);
		assertEquals(c.getResolutionLabels(), ImmutableList.of(
                BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
                BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0],
				BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2],
				BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
		assertEquals(c.getShapeModelFileExtension(), ".obj");

        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "H2 Simulated SPC");
        assertEquals(c.rootDirOnServer, "/ryugu/gaskell");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 0.00); // (g/cm^3)
        assertEquals(c.rotationRate, 0.0); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 1);

//        testONC(imagingConfig, "/ryugu/gaskell/onc", "/ryugu/gaskell/onc/images", "/ryugu/gaskell/onc/gallery");

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/gaskell/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/gaskell/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/gaskell/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/gaskell/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });

	}

	@Test
	void testJAXASFM20180627()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180627);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

//        testBodyConfig(c);

        assertEquals(c.body, ShapeModelBody.RYUGU);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
		assertEquals(c.getResolutionLabels(), ImmutableList.of("Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
		assertEquals(c.getShapeModelFileExtension(), ".obj");

        assertEquals(c.author, ShapeModelType.JAXA_SFM_v20180627);
        assertEquals(c.modelLabel, "JAXA-SFM-v20180627");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-sfm-v20180627");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-sfm-v20180627/onc", "/ryugu/jaxa-sfm-v20180627/onc/images", "/ryugu/jaxa-sfm-v20180627/onc/gallery");
		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-sfm-v20180627/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-sfm-v20180627/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-sfm-v20180627/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        testTIR(imagingConfig, "/ryugu/jaxa-sfm-v20180627/tir", "/ryugu/jaxa-sfm-v20180627/tir/images", "/ryugu/jaxa-sfm-v20180627/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180627/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/onc", "jaxasfmv20180627", "ryugu/jaxa-sfm-v20180627/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180627/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180627/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180627/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASFM20180714()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180714);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SFM_v20180714);
        assertEquals(c.modelLabel, "JAXA-SFM-v20180714");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-sfm-v20180714");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-sfm-v20180714/onc", "/ryugu/jaxa-sfm-v20180714/onc/images", "/ryugu/jaxa-sfm-v20180714/onc/gallery");
		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-sfm-v20180714/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-sfm-v20180714/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-sfm-v20180714/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

		testTIR(imagingConfig, "/ryugu/jaxa-sfm-v20180714/tir", "/ryugu/jaxa-sfm-v20180714/tir/images", "/ryugu/jaxa-sfm-v20180714/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180714/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180714/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180714/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180714/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASFM201807252()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180725_2);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SFM_v20180725_2);
        assertEquals(c.modelLabel, "JAXA-SFM-v20180725_2");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-sfm-v20180725-2");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-sfm-v20180725-2/onc", "/ryugu/jaxa-sfm-v20180725-2/onc/images", "/ryugu/jaxa-sfm-v20180725-2/onc/gallery");
		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-sfm-v20180725-2/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-sfm-v20180725-2/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-sfm-v20180725-2/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        testTIR(imagingConfig, "/ryugu/jaxa-sfm-v20180725-2/tir", "/ryugu/jaxa-sfm-v20180725-2/tir/images", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180725-2/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180725-2/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180725-2/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180725-2/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASFM20180804()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SFM_v20180804);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SFM_v20180804);
        assertEquals(c.modelLabel, "JAXA-SFM-v20180804");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-sfm-v20180804");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-sfm-v20180804/onc", "/ryugu/jaxa-sfm-v20180804/onc/images", "/ryugu/jaxa-sfm-v20180804/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-sfm-v20180804/tir", "/ryugu/jaxa-sfm-v20180804/tir/images", "/ryugu/jaxa-sfm-v20180804/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-sfm-v20180804/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/onc", "ryugu_nasa002", "ryugu/jaxa-sfm-v20180804/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-sfm-v20180804/tir", "ryugu_nasa002_tir", "ryugu/jaxa-sfm-v20180804/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180705()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180705);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180705);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180705");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180705");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180705/onc", "/ryugu/jaxa-spc-v20180705/onc/images", "/ryugu/jaxa-spc-v20180705/onc/gallery");
		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-spc-v20180705/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-spc-v20180705/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-spc-v20180705/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

		testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180705/tir", "/ryugu/jaxa-spc-v20180705/tir/images", "/ryugu/jaxa-spc-v20180705/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180705/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180705/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180705/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180705/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180717()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180717);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180717);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180717");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180717");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180717/onc", "/ryugu/jaxa-spc-v20180717/onc/images", "/ryugu/jaxa-spc-v20180717/onc/gallery");
		assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-spc-v20180717/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-spc-v20180717/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-spc-v20180717/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

		testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180717/tir", "/ryugu/jaxa-spc-v20180717/tir/images", "/ryugu/jaxa-spc-v20180717/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180717/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180717/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180717/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180717/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC201807192()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180719_2);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180719_2);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180719_2");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180719-2");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

//        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180719-2/onc", "/ryugu/jaxa-spc-v20180719-2/onc/images", "/ryugu/jaxa-spc-v20180719-2/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180719-2/tir", "/ryugu/jaxa-spc-v20180719-2/tir/images", "/ryugu/jaxa-spc-v20180719-2/tir/gallery");

        assertEquals(c.hasMapmaker, false);

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/jaxa-spc-v20180719-2/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/jaxa-spc-v20180719-2/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/jaxa-spc-v20180719-2/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 90.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180719-2/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180719-2/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180719-2/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180719-2/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180731()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180731);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180731);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180731");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180731");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180731/onc", "/ryugu/jaxa-spc-v20180731/onc/images", "/ryugu/jaxa-spc-v20180731/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180731/tir", "/ryugu/jaxa-spc-v20180731/tir/images", "/ryugu/jaxa-spc-v20180731/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180731/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/onc", "ryugu_nasa002", "ryugu/jaxa-spc-v20180731/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180731/tir", "ryugu_nasa002_tir", "ryugu/jaxa-spc-v20180731/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180810()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180810);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180810);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180810");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180810");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180810/onc", "/ryugu/jaxa-spc-v20180810/onc/images", "/ryugu/jaxa-spc-v20180810/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180810/tir", "/ryugu/jaxa-spc-v20180810/tir/images", "/ryugu/jaxa-spc-v20180810/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180810/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180810/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180810/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180810/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180816()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180816);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180816);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180816");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180816");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180816/onc", "/ryugu/jaxa-spc-v20180816/onc/images", "/ryugu/jaxa-spc-v20180816/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180816/tir", "/ryugu/jaxa-spc-v20180816/tir/images", "/ryugu/jaxa-spc-v20180816/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180816/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180816/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180816/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180816/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {  });
	}

	@Test
	void testJAXASPC20180829()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20180829);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20180829);
        assertEquals(c.modelLabel, "JAXA-SPC-v20180829");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20180829");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-spc-v20180829/onc", "/ryugu/jaxa-spc-v20180829/onc/images", "/ryugu/jaxa-spc-v20180829/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20180829/tir", "/ryugu/jaxa-spc-v20180829/tir/images", "/ryugu/jaxa-spc-v20180829/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20180829/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20180829/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20180829/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20180829/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] { });
	}

	@Test
	void testJAXASPC20181014()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.JAXA_SPC_v20181014);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.JAXA_SPC_v20181014);
        assertEquals(c.modelLabel, "JAXA-SPC-v20181014");
        assertEquals(c.rootDirOnServer, "/ryugu/jaxa-spc-v20181014");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/jaxa-spc-v20181014/onc", "/ryugu/jaxa-spc-v20181014/onc/images", "/ryugu/jaxa-spc-v20181014/onc/gallery");
        testTIR(imagingConfig, "/ryugu/jaxa-spc-v20181014/tir", "/ryugu/jaxa-spc-v20181014/tir/images", "/ryugu/jaxa-spc-v20181014/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testSpectra(spectrumConfig, c.rootDirOnServer, c.instrumentSearchSpecs);
        testStateHistory(stateHistoryConfig, "/ryugu/jaxa-spc-v20181014/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20181014/onc", "ryugu_nasa005", "ryugu/jaxa-spc-v20181014/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/jaxa-spc-v20181014/tir", "ryugu_nasa005_tir", "ryugu/jaxa-spc-v20181014/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] { Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV });
	}

	@Test
	void testNASA001()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_001);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.NASA_001);
        assertEquals(c.modelLabel, "NASA-001");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-001");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 1);

//        testONC(imagingConfig, "/ryugu/nasa-001/onc", "/ryugu/nasa-001/onc/images", "/ryugu/nasa-001/onc/gallery");

        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/ryugu/nasa-001/onc");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/ryugu/nasa-001/onc/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/ryugu/nasa-001/onc/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-001/onc", "ryugu_flight", "ryugu/nasa-001/onc"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNASA002()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_002);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.NASA_002);
        assertEquals(c.modelLabel, "NASA-002");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-002");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/nasa-002/onc", "/ryugu/nasa-002/onc/images", "/ryugu/nasa-002/onc/gallery");
        testTIR(imagingConfig, "/ryugu/nasa-002/tir", "/ryugu/nasa-002/tir/images", "/ryugu/nasa-002/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/nasa-002/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/onc", "ryugu_nasa002", "ryugu/nasa-002/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-002/tir", "ryugu_nasa002_tir", "ryugu/nasa-002/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNASA003()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_003);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.NASA_003);
        assertEquals(c.modelLabel, "NASA-003");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-003");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.500); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/nasa-003/onc", "/ryugu/nasa-003/onc/images", "/ryugu/nasa-003/onc/gallery");
        testTIR(imagingConfig, "/ryugu/nasa-003/tir", "/ryugu/nasa-003/tir/images", "/ryugu/nasa-003/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/nasa-003/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/onc", "ryugu_nasa002", "ryugu/nasa-003/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-003/tir", "ryugu_nasa002_tir", "ryugu/nasa-003/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNASA004()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_004);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.NASA_004);
        assertEquals(c.modelLabel, "NASA-004");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-004");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/nasa-004/onc", "/ryugu/nasa-004/onc/images", "/ryugu/nasa-004/onc/gallery");
        testTIR(imagingConfig, "/ryugu/nasa-004/tir", "/ryugu/nasa-004/tir/images", "/ryugu/nasa-004/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/nasa-004/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/onc", "ryugu_nasa005", "ryugu/nasa-004/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-004/tir", "ryugu_nasa005_tir", "ryugu/nasa-004/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNASA005()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_005);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        testBodyConfig(c);

        assertEquals(c.author, ShapeModelType.NASA_005);
        assertEquals(c.modelLabel, "NASA-005");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-005");
        assertEquals(c.hasColoringData, true);
        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022871); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/nasa-005/onc", "/ryugu/nasa-005/onc/images", "/ryugu/nasa-005/onc/gallery");
        testTIR(imagingConfig, "/ryugu/nasa-005/tir", "/ryugu/nasa-005/tir/images", "/ryugu/nasa-005/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/nasa-005/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/onc", "ryugu_nasa005", "ryugu/nasa-005/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-005/tir", "ryugu_nasa005_tir", "ryugu/nasa-005/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNASA006()
    {
		RyuguConfigs c = (RyuguConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RYUGU, ShapeModelType.NASA_006);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.RYUGU);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.NASA_006);
        assertEquals(c.modelLabel, "NASA-006");
        assertEquals(c.rootDirOnServer, "/ryugu/nasa-006");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, true);

        assertEquals(c.density, 1.200); // (g/cm^3)
        assertEquals(c.rotationRate, 0.00022867); // (rad/sec)

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);

		assertEquals(imagingConfig.imagingInstruments.size(), 2);

        testONC(imagingConfig, "/ryugu/nasa-006/onc", "/ryugu/nasa-006/onc/images", "/ryugu/nasa-006/onc/gallery");
        testTIR(imagingConfig, "/ryugu/nasa-006/tir", "/ryugu/nasa-006/tir/images", "/ryugu/nasa-006/tir/gallery");

        assertEquals(c.hasMapmaker, false);
        testImages(imagingConfig);
        testStateHistory(stateHistoryConfig, "/ryugu/nasa-006/history/timeHistory.bth");
        testLidarConfig(lidarConfig);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.SPICE, Instrument.ONC, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-006/onc", "ryugu_nasa005", "ryugu/nasa-006/onc"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.TIR, ShapeModelBody.RYUGU.toString(), "/project/sbmt2/sbmt/data/bodies/ryugu/nasa-006/tir", "ryugu_nasa005_tir", "ryugu/nasa-006/tir"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

	}

	private static void testONC(ImagingInstrumentConfig imagingConfig, String rootDir, String dataDir, String galleryDir)
	{
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), rootDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), dataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), galleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.ONC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ONC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
	}

	private static void testTIR(ImagingInstrumentConfig imagingConfig, String rootDir, String dataDir, String galleryDir)
	{
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), rootDir);
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), dataDir);
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), galleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.TIR_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.TIR);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
	}

	private static void testBodyConfig(RyuguConfigs c)
	{
		assertEquals(c.body, ShapeModelBody.RYUGU);
		assertEquals(c.type, BodyType.ASTEROID);
		assertEquals(c.population, ShapeModelPopulation.NEO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
		assertEquals(c.getResolutionLabels(), ImmutableList.of(
                "Very Low (12288 plates)", BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1],
                BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12288, BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0],
				BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2],
				BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
		assertEquals(c.getShapeModelFileExtension(), ".obj");

	}

	private static void testImages(ImagingInstrumentConfig imagingConfig)
	{
		assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
		assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});

		assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime());
		assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime());
		assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 120000.0);
		assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 300.0);
	}

	private static void testSpectra(SpectrumInstrumentConfig spectrumConfig, String rootOnServer, List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> searchSpecs)
	{
		assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.spectralInstruments.get(0), new NIRS3());

        assertEquals(spectrumConfig.hasHierarchicalSpectraSearch, true);
        assertEquals(spectrumConfig.hasHypertreeBasedSpectraSearch, false);
        assertEquals(spectrumConfig.spectraSearchDataSourceMap.get("NIRS3"), rootOnServer + "/nirs3/l2c/hypertree/dataSource.spectra");
        assertEquals(spectrumConfig.spectrumMetadataFile, rootOnServer + "/spectraMetadata.json");

        SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO("HAYABUSA2", searchSpecs);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getSelectedDatasets().size(), specIO.getSelectedDatasets().size());
	}

	private static void testStateHistory(StateHistoryConfig stateHistoryConfig, String path)
	{
		assertEquals(stateHistoryConfig.hasStateHistory, true);
		assertEquals(stateHistoryConfig.timeHistoryFile, path);
	}

	private static void testLidarConfig(LidarInstrumentConfig lidarConfig)
	{
		assertEquals(lidarConfig.hasLidarData, true);
		assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true); // enable tree-based lidar searching
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.LASER);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Hayabusa2"), "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.get("Hayabusa2"), "/ryugu/shared/lidar/browse/fileList.txt");
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/ryugu/shared/lidar/browse/fileList.txt");

        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, OlaCubesGenerator.xyzIndices);
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, OlaCubesGenerator.scIndices);
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
