package edu.jhuapl.near.gui.rq36;

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
import edu.jhuapl.near.gui.TopoPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.MapletBoundaryCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.rq36.RQ36;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.MapletBoundaryPopupMenu;
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
public class RQ36Viewer extends Viewer
{
    public static final String NAME = "RQ36";

    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private ModelInfoWindowManager infoPanelManager;
    private boolean initialized = false;

    public RQ36Viewer(StatusBar statusBar)
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
        controlPanel.addTab("RQ36", new SmallBodyControlPanel(modelManager, "RQ36"));
        if (Configuration.isAPLVersion())
        {
            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Images", new CustomImagesPanel(modelManager, infoPanelManager, pickManager, renderer, true, getUniqueName()));
            controlPanel.addTab("Mapmaker", new TopoPanel(modelManager, pickManager, "/RQ36/mapmaker.zip"));
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

        SmallBodyModel smallBodyModel = new RQ36();

        String[] gridFiles = {
            "/RQ36/coordinate_grid_res0.vtk.gz",
            "/RQ36/coordinate_grid_res1.vtk.gz",
            "/RQ36/coordinate_grid_res2.vtk.gz",
            "/RQ36/coordinate_grid_res3.vtk.gz"
        };
        Graticule graticule = new Graticule(smallBodyModel, gridFiles, true);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.MAPLET_BOUNDARY, new MapletBoundaryCollection(smallBodyModel));

        modelManager.setModels(allModels);

    }

    private void setupPopupManager()
    {
        popupManager = new PopupManager(modelManager, infoPanelManager, renderer);

        PopupMenu popupMenu = new MapletBoundaryPopupMenu(modelManager, renderer);
        popupManager.registerPopup(modelManager.getModel(ModelNames.MAPLET_BOUNDARY), popupMenu);
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
