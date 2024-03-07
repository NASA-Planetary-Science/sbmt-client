package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
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

class SaturnConfigsTest
{

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
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
		SaturnConfigs.initialize(builtInConfigs);

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
	void testDioneGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.DIONE, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.DIONE);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2013a)");
        assertEquals(c.rootDirOnServer, "/GASKELL/DIONE");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of("/GASKELL/DIONE/IMAGING", "", null, null, "/GASKELL/DIONE/IMAGING/gallery");

//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new FixedListDataQuery(imagingMetadata),
//                        ImageType.SATURN_MOON_IMAGE, //
//                        new PointingSource[]{PointingSource.GASKELL}, //
//                        Instrument.IMAGING_DATA //
//                        ) //
//        );

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/DIONE/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/DIONE/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testEpimetheusThomas2000()
	{
//		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.THOMAS);
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//
//        assertEquals(c.body, ShapeModelBody.EPIMETHEUS);
//        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
//        assertEquals(c.population, ShapeModelPopulation.SATURN);
//        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
//        assertEquals(c.author, ShapeModelType.THOMAS);
//        assertEquals(c.modelLabel, "Thomas (2000)");
//        assertEquals(c.rootDirOnServer, "/THOMAS/EPIMETHEUS");
//        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "s11epimetheus.llr.gz"));
//        assertEquals(c.hasColoringData, false);
//        assertEquals(c.getResolutionNumberElements() , ImmutableList.of(5040));
//        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
//        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testEpimetheusStooke()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.EPIMETHEUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/epimetheus/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHyperionGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HYPERION, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HYPERION);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/GASKELL/HYPERION");
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHyperionThomas()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HYPERION, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HYPERION);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/HYPERION");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "s7hyperion.llr.gz"));
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testJanusThomas2000()
	{
//		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.THOMAS);
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//
//        assertEquals(c.body, ShapeModelBody.JANUS);
//        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
//        assertEquals(c.population, ShapeModelPopulation.SATURN);
//        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
//        assertEquals(c.author, ShapeModelType.THOMAS);
//        assertEquals(c.modelLabel, "Thomas (2000)");
//        assertEquals(c.rootDirOnServer, "/THOMAS/JANUS");
//        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "s10janus.llr.gz"));
//        assertEquals(c.hasColoringData, false);
//        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));
//        assertArrayEquals(c.presentInMissions, new Mission[]
//		{ Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL,
//				Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
//        assertArrayEquals(c.defaultForMissions, new Mission[]{});
	}

	@Test
	void testJanusStooke()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.JANUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/janus/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testMimasGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.MIMAS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.MIMAS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2013b)");
        assertEquals(c.rootDirOnServer, "/GASKELL/MIMAS");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/MIMAS/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/MIMAS/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPandoraStooke()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PANDORA, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PANDORA);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/pandora/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements() ,ImmutableList.of(5040));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPhoebeGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOEBE, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOEBE);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2013c)");
        assertEquals(c.rootDirOnServer, "/GASKELL/PHOEBE");

//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of("/GASKELL/PHOEBE/IMAGING", "", null, null, "/GASKELL/PHOEBE/IMAGING/gallery");
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/PHOEBE/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/PHOEBE/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new FixedListDataQuery(imagingMetadata),
////	                        new FixedListQuery<Object>("/GASKELL/PHOEBE/IMAGING", "/GASKELL/PHOEBE/IMAGING/gallery"), //
//                        ImageType.SATURN_MOON_IMAGE, //
//                        new PointingSource[]{PointingSource.GASKELL}, //
//                        Instrument.IMAGING_DATA //
//                        ) //
//        );

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});

        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPrometheusStooke()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PROMETHEUS, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PROMETHEUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/prometheus/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testRheaGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.RHEA, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.RHEA);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (in progress)");
        assertEquals(c.rootDirOnServer, "/GASKELL/RHEA");
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testTethysGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.TETHYS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.TETHYS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2013d)");
        assertEquals(c.rootDirOnServer, "/GASKELL/TETHYS");
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testTelestoGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.TELESTO, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.TELESTO);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/GASKELL/TELESTO");

//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of("/GASKELL/TELESTO/IMAGING", "", null, null, "/GASKELL/TELESTO/IMAGING/gallery");
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/TELESTO/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/TELESTO/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {-3.4028234663852886e38, 3.4028234663852886e38});

//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new FixedListDataQuery(imagingMetadata),
////                        new FixedListQuery<Object>("/GASKELL/TELESTO/IMAGING", "/GASKELL/TELESTO/IMAGING/gallery"), //
//                        ImageType.SATURN_MOON_IMAGE, //
//                        new PointingSource[]{PointingSource.GASKELL}, //
//                        Instrument.IMAGING_DATA, //
//                        0.0,
//                        "None",
//                        Set.of((float)3.4028234663852886e38, (float)-3.4028234663852886e38)
//                        ) //
//        );

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testAtlasGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.ATLAS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.ATLAS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/atlas/gaskell");
        assertEquals(c.getResolutionLabels(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(BodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/atlas/gaskell/imaging");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/atlas/gaskell/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/atlas/gaskell/imaging/images/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 5, 7, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 3, 13, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 400000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 5000.0);
        assertEquals(c.hasColoringData, false);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.ATLAS.toString(), "/project/sbmt2/data/atlas/gaskell/imaging/imagelist-fullpath.txt", "atlas"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testCalypsoGaskell()
	{
		//not currently used
//		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.CALYPSO, ShapeModelType.GASKELL);
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
	}

	@Test
	void testCalypsoDaly()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.CALYPSO, ShapeModelType.provide("Daly"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.CALYPSO);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("Daly"));
        assertEquals(c.modelLabel, "Daly et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/calypso/daly-2020");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);


        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();
//
        String issTable = tableBaseName + "iss";
//
        String issRootDirPrimary = c.rootDirOnServer + "/iss";
//
        String issDataDir = "/cassini/iss/images";
        String issGalleryDir = "/cassini/iss/gallery";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), issRootDirPrimary);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), issDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), issGalleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e6);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e3);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testCalypsoThomas()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.CALYPSO, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.CALYPSO);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2018)");
        assertEquals(c.rootDirOnServer, "/calypso/thomas-2018");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        int numberPlates = 28269;
        assertEquals(c.getResolutionLabels(), ImmutableList.of(numberPlates + " plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(numberPlates));

        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String issTable = tableBaseName + "iss";

        String issDataDir = "/cassini/iss/images";
        String issGalleryDir = "/cassini/iss/gallery";
        String issRootDirPrimary = c.rootDirOnServer + "/iss";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), issRootDirPrimary);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), issDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), issGalleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);
//
//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(imagingMetadata),
////                        new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
//                        ImageType.valueOf("ISS_IMAGE"), //
//                        SumFiles, //
//                        Instrument.ISS, //
//                        0., //
//                        "None" //
//                )
//        );

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e6);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e3);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testEnceladusGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.ENCELADUS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.ENCELADUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (in progress)");
        assertEquals(c.rootDirOnServer, "/enceladus/gaskell");
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testEpimetheusGaskell()
	{
		//not currently used
//		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.GASKELL);
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
	}

	@Test
	void testEpimetheusDaly()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.provide("Daly"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.EPIMETHEUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("Daly"));
        assertEquals(c.modelLabel, "Daly et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/epimetheus/daly-2020");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);

        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String issTable = tableBaseName + "iss";
        String issRootDirPrimary = c.rootDirOnServer + "/iss";

        String issDataDir = "/cassini/iss/images";
        String issGalleryDir = "/cassini/iss/gallery";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);
//
//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(imagingMetadata),
////                        new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
//                        ImageType.valueOf("ISS_IMAGE"), //
//                        SumFiles, //
//                        Instrument.ISS, //
//                        0., //
//                        "None" //
//                )
//        );

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), issRootDirPrimary);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), issDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), issGalleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 4, 4, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e7);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e5);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testEpimetheusThomas()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.EPIMETHEUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2018)");
        assertEquals(c.rootDirOnServer, "/epimetheus/thomas-2018");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        int numberPlates = 49152;
        assertEquals(c.getResolutionLabels(), ImmutableList.of(numberPlates + " plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(numberPlates));

        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String issTable = tableBaseName + "iss";

        // DO NOT SET THIS VARIABLE: use the SUM files that belong to
        // the Primary model.
        // issRootDirPrimary = c.rootDirOnServer + "/iss";

//        String issDataDir = "/cassini/iss/images";
//        String issGalleryDir = "/cassini/iss/gallery";
        String issRootDirPrimary = c.rootDirOnServer + "/iss";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);
//
//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(imagingMetadata),
////                        new GenericPhpQuery(issRootDirPrimary, issTable, issTable, issGalleryDir, issDataDir), //
//                        ImageType.valueOf("ISS_IMAGE"), //
//                        SumFiles, //
//                        Instrument.ISS, //
//                        0., //
//                        "None" //
//                )
//        );

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), issRootDirPrimary);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/cassini/iss/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/cassini/iss/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 4, 4, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e7);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e5);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHeleneGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HELENE, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HELENE);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/helene/gaskell");
        assertEquals(c.hasColoringData, false);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/helene/gaskell/imaging/");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/helene/gaskell/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
        double[] fillValues = imagingConfig.imagingInstruments.get(0).getFillValues();
        Arrays.sort(fillValues);
        assertArrayEquals(fillValues, new double[] {(double)-3.4028234663852886e38, (double)3.4028234663852886e38});

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime());

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testIapetusGaskell()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.IAPETUS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.IAPETUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (in progress)");
        assertEquals(c.rootDirOnServer, "/iapetus/gaskell");
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testJanusDaly()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.provide("Daly"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.JANUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("Daly"));
        assertEquals(c.modelLabel, "Daly et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/janus/daly-2020");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);

        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String issTable = tableBaseName + "iss";
        String issRootDirPrimary = c.rootDirOnServer + "/iss";

        String issDataDir = "/cassini/iss/images";
        String issGalleryDir = "/cassini/iss/gallery";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);
//
//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(imagingMetadata),
//                        ImageType.valueOf("ISS_IMAGE"), //
//                        SumFiles, //
//                        Instrument.ISS, //
//                        0., //
//                        "None" //
//                )
//        );

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/iss");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), issDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), issGalleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 3, 19, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e7);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e5);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testJanusErnst()
	{
		//not currently used
//		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.GASKELL);
//		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
//        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
	}

	@Test
	void testJanusThomas2018()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.JANUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2018)");
        assertEquals(c.rootDirOnServer, "/janus/thomas-2018");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.hasColoringData, false);
        int numberPlates = 49152;
        assertEquals(c.getResolutionLabels(), ImmutableList.of(numberPlates + " plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(numberPlates));

        String tableBaseName = (c.body.name() + "_" + c.author.toString() + "_").replaceAll("[\\s-]", "_").toLowerCase();

        String issTable = tableBaseName + "iss";

        String issDataDir = "/cassini/iss/images";
        String issGalleryDir = "/cassini/iss/gallery";
        String issRootDirPrimary = c.rootDirOnServer + "/iss";
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        DataQuerySourcesMetadata imagingMetadata =
//        		DataQuerySourcesMetadata.of(issRootDirPrimary,issDataDir, issTable, issTable, issGalleryDir);

//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(imagingMetadata),
//                        ImageType.valueOf("ISS_IMAGE"), //
//                        SumFiles, //
//                        Instrument.ISS, //
//                        0., //
//                        "None" //
//                )
//        );

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/iss");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), issDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), issGalleryDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ISS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.ISS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 1, 17, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 3, 19, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e7);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e5);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ISS, c.body.toString(), //
                        issRootDirPrimary + "/imagelist-fullpath-sum.txt", issTable) //
        });

        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
                Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPan()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PAN, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PAN);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/pan/gaskell");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/pan/gaskell/imaging/");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/pan/gaskell/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime());
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPandora()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PANDORA, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PANDORA);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/pandora/gaskell");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/pandora/gaskell/imaging/");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/pandora/gaskell/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 4, 20, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 11, 19, 0, 0, 0).getTime());
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPrometheus()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PROMETHEUS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PROMETHEUS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.SATURN);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/prometheus/gaskell");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/prometheus/gaskell/imaging/");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/prometheus/gaskell/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.SATURN_MOON_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime());
        assertEquals(c.hasColoringData, false);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

}
