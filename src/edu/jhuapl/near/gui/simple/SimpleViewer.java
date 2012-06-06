package edu.jhuapl.near.gui.simple;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.CylindricalImageCollection;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.simple.SimpleSmallBody;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.Configuration;

public class SimpleViewer extends Viewer
{
    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private boolean initialized = false;
    private String name;
    private String pathToSmallBodyFileOnServer;
    private String imageMap;

    public SimpleViewer(
            StatusBar statusBar,
            String name,
            String pathToSmallBodyFileOnServer)
    {
        this(statusBar, name, pathToSmallBodyFileOnServer, null);
    }

    public SimpleViewer(
            StatusBar statusBar,
            String name,
            String pathToSmallBodyFileOnServer,
            String imageMap)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.name = name;
        this.pathToSmallBodyFileOnServer = pathToSmallBodyFileOnServer;
        this.imageMap = imageMap;
    }

    public void initialize()
    {
        if (initialized)
            return;

        setupModelManager();

        renderer = new Renderer(modelManager);

        popupManager = new PopupManager(modelManager);

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab(name, new SmallBodyControlPanel(modelManager, name));
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

        SmallBodyModel smallBodyModel = new SimpleSmallBody(
                name, pathToSmallBodyFileOnServer, imageMap);
        Graticule graticule = new Graticule(smallBodyModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
        allModels.put(ModelNames.GRATICULE, graticule);
        if (imageMap != null)
            allModels.put(ModelNames.CYLINDRICAL_IMAGES, new CylindricalImageCollection(smallBodyModel));

        modelManager.setModels(allModels);

    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public String getDisplayName()
    {
        return name;
    }

    public String getSubmenu()
    {
        // Capitalize only the first letter
        int idxOfSlash = pathToSmallBodyFileOnServer.lastIndexOf('/');
        String submenu = pathToSmallBodyFileOnServer.substring(2, idxOfSlash).toLowerCase();
        return pathToSmallBodyFileOnServer.substring(1, 2) + submenu;
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
