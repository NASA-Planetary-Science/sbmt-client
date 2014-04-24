package edu.jhuapl.near.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.eros.LineamentControlPanel;
import edu.jhuapl.near.gui.eros.NISSearchPanel;
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
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelFactory.Instrument;
import edu.jhuapl.near.model.ModelFactory.ModelConfig;
import edu.jhuapl.near.model.ModelFactory.ShapeModelAuthor;
import edu.jhuapl.near.model.ModelFactory.ShapeModelBody;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
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
    private ModelConfig modelConfig;
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
            ModelConfig modelConfig)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.modelConfig = modelConfig;
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
        controlPanel.addTab(modelConfig.getShapeModelName(), new SmallBodyControlPanel(modelManager, modelConfig.getShapeModelName()));

        if (modelConfig.hasPerspectiveImages)
        {
            // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
            if (Configuration.isAPLVersion() ||
                    modelConfig.name == ShapeModelBody.EROS ||
                    (modelConfig.name == ShapeModelBody.ITOKAWA && ShapeModelAuthor.GASKELL == modelConfig.author))
            {
                JComponent component = new ImagingSearchPanel(modelConfig, modelManager, infoPanelManager, pickManager, renderer);
                controlPanel.addTab(modelConfig.imageInstrumentName.toString(), component);
            }
        }

        if (modelConfig.hasSpectralData)
        {
            JComponent component = new NISSearchPanel(modelManager, infoPanelManager, pickManager);
            controlPanel.addTab(Instrument.NIS.toString(), component);
        }

        if (modelConfig.hasLidarData)
        {
            JComponent component = new LidarPanel(modelConfig, modelManager, pickManager, renderer);
            controlPanel.addTab(modelConfig.lidarInstrumentName.toString(), component);
        }

        if (Configuration.isAPLVersion())
        {
            if (modelConfig.hasLineamentData)
            {
                JComponent component = new LineamentControlPanel(modelManager);
                controlPanel.addTab("Lineament", component);
            }

            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, pickManager, renderer));

            if (modelConfig.hasMapmaker)
            {
                JComponent component = new MapmakerPanel(modelManager, pickManager, modelConfig.pathOnServer + "/mapmaker.zip");
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

        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(modelConfig);
        Graticule graticule = ModelFactory.createGraticule(smallBodyModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));

        if (modelConfig.hasPerspectiveImages)
        {
            allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel));
            allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
        }

        if (modelConfig.hasSpectralData)
        {
            allModels.put(ModelNames.SPECTRA, ModelFactory.createSpectralModel(smallBodyModel));
        }

        if (modelConfig.hasLidarData)
        {
            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
        }

        if (modelConfig.hasLineamentData)
        {
            allModels.put(ModelNames.LINEAMENT, ModelFactory.createLineament());
        }

        if (modelConfig.hasMapmaker)
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

        if (modelConfig.hasPerspectiveImages)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);

            PopupMenu popupMenu = new ImagePopupMenu(images, boundaries, infoPanelManager, renderer, renderer);
            popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

            popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);
        }

        if (modelConfig.hasSpectralData)
        {
            PopupMenu popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.SPECTRA), popupMenu);
        }

        if (modelConfig.hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, renderer);
            popupManager.registerPopup(lidarSearch, popupMenu);
        }

        if (modelConfig.hasLineamentData)
        {
            PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
            popupManager.registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);
        }

        if (modelConfig.hasMapmaker)
        {
            PopupMenu popupMenu = new MapletBoundaryPopupMenu(modelManager, renderer);
            popupManager.registerPopup(modelManager.getModel(ModelNames.MAPLET_BOUNDARY), popupMenu);
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
        return modelConfig.getUniqueName();
    }


    /**
     * Return the display name for this view (the name to be shown in the menu).
     * This name need not be unique among all views.
     * @return
     */
    public String getDisplayName()
    {
        if (modelConfig.author == ShapeModelAuthor.CUSTOM)
            return modelConfig.customName;
        else if (modelConfig.author == null)
            return modelConfig.name.toString();
        else
            return modelConfig.author.toString();
    }

    public ModelConfig getModelConfig()
    {
        return modelConfig;
    }

    static public View createCustomView(StatusBar statusBar, String name)
    {
        ModelConfig config = new ModelConfig();
        config.customName = name;
        config.author = ShapeModelAuthor.CUSTOM;
        return new View(statusBar, config);
    }
}
