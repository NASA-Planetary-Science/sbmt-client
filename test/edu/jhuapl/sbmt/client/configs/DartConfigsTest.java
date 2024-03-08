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

import crucible.crust.metadata.impl.InstanceGetter;

class DartConfigsTest
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
		fail("Not yet implemented");
	}

	@Test
	void testDidymosIdealImpact9_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact1_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact2_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact3_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact4_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact4RA_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact5_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact6_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosIdealImpact9_20200629_v01()
	{
		fail("Not yet implemented");
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
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact4RA_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact5_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact6_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosErrorImpact9_20200629_v01()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosv002()
	{

	}

	@Test
	void testDimorphosv003()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDimorphosv004()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosv001()
	{
		fail("Not yet implemented");
	}

	@Test
	void testDidymosv003()
	{
		fail("Not yet implemented");
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

}
