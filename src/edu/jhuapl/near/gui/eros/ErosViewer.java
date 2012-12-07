package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.CustomImagesPanel;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.TopoPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.MapletBoundaryCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.Eros;
import edu.jhuapl.near.model.eros.ErosGraticule;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NLRBrowseDataCollection;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection;
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
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class ErosViewer extends Viewer
{
    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private ModelInfoWindowManager infoPanelManager;

    public ErosViewer(StatusBar statusBar)
    {
        super(new BorderLayout());

        setupModelManager();

        infoPanelManager = new ModelInfoWindowManager(modelManager);

        renderer = new Renderer(modelManager);

        setupPopupManager();

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab(Eros.NAME, new SmallBodyControlPanel(modelManager, Eros.NAME));
        controlPanel.addTab("MSI", new MSISearchPanel(modelManager, infoPanelManager, pickManager, renderer));
        controlPanel.addTab("NIS", new NISSearchPanel(modelManager, infoPanelManager, pickManager));
        controlPanel.addTab("NLR", new NLRPanel(modelManager, pickManager, renderer));
        if (Configuration.isAPLVersion())
        {
            controlPanel.addTab("Lineament", new LineamentControlPanel(modelManager));
            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, pickManager, renderer));
            controlPanel.addTab("Mapmaker", new TopoPanel(modelManager, pickManager, "/MSI/mapmaker.zip"));
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
        splitPane.setOneTouchExpandable(true);

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

        this.add(splitPane, BorderLayout.CENTER);
    }

    private void setupModelManager()
    {
        modelManager = new ModelManager();

        SmallBodyModel erosModel = new Eros();
        Graticule graticule = new ErosGraticule(erosModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, erosModel);
        allModels.put(ModelNames.LINEAMENT, new LineamentModel());
        allModels.put(ModelNames.IMAGES, new ImageCollection(erosModel));
        allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(erosModel));
        allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(erosModel));
        allModels.put(ModelNames.NIS_SPECTRA, new NISSpectraCollection(erosModel));
        allModels.put(ModelNames.LIDAR_BROWSE, new NLRBrowseDataCollection());
        allModels.put(ModelNames.LIDAR_SEARCH, new NLRSearchDataCollection(erosModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(erosModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(erosModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(erosModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(erosModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(erosModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(erosModel));
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.MAPLET_BOUNDARY, new MapletBoundaryCollection(erosModel));

        modelManager.setModels(allModels);
    }

    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager, infoPanelManager, renderer);

        PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
        popupManager.registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);

        ImageCollection msiImages = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        PerspectiveImageBoundaryCollection msiBoundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        ColorImageCollection msiColorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
        NLRSearchDataCollection lidarSearch = (NLRSearchDataCollection)modelManager.getModel(ModelNames.LIDAR_SEARCH);

        popupMenu = new ImagePopupMenu(msiImages, msiBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

        popupMenu = new ColorImagePopupMenu(msiColorImages, infoPanelManager);
        popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);

        popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
        popupManager.registerPopup(modelManager.getModel(ModelNames.NIS_SPECTRA), popupMenu);

        popupMenu = new LidarPopupMenu(lidarSearch, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.LIDAR_SEARCH), popupMenu);

        popupMenu = new MapletBoundaryPopupMenu(modelManager, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.MAPLET_BOUNDARY), popupMenu);
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public String getDisplayName()
    {
        return Eros.NAME;
    }

    public String getSubmenu()
    {
        return Eros.CATEGORY;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public PickManager getPickManager()
    {
        return pickManager;
    }
}
