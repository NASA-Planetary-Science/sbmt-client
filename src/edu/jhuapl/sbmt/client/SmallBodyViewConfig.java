package edu.jhuapl.sbmt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.SessionConfiguration;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.OREXSpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.model.bennu.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.otes.SpectraHierarchicalSearchSpecification;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.image.BasicImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.phobos.PhobosExperimentalSearchSpecification;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.Hayabusa2SpectrumInstrumentMetadataIO;
import edu.jhuapl.sbmt.model.spectrum.instruments.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;

/**
* A SmallBodyConfig is a class for storing all which models should be instantiated
* together with a particular small body. For example some models like Eros
* have imaging, spectral, and lidar data whereas other models may only have
* imaging data. This class is also used when creating (to know which tabs
* to create).
*/
public class SmallBodyViewConfig extends BodyViewConfig implements ISmallBodyViewConfig
{
    static public SmallBodyViewConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelType author)
    {
        return (SmallBodyViewConfig) getConfig(name, author, null);
    }

    static public SmallBodyViewConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelType author, String version)
    {
        return (SmallBodyViewConfig) getConfig(name, author, version);
    }

    public static SmallBodyViewConfig ofCustom(String name, boolean temporary)
    {
        SmallBodyViewConfig config = new SmallBodyViewConfig(ImmutableList.<String>of(name), ImmutableList.<Integer>of(1));
        config.modelLabel = name;
        config.customTemporary = temporary;
        config.author = ShapeModelType.CUSTOM;

        SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
        String fileName = temporary ? safeUrlPaths.getUrl(config.modelLabel) : safeUrlPaths.getUrl(safeUrlPaths.getString(Configuration.getImportedShapeModelsDir(), config.modelLabel, "model.vtk"));

        config.shapeModelFileNames = new String[] { fileName };

        return config;
    }

    public static void initialize()
    {
        List<ViewConfig> configArray = getBuiltInConfigs();

        // Gaskell Eros
        SmallBodyViewConfig c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EROS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2008)";
        c.rootDirOnServer = "/GASKELL/EROS";
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth";
        c.hasImageMap = true;
        c.hasStateHistory = true;
        c.shapeModelFileNames = prepend("/EROS", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS", "/GASKELL/EROS/MSI/gallery"),
                        ImageType.MSI_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL_UPDATED, ImageSource.SPICE},
                        Instrument.MSI
                        )
        };

        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasRemoteMapmaker = true;
        c.bodyDensity = 2.67;
        c.bodyRotationRate = 0.000331165761670640;
        c.bodyReferencePotential = -53.765039959572114;
        c.bodyLowestResModelName = "EROS/shape/shape0.obj";

        c.hasSpectralData = true;
        c.spectralInstruments = new BasicSpectrumInstrument[] {
                new NIS()
        };

        c.hasLineamentData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "iofdbl", "cifdbl" };
        c.imageSearchDefaultMaxSpacecraftDistance = 1000.0;
        c.imageSearchDefaultMaxResolution = 50.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
        c.lidarBrowseXYZIndices = new int[] { 14, 15, 16 };
        c.lidarBrowseSpacecraftIndices = new int[] { 8, 9, 10 };
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
        c.lidarBrowseTimeIndex = 4;
        c.lidarBrowseNoiseIndex = 7;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/NlrFiles.txt";
        c.lidarBrowseNumberHeaderLines = 2;
        c.lidarBrowseIsInMeters = true;
        c.lidarOffsetScale = 0.025;
        c.lidarInstrumentName = Instrument.NLR;
        configArray.add(c);

        // Thomas Eros
        c = c.clone();
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2001)";
        c.rootDirOnServer = "/THOMAS/EROS";
        c.shapeModelFileNames = prepend(c.rootDirOnServer, "eros001708.obj.gz", "eros007790.obj.gz", "eros010152.obj.gz", "eros022540.obj.gz", "eros089398.obj.gz", "eros200700.obj.gz");
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.setResolution(ImmutableList.of(
                "1708 plates", "7790 plates", "10152 plates",
                "22540 plates", "89398 plates", "200700 plates"
        ), ImmutableList.of(
                1708, 7790, 10152, 22540, 89398, 200700
        ));
        c.hasMapmaker = false;
        configArray.add(c);

        // Eros NLR
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNLR;
        c.modelLabel = "Neumann et al. (2001)";
        c.rootDirOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO
        c.setResolution(ImmutableList.of(129600));

        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelType.EROSNAV;
        c.modelLabel = "NAV team (2001)";
        c.rootDirOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.setResolution(ImmutableList.of(56644));
        configArray.add(c);

        // Gaskell Itokawa
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell et al. (2008)";
        c.rootDirOnServer = "/GASKELL/ITOKAWA";
        c.shapeModelFileNames = prepend("/ITOKAWA", "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");

        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/ITOKAWA/history/TimeHistory.bth";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/ITOKAWA/AMICA", "AMICA", "/GASKELL/ITOKAWA/AMICA/gallery"),
                        ImageType.AMICA_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE, ImageSource.CORRECTED},
                        Instrument.AMICA
                        )
        };

        c.hasLidarData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 26.0;
        c.imageSearchDefaultMaxResolution = 3.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        c.lidarSearchDataSourceMap.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        c.lidarBrowseXYZIndices = new int[] { 6, 7, 8 };
        c.lidarBrowseSpacecraftIndices = new int[] { 3, 4, 5 };
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
        c.lidarBrowseTimeIndex = 1;
        c.lidarBrowseNoiseIndex = -1;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/HayLidarFiles.txt";
        c.lidarBrowseNumberHeaderLines = 0;
        c.lidarBrowseIsInMeters = false;
        // The following value is the Itokawa diagonal length divided by 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is 0.025 km.
        c.lidarOffsetScale = 0.00044228259621279913;
        c.lidarInstrumentName = Instrument.LIDAR;

        c.spectralInstruments = new BasicSpectrumInstrument[] {};

        configArray.add(c);

        // Ostro Itokawa
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.OSTRO;
        c.modelLabel = "Ostro et al. (2004)";
        c.rootDirOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        c.setResolution(ImmutableList.of(12192));
        configArray.add(c);

        c = new SmallBodyViewConfig();
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
        c.density = 1260;
        c.useMinimumReferencePotential = true;
        c.rotationRate = 0.00040613;
        configArray.add(c);

//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = ShapeModelType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.ENHANCED;
//            c.author = ShapeModelAuthor.GASKELL;
//            c.version = "V2";
//            c.rootDirOnServer = "/GASKELL/RQ36";
//            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
//            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
//            c.hasLidarData = true;
//            c.hasMapmaker = true;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
//            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36/OLA/cubes");
//            c.lidarBrowseXYZIndices = new int[]{96, 104, 112};
//            c.lidarBrowseSpacecraftIndices = new int[]{144, 152, 160};
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 18;
//            c.lidarBrowseNoiseIndex = -1;
//            c.lidarBrowseFileListResourcePath = "/edu/jhuapl/sbmt/data/OlaLidarFiles.txt";
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 168;
//            c.lidarOffsetScale = 0.0005;
//            c.lidarInstrumentName = Instrument.OLA;
//            configArray.add(c);
//        }

        // PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/RQ36_V3";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz", "ver128q.vtk.gz", "ver256q.vtk.gz", "ver512q.vtk.gz");
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.hasMapmaker = true;
            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/POLYCAM", "RQ36_POLY"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/POLYCAM", true),
                            ImageType.POLYCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/MAPCAM", "RQ36_MAP"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/MAPCAM"),
                            ImageType.MAPCAM_V3_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };

//            c.hasSpectralData = true;
//            c.spectralInstruments=new SpectralInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };

            c.density = 1.0;
            c.useMinimumReferencePotential = false;
            c.rotationRate = 0.000407026411379;
            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            // c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/cubes");
            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;
            c.lidarInstrumentName = Instrument.OLA;
            configArray.add(c);

            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            // default ideal data

//            c.lidarSearchDataSourceMap.put("Default","/GASKELL/RQ36_V3/OLA/trees/with_range2/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", "/bennu/bennu-simulated-v4/ola/search/hypertree/dataSource.lidar");

            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
            // noisy data
            c.lidarSearchDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Noise", "/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");

            c.hasStateHistory = true;
            c.timeHistoryFile = "/GASKELL/RQ36_V3/history/timeHistory.bth";
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "OREX Simulated";
            c.version = "V4";
            c.rootDirOnServer = "/bennu/bennu-simulated-v4";
            c.shapeModelFileNames = prepend(c.rootDirOnServer + "/shape", "shape0.obj.gz", "shape1.vtk.gz", "shape2.vtk.gz", "shape3.vtk.gz", "shape4.vtk.gz");
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = false;
            }
            c.hasMapmaker = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "RQ36V4_POLY", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "RQ36V4_MAP", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.MAPCAM
                            )
            };

//            c.hasSpectralData = true;
//            c.spectralInstruments=new SpectralInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
            c.density = 1.26;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.0004061303295118512;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V4/OLA/trees/default/tree/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/hypertree/dataSource.lidar");
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
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
//            c.lidarBrowseFileListResourcePath =  c.rootDirOnServer + "/ola/browse/default/fileList.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//            c.dtmSearchDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/search/hypertree/dataSource.lidar");

//            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
//                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
//            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//            }
            configArray.add(c);
        }

//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181109b;
//            c.modelLabel = "ALTWG-SPC-v20181109b";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181109b";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = false;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181115;
//            c.modelLabel = "ALTWG-SPC-v20181115"; // NOTE: labeled SPC, but this is a Palmer model.
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181115";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = false;
//            }
//
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181109b_polycam", "bennu_altwgspcv20181109b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[] { ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181109b_mapcam", "bennu_altwgspcv20181109b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181116;
//            c.modelLabel = "ALTWG-SPC-v20181116";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181116";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181116_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181116_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
////            c.dtmBrowseDataSourceMap.put("Default", "bennu/bennu-simulated-v4/dtm/browse/fileList.txt");
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181123b;
//            c.modelLabel = "ALTWG-SPC-v20181123b";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181123b";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181123b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181123b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//
//
//
//
//
//            /*
//             * New hypertrees split into phases
//             */
//            ArrayList<Date> startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Default", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(c.lidarSearchDefaultStartDate);
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);
//
//            startStop = new ArrayList<Date>();
//            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
//            startStop.add(c.lidarSearchDefaultEndDate);
//            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
//            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);
//
//            /*
//             *
//             */
//
//
//
//
//
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181202;
//            c.modelLabel = "ALTWG-SPC-v20181202";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181202";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181202_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181202_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=false; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }
//
//        if (Configuration.isAPLVersion())
//        {
//            c = new SmallBodyViewConfig();
//            c.body = ShapeModelBody.RQ36;
//            c.type = BodyType.ASTEROID;
//            c.population = ShapeModelPopulation.NEO;
//            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
//            c.author = ShapeModelType.ALTWG_SPC_v20181206;
//            c.modelLabel = "ALTWG-SPC-v20181206";
//            c.rootDirOnServer = "/bennu/altwg-spc-v20181206";
//            c.shapeModelFileExtension = ".obj";
//            c.setResolution(ImmutableList.of(
//                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
//                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
//            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
//            c.imageSearchDefaultMaxResolution = 1.0e3;
//            c.density = 1260;
//            c.useMinimumReferencePotential = true;
//            c.rotationRate = 0.00040613;
//            if(Configuration.isMac())
//            {
//                // Right now bigmap only works on Macs
//                c.hasBigmap = true;
//            }
//
//            c.imagingInstruments = new ImagingInstrument[] {
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181206_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            ImageType.POLYCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
//                            Instrument.POLYCAM
//                            ),
//                    new ImagingInstrument(
//                            SpectralMode.MONO,
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
////                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181206_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            ImageType.MAPCAM_FLIGHT_IMAGE,
//                            new ImageSource[]{ImageSource.SPICE},
//                            Instrument.MAPCAM
//                            )
//            };
//
//            c.hasSpectralData=true;
//            c.spectralInstruments=new BasicSpectrumInstrument[] {
//                    new OTES(),
//                    new OVIRS()
//            };
//
//            c.hasStateHistory = true;
//            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";
//
//            c.hasMapmaker = false;
//            c.hasHierarchicalSpectraSearch = true;
//            c.hasHypertreeBasedSpectraSearch = false;
//            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
//            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
//            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
//            c.spectrumMetadataFile =  c.rootDirOnServer + "/spectraMetadata.json";
//            try
//            {
//                //TODO: eventually point this to a URL
//                OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
//                specIO.setPathString(c.spectrumMetadataFile);
//                c.hierarchicalSpectraSearchSpecification = specIO;
//
//            }
//            catch (Exception e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            c.hasLidarData=true;
//            c.hasHypertreeBasedLidarSearch=false; // enable tree-based lidar searching
//            c.lidarInstrumentName = Instrument.OLA;
//            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
//            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
//            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
////            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
//            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
//            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";
//
//            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
//            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
//            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
//            c.lidarBrowseTimeIndex = 26;
//            c.lidarBrowseNoiseIndex = 62;
//            c.lidarBrowseOutgoingIntensityIndex = 98;
//            c.lidarBrowseReceivedIntensityIndex = 106;
//            c.lidarBrowseIntensityEnabled = true;
//            c.lidarBrowseNumberHeaderLines = 0;
//            c.lidarBrowseIsInMeters = true;
//            c.lidarBrowseIsBinary = true;
//            c.lidarBrowseBinaryRecordSize = 186;
//            c.lidarOffsetScale = 0.0005;
//
////            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
////                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
////            {
////                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
////            }
//            configArray.add(c);
//        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181217;
            c.modelLabel = "ALTWG-SPC-v20181217";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181217";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181217_polycam", "bennu_altwgspcv20181217_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181217_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181217_mapcam", "bennu_altwgspcv20181217_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181217_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20181217_navcam", "bennu_altwgspcv20181217_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

//            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
//                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
//            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20181227;
            c.modelLabel = "ALTWG-SPC-v20181227";
            c.rootDirOnServer = "/bennu/altwg-spc-v20181227";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181227_polycam", "bennu_altwgspcv20181227_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20181227_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20181227_mapcam", "bennu_altwgspcv20181227_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20181227_navcam", "bennu_altwgspcv20181227_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

//            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
//                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
//            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
//            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190105;
            c.modelLabel = "ALTWG-SPC-v20190105";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190105";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190105_polycam", "bennu_altwgspcv20190105_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190105_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190105_mapcam", "bennu_altwgspcv20190105_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190105_navcam", "bennu_altwgspcv20190105_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190114;
            c.modelLabel = "ALTWG-SPC-v20190114";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190114";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1260;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.00040613;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190114_polycam", "bennu_altwgspcv20190114_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190114_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190114_mapcam", "bennu_altwgspcv20190114_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190114_navcam", "bennu_altwgspcv20190114_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190114/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190117;
            c.modelLabel = "ALTWG-SPC-v20190117";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190117";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190117_polycam", "bennu_altwgspcv20190117_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190117_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190117_mapcam", "bennu_altwgspcv20190117_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190117_navcam", "bennu_altwgspcv20190117_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190117/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190121;
            c.modelLabel = "ALTWG-SPC-v20190121";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190121";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190121_polycam", "bennu_altwgspcv20190121_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190121_mapcam", "bennu_altwgspcv20190121_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190121_navcam", "bennu_altwgspcv20190121_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190121/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207a;
            c.modelLabel = "ALTWG-SPC-v20190207a";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207a";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207a_polycam", "bennu_altwgspcv20190207a_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207a_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190207a_mapcam", "bennu_altwgspcv20190207a_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190207a_navcam", "bennu_altwgspcv20190207a_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207a/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190207b;
            c.modelLabel = "ALTWG-SPC-v20190207b";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190207b";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207b_polycam", "bennu_altwgspcv20190207b_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190207b_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190207b_mapcam", "bennu_altwgspcv20190207b_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190207b_navcam", "bennu_altwgspcv20190207b_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190207b/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.ALTWG_SPC_v20190414;
            c.modelLabel = "ALTWG-SPC-v20190414";
            c.rootDirOnServer = "/bennu/altwg-spc-v20190414";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.density = 1186.0;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 4.0626E-4;

            c.hasImageMap = true;
            c.imageMaps = new String[] { "basemap/bennu_arrival_obl_1201_cnorm_CCv0001.png" };

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190414_polycam", "bennu_altwgspcv20190414_polycam", c.rootDirOnServer + "/polycam/gallery"),
//                            new GenericPhpQuery(c.rootDirOnServer + "/polycam", "bennu_altwgspcv20190414_polycam", c.rootDirOnServer + "/polycam/gallery"),
                            ImageType.POLYCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/mapcam", "bennu_altwgspcv20190414_mapcam", "bennu_altwgspcv20190414_mapcam", c.rootDirOnServer + "/mapcam/gallery"),
                            ImageType.MAPCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL,ImageSource.SPICE},
                            Instrument.MAPCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery(c.rootDirOnServer + "/navcam", "bennu_altwgspcv20190414_navcam", "bennu_altwgspcv20190414_navcam", c.rootDirOnServer + "/navcam/gallery"),
                            ImageType.NAVCAM_FLIGHT_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.NAVCAM
                            )
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = c.rootDirOnServer + "/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", c.rootDirOnServer + "/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", c.rootDirOnServer + "/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", c.rootDirOnServer + "/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", c.rootDirOnServer + "ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default", c.rootDirOnServer + "/ola/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = c.rootDirOnServer + "/ola/browse/fileList.txt";

            /*
             * New hypertrees split into phases
             */
            ArrayList<Date> startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Default", c.rootDirOnServer + "/ola/search/default/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Default", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(c.lidarSearchDefaultStartDate);
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Preliminary", c.rootDirOnServer + "/ola/search/preliminary/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Preliminary", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 0, 1, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("Detailed", c.rootDirOnServer + "/ola/search/detailed/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Detailed", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 5, 9, 0, 0, 0).getTime());
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            c.lidarSearchDataSourceMap.put("OrbB", c.rootDirOnServer + "/ola/search/orbB/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("OrbB", startStop);

            startStop = new ArrayList<Date>();
            startStop.add(new GregorianCalendar(2019, 8, 10, 0, 0, 0).getTime());
            startStop.add(c.lidarSearchDefaultEndDate);
            c.lidarSearchDataSourceMap.put("Recon", c.rootDirOnServer + "/ola/search/recon/dataSource.lidar");
            c.lidarSearchDataSourceTimeMap.put("Recon", startStop);

            /*
             *
             */

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

            c.dtmBrowseDataSourceMap.put("Default", "bennu/altwg-spc-v20190414/dtm/browse/fileList.txt");

            if ((SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_DEPLOY) ||
                    (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_STAGE) || (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.OSIRIS_REX_MIRROR_DEPLOY))
            {
//                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.BETULIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/BETULIA/betulia.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GEOGRAPHOS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.BACCHUS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.RASHALOM;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/RASHALOM/rashalom.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TOUTATIS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Hudson et al. (2004)";
        c.rootDirOnServer = "/toutatis/hudson/";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of("Low (12796 plates)", "High (39996 plates)"), ImmutableList.of(12796, 39996));
        configArray.add(c);

//       c = new SmallBodyViewConfig();
//       c.body = ShapeModelBody.TOUTATIS;
//       c.type = BodyType.ASTEROID;
//       c.population = ShapeModelPopulation.NEO;
//       c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
//       c.author = ShapeModelType.HUDSON;
//       c.modelLabel = "Hudson et al. (2004)";
//       c.rootDirOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
//       c.version = "High resolution";
//       c.setResolution(ImmutableList.of(39996));
//       configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MITHRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/MITHRA/Mithra.v1.PA.prograde.mod.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.NEREUS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/NEREUS/Nereus_alt1.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CASTALIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GOLEVKA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HW1;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/HW1/1996hw1.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.SK;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/SK/sk.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._1950DAPROGRADE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DAPROGRADE/1950DA_ProgradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._1950DARETROGRADE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DARETROGRADE/1950DA_RetrogradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.WT24;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/WT24/wt24.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._52760_1998_ML14;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/52760/52760.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.YORP;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/YORP/yorp.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KW4A;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4A/kw4a.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KW4B;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4B/kw4b.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CCALPHA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/CCALPHA/1994CC_nominal.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CE26;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/CE26/ce26.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EV5;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/EV5/2008ev5.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KY26;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.rootDirOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.CERES;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "SPC";
            c.rootDirOnServer = "/GASKELL/CERES";
            c.hasMapmaker = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/CERES/FC", "Ceres", "/GASKELL/CERES/FC/gallery"),
                            ImageType.FCCERES_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.FC
                    )
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, GregorianCalendar.APRIL, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, GregorianCalendar.JULY, 1, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "Filter 1 (735 nm)",
                    "Filter 2 (548 nm)",
                    "Filter 3 (749 nm)",
                    "Filter 4 (918 nm)",
                    "Filter 5 (978 nm)",
                    "Filter 6 (829 nm)",
                    "Filter 7 (650 nm)",
                    "Filter 8 (428 nm)"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PALLAS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/PALLAS/pallas.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013)";
        c.rootDirOnServer = "/GASKELL/VESTA";
        c.hasMapmaker = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/VESTA/FC", "FC", "/GASKELL/VESTA/FC/gallery"),
                        ImageType.FC_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                        Instrument.FC
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "FC1", "FC2" };
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/VESTA_OLD";
        c.shapeModelFileNames = new String[] { "/VESTA_OLD/VESTA.vtk.gz" };
        c.setResolution(ImmutableList.of(49152));
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.LUTETIA;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "SPC";
            c.rootDirOnServer = "/GASKELL/LUTETIA";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery("/GASKELL/LUTETIA/IMAGING", "/GASKELL/LUTETIA/IMAGING/gallery"),
                            ImageType.OSIRIS_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.OSIRIS
                            )
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.LUTETIA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham et al. (2013)";
        c.rootDirOnServer = "/JORDA/LUTETIA";
        c.setResolution(ImmutableList.of(
                "2962 plates ", "5824 plates ", "11954 plates ", "24526 plates ",
                "47784 plates ", "98280 plates ", "189724 plates ", "244128 plates ",
                "382620 plates ", "784510 plates ", "1586194 plates ", "3145728 plates"
            ), ImmutableList.of(
                2962, 5824, 11954, 24526, 47784, 98280, 189724,
                244128, 382620, 784510, 1586194, 3145728
            ));
        c.shapeModelFileNames = prepend(c.rootDirOnServer,
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
            );
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DAPHNE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/DAPHNE/daphne.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HERMIONE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.CARRY;
        c.rootDirOnServer = "/CARRY/HERMIONE/hermione.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KLEOPATRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelType.HUDSON;
        c.modelLabel = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(32040));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/IDA/SSI", "/THOMAS/IDA/SSI/images/gallery"),
                        ImageType.SSI_IDA_IMAGE,
                        new ImageSource[]{ImageSource.CORRECTED},
                        Instrument.SSI
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1993, 7, 28, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1993, 7, 29, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        // This model was delivered on 2018-03-08 to replace the previous model of unknown specific origin.
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.IDA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "ida/stooke2016";
        c.shapeModelFileExtension = ".obj";
        c.setResolution(ImmutableList.of(5040));
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2600.;
        c.rotationRate = 0.0003766655;
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(14160));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/MATHILDE/MSI", "/THOMAS/MATHILDE/MSI/images/gallery"),
                        ImageType.MSI_MATHILDE_IMAGE,
                        new ImageSource[]{ImageSource.CORRECTED},
                        Instrument.MSI
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1997, 5, 27, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1997, 5, 28, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        // This new model was delivered on 2018-03-08.
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MATHILDE;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "mathilde/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 1300.;
        c.rotationRate = 0.0000041780;
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(32040));

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/GASPRA/SSI", "/THOMAS/GASPRA/SSI/images/gallery"),
                        ImageType.SSI_GASPRA_IMAGE,
                        new ImageSource[]{ImageSource.CORRECTED},
                        Instrument.SSI
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        // This model was delivered on 2018-03-08 to replace the previous model of unknown specific origin.
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "gaspra/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 2700.;
        c.rotationRate = 0.0002478;
        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.STEINS;
        c.type = BodyType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.JORDA;
        c.modelLabel = "Farnham and Jorda (2013)";
        c.rootDirOnServer = "/JORDA/STEINS/steins_cart.plt.gz";
        c.setResolution(ImmutableList.of(20480));
        configArray.add(c);

        // This model was delivered on 2018-03-08 to replace the existing model of unknown specific origin.
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HALLEY;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/halley/stooke2016";
        c.shapeModelFileExtension = ".obj";
        // Provided with the delivery in the file aamanifest.txt.
        c.density = 600;
        c.rotationRate = 0.0000323209;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DEIMOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/DEIMOS/DEIMOS.vtk.gz";
//        c.hasStateHistory = true;
//        c.timeHistoryFile = "/DEIMOS/history/TimeHistory.bth";

        c.hasImageMap = true;
        c.setResolution(ImmutableList.of(49152));
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.DEIMOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.BLENDER;
            c.modelLabel = "OLD Ernst et al. (in progress)";
            c.rootDirOnServer = "/THOMAS/DEIMOSEXPERIMENTAL/DEIMOS.vtk.gz";
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/THOMAS/DEIMOSEXPERIMENTAL/IMAGING", "DEIMOS", "/THOMAS/DEIMOSEXPERIMENTAL/IMAGING/viking/gallery"),
                            ImageType.DEIMOS_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED},
                            Instrument.IMAGING_DATA
                            )
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "VIS, Blue",
                    "VIS, Minus Blue",
                    "VIS, Violet",
                    "VIS, Clear",
                    "VIS, Green",
                    "VIS, Red",
            };

            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
            c.imageSearchDefaultMaxResolution = 800.0;
//            configArray.add(c);

        }

        // Latest Gaskell Deimos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.DEIMOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.EXPERIMENTAL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/deimos/ernst2018";
            c.shapeModelFileExtension = ".obj";
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
//                            new GenericPhpQuery("/deimos/ernst2018/imaging", "DEIMOS_ERNST_2018", "/deimos/ernst2018/imaging/gallery"),
                            new FixedListQuery("/deimos/ernst2018/imaging", "/deimos/ernst2018/imaging/gallery"),
                            ImageType.DEIMOS_IMAGE,
                            new ImageSource[]{ ImageSource.GASKELL },
                            Instrument.IMAGING_DATA,
                            0.,
                            "Y" // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                            )
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
//            c.imageSearchFilterNames = new String[]{
//                    "VIS, Blue",
//                    "VIS, Minus Blue",
//                    "VIS, Violet",
//                    "VIS, Clear",
//                    "VIS, Green",
//                    "VIS, Red",
//            };
//
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
            c.imageSearchDefaultMaxResolution = 800.0;
            configArray.add(c);

        }

        // Gaskell Phobos
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2011)";
        c.density = 1.876;
        c.rotationRate = 0.00022803304110600688;
        c.rootDirOnServer = "/GASKELL/PHOBOS";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/PHOBOS/IMAGING", "PHOBOS", "/GASKELL/PHOBOS/IMAGING/images/gallery"),
                        ImageType.PHOBOS_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                        Instrument.IMAGING_DATA
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {
                "VSK, Channel 1",
                "VSK, Channel 2",
                "VSK, Channel 3",
                "VIS, Blue",
                "VIS, Minus Blue",
                "VIS, Violet",
                "VIS, Clear",
                "VIS, Green",
                "VIS, Red",
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[] { "Phobos 2", "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC" };
        c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        c.imageSearchDefaultMaxResolution = 300.0;
        c.hasLidarData = true;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        c.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
        c.lidarBrowseIsLidarInSphericalCoordinates = true;
        c.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
        c.lidarBrowseIsTimeInET = true;
        c.lidarBrowseTimeIndex = 5;
        c.lidarBrowseNoiseIndex = -1;
        c.lidarBrowseIsRangeExplicitInData = true;
        c.lidarBrowseRangeIndex = 3;
        c.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
        c.lidarBrowseNumberHeaderLines = 1;
        c.lidarBrowseIsInMeters = true;
        c.lidarOffsetScale = 0.025;
        c.lidarInstrumentName = Instrument.MOLA;

        // MOLA search is disabled for now. See LidarPanel class.
        c.hasHypertreeBasedLidarSearch = true;
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        configArray.add(c);

        // Thomas Phobos
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";
        c.setResolution(ImmutableList.of(32040));

        c.lidarSearchDataSourceMap = Maps.newHashMap(); // this must be instantiated, but can be empty

        configArray.add(c);

        // New Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PHOBOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.BLENDER;
            c.modelLabel = "OLD Ernst et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"),
                            ImageType.PHOBOS_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.IMAGING_DATA
                            )
            };

            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    "VSK, Channel 1",
                    "VSK, Channel 2",
                    "VSK, Channel 3",
                    "VIS, Blue",
                    "VIS, Minus Blue",
                    "VIS, Violet",
                    "VIS, Clear",
                    "VIS, Green",
                    "VIS, Red",
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {
                    "Phobos 2",
                    "Viking Orbiter 1-A",
                    "Viking Orbiter 1-B",
                    "Viking Orbiter 2-A",
                    "Viking Orbiter 2-B",
                    "MEX HRSC",
                    "MRO HiRISE",
                    "MGS MOC"
            };
            c.hasHierarchicalImageSearch = true;
            c.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.lidarSearchDataSourceMap = Maps.newHashMap();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

//            configArray.add(c);
        }

        // Latest Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PHOBOS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.EXPERIMENTAL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/phobos/ernst2018";
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
//                            new GenericPhpQuery("/phobos/ernst2018/imaging", "PHOBOS_ERNST_2018", "/phobos/ernst2018/imaging/gallery"),
                            new FixedListQuery("/phobos/ernst2018/imaging", "/phobos/ernst2018/imaging/gallery"),
                            ImageType.PHOBOS_IMAGE,
                            new ImageSource[]{ ImageSource.GASKELL },
                            Instrument.IMAGING_DATA,
                            0.,
                            "Y" // Note: this means "flip along Y axis". Don't know why, but this flip is needed as of this delivery.
                            )
            };

            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 8, 1, 0, 0, 0).getTime();
//            c.imageSearchFilterNames = new String[]{
//                    "VSK, Channel 1",
//                    "VSK, Channel 2",
//                    "VSK, Channel 3",
//                    "VIS, Blue",
//                    "VIS, Minus Blue",
//                    "VIS, Violet",
//                    "VIS, Clear",
//                    "VIS, Green",
//                    "VIS, Red",
//            };
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                    "Phobos 2",
//                    "Viking Orbiter 1-A",
//                    "Viking Orbiter 1-B",
//                    "Viking Orbiter 2-A",
//                    "Viking Orbiter 2-B",
//                    "MEX HRSC",
//                    "MRO HiRISE",
//                    "MGS MOC"
//            };
//            c.hasHierarchicalImageSearch = true;
//            c.hierarchicalImageSearchSpecification = new PhobosExperimentalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
            c.lidarBrowseXYZIndices = new int[] { 0, 1, 2 };
            c.lidarBrowseIsLidarInSphericalCoordinates = true;
            c.lidarBrowseSpacecraftIndices = new int[] { -1, -1, -1 };
            c.lidarBrowseIsTimeInET = true;
            c.lidarBrowseTimeIndex = 5;
            c.lidarBrowseNoiseIndex = -1;
            c.lidarBrowseIsRangeExplicitInData = true;
            c.lidarBrowseRangeIndex = 3;
            c.lidarBrowseFileListResourcePath = "/GASKELL/PHOBOS/MOLA/allMolaFiles.txt";
            c.lidarBrowseNumberHeaderLines = 1;
            c.lidarBrowseIsInMeters = true;
            c.lidarOffsetScale = 0.025;
            c.lidarInstrumentName = Instrument.MOLA;

            // MOLA search is disabled for now. See LidarPanel class.
            c.hasHypertreeBasedLidarSearch = true;
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.JUPITER;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/JUPITER/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.hasImageMap = false;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(SpectralMode.MONO, new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery"), ImageType.LORRI_IMAGE, new ImageSource[] { ImageSource.SPICE }, Instrument.LORRI),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/JUPITER/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            ),

                    new ImagingInstrument(
                            SpectralMode.HYPER,
                            new FixedListQuery("/NEWHORIZONS/JUPITER/LEISA"),
                            ImageType.LEISA_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LEISA
                            )
                    };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);
            SmallBodyViewConfig callisto = new SmallBodyViewConfig();
            callisto = c.clone();

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.AMALTHEA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.STOOKE;
            // 2017-12-20: this name will be correct when "the new model" has been brought in.
            // c.modelLabel = "Stooke (2016)";
            c.rootDirOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);

            c = callisto.clone();
            c.body = ShapeModelBody.CALLISTO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/CALLISTO/shape_res0.vtk.gz";
            c.hasImageMap = true;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO", "/NEWHORIZONS/CALLISTO/IMAGING/images/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            )
            };

            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.EUROPA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/EUROPA/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.hasFlybyData = true;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/EUROPA/IMAGING", "EUROPA", "/NEWHORIZONS/EUROPA/IMAGING/images/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/EUROPA/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            )
                    };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.GANYMEDE;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/GANYMEDE/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/GANYMEDE/IMAGING", "GANYMEDE", "/NEWHORIZONS/GANYMEDE/IMAGING/images/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/GANYMEDE/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            )
                    };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.IO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/IO/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/IO/IMAGING", "IO", "/NEWHORIZONS/IO/IMAGING/images/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/IO/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            )
                    };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            // configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DIONE;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013a)";
        c.rootDirOnServer = "/GASKELL/DIONE";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/DIONE/IMAGING", "/GASKELL/DIONE/IMAGING/gallery"),
                        ImageType.SATURN_MOON_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL},
                        Instrument.IMAGING_DATA
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SmallBodyViewConfig();
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
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.HYPERION;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/HYPERION";
            c.hasColoringData = false;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HYPERION;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        c.hasColoringData = false;
        c.setResolution(ImmutableList.of(5040));
        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SmallBodyViewConfig();
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
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MIMAS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013b)";
        c.rootDirOnServer = "/GASKELL/MIMAS";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/MIMAS/IMAGING", "/GASKELL/MIMAS/IMAGING/gallery"),
                        ImageType.SATURN_MOON_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL},
                        Instrument.IMAGING_DATA
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SmallBodyViewConfig();
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
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOEBE;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013c)";
        c.rootDirOnServer = "/GASKELL/PHOEBE";


        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/PHOEBE/IMAGING", "/GASKELL/PHOEBE/IMAGING/gallery"),
                        ImageType.SATURN_MOON_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL},
                        Instrument.IMAGING_DATA
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.LARISSA;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        // 2017-12-20: this name will be correct when "the new model" has been brought in.
        // c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PROTEUS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.STOOKE;
        // 2017-12-20: this name will be correct when "the new model" has been brought in.
        // c.modelLabel = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        // configArray.add(c);

        // Model stooke2016 delivered 2018-03-06.
        c = new SmallBodyViewConfig();
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
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RHEA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/GASKELL/RHEA";
            c.hasColoringData = false;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TETHYS;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Gaskell (2013d)";
        c.rootDirOnServer = "/GASKELL/TETHYS";
        c.hasColoringData = false;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.TEMPEL_1;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/TEMPEL1";
            c.shapeModelFileNames = prepend(c.rootDirOnServer, "ver64q.vtk.gz");
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            configArray.add(c);
        }

        // An update was delivered to this model on 2018-02-22.
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TEMPEL_1;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Farnham and Thomas (2013)";
        c.rootDirOnServer = "/tempel1/farnham";
        c.shapeModelFileExtension = ".obj";
        // Number of plates found by inspection of files.
        c.setResolution(ImmutableList.of("32040 plates"), ImmutableList.of(32040));
        // Density and rotation rate were provided with delivery manifest.
        c.density = 470.0;
        c.rotationRate = 4.28434129815435E-5;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.WILD_2;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.DUXBURY;
        c.modelLabel = "Farnham et al. (2005)";
        c.rootDirOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
        c.setResolution(ImmutableList.of(17518));
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "SHAP5 V0.3";
            c.rootDirOnServer = "/GASKELL/67P";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/67P/IMAGING", "67P", "/GASKELL/67P/IMAGING/images/gallery"),
                            ImageType.OSIRIS_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.OSIRIS
                            )
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 7, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2014, 11, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name, begins with a star, it is not selected by default
                    "*Filter 1,2",
                    "*Filter 1,6",
                    "*Filter 1,8",
                    "Filter 2,2",
                    "*Filter 2,3",
                    "*Filter 2,4",
                    "*Filter 2,7",
                    "*Filter 2,8",
                    "*Filter 4,1",
                    "*Filter 5,1",
                    "*Filter 5,4",
                    "*Filter 6,1"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);

            c = c.clone();
            c.author = ShapeModelType.DLR;
            c.rootDirOnServer = "/DLR/67P";
            c.shapeModelFileNames = prepend(c.rootDirOnServer,
                    "cg-dlr_spg-shap4s-v0.9_64m.ply.gz", "cg-dlr_spg-shap4s-v0.9_32m.ply.gz", "cg-dlr_spg-shap4s-v0.9_16m.ply.gz", "cg-dlr_spg-shap4s-v0.9_8m.ply.gz", "cg-dlr_spg-shap4s-v0.9_4m.ply.gz", "cg-dlr_spg-shap4s-v0.9.ply.gz");

            c.version = "SHAP4S";
            c.imagingInstruments[0].searchQuery = new GenericPhpQuery("/DLR/67P/IMAGING", "67P_DLR", "/DLR/67P/IMAGING/images/gallery");
            c.setResolution(ImmutableList.of(
                    "17442 plates ", "72770 plates ", "298442 plates ", "1214922 plates ",
                    "4895631 plates ", "16745283 plates "
                ), ImmutableList.of(
                    17442, 72770, 298442, 1214922, 4895631, 16745283
                ));
            c.hasColoringData = false;
            configArray.add(c);

            // 67P_V2
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "V2";
            c.rootDirOnServer = "/GASKELL/67P_V2";

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/67P_V2/IMAGING", "67P_V2", "/GASKELL/67P_V3/IMAGING/gallery"), // V2 has no gallery but images are in V3 gallery
                            //new FixedListQuery("/GASKELL/67P_V2/IMAGING"),
                            ImageType.OSIRIS_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.OSIRIS
                            )
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name, begins with a star, it is not selected by default
                    "*Filter 1,2",
                    "*Filter 1,6",
                    "*Filter 1,8",
                    "Filter 2,2",
                    "*Filter 2,3",
                    "*Filter 2,4",
                    "*Filter 2,7",
                    "*Filter 2,8",
                    "*Filter 4,1",
                    "*Filter 5,1",
                    "*Filter 5,4",
                    "*Filter 6,1",
                    "*Filter 1,3",
                    "*Filter 1,5",
                    "*Filter 1,7",
                    "*Filter 3,1",
                    "*Filter 7,1",
                    "*Filter 8,2",
                    "*Filter 8,4",
                    "*Filter 8,7",
                    "*Filter 8,8"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);

            // 67P_V3
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = BodyType.COMETS;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/67P_V3";

            c.hasCustomBodyCubeSize = true;
            c.customBodyCubeSize = 0.10; // km

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/67P_V3/IMAGING", "67P_V3", "/GASKELL/67P_V3/IMAGING/gallery"),
                            //new FixedListQuery("/GASKELL/67P_V3/IMAGING"),
                            ImageType.OSIRIS_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.OSIRIS
                            )
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 0, 31, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {
                    // If a name, begins with a star, it is not selected by default
                    "*Filter 1,2",
                    "*Filter 1,6",
                    "*Filter 1,8",
                    "Filter 2,2",
                    "*Filter 2,3",
                    "*Filter 2,4",
                    "*Filter 2,7",
                    "*Filter 2,8",
                    "*Filter 4,1",
                    "*Filter 5,1",
                    "*Filter 5,4",
                    "*Filter 6,1",
                    "*Filter 1,3",
                    "*Filter 1,5",
                    "*Filter 1,7",
                    "*Filter 3,1",
                    "*Filter 7,1",
                    "*Filter 8,2",
                    "*Filter 8,4",
                    "*Filter 8,7",
                    "*Filter 8,8",
                    "*Filter 2,1"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[] { "NAC", "*WAC" };
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HARTLEY;
        c.type = BodyType.COMETS;
        c.population = ShapeModelPopulation.NA;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.THOMAS;
        c.modelLabel = "Farnham and Thomas (2013)";
        c.rootDirOnServer = "/THOMAS/HARTLEY/hartley2_2012_cart.plt.gz";
        c.setResolution(ImmutableList.of(32040));
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PLUTO;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Nimmo et al. (2017)";
//            c.pathOnServer = "/NEWHORIZONS/PLUTO/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/PLUTO/shape_res0.obj.gz";
            c.hasColoringData = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
//                            new GenericPhpQuery("/NEWHORIZONS/PLUTO/IMAGING", "PLUTO"),
                            new FixedListQuery("/NEWHORIZONS/PLUTO/IMAGING", true),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED, ImageSource.CORRECTED_SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/PLUTO/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            ),

                    new ImagingInstrument(
                            SpectralMode.HYPER,
                            new FixedListQuery("/NEWHORIZONS/PLUTO/LEISA"),
                            ImageType.LEISA_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LEISA
                            )
            };

            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.CHARON;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Nimmo et al. (2017)";
//           c.pathOnServer = "/NEWHORIZONS/CHARON/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/CHARON/shape_res0.obj.gz";
            c.hasColoringData = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery("/NEWHORIZONS/CHARON/IMAGING", true),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/CHARON/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            ),

                    new ImagingInstrument(
                            SpectralMode.HYPER,
                            new FixedListQuery("/NEWHORIZONS/CHARON/LEISA"),
                            ImageType.LEISA_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LEISA
                            )
            };

            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);

            SmallBodyViewConfig hydra = new SmallBodyViewConfig();

            c = c.clone();
            c.body = ShapeModelBody.HYDRA;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Weaver et al. (2016)";
//            c.pathOnServer = "/NEWHORIZONS/HYDRA/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/HYDRA/shape_res0.obj.gz";
            c.hasColoringData = false;
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery("/NEWHORIZONS/HYDRA/IMAGING", true),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/HYDRA/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            ),

                    new ImagingInstrument(
                            SpectralMode.HYPER,
                            new FixedListQuery("/NEWHORIZONS/HYDRA/LEISA"),
                            ImageType.LEISA_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LEISA
                            )
            };
            hydra = c.clone();
            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.KERBEROS;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Weaver et al. (2016)";
            c.rootDirOnServer = "/NEWHORIZONS/KERBEROS/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);

            c = hydra;
            c.body = ShapeModelBody.NIX;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Weaver et al. (2016)";
//            c.pathOnServer = "/NEWHORIZONS/NIX/shape_res0.vtk.gz";
            c.rootDirOnServer = "/NEWHORIZONS/NIX/shape_res0.obj.gz";
            c.hasColoringData = false;
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery("/NEWHORIZONS/NIX/IMAGING", true),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED_SPICE},
                            Instrument.LORRI
                            ),

                    new ImagingInstrument(
                            SpectralMode.MULTI,
                            new FixedListQuery("/NEWHORIZONS/NIX/MVIC"),
                            ImageType.MVIC_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.MVIC
                            ),

                    new ImagingInstrument(
                            SpectralMode.HYPER,
                            new FixedListQuery("/NEWHORIZONS/NIX/LEISA"),
                            ImageType.LEISA_JUPITER_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LEISA
                            )
            };
            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.STYX;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = ShapeModelDataUsed.TRIAXIAL;
            c.author = null;
            c.modelLabel = "Weaver et al. (2016)";
            c.rootDirOnServer = "/NEWHORIZONS/STYX/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.setResolution(ImmutableList.of(128880));
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TELESTO;
        c.type = BodyType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelType.GASKELL;
        c.modelLabel = "Ernst et al. (in progress)";
        c.rootDirOnServer = "/GASKELL/TELESTO";

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/TELESTO/IMAGING", "/GASKELL/TELESTO/IMAGING/gallery"),
                        ImageType.SATURN_MOON_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL},
                        Instrument.IMAGING_DATA
                        )
        };

        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[] {};
        c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.hasColoringData = false;
        configArray.add(c);

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
            BasicImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.SAMCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                samCam = BasicImagingInstrument.of(builder.build());
            }

            c = new SmallBodyViewConfig();
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
                    // new Vis(ShapeModelBody.PHOBOS)
                    mapCam,
                    polyCam,
                    samCam,
// TODO when samCam is handled for sbmt1dev (see above), uncomment the next line to add it to the panel.
//                        samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {

                    new OTES(),
                    new OVIRS()
            };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
// TODO make hierarchical search work sbmt1dev-style.
//            c.imageSearchFilterNames = new String[]{
//                    EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
//            };
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                    EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
//            };
//            c.hasHierarchicalImageSearch = true;
//            c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
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

            // Set up shape model -- one will suffice. Note the "orex" here must be kept
            // exactly as it is; that is what the directory is named in the data area.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("orex", ShapeModelDataUsed.WGS84).build();
            BasicImagingInstrument mapCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.MAPCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.MAPCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                mapCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument polyCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.POLYCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.POLYCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                polyCam = BasicImagingInstrument.of(builder.build());
            }
            BasicImagingInstrument samCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.SAMCAM,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.SAMCAM_EARTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                samCam = BasicImagingInstrument.of(builder.build());
            }

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.OREX;
            c.rootDirOnServer = "/earth/orex";
//            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasColoringData = false;
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    // new Vis(ShapeModelBody.PHOBOS)
                    mapCam,
                    polyCam,
                    samCam,
                    // TODO when samCam is handled for sbmt1dev (see above), uncomment the next line
                    // to add it to the panel.
                    // samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
            };

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new OTES(),
                    new OVIRS()
            };

            c.hasStateHistory = true;
            c.timeHistoryFile = "/earth/osirisrex/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
            // TODO make hierarchical search work sbmt1dev-style.
            // c.imageSearchFilterNames = new String[]{
            // EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
            // };
            // c.imageSearchUserDefinedCheckBoxesNames = new String[]{
            // EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
            // };
//            c.hasHierarchicalImageSearch = true;
            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = true;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("OTES_L2", "/earth/osirisrex/otes/l2/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OTES_L3", "/earth/osirisrex/otes/l3/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_IF", "/earth/osirisrex/ovirs/l3/if/hypertree/dataSource.spectra");
            c.spectraSearchDataSourceMap.put("OVIRS_REF", "/earth/osirisrex/ovirs/l3/reff/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = "/earth/osirisrex/spectraMetadata.json";

            OREXSpectrumInstrumentMetadataIO specIO = new OREXSpectrumInstrumentMetadataIO("OREX");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, Hayabusa2 WGS84 version
            // :

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();


            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("hayabusa2", ShapeModelDataUsed.WGS84).build();
//            BasicImagingInstrument mapCam;
//            {
//                // Set up images.
//                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.MAPCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
//                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
//                        Instrument.MAPCAM,
//                        SpectralMode.MONO,
//                        queryBase,
//                        new ImageSource[] { ImageSource.SPICE },
//                        fileLocator,
//                        ImageType.MAPCAM_IMAGE);
//
//                // Put it all together in a session.
//                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
//                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
//                mapCam = BasicImagingInstrument.of(builder.build());
//            }
//            BasicImagingInstrument polyCam;
//            {
//                // Set up images.
//                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.POLYCAM, ".fit", ".INFO", null, ".jpeg");
//                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
//                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
//                        Instrument.POLYCAM,
//                        SpectralMode.MONO,
//                        queryBase,
//                        new ImageSource[] { ImageSource.SPICE },
//                        fileLocator,
//                        ImageType.POLYCAM_IMAGE);
//
//                // Put it all together in a session.
//                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
//                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
//                polyCam = BasicImagingInstrument.of(builder.build());
//            }
    //TODO handle SAMCAM sbmt1dev-style. Add and handle the ImageType for it, then uncomment this block and the line below.
    //        BasicImagingInstrument samCam;
    //        {
    //            // Set up images.
    //            SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.SAMCAM, ".fits", ".INFO", null, ".jpeg");
    //            QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
    //            Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
    //                    Instrument.SAMCAM,
    //                    SpectralMode.MONO,
    //                    queryBase,
    //                    new ImageSource[] { ImageSource.SPICE },
    //                    fileLocator,
    //                    ImageType.SAMCAM_IMAGE);
    //
    //            // Put it all together in a session.
    //            Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
    //            builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
    //            samCam = BasicImagingInstrument.of(builder.build());
    //        }

          BasicImagingInstrument tir;
          {
              // Set up images.
              SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
              QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
              Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                      Instrument.TIR,
                      SpectralMode.MONO,
                      queryBase,
                      new ImageSource[] { ImageSource.SPICE },
                      fileLocator,
                      ImageType.TIR_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                tir = BasicImagingInstrument.of(builder.build());
            }

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.EARTH;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelType.JAXA_SFM_v20180627;
            c.modelLabel = "Haybusa2-testing";
            c.rootDirOnServer = "/earth/hayabusa2";
//            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.hasImageMap = true;
            c.hasColoringData = false;

            c.imagingInstruments = new ImagingInstrument[] {
                    //                       // new Vis(ShapeModelBody.PHOBOS)
                    //                        mapCam,
                    //                        polyCam,
                    // TODO when samCam is handled for sbmt1dev (see above), uncomment the next line
                    // to add it to the panel.
                    // samCam
                    /*
                     * new ImagingInstrument( SpectralMode.MONO, new
                     * GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP",
                     * "/GASKELL/PHOBOS/IMAGING/images/gallery"), ImageType.PHOBOS_IMAGE, new
                     * ImageSource[]{ImageSource.GASKELL}, Instrument.IMAGING_DATA )
                     */
                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

//                c.hasSpectralData=true;
//                c.spectralInstruments=new SpectralInstrument[] {
//                        new OTES()
//                };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2015, 11, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2015, 11, 31, 0, 0, 0).getTime();
            // TODO make hierarchical search work sbmt1dev-style.
//            c.imageSearchFilterNames = new String[]{
//                    EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
//            };
//            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
//                    EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
//            };
//            c.hasHierarchicalImageSearch = true;
//            c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            configArray.add(c);

            c.hasLidarData = false;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
//          c.lidarSearchDataSourceMap.put("Hayabusa2","/ryugu/shared/lidar/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/earth/hayabusa2/laser/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/earth/hayabusa2/laser/browse/fileList.txt";

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
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Truth", ShapeModelDataUsed.IMAGE_BASED).build();

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.TRUTH;
            c.modelLabel = "H2 Simulated Truth";
            c.rootDirOnServer = "/ryugu/truth";
            c.shapeModelFileExtension = ".obj";

            c.setResolution(ImmutableList.of("Low (54504 plates)", "High (5450420 plates)"), ImmutableList.of(54504, 5450420));

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/truth/history/timeHistory.bth";

            // This version would enable image search but this seems to hang, possibly
            // because of the very high resolution of the model.
            // Re-enable this if/when that issue is addressed.
//            QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/imaging", "ryugu", "/ryugu/truth/imaging/images/gallery");
//            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.IMAGING_DATA, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_TRUTH_IMAGE);
            SBMTFileLocator fileLocatorTir = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
//            QueryBase queryBaseTir = new FixedListQuery(fileLocatorTir.get(SBMTFileLocator.TOP_PATH).getLocation("") + "/simulated", fileLocatorTir.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            QueryBase queryBaseTir = new GenericPhpQuery("/ryugu/truth/tir", "ryugu_nasa002_tir", "/ryugu/truth/tir/gallery");
            ImagingInstrument tir = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, queryBaseTir, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            QueryBase queryBase = new GenericPhpQuery("/ryugu/truth/onc", "ryugu_sim", "/ryugu/truth/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_TRUTH_IMAGE);

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
//                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Gaskell", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/gaskell/onc", "ryugu_sim", "/ryugu/gaskell/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL }, ImageType.ONC_IMAGE);

            SBMTFileLocator fileLocatorTir = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.TIR, ".fit", ".INFO", null, ".jpeg");
//            QueryBase queryBaseTir = new FixedListQuery(fileLocatorTir.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocatorTir.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
            QueryBase queryBaseTir = new GenericPhpQuery("/ryugu/gaskell/tir", "ryugu_nasa002_tir", "/ryugu/gaskell/tir/gallery");
            ImagingInstrument tir = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, queryBaseTir, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.SIMULATED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "H2 Simulated SPC";
            c.rootDirOnServer = "/ryugu/gaskell";
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/gaskell/history/timeHistory.bth"; // TODO move this to shared/timeHistory.bth

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
//                    tir
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/laser/search/test_hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180627", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180627/onc", "jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180627/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180627/tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180627/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180627;
            c.modelLabel = "JAXA-SFM-v20180627";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180627";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180627/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180714", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180714/onc", "ryugu_jaxasfmv20180627", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180714/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180627/tir", "/ryugu/jaxa-sfm-v20180627/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180714/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180714/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180714;
            c.modelLabel = "JAXA-SFM-v20180714";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180714";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180714/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180725_2", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180725-2/onc", "ryugu_jaxasfmv201807252", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180725-2/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180725-2/tir", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180725-2/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180725-2/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180725_2;
            c.modelLabel = "JAXA-SFM-v20180725_2";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180725-2";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180725-2/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SFM-v20180804", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180804/onc", "ryugu_jaxasfmv20180804", "ryugu_nasa002", "/ryugu/jaxa-sfm-v20180804/onc/gallery");
            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180804/onc",
            // "/ryugu/jaxa-sfm-v20180804/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-sfm-v20180804/tir", "/ryugu/jaxa-sfm-v20180804/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-sfm-v20180804/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-sfm-v20180804/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SFM_v20180804;
            c.modelLabel = "JAXA-SFM-v20180804";
            c.rootDirOnServer = "/ryugu/jaxa-sfm-v20180804";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-sfm-v20180804/history/timeHistory.bth";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(ShapeModelBody.RYUGU.name(), BodyType.ASTEROID.name(), ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180705", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180705/onc", "ryugu_jaxaspcv20180705", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180705/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180705/tir", "/ryugu/jaxa-spc-v20180705/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180705/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180705/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180705;
            c.modelLabel = "JAXA-SPC-v20180705";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180705";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180705/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180717", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180717/onc", "ryugu_jaxaspcv20180717", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180717/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180717/tir", "/ryugu/jaxa-spc-v20180717/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180717/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180717/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180717;
            c.modelLabel = "JAXA-SPC-v20180717";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180717";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180717/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180719_2", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180719-2/onc", "ryugu_jaxaspcv201807192", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180719-2/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180719-2/tir", "/ryugu/jaxa-spc-v20180719-2/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180719-2/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180719-2/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180719_2;
            c.modelLabel = "JAXA-SPC-v20180719_2";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180719-2";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180719-2/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180731", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180731/onc",
            // "/ryugu/jaxa-spc-v20180731/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180731/onc", "ryugu_jaxaspcv20180731", "ryugu_nasa002", "/ryugu/jaxa-spc-v20180731/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180731/tir", "/ryugu/jaxa-spc-v20180731/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180731/tir", "", "ryugu_nasa002_tir", "/ryugu/jaxa-spc-v20180731/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180731;
            c.modelLabel = "JAXA-SPC-v20180731";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180731";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180731/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                // ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180810", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180810/onc",
            // "/ryugu/jaxa-spc-v20180810/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180810/onc", "ryugu_jaxaspcv20180810", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180810/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180810/tir", "/ryugu/jaxa-spc-v20180810/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180810/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180810/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180810;
            c.modelLabel = "JAXA-SPC-v20180810";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180810";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180810/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180816", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180816/onc",
            // "/ryugu/jaxa-spc-v20180816/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180816/onc", "ryugu_jaxaspcv20180816", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180816/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180816/tir", "/ryugu/jaxa-spc-v20180816/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180816/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180816/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180816;
            c.modelLabel = "JAXA-SPC-v20180816";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180816";
            c.setResolution(ImmutableList.of("Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]), ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180816/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20180829", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180829/onc",
            // "/ryugu/jaxa-spc-v20180829/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180829/onc", "ryugu_jaxaspcv20180829", "ryugu_nasa005", "/ryugu/jaxa-spc-v20180829/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20180829/tir", "/ryugu/jaxa-spc-v20180829/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20180829/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20180829/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20180829;
            c.modelLabel = "JAXA-SPC-v20180829";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20180829";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20180829/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
//                    ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("JAXA-SPC-v20181014", ShapeModelDataUsed.IMAGE_BASED).build();

            // QueryBase queryBase = new FixedListQuery("/ryugu/jaxa-spc-v20181014/onc",
            // "/ryugu/jaxa-spc-v20181014/onc/gallery");
            QueryBase queryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20181014/onc", "ryugu_jaxaspcv20181014", "ryugu_nasa005", "/ryugu/jaxa-spc-v20181014/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/jaxa-spc-v20181014/tir", "/ryugu/jaxa-spc-v20181014/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/jaxa-spc-v20181014/tir", "", "ryugu_nasa005_tir", "/ryugu/jaxa-spc-v20181014/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.JAXA_SPC_v20181014;
            c.modelLabel = "JAXA-SPC-v20181014";
            c.rootDirOnServer = "/ryugu/jaxa-spc-v20181014";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/jaxa-spc-v20181014/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasSpectralData = true;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
                    new NIRS3()
            };

            c.hasHierarchicalSpectraSearch = true;
            c.hasHypertreeBasedSpectraSearch = false;
            c.spectraSearchDataSourceMap = new LinkedHashMap<>();
            c.spectraSearchDataSourceMap.put("NIRS3", c.rootDirOnServer + "/nirs3/l2c/hypertree/dataSource.spectra");
            c.spectrumMetadataFile = c.rootDirOnServer + "/spectraMetadata.json";

            Hayabusa2SpectrumInstrumentMetadataIO specIO = new Hayabusa2SpectrumInstrumentMetadataIO("HAYABUSA2");
            specIO.setPathString(c.spectrumMetadataFile);
            c.hierarchicalSpectraSearchSpecification = specIO;

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            switch (SbmtMultiMissionTool.getMission())
            {
            case HAYABUSA2_DEV:
            case HAYABUSA2_DEPLOY:
            case HAYABUSA2_STAGE:
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            default:
                break;
            }

            configArray.add(c);

        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-001", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase queryBase = new GenericPhpQuery("/ryugu/nasa-001/onc", "ryugu_flight", "/ryugu/nasa-001/onc/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, queryBase, new ImageSource[] { ImageSource.GASKELL }, ImageType.ONC_IMAGE);

            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-001/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-001/tir/gallery");
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_001;
            c.modelLabel = "NASA-001";
            c.rootDirOnServer = "/ryugu/nasa-001";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            // c.hasStateHistory = true;
//            c.timeHistoryFile = "/ryugu/nasa-001/history/timeHistory.bth"; // TODO move this to shared/timeHistory.bth

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam // , tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/laser/search/test_hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-002", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-002/onc", "ryugu_nasa002", "ryugu_nasa002", "/ryugu/nasa-002/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-002/tir", "/ryugu/nasa-002/tir/gallery", false);            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-001/tir", "", "ryugu_nasa003_tir", "/ryugu/nasa-001/tir/gallery");
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-002/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-002/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_002;
            c.modelLabel = "NASA-002";
            c.rootDirOnServer = "/ryugu/nasa-002";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-002/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-003", ShapeModelDataUsed.IMAGE_BASED).build();

            // NOTE THE FOLLOWING LINE IS NOT A TYPO: THIRD ARGUMENT SHOULD BE ryugu_nasa002, not ryugu_nasa003.
            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-003/onc", "ryugu_nasa003", "ryugu_nasa002", "/ryugu/nasa-003/onc/gallery");
            // QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-003/onc",
            // "/ryugu/nasa-003/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-003/tir", "/ryugu/nasa-003/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-003/tir", "", "ryugu_nasa002_tir", "/ryugu/nasa-003/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_003;
            c.modelLabel = "NASA-003";
            c.rootDirOnServer = "/ryugu/nasa-003";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-003/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1500.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-004", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-004/onc", "ryugu_nasa004", "ryugu_nasa005", "/ryugu/nasa-004/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-004/onc", "/ryugu/nasa-004/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-004/tir", "/ryugu/nasa-004/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-004/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-004/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_004;
            c.modelLabel = "NASA-004";
            c.rootDirOnServer = "/ryugu/nasa-004";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-004/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-005", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-005/onc", "ryugu_nasa005", "ryugu_nasa005", "/ryugu/nasa-005/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-005/onc", "/ryugu/nasa-005/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-005/tir", "/ryugu/nasa-005/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-005/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-005/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_005;
            c.modelLabel = "NASA-005";
            c.rootDirOnServer = "/ryugu/nasa-005";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-005/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022871; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    BodyType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("NASA-006", ShapeModelDataUsed.IMAGE_BASED).build();

            QueryBase oncQueryBase = new GenericPhpQuery("/ryugu/nasa-006/onc", "ryugu_nasa006", "ryugu_nasa005", "/ryugu/nasa-006/onc/gallery");
            //QueryBase oncQueryBase = new FixedListQuery("/ryugu/nasa-006/onc", "/ryugu/nasa-006/onc/gallery");
//            QueryBase tirQueryBase = new FixedListQuery("/ryugu/nasa-006/tir", "/ryugu/nasa-006/tir/gallery", false);
            QueryBase tirQueryBase = new GenericPhpQuery("/ryugu/nasa-006/tir", "", "ryugu_nasa005_tir", "/ryugu/nasa-006/tir/gallery");
            ImagingInstrument oncCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.ONC, oncQueryBase, new ImageSource[] { ImageSource.GASKELL, ImageSource.SPICE }, ImageType.ONC_IMAGE);
            ImagingInstrument tirCam = setupImagingInstrument(bodyConfig, modelConfig, Instrument.TIR, tirQueryBase, new ImageSource[] { ImageSource.SPICE }, ImageType.TIR_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = BodyType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.NASA_006;
            c.modelLabel = "NASA-006";
            c.rootDirOnServer = "/ryugu/nasa-006";
            c.setResolution(ImmutableList.of(
                    "Very Low (12288 plates)", DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[1], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[2], DEFAULT_GASKELL_LABELS_PER_RESOLUTION[3]),
                    ImmutableList.of(12288, DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[1], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[2], DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[3]));
            c.shapeModelFileExtension = ".obj";

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam, tirCam
            };

            c.imageSearchFilterNames = new String[] {};
            c.imageSearchUserDefinedCheckBoxesNames = new String[] {};
            c.imageSearchDefaultMaxSpacecraftDistance = 0;
            c.imageSearchDefaultMaxResolution = 0;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/nasa-006/history/timeHistory.bth";

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 5, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 0, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.density = 1200.; // (kg/m^3)
            c.rotationRate = 0.00022867; // (rad/sec)

            c.hasLidarData = true;
            c.hasHypertreeBasedLidarSearch = true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2018, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2020, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/search/hypertree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2", "/ryugu/shared/lidar/browse/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/ryugu/shared/lidar/browse/fileList.txt";

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

            configArray.add(c);
        }

        // Standard Gaskell shape model may be described once.
        final ShapeModelConfiguration gaskellModelConfig = ShapeModelConfiguration.builder(ShapeModelType.GASKELL.name(), ShapeModelDataUsed.IMAGE_BASED).build();

        // Gaskell images only.
        final ImageSource[] gaskellImagingSource = new ImageSource[] { ImageSource.GASKELL };

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.ATLAS.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            QueryBase queryBase = new GenericPhpQuery("/atlas/gaskell/imaging", "atlas", "/atlas/gaskell/imaging/images/gallery");
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, queryBase, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.ATLAS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/atlas/gaskell";
            c.setResolution(ImmutableList.of(DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0]), ImmutableList.of(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0]));

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 5, 7, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 3, 13, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 400000.0;
            c.imageSearchDefaultMaxResolution = 5000.0;
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.CALYPSO.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.CALYPSO;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/calypso/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.ENCELADUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/enceladus/gaskell";
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EPIMETHEUS.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.EPIMETHEUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/epimetheus/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.HELENE.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.HELENE;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/helene/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.IAPETUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Gaskell (in progress)";
            c.rootDirOnServer = "/iapetus/gaskell";
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.JANUS.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.JANUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/janus/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PAN.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PAN;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pan/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PANDORA.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PANDORA;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pandora/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 4, 20, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 11, 19, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PROMETHEUS.name(),
                    BodyType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PROMETHEUS;
            c.type = BodyType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.GASKELL;
            c.modelLabel = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/prometheus/gaskell";

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            c.hasColoringData = false;
            configArray.add(c);
        }

        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.MU69;
            c.type = BodyType.KBO;
            c.population = ShapeModelPopulation.NA;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelType.MU69_TEST5H_1_FINAL_ORIENTED;
            c.rootDirOnServer = "/mu69/mu69-test5h-1-final-oriented";
            c.shapeModelFileExtension = ".obj";
            c.setResolution(ImmutableList.of("Very Low (25708 plates)"), ImmutableList.of(25708));
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 11, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2019, 0, 2, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e6;
            c.imageSearchDefaultMaxResolution = 1.0e4;
            c.density = Double.NaN;
            c.useMinimumReferencePotential = true;
            c.rotationRate = Double.NaN;

            c.hasImageMap = false;

            if (Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery(c.rootDirOnServer + "/lorri", c.rootDirOnServer + "/lorri/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE, ImageSource.GASKELL},
                            Instrument.LORRI
                            ),
            };

            c.hasSpectralData = false;
            c.spectralInstruments = new BasicSpectrumInstrument[] {
            };

            c.hasStateHistory = false;

            c.hasMapmaker = false;
            c.hasHierarchicalSpectraSearch = false;
            c.hasHypertreeBasedSpectraSearch = false;

            c.hasLidarData = false;
            c.hasHypertreeBasedLidarSearch = false;

            if (SbmtMultiMissionTool.getMission() == SbmtMultiMissionTool.Mission.NH_DEPLOY)
            {
                ViewConfig.setFirstTimeDefaultModelName(c.getUniqueName());
            }

            configArray.add(c);
        }
    }

    // Imaging instrument helper methods.
    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, ImageSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, QueryBase queryBase, ImageSource[] imageSources, ImageType imageType)
    {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources, imageType);
    }

    private static ImagingInstrument setupImagingInstrument(SBMTFileLocator fileLocator, SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, QueryBase queryBase, ImageSource[] imageSources, ImageType imageType)
    {
        Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(instrument, SpectralMode.MONO, queryBase, imageSources, fileLocator, imageType);

        // Put it all together in a session.
        Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
        builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
        return BasicImagingInstrument.of(builder.build());
    }

    public SmallBodyViewConfig(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberElements)
    {
        super(resolutionLabels, resolutionNumberElements);
    }

    private SmallBodyViewConfig()
    {
        super(ImmutableList.<String>copyOf(DEFAULT_GASKELL_LABELS_PER_RESOLUTION), ImmutableList.<Integer>copyOf(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION));
    }

    @Override
    public SmallBodyViewConfig clone() // throws CloneNotSupportedException
    {
        SmallBodyViewConfig c = (SmallBodyViewConfig) super.clone();

        return c;
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
