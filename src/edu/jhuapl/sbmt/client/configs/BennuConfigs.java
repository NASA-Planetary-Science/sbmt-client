package edu.jhuapl.sbmt.client.configs;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtMultiMissionTool;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.SessionConfiguration;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.model.bennu.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.image.BasicImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.image.SpectralImageMode;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumInstrumentMetadata;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.spectrum.model.core.search.SpectrumSearchSpec;
import edu.jhuapl.sbmt.spectrum.model.io.SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.tools.DBRunInfo;

public class BennuConfigs extends SmallBodyViewConfig
{
    private static final SbmtMultiMissionTool.Mission[] OREXClients = new SbmtMultiMissionTool.Mission[] { //
            SbmtMultiMissionTool.Mission.OSIRIS_REX, SbmtMultiMissionTool.Mission.OSIRIS_REX_TEST, SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY, //
            SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final SbmtMultiMissionTool.Mission[] ClientsWithOREXModels = new SbmtMultiMissionTool.Mission[] { //
            SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL, SbmtMultiMissionTool.Mission.STAGE_APL_INTERNAL, //
            SbmtMultiMissionTool.Mission.OSIRIS_REX, SbmtMultiMissionTool.Mission.OSIRIS_REX_TEST, SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY, //
            SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final SbmtMultiMissionTool.Mission[] AllBennuClients = new SbmtMultiMissionTool.Mission[] { //
            SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.STAGE_PUBLIC_RELEASE, //
            SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL, SbmtMultiMissionTool.Mission.STAGE_APL_INTERNAL, //
            SbmtMultiMissionTool.Mission.OSIRIS_REX, SbmtMultiMissionTool.Mission.OSIRIS_REX_TEST, SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY, //
            SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY
    };

    private static final SbmtMultiMissionTool.Mission[] InternalOnly = new SbmtMultiMissionTool.Mission[] {
    		SbmtMultiMissionTool.Mission.APL_INTERNAL, SbmtMultiMissionTool.Mission.TEST_APL_INTERNAL, SbmtMultiMissionTool.Mission.STAGE_APL_INTERNAL
    };

    private static final SbmtMultiMissionTool.Mission[] PublicOnly = new SbmtMultiMissionTool.Mission[] {
    		SbmtMultiMissionTool.Mission.PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.TEST_PUBLIC_RELEASE, SbmtMultiMissionTool.Mission.STAGE_PUBLIC_RELEASE
    };


	List<SpectrumInstrumentMetadata<SpectrumSearchSpec>> instrumentSearchSpecs = new ArrayList<SpectrumInstrumentMetadata<SpectrumSearchSpec>>();

	public BennuConfigs()
	{
		super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));

		this.defaultForMissions = DefaultForNoMissions;

		SpectrumSearchSpec otesL2 = new SpectrumSearchSpec("OTES L2 Calibrated Radiance", "/bennu/shared/otes/l2", "spectra", "spectrumlist.txt", ImageSource.valueFor("Corrected SPICE Derived"), "Wave Number (1/cm)", "Radiance", "OTES L2 Calibrated Radiance");
		SpectrumSearchSpec otesL3 = new SpectrumSearchSpec("OTES L3 Spot Emissivity", "/bennu/shared/otes/l3", "spectra", "spectrumlist.txt", ImageSource.valueFor("Corrected SPICE Derived"), "Wave Number (1/cm)", "Emissivity", "OTES L3 Spot Emissivity");
		List<SpectrumSearchSpec> otesSpecs = new ArrayList<SpectrumSearchSpec>();
		otesSpecs.add(otesL2);
		otesSpecs.add(otesL3);

		SpectrumSearchSpec ovirsSA16 = new SpectrumSearchSpec("OVIRS SA-16 Photometrically Corrected SPOT Reflectance Factor (REFF)", "/bennu/shared/ovirs/l3/SA16l3escireff", "spectra", "spectrumlist.txt", ImageSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "REFF", "OVIRS L3 SA-16 Photometrically Corrected SPOT Reflectance Factor (REFF)");
		SpectrumSearchSpec ovirsSA27 = new SpectrumSearchSpec("OVIRS SA-27 SPOT I/F", "/bennu/shared/ovirs/l3/SA27l3csci", "spectra", "spectrumlist.txt", ImageSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "I/F", "OVIRS L3 SA-27 SPOT I/F");
		SpectrumSearchSpec ovirsSA29 = new SpectrumSearchSpec("OVIRS SA-29 Photometrically corrected SPOT I/F, aka RADF", "/bennu/shared/ovirs/l3/SA29l3esciradf", "spectra", "spectrumlist.txt", ImageSource.valueFor("Corrected SPICE Derived"), "Wavelength (microns)", "RADF", "OVIRS L3 SA-29 Photometrically corrected SPOT I/F, aka RADF");
		List<SpectrumSearchSpec> ovirsSpecs = new ArrayList<SpectrumSearchSpec>();
		ovirsSpecs.add(ovirsSA16);
		ovirsSpecs.add(ovirsSA27);
		ovirsSpecs.add(ovirsSA29);

		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("OTES", otesSpecs));
		instrumentSearchSpecs.add(new SpectrumInstrumentMetadata<SpectrumSearchSpec>("OVIRS", ovirsSpecs));

        presentInMissions = ClientsWithOREXModels;
	}


	public static void initialize(List<ViewConfig> configArray, boolean publicOnly)
    {
        BennuConfigs c = new BennuConfigs();

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, test spherical version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("osirisrex", ShapeModelDataUsed.IMAGE_BASED).build();
            ImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralImageMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            ImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralImageMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            ImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralImageMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.SAMCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                samCam = BasicImagingInstrument.of(builder.build());
            }

            c = new BennuConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.BLENDER;
            c.rootDirOnServer = "/earth/osirisrex";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
            c.hasImageMap = true;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    mapCam,
                    polyCam,
                    samCam,
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
            c.spectralInstruments.add(new OTES());
            c.spectralInstruments.add(new OVIRS());

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            // 2017-12-21: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, OREX WGS84 version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();

//            // Set up shape model -- one will suffice. Note the "orex" here must be kept
//            // exactly as it is; that is what the directory is named in the data area.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("orex", ShapeModelDataUsed.WGS84).build();

            c = new BennuConfigs();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.OREX;
            c.rootDirOnServer = "/earth/orex";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
            c.hasImageMap = true;

            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            SBMTFileLocator polyCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fits", ".INFO", null, ".jpeg");
            QueryBase polyCamQueryBase = new FixedListQuery(polyCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), polyCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            SBMTFileLocator mapCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fits", ".INFO", null, ".jpeg");
            QueryBase mapCamQueryBase = new FixedListQuery(mapCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), mapCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            SBMTFileLocator samCamFileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
            QueryBase samCamQueryBase = new FixedListQuery(samCamFileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), samCamFileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));

            c.imagingInstruments = new ImagingInstrument[] {
            		new ImagingInstrument(
                            SpectralImageMode.MONO,
                            polyCamQueryBase,
                            ImageType.POLYCAM_EARTH_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            mapCamQueryBase,
                            ImageType.MAPCAM_EARTH_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            samCamQueryBase,
                            ImageType.SAMCAM_EARTH_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.SAMCAM
                            )
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();

            c.setSpectrumParameters();
            //override
            c.hasHypertreeBasedSpectraSearch = true;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", "/earth/osirisrex/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", "/earth/osirisrex/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", "/earth/osirisrex/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", "/earth/osirisrex/ovirs/l3/reff/hypertree/dataSource.spectra");

            c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};

//            configArray.add(c);
        }


        c = new BennuConfigs();
        c.body = ShapeModelBody.RQ36;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.NOLAN;
        c.modelLabel = "Nolan et al. (2013)";
        c.rootDirOnServer = "/bennu/nolan";
        c.shapeModelFileExtension = ".obj";

        c.hasStateHistory = true;
        c.timeHistoryFile = "/bennu/nolan/history/timeHistory.bth";

        c.setResolution(ImmutableList.of(2692));
        c.density = 1.260;
        c.useMinimumReferencePotential = true;
        c.rotationRate = 0.00040613;

        c.presentInMissions = AllBennuClients;

        configArray.add(c);

        // PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.useMinimumReferencePotential = false;

            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/RQ36_V3";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
            c.density = 1.0;
            c.rotationRate = 0.000407026411379;
            c.presentInMissions = AllBennuClients;

            c.hasMapmaker = true;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/POLYCAM", "RQ36_POLY"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/POLYCAM", true),
                            ImageType.POLYCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/MAPCAM", "RQ36_MAP"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/MAPCAM"),
                            ImageType.MAPCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

            c.hasLidarData = true;
            c.lidarInstrumentName = Instrument.OLA;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt";

            // default ideal data

            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
            // noisy data
            c.lidarSearchDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            c.databaseRunInfos = new DBRunInfo[]
            {
        		new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/MAPCAM/imagelist-fullpath.txt", "RQ36_MAP"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/project/nearsdc/data/GASKELL/RQ36_V3/POLYCAM/imagelist-fullpath.txt", "RQ36_POLY"),
            };

            configArray.add(c);



            c.hasStateHistory = true;
            c.timeHistoryFile = "/GASKELL/RQ36_V3/history/timeHistory.bth";
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.shapeModelFileExtension = ".vtk";
            c.hasImageMap = false;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V4";
            c.rootDirOnServer = "/bennu/bennu-simulated-v4";
            c.shapeModelFileNames = prepend(c.rootDirOnServer + "/shape", "shape0.obj.gz", "shape1.vtk.gz", "shape2.vtk.gz", "shape3.vtk.gz", "shape4.vtk.gz");
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.26;
            c.rotationRate = 0.0004061303295118512;
            c.presentInMissions = AllBennuClients;
            if(Configuration.isMac()) c.hasBigmap = false;  // Right now bigmap only works on Macs

            c.hasMapmaker = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "RQ36V4_POLY", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "RQ36V4_MAP", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.MAPCAM
                            )
            };

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V4/OLA/trees/default/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/Phase07_OB/fileList.txt";

            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            c.generateStateHistoryParameters();

            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/mapcam/imagelist-fullpath.txt", "RQ36V4_MAP"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/bennu-simulated-v4/polycam/imagelist-fullpath.txt", "RQ36V4_POLY"),
            };


            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181109b;
            c.modelLabel = "ALTWG-SPC-v20181109b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181109b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", true, false, true),
            												 c.generateMapcamInstrument("bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", true, false, true)
            };
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;
            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");

            c.setSpectrumParameters();
            c.setLidarParameters(true);
        	c.presentInMissions  = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181115;
            c.modelLabel = "ALTWG-SPC-v20181115"; // NOTE: labeled SPC, but this is a Palmer model.
            c.rootDirOnServer = "/bennu/altwg-spc-v20181115";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181115_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[] { ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setSpectrumParameters();
            c.setLidarParameters(true);

            c.hasMapmaker = false;
        	c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181116;
            c.modelLabel = "ALTWG-SPC-v20181116";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181116";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;
            c.setSpectrumParameters();

            c.setLidarParameters(true);
			c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181123b;
            c.modelLabel = "ALTWG-SPC-v20181123b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181123b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();
            c.hasMapmaker = false;
            c.setLidarParameters(true);
			c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181202;
            c.modelLabel = "ALTWG-SPC-v20181202";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181202";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();

      		c.hasMapmaker = false;

            c.setLidarParameters(false);
			c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181206;
            c.modelLabel = "ALTWG-SPC-v20181206";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181206";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralImageMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

            c.setSpectrumParameters();
            c.generateStateHistoryParameters();
      		c.hasMapmaker = false;
            c.setLidarParameters(false);
			c.presentInMissions = new SbmtMultiMissionTool.Mission[] {};
            configArray.add(c);
        }


        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181217;
            c.modelLabel = "SPC v13";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181217";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", true, false),
					 										 c.generateMapcamInstrument("bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", true, false),
					 										 c.generateNavcamInstrument("bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public1217 = (BennuConfigs)c.clone();
//            public1217.author = ShapeModelType.provide("ALTWG-SPC-v20181217_SPC_v13_PUBLIC");
            public1217.modelLabel = "SPC v13";
            public1217.imagingInstruments = new ImagingInstrument[] { public1217.generatePolycamInstrument("bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", true, false, true),	//false = SPC only
            															public1217.generateMapcamInstrument("bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", true, false, true),
            };
            public1217.disableSpectra();
            public1217.hasImageMap = false;
            public1217.presentInMissions = PublicOnly;
            public1217.lidarBrowseDataSourceMap.put("Default", public1217.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public1217.lidarBrowseFileListResourcePath = public1217.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public1217.lidarBrowseWithPointsDataSourceMap.put("Default", public1217.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public1217.orexSearchTimeMap.remove("Recon");
	        public1217.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public1217);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181227;
            c.modelLabel = "SPC v14";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181227";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;


            if(Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20181227_navcam", "bennu_altwgspcv20181227_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public1227 = (BennuConfigs)c.clone();
//            public1227.author = ShapeModelType.provide("ALTWG-SPC-v20181227_SPC_v14_PUBLIC");
            public1227.modelLabel = "SPC v14";
            public1227.imagingInstruments = new ImagingInstrument[] { public1227.generatePolycamInstrument("bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", true, false, true),	//false = SPC only
            														public1227.generateMapcamInstrument("bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", true, false, true),
            };
            public1227.disableSpectra();
            public1227.hasImageMap = false;
            public1227.presentInMissions = PublicOnly;
            public1227.lidarBrowseDataSourceMap.put("Default", public1227.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public1227.lidarBrowseFileListResourcePath = public1227.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public1227.lidarBrowseWithPointsDataSourceMap.put("Default", public1227.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public1227.orexSearchTimeMap.remove("Recon");
	        public1227.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public1227);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190105;
            c.modelLabel = "ALTWG-SPC-v20190105";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190105";

            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            c.bodyReferencePotential = -0.02654811544296466;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs


            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190105_polycam", "bennu_altwgspcv20190105_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190105_mapcam", "bennu_altwgspcv20190105_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190105_navcam", "bennu_altwgspcv20190105_navcam")
            };

            c.setSpectrumParameters();

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190105/dtm/browse/fileList.txt");

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190105_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190105/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190105_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190114;
            c.modelLabel = "ALTWG-SPC-v20190114";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190114";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.260;
            c.rotationRate = 0.00040613;
            c.bodyReferencePotential = -0.02637307554771602;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs


            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190114_polycam", "bennu_altwgspcv20190114_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190114_mapcam", "bennu_altwgspcv20190114_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190114_navcam", "bennu_altwgspcv20190114_navcam")
            };

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190114/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190114_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190114/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190114_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190117;
            c.modelLabel = "ALTWG-SPC-v20190117";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190117";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02530442113463265;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs


            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190117_polycam", "bennu_altwgspcv20190117_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190117_mapcam", "bennu_altwgspcv20190117_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190117_navcam", "bennu_altwgspcv20190117_navcam")
            };

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190117/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190117_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190117/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190117_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190121;
            c.modelLabel = "SPC v20";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190121";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02513575424405747;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190121_navcam", "bennu_altwgspcv20190121_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190121/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190121_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190121/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190121_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public0121 = (BennuConfigs)c.clone();
//            public0121.author = ShapeModelType.provide("ALTWG-SPC-v20190121_SPC_v20_PUBLIC");
            public0121.modelLabel = "SPC v20";
            public0121.imagingInstruments = new ImagingInstrument[] { public0121.generatePolycamInstrument("bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", true, false, true),	//false = SPC only
            															public0121.generateMapcamInstrument("bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", true, false, true),
            };
            public0121.disableSpectra();
            public0121.hasImageMap = true;
            public0121.presentInMissions = PublicOnly;
            public0121.lidarBrowseDataSourceMap.put("Default", public0121.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0121.lidarBrowseFileListResourcePath = public0121.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public0121.lidarBrowseWithPointsDataSourceMap.put("Default", public0121.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0121.orexSearchTimeMap.remove("Recon");
	        public0121.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public0121);

        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207a;
            c.modelLabel = "ALTWG-SPC-v20190207a";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207a";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.0253033332766406;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190207a_polycam", "bennu_altwgspcv20190207a_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190207a_mapcam", "bennu_altwgspcv20190207a_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190207a_navcam", "bennu_altwgspcv20190207a_navcam")
            };

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207a/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207a_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207a/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207a_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207b;
            c.modelLabel = "ALTWG-SPC-v20190207b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207b";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02528907231151947;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190207b_polycam", "bennu_altwgspcv20190207b_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190207b_mapcam", "bennu_altwgspcv20190207b_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190207b_navcam", "bennu_altwgspcv20190207b_navcam")
            };

            c.setSpectrumParameters();

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207b/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190207b_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190207b/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190207b_navcam")
            };
            c.presentInMissions = InternalOnly;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190414;
            c.modelLabel = "SPC v28";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190414";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02520767997203304;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190414_navcam", "bennu_altwgspcv20190414_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190414/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
            	new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_mapcam"),
            	new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190414_polycam"),

            	new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_mapcam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_polycam"),
            	new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190414/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190414_navcam")
            };
            c.presentInMissions = InternalOnly;
            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public0414 = (BennuConfigs)c.clone();
//            public0414.author = ShapeModelType.provide("ALTWG-SPC-v20190414_SPC_v28_PUBLIC");
            public0414.modelLabel = "SPC v28";
            public0414.imagingInstruments = new ImagingInstrument[] { public0414.generatePolycamInstrument("bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", true, false, true),	//false = SPC only
            														public0414.generateMapcamInstrument("bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", true, false, true),
            };
            public0414.disableSpectra();
            public0414.hasImageMap = true;
            public0414.baseMapConfigName = "config_public.txt";
            public0414.presentInMissions = PublicOnly;
            public0414.lidarBrowseDataSourceMap.put("Default", public0414.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0414.lidarBrowseFileListResourcePath = public0414.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public0414.lidarBrowseWithPointsDataSourceMap.put("Default", public0414.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0414.orexSearchTimeMap.remove("Recon");
	        public0414.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public0414);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPO_v20190612;
            c.modelLabel = "SPO v34";
            c.rootDirOnServer = "/bennu/altwg-spo-v20190612";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517871436774813;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspov20190612_polycam", "bennu_altwgspov20190612_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspov20190612_mapcam", "bennu_altwgspov20190612_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspov20190612_navcam", "bennu_altwgspov20190612_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spo-v20190612/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspov20190612_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/polycam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spo-v20190612/navcam/imagelist-fullpath-info.txt", "bennu_altwgspov20190612_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public0612 = (BennuConfigs)c.clone();
//            public0612.author = ShapeModelType.provide("ALTWG-SPC-v20190612_SPC_v34_PUBLIC");
            public0612.modelLabel = "SPO v34";
            public0612.imagingInstruments = new ImagingInstrument[] { public0612.generatePolycamInstrument("bennu_altwgspov20190612_polycam", "bennu_altwgspov20190612_polycam", true, false, true),
            														public0612.generateMapcamInstrument("bennu_altwgspov20190612_mapcam", "bennu_altwgspov20190612_mapcam", true, false, true),
            };
            public0612.disableSpectra();
            public0612.hasImageMap = true;
            public0612.baseMapConfigName = "config_public.txt";
            public0612.presentInMissions = PublicOnly;
            public0612.lidarBrowseDataSourceMap.put("Default", public0612.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0612.lidarBrowseFileListResourcePath = public0612.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public0612.lidarBrowseWithPointsDataSourceMap.put("Default", public0612.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0612.orexSearchTimeMap.remove("Recon");
	        public0612.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public0612);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("ALTWG-SPC-v20190828");
            c.modelLabel = "SPC v42";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190828";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517940647257273;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20190828_polycam", "bennu_altwgspcv20190828_polycam", true, false),
															 c.generateMapcamInstrument("bennu_altwgspcv20190828_mapcam", "bennu_altwgspcv20190828_mapcam", true, false),
															 c.generateNavcamInstrument("bennu_altwgspcv20190828_navcam", "bennu_altwgspcv20190828_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190828/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20190828_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20190828/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20190828_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs public0828 = (BennuConfigs)c.clone();
//            public0828.author = ShapeModelType.provide("ALTWG-SPC-v20190828_SPC_v42_PUBLIC");
            public0828.modelLabel = "SPC v42";
            public0828.imagingInstruments = new ImagingInstrument[] { public0828.generatePolycamInstrument("bennu_altwgspcv20190828_polycam", "bennu_altwgspcv20190828_polycam", true, false, true),	//false = SPC only
            														public0828.generateMapcamInstrument("bennu_altwgspcv20190828_mapcam", "bennu_altwgspcv20190828_mapcam", true, false, true),
            };
            public0828.disableSpectra();
            public0828.hasImageMap = true;
            public0828.presentInMissions = PublicOnly;
            public0828.baseMapConfigName = "config_public.txt";

            public0828.lidarBrowseDataSourceMap.put("Default", public0828.rootDirOnServer + "/ola/browse/fileList_public.txt");
            public0828.lidarBrowseFileListResourcePath = public0828.rootDirOnServer + "/ola/browse/fileList_public.txt";
            public0828.lidarBrowseWithPointsDataSourceMap.put("Default", public0828.rootDirOnServer + "/ola/browse/fileList_public.txt");
	        public0828.orexSearchTimeMap.remove("Recon");
	        public0828.lidarSearchDataSourceMap.remove("Recon");
	        if (publicOnly)
	        	configArray.add(public0828);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.provide("ALTWG-SPC-v20191027");
            c.modelLabel = c.author.name();
            c.rootDirOnServer = "/bennu/altwg-spc-v20191027";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.186;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02517940647257273;
            c.hasCustomBodyCubeSize = true;
            c.customBodyCubeSize = 0.02; //km

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_altwgspcv20191027_polycam", "bennu_altwgspcv20191027_polycam", true, true),
															 c.generateMapcamInstrument("bennu_altwgspcv20191027_mapcam", "bennu_altwgspcv20191027_mapcam", true, true),
															 c.generateNavcamInstrument("bennu_altwgspcv20191027_navcam", "bennu_altwgspcv20191027_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;

            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20191027/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-sum.txt", "bennu_altwgspcv20191027_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/mapcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/polycam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/altwg-spc-v20191027/navcam/imagelist-fullpath-info.txt", "bennu_altwgspcv20191027_navcam")
            };

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v20");
            c.modelLabel = "OLA v20";
            c.rootDirOnServer = "/bennu/ola-v20-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02527683882517149;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_olav20_polycam", "bennu_olav20_polycam", true, true),
															 c.generateMapcamInstrument("bennu_olav20_mapcam", "bennu_olav20_mapcam", true, true),
															 c.generateNavcamInstrument("bennu_olav20_navcam", "bennu_olav20_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.clear();
            c.orexSearchTimeMap.clear();
            c.orexSearchTimeMap.put("OLAv20", startStop);
            c.lidarSearchDataSourceMap.put("OLAv20", c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            c.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.dtmBrowseDataSourceMap.put("Default", "bennu/ola-v20-spc/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav20_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav20_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav20_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav20_navcam")
            };

            if (!publicOnly)
            	configArray.add(c);

            //public version
            BennuConfigs publicOLA = (BennuConfigs)c.clone();
//            publicOLA.author = ShapeModelType.provide("OLA-v20_PUBLIC");
            publicOLA.modelLabel = "OLA v20";
            publicOLA.disableSpectra();
            publicOLA.hasImageMap = true;
            publicOLA.presentInMissions = PublicOnly;
            publicOLA.baseMapConfigName = "config_public.txt";

            publicOLA.imagingInstruments = new ImagingInstrument[] { publicOLA.generatePolycamInstrument("bennu_olav20_polycam", "bennu_olav20_polycam", true, true, true),
					 publicOLA.generateMapcamInstrument("bennu_olav20_mapcam", "bennu_olav20_mapcam", true, true, true),
					 publicOLA.generateNavcamInstrument("bennu_olav20_navcam", "bennu_olav20_navcam", true)
            };
            publicOLA.hasHypertreeBasedSpectraSearch = false;
            publicOLA.lidarBrowseDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLA.lidarBrowseFileListResourcePath = publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            publicOLA.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLA.lidarSearchDataSourceMap.clear();
            publicOLA.orexSearchTimeMap.clear();
            publicOLA.orexSearchTimeMap.put("OLAv20", startStop);
            publicOLA.lidarSearchDataSourceMap.put("OLAv20", publicOLA.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            if (publicOnly)
            	configArray.add(publicOLA);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v20-PTM");
            c.modelLabel = "OLA v20 PTM";
            c.rootDirOnServer = "/bennu/ola-v20-ptm";
            c.setResolution( //
                    ImmutableList.of("Medium (217032 plates)", "High (886904 plates)", "Very High (3366134 plates)"),
                    ImmutableList.of(217032, 886904, 3366134));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02527683882517149;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_olav20ptm_polycam", "bennu_olav20ptm_polycam", true, true),
            												 c.generateMapcamInstrument("bennu_olav20ptm_mapcam", "bennu_olav20ptm_mapcam", true, true),
            												 c.generateNavcamInstrument("bennu_olav20ptm_navcam", "bennu_olav20ptm_navcam")
            };

            c.generateStateHistoryParameters();

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.clear();
            c.orexSearchTimeMap.clear();
            c.orexSearchTimeMap.put("OLAv20", startStop);
            c.lidarSearchDataSourceMap.put("OLAv20", c.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            c.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.hasMapmaker = false;

            c.dtmBrowseDataSourceMap.put("Default", "bennu/ola-v20-ptm/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-sum.txt", "bennu_olav20ptm_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/mapcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/polycam/imagelist-fullpath-info.txt", "bennu_olav20ptm_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v20-ptm/navcam/imagelist-fullpath-info.txt", "bennu_olav20ptm_navcam")
            };
            if (!publicOnly)
            	configArray.add(c);


            //public version
            BennuConfigs publicOLAptm = (BennuConfigs)c.clone();
//            publicOLAptm.author = ShapeModelType.provide("OLA-v20-PTM_PUBLIC");
            publicOLAptm.modelLabel = "OLA v20 PTM";
            publicOLAptm.disableSpectra();
            publicOLAptm.hasImageMap = true;
            publicOLAptm.baseMapConfigName = "config_public.txt";

            publicOLAptm.presentInMissions = PublicOnly;
            publicOLAptm.imagingInstruments = new ImagingInstrument[] { publicOLAptm.generatePolycamInstrument("bennu_olav20ptm_polycam", "bennu_olav20ptm_polycam", true, true, true),
            		publicOLAptm.generateMapcamInstrument("bennu_olav20ptm_mapcam", "bennu_olav20ptm_mapcam", true, true, true),
            		publicOLAptm.generateNavcamInstrument("bennu_olav20ptm_navcam", "bennu_olav20ptm_navcam", true)
            };
            publicOLAptm.hasHypertreeBasedSpectraSearch = false;
            publicOLAptm.lidarBrowseDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLAptm.lidarBrowseFileListResourcePath = publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            publicOLAptm.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLAptm.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLAptm.lidarSearchDataSourceMap.clear();
            publicOLAptm.orexSearchTimeMap.clear();
            publicOLAptm.orexSearchTimeMap.put("OLAv20", startStop);
            publicOLAptm.lidarSearchDataSourceMap.put("OLAv20", publicOLAptm.rootDirOnServer + "/ola/search/olav20/dataSource.lidar");
            if (publicOnly)
            	configArray.add(publicOLAptm);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("OLA-v21");
            c.modelLabel = "OLA v21";
            c.rootDirOnServer = "/bennu/ola-v21-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.1953;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02524469206484981;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_olav21_polycam", "bennu_olav21_polycam", true, true),
                                                             c.generateMapcamInstrument("bennu_olav21_mapcam", "bennu_olav21_mapcam", true, true),
                                                             c.generateNavcamInstrument("bennu_olav21_navcam", "bennu_olav21_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.clear();
            c.orexSearchTimeMap.clear();
            c.orexSearchTimeMap.put("OLAv21", startStop);
            c.lidarSearchDataSourceMap.put("OLAv21", c.rootDirOnServer + "/ola/search/olav21/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            c.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.dtmBrowseDataSourceMap.put("Default", "bennu/ola-v21-spc/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_olav21_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-sum.txt", "bennu_olav21_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/mapcam/imagelist-fullpath-info.txt", "bennu_olav21_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/polycam/imagelist-fullpath-info.txt", "bennu_olav21_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/ola-v21-spc/navcam/imagelist-fullpath-info.txt", "bennu_olav21_navcam")
            };

            c.defaultForMissions = OREXClients;
            if (!publicOnly)
                configArray.add(c);

            //public version
            BennuConfigs publicOLA = (BennuConfigs)c.clone();
//            publicOLA.author = ShapeModelType.provide("OLA-v21_PUBLIC");
            publicOLA.modelLabel = "OLA v21";
            publicOLA.disableSpectra();
            publicOLA.hasImageMap = true;
            publicOLA.presentInMissions = PublicOnly;
            publicOLA.baseMapConfigName = "config_public.txt";

            publicOLA.imagingInstruments = new ImagingInstrument[] { publicOLA.generatePolycamInstrument("bennu_olav21_polycam", "bennu_olav21_polycam", true, true, true),
                     publicOLA.generateMapcamInstrument("bennu_olav21_mapcam", "bennu_olav21_mapcam", true, true, true),
                     publicOLA.generateNavcamInstrument("bennu_olav21_navcam", "bennu_olav21_navcam", true)
            };
            publicOLA.hasHypertreeBasedSpectraSearch = false;
            publicOLA.lidarBrowseDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLA.lidarBrowseFileListResourcePath = publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            publicOLA.lidarBrowseWithPointsDataSourceMap.put("Default", publicOLA.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicOLA.lidarSearchDataSourceMap.clear();
            publicOLA.orexSearchTimeMap.clear();
            publicOLA.orexSearchTimeMap.put("OLAv21", startStop);
            publicOLA.lidarSearchDataSourceMap.put("OLAv21", publicOLA.rootDirOnServer + "/ola/search/olav21/dataSource.lidar");
            if (publicOnly)
                configArray.add(publicOLA);
        }

        if (Configuration.isAPLVersion())
        {
            c = new BennuConfigs();
            c.setBodyParameters();

            c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
            c.author = ShapeModelType.provide("SPO-v54");
            c.modelLabel = "SPO v54";
            c.rootDirOnServer = "/bennu/spo-v54-spc";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.density = 1.194;
            c.rotationRate = 4.0626E-4;
            c.bodyReferencePotential = -0.02538555084803482;

            if(Configuration.isMac()) c.hasBigmap = true;  // Right now bigmap only works on Macs

            c.imagingInstruments = new ImagingInstrument[] { c.generatePolycamInstrument("bennu_spov54_polycam", "bennu_spov54_polycam", true, true),
                                                             c.generateMapcamInstrument("bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true),
                                                             c.generateNavcamInstrument("bennu_spov54_navcam", "bennu_spov54_navcam")
            };

            c.setSpectrumParameters();
            c.hasHypertreeBasedSpectraSearch = true;
            c.generateStateHistoryParameters();

            c.hasMapmaker = false;

            c.setLidarParameters(true);
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 6, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 7, 6, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.clear();
            c.orexSearchTimeMap.clear();
            c.orexSearchTimeMap.put("SPOv54", startStop);
            c.lidarSearchDataSourceMap.put("SPOv54", c.rootDirOnServer + "/ola/search/olav54/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            c.lidarBrowseWithPointsDataSourceMap.put("Default", c.rootDirOnServer + "/ola/l2a/fileListL2A.txt");

            c.dtmBrowseDataSourceMap.put("Default", "bennu/spo-v54-spc/dtm/browse/fileList.txt");

            c.databaseRunInfos = new DBRunInfo[]
            {
                new DBRunInfo(ImageSource.GASKELL, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-sum.txt", "bennu_spov54_mapcam"),
                new DBRunInfo(ImageSource.GASKELL, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-sum.txt", "bennu_spov54_polycam"),

                new DBRunInfo(ImageSource.SPICE, Instrument.MAPCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/mapcam/imagelist-fullpath-info.txt", "bennu_spov54_mapcam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.POLYCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/polycam/imagelist-fullpath-info.txt", "bennu_spov54_polycam"),
                new DBRunInfo(ImageSource.SPICE, Instrument.NAVCAM, ShapeModelBody.RQ36.toString(), "/bennu/spo-v54-spc/navcam/imagelist-fullpath-info.txt", "bennu_spov54_navcam")
            };

            if (!publicOnly)
                configArray.add(c);

            //public version
            BennuConfigs publicModel = (BennuConfigs)c.clone();
//            publicModel.author = ShapeModelType.provide("SPO-v54_PUBLIC");
            publicModel.modelLabel = "SPO v54";
            publicModel.disableSpectra();
            publicModel.hasImageMap = true;
            publicModel.presentInMissions = PublicOnly;
            publicModel.baseMapConfigName = "config_public.txt";

            publicModel.imagingInstruments = new ImagingInstrument[] { publicModel.generatePolycamInstrument("bennu_spov54_polycam", "bennu_spov54_polycam", true, true, true),
                     publicModel.generateMapcamInstrument("bennu_spov54_mapcam", "bennu_spov54_mapcam", true, true, true),
                     publicModel.generateNavcamInstrument("bennu_spov54_navcam", "bennu_olav54_navcam", true)
            };
            publicModel.hasHypertreeBasedSpectraSearch = false;
            publicModel.lidarBrowseDataSourceMap.put("Default", publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicModel.lidarBrowseFileListResourcePath = publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt";
            publicModel.lidarBrowseWithPointsDataSourceMap.put("Default", publicModel.rootDirOnServer + "/ola/l2a/fileListL2A.txt");
            publicModel.lidarSearchDataSourceMap.clear();
            publicModel.orexSearchTimeMap.clear();
            publicModel.orexSearchTimeMap.put("SPOv54", startStop);
            publicModel.lidarSearchDataSourceMap.put("SPOv54", publicModel.rootDirOnServer + "/ola/search/olav54/dataSource.lidar");
            if (publicOnly)
                configArray.add(publicModel);
        }

    }

	private void generateStateHistoryParameters()
	{
        hasStateHistory = true;
        timeHistoryFile = rootDirOnServer + "/history/timeHistory.bth";
        stateHistoryStartDate = new GregorianCalendar(2018, 10, 25, 0, 0, 0).getTime();
        stateHistoryEndDate = new GregorianCalendar(2025, 1, 1, 0, 0, 0).getTime();
        spiceInfo = new SpiceInfo("ORX", "IAU_BENNU", "ORX_SPACECRAFT", "BENNU",
    			new String[] {"EARTH" , "SUN"}, new String[] {"ORX_OCAMS_POLYCAM", "ORX_OCAMS_MAPCAM",
    															"ORX_OCAMS_SAMCAM", "ORX_NAVCAM1", "ORX_NAVCAM2",
//    															"ORX_OTES", "ORX_OVIRS",
    															"ORX_OLA_LOW", "ORX_OLA_HIGH"});
	}

	private ImagingInstrument generatePolycamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE)
	{
		return generatePolycamInstrument(spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, false);
	}

	private ImagingInstrument generatePolycamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		String gallery = "/polycam/gallery";
		ImageSource[] imageSources = {};
		ArrayList<ImageSource> imageSourceArray = new ArrayList<ImageSource>();
		if (includeSPC) imageSourceArray.add(ImageSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(ImageSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);
		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/polycam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
		phpQuery.setPublicOnly(publicOnly);
		phpQuery.setImageNameTable("bennu_polycam_images");
		ImagingInstrument instrument = new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.POLYCAM_FLIGHT_IMAGE,
                imageSources,
                Instrument.POLYCAM
                );
		return instrument;
	}

	private ImagingInstrument generateMapcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE)
	{
		return generateMapcamInstrument(spcNamePrefix, spiceNamePrefix, includeSPC, includeSPICE, false);
	}

	private ImagingInstrument generateMapcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean includeSPC, boolean includeSPICE, boolean publicOnly)
	{
		String gallery = "/mapcam/gallery";
		ImageSource[] imageSources = {};
		ArrayList<ImageSource> imageSourceArray = new ArrayList<ImageSource>();
		if (includeSPC) imageSourceArray.add(ImageSource.GASKELL);
		if (includeSPICE) imageSourceArray.add(ImageSource.SPICE);
		imageSources = imageSourceArray.toArray(imageSources);
		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/mapcam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
		phpQuery.setPublicOnly(publicOnly);
		phpQuery.setImageNameTable("bennu_mapcam_images");
		return new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.MAPCAM_FLIGHT_IMAGE,
                imageSources,
                Instrument.MAPCAM
                );
	}

	private ImagingInstrument generateNavcamInstrument(String spcNamePrefix, String spiceNamePrefix)
	{
		return generateNavcamInstrument(spcNamePrefix, spiceNamePrefix, false);
	}

	private ImagingInstrument generateNavcamInstrument(String spcNamePrefix, String spiceNamePrefix, boolean publicOnly)
	{
		String gallery = "/navcam/gallery";
		GenericPhpQuery phpQuery = new GenericPhpQuery(rootDirOnServer + "/navcam", spcNamePrefix, spiceNamePrefix, rootDirOnServer + gallery);
		phpQuery.setPublicOnly(publicOnly);
		phpQuery.setImageNameTable("bennu_navcam_images");
		return new ImagingInstrument(
                SpectralImageMode.MONO,
                phpQuery,
                ImageType.NAVCAM_FLIGHT_IMAGE,
                new ImageSource[]{ImageSource.SPICE},
                Instrument.NAVCAM
                );
	}

	private void setBodyParameters()
	{
		body = ShapeModelBody.RQ36;
        type = BodyType.ASTEROID;
        population = ShapeModelPopulation.NEO;
        shapeModelFileExtension = ".obj";
        imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
        imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
        imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
        imageSearchDefaultMaxResolution = 1.0e3;
        useMinimumReferencePotential = true;
        hasImageMap = true;

	}

	private void disableSpectra()
	{
		hasSpectralData = false;
        hasHierarchicalSpectraSearch = false;
        hasHypertreeBasedSpectraSearch = false;
        spectraSearchDataSourceMap.clear();
        spectralInstruments.clear();
	}

	private void setSpectrumParameters()
	{
		hasSpectralData = true;
        spectralInstruments = new ArrayList<BasicSpectrumInstrument>();
        spectralInstruments.add(new OTES());
        spectralInstruments.add(new OVIRS());

        hasHierarchicalSpectraSearch = true;
        hasHypertreeBasedSpectraSearch = false;
        spectraSearchDataSourceMap = new LinkedHashMap<>();
        spectraSearchDataSourceMap.put("OTES_L2", rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
        spectraSearchDataSourceMap.put("OTES_L3", rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
        spectraSearchDataSourceMap.put("OVIRS_IF", rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
        spectraSearchDataSourceMap.put("OVIRS_REF", rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");

        SpectrumInstrumentMetadataIO specIO = new SpectrumInstrumentMetadataIO("OREX", instrumentSearchSpecs);
        hierarchicalSpectraSearchSpecification = specIO;
	}

	private void setLidarParameters(boolean hasHypertree)
	{
		 hasLidarData = true;
         hasHypertreeBasedLidarSearch = hasHypertree; // enable tree-based lidar searching
         lidarInstrumentName = Instrument.OLA;
         lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
         lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
         lidarSearchDataSourceMap = new LinkedHashMap<>();
         lidarBrowseDataSourceMap = new LinkedHashMap<>();
         lidarBrowseDataSourceMap.put("Default", rootDirOnServer + "/ola/browse/fileListv2.txt");
         lidarBrowseFileListResourcePath = rootDirOnServer + "/ola/browse/fileListv2.txt";
         lidarBrowseWithPointsDataSourceMap.put("Default", rootDirOnServer + "/ola/browse/fileListv2.txt");

         if (hasHypertree)
         {
	         /*
	          * search times split into phases
	          */
	         ArrayList<Date> startStop = new ArrayList<Date>();
	         startStop.add(lidarSearchDefaultStartDate);
	         startStop.add(lidarSearchDefaultEndDate);
	         orexSearchTimeMap.put("Default", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(lidarSearchDefaultStartDate);
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         orexSearchTimeMap.put("Preliminary", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         orexSearchTimeMap.put("Detailed", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         orexSearchTimeMap.put("OrbB", startStop);

	         startStop = new ArrayList<Date>();
	         startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
	         startStop.add(lidarSearchDefaultEndDate);
	         orexSearchTimeMap.put("Recon", startStop);

	         lidarSearchDataSourceMap.put("Preliminary", rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
	         lidarSearchDataSourceMap.put("Detailed", rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
	         lidarSearchDataSourceMap.put("OrbB", rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
	         lidarSearchDataSourceMap.put("Recon", rootDirOnServer + "/ola/search/recon/dataSource.lidar");
         }

         lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
         lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
         lidarBrowseIsSpacecraftInSphericalCoordinates = false;
         lidarBrowseTimeIndex = 26;
         lidarBrowseNoiseIndex = 62;
         lidarBrowseOutgoingIntensityIndex = 98;
         lidarBrowseReceivedIntensityIndex = 106;
         lidarBrowseIntensityEnabled = true;
         lidarBrowseNumberHeaderLines = 0;
         lidarBrowseIsInMeters = true;
         lidarBrowseIsBinary = true;
         lidarBrowseBinaryRecordSize = 186;
         lidarOffsetScale = 0.0005;
	}

	@Override
    public boolean isAccessible()
    {
        return FileCache.instance().isAccessible(getShapeModelFileNames()[0]);
    }

    @Override
    public Instrument getLidarInstrument()
    {
        // TODO Auto-generated method stub
        return lidarInstrumentName;
    }

    public boolean hasHypertreeLidarSearch()
    {
        return hasHypertreeBasedLidarSearch;
    }

    public SpectraHierarchicalSearchSpecification<?> getHierarchicalSpectraSearchSpecification()
    {
        return hierarchicalSpectraSearchSpecification;
    }
}
