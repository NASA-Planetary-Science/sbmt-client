package edu.jhuapl.near.gui.gaspra;

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
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ImageBoundaryCollection;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.SmallBodyImageMapCollection;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.gaspra.SSIGaspraImage;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
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
public class GaspraViewer extends Viewer
{
    public static final String NAME = "Gaspra";

    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private ModelInfoWindowManager infoPanelManager;
    private boolean initialized = false;

    public GaspraViewer(StatusBar statusBar)
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
                if (model instanceof SSIGaspraImage)
                {
                    ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
                    ImageBoundaryCollection imageBoundaries = (ImageBoundaryCollection)modelManager.getModel(ModelNames.IMAGE_BOUNDARIES);
                    return new ImageInfoPanel((SSIGaspraImage)model, images, imageBoundaries);
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
        controlPanel.addTab(NAME, new SmallBodyControlPanel(modelManager, NAME));
        if (Configuration.isAPLVersion())
        {
            //controlPanel.addTab("SSI", new SSIGaspraSearchPanel(modelManager, infoPanelManager, pickManager, renderer));
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

        SmallBodyModel smallBodyModel = new SimpleSmallBody(
                NAME,
                "/THOMAS/GASPRA/951gaspra.llr.gz",
                "/THOMAS/GASPRA/gaspra_image_map.png");
        Graticule graticule = new Graticule(smallBodyModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));
        allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel));
        allModels.put(ModelNames.IMAGE_BOUNDARIES, new ImageBoundaryCollection(smallBodyModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
        allModels.put(ModelNames.SMALL_BODY_IMAGE_MAP, new SmallBodyImageMapCollection(smallBodyModel));
        allModels.put(ModelNames.GRATICULE, graticule);

        modelManager.setModels(allModels);

    }

    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager);

        ImageCollection images = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        ImageBoundaryCollection imageBoundaries = (ImageBoundaryCollection)modelManager.getModel(ModelNames.IMAGE_BOUNDARIES);
        ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);

        PopupMenu popupMenu = new ImagePopupMenu(images, imageBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.IMAGE_BOUNDARIES), popupMenu);

        popupMenu = new ImagePopupMenu(images, imageBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.IMAGES), popupMenu);

        popupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager);
        popupManager.registerPopup(modelManager.getModel(ModelNames.COLOR_IMAGES), popupMenu);
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
        return "Thomas";
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }
}
