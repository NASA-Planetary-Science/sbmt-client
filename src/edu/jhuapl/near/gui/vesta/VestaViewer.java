package edu.jhuapl.near.gui.vesta;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.CustomImagesPanel;
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
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.vesta.Vesta;
import edu.jhuapl.near.model.vesta.VestaGraticule;
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
public class VestaViewer extends Viewer
{
    public static final String NAME = "Vesta";

    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private ModelInfoWindowManager infoPanelManager;
    private boolean initialized = false;

    public VestaViewer(StatusBar statusBar)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
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
        controlPanel.addTab("Vesta", new SmallBodyControlPanel(modelManager, "Vesta"));
        if (Configuration.isAPLVersion())
        {
            controlPanel.addTab("FC", new FCSearchPanel(modelManager, infoPanelManager, pickManager, renderer));
            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, pickManager, renderer, true, getUniqueName()));
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

        SmallBodyModel vestaModel = new Vesta();
        Graticule graticule = new VestaGraticule(vestaModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, vestaModel);
        allModels.put(ModelNames.IMAGES, new ImageCollection(vestaModel));
        allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(vestaModel));
        allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(vestaModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(vestaModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(vestaModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(vestaModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(vestaModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(vestaModel));
        allModels.put(ModelNames.GRATICULE, graticule);

        modelManager.setModels(allModels);

    }

    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager, infoPanelManager, renderer);

        ImageCollection fcImages = (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        PerspectiveImageBoundaryCollection fcBoundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        ColorImageCollection fcColorImages = (ColorImageCollection)modelManager.getModel(ModelNames.COLOR_IMAGES);

        PopupMenu popupMenu = new ImagePopupMenu(fcImages, fcBoundaries, infoPanelManager, renderer, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

        popupMenu = new ColorImagePopupMenu(fcColorImages, infoPanelManager);
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
