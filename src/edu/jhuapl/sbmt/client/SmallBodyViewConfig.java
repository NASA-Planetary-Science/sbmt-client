package edu.jhuapl.sbmt.client;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.SessionConfiguration;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.imaging.instruments.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.lidar.old.OlaCubesGenerator;
import edu.jhuapl.sbmt.model.bennu.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.image.BasicImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.model.phobos.PhobosExperimentalSearchSpecification;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.spectrum.SpectralInstrument;
import edu.jhuapl.sbmt.query.FixedListQuery;
import edu.jhuapl.sbmt.query.GenericPhpQuery;
import edu.jhuapl.sbmt.query.QueryBase;

/**
* A SmallBodyConfig is a class for storing all which models should be instantiated
* together with a particular small body. For example some models like Eros
* have imaging, spectral, and lidar data whereas other models may only have
* imaging data. This class is also used when creating (to know which tabs
* to create).
*/
public class SmallBodyViewConfig extends BodyViewConfig
{
    static public SmallBodyViewConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        return (SmallBodyViewConfig)getConfig(name, author, null);
    }

    static public SmallBodyViewConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
    {
        return (SmallBodyViewConfig)getConfig(name, author, version);
    }

    public static void initialize()
    {
        List<ViewConfig> configArray = getBuiltInConfigs();

        // Gaskell Eros
        SmallBodyViewConfig c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EROS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2008)";
        c.rootDirOnServer = "/GASKELL/EROS";
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasImageMap = true;
        c.hasStateHistory = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS", "/GASKELL/EROS/MSI/gallery"),
                        ImageType.MSI_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.GASKELL_UPDATED, ImageSource.SPICE},
                        Instrument.MSI
                        )
        };

        c.hasLidarData = true;
        c.hasMapmaker = true;

        c.hasSpectralData = true;
        c.spectralInstruments=new SpectralInstrument[]{
                new NIS()
        };

        c.hasLineamentData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.imageSearchFilterNames = new String[]{
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{"iofdbl", "cifdbl"};
        c.imageSearchDefaultMaxSpacecraftDistance = 1000.0;
        c.imageSearchDefaultMaxResolution = 50.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
        c.lidarBrowseXYZIndices = new int[]{14, 15, 16};
        c.lidarBrowseSpacecraftIndices = new int[]{8, 9, 10};
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
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas et al. (2001)";
        c.rootDirOnServer = "/THOMAS/EROS";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        c.smallBodyLabelPerResolutionLevel = new String[]{
                "1708 plates", "7790 plates", "10152 plates",
                "22540 plates", "89398 plates", "200700 plates"
        };
        c.smallBodyNumberOfPlatesPerResolutionLevel = new int[]{
                1708, 7790, 10152, 22540, 89398, 200700
        };
        c.hasMapmaker = false;
        configArray.add(c);

        // Eros NLR
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelAuthor.EROSNLR;
        c.customName = "Neumann et al. (2001)";
        c.rootDirOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO

        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelAuthor.EROSNAV;
        c.customName = "NAV team (2001)";
        c.rootDirOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/EROS/history/TimeHistory.bth"; // TODO - use the shared/history directory
        configArray.add(c);

        // Gaskell Itokawa
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell et al. (2008)";
        c.rootDirOnServer = "/GASKELL/ITOKAWA";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/GASKELL/ITOKAWA/history/TimeHistory.bth";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;


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
        c.imageSearchFilterNames = new String[]{
                "Filter ul (381 nm)",
                "Filter b (429 nm)",
                "Filter v (553 nm)",
                "Filter w (700 nm)",
                "Filter x (861 nm)",
                "Filter p (960 nm)",
                "Filter zs (1008 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 26.0;
        c.imageSearchDefaultMaxResolution = 3.0;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime();
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        c.lidarSearchDataSourceMap.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        c.lidarBrowseXYZIndices = new int[]{6, 7, 8};
        c.lidarBrowseSpacecraftIndices = new int[]{3, 4, 5};
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

        c.spectralInstruments=new SpectralInstrument[]{};

        configArray.add(c);

        // Ostro Itokawa
       c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.OSTRO;
        c.customName = "Ostro et al. (2004)";
        c.rootDirOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.RQ36;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.NOLAN;
        c.customName = "Nolan et al. (2013)";
        c.rootDirOnServer = "/NOLAN/BENNU/101955bennu.obj.gz";
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

        //PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "OREX Simulated";
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/RQ36_V3";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            c.hasMapmaker = true;
            if(Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/POLYCAM", "RQ36_POLY"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/POLYCAM", true),
                            ImageType.POLYCAM_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V3/MAPCAM", "RQ36_MAP"),
                            //new FixedListQuery("/GASKELL/RQ36_V3/MAPCAM"),
                            ImageType.MAPCAM_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };
            c.density = 1.0;
            c.useMinimumReferencePotential = false;
            c.rotationRate = 0.000407026411379;
            c.hasLidarData = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            //c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/cubes");
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

            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            // default ideal data
            c.lidarSearchDataSourceMap.put("Default","/GASKELL/RQ36_V3/OLA/trees/default/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default","/GASKELL/RQ36_V3/OLA/browse/default/fileList.txt");
            // noisy data
            c.lidarSearchDataSourceMap.put("Noise","/GASKELL/RQ36_V3/OLA/trees/noise/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Noise","/GASKELL/RQ36_V3/OLA/browse/noise/fileList.txt");
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "OREX Simulated";
            c.version = "V4";
            c.rootDirOnServer = "/GASKELL/RQ36_V4";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e3;
            c.imageSearchDefaultMaxResolution = 1.0e3;
            if(Configuration.isMac())
            {
                // Right now bigmap only works on Macs
                c.hasBigmap = true;
            }

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V4/POLYCAM", "RQ36V4_POLY", "/GASKELL/RQ36_V4/POLYCAM/gallery"),
                            ImageType.POLYCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V4/MAPCAM", "RQ36V4_MAP", "/GASKELL/RQ36_V4/MAPCAM/gallery"),
                            ImageType.MAPCAM_V4_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL},
                            Instrument.MAPCAM
                            )
            };
            c.density = 1.26;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.0004061303295118512;



            c.hasLidarData=true;
            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.OLA;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Default","/GASKELL/RQ36_V4/OLA/Phase07_OB/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Default","/GASKELL/RQ36_V4/OLA/browse/Phase07_OB/fileList.txt");
            c.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V4/OLA/browse/Phase07_OB/fileList.txt";

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


            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.BETULIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/BETULIA/betulia.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GEOGRAPHOS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.BACCHUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.RASHALOM;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/RASHALOM/rashalom.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TOUTATIS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Hudson et al. (2004)";
        c.rootDirOnServer = "/HUDSON/TOUTATIS/4179toutatis.obj.gz";
        c.version = "Low resolution";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TOUTATIS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Hudson et al. (2004)";
        c.rootDirOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
        c.version = "High resolution";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MITHRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/MITHRA/Mithra.v1.PA.prograde.mod.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.NEREUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/NEREUS/Nereus_alt1.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CASTALIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GOLEVKA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HW1;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/HW1/1996hw1.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.SK;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/SK/sk.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._1950DAPROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DAPROGRADE/1950DA_ProgradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._1950DARETROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DARETROGRADE/1950DA_RetrogradeModel.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.WT24;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/WT24/wt24.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody._52760_1998_ML14;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/52760/52760.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.YORP;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/YORP/yorp.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KW4A;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4A/kw4a.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KW4B;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4B/kw4b.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CCALPHA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/CCALPHA/1994CC_nominal.mod.wf.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.CE26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/CE26/ce26.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EV5;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/EV5/2008ev5.obj.gz";
        c.hasColoringData = false;
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KY26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.CERES;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "SPC";
            c.rootDirOnServer = "/GASKELL/CERES";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
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
            c.imageSearchFilterNames = new String[]{
                    "Filter 1 (735 nm)",
                    "Filter 2 (548 nm)",
                    "Filter 3 (749 nm)",
                    "Filter 4 (918 nm)",
                    "Filter 5 (978 nm)",
                    "Filter 6 (829 nm)",
                    "Filter 7 (650 nm)",
                    "Filter 8 (428 nm)"
            };
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"FC1", "FC2"};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PALLAS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/PALLAS/pallas.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2013)";
        c.rootDirOnServer = "/GASKELL/VESTA";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
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
        c.imageSearchFilterNames = new String[]{
                "Filter 1 (735 nm)",
                "Filter 2 (548 nm)",
                "Filter 3 (749 nm)",
                "Filter 4 (918 nm)",
                "Filter 5 (978 nm)",
                "Filter 6 (829 nm)",
                "Filter 7 (650 nm)",
                "Filter 8 (428 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{"FC1", "FC2"};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/VESTA_OLD";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.LUTETIA;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "SPC";
            c.rootDirOnServer = "/GASKELL/LUTETIA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.LUTETIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
        c.customName = "Farnham et al. (2013)";
        c.rootDirOnServer = "/JORDA/LUTETIA";
        c.smallBodyLabelPerResolutionLevel = new String[]{
                "2962 plates ", "5824 plates ", "11954 plates ", "24526 plates ",
                "47784 plates ", "98280 plates ", "189724 plates ", "244128 plates ",
                "382620 plates ", "784510 plates ", "1586194 plates ", "3145728 plates"
        };
        c.smallBodyNumberOfPlatesPerResolutionLevel = new int[]{
                2962, 5824, 11954, 24526, 47784, 98280, 189724,
                244128, 382620, 784510, 1586194, 3145728
        };
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DAPHNE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/DAPHNE/daphne.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HERMIONE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/HERMIONE/hermione.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.KLEOPATRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.customName = "Neese (2004)";
        c.rootDirOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;

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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MATHILDE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;

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
        c.imageSearchFilterNames = new String[]{
                "Filter 1 (550 nm)",
                "Filter 2 (450 nm)",
                "Filter 3 (760 nm)",
                "Filter 4 (950 nm)",
                "Filter 5 (900 nm)",
                "Filter 6 (1000 nm)",
                "Filter 7 (1050 nm)"
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas et al. (2000)";
        c.rootDirOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;

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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.STEINS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
        c.customName = "Farnham and Jorda (2013)";
        c.rootDirOnServer = "/JORDA/STEINS/steins_cart.plt.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HALLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/HALLEY/1682q1halley.llr.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DEIMOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/DEIMOS/DEIMOS.vtk.gz";
        c.hasStateHistory = true;
        c.timeHistoryFile = "/DEIMOS/history/TimeHistory.bth";

        c.hasImageMap = true;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.DEIMOS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.EXPERIMENTAL;
            c.customName = "Ernst et al. (in progress)";
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
            c.imageSearchFilterNames = new String[]{
                    "VIS, Blue",
                    "VIS, Minus Blue",
                    "VIS, Violet",
                    "VIS, Clear",
                    "VIS, Green",
                    "VIS, Red",
            };

            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 30000.0;
            c.imageSearchDefaultMaxResolution = 800.0;
            configArray.add(c);

        }

        // Gaskell Phobos
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2011)";
        c.density = 1.876;
        c.rotationRate = 0.00022803304110600688;
        c.rootDirOnServer = "/GASKELL/PHOBOS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
        c.imageSearchFilterNames = new String[]{
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
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Phobos 2", "Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
        c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
        c.imageSearchDefaultMaxResolution = 300.0;
        c.hasLidarData = true;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(1998, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(1998, 8, 30, 0, 0, 0).getTime();
        c.lidarBrowseXYZIndices = new int[]{0, 1, 2};
        c.lidarBrowseIsLidarInSphericalCoordinates = true;
        c.lidarBrowseSpacecraftIndices = new int[]{-1, -1, -1};
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

        c.hasHypertreeBasedLidarSearch=true;
        c.lidarSearchDataSourceMap = new LinkedHashMap<>();
        c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");

        configArray.add(c);

        // Thomas Phobos
        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";

        c.lidarSearchDataSourceMap=Maps.newHashMap();   // this must be instantiated, but can be empty

        configArray.add(c);

        // New Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PHOBOS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.EXPERIMENTAL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
            c.imageSearchFilterNames = new String[]{
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
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{
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

            c.lidarSearchDataSourceMap=Maps.newHashMap();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/PHOBOS/MOLA/tree/dataSource.lidar");


            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.JUPITER;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/JUPITER/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.hasImageMap = false;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER", "/NEWHORIZONS/JUPITER/IMAGING/images/gallery"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            ),

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
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            //  configArray.add(c);
            SmallBodyViewConfig callisto = new SmallBodyViewConfig();
            callisto = c.clone();

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.AMALTHEA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.STOOKE;
            c.customName = "Stooke (2016)";
            c.rootDirOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            //  configArray.add(c);

            c = callisto.clone();
            c.body = ShapeModelBody.CALLISTO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
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
            //  configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.EUROPA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
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
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            //  configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.GANYMEDE;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
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

            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            //  configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.IO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
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

            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
            // this block so that Eclipse updates will continue to keep this code intact.
            //  configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.DIONE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2013a)";
        c.rootDirOnServer = "/GASKELL/DIONE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.HYPERION;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/HYPERION";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HYPERION;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Thomas (2000)";
        c.rootDirOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.MIMAS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2013b)";
        c.rootDirOnServer = "/GASKELL/MIMAS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PANDORA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/PANDORA/s17pandora.llr.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PHOEBE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2013c)";
        c.rootDirOnServer = "/GASKELL/PHOEBE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;


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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.LARISSA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "STOOKE (2016)";
        c.rootDirOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PROTEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "STOOKE (2016)";
        c.rootDirOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        // 2017-12-12: exclude this body/model for now, but do not comment out anything else in
        // this block so that Eclipse updates will continue to keep this code intact.
        //  configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.PROMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.customName = "Stooke (2016)";
        c.rootDirOnServer = "/STOOKE/PROMETHEUS/s16prometheus.llr.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RHEA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Gaskell (in progress)";
            c.rootDirOnServer = "/GASKELL/RHEA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TETHYS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Gaskell (2013d)";
        c.rootDirOnServer = "/GASKELL/TETHYS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.TEMPEL_1;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Gaskell et al. (in progress)";
            c.rootDirOnServer = "/GASKELL/TEMPEL1";
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TEMPEL_1;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.customName = "Farnham and Thomas (2013)";
        c.rootDirOnServer = "/THOMAS/TEMPEL1/tempel1_cart.t1.gz";
        configArray.add(c);

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.WILD_2;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.DUXBURY;
        c.customName = "Farnham et al. (2005)";
        c.rootDirOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "SHAP5 V0.3";
            c.rootDirOnServer = "/GASKELL/67P";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
            c.imageSearchFilterNames = new String[]{
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
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"NAC", "*WAC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);

            c = c.clone();
            c.author = ShapeModelAuthor.DLR;
            c.rootDirOnServer = "/DLR/67P";
            c.version = "SHAP4S";
            c.imagingInstruments[0].searchQuery = new GenericPhpQuery("/DLR/67P/IMAGING", "67P_DLR", "/DLR/67P/IMAGING/images/gallery");
            c.smallBodyLabelPerResolutionLevel = new String[]{
                    "17442 plates ", "72770 plates ", "298442 plates ", "1214922 plates ",
                    "4895631 plates ", "16745283 plates "
            };
            c.smallBodyNumberOfPlatesPerResolutionLevel = new int[]{
                    17442, 72770, 298442, 1214922, 4895631, 16745283
            };
            configArray.add(c);

            // 67P_V2
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "V2";
            c.rootDirOnServer = "/GASKELL/67P_V2";

            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
            c.imageSearchFilterNames = new String[]{
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
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"NAC", "*WAC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);

            // 67P_V3
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody._67P;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "V3";
            c.rootDirOnServer = "/GASKELL/67P_V3";

            c.hasCustomBodyCubeSize = true;
            c.customBodyCubeSize = 0.10; // km

            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
            c.imageSearchFilterNames = new String[]{
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
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{"NAC", "*WAC"};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.HARTLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/HARTLEY/hartley2_2012_cart.plt.gz";
        configArray.add(c);


        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PLUTO;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
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
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            configArray.add(c);


            c = c.clone();
            c.body = ShapeModelBody.CHARON;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
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

            configArray.add(c);

            SmallBodyViewConfig hydra = new SmallBodyViewConfig();

            c = c.clone();
            c.body = ShapeModelBody.HYDRA;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
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
            configArray.add(c);


            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.KERBEROS;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
            c.rootDirOnServer = "/NEWHORIZONS/KERBEROS/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = hydra;
            c.body = ShapeModelBody.NIX;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
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
            configArray.add(c);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.STYX;
            c.type = ShapeModelType.KBO;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.customName = "Stern et. al (2015)";
            c.rootDirOnServer = "/NEWHORIZONS/STYX/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);
        }

        c = new SmallBodyViewConfig();
        c.body = ShapeModelBody.TELESTO;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.customName = "Ernst et al. (in progress)";
        c.rootDirOnServer = "/GASKELL/TELESTO";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

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
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, test spherical version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
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
                        ImageType.MAPCAM_EARTH_IMAGE);

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
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.BLENDER;
            c.rootDirOnServer = "/earth/osirisrex";
            c.smallBodyLabelPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_LABELS_PER_RESOLUTION, 0, 1);
            c.smallBodyNumberOfPlatesPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION, 0, 1);
            c.hasColoringData = false;
            c.hasImageMap=true;

                c.imagingInstruments = new ImagingInstrument[] {
                       // new Vis(ShapeModelBody.PHOBOS)
                        mapCam,
                        polyCam,
                        samCam,
// TODO when samCam is handled for sbmt1dev (see above), uncomment the next line to add it to the panel.
//                        samCam
/*                    new ImagingInstrument(
                                SpectralMode.MONO,
                                new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"),
                                ImageType.PHOBOS_IMAGE,
                                new ImageSource[]{ImageSource.GASKELL},
                                Instrument.IMAGING_DATA
                                )*/
                };

                c.hasSpectralData=true;
                c.spectralInstruments=new SpectralInstrument[] {
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
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, OREX WGS84 version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.EARTH.name()).build();


            // Set up shape model -- one will suffice.
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
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelAuthor.OREX;
            c.rootDirOnServer = "/earth/orex";
//            c.shapeModelFileExtension = ".obj";
            c.smallBodyLabelPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_LABELS_PER_RESOLUTION, 0, 1);
            c.smallBodyNumberOfPlatesPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION, 0, 1);
            c.hasColoringData = false;
            c.hasImageMap=true;

                c.imagingInstruments = new ImagingInstrument[] {
                       // new Vis(ShapeModelBody.PHOBOS)
                        mapCam,
                        polyCam,
                        samCam,
    //TODO when samCam is handled for sbmt1dev (see above), uncomment the next line to add it to the panel.
    //                    samCam
    /*                    new ImagingInstrument(
                                SpectralMode.MONO,
                                new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"),
                                ImageType.PHOBOS_IMAGE,
                                new ImageSource[]{ImageSource.GASKELL},
                                Instrument.IMAGING_DATA
                                )*/
                };

                c.hasSpectralData=true;
                c.spectralInstruments=new SpectralInstrument[] {
                        new OTES(),new OVIRS()
                };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
    //TODO make hierarchical search work sbmt1dev-style.
    //        c.imageSearchFilterNames = new String[]{
    //                EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
    //        };
    //        c.imageSearchUserDefinedCheckBoxesNames = new String[]{
    //                EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
    //        };
    //        c.hasHierarchicalImageSearch = true;
    //        c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            //
            // Earth, Hayabusa2 WGS84 version
            //

            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EARTH.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
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
              QueryBase queryBase = new FixedListQuery("/" + fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), "/" + fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
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
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.EARTH;
            c.dataUsed = ShapeModelDataUsed.WGS84;
            c.author = ShapeModelAuthor.HAYABUSA2;
            c.rootDirOnServer = "/earth/hayabusa2";
//            c.shapeModelFileExtension = ".obj";
            c.smallBodyLabelPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_LABELS_PER_RESOLUTION, 0, 1);
            c.smallBodyNumberOfPlatesPerResolutionLevel = Arrays.copyOfRange(DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION, 0, 1);
            c.hasImageMap=true;
            c.hasColoringData = false;

                c.imagingInstruments = new ImagingInstrument[] {
//                       // new Vis(ShapeModelBody.PHOBOS)
//                        mapCam,
//                        polyCam,
    //TODO when samCam is handled for sbmt1dev (see above), uncomment the next line to add it to the panel.
    //                    samCam
    /*                    new ImagingInstrument(
                                SpectralMode.MONO,
                                new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP", "/GASKELL/PHOBOS/IMAGING/images/gallery"),
                                ImageType.PHOBOS_IMAGE,
                                new ImageSource[]{ImageSource.GASKELL},
                                Instrument.IMAGING_DATA
                                )*/
                        tir
                };

//                c.hasSpectralData=true;
//                c.spectralInstruments=new SpectralInstrument[] {
//                        new OTES()
//                };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2017, 6, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 12, 31, 0, 0, 0).getTime();
    //TODO make hierarchical search work sbmt1dev-style.
    //        c.imageSearchFilterNames = new String[]{
    //                EarthHierarchicalSearchSpecification.FilterCheckbox.MAPCAM_CHANNEL_1.getName()
    //        };
    //        c.imageSearchUserDefinedCheckBoxesNames = new String[]{
    //                EarthHierarchicalSearchSpecification.CameraCheckbox.OSIRIS_REX.getName()
    //        };
    //        c.hasHierarchicalImageSearch = true;
    //        c.hierarchicalImageSearchSpecification = new EarthHierarchicalSearchSpecification();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasSpectralData=true;
            c.spectralInstruments=new SpectralInstrument[]
                    {
                            new NIRS3()
                    };


            configArray.add(c);
        }



        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    ShapeModelType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Truth", ShapeModelDataUsed.IMAGE_BASED).build();

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.TRUTH;
            c.customName = "H2 Simulated Truth";
            c.rootDirOnServer = "/ryugu/truth";
            c.shapeModelFileExtension = ".obj";
            c.smallBodyLabelPerResolutionLevel = new String[] { "High" };
            c.smallBodyNumberOfPlatesPerResolutionLevel = new int[] { 5450419 };

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/truth/history/timeHistory.bth";

            BasicImagingInstrument oncCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.IMAGING_DATA, ".fit", ".INFO", null, ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.IMAGING_DATA,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.SPICE },
                        fileLocator,
                        ImageType.ONC_TRUTH_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                oncCam = BasicImagingInstrument.of(builder.build());
            }
            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
            };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 7, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 1, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;


            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            // Set up body -- one will suffice.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.RYUGU.name(),
                    ShapeModelType.ASTEROID.name(),
                    ShapeModelPopulation.NEO.name()).build();

            // Set up shape model -- one will suffice.
            ShapeModelConfiguration modelConfig = ShapeModelConfiguration.builder("Gaskell", ShapeModelDataUsed.IMAGE_BASED).build();

            BasicImagingInstrument oncCam;
            {
                // Set up images.
                SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, Instrument.IMAGING_DATA, ".fit", null, ".SUM", ".jpeg");
                QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
                Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                        Instrument.IMAGING_DATA,
                        SpectralMode.MONO,
                        queryBase,
                        new ImageSource[] { ImageSource.GASKELL },
                        fileLocator,
                        ImageType.ONC_IMAGE);

                // Put it all together in a session.
                Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
                builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
                oncCam = BasicImagingInstrument.of(builder.build());
            }

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.RYUGU;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "H2 Simulated Gaskell";
            c.rootDirOnServer = "/ryugu/gaskell";
            c.shapeModelFileExtension = ".obj";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.hasStateHistory = true;
            c.timeHistoryFile = "/ryugu/gaskell/history/timeHistory.bth"; // TODO move this to shared/timeHistory.bth

            c.imagingInstruments = new ImagingInstrument[] {
                    oncCam,
            };

            c.hasMapmaker = false;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2018, 7, 1, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2021, 1, 31, 0, 0, 0).getTime();
            c.imageSearchDefaultMaxSpacecraftDistance = 120000.0;
            c.imageSearchDefaultMaxResolution = 300.0;

            c.hasLidarData=true;
            c.hasHypertreeBasedLidarSearch=true; // enable tree-based lidar searching
            c.lidarInstrumentName = Instrument.LASER;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<>();
            c.lidarBrowseDataSourceMap = new LinkedHashMap<>();
            c.lidarSearchDataSourceMap.put("Hayabusa2","/earth/hayabusa2/laser/tree/dataSource.lidar");
            c.lidarBrowseDataSourceMap.put("Hayabusa2","/earth/hayabusa2/laser/browse/fileList.txt");
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

            configArray.add(c);
        }

        // Standard Gaskell shape model may be described once.
        final ShapeModelConfiguration gaskellModelConfig = ShapeModelConfiguration.builder(ShapeModelAuthor.GASKELL.name(), ShapeModelDataUsed.IMAGE_BASED).build();

        // Gaskell images only.
        final ImageSource[] gaskellImagingSource = new ImageSource[] { ImageSource.GASKELL };

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.ATLAS.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.ATLAS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/atlas/gaskell";
//            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
//            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.smallBodyLabelPerResolutionLevel = new String[] { DEFAULT_GASKELL_LABELS_PER_RESOLUTION[0] };
            c.smallBodyNumberOfPlatesPerResolutionLevel = new int[] { DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION[0] };

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.CALYPSO.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.CALYPSO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/calypso/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 23, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 1, 14, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.ENCELADUS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Gaskell (in progress)";
            c.rootDirOnServer = "/enceladus/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.EPIMETHEUS.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.EPIMETHEUS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/epimetheus/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.HELENE.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.HELENE;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/helene/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.IAPETUS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Gaskell (in progress)";
            c.rootDirOnServer = "/iapetus/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.JANUS.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.JANUS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/janus/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PAN.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PAN;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pan/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PANDORA.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PANDORA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/pandora/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 4, 20, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2016, 11, 19, 0, 0, 0).getTime();
            configArray.add(c);
        }

        {
            // Set up body.
            SBMTBodyConfiguration bodyConfig = SBMTBodyConfiguration.builder(
                    ShapeModelBody.PROMETHEUS.name(),
                    ShapeModelType.PLANETS_AND_SATELLITES.name(),
                    ShapeModelPopulation.SATURN.name()).build();
            ImagingInstrument imagingInstrument = setupImagingInstrument(bodyConfig, gaskellModelConfig, Instrument.IMAGING_DATA, gaskellImagingSource, ImageType.SATURN_MOON_IMAGE);

            c = new SmallBodyViewConfig();
            c.body = ShapeModelBody.PROMETHEUS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.customName = "Ernst et al. (in progress)";
            c.rootDirOnServer = "/prometheus/gaskell";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    imagingInstrument
            };
            c.imageSearchDefaultStartDate = new GregorianCalendar(2006, 3, 29, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2017, 2, 7, 0, 0, 0).getTime();
            configArray.add(c);
        }
    }

    // SBMT1-style helper method.
    private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument, ImageSource[] imageSources, ImageType imageType) {
        SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM", ".jpeg");
        QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
        Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(
                instrument,
                SpectralMode.MONO,
                queryBase,
                imageSources,
                fileLocator,
                imageType);

        // Put it all together in a session.
        Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
        builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
        return BasicImagingInstrument.of(builder.build());
    }

    @Override
    public SmallBodyViewConfig clone() // throws CloneNotSupportedException
    {
        SmallBodyViewConfig c = (SmallBodyViewConfig)super.clone();

        return c;
    }


}
