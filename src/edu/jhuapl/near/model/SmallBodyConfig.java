package edu.jhuapl.near.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.query.FixedListQuery;
import edu.jhuapl.near.query.GenericPhpQuery;
import edu.jhuapl.near.query.QueryBase;
import edu.jhuapl.near.util.Configuration;

/**
 * A SmallBodyConfig is a class for storing all which models should be instantiated
 * together with a particular small body. For example some models like Eros
 * have imaging, spectral, and lidar data whereas other models may only have
 * imaging data. This class is also used when creating (to know which tabs
 * to create).
 */
public class SmallBodyConfig
{
    // Names of built-in small body models
    static public enum ShapeModelBody
    {
        EROS("Eros"),
        ITOKAWA("Itokawa"),
        VESTA("Vesta"),
        MIMAS("Mimas"),
        PHOEBE("Phoebe"),
        PHOBOS("Phobos"),
        RQ36("Bennu"),
        DIONE("Dione"),
        RHEA("Rhea"),
        TETHYS("Tethys"),
        LUTETIA("Lutetia"),
        IDA("Ida"),
        GASPRA("Gaspra"),
        MATHILDE("Mathilde"),
        DEIMOS("Deimos"),
        JANUS("Janus"),
        EPIMETHEUS("Epimetheus"),
        HYPERION("Hyperion"),
        TEMPEL_1("Tempel 1"),
        HALLEY("Halley"),
        JUPITER("Jupiter"),
        AMALTHEA("Amalthea"),
        CALLISTO("Callisto"),
        EUROPA("Europa"),
        GANYMEDE("Ganymede"),
        IO("Io"),
        LARISSA("Larissa"),
        PROTEUS("Proteus"),
        PROMETHEUS("Prometheus"),
        PANDORA("Pandora"),
        GEOGRAPHOS("Geographos"),
        KY26("KY26"),
        BACCHUS("Bacchus"),
        KLEOPATRA("Kleopatra"),
        TOUTATIS_LOW_RES("Toutatis (Low Res)"),
        TOUTATIS_HIGH_RES("Toutatis (High Res)"),
        CASTALIA("Castalia"),
        _52760_1998_ML14("52760 (1998 ML14)"),
        GOLEVKA("Golevka"),
        WILD_2("Wild 2"),
        STEINS("Steins"),
        HARTLEY("Hartley"),
        PLUTO("Pluto"),
        CHARON("Charon"),
        HYDRA("Hydra"),
        KERBEROS("Kerberos"),
        NIX("Nix"),
        STYX("Styx"),
        _1950DAPROGRADE("1950 DA Prograde"),
        _1950DARETROGRADE("1950 DA Retrograde"),
        BETULIA("Betulia"),
        CCALPHA("1994 CC Alpha"),
        CE26("CE26 Alpha"),
        EV5("EV5"),
        HW1("1996 HW1"),
        KW4A("KW4 Alpha"),
        KW4B("KW4 Beta"),
        MITHRA("Mithra"),
        NEREUS("Nereus"),
        RASHALOM("Ra-Shalom"),
        SK("SK"),
        WT24("WT24"),
        YORP("YORP"),
        PALLAS("Pallas"),
        DAPHNE("Daphne"),
        HERMIONE("Hermione"),
        _67P("67P");

        final private String str;
        private ShapeModelBody(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    // Types of bodies
    static public enum ShapeModelType
    {
        ASTEROID("Asteroids"),
        PLANETS_AND_SATELLITES("Planets and Satellites"),
        COMETS("Comets");

        final private String str;
        private ShapeModelType(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    // Populations
    static public enum ShapeModelPopulation
    {
        MARS("Mars"),
        JUPITER("Jupiter"),
        SATURN("Saturn"),
        NEPTUNE("Neptune"),
        NEO("Near-Earth"),
        MAIN_BELT("Main Belt"),
        PLUTO("Pluto");

        final private String str;
        private ShapeModelPopulation(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    // Names of authors
    static public enum ShapeModelAuthor
    {
        GASKELL("Gaskell"),
        THOMAS("Thomas"),
        STOOKE("Stooke"),
        HUDSON("Hudson"),
        DUXBURY("Duxbury"),
        OSTRO("Ostro"),
        JORDA("Jorda"),
        NOLAN("Nolan"),
        CUSTOM("Custom"),
        EROSNAV("NAV"),
        EROSNLR("NLR"),
        EXPERIMENTAL("Experimental"),
        CARRY("Carry");

        final private String str;
        private ShapeModelAuthor(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    // Data used to construct shape model (either images, radar, lidar, or enhanced)
    static public enum ShapeModelDataUsed
    {
        IMAGE_BASED("Image Based"),
        RADAR_BASED("Radar Based"),
        LIDAR_BASED("Lidar Based"),
        ENHANCED("Enhanced");

        final private String str;
        private ShapeModelDataUsed(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    // Names of instruments
    static public enum Instrument
    {
        MSI("MSI"),
        NLR("NLR"),
        NIS("NIS"),
        AMICA("AMICA"),
        LIDAR("LIDAR"),
        FC("FC"),
        SSI("SSI"),
        OSIRIS("OSIRIS"),
        OLA("OLA"),
        IMAGING_DATA("Imaging Data"),
        LORRI("LORRI");

        final private String str;
        private Instrument(String str)
        {
            this.str = str;
        }

        @Override
        public String toString()
        {
            return str;
        }
    }

    static public enum ImageType
    {
        MSI_IMAGE,
        AMICA_IMAGE,
        FC_IMAGE,
        PHOBOS_IMAGE,
        DEIMOS_IMAGE,
        OSIRIS_IMAGE,
        SATURN_MOON_IMAGE,
        SSI_GASPRA_IMAGE,
        SSI_IDA_IMAGE,
        MSI_MATHILDE_IMAGE,
        LORRI_IMAGE,
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

    static public final ArrayList<SmallBodyConfig> builtInSmallBodyConfigs = new ArrayList<SmallBodyConfig>();

    static
    {
        ArrayList<SmallBodyConfig> configArray = builtInSmallBodyConfigs;

        // Gaskell Eros
        SmallBodyConfig c = new SmallBodyConfig();
        c.body = ShapeModelBody.EROS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/EROS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasSpectralData = true;
        c.hasLineamentData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2000, 0, 12, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
        c.imageSearchQuery = new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS");
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE};
        c.imageType = ImageType.MSI_IMAGE;
        c.imageInstrumentName = Instrument.MSI;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2001, 1, 13, 0, 0, 0).getTime();
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
        c.lidarInstrumentName = Instrument.NLR;
        configArray.add(c);

        // Thomas Eros
        c = c.clone();
        c.author = ShapeModelAuthor.THOMAS;
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
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelAuthor.EROSNLR;
        c.pathOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelAuthor.EROSNAV;
        c.pathOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        configArray.add(c);

        // Gaskell Itokawa
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/ITOKAWA";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2005, 10, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new GenericPhpQuery("/GASKELL/ITOKAWA/AMICA", "AMICA");
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE, ImageSource.CORRECTED};
        c.imageType = ImageType.AMICA_IMAGE;
        c.imageInstrumentName = Instrument.AMICA;
        c.lidarSearchDefaultStartDate = new GregorianCalendar(2005, 8, 1, 0, 0, 0).getTime();
        c.lidarSearchDefaultEndDate = new GregorianCalendar(2005, 10, 30, 0, 0, 0).getTime();
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
        c.lidarInstrumentName = Instrument.LIDAR;
        configArray.add(c);

        // Ostro Itokawa
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.OSTRO;
        c.pathOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        configArray.add(c);

        // Gaskell Phobos
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/PHOBOS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
        c.imageSearchQuery = new GenericPhpQuery("/GASKELL/PHOBOS/IMAGING", "PHOBOS");
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE};
        c.imageType = ImageType.PHOBOS_IMAGE;
        c.imageInstrumentName = Instrument.IMAGING_DATA;
        configArray.add(c);

        // Thomas Phobos
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";
        configArray.add(c);

        // New Gaskell Phobos (experimental)
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.PHOBOS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.EXPERIMENTAL;
            c.pathOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasPerspectiveImages = true;
            c.hasMapmaker = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 6, 24, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 6, 7, 0, 0, 0).getTime();
            c.imageSearchQuery = new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP");
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
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.PHOBOS_IMAGE;
            c.imageInstrumentName = Instrument.IMAGING_DATA;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.JUPITER;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/JUPITER/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.hasImageMap = false;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2007, 0, 8, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2007, 2, 5, 0, 0, 0).getTime();
            c.imageSearchQuery = new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER");
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 1.0e9;
            c.imageSearchDefaultMaxResolution = 1.0e6;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.SPICE};
            c.imageType = ImageType.LORRI_IMAGE;
            c.imageInstrumentName = Instrument.LORRI;
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.CALLISTO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/CALLISTO/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchQuery = new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO");
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.EUROPA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/EUROPA/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchQuery = new GenericPhpQuery("/NEWHORIZONS/EUROPA/IMAGING", "EUROPA");
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.GANYMEDE;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/GANYMEDE/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchQuery = new GenericPhpQuery("/NEWHORIZONS/GANYMEDE/IMAGING", "GANYMEDE");
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.IO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/IO/shape_res0.vtk.gz";
            c.hasImageMap = true;
            c.imageSearchQuery = new GenericPhpQuery("/NEWHORIZONS/IO/IMAGING", "IO");
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.AMALTHEA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MIMAS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/MIMAS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/GASKELL/MIMAS/IMAGING");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
        c.imageType = ImageType.SATURN_MOON_IMAGE;
        c.imageInstrumentName = Instrument.IMAGING_DATA;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOEBE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/PHOEBE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/GASKELL/PHOEBE/IMAGING");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
        c.imageType = ImageType.SATURN_MOON_IMAGE;
        c.imageInstrumentName = Instrument.IMAGING_DATA;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.VESTA;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/VESTA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasMapmaker = true;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2011, 4, 3, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2012, 7, 27, 0, 0, 0).getTime();
            c.imageSearchQuery = new GenericPhpQuery("/GASKELL/VESTA/FC", "FC");
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
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE};
            c.imageType = ImageType.FC_IMAGE;
            c.imageInstrumentName = Instrument.FC;
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/VESTA_OLD";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1993, 7, 28, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1993, 7, 29, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/THOMAS/IDA/SSI");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.CORRECTED};
        c.imageType = ImageType.SSI_IDA_IMAGE;
        c.imageInstrumentName = Instrument.SSI;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1991, 9, 29, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1991, 9, 30, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/THOMAS/GASPRA/SSI");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.CORRECTED};
        c.imageType = ImageType.SSI_GASPRA_IMAGE;
        c.imageInstrumentName = Instrument.SSI;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MATHILDE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1997, 5, 27, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(1997, 5, 28, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/THOMAS/MATHILDE/MSI");
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
        c.imageInstrumentName = Instrument.MSI;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DEIMOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/DEIMOS/DEIMOS.vtk.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1976, 7, 16, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 7, 10, 0, 0, 0).getTime();
        c.imageSearchQuery = new GenericPhpQuery("/THOMAS/DEIMOS/IMAGING", "DEIMOS");
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
        c.imageSearchImageSources = new ImageSource[]{ImageSource.SPICE, ImageSource.CORRECTED};
        c.imageType = ImageType.DEIMOS_IMAGE;
        c.imageInstrumentName = Instrument.IMAGING_DATA;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HALLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/HALLEY/1682q1halley.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.LARISSA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PROTEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PROMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/PROMETHEUS/s16prometheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PANDORA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.pathOnServer = "/STOOKE/PANDORA/s17pandora.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GEOGRAPHOS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KY26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.BACCHUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KLEOPATRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TOUTATIS_LOW_RES;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS/4179toutatis.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TOUTATIS_HIGH_RES;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CASTALIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._52760_1998_ML14;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/52760/52760.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GOLEVKA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.YORP;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/YORP/yorp.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.WT24;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/WT24/wt24.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.SK;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/SK/sk.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.RASHALOM;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/RASHALOM/rashalom.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.NEREUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/NEREUS/Nereus_alt1.mod.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MITHRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/MITHRA/Mithra.v1.PA.prograde.mod.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KW4A;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/KW4A/kw4a.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KW4B;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/KW4B/kw4b.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HW1;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/HW1/1996hw1.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EV5;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/EV5/2008ev5.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CE26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/CE26/ce26.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CCALPHA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/CCALPHA/1994CC_nominal.mod.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.BETULIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/BETULIA/betulia.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._1950DAPROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/1950DAPROGRADE/1950DA_ProgradeModel.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._1950DARETROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.pathOnServer = "/HUDSON/1950DARETROGRADE/1950DA_RetrogradeModel.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.RQ36;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.NOLAN;
        c.pathOnServer = "/NOLAN/BENNU/101955bennu.obj.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.ENHANCED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/RQ36";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasLidarData = true;
            c.hasMapmaker = true;
            c.lidarSearchDefaultStartDate = new GregorianCalendar(2000, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDefaultEndDate = new GregorianCalendar(2050, 0, 1, 0, 0, 0).getTime();
            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36/OLA/cubes");
            c.lidarBrowseXYZIndices = new int[]{96, 104, 112};
            c.lidarBrowseSpacecraftIndices = new int[]{144, 152, 160};
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 18;
            c.lidarBrowseNoiseIndex = -1;
            c.lidarBrowseFileListResourcePath = "/edu/jhuapl/near/data/OlaLidarFiles.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 168;
            c.lidarOffsetScale = 0.0005;
            c.lidarInstrumentName = Instrument.OLA;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.LUTETIA;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/LUTETIA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2010, 6, 10, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2010, 6, 11, 0, 0, 0).getTime();
            c.imageSearchQuery = new FixedListQuery("/GASKELL/LUTETIA/IMAGING");
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.OSIRIS_IMAGE;
            c.imageInstrumentName = Instrument.OSIRIS;
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.LUTETIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.STEINS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
        c.pathOnServer = "/JORDA/STEINS/steins_cart.plt.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DIONE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/DIONE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasPerspectiveImages = true;
        c.imageSearchDefaultStartDate = new GregorianCalendar(1980, 10, 10, 0, 0, 0).getTime();
        c.imageSearchDefaultEndDate = new GregorianCalendar(2011, 0, 31, 0, 0, 0).getTime();
        c.imageSearchQuery = new FixedListQuery("/GASKELL/DIONE/IMAGING");
        c.imageSearchFilterNames = new String[]{};
        c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
        c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
        c.imageSearchDefaultMaxResolution = 4000.0;
        c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
        c.imageType = ImageType.SATURN_MOON_IMAGE;
        c.imageInstrumentName = Instrument.IMAGING_DATA;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TETHYS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.pathOnServer = "/GASKELL/TETHYS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.HYPERION;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/HYPERION";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            configArray.add(c);
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.RHEA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.SATURN;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/RHEA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HYPERION;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.TEMPEL_1;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/TEMPEL1";
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TEMPEL_1;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/TEMPEL1/tempel1_cart.t1.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.WILD_2;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.DUXBURY;
        c.pathOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HARTLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.pathOnServer = "/THOMAS/HARTLEY/hartley2_2012_cart.plt.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody._67P;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.pathOnServer = "/GASKELL/67P";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasPerspectiveImages = true;
            c.imageSearchDefaultStartDate = new GregorianCalendar(2014, 6, 11, 0, 0, 0).getTime();
            c.imageSearchDefaultEndDate = new GregorianCalendar(2014, 11, 8, 0, 0, 0).getTime();
            c.imageSearchQuery = new FixedListQuery("/GASKELL/67P/IMAGING");
            c.imageSearchFilterNames = new String[]{};
            c.imageSearchUserDefinedCheckBoxesNames = new String[]{};
            c.imageSearchDefaultMaxSpacecraftDistance = 40000.0;
            c.imageSearchDefaultMaxResolution = 4000.0;
            c.imageSearchImageSources = new ImageSource[]{ImageSource.GASKELL};
            c.imageType = ImageType.OSIRIS_IMAGE;
            c.imageInstrumentName = Instrument.OSIRIS;
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PALLAS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.pathOnServer = "/CARRY/PALLAS/pallas.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DAPHNE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.pathOnServer = "/CARRY/DAPHNE/daphne.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HERMIONE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.pathOnServer = "/CARRY/HERMIONE/hermione.obj.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.PLUTO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/PLUTO/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.CHARON;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/CHARON/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.HYDRA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/HYDRA/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.KERBEROS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/KERBEROS/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.NIX;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/NIX/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.STYX;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.pathOnServer = "/NEWHORIZONS/STYX/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);
        }
    }

    public ShapeModelBody body; // e.g. EROS or ITOKAWA
    public ShapeModelType type; // e.g. asteroid, comet, satellite
    public ShapeModelPopulation population; // e.g. Mars for satellites or main belt for asteroids
    public ShapeModelDataUsed dataUsed; // e.g. images, radar, lidar, or enhanced
    public ShapeModelAuthor author; // e.g. Gaskell
    public String pathOnServer;
    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1
    public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1
    public boolean hasColoringData = true;
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
    public Instrument imageInstrumentName = Instrument.IMAGING_DATA;
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
    public Instrument lidarInstrumentName = Instrument.LIDAR;
    public String customName;
    public boolean customTemporary = false;


    protected SmallBodyConfig clone()
    {
        SmallBodyConfig c = new SmallBodyConfig();
        c.body = this.body;
        c.type = this.type;
        c.population = this.population;
        c.dataUsed = this.dataUsed;
        c.author = this.author;
        c.pathOnServer = this.pathOnServer;
        c.hasColoringData = this.hasColoringData;
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
        c.customName = this.customName;
        c.customTemporary = this.customTemporary;
        return c;
    }

    /**
     * Returns model as a path. e.g. "Asteroid > Near-Earth > Eros > Image Based > Gaskell"
     */
     public String getPathRepresentation()
     {
         if (ShapeModelAuthor.CUSTOM == author)
         {
             return ShapeModelAuthor.CUSTOM + " > " + customName;
         }
         else
         {
             String path = type.str;
             if (population != null)
                 path += " > " + population;
             path += " > " + body;
             if (dataUsed != null)
                 path += " > " + dataUsed;
             if (author != null)
                 path += " > " + author;
             return path;
         }
     }

     /**
      * Return a unique name for this model. No other model may have this
      * name. Note that only applies within built-in models or custom models
      * but a custom model can share the name of a built-in one or vice versa.
      * By default simply return the author concatenated with the
      * name if the author is not null or just the name if the author
      * is null.
      * @return
      */
     public String getUniqueName()
     {
         if (ShapeModelAuthor.CUSTOM == author)
             return author + "/" + customName;
         else if (author != null)
             return author + "/" + body;
         else
             return body.toString();
     }

     public String getShapeModelName()
     {
         if (author == ShapeModelAuthor.CUSTOM)
             return customName;
         else
             return body.toString();
     }

    /**
     * Get a SmallBodyConfig of a specific name and author.
     * Note a SmallBodyConfig is uniquely described by its name and author.
     * No two small body configs can have both the same.
     *
     * @param name
     * @param author
     * @return
     */
    static public SmallBodyConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        for (SmallBodyConfig config : builtInSmallBodyConfigs)
        {
            if (config.body == name && config.author == author)
                return config;
        }

        System.err.println("Error: Cannot find SmallBodyConfig with name " + name + " and author " + author);

        return null;
    }
}
