package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import nom.tam.fits.FitsException;

import edu.jhuapl.near.model.Image.ImageKey;
import edu.jhuapl.near.model.Image.ImageSource;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.model.deimos.Deimos;
import edu.jhuapl.near.model.eros.Eros;
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
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.model.vesta.FcImage;
import edu.jhuapl.near.model.vesta.Vesta;
import edu.jhuapl.near.model.vesta_old.VestaOld;

public class ModelFactory
{
    // Names of built-in small body models
    static public final String EROS = "Eros";
    static public final String ITOKAWA = "Itokawa";
    static public final String VESTA = "Vesta";
    static public final String MIMAS = "Mimas";
    static public final String PHOEBE = "Phoebe";
    static public final String PHOBOS = "Phobos";
    static public final String RQ36 = "RQ36";
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

    // Names of submenus
    static public final String GASKELL = "Gaskell";
    static public final String THOMAS = "Thomas";
    static public final String STOOKE = "Stooke";
    static public final String HUDSON = "Hudson";
    static public final String OTHER = "Other";
    static public final String CUSTOM = "Custom";

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

    static public final ModelConfig[] builtInModelConfigs = {
        new ModelConfig(EROS, GASKELL, "/GASKELL/EROS", false, true, true, true, true, true),
        new ModelConfig(ITOKAWA, GASKELL, "/GASKELL/ITOKAWA", false, true, true, false, false, false),
        new ModelConfig(VESTA, GASKELL, "/GASKELL/VESTA", false, true),
        new ModelConfig(RQ36, GASKELL, "/GASKELL/RQ36"),
        new ModelConfig(MIMAS, GASKELL, "/GASKELL/MIMAS"),
        new ModelConfig(PHOEBE, GASKELL, "/GASKELL/PHOEBE"),
        new ModelConfig(PHOBOS, GASKELL, "/GASKELL/PHOBOS", false, true),
        new ModelConfig(LUTETIA, GASKELL, "/GASKELL/LUTETIA", false, true),
        new ModelConfig(IDA, THOMAS, "/THOMAS/IDA/243ida.llr.gz", true, true),
        new ModelConfig(GASPRA, THOMAS, "/THOMAS/GASPRA/951gaspra.llr.gz", true, true),
        new ModelConfig(MATHILDE, THOMAS, "/THOMAS/MATHILDE/253mathilde.llr.gz", true, true),
        new ModelConfig(VESTA, THOMAS, "/THOMAS/VESTA_OLD"),
        new ModelConfig(DEIMOS, THOMAS, "/THOMAS/DEIMOS", true),
        new ModelConfig(PHOBOS, THOMAS, "/THOMAS/PHOBOS/m1phobos.llr.gz"),
        new ModelConfig(JANUS, THOMAS, "/THOMAS/JANUS/s10janus.llr.gz"),
        new ModelConfig(EPIMETHEUS, THOMAS, "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz"),
        new ModelConfig(HYPERION, THOMAS, "/THOMAS/HYPERION/s7hyperion.llr.gz"),
        new ModelConfig(TEMPEL_1, THOMAS, "/THOMAS/TEMPEL1/tempel1_cart.t1.gz"),
        new ModelConfig(IDA, STOOKE, "/STOOKE/IDA/243ida.llr.gz", true),
        new ModelConfig(GASPRA, STOOKE, "/STOOKE/GASPRA/951gaspra.llr.gz", true),
        new ModelConfig(HALLEY, STOOKE, "/STOOKE/HALLEY/1682q1halley.llr.gz"),
        new ModelConfig(AMALTHEA, STOOKE, "/STOOKE/AMALTHEA/j5amalthea.llr.gz"),
        new ModelConfig(LARISSA, STOOKE, "/STOOKE/LARISSA/n7larissa.llr.gz"),
        new ModelConfig(PROTEUS, STOOKE, "/STOOKE/PROTEUS/n8proteus.llr.gz"),
        new ModelConfig(JANUS, STOOKE, "/STOOKE/JANUS/s10janus.llr.gz"),
        new ModelConfig(EPIMETHEUS, STOOKE, "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz"),
        new ModelConfig(PROMETHEUS, STOOKE, "/STOOKE/PROMETHEUS/s16prometheus.llr.gz"),
        new ModelConfig(PANDORA, STOOKE, "/STOOKE/PANDORA/s17pandora.llr.gz"),
        new ModelConfig(GEOGRAPHOS, HUDSON, "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz"),
        new ModelConfig(KY26, HUDSON, "/HUDSON/KY26/1998ky26.obj.gz"),
        new ModelConfig(BACCHUS, HUDSON, "/HUDSON/BACCHUS/2063bacchus.obj.gz"),
        new ModelConfig(KLEOPATRA, HUDSON, "/HUDSON/KLEOPATRA/216kleopatra.obj.gz"),
        new ModelConfig(ITOKAWA, HUDSON, "/HUDSON/ITOKAWA/25143itokawa.obj.gz"),
        new ModelConfig(TOUTATIS_LOW_RES, HUDSON, "/HUDSON/TOUTATIS/4179toutatis.obj.gz"),
        new ModelConfig(TOUTATIS_HIGH_RES, HUDSON, "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz"),
        new ModelConfig(CASTALIA, HUDSON, "/HUDSON/CASTALIA/4769castalia.obj.gz"),
        new ModelConfig(_52760_1998_ML14, HUDSON, "/HUDSON/52760/52760.obj.gz"),
        new ModelConfig(GOLEVKA, HUDSON, "/HUDSON/GOLEVKA/6489golevka.obj.gz"),
        new ModelConfig(WILD_2, OTHER, "/OTHER/WILD2/wild2_cart_full.w2.gz")
    };


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
        public final String name;
        public final String submenu;
        public final String pathOnServer;
        public final boolean hasImageMap;
        public final boolean hasPerspectiveImages;
        public final boolean hasLidarData;
        public final boolean hasMapmaker;
        public final boolean hasSpectralData;
        public final boolean hasLineamentData;
        public ModelConfig(
                String name,
                String submenu,
                String pathOnServer)
        {
            this(name, submenu, pathOnServer, false, false, false, false, false, false);
        }
        public ModelConfig(
                String name,
                String submenu,
                String pathOnServer,
                boolean hasImageMap)
        {
            this(name, submenu, pathOnServer, hasImageMap, false, false, false, false, false);
        }
        public ModelConfig(
                String name,
                String submenu,
                String pathOnServer,
                boolean hasImageMap,
                boolean hasPerspectiveImages)
        {
            this(name, submenu, pathOnServer, hasImageMap, hasPerspectiveImages, false, false, false, false);
        }
        public ModelConfig(
                String name,
                String submenu,
                String pathOnServer,
                boolean hasImageMap,
                boolean hasPerspectiveImages,
                boolean hasLidarData,
                boolean hasMapmaker,
                boolean hasSpectralData,
                boolean hasLineamentData)
        {
            this.name = name;
            this.submenu = submenu;
            this.pathOnServer = pathOnServer;
            this.hasImageMap = hasImageMap;
            this.hasPerspectiveImages = hasPerspectiveImages;
            this.hasLidarData = hasLidarData;
            this.hasMapmaker = hasMapmaker;
            this.hasSpectralData = hasSpectralData;
            this.hasLineamentData = hasLineamentData;
        }

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

    // The remaining functions are various factory functions for instantiating
    // various models.

    static public Image createImage(ImageKey key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly,
            File rootFolder) throws FitsException, IOException
    {
        if (ImageSource.PDS.equals(key.source) ||
                ImageSource.GASKELL.equals(key.source) ||
                ImageSource.CORRECTED.equals(key.source))
        {
            if (smallBodyModel instanceof Eros)
                return new MSIImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel instanceof Itokawa)
                return new AmicaImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel instanceof Vesta)
                return new FcImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("phobos"))
                return new PhobosImage(key, smallBodyModel, loadPointingOnly, rootFolder);
            else if (smallBodyModel.getModelName().toLowerCase().startsWith("lutetia"))
                return new OsirisImage(key, smallBodyModel, loadPointingOnly, rootFolder);
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
        String submenu = config.submenu;

        if (GASKELL.equals(submenu))
        {
            if (EROS.equals(name))
                return new Eros();
            else if (ITOKAWA.equals(name))
                return new Itokawa();
            else if (VESTA.equals(name))
                return new Vesta();
            else if (RQ36.equals(name))
                return new RQ36();
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
                if (LUTETIA.equals(name))
                    useAPLServer = true;

                boolean hasColoringData = false;
                if (LUTETIA.equals(name))
                    hasColoringData = true;

                return new SimpleSmallBody(name, submenu, names, paths, hasColoringData, useAPLServer);
            }
        }
        else if (THOMAS.equals(submenu))
        {
            if (DEIMOS.equals(name))
                return new Deimos();
            else if (VESTA.equals(name))
                return new VestaOld();
        }
        else if (CUSTOM.equals(submenu))
        {
            return new CustomShapeModel(name);
        }

        String imageMap = null;
        if (config.hasImageMap)
            imageMap = (new File(config.pathOnServer)).getParent() + "/image_map.png";

        return new SimpleSmallBody(name, submenu, config.pathOnServer, imageMap);
    }

    static public Graticule createGraticule(ModelConfig config, SmallBodyModel smallBodyModel)
    {
        String submenu = config.submenu;

        if (GASKELL.equals(submenu))
        {
            String[] graticulePaths = {
                    config.pathOnServer + "/coordinate_grid_res0.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res1.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res2.vtk.gz",
                    config.pathOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            boolean useAPLServer = false;
            String name = config.name;
            if (VESTA.equals(name) || RQ36.equals(name) || LUTETIA.equals(name))
                useAPLServer = true;

            return new Graticule(smallBodyModel, graticulePaths, useAPLServer);
        }
        else if (CUSTOM.equals(submenu))
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
        if (smallBodyModel instanceof Eros)
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
