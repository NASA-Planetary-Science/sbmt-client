package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

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
import edu.jhuapl.sbmt.spectrum.model.core.SpectraTypeFactory;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfig;
import edu.jhuapl.sbmt.stateHistory.config.StateHistoryConfigIO;

import crucible.crust.metadata.impl.InstanceGetter;

class MarsConfigsTest
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
		MarsConfigs.initialize(builtInConfigs, false);

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
	void testPhobosGaskell()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOBOS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOBOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2011)");
        assertEquals(c.density, 1.876);
        assertEquals(c.rotationRate, 0.00022803304110600688);
        assertEquals(c.rootDirOnServer, "/GASKELL/PHOBOS");
        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/PHOBOS/imaging");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/PHOBOS/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("MARS_MOON_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);


        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 12000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 300.0);


        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime());
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 0, 1, 2 });
        assertEquals(lidarConfig.lidarBrowseIsLidarInSphericalCoordinates, true);
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { -1, -1, -1 });
        assertEquals(lidarConfig.lidarBrowseIsTimeInET, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 5);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, -1);
        assertEquals(lidarConfig.lidarBrowseIsRangeExplicitInData, true);
        assertEquals(lidarConfig.lidarBrowseRangeIndex, 3);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 1);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.MOLA);

        // MOLA search is disabled for now. See LidarPanel class.
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/nearsdc/data/GASKELL/PHOBOS/IMAGING/pdsImageList.txt", ShapeModelBody.PHOBOS.toString().toLowerCase()),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

    @Test
	void testPhobosThomas()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOBOS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOBOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/PHOBOS");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "m1phobos.llr.gz"));
        c.setResolution(ImmutableList.of(32040));
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(lidarConfig.lidarSearchDataSourceMap, Maps.newHashMap()); // this must be instantiated, but can be empty

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

    @Test
	void testPhobosErnst2018()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOBOS, ShapeModelType.EXPERIMENTAL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOBOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.EXPERIMENTAL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/phobos/ernst2018");
        assertEquals(c.getShapeModelFileExtension(), ".obj");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/phobos/ernst2018/imaging");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/phobos/ernst2018/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("MARS_MOON_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);

        assertEquals(c.hasMapmaker, true);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime());

        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 12000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 300.0);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime());
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 0, 1, 2 });
        assertEquals(lidarConfig.lidarBrowseIsLidarInSphericalCoordinates, true);
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { -1, -1, -1 });
        assertEquals(lidarConfig.lidarBrowseIsTimeInET, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 5);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, -1);
        assertEquals(lidarConfig.lidarBrowseIsRangeExplicitInData, true);
        assertEquals(lidarConfig.lidarBrowseRangeIndex, 3);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 1);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.MOLA);

        // MOLA search is disabled for now. See LidarPanel class.
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch,true);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.PHOBOS.toString(), "/project/sbmt2/sbmt/data/bodies/phobos/ernst2018/imaging/imagelist-fullpath.txt", "phobos_ernst_2018"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

    @Test
	void testDeimosThomas()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.DEIMOS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.DEIMOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
		assertEquals(c.author, ShapeModelType.THOMAS);
		assertEquals(c.modelLabel, "Thomas (2000)");
		assertEquals(c.rootDirOnServer, "/THOMAS/DEIMOS");
		assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "DEIMOS.vtk.gz"));
		assertEquals(c.getResolutionNumberElements(), ImmutableList.of(49152));
		assertArrayEquals(c.presentInMissions, new Mission[]
		{ Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL,
				Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL });
		assertArrayEquals(c.defaultForMissions, new Mission[]{});
	}

    @Test
	void testDeimosErnst2018()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.DEIMOS, ShapeModelType.EXPERIMENTAL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.DEIMOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.EXPERIMENTAL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.rootDirOnServer, "/deimos/ernst2018");
        assertEquals(c.getShapeModelFileExtension(), ".obj");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/deimos/ernst2018/imaging");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/deimos/ernst2018/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("MARS_MOON_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime());

        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 30000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 800.0);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.IMAGING_DATA, ShapeModelBody.DEIMOS.toString(), "/project/sbmt2/sbmt/data/bodies/deimos/ernst2018/imaging/imagelist-fullpath.txt", "deimos_ernst_2018"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

    @Test
	void testPhobosErnst2018Megane()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOBOS, ShapeModelType.EXPERIMENTAL, "with MEGANE");
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOBOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.EXPERIMENTAL);
        assertEquals(c.modelLabel, "Ernst et al. (in progress)");
        assertEquals(c.version, "with MEGANE");
        assertEquals(c.rootDirOnServer, "/phobos/ernst2018-megane");
        assertEquals(c.getShapeModelFileExtension(), ".obj");


        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, c.rootDirOnServer + "/history/timeHistory.bth");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/phobos/ernst2018-megane/imaging");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/phobos/ernst2018-megane/imaging/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.valueOf("MARS_MOON_IMAGE"));
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.IMAGING_DATA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);


        assertEquals(c.hasMapmaker, true);
        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime());

        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 12000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 300.0);



        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);

        assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.spectralInstruments.size(), 1);



        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime());
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 0, 1, 2 });
        assertEquals(lidarConfig.lidarBrowseIsLidarInSphericalCoordinates, true);
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { -1, -1, -1 });
        assertEquals(lidarConfig.lidarBrowseIsTimeInET,  true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 5);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex,  -1);
        assertEquals(lidarConfig.lidarBrowseIsRangeExplicitInData, true);
        assertEquals(lidarConfig.lidarBrowseRangeIndex, 3);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 1);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.MOLA);

        // MOLA search is disabled for now. See LidarPanel class.
        assertEquals(lidarConfig.hasHypertreeBasedLidarSearch, true);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");


        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, c.rootDirOnServer + "/history/timeHistory.bth");
        assertEquals(stateHistoryConfig.stateHistoryStartDate, new GregorianCalendar(2026, 1, 1, 0, 0, 0).getTime());
        assertEquals(stateHistoryConfig.stateHistoryEndDate, new GregorianCalendar(2026, 9, 30, 0, 0, 0).getTime());
        stateHistoryConfig.spiceInfo = new SpiceInfo("MMX", "IAU_PHOBOS", "MMX_SPACECRAFT", "PHOBOS",
        				new String[] {"EARTH" , "SUN", "MARS"},
        				new String[] {"IAU_EARTH" , "IAU_SUN", "IAU_MARS"},
        				new String[] {"MMX_MEGANE"},
        				new String[] {});

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
        														  Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
        														  Mission.MEGANE_TEST});
        assertArrayEquals(c.defaultForMissions, new Mission[] {Mission.MEGANE_DEPLOY, Mission.MEGANE_DEV, Mission.MEGANE_STAGE,
				  													Mission.MEGANE_TEST});
	}

    @Test
	void testPhobosErnst2023()
	{
    	MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PHOBOS, ShapeModelType.provide("ernst-et-al-2023"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PHOBOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ernst-et-al-2023"));
        assertEquals(c.modelLabel, "Ernst et al. 2023");
        assertEquals(c.rootDirOnServer, "/phobos/ernst-et-al-2023");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertArrayEquals(c.presentInMissions, new Mission[] { //
                Mission.APL_INTERNAL, //
                Mission.TEST_APL_INTERNAL, //
                Mission.STAGE_APL_INTERNAL, //
                Mission.PUBLIC_RELEASE, //
                Mission.TEST_PUBLIC_RELEASE, //
                Mission.STAGE_PUBLIC_RELEASE //
        });
        assertEquals(c.getResolutionLabels(), ImmutableList.of( //
                        SmallBodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], //
                        SmallBodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], //
                        SmallBodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], //
                        SmallBodyViewConfig.DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3], //
                		"Extremely High (12582912 plates)"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of( //
        		SmallBodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], //
        		SmallBodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], //
        		SmallBodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], //
        		SmallBodyViewConfig.DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3], //
                12582912));
	}

	@Test
	void testDeimosErnst2023()
	{
		MarsConfigs c = (MarsConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.DEIMOS, ShapeModelType.provide("ernst-et-al-2023"));
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.DEIMOS);
        assertEquals(c.type, BodyType.PLANETS_AND_SATELLITES);
        assertEquals(c.population, ShapeModelPopulation.MARS);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.provide("ernst-et-al-2023"));
        assertEquals(c.modelLabel, "Ernst et al. 2023");
        assertEquals(c.rootDirOnServer, "/deimos/ernst-et-al-2023");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertArrayEquals(c.presentInMissions, new Mission[] { //
                Mission.APL_INTERNAL, //
                Mission.TEST_APL_INTERNAL, //
                Mission.STAGE_APL_INTERNAL, //
                Mission.PUBLIC_RELEASE, //
                Mission.TEST_PUBLIC_RELEASE, //
                Mission.STAGE_PUBLIC_RELEASE //
        });
	}

}
