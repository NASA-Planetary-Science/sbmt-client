package edu.jhuapl.sbmt.model;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import com.google.common.collect.ImmutableList;

import vtk.vtkCamera;

import edu.jhuapl.saavtk.colormap.Colorbar;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.structure.gui.StructureMainPanel;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.BasicConfigInfo;
import edu.jhuapl.sbmt.client.BodyType;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.client.ShapeModelDataUsed;
import edu.jhuapl.sbmt.client.ShapeModelPopulation;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.dtm.controller.DEMPopupMenuActionListener;
import edu.jhuapl.sbmt.dtm.controller.ExperimentalDEMController;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerDEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerRemoteDEMCreator;
import edu.jhuapl.sbmt.dtm.ui.menu.DEMPopupMenu;
import edu.jhuapl.sbmt.gui.eros.LineamentControlPanel;
import edu.jhuapl.sbmt.gui.image.ui.images.ImageDefaultPickHandler;
import edu.jhuapl.sbmt.lidar.gui.LidarPanel;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
import edu.jhuapl.sbmt.stateHistory.controllers.StateHistoryController;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.EmptyMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.TrackedMetadataManager;
import crucible.crust.metadata.impl.Utilities;

public abstract class AbstractBodyView extends View implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;
    private final TrackedMetadataManager stateManager;
    protected final Map<String, MetadataManager> metadataManagers;
    private Colorbar smallBodyColorbar;
	private BasicConfigInfo configInfo;
	protected SmallBodyModel smallBodyModel;

	public AbstractBodyView(StatusBar statusBar, BasicConfigInfo configInfo)
	{
		super(statusBar, null);
		this.configInfo = configInfo;
		uniqueName = configInfo.getUniqueName();
		shapeModelName = configInfo.getShapeModelName();
    	this.stateManager = TrackedMetadataManager.of("View " + configInfo.getUniqueName());
		this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.getConfigURL();
		initializeStateManager();
	}

	/**
	 * By default a view should be created empty. Only when the user requests to
	 * show a particular View, should the View's contents be created in order to
	 * reduce memory and startup time. Therefore, this function should be called
	 * prior to first time the View is shown in order to cause it
     */
    public AbstractBodyView(StatusBar statusBar, SmallBodyViewConfig smallBodyConfig)
    {
        super(statusBar, smallBodyConfig);
		this.configInfo = new BasicConfigInfo(smallBodyConfig);
		uniqueName = configInfo.getUniqueName();
		shapeModelName = configInfo.getShapeModelName();
        this.stateManager = TrackedMetadataManager.of("View " + getUniqueName());
        this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.getConfigURL();

        initializeStateManager();
    }

    @Override
	protected void initialize() throws InvocationTargetException, InterruptedException
	{
		if (configInfo != null && (getConfig() == null))
		{
			setConfig(SmallBodyViewConfig.getSmallBodyConfig(configInfo));
		}

		// TODO Auto-generated method stub
		super.initialize();
	}

	@Override
	public String getUniqueName()
	{
		if (uniqueName != null) return uniqueName;
		return super.getUniqueName();
	}

	@Override
	public boolean isAccessible()
	{
		if (configURL != null)
		{
			return FileCache.instance().isAccessible(configURL);
		}
		return super.isAccessible();
	}

	@Override
	public String getShapeModelName()
	{
		if (configURL != null)
		{
			String[] parts = uniqueName.split("/");
			return parts[1];
		}
		return super.getShapeModelName();
	}

    public SmallBodyViewConfig getPolyhedralModelConfig()
    {
        return (SmallBodyViewConfig)super.getConfig();
    }

    @Override
    public String getPathRepresentation()
    {
		ShapeModelType author;
		String modelLabel;
		BodyType type;
		ShapeModelPopulation population;
		ShapeModelDataUsed dataUsed;
		ShapeModelBody body;
		if (configInfo == null)
		{
        SmallBodyViewConfig config = getPolyhedralModelConfig();
			author = config.author;
			modelLabel = config.modelLabel;
			type = config.type;
			population = config.population;
			dataUsed = config.dataUsed;
			body = config.body;
		}
		else
		{
			author = configInfo.getAuthor();
			modelLabel = configInfo.getModelLabel();
			type = configInfo.getType();
			population = configInfo.getPopulation();
			dataUsed = configInfo.getDataUsed();
			body = configInfo.getBody();
		}
        if (ShapeModelType.CUSTOM == author)
        {
            return Configuration.getAppTitle() + " - " + ShapeModelType.CUSTOM + " > " + modelLabel;
        }
        else
        {
            String path = type.str;
            if (population != null && population != ShapeModelPopulation.NA)
                path += " > " + population;
            path += " > " + body;
            if (dataUsed != null && dataUsed != ShapeModelDataUsed.NA)
                path += " > " + dataUsed;
            path += " > " + getDisplayName();
            return Configuration.getAppTitle() + " - " + path;
        }
    }

    @Override
    public String getDisplayName()
    {
    	String result = "";
		if (configInfo == null)
		{
    	SmallBodyViewConfig config = getPolyhedralModelConfig();
    	if (config.modelLabel != null)
    	    result = config.modelLabel;
    	else if (config.author == null)
    	    result = config.body.toString();
    	else
    	    result = config.author.toString();

    	if (config.version != null)
    	    result = result + " (" + config.version + ")";

		}
		else
		{
			if (configInfo.getModelLabel() != null)
				result = configInfo.getModelLabel();
			else if (configInfo.getAuthor() == null)
				result = configInfo.getBody().toString();
			else
				result = configInfo.getAuthor().toString();

			if (configInfo.getVersion() != null)
				result = result + " (" + configInfo.getVersion() + ")";
		}


    	return result;
    }

    @Override
    public String getModelDisplayName()
    {
		ShapeModelBody body = null;
		body = configInfo == null ?  getConfig().body : configInfo.getBody();
        return body != null ? body + " / " + getDisplayName() : getDisplayName();
    }


	static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

    static public HashMap<ModelNames, Model> createSpectralModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;

        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));

        SpectraCollection collection = new SpectraCollection(smallBodyModel);

        models.put(ModelNames.SPECTRA, collection);
        return models;
    }


    static public Graticule createGraticule(SmallBodyModel smallBodyModel)
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig)smallBodyModel.getSmallBodyConfig();
        ShapeModelType author = config.author;

        if (ShapeModelType.GASKELL == author && smallBodyModel.getNumberResolutionLevels() == 4)
        {
            String[] graticulePaths = new String[]{
                    config.rootDirOnServer + "/coordinate_grid_res0.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res1.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res2.vtk.gz",
                    config.rootDirOnServer + "/coordinate_grid_res3.vtk.gz"
            };

            return new Graticule(smallBodyModel, graticulePaths);
        }
        else if (ShapeModelType.CUSTOM == author && !config.customTemporary)
        {
            return new CustomGraticule(smallBodyModel);
        }

        return new Graticule(smallBodyModel);
    }

	public BasicConfigInfo getConfigInfo()
	{
		return configInfo;
	}

    @Override
    protected void setupPickManager()
    {
		PickManager tmpPickManager = new PickManager(getRenderer(), getModelManager());
		PickUtil.installDefaultPickHandler(tmpPickManager, getStatusBar(), getRenderer(), getModelManager());
		setPickManager(tmpPickManager);

		// Manually register the PopupManager with the PickManager
		tmpPickManager.getDefaultPicker().addListener(getPopupManager());

		// TODO: This should be moved out of here to a logical relevant location
		tmpPickManager.getDefaultPicker().addListener(new ImageDefaultPickHandler(getModelManager()));
    }

    @Override
    protected void setupInfoPanelManager()
    {
        setInfoPanelManager(new SbmtInfoWindowManager(getModelManager(), getStatusBar()));
    }

    @Override
    protected void setupSpectrumPanelManager()
    {
		SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
		SpectrumBoundaryCollection spectrumBoundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);

		PopupMenu spectralImagesPopupMenu =
    	        new SpectrumPopupMenu(spectrumCollection, spectrumBoundaryCollection, getModelManager(), null, null);
		setSpectrumPanelManager(new SbmtSpectrumWindowManager(getModelManager(), spectralImagesPopupMenu));
    }

    @Override
    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
        smallBodyColorbar = new Colorbar(renderer);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
        {
            renderer.setProps(getModelManager().getProps());

            if (smallBodyColorbar==null)
                return;

            PolyhedralModel sbModel=(PolyhedralModel)getModelManager().getModel(ModelNames.SMALL_BODY);
            if (sbModel.isColoringDataAvailable() && sbModel.getColoringIndex()>=0)
            {
                if (!smallBodyColorbar.isVisible())
                    smallBodyColorbar.setVisible(true);
                smallBodyColorbar.setColormap(sbModel.getColormap());
                int index = sbModel.getColoringIndex();
                String title = sbModel.getColoringName(index).trim();
                String units = sbModel.getColoringUnits(index).trim();
                if (units != null && !units.isEmpty())
                {
                    title += " (" + units + ")";
                }
                smallBodyColorbar.setTitle(title);
                if (renderer.getRenderWindowPanel().getRenderer().HasViewProp(smallBodyColorbar.getActor())==0)
                    renderer.getRenderWindowPanel().getRenderer().AddActor(smallBodyColorbar.getActor());
                smallBodyColorbar.getActor().SetNumberOfLabels(sbModel.getColormap().getNumberOfLabels());
            }
            else
                smallBodyColorbar.setVisible(false);

        }
        else
        {
            renderer.getRenderWindowPanel().Render();
        }
    }

	private static final Version METADATA_VERSION = Version.of(1, 1); // Nested CURRENT_TAB stored as an array of strings.
    private static final Version METADATA_VERSION_1_0 = Version.of(1, 0); // Top level CURRENT_TAB only stored as a single string.
    private static final Key<Map<String, Metadata>> METADATA_MANAGERS_KEY = Key.of("metadataManagers");
    private static final Key<Metadata> MODEL_MANAGER_KEY = Key.of("modelState");
    private static final Key<Integer> RESOLUTION_LEVEL_KEY = Key.of("resolutionLevel");
    private static final Key<double[]> POSITION_KEY = Key.of("cameraPosition");
    private static final Key<double[]> UP_KEY = Key.of("cameraUp");
    private static final Key<List<String>> CURRENT_TAB_KEY = Key.of("currentTab");
    private static final Key<String> CURRENT_TAB_KEY_1_0 = Key.of("currentTab");

    @Override
    public void initializeStateManager()
    {
		if (!stateManager.isRegistered())
		{
            stateManager.register(new MetadataManager() {

                @Override
                public Metadata store()
                {
                    if (!isInitialized())
                    {
                        return EmptyMetadata.instance();
                    }

                    SettableMetadata result = SettableMetadata.of(METADATA_VERSION);

                    result.put(RESOLUTION_LEVEL_KEY, getModelManager().getPolyhedralModel().getModelResolution());

                    Renderer localRenderer = AbstractBodyView.this.getRenderer();
					if (localRenderer != null)
					{
                        RenderPanel panel = (RenderPanel) localRenderer.getRenderWindowPanel();
                        vtkCamera camera = panel.getActiveCamera();
                        result.put(POSITION_KEY, camera.GetPosition());
                        result.put(UP_KEY, camera.GetViewUp());
                    }

                    // Redmine #1320/1439: this is what used to be here to save the state of imaging search panels.
//                    if (!searchPanelMap.isEmpty())
//                    {
//                        ImmutableSortedMap.Builder<String, Metadata> builder = ImmutableSortedMap.naturalOrder();
//                        for (Entry<String, ImagingSearchPanel> entry : searchPanelMap.entrySet())
//                        {
//                            MetadataManager imagingStateManager = entry.getValue().getMetadataManager();
//                            if (imagingStateManager != null)
//                            {
//                                builder.put(entry.getKey(), imagingStateManager.store());
//                            }
//                        }
//                        result.put(imagingKey, builder.build());
//                    }
                    Map<String, Metadata> metadata = Utilities.bulkStore(metadataManagers);
                    result.put(METADATA_MANAGERS_KEY, metadata);

                    ModelManager modelManager = getModelManager();
                    if (modelManager instanceof MetadataManager)
                    {
                    	result.put(MODEL_MANAGER_KEY, ((MetadataManager) modelManager).store());
                    }

                    JTabbedPane controlPanel = getControlPanel();
                    if (controlPanel != null)
                    {
                    	List<String> currentTabs = new ArrayList<>();
                    	compileCurrentTabs(controlPanel, currentTabs);
                        result.put(CURRENT_TAB_KEY, currentTabs);
                    }
                    return result;
                }

                @Override
                public void retrieve(Metadata state)
                {
					try
                    {
                    initialize();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        return;
                    }

                    Version serializedVersion = state.getVersion();

                    if (state.hasKey(RESOLUTION_LEVEL_KEY))
                    {
                        try
                        {
                            getModelManager().getPolyhedralModel().setModelResolution(state.get(RESOLUTION_LEVEL_KEY));
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                        Renderer localRenderer = AbstractBodyView.this.getRenderer();
                        if (localRenderer != null)
                        {
                            RenderPanel panel = (RenderPanel) localRenderer.getRenderWindowPanel();
                            vtkCamera camera = panel.getActiveCamera();
                            camera.SetPosition(state.get(POSITION_KEY));
                            camera.SetViewUp(state.get(UP_KEY));
                            panel.resetCameraClippingRange();
                            panel.Render();
                        }

                    // Redmine #1320/1439: this is what used to be here to retrieve the state of imaging search panels.
//                    if (!searchPanelMap.isEmpty())
//                    {
//                        SortedMap<String, Metadata> metadataMap = state.get(imagingKey);
//                        for (Entry<String, ImagingSearchPanel> entry : searchPanelMap.entrySet())
//                        {
//                            Metadata imagingMetadata = metadataMap.get(entry.getKey());
//                            if (imagingMetadata != null)
//                            {
//                                MetadataManager imagingStateManager = entry.getValue().getMetadataManager();
//                                imagingStateManager.retrieve(imagingMetadata);
//                            }
//                        }
//                    }
                    Map<String, Metadata> metadata = state.get(METADATA_MANAGERS_KEY);
                    Utilities.bulkRetrieve(metadataManagers, metadata);

					if (state.hasKey(MODEL_MANAGER_KEY))
					{
                    	ModelManager modelManager = getModelManager();
                    	if (modelManager instanceof MetadataManager)
                    	{
                    		((MetadataManager) modelManager).retrieve(state.get(MODEL_MANAGER_KEY));
                    	}
                    }

                    List<String> currentTabs = ImmutableList.of();
                    if (serializedVersion.compareTo(METADATA_VERSION_1_0) > 0)
                    {
                    	currentTabs = state.get(CURRENT_TAB_KEY);
                    }
                    else if (state.hasKey(CURRENT_TAB_KEY_1_0))
                    {
                    	currentTabs = ImmutableList.of(state.get(CURRENT_TAB_KEY_1_0));
                    }

                    restoreCurrentTabs(getControlPanel(), currentTabs);
                }

                private void compileCurrentTabs(JTabbedPane tabbedPane, List<String> tabs)
                {
                	int selectedIndex = tabbedPane.getSelectedIndex();
                	if (selectedIndex >= 0)
                	{
                		tabs.add(tabbedPane.getTitleAt(selectedIndex));
                		Component component = tabbedPane.getSelectedComponent();
						if (component instanceof JTabbedPane)
						{
                			compileCurrentTabs((JTabbedPane) component, tabs);
                		}
                	}
                }

				private void restoreCurrentTabs(JTabbedPane tabbedPane, List<String> tabTitles)
				{
                	if (tabbedPane != null)
                	{
                		if (!tabTitles.isEmpty())
                		{
                			String title = tabTitles.get(0);
                			for (int index = 0; index < tabbedPane.getTabCount(); ++index)
                			{
                				String tabTitle = tabbedPane.getTitleAt(index);
                				if (title.equalsIgnoreCase(tabTitle))
                				{
                					tabbedPane.setSelectedIndex(index);
                					Component component = tabbedPane.getSelectedComponent();
                					if (component instanceof JTabbedPane)
                					{
                						restoreCurrentTabs((JTabbedPane) component, tabTitles.subList(1, tabTitles.size()));
                					}
                					break;
                				}
                			}
                		}
                	}
                }

            });
        }
    }

    protected void addLidarTab()
    {
    	if (getPolyhedralModelConfig().hasLidarData)
        {
            JComponent component = new LidarPanel(getModelManager(), getPickManager(), getRenderer(), getPolyhedralModelConfig());
            addTab(getPolyhedralModelConfig().lidarInstrumentName.toString(), component);
        }
    }

    protected void addLineamentTab()
    {
    	if (getPolyhedralModelConfig().hasLineamentData)
        {
            JComponent component = new LineamentControlPanel(getModelManager());
            addTab("Lineament", component);
        }
    }

    protected void addStructuresTab()
    {
		addTab("Structures", new StructureMainPanel(getPickManager(), getRenderer(), getStatusBar(), getModelManager()));
    }

	protected void addDEMTab()
	{
		DEMCollection dems = (DEMCollection) getModel(ModelNames.DEM);
		DEMBoundaryCollection demBoundaries = (DEMBoundaryCollection) getModel(ModelNames.DEM_BOUNDARY);
		DEMPopupMenu demPopupMenu = new DEMPopupMenu(getModelManager().getPolyhedralModel(), dems, demBoundaries,
				renderer, getRenderer(), new DEMPopupMenuActionListener(dems, demBoundaries));
		registerPopup(getModel(ModelNames.DEM), demPopupMenu);
		registerPopup(getModel(ModelNames.DEM_BOUNDARY), demPopupMenu);

		DEMCreator creationTool = null;
		if (getPolyhedralModelConfig().hasRemoteMapmaker) // config builds DEMs
															// from the server
		{
			creationTool = new MapmakerRemoteDEMCreator(
					Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()),
					getPolyhedralModelConfig());
		}
		else if (getPolyhedralModelConfig().hasMapmaker) // config builds DEMs
															// locally.
		{
			creationTool = new MapmakerDEMCreator(
					Paths.get(getPolyhedralModelConfig().rootDirOnServer + File.separator + "mapmaker.zip"),
					Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()));
		}

		addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), creationTool,
				getPolyhedralModelConfig(), getRenderer()).getPanel());
	}

    protected void addStateHistoryTab()
    {
        if (getPolyhedralModelConfig().hasStateHistory)
        {
            StateHistoryController controller = new StateHistoryController(getModelManager(), getRenderer());
            addTab("Observing Conditions", controller.getView());
        }
    }

}
