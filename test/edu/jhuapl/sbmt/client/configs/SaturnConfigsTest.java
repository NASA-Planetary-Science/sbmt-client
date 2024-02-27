package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.client.Mission;
import edu.jhuapl.sbmt.core.config.FeatureConfigIOFactory;
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
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
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
		fail("Not yet implemented");
	}

	@Test
	void testEpimetheusThomas2000()
	{
		fail("Not yet implemented");
	}

	@Test
	void testEpimetheusStooke()
	{
		fail("Not yet implemented");
	}

	@Test
	void testHyperionGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testHyperionThomas()
	{
		fail("Not yet implemented");
	}

	@Test
	void testJanusThomas2000()
	{
		fail("Not yet implemented");
	}

	@Test
	void testJanusStooke()
	{
		fail("Not yet implemented");
	}

	@Test
	void testMimasGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPandoraStooke()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPhoebeGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPrometheusStooke()
	{
		fail("Not yet implemented");
	}

	@Test
	void testRheaGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testTethysGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testTelestoGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testAtlasGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testCalypsoGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testCalypsoDaly()
	{
		fail("Not yet implemented");
	}

	@Test
	void testCalypsoThomas()
	{
		fail("Not yet implemented");
	}

	@Test
	void testEnceladusGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testEpimetheusFaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testEpimetheusDaly()
	{
		fail("Not yet implemented");
	}

	@Test
	void testEpimetheusThomas()
	{
		fail("Not yet implemented");
	}

	@Test
	void testHeleneGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testIaepetusGaskell()
	{
		fail("Not yet implemented");
	}

	@Test
	void testJanusDaly()
	{
		fail("Not yet implemented");
	}

	@Test
	void testJanuErnst()
	{
		fail("Not yet implemented");
	}

	@Test
	void testJanusThomas2018()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPan()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPandora()
	{
		fail("Not yet implemented");
	}

	@Test
	void testPrometheus()
	{
		fail("Not yet implemented");
	}

}
