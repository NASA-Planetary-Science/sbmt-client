package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.eros.LineamentControlPanel;
import edu.jhuapl.near.gui.eros.MSISearchPanel;
import edu.jhuapl.near.gui.eros.NISSearchPanel;
import edu.jhuapl.near.gui.eros.NLRPanel;
import edu.jhuapl.near.gui.gaspra.SSIGaspraSearchPanel;
import edu.jhuapl.near.gui.ida.SSIIdaSearchPanel;
import edu.jhuapl.near.gui.itokawa.AmicaSearchPanel;
import edu.jhuapl.near.gui.itokawa.HayLidarPanel;
import edu.jhuapl.near.gui.lutetia.OsirisImagingDataSearchPanel;
import edu.jhuapl.near.gui.mathilde.MSIMathildeSearchPanel;
import edu.jhuapl.near.gui.phobos.PhobosImagingDataSearchPanel;
import edu.jhuapl.near.gui.vesta.FCSearchPanel;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.MapletBoundaryCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.model.deimos.Deimos;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NLRBrowseDataCollection;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarBrowseDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarSearchDataCollection;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.phobos.Phobos;
import edu.jhuapl.near.model.rq36.RQ36;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.model.vesta.Vesta;
import edu.jhuapl.near.model.vesta_old.VestaOld;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.popupmenus.MapletBoundaryPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.popupmenus.eros.LineamentPopupMenu;
import edu.jhuapl.near.popupmenus.eros.NISPopupMenu;
import edu.jhuapl.near.util.Configuration;
import edu.jhuapl.near.util.Preferences;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. This class is used to build all built-in and custom views.
 * All the configuration details of all the built-in and custom views
 * are contained in this class.

 * @author kahneg1
 *
 */
public class View extends JPanel
{
    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private ModelInfoWindowManager infoPanelManager;
    private StatusBar statusBar;
    private boolean initialized = false;
    private ViewConfig viewConfig;
    static private boolean initializedPanelSizing = false;

    /**
     * By default a view should be created empty. Only when the user
     * requests to show a particular View, should the View's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the View is
     * shown in order to cause it
     */
    public View(
            StatusBar statusBar,
            ViewConfig viewConfig)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.viewConfig = viewConfig;
    }

    public void initialize()
    {
        if (initialized)
            return;

        setupModelManager();

        infoPanelManager = new ModelInfoWindowManager(modelManager);

        renderer = new Renderer(modelManager);

        setupPopupManager();

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab(viewConfig.name, new SmallBodyControlPanel(modelManager, viewConfig.name));

        if (viewConfig.hasPerspectiveImages)
        {
            JComponent component = createPerspectiveImageSearchTab(viewConfig, modelManager, infoPanelManager, pickManager, renderer);
            controlPanel.addTab(viewConfig.getImagingInstrumentName(), component);
        }

        if (viewConfig.hasSpectralData)
        {
            JComponent component = createSpectralDataSearchTab(modelManager, infoPanelManager, pickManager);
            controlPanel.addTab(viewConfig.getSpectrographName(), component);
        }

        if (viewConfig.hasLidarData)
        {
            JComponent component = createLidarDataSearchTab(modelManager, pickManager, renderer);
            controlPanel.addTab(viewConfig.getLidarInstrumentName(), component);
        }

        if (Configuration.isAPLVersion())
        {
            if (viewConfig.hasLineamentData)
            {
                JComponent component = createLineamentTab(modelManager);
                controlPanel.addTab("Lineament", component);
            }

            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, pickManager, renderer));

            if (viewConfig.hasMapmaker)
            {
                JComponent component = createMapmakerTab(viewConfig, modelManager, pickManager);
                controlPanel.addTab("Mapmaker", component);
            }
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
        splitPane.setOneTouchExpandable(true);

        if (!initializedPanelSizing)
        {
            int width = (int)Preferences.getInstance().getAsLong(Preferences.RENDERER_PANEL_WIDTH, 800L);
            int height = (int)Preferences.getInstance().getAsLong(Preferences.RENDERER_PANEL_HEIGHT, 800L);

            renderer.setMinimumSize(new Dimension(100, 100));
            renderer.setPreferredSize(new Dimension(width, height));

            width = (int)Preferences.getInstance().getAsLong(Preferences.CONTROL_PANEL_WIDTH, 320L);
            height = (int)Preferences.getInstance().getAsLong(Preferences.CONTROL_PANEL_HEIGHT, 800L);

            controlPanel.setMinimumSize(new Dimension(320, 100));
            controlPanel.setPreferredSize(new Dimension(width, height));

            // Save out the size of the control panel and renderer when the tool exits
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                private LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

                @Override
                public void run()
                {
                    map.put(Preferences.RENDERER_PANEL_WIDTH, new Long(renderer.getWidth()).toString());
                    map.put(Preferences.RENDERER_PANEL_HEIGHT, new Long(renderer.getHeight()).toString());
                    map.put(Preferences.CONTROL_PANEL_WIDTH, new Long(controlPanel.getWidth()).toString());
                    map.put(Preferences.CONTROL_PANEL_HEIGHT, new Long(controlPanel.getHeight()).toString());
                    Preferences.getInstance().put(map);
                }
            });

            initializedPanelSizing = true;
        }
        else
        {
            renderer.setMinimumSize(new Dimension(100, 100));
            renderer.setPreferredSize(new Dimension(800, 800));
            controlPanel.setMinimumSize(new Dimension(320, 100));
            controlPanel.setPreferredSize(new Dimension(320, 800));
        }

        this.add(splitPane, BorderLayout.CENTER);

        initialized = true;
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public PickManager getPickManager()
    {
        return pickManager;
    }

    private void setupModelManager()
    {
        modelManager = new ModelManager();

        SmallBodyModel smallBodyModel = createSmallBodyModel(viewConfig);
        Graticule graticule = createGraticule(viewConfig, smallBodyModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));

        if (viewConfig.hasPerspectiveImages)
        {
            allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel));
            allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
        }

        if (viewConfig.hasSpectralData)
        {
            allModels.put(ModelNames.SPECTRA, createSpectralModel(smallBodyModel));
        }

        if (viewConfig.hasLidarData)
        {
            allModels.putAll(createLidarModels(smallBodyModel));
        }

        if (viewConfig.hasLineamentData)
        {
            allModels.put(ModelNames.LINEAMENT, createLineament());
        }

        if (viewConfig.hasMapmaker)
        {
            allModels.put(ModelNames.MAPLET_BOUNDARY, new MapletBoundaryCollection(smallBodyModel));
        }

        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));

        modelManager.setModels(allModels);
    }


    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager, infoPanelManager, renderer);

        if (viewConfig.hasPerspectiveImages)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);

            PopupMenu popupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, renderer, renderer);
            popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

            popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);
        }

        if (viewConfig.hasSpectralData)
        {
            PopupMenu popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.SPECTRA), popupMenu);
        }

        if (viewConfig.hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, renderer);
            popupManager.registerPopup(lidarSearch, popupMenu);
        }

        if (viewConfig.hasLineamentData)
        {
            PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);
        }

        if (viewConfig.hasMapmaker)
        {
            PopupMenu popupMenu = new MapletBoundaryPopupMenu(modelManager, renderer);
            popupManager.registerPopup(modelManager.getModel(ModelNames.MAPLET_BOUNDARY), popupMenu);
        }
    }

    /**
     * Return a unique name for this view. No other view may have this
     * name. Note that only applies within built-in views or custom views
     * but a custom view can share the name of a built-in one or vice versa.
     * By default simply return the submenu concatenated with the display
     * name if the submenu is not null or just the display name if the submenu
     * is null.
     * @return
     */
    public String getUniqueName()
    {
        return SmallBodyModel.getUniqueName(getDisplayName(), getSubmenu());
    }


    /**
     * Return the display name for this view (the name to be shown in the menu).
     * This name need not be unique among all views.
     * @return
     */
    public String getDisplayName()
    {
        return viewConfig.name;
    }

    /**
     * Return the submenu in which this view should be placed.
     * If null (the default), do not place in a submenu but in the
     * top menu.
     *
     * @return
     */
    public String getSubmenu()
    {
        return viewConfig.submenu;
    }

    static public View createCustomView(StatusBar statusBar, String name)
    {
        ViewConfig config = new ViewConfig(name, CUSTOM, null);
        return new View(statusBar, config);
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    // The remainder of this file contains various factory functions and
    // a configuration class needed to setup all the built-in views.

    // Names of built-in views
    static private final String EROS = "Eros";
    static private final String ITOKAWA = "Itokawa";
    static private final String VESTA = "Vesta";
    static private final String MIMAS = "Mimas";
    static private final String PHOEBE = "Phoebe";
    static private final String PHOBOS = "Phobos";
    static private final String RQ36 = "RQ36";
    static private final String LUTETIA = "Lutetia";
    static private final String IDA = "Ida";
    static private final String GASPRA = "Gaspra";
    static private final String MATHILDE = "Mathilde";
    static private final String DEIMOS = "Deimos";
    static private final String JANUS = "Janus";
    static private final String EPIMETHEUS = "Epimetheus";
    static private final String HYPERION = "Hyperion";
    static private final String TEMPEL_1 = "Tempel 1";
    static private final String HALLEY = "Halley";
    static private final String AMALTHEA = "Amalthea";
    static private final String LARISSA = "Larissa";
    static private final String PROTEUS = "Proteus";
    static private final String PROMETHEUS = "Prometheus";
    static private final String PANDORA = "Pandora";
    static private final String GEOGRAPHOS = "Geographos";
    static private final String KY26 = "KY26";
    static private final String BACCHUS = "Bacchus";
    static private final String KLEOPATRA = "Kleopatra";
    static private final String TOUTATIS_LOW_RES = "Toutatis (Low Res)";
    static private final String TOUTATIS_HIGH_RES = "Toutatis (High Res)";
    static private final String CASTALIA = "Castalia";
    static private final String _52760_1998_ML14 = "52760 (1998 ML14)";
    static private final String GOLEVKA = "Golevka";
    static private final String WILD_2 = "Wild 2";

    // Names of submenus
    static private final String GASKELL = "Gaskell";
    static private final String THOMAS = "Thomas";
    static private final String STOOKE = "Stooke";
    static private final String HUDSON = "Hudson";
    static private final String OTHER = "Other";
    static private final String CUSTOM = "Custom";

    // Names of instruments
    static private final String MSI = "MSI";
    static private final String NLR = "NLR";
    static private final String NIS = "NIS";
    static private final String AMICA = "AMICA";
    static private final String LIDAR = "LIDAR";
    static private final String FC = "FC";
    static private final String SSI = "SSI";
    static private final String OSIRIS = "OSIRIS";
    static private final String IMAGING_DATA = "Imaging Data";

    public static class ViewConfig
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
        private ViewConfig(
                String name,
                String submenu,
                String pathOnServer)
        {
            this(name, submenu, pathOnServer, false, false, false, false, false, false);
        }
        private ViewConfig(
                String name,
                String submenu,
                String pathOnServer,
                boolean hasImageMap)
        {
            this(name, submenu, pathOnServer, hasImageMap, false, false, false, false, false);
        }
        private ViewConfig(
                String name,
                String submenu,
                String pathOnServer,
                boolean hasImageMap,
                boolean hasPerspectiveImages)
        {
            this(name, submenu, pathOnServer, hasImageMap, hasPerspectiveImages, false, false, false, false);
        }
        private ViewConfig(
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

    static public final ViewConfig[] builtInViewConfigs = {
        new ViewConfig(EROS, GASKELL, "/GASKELL/EROS", false, true, true, true, true, true),
        new ViewConfig(ITOKAWA, GASKELL, "/GASKELL/ITOKAWA", false, true, true, false, false, false),
        new ViewConfig(VESTA, GASKELL, "/GASKELL/VESTA", false, true),
        new ViewConfig(RQ36, GASKELL, "/GASKELL/RQ36"),
        new ViewConfig(MIMAS, GASKELL, "/GASKELL/MIMAS"),
        new ViewConfig(PHOEBE, GASKELL, "/GASKELL/PHOEBE"),
        new ViewConfig(PHOBOS, GASKELL, "/GASKELL/PHOBOS", false, false),
        new ViewConfig(LUTETIA, GASKELL, "/GASKELL/LUTETIA", false, true),
        new ViewConfig(IDA, THOMAS, "/THOMAS/IDA/243ida.llr.gz", true, true),
        new ViewConfig(GASPRA, THOMAS, "/THOMAS/GASPRA/951gaspra.llr.gz", true, true),
        new ViewConfig(MATHILDE, THOMAS, "/THOMAS/MATHILDE/253mathilde.llr.gz", true, true),
        new ViewConfig(VESTA, THOMAS, "/THOMAS/VESTA_OLD"),
        new ViewConfig(DEIMOS, THOMAS, "/THOMAS/DEIMOS", true),
        new ViewConfig(PHOBOS, THOMAS, "/THOMAS/PHOBOS/m1phobos.llr.gz"),
        new ViewConfig(JANUS, THOMAS, "/THOMAS/JANUS/s10janus.llr.gz"),
        new ViewConfig(EPIMETHEUS, THOMAS, "/THOMAS/EPIMETHEUS/s11epimetheus.llr.gz"),
        new ViewConfig(HYPERION, THOMAS, "/THOMAS/HYPERION/s7hyperion.llr.gz"),
        new ViewConfig(TEMPEL_1, THOMAS, "/THOMAS/TEMPEL1/tempel1_cart.t1.gz"),
        new ViewConfig(IDA, STOOKE, "/STOOKE/IDA/243ida.llr.gz", true),
        new ViewConfig(GASPRA, STOOKE, "/STOOKE/GASPRA/951gaspra.llr.gz", true),
        new ViewConfig(HALLEY, STOOKE, "/STOOKE/HALLEY/1682q1halley.llr.gz"),
        new ViewConfig(AMALTHEA, STOOKE, "/STOOKE/AMALTHEA/j5amalthea.llr.gz"),
        new ViewConfig(LARISSA, STOOKE, "/STOOKE/LARISSA/n7larissa.llr.gz"),
        new ViewConfig(PROTEUS, STOOKE, "/STOOKE/PROTEUS/n8proteus.llr.gz"),
        new ViewConfig(JANUS, STOOKE, "/STOOKE/JANUS/s10janus.llr.gz"),
        new ViewConfig(EPIMETHEUS, STOOKE, "/STOOKE/EPIMETHEUS/s11epimetheus.llr.gz"),
        new ViewConfig(PROMETHEUS, STOOKE, "/STOOKE/PROMETHEUS/s16prometheus.llr.gz"),
        new ViewConfig(PANDORA, STOOKE, "/STOOKE/PANDORA/s17pandora.llr.gz"),
        new ViewConfig(GEOGRAPHOS, HUDSON, "/HUDSON/GEOGRAPHOS/1620geographos.obj.gz"),
        new ViewConfig(KY26, HUDSON, "/HUDSON/KY26/1998ky26.obj.gz"),
        new ViewConfig(BACCHUS, HUDSON, "/HUDSON/BACCHUS/2063bacchus.obj.gz"),
        new ViewConfig(KLEOPATRA, HUDSON, "/HUDSON/KLEOPATRA/216kleopatra.obj.gz"),
        new ViewConfig(ITOKAWA, HUDSON, "/HUDSON/ITOKAWA/25143itokawa.obj.gz"),
        new ViewConfig(TOUTATIS_LOW_RES, HUDSON, "/HUDSON/TOUTATIS/4179toutatis.obj.gz"),
        new ViewConfig(TOUTATIS_HIGH_RES, HUDSON, "/HUDSON/TOUTATIS2/4179toutatis2.obj.gz"),
        new ViewConfig(CASTALIA, HUDSON, "/HUDSON/CASTALIA/4769castalia.obj.gz"),
        new ViewConfig(_52760_1998_ML14, HUDSON, "/HUDSON/52760/52760.obj.gz"),
        new ViewConfig(GOLEVKA, HUDSON, "/HUDSON/GOLEVKA/6489golevka.obj.gz"),
        new ViewConfig(WILD_2, OTHER, "/OTHER/WILD2/wild2_cart_full.w2.gz")
    };

    static private SmallBodyModel createSmallBodyModel(ViewConfig viewConfig)
    {
        String name = viewConfig.name;
        String submenu = viewConfig.submenu;

        if (GASKELL.equals(submenu))
        {
            if (EROS.equals(name))
                return new Eros();
            else if (ITOKAWA.equals(name))
                return new Itokawa();
            else if (VESTA.equals(name))
                return new Vesta();
            else if (PHOBOS.equals(name))
                return new Phobos();
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
                        viewConfig.pathOnServer + "/ver64q.vtk.gz",
                        viewConfig.pathOnServer + "/ver128q.vtk.gz",
                        viewConfig.pathOnServer + "/ver256q.vtk.gz",
                        viewConfig.pathOnServer + "/ver512q.vtk.gz"
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
        if (viewConfig.hasImageMap)
            imageMap = (new File(viewConfig.pathOnServer)).getParent() + "/image_map.png";

        return new SimpleSmallBody(name, submenu, viewConfig.pathOnServer, imageMap);
    }

    static private Graticule createGraticule(ViewConfig viewConfig, SmallBodyModel smallBodyModel)
    {
        String submenu = viewConfig.submenu;

        if (GASKELL.equals(submenu))
        {
            String[] graticulePaths = {
                    viewConfig.pathOnServer + "/coordinate_grid_res0.vtk.gz",
                    viewConfig.pathOnServer + "/coordinate_grid_res1.vtk.gz",
                    viewConfig.pathOnServer + "/coordinate_grid_res2.vtk.gz",
                    viewConfig.pathOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            boolean useAPLServer = false;
            String name = viewConfig.name;
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

    static private LineamentModel createLineament()
    {
        return new LineamentModel();
    }

    static private NISSpectraCollection createSpectralModel(SmallBodyModel smallBodyModel)
    {
        return new NISSpectraCollection(smallBodyModel);
    }

    static private HashMap<String, Model> createLidarModels(SmallBodyModel smallBodyModel)
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

    static private JComponent createPerspectiveImageSearchTab(
            ViewConfig viewConfig,
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();
        String name = viewConfig.name;
        if (smallBodyModel instanceof Eros)
            return new MSISearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (smallBodyModel instanceof Itokawa)
            return new AmicaSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (smallBodyModel instanceof Vesta)
            return new FCSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (smallBodyModel instanceof Phobos)
            return new PhobosImagingDataSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (LUTETIA.equals(name))
            return new OsirisImagingDataSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (GASPRA.equals(name))
            return new SSIGaspraSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (IDA.equals(name))
            return new SSIIdaSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else if (MATHILDE.equals(name))
            return new MSIMathildeSearchPanel(modelManager, infoPanelManager, pickManager, renderer);
        else
            return null;
    }

    static private JComponent createSpectralDataSearchTab(
            ModelManager modelManager,
            ModelInfoWindowManager infoPanelManager,
            PickManager pickManager)
    {
        return new NISSearchPanel(modelManager, infoPanelManager, pickManager);
    }

    static private JComponent createLidarDataSearchTab(
            ModelManager modelManager,
            PickManager pickManager,
            Renderer renderer)
    {
        SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();
        if (smallBodyModel instanceof Eros)
            return new NLRPanel(modelManager, pickManager, renderer);
        else if (smallBodyModel instanceof Itokawa)
            return new HayLidarPanel(modelManager, pickManager, renderer);
        else
            return null;
    }

    static private JComponent createLineamentTab(ModelManager modelManager)
    {
        return new LineamentControlPanel(modelManager);
    }

    static private JComponent createMapmakerTab(ViewConfig viewConfig, ModelManager modelManager, PickManager pickManager)
    {
        return new TopoPanel(modelManager, pickManager, viewConfig.pathOnServer + "/mapmaker.zip");
    }
}
