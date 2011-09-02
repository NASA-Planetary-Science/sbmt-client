package edu.jhuapl.near.gui.custom;

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
import edu.jhuapl.near.model.SmallBodyImageMapCollection;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.custom.CustomGraticule;
import edu.jhuapl.near.model.custom.CustomShapeModel;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.util.Configuration;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class CustomViewer extends Viewer
{
    private String name;
    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private PopupManager popupManager;
    private StatusBar statusBar;
    private boolean initialized = false;

    public CustomViewer(StatusBar statusBar, String name)
    {
        super(new BorderLayout());
        this.statusBar = statusBar;
        this.name = name;
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

        SmallBodyModel customModel = new CustomShapeModel(name);
        Graticule graticule = new CustomGraticule(customModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, customModel);
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(customModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(customModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(customModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(customModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(customModel));
        allModels.put(ModelNames.SMALL_BODY_IMAGE_MAP, new SmallBodyImageMapCollection(customModel));
        allModels.put(ModelNames.GRATICULE, graticule);

        modelManager.setModels(allModels);

    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public String getName()
    {
        return name;
    }

    public ModelManager getModelManager()
    {
        return modelManager;
    }
}
