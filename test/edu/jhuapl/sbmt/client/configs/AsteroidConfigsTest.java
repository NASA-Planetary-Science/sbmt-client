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

class AsteroidConfigsTest
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
				Mission.DART_STAGE,
				Mission.LUCY_DEPLOY,
				Mission.LUCY_STAGE,
				Mission.LUCY_TEST});
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

	@Test
	void testItokawaGaskell()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.ITOKAWA, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.ITOKAWA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell et al. (2008)");
        assertEquals(c.rootDirOnServer, "/GASKELL/ITOKAWA");
        assertArrayEquals(c.getShapeModelFileNames(), prepend("/ITOKAWA", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz"));

        StateHistoryConfig stateHistoryConfig = (StateHistoryConfig)c.getConfigForClass(StateHistoryConfig.class);
        assertEquals(stateHistoryConfig.hasStateHistory, true);
        assertEquals(stateHistoryConfig.timeHistoryFile, "/GASKELL/ITOKAWA/history/TimeHistory.bth");

//        ImageBinPadding binPadding = new ImageBinPadding();
//        binPadding.binExtents.put(1, new BinExtents(1024, 1024, 1024, 1024));
//        binPadding.binTranslations.put(1,  new BinTranslations(0,0));
//        binPadding.binSpacings.put(1,  new BinSpacings(1.0, 1.0, 1.0));
//
//        binPadding.binExtents.put(2, new BinExtents(512, 512, 1024, 1024));
//        binPadding.binTranslations.put(2,  new BinTranslations(0,0));
//        binPadding.binSpacings.put(2,  new BinSpacings(2.0, 2.0, 1.0));
//
//        binPadding.binExtents.put(4, new BinExtents(256, 256, 1024, 1024));
//        binPadding.binTranslations.put(4,  new BinTranslations(0, 0));
//        binPadding.binSpacings.put(4,  new BinSpacings(4.0, 4.0, 1.0));
//
//        binPadding.binExtents.put(8, new BinExtents(128, 128, 1024, 1024));
//        binPadding.binTranslations.put(8,  new BinTranslations(0, 0));
//        binPadding.binSpacings.put(8,  new BinSpacings(8.0, 8.0, 1.0));

        c.addFeatureConfig(BasemapImageConfig.class, new BasemapImageConfig(c));
        c.addFeatureConfig(ImagingInstrumentConfig.class, new ImagingInstrumentConfig(c));
        c.addFeatureConfig(SpectrumInstrumentConfig.class, new SpectrumInstrumentConfig(c));
        c.addFeatureConfig(LidarInstrumentConfig.class, new LidarInstrumentConfig(c));

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

//        DataQuerySourcesMetadata amicaMetadata =
//        		DataQuerySourcesMetadata.of("/GASKELL/ITOKAWA/AMICA", "", "AMICA", "AMICA", "/GASKELL/ITOKAWA/AMICA/gallery");
//
//        imagingConfig.imagingInstruments = Lists.newArrayList(
//                new ImagingInstrument( //
//                        SpectralImageMode.MONO, //
//                        new ImageDataQuery(amicaMetadata),
//                        ImageType.AMICA_IMAGE, //
//                        new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE, PointingSource.CORRECTED}, //
//                        Instrument.AMICA, //
//                        0.0,
//                        "None",
//                        binPadding
//                        ) //
//        );


        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/ITOKAWA/AMICA");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/ITOKAWA/AMICA/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/ITOKAWA/AMICA/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.AMICA_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE, PointingSource.CORRECTED});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.AMICA);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.NONE);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.CORRECTED).getFlip(), ImageFlip.NONE);

        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binExtents.get(1), new BinExtents(1024, 1024, 1024, 1024));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binTranslations.get(1), new BinTranslations(0, 0));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binSpacings.get(1), new BinSpacings(1.0, 1.0, 1.0));

        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binExtents.get(2), new BinExtents(512, 512, 1024, 1024));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binTranslations.get(2), new BinTranslations(0, 0));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binSpacings.get(2), new BinSpacings(2.0, 2.0, 1.0));

        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binExtents.get(4), new BinExtents(256, 256, 1024, 1024));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binTranslations.get(4), new BinTranslations(0, 0));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binSpacings.get(4), new BinSpacings(4.0, 4.0, 1.0));

        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binExtents.get(8), new BinExtents(128, 128, 1024, 1024));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binTranslations.get(8), new BinTranslations(0, 0));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binSpacings.get(8), new BinSpacings(8.0, 8.0, 1.0));



        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 26.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 3.0);


        LidarInstrumentConfig lidarConfig = (LidarInstrumentConfig)c.getConfigForClass(LidarInstrumentConfig.class);

        assertEquals(lidarConfig.hasLidarData, true);
        assertEquals(lidarConfig.lidarSearchDefaultStartDate, new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDefaultEndDate, new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime());
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Optimized"), "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        assertEquals(lidarConfig.lidarSearchDataSourceMap.get("Unfiltered"), "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        assertArrayEquals(lidarConfig.lidarBrowseXYZIndices, new int[] { 6, 7, 8 });
        assertArrayEquals(lidarConfig.lidarBrowseSpacecraftIndices, new int[] { 3, 4, 5 });
        assertEquals(lidarConfig.lidarBrowseIsSpacecraftInSphericalCoordinates, false);
        assertEquals(lidarConfig.lidarBrowseTimeIndex, 1);
        assertEquals(lidarConfig.lidarBrowseNoiseIndex, -1);
        assertEquals(lidarConfig.lidarBrowseFileListResourcePath, "/edu/jhuapl/sbmt/data/HayLidarFiles.txt");
        assertEquals(lidarConfig.lidarBrowseNumberHeaderLines, 0);
        assertEquals(lidarConfig.lidarBrowseIsInMeters, false);

        assertEquals(lidarConfig.lidarOffsetScale, 0.00044228259621279913);
        assertEquals(lidarConfig.lidarInstrumentName, Instrument.LIDAR);

        SpectrumInstrumentConfig spectrumConfig = (SpectrumInstrumentConfig)c.getConfigForClass(SpectrumInstrumentConfig.class);
        assertEquals(spectrumConfig.spectralInstruments, new ArrayList<BasicSpectrumInstrument>());

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.AMICA, ShapeModelBody.ITOKAWA.toString(), "/project/nearsdc/data/GASKELL/ITOKAWA/AMICA/imagelist.txt", "amica"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE,
        															Mission.STAGE_PUBLIC_RELEASE,
        															Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL,
        															Mission.STAGE_APL_INTERNAL,
        															Mission.HAYABUSA2_DEPLOY, Mission.HAYABUSA2_DEV,
        															Mission.OSIRIS_REX, Mission.OSIRIS_REX_TEST,
        															Mission.OSIRIS_REX_DEPLOY,
        															Mission.OSIRIS_REX_MIRROR_DEPLOY, Mission.NH_DEPLOY});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testItokawaOstro()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.ITOKAWA, ShapeModelType.OSTRO);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.ITOKAWA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.RADAR_BASED);
        assertEquals(c.author, ShapeModelType.OSTRO);
        assertEquals(c.modelLabel, "Ostro et al. (2004)");
        assertEquals(c.rootDirOnServer, "/HUDSON/ITOKAWA");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "25143itokawa.obj.gz"));
        assertEquals(c.getResolutionNumberElements().get(0), 12192);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testToutatisHudson()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.TOUTATIS, ShapeModelType.HUDSON);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.TOUTATIS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.NEO);
        assertEquals(c.dataUsed, ShapeModelDataUsed.RADAR_BASED);
        assertEquals(c.author, ShapeModelType.HUDSON);
        assertEquals(c.modelLabel, "Hudson et al. (2004)");
        assertEquals(c.rootDirOnServer, "/toutatis/hudson");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getResolutionLabels(), ImmutableList.of("Low (12796 plates)", "High (39996 plates)"));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of(12796, 39996));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});

	}

	@Test
	void testCeresGaskell()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.CERES, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.CERES);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "SPC");
        assertEquals(c.rootDirOnServer, "/GASKELL/CERES");
        assertEquals(c.hasMapmaker, true);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/CERES/FC");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/CERES/FC/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/CERES/FC/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.FCCERES_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.FC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2015, GregorianCalendar.APRIL, 1, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2016, GregorianCalendar.JULY, 1, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "FC1", "FC2" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);

        assertArrayEquals(c.databaseRunInfos, new DBRunInfo[]
        {
        	new DBRunInfo(PointingSource.GASKELL, Instrument.FC, ShapeModelBody.CERES.toString(), "/project/nearsdc/data/GASKELL/CERES/FC/uniqFcFiles.txt", "ceres"),
        });

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testVestaGaskell()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.VESTA, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.VESTA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "Gaskell (2013)");
        assertEquals(c.rootDirOnServer, "/GASKELL/VESTA");
        assertEquals(c.hasMapmaker, true);

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/VESTA/FC");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/VESTA/FC/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), "/GASKELL/VESTA/FC/gallery");
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.FC_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL, PointingSource.SPICE});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.FC);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.X);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.SPICE).getFlip(), ImageFlip.X);

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        });
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] { "FC1", "FC2" });
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);

        assertArrayEquals(c.databaseRunInfos,  new DBRunInfo[] {new DBRunInfo(PointingSource.GASKELL, Instrument.FC, ShapeModelBody.VESTA.toString(), "/project/nearsdc/data/GASKELL/VESTA/FC/uniqFcFiles.txt", "fc")});

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testVestaThomas()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.VESTA, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.VESTA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/VESTA_OLD");
        assertArrayEquals(c.getShapeModelFileNames(), new String[] { "/VESTA_OLD/VESTA.vtk.gz" });
        assertEquals(c.getResolutionNumberElements().get(0), 49152);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testLutetiaGaskell()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.LUTETIA, ShapeModelType.GASKELL);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.LUTETIA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.GASKELL);
        assertEquals(c.modelLabel, "SPC");
        assertEquals(c.rootDirOnServer, "/GASKELL/LUTETIA");

        ImagingInstrumentConfig imagingConfig = (ImagingInstrumentConfig)c.getConfigForClass(ImagingInstrumentConfig.class);

        assertEquals(imagingConfig.imagingInstruments.size(), 1);
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getRootPath(), "/GASKELL/LUTETIA/IMAGING");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getDataPath(), "/GASKELL/LUTETIA/IMAGING/images");
        assertEquals(imagingConfig.imagingInstruments.get(0).getSearchQuery().getGalleryPath(), null);
        assertEquals(imagingConfig.imagingInstruments.get(0).spectralMode, SpectralImageMode.MONO);
        assertEquals(imagingConfig.imagingInstruments.get(0).getType(), ImageType.OSIRIS_IMAGE);
        assertArrayEquals(imagingConfig.imagingInstruments.get(0).searchImageSources, new PointingSource[]{PointingSource.GASKELL});
        assertEquals(imagingConfig.imagingInstruments.get(0).getInstrumentName(), Instrument.OSIRIS);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getRotation(), 0.0);
        assertEquals(imagingConfig.imagingInstruments.get(0).getOrientation(PointingSource.GASKELL).getFlip(), ImageFlip.Y);

        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binExtents.get(1), new BinExtents(2048, 2048, 2048, 2048));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binTranslations.get(1), new BinTranslations(559, 575));
        assertEquals(imagingConfig.imagingInstruments.get(0).getBinPadding().binSpacings.get(1), new BinSpacings(1.0, 1.0, 1.0));

        assertEquals(imagingConfig.imageSearchDefaultStartDate, new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime());
        assertEquals(imagingConfig.imageSearchDefaultEndDate, new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime());
        assertArrayEquals(imagingConfig.imageSearchFilterNames, new String[] {});
        assertArrayEquals(imagingConfig.imageSearchUserDefinedCheckBoxesNames, new String[] {});
        assertEquals(imagingConfig.imageSearchDefaultMaxSpacecraftDistance, 40000.0);
        assertEquals(imagingConfig.imageSearchDefaultMaxResolution, 4000.0);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testLutetiaJordan()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.LUTETIA, ShapeModelType.JORDA);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.LUTETIA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.JORDA);
        assertEquals(c.modelLabel, "Farnham et al. (2013)");
        assertEquals(c.rootDirOnServer, "/JORDA/LUTETIA");
        assertEquals(c.getResolutionLabels(), ImmutableList.of( //
                "2962 plates ", "5824 plates ", "11954 plates ", "24526 plates ", //
                "47784 plates ", "98280 plates ", "189724 plates ", "244128 plates ", //
                "382620 plates ", "784510 plates ", "1586194 plates ", "3145728 plates" //
            ));
        assertEquals(c.getResolutionNumberElements(), ImmutableList.of( //
                2962, 5824, 11954, 24526, 47784, 98280, 189724, //
                244128, 382620, 784510, 1586194, 3145728 //
            ));
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer,
                "shape_res0.vtk.gz", //
                "shape_res1.vtk.gz", //
                "shape_res2.vtk.gz", //
                "shape_res3.vtk.gz", //
                "shape_res4.vtk.gz", //
                "shape_res5.vtk.gz", //
                "shape_res6.vtk.gz", //
                "shape_res7.vtk.gz", //
                "shape_res8.vtk.gz", //
                "shape_res9.vtk.gz", //
                "shape_res10.vtk.gz", //
                "shape_res11.vtk.gz" //
            ));
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testIdaThomas()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.IDA, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.IDA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/IDA");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "243ida.llr.gz"));
        assertEquals(c.getResolutionNumberElements().get(0), 32040);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testIdaStooke()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.IDA, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.IDA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/ida/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getResolutionNumberElements().get(0), 5040);
        assertEquals(c.density, 2600.);
        assertEquals(c.rotationRate, 0.0003766655);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions,new Mission[] {});
	}

	@Test
	void testMathildeThomas()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.MATHILDE, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.MATHILDE);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/MATHILDE");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "253mathilde.llr.gz"));
        assertEquals(c.getResolutionNumberElements().get(0), 14160);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testMathildeStooke()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.MATHILDE, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.MATHILDE);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/mathilde/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.density, 1300.);
        assertEquals(c.rotationRate, 0.0000041780);
        assertEquals(c.getResolutionNumberElements().get(0), 5040);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testGaspraThomas()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.GASPRA, ShapeModelType.THOMAS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.GASPRA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.THOMAS);
        assertEquals(c.modelLabel, "Thomas et al. (2000)");
        assertEquals(c.rootDirOnServer, "/THOMAS/GASPRA");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "951gaspra.llr.gz"));
        assertEquals(c.getResolutionNumberElements().get(0), 32040);
        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testGaspraStooke()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.GASPRA, ShapeModelType.STOOKE);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.GASPRA);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.STOOKE);
        assertEquals(c.modelLabel, "Stooke (2016)");
        assertEquals(c.rootDirOnServer, "/gaspra/stooke2016");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.density, 2700.);
        assertEquals(c.rotationRate, 0.0002478);
        assertEquals(c.getResolutionNumberElements().get(0), 5040);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testSteinsJorda()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.STEINS, ShapeModelType.JORDA);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.STEINS);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.JORDA);
        assertEquals(c.modelLabel, "Farnham and Jorda (2013)");
        assertEquals(c.rootDirOnServer, "/JORDA/STEINS");
        assertArrayEquals(c.getShapeModelFileNames(), prepend(c.rootDirOnServer, "steins_cart.plt.gz"));
        assertEquals(c.getResolutionNumberElements().get(0), 20480);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

	@Test
	void testPsycheHanus()
	{
		AsteroidConfigs c = (AsteroidConfigs)SmallBodyViewConfig.getConfig(ShapeModelBody.PSYCHE, ShapeModelType.HANUS);
		FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);
        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)c);

        assertEquals(c.body, ShapeModelBody.PSYCHE);
        assertEquals(c.type, BodyType.ASTEROID);
        assertEquals(c.population, ShapeModelPopulation.MAIN_BELT);
        assertEquals(c.dataUsed, ShapeModelDataUsed.IMAGE_BASED);
        assertEquals(c.author, ShapeModelType.HANUS);
        assertEquals(c.modelLabel, "Hanus et al. (2013)");
        assertEquals(c.rootDirOnServer, "/psyche/hanus");
        assertEquals(c.getShapeModelFileExtension(), ".obj");
        assertEquals(c.getResolutionNumberElements().get(0), 2038);

        assertArrayEquals(c.presentInMissions, new Mission[] {Mission.PUBLIC_RELEASE, Mission.TEST_PUBLIC_RELEASE, Mission.STAGE_PUBLIC_RELEASE, Mission.STAGE_APL_INTERNAL, Mission.APL_INTERNAL, Mission.TEST_APL_INTERNAL});
        assertArrayEquals(c.defaultForMissions, new Mission[] {});
	}

}
