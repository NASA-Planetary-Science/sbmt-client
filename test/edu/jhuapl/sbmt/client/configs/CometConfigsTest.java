package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
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
import edu.jhuapl.sbmt.config.BasicConfigInfo;
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
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfigIO;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

class CometConfigsTest
{
	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		String configInfoVersion = BasicConfigInfo.getConfigInfoVersion();
		System.setProperty("edu.jhuapl.sbmt.mission", "TEST_APL_INTERNAL");
		@SuppressWarnings("unused")
		SettableMetadata allBodiesMetadata = SettableMetadata.of(Version.of(configInfoVersion));
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
		CometConfigs.initialize(builtInConfigs);

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

	private static String[] filterNamesV03 = {
	            // If a name begins with a star, it is not selected by
	            // default
	            "*Filter 1,2", //
	            "*Filter 1,6", //
	            "*Filter 1,8", //
	            "Filter 2,2", //
	            "*Filter 2,3", //
	            "*Filter 2,4", //
	            "*Filter 2,7", //
	            "*Filter 2,8", //
	            "*Filter 4,1", //
	            "*Filter 5,1", //
	            "*Filter 5,4", //
	            "*Filter 6,1" //
	    };

	private static String[] filterNamesV2 = {
	            // If a name, begins with a star, it is not selected by
	            // default
	            "*Filter 1,2", //
	            "*Filter 1,6", //
	            "*Filter 1,8", //
	            "Filter 2,2", //
	            "*Filter 2,3", //
	            "*Filter 2,4", //
	            "*Filter 2,7", //
	            "*Filter 2,8", //
	            "*Filter 4,1", //
	            "*Filter 5,1", //
	            "*Filter 5,4", //
	            "*Filter 6,1", //
	            "*Filter 1,3", //
	            "*Filter 1,5", //
	            "*Filter 1,7", //
	            "*Filter 3,1", //
	            "*Filter 7,1", //
	            "*Filter 8,2", //
	            "*Filter 8,4", //
	            "*Filter 8,7", //
	            "*Filter 8,8" //
	    };

	private static String[] filterNamesV3 = {
	            // If a name, begins with a star, it is not selected by
	            // default
	            "*Filter 1,2", //
	            "*Filter 1,6", //
	            "*Filter 1,8", //
	            "Filter 2,2", //
	            "*Filter 2,3", //
	            "*Filter 2,4", //
	            "*Filter 2,7", //
	            "*Filter 2,8", //
	            "*Filter 4,1", //
	            "*Filter 5,1", //
	            "*Filter 5,4", //
	            "*Filter 6,1", //
	            "*Filter 1,3", //
	            "*Filter 1,5", //
	            "*Filter 1,7", //
	            "*Filter 3,1", //
	            "*Filter 7,1", //
	            "*Filter 8,2", //
	            "*Filter 8,4", //
	            "*Filter 8,7", //
	            "*Filter 8,8", //
	            "*Filter 2,1" //
	    };

	private static final Date ImageSearchDefaultStartDate = new GregorianCalendar(2005, 6, 4, 0, 0, 0).getTime();
    // Months are 0-based: FEBRUARY 16 is 1, 16 not 2, 16.
    private static final Date ImageSearchDefaultEndDate = new GregorianCalendar(2011, 1, 16, 0, 0, 0).getTime();

    private static final PointingSource[] SumFiles = new PointingSource[] { PointingSource.GASKELL };

	@Test
	void test67P()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "SHAP5 V0.3");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.getBody(), ShapeModelBody._67P);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.getAuthor(), ShapeModelType.GASKELL);
        assertEquals(c.getVersion(), "SHAP5 V0.3");
        assertEquals(c.modelLabel, "Gaskell (SHAP5 V0.3)");
        assertEquals(c.getRootDirOnServer(), "/GASKELL/67P");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/67P/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/67P/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/67P/IMAGING/images/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.OSIRIS_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.OSIRIS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.Y);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2014, 7, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2014, 11, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "NAC", "*WAC"});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

	}

	@Test
	void test67PDLR()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody._67P, ShapeModelType.DLR, "SHAP4S");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody._67P);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.DLR);
        assertEquals(c.version, "SHAP4S");
        assertEquals(c.rootDirOnServer, "/DLR/67P");
        assertEquals(c.modelLabel, "DLR (SHAP4S V0.9)");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, //
                "cg-dlr_spg-shap4s-v0.9_64m.ply.gz", "cg-dlr_spg-shap4s-v0.9_32m.ply.gz", "cg-dlr_spg-shap4s-v0.9_16m.ply.gz", "cg-dlr_spg-shap4s-v0.9_8m.ply.gz", "cg-dlr_spg-shap4s-v0.9_4m.ply.gz", "cg-dlr_spg-shap4s-v0.9.ply.gz")); //


        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/DLR/67P/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/DLR/67P/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/DLR/67P/IMAGING/images/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.OSIRIS_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.OSIRIS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.Y);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2014, 7, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2014, 11, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, filterNamesV03);
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "NAC", "*WAC" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);

        assertEquals(c.getResolutionLabels(), ImmutableList.of( //
                "17442 plates ", "72770 plates ", "298442 plates ", "1214922 plates ", //
                "4895631 plates ", "16745283 plates " //
        ));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of( //
                17442, 72770, 298442, 1214922, 4895631, 16745283 //
        ));
        //
        assertEquals(c.hasColoringData, false);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { new DBRunInfo(PointingSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/DLR/67P/IMAGING/imagelist-fullpath.txt", "67p_dlr"),
        });

        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void test67PV2()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "V2");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody._67P);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.version, "V2");
        assertEquals(c.rootDirOnServer, "/GASKELL/67P_V2");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/67P_V2/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/67P_V2/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/67P_V3/IMAGING/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.OSIRIS_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.OSIRIS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.Y);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, filterNamesV2);
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "NAC", "*WAC" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { new DBRunInfo(PointingSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/GASKELL/67P_V2/IMAGING/imagelist-fullpath.txt", "67p_v2"),
        });

        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void test67PV3()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody._67P, ShapeModelType.GASKELL, "V3");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody._67P);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.version, "V3");
        assertEquals(c.rootDirOnServer, "/GASKELL/67P_V3");

        assertEquals(c.hasCustomBodyCubeSize, true);
        assertEquals(c.customBodyCubeSize, 0.10); // km

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/67P_V3/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/67P_V3/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/67P_V3/IMAGING/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.OSIRIS_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.OSIRIS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.Y);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 0, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, filterNamesV3);
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "NAC", "*WAC" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { new DBRunInfo(PointingSource.GASKELL, Instrument.OSIRIS, ShapeModelBody._67P.toString(), "/project/nearsdc/data/GASKELL/67P_V3/IMAGING/imagelist-fullpath.txt", "67p_v3"),
        });

        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHalley()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HALLEY, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HALLEY);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/halley/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.density, 600);
        assertEquals(c.rotationRate, 0.0000323209);
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(5040));

        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testHartleyThomas()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.HARTLEY, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.HARTLEY);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Farnham and Thomas (2013)");
        assertEquals(c.rootDirOnServer, "/THOMAS/HARTLEY");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "hartley2_2012_cart.plt.gz"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(32040));
        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testTempel1Ernst()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.TEMPEL_1, ShapeModelType.provide("ernst-et-al-2019"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.TEMPEL_1);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.modelLabel, "Ernst et al. (2019)");
        assertEquals(c.author, ShapeModelType.provide(c.modelLabel.replaceAll("\\W+", "-").replaceAll("-$", "").toLowerCase()));
        assertEquals(c.rootDirOnServer,  "/tempel1/" + c.author);
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertArrayEquals(c.presentInMissions, new Mission[] { Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL,
				Mission.TEST_APL_INTERNAL, Mission.PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
				Mission.TEST_PUBLIC_RELEASE });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

        String modelId = c.author.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        String bodyId = c.body.name().replaceAll("[\\s-_]+", "-").toLowerCase();
        String tableBaseName = (bodyId + "_" + modelId + "_").replaceAll("-", "_").toLowerCase();


        String itsDir = c.rootDirOnServer + "/its";
        String itsTable = tableBaseName + "its";
        String itsDataDir = "/deep-impact/its/";
        itsDataDir = itsDir + File.separator + "images";

        String hriDir = c.rootDirOnServer + "/hri";
        String hriTable = tableBaseName + "hri";
        String hriDataDir = "/deep-impact/hri/";
        hriDataDir = hriDir + File.separator + "images";

        String mriDir = c.rootDirOnServer + "/mri";
        String mriTable = tableBaseName + "mri";
        String mriDataDir = "/deep-impact/mri/";
        mriDataDir = mriDir + File.separator + "images";

        String navcamDir = c.rootDirOnServer + "/navcam";
        String navcamTable = tableBaseName + "navcam";
        String navcamDataDir = "/stardust/navcam/";
        navcamDataDir = navcamDir + File.separator + "images";

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 4);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), itsDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), itsDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("ITS_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, SumFiles);
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.valueFor("ITS"));
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getRootPath(), hriDir);
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getDataPath(), hriDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(1).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(1).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(1).getType(), ImageType.valueOf("HRI_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(1).searchImageSources, SumFiles);
        assertEquals(imagingConfig.imagingInstruments.get(1).getInstrumentName(), Instrument.valueFor("HRI"));
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(1).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getRootPath(), mriDir);
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getDataPath(), mriDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(2).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(2).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(2).getType(), ImageType.valueOf("MRI_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(2).searchImageSources, SumFiles);
        assertEquals(imagingConfig.imagingInstruments.get(2).getInstrumentName(), Instrument.valueFor("MRI"));
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(2).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(3).getSearchQuery().getRootPath(), navcamDir);
        assertEquals(imagingConfig.imagingInstruments.get(3).getSearchQuery().getDataPath(), navcamDataDir);
        assertEquals(imagingConfig.imagingInstruments.get(3).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(3).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(3).getType(), ImageType.valueOf("NAVCAM_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(3).searchImageSources, SumFiles);
        assertEquals(imagingConfig.imagingInstruments.get(3).getInstrumentName(), Instrument.valueFor("NAVCAM"));
        assertEquals(imagingConfig.imagingInstruments.get(3).getOrientation(PointingSource.GASKELL).getRotation(), 180.0);
        assertEquals(imagingConfig.imagingInstruments.get(03).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);



        assertEquals(imagingConfig.imageSearchDefaultStartDate, ImageSearchDefaultStartDate);
        assertEquals(imagingConfig.imageSearchDefaultEndDate, ImageSearchDefaultEndDate);
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1.0e4);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 1.0e3);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[] { //
                new DBRunInfo(PointingSource.GASKELL, Instrument.ITS, bodyId, //
                        itsDir + "/imagelist-fullpath-sum.txt", itsTable), //
                new DBRunInfo(PointingSource.GASKELL, Instrument.HRI, bodyId, //
                        hriDir + "/imagelist-fullpath-sum.txt", hriTable), //
                new DBRunInfo(PointingSource.GASKELL, Instrument.MRI, bodyId, //
                        mriDir + "/imagelist-fullpath-sum.txt", mriTable), //
                new DBRunInfo(PointingSource.GASKELL, Instrument.NAVCAM, bodyId, //
                        navcamDir + "/imagelist-fullpath-sum.txt", navcamTable), //
        });
	}

	@Test
	void testTempel1Thomas()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.TEMPEL_1, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.TEMPEL_1);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Farnham and Thomas (2013)");
        assertEquals(c.rootDirOnServer, "/tempel1/farnham");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getResolutionLabels(), ImmutableList.of("32040 plates"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(32040));

        assertEquals(c.density, 470.0);
        assertEquals(c.rotationRate, 4.28434129815435E-5);
        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testWild2()
	{
		CometConfigs c = (CometConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.WILD_2, ShapeModelType.DUXBURY);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.WILD_2);
        assertEquals(c.type, BodyType.COMETS);
        assertEquals(c.population, ShapeModelPopulation.NA);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.DUXBURY);
        assertEquals(c.modelLabel, "Farnham et al. (2005)");
        assertEquals(c.rootDirOnServer, "/OTHER/WILD2");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "wild2_cart_full.w2.gz"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(17518));
        assertArrayEquals(c.presentInMissions,
                new Mission[] { Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

}
