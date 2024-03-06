package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
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
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfigIO;

import crucible.crust.metadata.impl.InstanceGetter;

class NewHorizonsConfigsTest
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
		NewHorizonsConfigs.initialize(builtInConfigs);

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

	private static Mission[] presentMissions = new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,Mission.STAGE_APL_INTERNAL,
			Mission.NH_DEPLOY};

	@Test
	void testPluto()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PLUTO, ShapeModelType.NIMMO);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PLUTO);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.NIMMO);
        assertEquals(c.modelLabel, "Nimmo et al. (2017)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/PLUTO");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.obj.gz"));
        assertEquals(c.hasColoringData, false);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/NEWHORIZONS/PLUTO/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/NEWHORIZONS/PLUTO/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED, PointingSource.CORRECTED_SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED_SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED_SPICE).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/NEWHORIZONS/PLUTO/MVIC");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/NEWHORIZONS/PLUTO/MVIC/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MULTI);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MVIC_JUPITER_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MVIC);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e9);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e6);

        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), "/project/nearsdc/data/NEWHORIZONS/PLUTO/IMAGING/imagelist-fullpath.txt", ShapeModelBody.PLUTO.toString().toLowerCase()),
        });

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPlutoTest()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PLUTO, ShapeModelType.provide("pluto-test"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

		assertEquals(c.body, ShapeModelBody.PLUTO);
		assertEquals(c.type, BodyType.KBO);
		assertEquals(c.population, ShapeModelPopulation.PLUTO);
		assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
		assertEquals(c.author, ShapeModelType.provide("pluto-test"));
		assertEquals(c.modelLabel, "Pluto (Test)");
		assertEquals(c.rootDirOnServer, "/pluto/pluto-test");
//		assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.obj.gz"));
		assertEquals(c.hasColoringData, false);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/lorri");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/new-horizons/lorri/pluto-test/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e9);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e6);

        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.PLUTO.toString(), c.rootDirOnServer + "/lorri" + "/imagelist-fullpath-sum.txt", "pluto_pluto_test_lorri"),
        });

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testCharon()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.CHARON, ShapeModelType.NIMMO);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.CHARON);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.NIMMO);
        assertEquals(c.modelLabel, "Nimmo et al. (2017)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/CHARON");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.obj.gz"));
        assertEquals(c.hasColoringData, false);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 2);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/NEWHORIZONS/CHARON/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/NEWHORIZONS/CHARON/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/NEWHORIZONS/CHARON/MVIC");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/NEWHORIZONS/CHARON/MVIC/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MULTI);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MVIC_JUPITER_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MVIC);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e9);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e6);

        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-sum.txt", "charon_nimmo2017_lorri"),
        	new DBRunInfo(PointingSource.SPICE, Instrument.LORRI, ShapeModelBody.CHARON.toString(), "/project/sbmt2/sbmt/data/bodies/charon/nimmo2017/lorri/imagelist-fullpath-info.txt", "charon_nimmo2017_lorri"),
        });

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHydra()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HYDRA, ShapeModelType.WEAVER);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HYDRA);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.WEAVER);
        assertEquals(c.modelLabel, "Weaver et al. (2016)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/HYDRA");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.obj.gz"));
        assertEquals(c.hasColoringData, false);

//        DataQuerySourcesMetadata lorriMetadata =
//        		DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/IMAGING", "", null, null, null);
//        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/HYDRA/MVIC", "", null, null, null);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
//				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

        assertEquals(imagingConfig.imagingInstruments.size(), 2);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/NEWHORIZONS/HYDRA/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/NEWHORIZONS/HYDRA/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/NEWHORIZONS/HYDRA/MVIC");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/NEWHORIZONS/HYDRA/MVIC/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MULTI);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MVIC_JUPITER_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MVIC);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e9);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e6);

        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testKerberos()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.KERBEROS, ShapeModelType.WEAVER);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.KERBEROS);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.WEAVER);
        assertEquals(c.modelLabel, "Weaver et al. (2016)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/KERBEROS");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.vtk.gz"));
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testNyx()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.NIX, ShapeModelType.WEAVER);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.NIX);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.WEAVER);
        assertEquals(c.modelLabel, "Weaver et al. (2016)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/NIX");
        assertArrayEquals(c.getShapeModelFileNames(),  prepend(c.rootDirOnServer, "shape_res0.obj.gz"));
        assertEquals(c.hasColoringData, false);

//        DataQuerySourcesMetadata lorriMetadata =
//        		DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/IMAGING", "", null, null, null);
//        DataQuerySourcesMetadata mvicMetadata = DataQuerySourcesMetadata.of("/NEWHORIZONS/NIX/MVIC", "", null, null, null);
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//        configurePlutoEncounterImaging(imagingConfig, lorriMetadata, mvicMetadata, null,
//				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});

        assertEquals(imagingConfig.imagingInstruments.size(), 2);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/NEWHORIZONS/NIX/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/NEWHORIZONS/NIX/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), "/NEWHORIZONS/NIX/MVIC");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), "/NEWHORIZONS/NIX/MVIC/images");
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MULTI);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.MVIC_JUPITER_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, new PointingSource[]{PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.MVIC);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e9);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e6);

        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testStyx()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.STYX, ShapeModelType.WEAVER);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.STYX);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.PLUTO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.TRIAXIAL);
        assertEquals(c.author, ShapeModelType.WEAVER);
        assertEquals(c.modelLabel, "Weaver et al. (2016)");
        assertEquals(c.rootDirOnServer, "/NEWHORIZONS/STYX");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "shape_res0.vtk.gz"));
        assertEquals(c.hasColoringData, false);
        assertEquals(c.getResolutionNumberElements(), (ImmutableList.of(128880)));

        assertArrayEquals(c.presentInMissions, presentMissions);
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testMU69()
	{
		NewHorizonsConfigs c = (NewHorizonsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.MU69, ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.MU69);
        assertEquals(c.type, BodyType.KBO);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED);
        assertEquals(c.rootDirOnServer, "/mu69/mu69-test5h-1-final-oriented");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        c.setResolution(ImmutableList.of("Very Low (25708 plates)"), ImmutableList.of(25708));

        c.density = Double.NaN;
        c.useMinimumReferencePotential = true;
        c.rotationRate = Double.NaN;

//        DataQuerySourcesMetadata lorriMetadata =
//        		DataQuerySourcesMetadata.of(c.rootDirOnServer + "/lorri", "", null, null, c.rootDirOnServer + "/lorri/gallery");
    	ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
//    	configurePlutoEncounterImaging(imagingConfig, lorriMetadata, null, null,
//				new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});


    	assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), c.rootDirOnServer + "/lorri");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), c.rootDirOnServer + "/lorri/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), c.rootDirOnServer + "/lorri/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.LORRI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.SPICE, PointingSource.CORRECTED});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.LORRI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2018, 11, 31, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2019, 0, 2, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e6);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e4);


        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(spectrumConfig.hasSpectralData, false);
        assertEquals(spectrumConfig.spectralInstruments, new ArrayList<BasicSpectrumInstrument>());


        assertEquals(c.hasStateHistory, false);

        assertEquals(c.hasMapmaker, false);
        assertEquals(spectrumConfig.hasHierarchicalSpectraSearch, false);
        assertEquals(spectrumConfig.hasHypertreeBasedSpectraSearch, false);

        assertEquals(lidarConfig.hasLidarData, false);
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, false);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL, Mission.STAGE_APL_INTERNAL, Mission.NH_DEPLOY});
        assertArrayEquals(c.defaultForMissions, new Mission[] {Mission.NH_DEPLOY});
	}

}
