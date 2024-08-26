package edu.jhuapl.sbmt.client.configs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.saavtk.config.ConfigArrayList;
import edu.jhuapl.saavtk.config.IBodyViewConfig;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.config.BasicConfigInfo;
import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.SmallBodyViewConfigMetadataIO;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
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
import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;
import edu.jhuapl.ses.jsqrl.impl.gson.Serializers;

public class SmallBodyViewConfigMetadataIORunner
{
	public static void main(String[] args) throws IOException
	{
		if (args.length < 1 || args.length > 2)
		{
			System.err.println(
					"Usage: SmallBodyViewConfigMetadataIO.sh <output-directory-full-path> [ -pub ]\n\n\t-pub means include only published data in the output model metadata; if omitted, include ALL data\n\n\tThe output directory will be created if it does not exist");
			System.exit(1);
		}

		boolean publishedDataOnly = args.length > 1
				&& (args[1].equalsIgnoreCase("-pub") || args[1].equalsIgnoreCase("--pub"));

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

		ConfigArrayList<IBodyViewConfig> configArray = SmallBodyViewConfig.getBuiltInConfigs();
		AsteroidConfigs.initialize(configArray);
		BennuConfigs.initialize(configArray, publishedDataOnly);
		CometConfigs.initialize(configArray);
		DartConfigs.instance().initialize(configArray);
		EarthConfigs.initialize(configArray);
		MarsConfigs.initialize(configArray, publishedDataOnly);
		NewHorizonsConfigs.initialize(configArray);
		RyuguConfigs.initialize(configArray);
		SaturnConfigs.initialize(configArray);
		LucyConfigs.initialize(configArray);

//		SmallBodyViewConfig.initializeWithStaticConfigs(publishedDataOnly);
		for (IBodyViewConfig each : SmallBodyViewConfig.getBuiltInConfigs())
		{
			each.enable(true);
		}

		String rootDir = args[0].replaceFirst("/*$", "/") + BasicConfigInfo.getConfigPathPrefix(publishedDataOnly)
				+ "/";

		// Create the directory just in case. Then make sure it exists before
		// proceeding.
		File rootDirFile = new File(rootDir);
		rootDirFile.mkdirs();
		if (!rootDirFile.isDirectory())
		{
			throw new IOException("Unable to create root config directory " + rootDir);
		}

		List<IBodyViewConfig> builtInConfigs = SmallBodyViewConfig.getBuiltInConfigs();
		System.out.println("SmallBodyViewConfigMetadataIO: main: ---------------------------------");
		for (IBodyViewConfig config : builtInConfigs)
		{
			FeatureConfigIOFactory.getIOForClassType(LidarInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)config);
	        FeatureConfigIOFactory.getIOForClassType(SpectrumInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)config);
	        FeatureConfigIOFactory.getIOForClassType(ImagingInstrumentConfig.class.getSimpleName()).setViewConfig((ViewConfig)config);
	        FeatureConfigIOFactory.getIOForClassType(BasemapImageConfig.class.getSimpleName()).setViewConfig((ViewConfig)config);
	        FeatureConfigIOFactory.getIOForClassType(StateHistoryConfig.class.getSimpleName()).setViewConfig((ViewConfig)config);

			try
			{
				SmallBodyViewConfigMetadataIO io = new SmallBodyViewConfigMetadataIO(config);
				String version = config.getVersion() == null ? "" : config.getVersion();
				File file = null;
				if (!config.hasSystemBodies())
					file = new File(rootDir + ((SmallBodyViewConfig) config).rootDirOnServer + "/" + config.getAuthor()
							+ "_" + config.getBody().toString().replaceAll(" ", "_") + version.replaceAll(" ", "_")
							+ "_v" + configInfoVersion + ".json");
				else
				{
					String bodyName = config.getBody().toString();
					bodyName = bodyName.replaceAll(" ", "_");

					String systemRoot = "/" + config.getBody().name().replaceAll("[\\s-_]+", "-").toLowerCase() + "/"
							+ config.getAuthor().name().replaceAll("[\\s-_]+", "-").toLowerCase();
					systemRoot = systemRoot.replaceAll("\\(", "");
					systemRoot = systemRoot.replaceAll("\\)", "");
					systemRoot = systemRoot.replaceAll("-\\w*-center", "");

					String fileNameString = rootDir + systemRoot + "/" + config
							.getAuthor() /*
											 * + "_" + config.getBody().toString().replaceAll(" ", "_") + "_System_" +
											 * bodyName.toLowerCase() + "center" + version.replaceAll(" ", "_")
											 */ + "_v" + configInfoVersion + ".json";

					fileNameString = fileNameString.replaceAll("\\(", "");
					fileNameString = fileNameString.replaceAll("\\)", "");
					file = new File(fileNameString);
				}
				BasicConfigInfo configInfo = new BasicConfigInfo((BodyViewConfig) config, publishedDataOnly);
				allBodiesMetadata.put(Key.of(config.getUniqueName()), configInfo.store());

				if (!file.exists())
					file.getParentFile().mkdirs();
				Metadata outgoingMetadata = io.store();
				io.write(config.getUniqueName(), file, outgoingMetadata);

				// read in data from file to do sanity check
				SmallBodyViewConfig cfg = new SmallBodyViewConfig();
				SmallBodyViewConfigMetadataIO io2 = new SmallBodyViewConfigMetadataIO(cfg);
				FixedMetadata metadata = Serializers.deserialize(file, config.getUniqueName());
				io2.setMetadataID(config.getUniqueName());
				io2.retrieve(metadata);
				checkEquality(cfg, config);

			}
			catch (Exception e)
			{
				System.err.println("WARNING: EXCEPTION! SKIPPING CONFIG " + config.getUniqueName());
				e.printStackTrace();
			}
		}

		Serializers.serialize("AllBodies", allBodiesMetadata,
				new File(rootDir + "allBodies_v" + configInfoVersion + ".json"));

	}

    /**
     * Perform the equality check in its own method so that one can more easily
     * debug. Set a breakpoint at the println, then drop-to-frame and re-run the
     * call to equals.
     */
    private static void checkEquality(IBodyViewConfig cfg, IBodyViewConfig config)
    {
        if (!cfg.equals(config))
            System.err.println("SmallBodyViewConfigMetadataIO: main: cfg equals config is " + (cfg.equals(config) + " for " + config.getUniqueName()));
    }
}
