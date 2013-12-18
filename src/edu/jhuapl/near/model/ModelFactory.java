package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import nom.tam.fits.FitsException;

import org.joda.time.DateTime;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.model.deimos.DeimosImage;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.ErosThomas;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.gaspra.SSIGaspraImage;
import edu.jhuapl.near.model.ida.SSIIdaImage;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.lutetia.Lutetia;
import edu.jhuapl.near.model.lutetia.OsirisImage;
import edu.jhuapl.near.model.mathilde.MSIMathildeImage;
import edu.jhuapl.near.model.phobos.PhobosImage;
import edu.jhuapl.near.model.saturnmoon.SaturnMoonImage;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.model.vesta_old.VestaOld;
import edu.jhuapl.near.query.DeimosQuery;
import edu.jhuapl.near.query.ErosQuery;
import edu.jhuapl.near.query.GaspraQuery;
import edu.jhuapl.near.query.IdaQuery;
import edu.jhuapl.near.query.ItokawaQuery;
import edu.jhuapl.near.query.LutetiaQuery;
import edu.jhuapl.near.query.MathildeQuery;
import edu.jhuapl.near.query.PhobosExperimentalQuery;
import edu.jhuapl.near.query.PhobosQuery;
import edu.jhuapl.near.query.QueryBase;
import edu.jhuapl.near.query.SaturnMoonQuery;
import edu.jhuapl.near.query.VestaQuery;
import edu.jhuapl.near.util.Configuration;

public class ModelFactory
{
    // Names of built-in small body models
    static public final String EROS = "Eros";
    static public final String ITOKAWA = "Itokawa";
    static public final String VESTA = "Vesta";
    static public final String MIMAS = "Mimas";
    static public final String PHOEBE = "Phoebe";
    static public final String PHOBOS = "Phobos";
    static public final String RQ36 = "Bennu";
    static public final String DIONE = "Dione";
    static public final String RHEA = "Rhea";
    static public final String TETHYS = "Tethys";
    static public final String LUTETIA = "Lutetia";
    static public final String IDA = "Ida";
    static public final String GASPRA = "Gaspra";
    static public final String MATHILDE = "Mathilde";
    static public final String DEIMOS = "Deimos";
    static public final String JANUS = "Janus";
    static public final String EPIMETHEUS = "Epimetheus";
    static public final String HYPERION = "Hyperion";
    static public final String TEMPEL_1 = "Tempel 1";
    static public final String HALLEY = "Halley";
    static public final String AMALTHEA = "Amalthea";
    static public final String LARISSA = "Larissa";
    static public final String PROTEUS = "Proteus";
    static public final String PROMETHEUS = "Prometheus";
    static public final String PANDORA = "Pandora";
    static public final String GEOGRAPHOS = "Geographos";
    static public final String KY26 = "KY26";
    static public final String BACCHUS = "Bacchus";
    static public final String KLEOPATRA = "Kleopatra";
    static public final String TOUTATIS_LOW_RES = "Toutatis (Low Res)";
    static public final String TOUTATIS_HIGH_RES = "Toutatis (High Res)";
    static public final String CASTALIA = "Castalia";
    static public final String _52760_1998_ML14 = "52760 (1998 ML14)";
    static public final String GOLEVKA = "Golevka";
    static public final String WILD_2 = "Wild 2";
    static public final String STEINS = "Steins";
    static public final String HARTLEY = "Hartley";

    // Types of bodies
    static public final String ASTEROID = "Asteroids";
    static public final String SATELLITES = "Satellites";
    static public final String COMETS = "Comets";

    // Populations
    static public final String MARS = "Mars";
    static public final String JUPITER = "Jupiter";
    static public final String SATURN = "Saturn";
    static public final String NEPTUNE = "Neptune";
    static public final String NEO = "Near-Earth";
    static public final String MAIN_BELT = "Main Belt";

    // Names of authors
    static public final String GASKELL = "Gaskell";
    static public final String THOMAS = "Thomas";
    static public final String STOOKE = "Stooke";
    static public final String HUDSON = "Hudson";
    static public final String DUXBURY = "Duxbury";
    static public final String OSTRO = "Ostro";
    static public final String JORDA = "Jorda";
    static public final String NOLAN = "Nolan";
    static public final String CUSTOM = "Custom";
    static public final String EROSNAV = "NAV";
    static public final String EROSNLR = "NLR";
    static public final String EXPERIMENTAL = "Experimental";

    // Data used to construct shape model (either images, radar, lidar, or enhanced)
    static public final String IMAGE_BASED = "Image Based";
    static public final String RADAR_BASED = "Radar Based";
    static public final String LIDAR_BASED = "Lidar Based";
    static public final String ENHANCED = "Enhanced";

    // Names of instruments
    static public final String MSI = "MSI";
    static public final String NLR = "NLR";
    static public final String NIS = "NIS";
    static public final String AMICA = "AMICA";
    static public final String LIDAR = "LIDAR";
    static public final String FC = "FC";
    static public final String SSI = "SSI";
    static public final String OSIRIS = "OSIRIS";
    static public final String OLA = "OLA";
    static public final String IMAGING_DATA = "Imaging Data";

    static public enum ImageType {
        MSI_IMAGE,
        AMICA_IMAGE,
        FC_IMAGE,
        PHOBOS_IMAGE,
        DEIMOS_IMAGE,
        OSIRIS_IMAGE,
        SATURN_MOON_IMAGE,
        SSI_GASPRA_IMAGE,
        SSI_IDA_IMAGE,
        MSI_MATHILDE_IMAGE
    }

    static private final String[] DEFAULT_GASKELL_LABELS_PER_RESOLUTION = {
        "Low (49152 plates)",
        "Medium (196608 plates)",
        "High (786432 plates)",
        "Very High (3145728 plates)"
    };

    static private final int[] DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION = {
        49152,
        196608,
        786432,
        3145728
    };

    static public final ArrayList<ModelConfig> builtInModelConfigs = new ArrayList<ModelConfig>();

    static
    {
        ArrayList<ModelConfig> configArray = builtInModelConfigs;

        // Gaskell Eros
        ModelConfig c = new ModelConfig();
        c.name = EROS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/EROS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasSpectralData = true;
        c.hasLineamentData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.imageSearchQuery = ErosQuery.getInstance();
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
        c.imageSearchDefaultMaxSpacecraftDistance = 100.0;
        c.imageSearchDefaultMaxResolution = 50.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS};
        c.imageType = ImageType.MSI_IMAGE;
        c.imageInstrumentName = MSI;
        c.lidarSearchDefaultStartDate = new DateTime(2000, 2, 28, 0, 0, 0, 0).toDate();
        c.lidarSearchDefaultEndDate = new DateTime(2001, 2, 13, 0, 0, 0, 0).toDate();
        c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
        c.lidarSearchDataSourceMap.put("Default", "/NLR/cubes");
        c.lidarBrowseXYZIndices = new int[]{14, 15, 16};
        c.lidarBrowseSpacecraftIndices = new int[]{8, 9, 10};
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = true;
        c.lidarBrowseTimeIndex = 4;
        c.lidarBrowseNoiseIndex = 7;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/near/data/NlrFiles.txt";
        c.lidarBrowseNumberHeaderLines = 2;
        c.lidarBrowseIsInMeters = true;
        c.lidarOffsetScale = 0.025;
        c.lidarInstrumentName = NLR;
        configArray.add(c);

        // Thomas Eros
        c = c.clone();
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/EROS";
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
        c.dataUsed = LIDAR_BASED;
        c.author = EROSNLR;
        c.pathOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = LIDAR_BASED;
        c.author = EROSNAV;
        c.pathOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        configArray.add(c);

        // Gaskell Itokawa
        c = new ModelConfig();
        c.name = ITOKAWA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/ITOKAWA";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = ItokawaQuery.getInstance();
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS, ImageSource.CORRECTED};
        c.imageType = ImageType.AMICA_IMAGE;
        c.imageInstrumentName = AMICA;
        c.lidarSearchDefaultStartDate = new DateTime(2005, 9, 1, 0, 0, 0, 0).toDate();
        c.lidarSearchDefaultEndDate = new DateTime(2005, 11, 30, 0, 0, 0, 0).toDate();
        c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
        c.lidarSearchDataSourceMap.put("Optimized", "/ITOKAWA/LIDAR/cdr/cubes-optimized");
        c.lidarSearchDataSourceMap.put("Unfiltered", "/ITOKAWA/LIDAR/cdr/cubes-unfiltered");
        c.lidarBrowseXYZIndices = new int[]{6, 7, 8};
        c.lidarBrowseSpacecraftIndices = new int[]{3, 4, 5};
        c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
        c.lidarBrowseTimeIndex = 1;
        c.lidarBrowseNoiseIndex = -1;
        c.lidarBrowseFileListResourcePath = "/edu/jhuapl/near/data/HayLidarFiles.txt";
        c.lidarBrowseNumberHeaderLines = 0;
        c.lidarBrowseIsInMeters = false;
        // The following value is the Itokawa diagonal length divided by 1546.4224133453388.
        // The value 1546.4224133453388 was chosen so that for Eros the offset scale is 0.025 km.
        c.lidarOffsetScale = 0.00044228259621279913;
        c.lidarInstrumentName = LIDAR;
        configArray.add(c);

        // Ostro Itokawa
        c = new ModelConfig();
        c.name = ITOKAWA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = OSTRO;
        c.pathOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        configArray.add(c);

        // Gaskell Phobos
        c = new ModelConfig();
        c.name = PHOBOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/PHOBOS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1989, 2, 26, 0, 0, 0).getTime();
        c.imageSearchQuery = PhobosQuery.getInstance();
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
        c.imageSearchDefaultMaxSpacecraftDistance = 9000.0;
        c.imageSearchDefaultMaxResolution = 300.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS};
        c.imageType = ImageType.PHOBOS_IMAGE;
        c.imageInstrumentName = IMAGING_DATA;
        configArray.add(c);

        // Thomas Phobos
        c = new ModelConfig();
        c.name = PHOBOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";
        configArray.add(c);

        // New Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = PHOBOS;
            c.type = SATELLITES;
            c.population = MARS;
            c.dataUsed = IMAGE_BASED;
            c.author = EXPERIMENTAL;
            c.pathOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            c.hasPerspectiveImages = true;
            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
            c.imageSearchQuery = PhobosExperimentalQuery.getInstance();
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
            c.imageSearchDefaultMaxSpacecraftDistance = 9000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.PHOBOS_IMAGE;
            c.imageInstrumentName = IMAGING_DATA;
            configArray.add(c);
        }

        c = new ModelConfig();
        c.name = AMALTHEA;
        c.type = SATELLITES;
        c.population = JUPITER;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = MIMAS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/MIMAS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new SaturnMoonQuery("/GASKELL/MIMAS/IMAGING");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
        c.imageType = ImageType.SATURN_MOON_IMAGE;
        c.imageInstrumentName = IMAGING_DATA;
        configArray.add(c);

        c = new ModelConfig();
        c.name = PHOEBE;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/PHOEBE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new SaturnMoonQuery("/GASKELL/PHOEBE/IMAGING");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
        c.imageType = ImageType.SATURN_MOON_IMAGE;
        c.imageInstrumentName = IMAGING_DATA;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = VESTA;
            c.type = ASTEROID;
            c.population = MAIN_BELT;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/VESTA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
            c.imageSearchQuery = VestaQuery.getInstance();
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
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.PDS};
            c.imageType = ImageType.FC_IMAGE;
            c.imageInstrumentName = FC;
            configArray.add(c);
        }

        c = new ModelConfig();
        c.name = VESTA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/VESTA_OLD";
        configArray.add(c);

        c = new ModelConfig();
        c.name = IDA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1993, 7, 28, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1993, 7, 29, 0, 0, 0).getTime();
        c.imageSearchQuery = IdaQuery.getInstance();
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.CORRECTED};
        c.imageType = ImageType.SSI_IDA_IMAGE;
        c.imageInstrumentName = SSI;
        configArray.add(c);

        c = new ModelConfig();
        c.name = IDA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new ModelConfig();
        c.name = GASPRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
        c.imageSearchQuery = GaspraQuery.getInstance();
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.CORRECTED};
        c.imageType = ImageType.SSI_GASPRA_IMAGE;
        c.imageInstrumentName = SSI;
        configArray.add(c);

        c = new ModelConfig();
        c.name = GASPRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new ModelConfig();
        c.name = MATHILDE;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1997, 5, 27, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1997, 5, 28, 0, 0, 0).getTime();
        c.imageSearchQuery = MathildeQuery.getInstance();
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.CORRECTED};
        c.imageType = ImageType.MSI_MATHILDE_IMAGE;
        c.imageInstrumentName = MSI;
        configArray.add(c);

        c = new ModelConfig();
        c.name = DEIMOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/DEIMOS/DEIMOS.vtk.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
        c.imageSearchQuery = DeimosQuery.getInstance();
        c.imageSearchFilterNames = new String[]{
                "VIS, Blue",
                "VIS, Minus Blue",
                "VIS, Violet",
                "VIS, Clear",
                "VIS, Green",
                "VIS, Red",
        };
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{"Viking Orbiter 1-A", "Viking Orbiter 1-B", "Viking Orbiter 2-A", "Viking Orbiter 2-B", "MEX HRSC"};
        c.imageSearchDefaultMaxSpacecraftDistance = 9000.0;
        c.imageSearchDefaultMaxResolution = 300.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.PDS, ImageSource.CORRECTED};
        c.imageType = ImageType.DEIMOS_IMAGE;
        c.imageInstrumentName = IMAGING_DATA;
        configArray.add(c);

        c = new ModelConfig();
        c.name = JANUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = JANUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = EPIMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = EPIMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = HALLEY;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/HALLEY/1682q1halley.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = LARISSA;
        c.type = SATELLITES;
        c.population = NEPTUNE;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = PROTEUS;
        c.type = SATELLITES;
        c.population = NEPTUNE;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = PROMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PROMETHEUS/s16prometheus.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = PANDORA;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PANDORA/s17pandora.llr.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = GEOGRAPHOS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = KY26;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = BACCHUS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = KLEOPATRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = TOUTATIS_LOW_RES;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS/4179toutatis.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = TOUTATIS_HIGH_RES;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = CASTALIA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = _52760_1998_ML14;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/52760/52760.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = GOLEVKA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = RQ36;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = NOLAN;
        c.pathOnServer = "/NOLAN/BENNU/101955bennu.obj.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = RQ36;
            c.type = ASTEROID;
            c.population = NEO;
            c.dataUsed = ENHANCED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/RQ36";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            c.hasLidarData = true;
            c.hasMapmaker = true;
            c.lidarSearchDefaultStartDate = new DateTime(2000, 1, 1, 0, 0, 0, 0).toDate();
            c.lidarSearchDefaultEndDate = new DateTime(2050, 1, 1, 0, 0, 0, 0).toDate();
            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36/OLA/cubes");
            c.lidarBrowseXYZIndices = new int[]{96, 104, 112};
            c.lidarBrowseSpacecraftIndices = new int[]{-1, -1, -1};
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 18;
            c.lidarBrowseNoiseIndex = -1;
            c.lidarBrowseFileListResourcePath = "/edu/jhuapl/near/data/OlaLidarFiles.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 144;
            c.lidarOffsetScale = 0.0005;
            c.lidarInstrumentName = OLA;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = LUTETIA;
            c.type = ASTEROID;
            c.population = MAIN_BELT;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/LUTETIA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
            c.imageSearchQuery = LutetiaQuery.getInstance();
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.OSIRIS_IMAGE;
            c.imageInstrumentName = OSIRIS;
            configArray.add(c);
        }

        c = new ModelConfig();
        c.name = LUTETIA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = JORDA;
        c.pathOnServer = "/JORDA/LUTETIA";
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

        c = new ModelConfig();
        c.name = STEINS;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = JORDA;
        c.pathOnServer = "/JORDA/STEINS/steins_cart.plt.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = DIONE;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/DIONE";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
            c.imageSearchQuery = new SaturnMoonQuery("/GASKELL/DIONE/IMAGING");
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.SATURN_MOON_IMAGE;
            c.imageInstrumentName = IMAGING_DATA;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = RHEA;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/RHEA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = TETHYS;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/TETHYS";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = HYPERION;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/HYPERION";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.useAPLServer = true;
            configArray.add(c);
        }

        c = new ModelConfig();
        c.name = HYPERION;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = TEMPEL_1;
            c.type = COMETS;
            c.population = null;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/TEMPEL1";
            c.useAPLServer = true;
            configArray.add(c);
        }

        c = new ModelConfig();
        c.name = TEMPEL_1;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/TEMPEL1/tempel1_cart.t1.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = WILD_2;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = DUXBURY;
        c.pathOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
        configArray.add(c);

        c = new ModelConfig();
        c.name = HARTLEY;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/HARTLEY/hartley2_2012_cart.plt.gz";
        configArray.add(c);
    }

    /**
     * A ModelConfig is a class for storing all which models should be instantiated
     * together with a particular small body. For example some models like Eros
     * have imaging, spectral, and lidar data whereas other models may only have
     * imaging data. This class is also used when creating (to know which tabs
     * to create).
     *
     * @author kahneg1
     *
     */
    public static class ModelConfig
    {

        public String name;
        public String type; // e.g. asteroid, comet, satellite
        public String population; // e.g. Mars for satellites or main belt for asteroids
        public String dataUsed; // e.g. images, radar, lidar, or enhanced
        public String author; // e.g. Gaskell
        public String pathOnServer;
        public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1
        public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1
        public boolean useAPLServer = false;
        public boolean hasImageMap = false;
        public boolean hasPerspectiveImages = false;
        public boolean hasLidarData = false;
        public boolean hasMapmaker = false;
        public boolean hasSpectralData = false;
        public boolean hasLineamentData = false;
        // if hasPerspectiveImages is true, the following must be filled in
        public Date imageSearchDefaultStartDate;
        public Date imageSearchDefaultEndDate;
        public QueryBase imageSearchQuery;
        public String[] imageSearchFilterNames;
        public String[] imageSearchUserDefinedCheckBoxesNames;
        public double imageSearchDefaultMaxSpacecraftDistance;
        public double imageSearchDefaultMaxResolution;
        public ImageSource[] imageSearchImageSources;
        public ImageType imageType;
        public String imageInstrumentName = IMAGING_DATA;
        // if hasLidarData is true, the following must be filled in
        public Date lidarSearchDefaultStartDate;
        public Date lidarSearchDefaultEndDate;
        public Map<String, String> lidarSearchDataSourceMap;
        public int[] lidarBrowseXYZIndices;
        public int[] lidarBrowseSpacecraftIndices;
        public boolean lidarBrowseIsSpacecraftInSphericalCoordinates;
        public int lidarBrowseTimeIndex;
        public int lidarBrowseNoiseIndex;
        public String lidarBrowseFileListResourcePath;
        public int lidarBrowseNumberHeaderLines;
        public boolean lidarBrowseIsBinary = false;
        public int lidarBrowseBinaryRecordSize; // only required if lidarBrowseIsBinary is true
        // Return whether or not the units of the lidar points are in meters. If false
        // they are assumed to be in kilometers.
        public boolean lidarBrowseIsInMeters;
        public double lidarOffsetScale;
        public String lidarInstrumentName = LIDAR;


        protected ModelConfig clone()
        {
            ModelConfig c = new ModelConfig();
            c.name = this.name;
            c.type = this.type;
            c.population = this.population;
            c.dataUsed = this.dataUsed;
            c.author = this.author;
            c.pathOnServer = this.pathOnServer;
            c.useAPLServer = this.useAPLServer;
            c.hasImageMap = this.hasImageMap;
            c.hasPerspectiveImages = this.hasPerspectiveImages;
            c.hasLidarData = this.hasLidarData;
            c.hasMapmaker = this.hasMapmaker;
            c.hasSpectralData = this.hasSpectralData;
            c.hasLineamentData = this.hasLineamentData;
            if (this.smallBodyLabelPerResolutionLevel != null)
                c.smallBodyLabelPerResolutionLevel = this.smallBodyLabelPerResolutionLevel.clone();
            if (this.smallBodyNumberOfPlatesPerResolutionLevel != null)
                c.smallBodyNumberOfPlatesPerResolutionLevel = this.smallBodyNumberOfPlatesPerResolutionLevel.clone();
            if (this.hasPerspectiveImages)
            {
                c.imageSearchDefaultStartDate = (Date)this.imageSearchDefaultStartDate.clone();
                c.imageSearchDefaultEndDate = (Date)this.imageSearchDefaultEndDate.clone();
                c.imageSearchQuery = this.imageSearchQuery;
                c.imageSearchFilterNames = this.imageSearchFilterNames.clone();
                c.imageSearchUserDefinedCheckBoxesNames = this.imageSearchFilterNames.clone();
                c.imageSearchDefaultMaxSpacecraftDistance = this.imageSearchDefaultMaxSpacecraftDistance;
                c.imageSearchDefaultMaxResolution = this.imageSearchDefaultMaxResolution;
                c.imageSearchImageSources = this.imageSearchImageSources.clone();
                c.imageType = this.imageType;
                c.imageInstrumentName = this.imageInstrumentName;
            }
            if (this.hasLidarData)
            {
                c.lidarSearchDefaultStartDate = (Date)this.lidarSearchDefaultStartDate.clone();
                c.lidarSearchDefaultEndDate = (Date)this.lidarSearchDefaultEndDate.clone();
                c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>(this.lidarSearchDataSourceMap);
                c.lidarBrowseXYZIndices = this.lidarBrowseXYZIndices.clone();
                c.lidarBrowseSpacecraftIndices = this.lidarBrowseSpacecraftIndices.clone();
                c.lidarBrowseIsSpacecraftInSphericalCoordinates = this.lidarBrowseIsSpacecraftInSphericalCoordinates;
                c.lidarBrowseTimeIndex = this.lidarBrowseTimeIndex;
                c.lidarBrowseNoiseIndex = this.lidarBrowseNoiseIndex;
                c.lidarBrowseFileListResourcePath = this.lidarBrowseFileListResourcePath;
                c.lidarBrowseNumberHeaderLines = this.lidarBrowseNumberHeaderLines;
                c.lidarBrowseIsInMeters = this.lidarBrowseIsInMeters;
                c.lidarBrowseIsBinary = this.lidarBrowseIsBinary;
                c.lidarBrowseBinaryRecordSize = this.lidarBrowseBinaryRecordSize;
                c.lidarOffsetScale = this.lidarOffsetScale;
                c.lidarInstrumentName = this.lidarInstrumentName;
            }
            return c;
        }

        /**
         * Returns model as a path. e.g. "Asteroid > Near-Earth > Eros > Image Based > Gaskell"
         */
        public String getPathRepresentation()
        {
            String path = type;
            if (population != null)
                path += " > " + population;
            path += " > " + name
                    + " > " + dataUsed
                    + " > " + author;
            return path;
        }

    }

    /**
     * Get a ModelConfig of a specific name and author.
     * Note a ModelConfig is uniquely described by its name and author.
     * No two model configs can have both the same.
     *
     * @param name
     * @param author
     * @return
     */
    static public ModelConfig getModelConfig(String name, String author)
    {
        for (ModelConfig config : builtInModelConfigs)
        {
            if (config.name.equals(name) && config.author.equals(author))
                return config;
        }

        System.err.println("Error: Cannot find ModelConfig with name " + name + " and author " + author);

        return null;
    }

    static public Image createImage(
            ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        ModelConfig config = smallBodyModel.getModelConfig();

        if (ImageSource.PDS.equals(key.source) ||
                ImageSource.GASKELL.equals(key.source) ||
                ImageSource.CORRECTED.equals(key.source))
        {
            if (config.imageType == ImageType.MSI_IMAGE)
                return new MSIImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.AMICA_IMAGE)
                return new AmicaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.FC_IMAGE)
                return new FcImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.PHOBOS_IMAGE)
                return new PhobosImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.DEIMOS_IMAGE)
                return new DeimosImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.OSIRIS_IMAGE)
                return new OsirisImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.SATURN_MOON_IMAGE)
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.SSI_GASPRA_IMAGE)
                return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.SSI_IDA_IMAGE)
                return new SSIIdaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (config.imageType == ImageType.MSI_MATHILDE_IMAGE)
                return new MSIMathildeImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else
                return null;
        }
        else if (ImageSource.LOCAL_PERSPECTIVE.equals(key.source))
        {
            return new CustomPerspectiveImage(key, smallBodyModel, loadPointingOnly, rootFolder);
        }
        else
        {
            return new CylindricalImage(key, smallBodyModel);
        }
    }

    static public SmallBodyModel createSmallBodyModel(ModelConfig config)
    {
        String name = config.name;
        String author = config.author;

        if (GASKELL.equals(author) || EXPERIMENTAL.equals(author))
        {
            if (EROS.equals(name))
                return new Eros(config);
            else if (ITOKAWA.equals(name))
                return new Itokawa(config);
            else if (TEMPEL_1.equals(name))
            {
                String[] names = {
                        name + " low"
                };
                String[] paths = {
                        config.pathOnServer + "/ver64q.vtk.gz",
                };

                return new SimpleSmallBody(config, names, paths, true);
            }
            else
            {
                String[] names = {
                        name + " low",
                        name + " med",
                        name + " high",
                        name + " very high"
                };
                String[] paths = {
                        config.pathOnServer + "/ver64q.vtk.gz",
                        config.pathOnServer + "/ver128q.vtk.gz",
                        config.pathOnServer + "/ver256q.vtk.gz",
                        config.pathOnServer + "/ver512q.vtk.gz"
                };

                return new SimpleSmallBody(config, names, paths, true);
            }
        }
        else if (THOMAS.equals(author))
        {
            if (EROS.equals(name))
                return new ErosThomas(config);
            else if (VESTA.equals(name))
                return new VestaOld(config);
        }
        else if (JORDA.equals(author))
        {
            if (LUTETIA.equals(name))
                return new Lutetia(config);
        }
        else if (CUSTOM.equals(author))
        {
            return new CustomShapeModel(config);
        }

        String imageMap = null;
        if (config.hasImageMap)
            imageMap = (new File(config.pathOnServer)).getParent() + "/image_map.png";

        return new SimpleSmallBody(config, imageMap, true);
    }

    static public Graticule createGraticule(SmallBodyModel smallBodyModel)
    {
        ModelConfig config = smallBodyModel.getModelConfig();
        String author = config.author;

        if (GASKELL.equals(author) && smallBodyModel.getNumberResolutionLevels() == 4)
        {
            String[] graticulePaths = new String[]{
                    config.pathOnServer + "/coordinate_grid_res0.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res1.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res2.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            return new Graticule(smallBodyModel, graticulePaths, config.useAPLServer);
        }
        else if (CUSTOM.equals(author))
        {
            return new CustomGraticule(smallBodyModel);
        }

        return new Graticule(smallBodyModel);
    }

    static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

    static public NISSpectraCollection createSpectralModel(SmallBodyModel smallBodyModel)
    {
        return new NISSpectraCollection(smallBodyModel);
    }

    static public HashMap<String, Model> createLidarModels(SmallBodyModel smallBodyModel)
    {
        HashMap<String, Model> models = new HashMap<String, Model>();

        models.put(ModelNames.LIDAR_BROWSE, new LidarBrowseDataCollection(smallBodyModel));
        models.put(ModelNames.LIDAR_SEARCH, new LidarSearchDataCollection(smallBodyModel));

        return models;
    }
}
