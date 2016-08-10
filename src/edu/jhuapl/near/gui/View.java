package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.eros.LineamentControlPanel;
import edu.jhuapl.near.gui.eros.NISSearchPanel;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.DEMBoundaryCollection;
import edu.jhuapl.near.model.DEMCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.Image.ImagingInstrument;
import edu.jhuapl.near.model.Image.SpectralMode;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.ImageCubeCollection;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyConfig.Instrument;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelAuthor;
import edu.jhuapl.near.model.SmallBodyConfig.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.NISStatisticsCollection;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImageCubePopupMenu;
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
    private ModelSpectrumWindowManager spectrumPanelManager;
    private StatusBar statusBar;
    private boolean initialized = false;
    private SmallBodyConfig smallBodyConfig;
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
            SmallBodyConfig smallBodyConfig)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.smallBodyConfig = smallBodyConfig;
    }

    public void initialize()
    {
        if (initialized)
            return;

        setupModelManager();

        infoPanelManager = new ModelInfoWindowManager(modelManager);

        spectrumPanelManager = new ModelSpectrumWindowManager(modelManager);

        //renderer = new Renderer(modelManager);
        renderer = new Renderer(modelManager,statusBar);

        setupPopupManager();

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab(smallBodyConfig.getShapeModelName(), new SmallBodyControlPanel(modelManager, smallBodyConfig.getShapeModelName()));


        for (ImagingInstrument instrument : smallBodyConfig.imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
                if (smallBodyConfig.body == ShapeModelBody.EROS)
                {
                    JComponent component = new CubicalImagingSearchPanel(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument).init();
                    controlPanel.addTab(instrument.instrumentName.toString(), component);
                }
                else if (Configuration.isAPLVersion() || (smallBodyConfig.body == ShapeModelBody.ITOKAWA && ShapeModelAuthor.GASKELL == smallBodyConfig.author))
                {
                    JComponent component = new ImagingSearchPanel(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument).init();
                    controlPanel.addTab(instrument.instrumentName.toString(), component);
                }
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new QuadraspectralImagingSearchPanel(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument).init();
                    controlPanel.addTab(instrument.instrumentName.toString(), component);
                }
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new HyperspectralImagingSearchPanel(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument, SmallBodyConfig.LEISA_NBANDS).init();
                    controlPanel.addTab(instrument.instrumentName.toString(), component);
                }
            }
        }

        if (smallBodyConfig.hasSpectralData)
        {
            JComponent component = new NISSearchPanel(modelManager, infoPanelManager, pickManager);
            controlPanel.addTab(Instrument.NIS.toString(), component);
        }

        if (smallBodyConfig.hasLidarData)
        {
            JComponent component = new LidarPanel(smallBodyConfig, modelManager, pickManager, renderer);
            controlPanel.addTab(smallBodyConfig.lidarInstrumentName.toString(), component);
        }

        if (Configuration.isAPLVersion())
        {
            if (smallBodyConfig.hasLineamentData)
            {
                JComponent component = new LineamentControlPanel(modelManager);
                controlPanel.addTab("Lineament", component);
            }

            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            if (!smallBodyConfig.customTemporary)
            {
                ImagingInstrument instrument = null;
                for (ImagingInstrument i : smallBodyConfig.imagingInstruments)
                {
                    instrument = i;
                    break;
                }

                controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument).init());
            }

            controlPanel.addTab("Tracks", new TrackPanel(smallBodyConfig, modelManager, pickManager, renderer));

            /*if (smallBodyConfig.hasMapmaker)
            {
                JComponent component = new MapmakerPanel(modelManager, pickManager, smallBodyConfig.rootDirOnServer + "/mapmaker.zip");
                controlPanel.addTab("Mapmaker", component);
            }

            if (smallBodyConfig.hasBigmap)
            {
                JComponent component = new BigmapPanel(modelManager, pickManager, smallBodyConfig.rootDirOnServer + "/bigmap.zip");
                controlPanel.addTab("Bigmap", component);
            }*/

            /*if(smallBodyConfig.hasMapmaker || smallBodyConfig.hasBigmap)
            {
                JComponent component = new DEMPanel(modelManager, pickManager, smallBodyConfig.rootDirOnServer,
                        smallBodyConfig.hasMapmaker, smallBodyConfig.hasBigmap);
                controlPanel.addTab("DEMs", component);
            }*/

            JComponent component = new CustomDEMPanel(modelManager, pickManager, smallBodyConfig.rootDirOnServer,
                    smallBodyConfig.hasMapmaker, smallBodyConfig.hasBigmap);
            controlPanel.addTab("DEMs", component);
        }


        // add capability to right click on tab title regions and set as default tab to load
        controlPanel.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showDefaultTabSelectionPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                showDefaultTabSelectionPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                showDefaultTabSelectionPopup(e);
            }
        });
        int tabIndex=FavoriteTabsFile.getInstance().getFavoriteTab(smallBodyConfig.getUniqueName());
        controlPanel.setSelectedIndex(tabIndex);    // load default tab (which is 0 if not specified in favorite tabs file)

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

    private void showDefaultTabSelectionPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu tabMenu=new JPopupMenu();
            JMenuItem menuItem=new JMenuItem("Set instrument as default");
            menuItem.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    FavoriteTabsFile.getInstance().setFavoriteTab(smallBodyConfig.getUniqueName(), controlPanel.getSelectedIndex());
                }
            });
            tabMenu.add(menuItem);
            tabMenu.show(controlPanel, e.getX(), e.getY());
        }

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

        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(smallBodyConfig);
        Graticule graticule = ModelFactory.createGraticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));

        for (ImagingInstrument instrument : smallBodyConfig.imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, modelManager));
                allModels.put(ModelNames.CUBE_IMAGES, new ImageCubeCollection(smallBodyModel, modelManager));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, modelManager));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, modelManager));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }
        }

        if (smallBodyConfig.hasSpectralData)
        {
            allModels.put(ModelNames.SPECTRA, ModelFactory.createSpectralModel(smallBodyModel));
            if (smallBodyConfig.body == ShapeModelBody.EROS)
                allModels.put(ModelNames.STATISTICS, new NISStatisticsCollection());
        }

        if (smallBodyConfig.hasLidarData)
        {
            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
        }

        if (smallBodyConfig.hasLineamentData)
        {
            allModels.put(ModelNames.LINEAMENT, ModelFactory.createLineament());
        }

        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
        allModels.put(ModelNames.TRACKS, new LidarSearchDataCollection(smallBodyModel));
        allModels.put(ModelNames.DEM, new DEMCollection(smallBodyModel, modelManager));
        allModels.put(ModelNames.DEM_BOUNDARY, new DEMBoundaryCollection(smallBodyModel, modelManager));

        modelManager.setModels(allModels);
    }


    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager, infoPanelManager, spectrumPanelManager, renderer);

        for (ImagingInstrument instrument : smallBodyConfig.imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)modelManager.getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(modelManager, images, boundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, modelManager, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(modelManager, images, boundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, modelManager, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)modelManager.getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(modelManager, images, boundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, modelManager, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
                popupManager.registerPopup(modelManager.getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }
            }

        if (smallBodyConfig.hasSpectralData)
        {
            PopupMenu popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.SPECTRA), popupMenu);
        }

        if (smallBodyConfig.hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, renderer);
            popupManager.registerPopup(lidarSearch, popupMenu);
        }

        if (smallBodyConfig.hasLineamentData)
        {
            PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);
        }

        if (smallBodyConfig.hasMapmaker || smallBodyConfig.hasBigmap)
        {
            PopupMenu popupMenu = new MapletBoundaryPopupMenu(modelManager, renderer);
            popupManager.registerPopup(modelManager.getModel(ModelNames.DEM_BOUNDARY), popupMenu);
        }
    }

    /**
     * Return a unique name for this view. No other view may have this
     * name. Note that only applies within built-in views or custom views
     * but a custom view can share the name of a built-in one or vice versa.
     * By default simply return the author concatenated with the
     * name if the author is not null or just the name if the author
     * is null.
     * @return
     */
    public String getUniqueName()
    {
        return smallBodyConfig.getUniqueName();
    }


    /**
     * Return the display name for this view (the name to be shown in the menu).
     * This name need not be unique among all views.
     * @return
     */
    public String getDisplayName()
    {
        if (smallBodyConfig.author == ShapeModelAuthor.CUSTOM)
            return smallBodyConfig.customName;
        else if (smallBodyConfig.author == null)
            return smallBodyConfig.body.toString();
        else
        {
            String version = "";
            if (smallBodyConfig.version != null)
                version += " (" + smallBodyConfig.version + ")";
            return smallBodyConfig.author.toString() + version;
        }
    }

    public SmallBodyConfig getSmallBodyConfig()
    {
        return smallBodyConfig;
    }

    static public View createCustomView(StatusBar statusBar, String name)
    {
        SmallBodyConfig config = new SmallBodyConfig();
        config.customName = name;
        config.author = ShapeModelAuthor.CUSTOM;
        return new View(statusBar, config);
    }

    static public View createTemporaryCustomView(StatusBar statusBar, String pathToShapeModel)
    {
        SmallBodyConfig config = new SmallBodyConfig();
        config.customName = pathToShapeModel;
        config.customTemporary = true;
        config.author = ShapeModelAuthor.CUSTOM;
        return new View(statusBar, config);
    }
}
