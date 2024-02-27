package edu.jhuapl.sbmt.client.configs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.GregorianCalendar;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

class AsteroidConfigsTest
{

	@BeforeAll
	static void setUpBeforeClass() throws Exception
	{
		String configInfoVersion = BasicConfigInfo.getConfigInfoVersion();

		SettableMetadata allBodiesMetadata = SettableMetadata.of(Version.of(configInfoVersion));
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
		AsteroidConfigs.initialize(builtInConfigs);

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
	void testErosGaskell()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EROS, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.getBody(), ShapeModelBody.EROS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.getAuthor(), ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2008)");
        assertEquals(c.getRootDirOnServer(), "/GASKELL/EROS");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/EROS", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz"));

        assertEquals(c.hasMapmaker, true);
        assertEquals(c.hasRemoteMapmaker, false);
        assertEquals(c.density, 2.67);
        assertEquals(c.rotationRate, 0.000331165761670640);
        assertEquals(c.bodyReferencePotential, -53.765039959572114);
        assertEquals(c.bodyLowestResModelName, "EROS/shape/shape0.obj");
        assertEquals(c.hasLineamentData, true);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/EROS/MSI");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/EROS/MSI/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/EROS/MSI/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.MSI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL_UPDATED, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.MSI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getFlip(), ImageFlip.NONE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getFillValues(), new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {537, 412});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {16, 16, 16, 16});

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "iofdbl", "cifdbl" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 50.0);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataName(), "NIS Calibrated Spectrum");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataRootLocation(), "/GASKELL/EROS/shared/nis");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataPath(), "spectra");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataListFilename(), "spectrumlist.txt");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getSource(), PointingSource.CORRECTED_SPICE);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getxAxisUnits(), "Wavelength (nm)");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getyAxisUnits(), "Reflectance");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataDescription(), "NIS");

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.size(), 0);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.size(), 1);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/NLR/cubes");
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 14, 15, 16 });
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { 8, 9, 10 });
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 4);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 7);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/edu/jhuapl/sbmt/data/NlrFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 2);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.NLR);

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/EROS/history/TimeHistory.bth");

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
				Mission.STAGE_PUBLIC_RELEASE,
				Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
				Mission.STAGE_APL_INTERNAL,
				Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV,
				Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST,
				Mission.OSIRIS_REX_DEPLOY, Mission.OSIRIS_REX_MIRROR_DEPLOY,
				Mission.NH_DEPLOY,
				Mission.DART_DEV,
				Mission.DART_DEPLOY,
				Mission.DART_TEST,
				Mission.DART_STAGE});
        assertArrayEquals(c.defaultForMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE,
				Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL,
				Mission.TEST_APL_INTERNAL});
	}

	@Test
	void testErosThomas()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EROS, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.getBody(), ShapeModelBody.EROS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.getAuthor(), ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2001)");
        assertEquals(c.getRootDirOnServer(), "/THOMAS/EROS");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "eros001708.obj.gz", "eros007790.obj.gz", "eros010152.obj.gz", "eros022540.obj.gz", "eros089398.obj.gz", "eros200700.obj.gz"));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/EROS/MSI");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/EROS/MSI/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/EROS/MSI/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.MSI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL_UPDATED, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.MSI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getFlip(), ImageFlip.NONE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getFillValues(), new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {537, 412});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {16, 16, 16, 16});

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "iofdbl", "cifdbl" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 50.0);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataName(), "NIS Calibrated Spectrum");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataRootLocation(), "/GASKELL/EROS/shared/nis");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataPath(), "spectra");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataListFilename(), "spectrumlist.txt");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getSource(), PointingSource.CORRECTED_SPICE);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getxAxisUnits(), "Wavelength (nm)");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getyAxisUnits(), "Reflectance");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataDescription(), "NIS");

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.size(), 0);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.size(), 1);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/NLR/cubes");
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 14, 15, 16 });
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { 8, 9, 10 });
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 4);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 7);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/edu/jhuapl/sbmt/data/NlrFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 2);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.NLR);

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/EROS/history/TimeHistory.bth");

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
				Mission.STAGE_PUBLIC_RELEASE,
        		Mission.STAGE_APL_INTERNAL,
        		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testErosNLR()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EROS, ShapeModelType.EROSNLR);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.getBody(), ShapeModelBody.EROS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.getAuthor(), ShapeModelType.EROSNLR);
        assertEquals(c.modelLabel, "Neumann et al. (2001)");
        assertEquals(c.getRootDirOnServer(), "/OTHER/EROSNLR");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "nlrshape.llr2.gz"));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/EROS/MSI");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/EROS/MSI/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/EROS/MSI/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.MSI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL_UPDATED, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.MSI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getFlip(), ImageFlip.NONE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getFillValues(), new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {537, 412});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {16, 16, 16, 16});

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "iofdbl", "cifdbl" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 50.0);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataName(), "NIS Calibrated Spectrum");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataRootLocation(), "/GASKELL/EROS/shared/nis");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataPath(), "spectra");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataListFilename(), "spectrumlist.txt");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getSource(), PointingSource.CORRECTED_SPICE);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getxAxisUnits(), "Wavelength (nm)");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getyAxisUnits(), "Reflectance");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataDescription(), "NIS");

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.size(), 0);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.size(), 1);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/NLR/cubes");
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 14, 15, 16 });
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { 8, 9, 10 });
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 4);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 7);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/edu/jhuapl/sbmt/data/NlrFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 2);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.NLR);

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/EROS/history/TimeHistory.bth");

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
				Mission.STAGE_PUBLIC_RELEASE,
        		Mission.STAGE_APL_INTERNAL,
        		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testErosNAV()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.EROS, ShapeModelType.EROSNAV);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.getBody(), ShapeModelBody.EROS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.LIDAR_BASED);
        assertEquals(c.getAuthor(), ShapeModelType.EROSNAV);
        assertEquals(c.modelLabel, "NAV team (2001)");
        assertEquals(c.getRootDirOnServer(), "/OTHER/EROSNAV");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "navplate.obj.gz"));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);
        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/EROS/MSI");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/EROS/MSI/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/EROS/MSI/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.MSI_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL_UPDATED, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.MSI);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL_UPDATED).getFlip(), ImageFlip.NONE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getFillValues(), new double[] {});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getLinearInterpolationDims(), new int[] {537, 412});
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).getMaskValues(), new int[] {16, 16, 16, 16});

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "iofdbl", "cifdbl" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 1000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 50.0);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.hasSpectralData, true);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataName(), "NIS Calibrated Spectrum");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataRootLocation(), "/GASKELL/EROS/shared/nis");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataPath(), "spectra");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataListFilename(), "spectrumlist.txt");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getSource(), PointingSource.CORRECTED_SPICE);
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getxAxisUnits(), "Wavelength (nm)");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getyAxisUnits(), "Reflectance");
        assertEquals(spectrumConfig.hierarchicalSpectraSearchSpecification.getInstrumentMetadata("NIS").getSpecs().get(0).getDataDescription(), "NIS");

        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);
        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarBrowseDataSourceMap.size(), 0);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.size(), 1);
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Default"), "/NLR/cubes");
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 14, 15, 16 });
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { 8, 9, 10 });
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, true);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 4);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, 7);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/edu/jhuapl/sbmt/data/NlrFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 2);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, true);
        assertEquals(lidarConfig.lidarOffsetScale, 0.025);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.NLR);


        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/EROS/history/TimeHistory.bth");

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
				Mission.STAGE_PUBLIC_RELEASE,
        		Mission.STAGE_APL_INTERNAL,
        		Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

}
