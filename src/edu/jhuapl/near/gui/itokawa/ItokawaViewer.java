package edu.jhuapl.near.gui.itokawa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.ImageInfoPanel;
import edu.jhuapl.near.gui.ModelInfoWindow;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.gui.eros.NISSpectrumInfoPanel;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PerspectiveImageCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.model.itokawa.AmicaImage;
import edu.jhuapl.near.model.itokawa.HayLidarBrowseDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarSearchDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarUnfilteredSearchDataCollection;
import edu.jhuapl.near.model.itokawa.Itokawa;
import edu.jhuapl.near.model.itokawa.ItokawaGraticule;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.util.Configuration;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class ItokawaViewer extends Viewer
{
    public static final String NAME = "Itokawa";

    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private boolean initialized = false;
    private ModelInfoWindowManager infoPanelManager;

    public ItokawaViewer(StatusBar statusBar)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
    }

    public void initialize()
    {
        if (initialized)
            return;

        setupModelManager();

        infoPanelManager = new ModelInfoWindowManager(modelManager)
        {
            public ModelInfoWindow createModelInfoWindow(Model model,
                    ModelManager modelManager)
            {
                if (model instanceof AmicaImage)
                {
                    PerspectiveImageCollection amicaImages = (PerspectiveImageCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGES);
                    PerspectiveImageBoundaryCollection amicaBoundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                    return new ImageInfoPanel((AmicaImage)model, amicaImages, amicaBoundaries);
                }
                else if (model instanceof NISSpectrum)
                {
                    return new NISSpectrumInfoPanel((NISSpectrum)model, modelManager);
                }
                else
                {
                    return null;
                }
            }
        };

        renderer = new Renderer(modelManager);

        setupPopupManager();

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab("Itokawa", new SmallBodyControlPanel(modelManager, "Itokawa"));
        controlPanel.addTab("AMICA", new AmicaSearchPanel(modelManager, infoPanelManager, pickManager, renderer));
        controlPanel.addTab("LIDAR", new HayLidarPanel(modelManager, pickManager, renderer));
        if (Configuration.isAPLVersion())
        {
            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
        splitPane.setOneTouchExpandable(true);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(800, 800));
        controlPanel.setMinimumSize(new Dimension(320, 100));
        controlPanel.setPreferredSize(new Dimension(320, 800));

        this.add(splitPane, BorderLayout.CENTER);

        initialized = true;
    }

    private void setupModelManager()
    {
        modelManager = new ModelManager();

        SmallBodyModel itokawaModel = new Itokawa();
        Graticule graticule = new ItokawaGraticule(itokawaModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, itokawaModel);
        allModels.put(ModelNames.PERSPECTIVE_IMAGES, new PerspectiveImageCollection(itokawaModel));
        allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(itokawaModel));
        allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(itokawaModel));
        allModels.put(ModelNames.HAYLIDAR_BROWSE, new HayLidarBrowseDataCollection());
        allModels.put(ModelNames.HAYLIDAR_SEARCH, new HayLidarSearchDataCollection(itokawaModel));
        allModels.put(ModelNames.HAYLIDAR_SEARCH_UNFILTERED, new HayLidarUnfilteredSearchDataCollection(itokawaModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(itokawaModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(itokawaModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(itokawaModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(itokawaModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(itokawaModel));
        allModels.put(ModelNames.GRATICULE, graticule);

        modelManager.setModels(allModels);

    }

    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager);

        PerspectiveImageCollection amicaImages = (PerspectiveImageCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGES);
        PerspectiveImageBoundaryCollection amicaBoundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        ColorImageCollection amicaColorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);
        HayLidarSearchDataCollection lidarSearch = (HayLidarSearchDataCollection)modelManager.getModel(ModelNames.HAYLIDAR_SEARCH);
        HayLidarUnfilteredSearchDataCollection lidarSearchUnfiltered =
            (HayLidarUnfilteredSearchDataCollection)modelManager.getModel(ModelNames.HAYLIDAR_SEARCH_UNFILTERED);

        PopupMenu popupMenu = new ImagePopupMenu(amicaImages, amicaBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

        popupMenu = new ImagePopupMenu(amicaImages, amicaBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGES), popupMenu);

        popupMenu = new ColorImagePopupMenu(amicaColorImages, infoPanelManager);
        popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);

        popupMenu = new LidarPopupMenu(lidarSearch, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.HAYLIDAR_SEARCH), popupMenu);

        popupMenu = new LidarPopupMenu(lidarSearchUnfiltered, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.HAYLIDAR_SEARCH_UNFILTERED), popupMenu);
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public String getDisplayName()
    {
        return NAME;
    }

    public String getSubmenu()
    {
        return "Gaskell";
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
