package edu.jhuapl.near.gui.eros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import edu.jhuapl.near.gui.ModelInfoWindow;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.SmallBodyControlPanel;
import edu.jhuapl.near.gui.StatusBar;
import edu.jhuapl.near.gui.StructuresControlPanel;
import edu.jhuapl.near.gui.Viewer;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.LineamentModel;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIColorImageCollection;
import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.model.eros.MSIImageCollection;
import edu.jhuapl.near.model.eros.MapletBoundaryCollection;
import edu.jhuapl.near.model.eros.NISSpectraCollection;
import edu.jhuapl.near.model.eros.NISSpectrum;
import edu.jhuapl.near.model.eros.NLRBrowseDataCollection;
import edu.jhuapl.near.model.eros.NLRSearchDataCollection;
import edu.jhuapl.near.pick.PickManager;
import edu.jhuapl.near.popupmenus.eros.ErosPopupManager;
import edu.jhuapl.near.util.Configuration;

/**
 * This class contains the "main" function called at the start of the program.
 * This class sets up the top level window and instantiates all the "managers" used
 * through out the program.
 * @author kahneg1
 *
 */
public class ErosViewer extends Viewer
{
    public static final String NAME = "Eros";

    private JSplitPane splitPane;
    private Renderer renderer;
    private JTabbedPane controlPanel;
    private ModelManager modelManager;
    private PickManager pickManager;
    private ErosPopupManager popupManager;
    private ModelInfoWindowManager infoPanelManager;

    public ErosViewer(StatusBar statusBar)
    {
        super(new BorderLayout());

        setupModelManager();

        infoPanelManager = new ModelInfoWindowManager(modelManager)
        {
            public ModelInfoWindow createModelInfoWindow(Model model,
                    ModelManager modelManager)
            {
                if (model instanceof MSIImage)
                    return new MSIImageInfoPanel((MSIImage)model, modelManager);
                else if (model instanceof NISSpectrum)
                    return new NISSpectrumInfoPanel((NISSpectrum)model, modelManager);
                else
                    return null;
            }
        };

        renderer = new Renderer(modelManager);

        popupManager = new ErosPopupManager(renderer, modelManager, infoPanelManager);

        pickManager = new PickManager(renderer, statusBar, modelManager, popupManager);

        controlPanel = new JTabbedPane();
        controlPanel.setBorder(BorderFactory.createEmptyBorder());
        controlPanel.addTab("Eros", new SmallBodyControlPanel(modelManager, "Eros"));
        controlPanel.addTab("MSI", new MSISearchPanel(modelManager, infoPanelManager, pickManager, renderer));
        controlPanel.addTab("NIS", new NISSearchPanel(modelManager, infoPanelManager, pickManager));
        controlPanel.addTab("NLR", new NLRPanel(modelManager, pickManager));
        if (Configuration.isAPLVersion())
        {
            controlPanel.addTab("Lineament", new LineamentControlPanel(modelManager));
            controlPanel.addTab("Structures", new StructuresControlPanel(modelManager, pickManager));
            controlPanel.addTab("Mapmaker", new TopoPanel(modelManager, pickManager));
        }

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                controlPanel, renderer);
        splitPane.setOneTouchExpandable(true);

        renderer.setMinimumSize(new Dimension(100, 100));
        renderer.setPreferredSize(new Dimension(800, 800));
        controlPanel.setMinimumSize(new Dimension(320, 100));
        controlPanel.setPreferredSize(new Dimension(320, 800));

        this.add(splitPane, BorderLayout.CENTER);
    }

    private void setupModelManager()
    {
        modelManager = new ModelManager();

        SmallBodyModel erosModel = ModelFactory.createErosBodyModel();
        Graticule graticule = ModelFactory.createErosGraticuleModel(erosModel);

        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.SMALL_BODY, erosModel);
        allModels.put(ModelNames.LINEAMENT, new LineamentModel());
        allModels.put(ModelNames.MSI_IMAGES, new MSIImageCollection(erosModel));
        allModels.put(ModelNames.MSI_COLOR_IMAGES, new MSIColorImageCollection(erosModel));
        allModels.put(ModelNames.MSI_BOUNDARY, new MSIBoundaryCollection(erosModel));
        allModels.put(ModelNames.NIS_SPECTRA, new NISSpectraCollection(erosModel));
        allModels.put(ModelNames.NLR_DATA_BROWSE, new NLRBrowseDataCollection());
        allModels.put(ModelNames.NLR_DATA_SEARCH, new NLRSearchDataCollection(erosModel));
        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(erosModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(erosModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(erosModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new RegularPolygonModel(erosModel,20,false,"Selection",ModelNames.CIRCLE_SELECTION));
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.MAPLET_BOUNDARY, new MapletBoundaryCollection(erosModel));

        modelManager.setModels(allModels);
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public String getName()
    {
        return NAME;
    }
}
