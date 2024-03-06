package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.GregorianCalendar;

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
import edu.jhuapl.sbmt.core.body.ShapeModelDataUsed;
import edu.jhuapl.sbmt.core.body.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
import edu.jhuapl.sbmt.core.config.Instrument;
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
        DataQuerySourcesMetadata imagingMetadata =
        		DataQuerySourcesMetadata.of("/GASKELL/DIONE/IMAGING", "", null, null, "/GASKELL/DIONE/IMAGING/gallery");

        imagingConfig.imagingInstruments = Lists.newArrayList(
                new ImagingInstrument( //
                        SpectralImageMode.MONO, //
                        new FixedListDataQuery(imagingMetadata),
                        ImageType.SATURN_MOON_IMAGE, //
                        new PointingSource[]{PointingSource.GASKELL}, //
                        Instrument.IMAGING_DATA //
                        ) //
        );

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
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EPIMETHEUS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

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
	}

	@Test
	void testJanusThomas2000()
	{
		SaturnConfigs c = (SaturnConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.JANUS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

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
		c.presentInMissions = new Mission[]
		{ Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL,
				Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL };
		c.defaultForMissions = new Mission[]
		{};
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
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
        fail("Not yet implemented");
	}

}
