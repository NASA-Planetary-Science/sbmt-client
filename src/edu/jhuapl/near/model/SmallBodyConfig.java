package edu.jhuapl.near.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.jhuapl.near.lidar.OlaCubesGenerator;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.Image.SpectralMode;
import edu.jhuapl.near.query.FixedListQuery;
import edu.jhuapl.near.query.GenericPhpQuery;
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
    // Flag for beta mode
    public static boolean betaMode = false;

    // Names of built-in small body models
    static public enum ShapeModelBody
    {
        EROS("Eros"),
        ITOKAWA("Itokawa"),
        VESTA("Vesta"),
        CERES("Ceres"),
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
        LORRI("LORRI"),
        MVIC("MVIC"),
        CARRY("Carry"),
        DLR("DLR");

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
        MAPCAM("MAPCAM"),
        POLYCAM("POLYCAM"),
        IMAGING_DATA("Imaging Data"),
        MVIC("MVIC"),
        LEISA("LEISA"),
        LORRI("LORRI"),
        GENERIC("GENERIC");

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
        FCCERES_IMAGE,
        PHOBOS_IMAGE,
        DEIMOS_IMAGE,
        OSIRIS_IMAGE,
        SATURN_MOON_IMAGE,
        SSI_GASPRA_IMAGE,
        SSI_IDA_IMAGE,
        MSI_MATHILDE_IMAGE,
        MVIC_JUPITER_IMAGE,
        LEISA_JUPITER_IMAGE,
        LORRI_IMAGE,
        POLYCAM_IMAGE,
        MAPCAM_IMAGE,
        GENERIC_IMAGE
    }

    static public final int LEISA_NBANDS = 256;
    static public final int MVIC_NBANDS = 4;

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
        c.rootDirOnServer = "/GASKELL/EROS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasImageMap = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/EROS/MSI", "EROS"),
                        ImageType.MSI_IMAGE,
                        new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                        Instrument.MSI
                        )
        };

        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasSpectralData = true;
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
        c.imageSearchDefaultMaxSpacecraftDistance = 100.0;
        c.imageSearchDefaultMaxResolution = 50.0;
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
        c.rootDirOnServer = "/THOMAS/EROS";
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
        c.rootDirOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        configArray.add(c);

        // Eros NAV
        c = c.clone();
        c.dataUsed = ShapeModelDataUsed.LIDAR_BASED;
        c.author = ShapeModelAuthor.EROSNAV;
        c.rootDirOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        configArray.add(c);

        // Gaskell Itokawa
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.ITOKAWA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/ITOKAWA";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/ITOKAWA/AMICA", "AMICA"),
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
        c.rootDirOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        configArray.add(c);

        // Gaskell Phobos
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/PHOBOS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/PHOBOS/IMAGING", "PHOBOS"),
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
        configArray.add(c);

        // Thomas Phobos
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOBOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";
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
            c.rootDirOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/PHOBOSEXPERIMENTAL/IMAGING", "PHOBOSEXP"),
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
            c.imageSearchDefaultMaxSpacecraftDistance = 12000.0;
            c.imageSearchDefaultMaxResolution = 300.0;
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
            c.rootDirOnServer = "/NEWHORIZONS/JUPITER/shape_res0.vtk.gz";
            c.hasColoringData = false;
            c.hasImageMap = false;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/JUPITER/IMAGING", "JUPITER"),
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
            configArray.add(c);

            c = c.clone();
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
                            new GenericPhpQuery("/NEWHORIZONS/CALLISTO/IMAGING", "CALLISTO"),
                            ImageType.LORRI_IMAGE,
                            new ImageSource[]{ImageSource.SPICE},
                            Instrument.LORRI
                            )
            };

            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.EUROPA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.JUPITER;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/EUROPA/shape_res0.vtk.gz";
            c.hasImageMap = true;

            // imaging instruments
            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/NEWHORIZONS/EUROPA/IMAGING", "EUROPA"),
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
            configArray.add(c);

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
                            new GenericPhpQuery("/NEWHORIZONS/GANYMEDE/IMAGING", "GANYMEDE"),
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
             configArray.add(c);

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
                            new GenericPhpQuery("/NEWHORIZONS/IO/IMAGING", "IO"),
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
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.AMALTHEA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.JUPITER;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MIMAS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/MIMAS";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/MIMAS/IMAGING"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PHOEBE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/PHOEBE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;


        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/PHOEBE/IMAGING"),
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
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.CERES;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.MAIN_BELT;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.rootDirOnServer = "/GASKELL/CERES";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
            c.hasMapmaker = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/CERES/FC", "Ceres"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/VESTA";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;
        c.hasMapmaker = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new GenericPhpQuery("/GASKELL/VESTA/FC", "FC"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.VESTA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/VESTA_OLD";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/IDA/SSI"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.IDA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/GASPRA/SSI"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GASPRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MATHILDE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/THOMAS/MATHILDE/MSI"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DEIMOS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.MARS;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/DEIMOS/DEIMOS.vtk.gz";
        c.hasImageMap = true;
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.DEIMOS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.MARS;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.EXPERIMENTAL;
            c.rootDirOnServer = "/THOMAS/DEIMOSEXPERIMENTAL/DEIMOS.vtk.gz";
            c.hasImageMap = true;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/THOMAS/DEIMOSEXPERIMENTAL/IMAGING", "DEIMOS"),
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
        c = new SmallBodyConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.JANUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/JANUS/s10janus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EPIMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HALLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/HALLEY/1682q1halley.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.LARISSA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PROTEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.NEPTUNE;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PROMETHEUS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/PROMETHEUS/s16prometheus.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PANDORA;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.STOOKE;
        c.rootDirOnServer = "/STOOKE/PANDORA/s17pandora.llr.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GEOGRAPHOS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KY26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.BACCHUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KLEOPATRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TOUTATIS_LOW_RES;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/TOUTATIS/4179toutatis.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TOUTATIS_HIGH_RES;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CASTALIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._52760_1998_ML14;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/52760/52760.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.GOLEVKA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.YORP;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/YORP/yorp.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.WT24;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/WT24/wt24.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.SK;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/SK/sk.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.RASHALOM;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/RASHALOM/rashalom.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.NEREUS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/NEREUS/Nereus_alt1.mod.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.MITHRA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/MITHRA/Mithra.v1.PA.prograde.mod.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KW4A;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4A/kw4a.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.KW4B;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/KW4B/kw4b.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HW1;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/HW1/1996hw1.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.EV5;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/EV5/2008ev5.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CE26;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/CE26/ce26.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.CCALPHA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/CCALPHA/1994CC_nominal.mod.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.BETULIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/BETULIA/betulia.obj.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._1950DAPROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DAPROGRADE/1950DA_ProgradeModel.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody._1950DARETROGRADE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.HUDSON;
        c.rootDirOnServer = "/HUDSON/1950DARETROGRADE/1950DA_RetrogradeModel.wf.gz";
        c.hasColoringData = false;
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.RQ36;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.NEO;
        c.dataUsed = ShapeModelDataUsed.RADAR_BASED;
        c.author = ShapeModelAuthor.NOLAN;
        c.rootDirOnServer = "/NOLAN/BENNU/101955bennu.obj.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.ENHANCED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "V2";
            c.rootDirOnServer = "/GASKELL/RQ36";
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

        //PolyCam, MapCam
        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "V3 Image";
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
            c.lidarSearchDataSourceMap = new LinkedHashMap<String, String>();
            c.lidarSearchDataSourceMap.put("Default", "/GASKELL/RQ36_V3/OLA/cubes");
            c.lidarBrowseXYZIndices = OlaCubesGenerator.xyzIndices;
            c.lidarBrowseSpacecraftIndices = OlaCubesGenerator.scIndices;
            c.lidarBrowseIsSpacecraftInSphericalCoordinates = false;
            c.lidarBrowseTimeIndex = 26;
            c.lidarBrowseNoiseIndex = 62;
            c.lidarBrowseOutgoingIntensityIndex = 98;
            c.lidarBrowseReceivedIntensityIndex = 106;
            c.lidarBrowseIntensityEnabled = true;
            c.lidarBrowseFileListResourcePath = "/GASKELL/RQ36_V3/OLA/allOlaFiles.txt";
            c.lidarBrowseNumberHeaderLines = 0;
            c.lidarBrowseIsInMeters = true;
            c.lidarBrowseIsBinary = true;
            c.lidarBrowseBinaryRecordSize = 186;
            c.lidarOffsetScale = 0.0005;
            c.lidarInstrumentName = Instrument.OLA;
            configArray.add(c);

            c.lidarSearchDataSourceMap.put("TreeBased", "/GASKELL/RQ36_V3/OLA/tree");
            c.hasTreeBasedLidarSearch=true; // enable tree-based lidar searching
        }

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.RQ36;
            c.type = ShapeModelType.ASTEROID;
            c.population = ShapeModelPopulation.NEO;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.version = "V4 Image";
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
                            new GenericPhpQuery("/GASKELL/RQ36_V4/POLYCAM", "RQ36V4_POLY"),
                            ImageType.POLYCAM_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.POLYCAM
                            ),
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new GenericPhpQuery("/GASKELL/RQ36_V4/MAPCAM", "RQ36V4_MAP"),
                            ImageType.MAPCAM_IMAGE,
                            new ImageSource[]{ImageSource.GASKELL, ImageSource.SPICE},
                            Instrument.MAPCAM
                            )
            };
            c.density = 1.26;
            c.useMinimumReferencePotential = true;
            c.rotationRate = 0.0004061303295118512;
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
            c.rootDirOnServer = "/GASKELL/LUTETIA";
            c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
            c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

            c.imagingInstruments = new ImagingInstrument[] {
                    new ImagingInstrument(
                            SpectralMode.MONO,
                            new FixedListQuery("/GASKELL/LUTETIA/IMAGING"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.LUTETIA;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.STEINS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.JORDA;
        c.rootDirOnServer = "/JORDA/STEINS/steins_cart.plt.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DIONE;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/DIONE";
        c.smallBodyLabelPerResolutionLevel = DEFAULT_GASKELL_LABELS_PER_RESOLUTION;
        c.smallBodyNumberOfPlatesPerResolutionLevel = DEFAULT_GASKELL_NUMBER_PLATES_PER_RESOLUTION;

        c.imagingInstruments = new ImagingInstrument[] {
                new ImagingInstrument(
                        SpectralMode.MONO,
                        new FixedListQuery("/GASKELL/DIONE/IMAGING"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TETHYS;
        c.type = ShapeModelType.PLANETS_AND_SATELLITES;
        c.population = ShapeModelPopulation.SATURN;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.GASKELL;
        c.rootDirOnServer = "/GASKELL/TETHYS";
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
            c.rootDirOnServer = "/GASKELL/HYPERION";
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
            c.rootDirOnServer = "/GASKELL/RHEA";
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
        c.rootDirOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.TEMPEL_1;
            c.type = ShapeModelType.COMETS;
            c.population = null;
            c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
            c.author = ShapeModelAuthor.GASKELL;
            c.rootDirOnServer = "/GASKELL/TEMPEL1";
            configArray.add(c);
        }

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.TEMPEL_1;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/TEMPEL1/tempel1_cart.t1.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.WILD_2;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.DUXBURY;
        c.rootDirOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HARTLEY;
        c.type = ShapeModelType.COMETS;
        c.population = null;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.THOMAS;
        c.rootDirOnServer = "/THOMAS/HARTLEY/hartley2_2012_cart.plt.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
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
                            new GenericPhpQuery("/GASKELL/67P/IMAGING", "67P"),
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
            c.imagingInstruments[0].searchQuery = new GenericPhpQuery("/DLR/67P/IMAGING", "67P_DLR");
            c.smallBodyLabelPerResolutionLevel = new String[]{
                    "17442 plates ", "72770 plates ", "298442 plates ", "1214922 plates ",
                    "4895631 plates ", "16745283 plates "
            };
            c.smallBodyNumberOfPlatesPerResolutionLevel = new int[]{
                    17442, 72770, 298442, 1214922, 4895631, 16745283
            };
            configArray.add(c);

            // 67P_V2
            c = new SmallBodyConfig();
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
                            new GenericPhpQuery("/GASKELL/67P_V2/IMAGING", "67P_V2"),
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
            c = new SmallBodyConfig();
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
                            new GenericPhpQuery("/GASKELL/67P_V3/IMAGING", "67P_V3"),
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

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.PALLAS;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/PALLAS/pallas.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.DAPHNE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/DAPHNE/daphne.obj.gz";
        configArray.add(c);

        c = new SmallBodyConfig();
        c.body = ShapeModelBody.HERMIONE;
        c.type = ShapeModelType.ASTEROID;
        c.population = ShapeModelPopulation.MAIN_BELT;
        c.dataUsed = ShapeModelDataUsed.IMAGE_BASED;
        c.author = ShapeModelAuthor.CARRY;
        c.rootDirOnServer = "/CARRY/HERMIONE/hermione.obj.gz";
        configArray.add(c);

        if (Configuration.isAPLVersion())
        {
            c = new SmallBodyConfig();
            c.body = ShapeModelBody.PLUTO;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
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
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
//            c.pathOnServer = "/NEWHORIZONS/CHARON/shape_res0.vtk.gz";
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

            c = c.clone();
            c.body = ShapeModelBody.HYDRA;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
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
            configArray.add(c);

            c = c.clone();
            c.body = ShapeModelBody.NIX;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
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

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.KERBEROS;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/KERBEROS/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);

            c = new SmallBodyConfig();
            c.body = ShapeModelBody.STYX;
            c.type = ShapeModelType.PLANETS_AND_SATELLITES;
            c.population = ShapeModelPopulation.PLUTO;
            c.dataUsed = null;
            c.author = null;
            c.rootDirOnServer = "/NEWHORIZONS/STYX/shape_res0.vtk.gz";
            c.hasColoringData = false;
            configArray.add(c);
        }
    }

    public ShapeModelBody body; // e.g. EROS or ITOKAWA
    public ShapeModelType type; // e.g. asteroid, comet, satellite
    public ShapeModelPopulation population; // e.g. Mars for satellites or main belt for asteroids
    public ShapeModelDataUsed dataUsed; // e.g. images, radar, lidar, or enhanced
    public ShapeModelAuthor author; // e.g. Gaskell
    public String version; // e.g. 2.0
    public String rootDirOnServer;
    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1
    public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1
    public boolean useMinimumReferencePotential = false; // uses average otherwise
    public double density = 0.0; // in units g/cm^3
    public double rotationRate = 0.0; // in units radians/sec

    public boolean hasColoringData = true;
    public boolean hasImageMap = false;
    public ImagingInstrument[] imagingInstruments = {};
    public boolean hasLidarData = false;
    public boolean hasMapmaker = false;
    public boolean hasBigmap = false;
    public boolean hasSpectralData = false;
    public boolean hasLineamentData = false;
    public boolean hasCustomBodyCubeSize = false;

    // if hasCustomBodyCubeSize is true, the following must be filled in and valid
    public double customBodyCubeSize; // km

    // if spectralModes is not empty, the following must be filled in
    public Date imageSearchDefaultStartDate;
    public Date imageSearchDefaultEndDate;
    public String[] imageSearchFilterNames;
    public String[] imageSearchUserDefinedCheckBoxesNames;
    public double imageSearchDefaultMaxSpacecraftDistance;
    public double imageSearchDefaultMaxResolution;

    // if hasLidarData is true, the following must be filled in
    public Date lidarSearchDefaultStartDate;
    public Date lidarSearchDefaultEndDate;
    public Map<String, String> lidarSearchDataSourceMap;
    public int[] lidarBrowseXYZIndices;
    public int[] lidarBrowseSpacecraftIndices;
    public int lidarBrowseOutgoingIntensityIndex;
    public int lidarBrowseReceivedIntensityIndex;
    public boolean lidarBrowseIntensityEnabled = false;
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

    public boolean hasTreeBasedLidarSearch=false;

    protected SmallBodyConfig clone()
    {
        SmallBodyConfig c = new SmallBodyConfig();
        c.body = this.body;
        c.type = this.type;
        c.population = this.population;
        c.dataUsed = this.dataUsed;
        c.author = this.author;
        c.version = this.version;
        c.rootDirOnServer = this.rootDirOnServer;
        c.hasColoringData = this.hasColoringData;
        c.hasImageMap = this.hasImageMap;

        // deep clone imaging instruments
        if (this.imagingInstruments != null)
        {
            int length = this.imagingInstruments.length;
            c.imagingInstruments = new ImagingInstrument[length];
            for (int i = 0; i < length; i++)
                c.imagingInstruments[i] = this.imagingInstruments[i].clone();
        }

        c.hasLidarData = this.hasLidarData;
        c.hasMapmaker = this.hasMapmaker;
        c.hasBigmap = this.hasBigmap;
        c.density = this.density;
        c.rotationRate = this.rotationRate;
        c.useMinimumReferencePotential = this.useMinimumReferencePotential;
        c.hasSpectralData = this.hasSpectralData;
        c.hasLineamentData = this.hasLineamentData;
        c.hasCustomBodyCubeSize = this.hasCustomBodyCubeSize;
        c.customBodyCubeSize = this.customBodyCubeSize;
        if (this.smallBodyLabelPerResolutionLevel != null)
            c.smallBodyLabelPerResolutionLevel = this.smallBodyLabelPerResolutionLevel.clone();
        if (this.smallBodyNumberOfPlatesPerResolutionLevel != null)
            c.smallBodyNumberOfPlatesPerResolutionLevel = this.smallBodyNumberOfPlatesPerResolutionLevel.clone();

        if (this.imagingInstruments != null && this.imagingInstruments.length > 0)
        {
            c.imagingInstruments = this.imagingInstruments.clone();
            c.imageSearchDefaultStartDate = (Date)this.imageSearchDefaultStartDate.clone();
            c.imageSearchDefaultEndDate = (Date)this.imageSearchDefaultEndDate.clone();
            c.imageSearchFilterNames = this.imageSearchFilterNames.clone();
            c.imageSearchUserDefinedCheckBoxesNames = this.imageSearchUserDefinedCheckBoxesNames.clone();
            c.imageSearchDefaultMaxSpacecraftDistance = this.imageSearchDefaultMaxSpacecraftDistance;
            c.imageSearchDefaultMaxResolution = this.imageSearchDefaultMaxResolution;
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
            c.lidarBrowseOutgoingIntensityIndex = this.lidarBrowseOutgoingIntensityIndex;
            c.lidarBrowseReceivedIntensityIndex = this.lidarBrowseReceivedIntensityIndex;
            c.lidarBrowseIntensityEnabled = this.lidarBrowseIntensityEnabled;
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
             if (version != null)
                 path += " (" + version + ")";
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
         {
             if (version == null)
                 return author + "/" + body;
             else
                 return author + "/" + body + " (" + version + ")";
         }
         else
             return body.toString();
     }

     public String getShapeModelName()
     {
         if (author == ShapeModelAuthor.CUSTOM)
             return customName;
         else
         {
             String ver = "";
             if (version != null)
                 ver += " (" + version + ")";
             return body.toString() + ver;
         }
     }

    /**
     * Get a SmallBodyConfig of a specific name and author.
     * Note a SmallBodyConfig is uniquely described by its name, author, and version.
     * No two small body configs can have all the same. This version of the function
     * assumes the version is null (unlike the other version in which you can specify
     * the version).
     *
     * @param name
     * @param author
     * @return
     */
    static public SmallBodyConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelAuthor author)
    {
        return getSmallBodyConfig(name, author, null);
    }

    /**
     * Get a SmallBodyConfig of a specific name, author, and version.
     * Note a SmallBodyConfig is uniquely described by its name, author, and version.
     * No two small body configs can have all the same.
     *
     * @param name
     * @param author
     * @param version
     * @return
     */
    static public SmallBodyConfig getSmallBodyConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
    {
        for (SmallBodyConfig config : builtInSmallBodyConfigs)
        {
            if (config.body == name && config.author == author &&
                    ((config.version == null && version == null) || (version != null && version.equals(config.version)))
                    )
                return config;
        }

        System.err.println("Error: Cannot find SmallBodyConfig with name " + name +
                " and author " + author + " and version " + version);

        return null;
    }
}
