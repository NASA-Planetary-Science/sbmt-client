package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.model.deimos.Deimos;
import edu.jhuapl.near.model.deimos.DeimosImage;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.ErosThomas;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NLRBrowseDataCollection;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection;
import edu.jhuapl.near.model.gaspra.SSIGaspraImage;
import edu.jhuapl.near.model.ida.SSIIdaImage;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.HayLidarBrowseDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarSearchDataCollection;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.lutetia.OsirisImage;
import edu.jhuapl.near.model.mathilde.MSIMathildeImage;
import edu.jhuapl.near.model.phobos.PhobosImage;
import edu.jhuapl.near.model.rq36.RQ36;
import edu.jhuapl.near.model.saturnmoon.SaturnMoonImage;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.model.vesta.Vesta;
import edu.jhuapl.near.model.vesta_old.VestaOld;
import edu.jhuapl.near.query.QueryBase;
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
    static public final String IMAGING_DATA = "Imaging Data";

    static public final ArrayList<ModelConfig> builtInModelConfigs = new ArrayList<ModelConfig>();

    static
    {
        ArrayList<ModelConfig> configArray = builtInModelConfigs;

        // Gaskell Eros
//      c.add(new ModelConfig(EROS, ASTEROID, NEO, IMAGE_BASED, GASKELL, "/GASKELL/EROS", false, true, true, true, true, true));
        ModelConfig c = new ModelConfig();
        c.name = EROS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/EROS";
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        c.hasMapmaker = true;
        c.hasSpectralData = true;
        c.hasLineamentData = true;
        configArray.add(c);

        // Thomas Eros
//      c.add(new ModelConfig(EROS, ASTEROID, NEO, IMAGE_BASED, THOMAS, "/THOMAS/EROS", false, true, true, false, true, true));
        c = c.clone();
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/EROS";
        c.hasMapmaker = false;
        configArray.add(c);

        // Eros NLR
//      c.add(new ModelConfig(EROS, ASTEROID, NEO, LIDAR_BASED, EROSNLR, "/OTHER/EROSNLR/nlrshape.llr2.gz", false, true, true, false, true, true));
        c = c.clone();
        c.dataUsed = LIDAR_BASED;
        c.author = EROSNLR;
        c.pathOnServer = "/OTHER/EROSNLR/nlrshape.llr2.gz";
        configArray.add(c);

        // Eros NAV
//      c.add(new ModelConfig(EROS, ASTEROID, NEO, LIDAR_BASED, EROSNAV, "/OTHER/EROSNAV/navplate.obj.gz", false, true, true, false, true, true));
        c = c.clone();
        c.dataUsed = LIDAR_BASED;
        c.author = EROSNAV;
        c.pathOnServer = "/OTHER/EROSNAV/navplate.obj.gz";
        configArray.add(c);

        // Gaskell Itokawa
//      c.add(new ModelConfig(ITOKAWA, ASTEROID, NEO, IMAGE_BASED, GASKELL, "/GASKELL/ITOKAWA", false, true, true, false, false, false));
        c = new ModelConfig();
        c.name = ITOKAWA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/ITOKAWA";
        c.hasPerspectiveImages = true;
        c.hasLidarData = true;
        configArray.add(c);

        // Ostro Itokawa
//      c.add(new ModelConfig(ITOKAWA, ASTEROID, NEO, RADAR_BASED, OSTRO, "/HUDSON/ITOKAWA/25143itokawa.obj.gz"));
        c = new ModelConfig();
        c.name = ITOKAWA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = OSTRO;
        c.pathOnServer = "/HUDSON/ITOKAWA/25143itokawa.obj.gz";
        configArray.add(c);

        // Gaskell Phobos
//      c.add(new ModelConfig(PHOBOS, SATELLITES, MARS, IMAGE_BASED, GASKELL, "/GASKELL/PHOBOS", false, true));
        c = new ModelConfig();
        c.name = PHOBOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/PHOBOS";
        c.hasPerspectiveImages = true;
        configArray.add(c);

        // Thomas Phobos
//      c.add(new ModelConfig(PHOBOS, SATELLITES, MARS, IMAGE_BASED, THOMAS, "/THOMAS/PHOBOS/m1phobos.llr.gz"));
        c = new ModelConfig();
        c.name = PHOBOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/PHOBOS/m1phobos.llr.gz";
        c.hasPerspectiveImages = true;
        configArray.add(c);

        // New Gaskell Phobos (experimental)
//      c.add(new ModelConfig(PHOBOS, SATELLITES, MARS, IMAGE_BASED, EXPERIMENTAL, "/GASKELL/PHOBOSEXPERIMENTAL", false, true));
        c = new ModelConfig();
        c.name = PHOBOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = EXPERIMENTAL;
        c.pathOnServer = "/GASKELL/PHOBOSEXPERIMENTAL";
        c.hasPerspectiveImages = true;
        configArray.add(c);

//      c.add(new ModelConfig(AMALTHEA, SATELLITES, JUPITER, IMAGE_BASED, STOOKE, "/STOOKE/AMALTHEA/j5amalthea.llr.gz"));
        c = new ModelConfig();
        c.name = AMALTHEA;
        c.type = SATELLITES;
        c.population = JUPITER;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/AMALTHEA/j5amalthea.llr.gz";
        configArray.add(c);

//      c.add(new ModelConfig(MIMAS, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/MIMAS", false, true));
        c = new ModelConfig();
        c.name = MIMAS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/MIMAS";
        configArray.add(c);

//      c.add(new ModelConfig(PHOEBE, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/PHOEBE", false, true));
        c = new ModelConfig();
        c.name = PHOEBE;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = GASKELL;
        c.pathOnServer = "/GASKELL/PHOEBE";
        configArray.add(c);

//      if (Configuration.isAPLVersion())
//      {
//          c.add(new ModelConfig(VESTA, ASTEROID, MAIN_BELT, IMAGE_BASED, GASKELL, "/GASKELL/VESTA", false, true));
//      }
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = VESTA;
            c.type = ASTEROID;
            c.population = MAIN_BELT;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/VESTA";
            configArray.add(c);
        }

//        c.add(new ModelConfig(VESTA, ASTEROID, MAIN_BELT, IMAGE_BASED, THOMAS, "/THOMAS/VESTA_OLD"));
        c = new ModelConfig();
        c.name = VESTA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/VESTA_OLD";
        configArray.add(c);

        //        c.add(new ModelConfig(IDA, ASTEROID, MAIN_BELT, IMAGE_BASED, THOMAS, "/THOMAS/IDA/243ida.llr.gz", true, true));
        c = new ModelConfig();
        c.name = IDA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        configArray.add(c);

        //        c.add(new ModelConfig(IDA, ASTEROID, MAIN_BELT, IMAGE_BASED, STOOKE, "/STOOKE/IDA/243ida.llr.gz", true));
        c = new ModelConfig();
        c.name = IDA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/IDA/243ida.llr.gz";
        c.hasImageMap = true;
        configArray.add(c);

//        c.add(new ModelConfig(GASPRA, ASTEROID, MAIN_BELT, IMAGE_BASED, THOMAS, "/THOMAS/GASPRA/951gaspra.llr.gz", true, true));
        c = new ModelConfig();
        c.name = GASPRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        configArray.add(c);

//        c.add(new ModelConfig(GASPRA, ASTEROID, MAIN_BELT, IMAGE_BASED, STOOKE, "/STOOKE/GASPRA/951gaspra.llr.gz", true));
        c = new ModelConfig();
        c.name = GASPRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/GASPRA/951gaspra.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        configArray.add(c);

//        c.add(new ModelConfig(MATHILDE, ASTEROID, MAIN_BELT, IMAGE_BASED, THOMAS, "/THOMAS/MATHILDE/253mathilde.llr.gz", true, true));
        c = new ModelConfig();
        c.name = MATHILDE;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/MATHILDE/253mathilde.llr.gz";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        configArray.add(c);

//        c.add(new ModelConfig(DEIMOS, SATELLITES, MARS, IMAGE_BASED, THOMAS, "/THOMAS/DEIMOS", true, true));
        c = new ModelConfig();
        c.name = DEIMOS;
        c.type = SATELLITES;
        c.population = MARS;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/DEIMOS";
        c.hasImageMap = true;
        c.hasPerspectiveImages = true;
        configArray.add(c);

        //        c.add(new ModelConfig(JANUS, SATELLITES, SATURN, IMAGE_BASED, THOMAS, "/THOMAS/JANUS/s10janus.llr.gz"));
        c = new ModelConfig();
        c.name = JANUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/JANUS/s10janus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(JANUS, SATELLITES, SATURN, IMAGE_BASED, STOOKE, "/STOOKE/JANUS/s10janus.llr.gz"));
        c = new ModelConfig();
        c.name = JANUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/JANUS/s10janus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(EPIMETHEUS, SATELLITES, SATURN, IMAGE_BASED, THOMAS, "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz"));
        c = new ModelConfig();
        c.name = EPIMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(EPIMETHEUS, SATELLITES, SATURN, IMAGE_BASED, STOOKE, "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz"));
        c = new ModelConfig();
        c.name = EPIMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(HALLEY, COMETS, null, IMAGE_BASED, STOOKE, "/STOOKE/HALLEY/1682q1halley.llr.gz"));
        c = new ModelConfig();
        c.name = HALLEY;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/HALLEY/1682q1halley.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(LARISSA, SATELLITES, NEPTUNE, IMAGE_BASED, STOOKE, "/STOOKE/LARISSA/n7larissa.llr.gz"));
        c = new ModelConfig();
        c.name = LARISSA;
        c.type = SATELLITES;
        c.population = NEPTUNE;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/LARISSA/n7larissa.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(PROTEUS, SATELLITES, NEPTUNE, IMAGE_BASED, STOOKE, "/STOOKE/PROTEUS/n8proteus.llr.gz"));
        c = new ModelConfig();
        c.name = PROTEUS;
        c.type = SATELLITES;
        c.population = NEPTUNE;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PROTEUS/n8proteus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(PROMETHEUS, SATELLITES, SATURN, IMAGE_BASED, STOOKE, "/STOOKE/PROMETHEUS/s16prometheus.llr.gz"));
        c = new ModelConfig();
        c.name = PROMETHEUS;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PROMETHEUS/s16prometheus.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(PANDORA, SATELLITES, SATURN, IMAGE_BASED, STOOKE, "/STOOKE/PANDORA/s17pandora.llr.gz"));
        c = new ModelConfig();
        c.name = PANDORA;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = STOOKE;
        c.pathOnServer = "/STOOKE/PANDORA/s17pandora.llr.gz";
        configArray.add(c);

//        c.add(new ModelConfig(GEOGRAPHOS, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz"));
        c = new ModelConfig();
        c.name = GEOGRAPHOS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(KY26, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/KY26/1998ky26.obj.gz"));
        c = new ModelConfig();
        c.name = KY26;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/KY26/1998ky26.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(BACCHUS, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/BACCHUS/2063bacchus.obj.gz"));
        c = new ModelConfig();
        c.name = BACCHUS;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/BACCHUS/2063bacchus.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(KLEOPATRA, ASTEROID, MAIN_BELT, RADAR_BASED, HUDSON, "/HUDSON/KLEOPATRA/216kleopatra.obj.gz"));
        c = new ModelConfig();
        c.name = KLEOPATRA;
        c.type = ASTEROID;
        c.population = MAIN_BELT;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/KLEOPATRA/216kleopatra.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(TOUTATIS_LOW_RES, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/TOUTATIS/4179toutatis.obj.gz"));
        c = new ModelConfig();
        c.name = TOUTATIS_LOW_RES;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS/4179toutatis.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(TOUTATIS_HIGH_RES, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz"));
        c = new ModelConfig();
        c.name = TOUTATIS_HIGH_RES;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(CASTALIA, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/CASTALIA/4769castalia.obj.gz"));
        c = new ModelConfig();
        c.name = CASTALIA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/CASTALIA/4769castalia.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(_52760_1998_ML14, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/52760/52760.obj.gz"));
        c = new ModelConfig();
        c.name = _52760_1998_ML14;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/52760/52760.obj.gz";
        configArray.add(c);

//        c.add(new ModelConfig(GOLEVKA, ASTEROID, NEO, RADAR_BASED, HUDSON, "/HUDSON/GOLEVKA/6489golevka.obj.gz"));
        c = new ModelConfig();
        c.name = GOLEVKA;
        c.type = ASTEROID;
        c.population = NEO;
        c.dataUsed = RADAR_BASED;
        c.author = HUDSON;
        c.pathOnServer = "/HUDSON/GOLEVKA/6489golevka.obj.gz";
        configArray.add(c);

//        if (Configuration.isAPLVersion())
//        {
//            c.add(new ModelConfig(RQ36, ASTEROID, NEO, ENHANCED, GASKELL, "/GASKELL/RQ36", false, false, false, true, false, false));
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = RQ36;
            c.type = ASTEROID;
            c.population = NEO;
            c.dataUsed = ENHANCED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/RQ36";
            c.hasMapmaker = true;
            configArray.add(c);
        }

//            c.add(new ModelConfig(LUTETIA, ASTEROID, MAIN_BELT, IMAGE_BASED, GASKELL, "/GASKELL/LUTETIA", false, true));
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = LUTETIA;
            c.type = ASTEROID;
            c.population = MAIN_BELT;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/LUTETIA";
            c.hasPerspectiveImages = true;
            configArray.add(c);
        }

//            c.add(new ModelConfig(DIONE, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/DIONE", false, true));
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = DIONE;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/DIONE";
            c.hasPerspectiveImages = true;
            configArray.add(c);
        }

//            c.add(new ModelConfig(RHEA, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/RHEA"));
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = RHEA;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/RHEA";
            configArray.add(c);
        }

//            c.add(new ModelConfig(TETHYS, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/TETHYS"));
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = TETHYS;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/TETHYS";
            configArray.add(c);
        }

//            c.add(new ModelConfig(HYPERION, SATELLITES, SATURN, IMAGE_BASED, GASKELL, "/GASKELL/HYPERION"));
//        }
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = HYPERION;
            c.type = SATELLITES;
            c.population = SATURN;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/HYPERION";
            configArray.add(c);
        }

//        c.add(new ModelConfig(HYPERION, SATELLITES, SATURN, IMAGE_BASED, THOMAS, "/THOMAS/HYPERION/s7hyperion.llr.gz"));
        c = new ModelConfig();
        c.name = HYPERION;
        c.type = SATELLITES;
        c.population = SATURN;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/HYPERION/s7hyperion.llr.gz";
        configArray.add(c);

//        if (Configuration.isAPLVersion())
//        {
//            c.add(new ModelConfig(TEMPEL_1, COMETS, null, IMAGE_BASED, GASKELL, "/GASKELL/TEMPEL1"));
//        }
        if (Configuration.isAPLVersion())
        {
            c = new ModelConfig();
            c.name = TEMPEL_1;
            c.type = COMETS;
            c.population = null;
            c.dataUsed = IMAGE_BASED;
            c.author = GASKELL;
            c.pathOnServer = "/GASKELL/TEMPEL1";
            configArray.add(c);
        }

//        c.add(new ModelConfig(TEMPEL_1, COMETS, null, IMAGE_BASED, THOMAS, "/THOMAS/TEMPEL1/tempel1_cart.t1.gz"));
        c = new ModelConfig();
        c.name = TEMPEL_1;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = THOMAS;
        c.pathOnServer = "/THOMAS/TEMPEL1/tempel1_cart.t1.gz";
        configArray.add(c);

//        c.add(new ModelConfig(WILD_2, COMETS, null, IMAGE_BASED, DUXBURY, "/OTHER/WILD2/wild2_cart_full.w2.gz"));
        c = new ModelConfig();
        c.name = WILD_2;
        c.type = COMETS;
        c.population = null;
        c.dataUsed = IMAGE_BASED;
        c.author = DUXBURY;
        c.pathOnServer = "/OTHER/WILD2/wild2_cart_full.w2.gz";
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
        // if hasLidarData is true, the following must be filled in
        public Date lidarSearchDefaultStartDate;
        public Date lidarSearchDefaultEndDate;

        protected ModelConfig clone()
        {
            ModelConfig c = new ModelConfig();
            c.name = this.name;
            c.type = this.type;
            c.population = this.population;
            c.dataUsed = this.dataUsed;
            c.author = this.author;
            c.pathOnServer = this.pathOnServer;
            c.hasImageMap = this.hasImageMap;
            c.hasPerspectiveImages = this.hasPerspectiveImages;
            c.hasLidarData = this.hasLidarData;
            c.hasMapmaker = this.hasMapmaker;
            c.hasSpectralData = this.hasSpectralData;
            c.hasLineamentData = this.hasLineamentData;
//            if (this.hasPerspectiveImages)
//            {
//                c.imageSearchDefaultStartDate = (Date)this.imageSearchDefaultStartDate.clone();
//                c.imageSearchDefaultEndDate = (Date)this.imageSearchDefaultEndDate.clone();
//                c.imageSearchQuery = this.imageSearchQuery;
//                c.imageSearchFilterNames = this.imageSearchFilterNames.clone();
//                c.imageSearchUserDefinedCheckBoxesNames = this.imageSearchFilterNames.clone();
//                c.imageSearchDefaultMaxSpacecraftDistance = this.imageSearchDefaultMaxSpacecraftDistance;
//                c.imageSearchDefaultMaxResolution = this.imageSearchDefaultMaxResolution;
//                c.imageSearchImageSources = this.imageSearchImageSources.clone();
//            }
//            if (this.hasLidarData)
//            {
//                c.lidarSearchDefaultStartDate = (Date)this.lidarSearchDefaultStartDate.clone();
//                c.lidarSearchDefaultEndDate = (Date)this.lidarSearchDefaultEndDate.clone();
//            }
            return c;
        };

        public String getImagingInstrumentName()
        {
            if (EROS.equals(name))
                return MSI;
            else if (MATHILDE.equals(name))
                return MSI;
            else if (ITOKAWA.equals(name))
                return AMICA;
            else if (VESTA.equals(name))
                return FC;
            else if (IDA.equals(name))
                return SSI;
            else if (GASPRA.equals(name))
                return SSI;
            else if (LUTETIA.equals(name))
                return OSIRIS;
            else
                return IMAGING_DATA;
        }

        public String getLidarInstrumentName()
        {
            if (EROS.equals(name))
                return NLR;
            else if (ITOKAWA.equals(name))
                return LIDAR;
            else
                return null;
        }

        public String getSpectrographName()
        {
            if (EROS.equals(name))
                return NIS;
            else
                return null;
        }
    }

    static public Image createImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        if (ImageSource.PDS.equals(key.source) ||
                ImageSource.GASKELL.equals(key.source) ||
                ImageSource.CORRECTED.equals(key.source))
        {
            if (smallBodyModel.getModelName().toLowerCase().equals("eros") ||
                smallBodyModel.getModelName().toLowerCase().startsWith("433 eros") ||
                smallBodyModel.getModelName().toLowerCase().startsWith("near-a-msi-5-erosshape"))
                return new MSIImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel instanceof Itokawa)
                return new AmicaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel instanceof Vesta)
                return new FcImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("phobos"))
                return new PhobosImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("deimos"))
                return new DeimosImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("lutetia"))
                return new OsirisImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("dione"))
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("mimas"))
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("phoebe"))
                return new SaturnMoonImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().equals("gaspra"))
                return new SSIGaspraImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().equals("ida"))
                return new SSIIdaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().equals("mathilde"))
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
                return new Eros();
            else if (ITOKAWA.equals(name))
                return new Itokawa();
            else if (VESTA.equals(name))
                return new Vesta();
            else if (RQ36.equals(name))
                return new RQ36();
            else if (TEMPEL_1.equals(name))
            {
                String[] names = {
                        name + " low"
                };
                String[] paths = {
                        config.pathOnServer + "/ver64q.vtk.gz",
                };

                boolean useAPLServer = true;
                boolean hasColoringData = true;

                return new SimpleSmallBody(name, author, names, paths, hasColoringData, useAPLServer);
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
                boolean useAPLServer = false;
                if (LUTETIA.equals(name) ||
                        DIONE.equals(name) ||
                        RHEA.equals(name) ||
                        HYPERION.equals(name) ||
                        TETHYS.equals(name) ||
                        (PHOBOS.equals(name) && EXPERIMENTAL.equals(author)))
                {
                    useAPLServer = true;
                }

                boolean hasColoringData = true;

                return new SimpleSmallBody(name, author, names, paths, hasColoringData, useAPLServer);
            }
        }
        else if (THOMAS.equals(author))
        {
            if (EROS.equals(name))
                return new ErosThomas();
            else if (DEIMOS.equals(name))
                return new Deimos();
            else if (VESTA.equals(name))
                return new VestaOld();
        }
        else if (CUSTOM.equals(author))
        {
            return new CustomShapeModel(name);
        }

        String imageMap = null;
        if (config.hasImageMap)
            imageMap = (new File(config.pathOnServer)).getParent() + "/image_map.png";

        boolean hasColoringData = false;
        if (EROS.equals(name) || IDA.equals(name) || GASPRA.equals(name) || MATHILDE.equals(name))
            hasColoringData = true;

        return new SimpleSmallBody(name, author, config.pathOnServer, imageMap, hasColoringData);
    }

    static public Graticule createGraticule(ModelConfig config, SmallBodyModel smallBodyModel)
    {
        String author = config.author;

        if (GASKELL.equals(author))
        {
            String name = config.name;

            String[] graticulePaths = null;
            if (TEMPEL_1.equals(name))
            {
                graticulePaths = new String[]{
                        config.pathOnServer + "/coordinate_grid_res0.vtk.gz"
                };
            }
            else
            {
                graticulePaths = new String[]{
                        config.pathOnServer + "/coordinate_grid_res0.vtk.gz",
                        config.pathOnServer + "/coordinate_grid_res1.vtk.gz",
                        config.pathOnServer + "/coordinate_grid_res2.vtk.gz",
                        config.pathOnServer + "/coordinate_grid_res3.vtk.gz"
                };
            };

            boolean useAPLServer = false;
            if (VESTA.equals(name) ||
                    RQ36.equals(name) ||
                    LUTETIA.equals(name) ||
                    DIONE.equals(name) ||
                    RHEA.equals(name) ||
                    HYPERION.equals(name) ||
                    TETHYS.equals(name) ||
                    TEMPEL_1.equals(name))
            {
                useAPLServer = true;
            }

            return new Graticule(smallBodyModel, graticulePaths, useAPLServer);
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
        if (smallBodyModel.getModelName().toLowerCase().equals("eros") ||
            smallBodyModel.getModelName().toLowerCase().startsWith("433 eros") ||
            smallBodyModel.getModelName().toLowerCase().startsWith("near-a-msi-5-erosshape"))
        {
            models.put(ModelNames.LIDAR_BROWSE, new NLRBrowseDataCollection());
            models.put(ModelNames.LIDAR_SEARCH, new NLRSearchDataCollection(smallBodyModel));
        }
        else if (smallBodyModel instanceof Itokawa)
        {
            models.put(ModelNames.LIDAR_BROWSE, new HayLidarBrowseDataCollection());
            models.put(ModelNames.LIDAR_SEARCH, new HayLidarSearchDataCollection(smallBodyModel));
        }

        return models;
    }
}
