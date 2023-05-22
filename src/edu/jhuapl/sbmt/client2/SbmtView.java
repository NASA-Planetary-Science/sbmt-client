package edu.jhuapl.sbmt.client2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;

import vtk.vtkCamera;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.render.ConfigurableSceneNotifier;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.IPositionOrientationManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.gui.StructureMainPanel;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.common.client.SBMTInfoWindowManagerFactory;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.BasicConfigInfo;
import edu.jhuapl.sbmt.config.BodyType;
import edu.jhuapl.sbmt.config.ShapeModelDataUsed;
import edu.jhuapl.sbmt.config.ShapeModelPopulation;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.dem.gui.DemMainPanel;
import edu.jhuapl.sbmt.dtm.controller.DEMPopupMenuActionListener;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerDEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerRemoteDEMCreator;
import edu.jhuapl.sbmt.dtm.ui.menu.DEMPopupMenu;
import edu.jhuapl.sbmt.dtm.ui.menu.MapletBoundaryPopupMenu;
import edu.jhuapl.sbmt.gui.eros.LineamentControlPanel;
import edu.jhuapl.sbmt.gui.eros.LineamentPopupMenu;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.image2.controllers.ImageSearchController;
import edu.jhuapl.sbmt.image2.model.BasemapImageCollection;
import edu.jhuapl.sbmt.image2.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image2.ui.table.popup.ImageListPopupManager;
import edu.jhuapl.sbmt.lidar.gui.LidarPanel;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectraFactory;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectrumSearchController;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectrumTabbedPane;
import edu.jhuapl.sbmt.model.bennu.spectra.otes.OTESSpectrum;
import edu.jhuapl.sbmt.model.bennu.spectra.ovirs.OVIRSSpectrum;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.model.eros.nis.NEARSpectraFactory;
import edu.jhuapl.sbmt.model.eros.nis.NISSearchModel;
import edu.jhuapl.sbmt.model.eros.nis.NISSpectrum;
import edu.jhuapl.sbmt.model.phobos.controllers.MEGANEController;
import edu.jhuapl.sbmt.model.phobos.model.CumulativeMEGANECollection;
import edu.jhuapl.sbmt.model.phobos.model.MEGANECollection;
import edu.jhuapl.sbmt.model.ryugu.nirs3.H2SpectraFactory;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SearchModel;
import edu.jhuapl.sbmt.model.ryugu.nirs3.atRyugu.NIRS3Spectrum;
import edu.jhuapl.sbmt.pointing.PositionOrientationManager;
import edu.jhuapl.sbmt.pointing.spice.PositionOrientationManagerListener;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.ingestion.controller.KernelSelectionFrame;
import edu.jhuapl.sbmt.spectrum.controllers.custom.CustomSpectraSearchController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatisticsCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
import edu.jhuapl.sbmt.stateHistory.controllers.ObservationPlanningController;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;
import edu.jhuapl.sbmt.util.TimeUtil;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.EmptyMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.TrackedMetadataManager;
import crucible.crust.metadata.impl.Utilities;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

/**
 * A view is a container which contains a control panel and renderer as well as
 * a collection of managers. A view is unique to a specific body. This class is
 * used to build all built-in and custom views. All the configuration details of
 * all the built-in and custom views are contained in this class.
 */
public class SbmtView extends View implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;
    private final TrackedMetadataManager stateManager;
    private final Map<String, MetadataManager> metadataManagers;
	private BasicConfigInfo configInfo;
	private List<SmallBodyModel> smallBodyModels;
	private List<PositionOrientationManagerListener> pomListeners;
	private StateHistoryCollection historyCollection;
	private StateHistoryRendererManager rendererManager;
	private MEGANECollection meganeCollection;
	private CumulativeMEGANECollection cumulativeMeganeCollection;
	protected HashMap<ModelNames, List<Model>> allModels = new HashMap<>();
	private ObservationPlanningController planningController;

	public SbmtView(StatusNotifier aStatusNotifier, BasicConfigInfo configInfo)
	{
		super(aStatusNotifier, null);
		this.configInfo = configInfo;
		uniqueName = configInfo.getUniqueName();
		shapeModelName = configInfo.getShapeModelName();
    	this.stateManager = TrackedMetadataManager.of("View " + configInfo.getUniqueName());
		this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.getConfigURL();
		this.pomListeners = Lists.newArrayList();
		initializeStateManager();
	}

    /**
	 * By default a view should be created empty. Only when the user requests to
	 * show a particular View, should the View's contents be created in order to
	 * reduce memory and startup time. Therefore, this function should be called
	 * prior to first time the View is shown in order to cause it
     */
	public SbmtView(StatusNotifier aStatusNotifier, SmallBodyViewConfig smallBodyConfig)
    {
		super(aStatusNotifier, smallBodyConfig);
		this.configInfo = new BasicConfigInfo(smallBodyConfig, SbmtMultiMissionTool.getMission().isPublishedDataOnly());
		uniqueName = configInfo.getUniqueName();
		shapeModelName = configInfo.getShapeModelName();
        this.stateManager = TrackedMetadataManager.of("View " + getUniqueName());
        this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.getConfigURL();
		this.pomListeners = Lists.newArrayList();
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
		ShapeModelBody system;
		ShapeModelDataUsed dataUsed;
		ShapeModelBody body;
		if (configInfo == null)
		{
        SmallBodyViewConfig config = getPolyhedralModelConfig();
			author = config.author;
			modelLabel = config.modelLabel;
			type = config.type;
			population = config.population;
			system = config.system;
			dataUsed = config.dataUsed;
			body = config.body;
		}
		else
		{
			author = configInfo.getAuthor();
			modelLabel = configInfo.getModelLabel();
			type = configInfo.getType();
			population = configInfo.getPopulation();
			system = configInfo.getSystem();
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
			if (system != null)
			    path += " > " + system;
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
		body = configInfo == null ?  ((ViewConfig)getConfig()).body : configInfo.getBody();
        return body != null ? body + " / " + getDisplayName() : getDisplayName();
    }

	@Override
	protected void setupModelManager()
	{
		ConfigurableSceneNotifier tmpSceneChangeNotifier = new ConfigurableSceneNotifier();

		setupBodyModels();
		setupImagerModel();
		setupSpectraModels(tmpSceneChangeNotifier);
		setLineamentModel();
		setStateHistoryModels();
		StatusNotifier tmpStatusNotifier = getStatusNotifier();
		setupStructureModels(tmpSceneChangeNotifier, tmpStatusNotifier);
		setupDEMModels();

		setModelManager(new ModelManager(smallBodyModels.get(0), allModels));

		tmpSceneChangeNotifier.setTarget(getModelManager());

		getModelManager().addPropertyChangeListener(this);

		SBMTInfoWindowManagerFactory.initializeModels(getModelManager(), getLegacyStatusHandler());

		tmpSceneChangeNotifier.setTarget(getModelManager());


	}

	protected void setupBodyModels()
	{
		smallBodyModels = SbmtModelFactory.createSmallBodyModels(getPolyhedralModelConfig());
//		allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
		List<Model> allBodies = Lists.newArrayList();
		allBodies.addAll(smallBodyModels);
		allModels.put(ModelNames.SMALL_BODY, allBodies);
	}

	protected void setupStructureModels(ConfigurableSceneNotifier tmpSceneChangeNotifier, StatusNotifier tmpStatusNotifier)
	{
		SmallBodyModel smallBodyModel = smallBodyModels.get(0);
//		ConfigurableSceneNotifier tmpSceneChangeNotifier = new ConfigurableSceneNotifier();
//		StatusNotifier tmpStatusNotifier = getStatusNotifier();
		allModels.put(ModelNames.LINE_STRUCTURES, List.of(new LineModel<>(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel)));
		allModels.put(ModelNames.POLYGON_STRUCTURES, List.of(new PolygonModel(tmpSceneChangeNotifier,tmpStatusNotifier, smallBodyModel)));
		allModels.put(ModelNames.CIRCLE_STRUCTURES, List.of(StructureLegacyUtil.createManager(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel, Mode.CIRCLE_MODE)));
		allModels.put(ModelNames.ELLIPSE_STRUCTURES, List.of(StructureLegacyUtil.createManager(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel, Mode.ELLIPSE_MODE)));
		allModels.put(ModelNames.POINT_STRUCTURES, List.of(StructureLegacyUtil.createManager(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel, Mode.POINT_MODE)));
		allModels.put(ModelNames.CIRCLE_SELECTION, List.of(new CircleSelectionModel(tmpSceneChangeNotifier, tmpStatusNotifier, smallBodyModel)));
//		tmpSceneChangeNotifier.setTarget(getModelManager());
	}

	protected void setupSpectraModels(ConfigurableSceneNotifier tmpSceneChangeNotifier)
	{
		SmallBodyModel smallBodyModel = smallBodyModels.get(0);
		HashMap<ModelNames, List<Model>> models = new HashMap<ModelNames, List<Model>>();

        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;

        //TODO FIX THIS
//        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));

        SpectraCollection collection = new SpectraCollection(tmpSceneChangeNotifier, smallBodyModel);

        models.put(ModelNames.SPECTRA, List.of(collection));

		allModels.putAll(models);
		allModels.put(ModelNames.SPECTRA_BOUNDARIES, List.of(new SpectrumBoundaryCollection(smallBodyModel, (SpectraCollection)allModels.get(ModelNames.SPECTRA).get(0))));
        //if (getPolyhedralModelConfig().body == ShapeModelBody.EROS)
            allModels.put(ModelNames.STATISTICS, List.of(new SpectrumStatisticsCollection()));

		SpectraCollection customCollection = new SpectraCollection(tmpSceneChangeNotifier,smallBodyModel);
		allModels.put(ModelNames.CUSTOM_SPECTRA, List.of(customCollection));
		allModels.put(ModelNames.CUSTOM_SPECTRA_BOUNDARIES, List.of(new SpectrumBoundaryCollection(smallBodyModel, (SpectraCollection)allModels.get(ModelNames.CUSTOM_SPECTRA).get(0))));

		if (!getPolyhedralModelConfig().spectralInstruments.stream().filter(inst -> inst.getDisplayName().equals("MEGANE")).toList().isEmpty())
		{
			meganeCollection = new MEGANECollection(smallBodyModel);
			allModels.put(ModelNames.GRNS_SPECTRA, List.of(meganeCollection));
			cumulativeMeganeCollection = new CumulativeMEGANECollection(smallBodyModel);
			allModels.put(ModelNames.GRNS_CUSTOM_SPECTRA, List.of(cumulativeMeganeCollection));
		}
	}

	protected void setupImagerModel()
	{
//		smallBodyModels = SbmtModelFactory.createSmallBodyModels(getPolyhedralModelConfig());
		SmallBodyModel smallBodyModel = smallBodyModels.get(0);

//		//OLDER WAY - remove this once the old basemap config files are retired
		ImageCollection imageCollection = new ImageCollection(smallBodyModels.get(0));
//
		allModels.put(ModelNames.IMAGES, List.of(imageCollection));
//		allModels.put(ModelNames.CUSTOM_IMAGES, List.of(imageCollection));
//        ImageCubeCollection customCubeCollection = new ImageCubeCollection(smallBodyModels, imageCollection);
//        ColorImageCollection customColorImageCollection = new ColorImageCollection(smallBodyModels, getModelManager());
//        allModels.put(ModelNames.CUSTOM_CUBE_IMAGES, List.of(customCubeCollection));
//        allModels.put(ModelNames.CUSTOM_COLOR_IMAGES, List.of(customColorImageCollection));
//        ImageCubeCollection cubeCollection = new ImageCubeCollection(smallBodyModels, imageCollection);
//        ColorImageCollection colorImageCollection = new ColorImageCollection(smallBodyModels, getModelManager());
//        allModels.put(ModelNames.COLOR_IMAGES, List.of(colorImageCollection));
//        allModels.put(ModelNames.CUBE_IMAGES, List.of(cubeCollection));

        //NEW TECHNIQUE
		allModels.put(ModelNames.IMAGES_V2, List.of(new PerspectiveImageCollection(smallBodyModels)));

		allModels.put(ModelNames.BASEMAPS, List.of(new BasemapImageCollection<>(smallBodyModels)));
	}

	protected void setupDEMModels()
	{
		SmallBodyModel smallBodyModel = smallBodyModels.get(0);
		DEMCollection demCollection = new DEMCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.DEM, List.of(demCollection));
		DEMBoundaryCollection demBoundaryCollection = new DEMBoundaryCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.DEM_BOUNDARY, List.of(demBoundaryCollection));
		demCollection.setModelManager(getModelManager());
		demBoundaryCollection.setModelManager(getModelManager());
	}

	protected void setStateHistoryModels()
	{
		SmallBodyModel smallBodyModel = smallBodyModels.get(0);

		rendererManager = new StateHistoryRendererManager(smallBodyModel, new StateHistoryCollection(smallBodyModel), getRenderer());
        allModels.put(ModelNames.STATE_HISTORY_COLLECTION_ELEMENTS, List.of(rendererManager));
	}

	protected void setLineamentModel()
	{
		allModels.put(ModelNames.LINEAMENT, List.of(createLineament()));
	}

    @Override
    protected void setupPopupManager()
    {
    	ModelManager modelManager = getModelManager();
		SbmtInfoWindowManager imageInfoWindowManager = (SbmtInfoWindowManager) getInfoPanelManager();
		SbmtSpectrumWindowManager multiSpectralInfoWindowManager = (SbmtSpectrumWindowManager) getSpectrumPanelManager();
		Renderer renderer = getRenderer();
		setPopupManager(new ImageListPopupManager(modelManager, imageInfoWindowManager, multiSpectralInfoWindowManager, renderer));
//		for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//		{
//			ImageCollection images = (ImageCollection) getModelManager().getModel(ModelNames.IMAGES);
//
//			PopupMenu popupMenu = new ImagePopupMenu<>(modelManager, images, imageInfoWindowManager, multiSpectralInfoWindowManager, renderer, renderer);
//			registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);
//		}

        if (getPolyhedralModelConfig().hasSpectralData)
        {
            //This needs to be updated to handle the multiple Spectral Instruments that can exist on screen at the same time....
//            for (SpectralInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
            {
				SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
				SpectrumBoundaryCollection spectrumBoundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);
				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, spectrumBoundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				registerPopup(getModel(ModelNames.SPECTRA), popupMenu);
			}
		}

        if (getPolyhedralModelConfig().hasLineamentData)
        {
            PopupMenu popupMenu = new LineamentPopupMenu(getModelManager());
            registerPopup(getModel(ModelNames.LINEAMENT), popupMenu);
        }

        if (getPolyhedralModelConfig().hasMapmaker || getPolyhedralModelConfig().hasBigmap)
        {
            PopupMenu popupMenu = new MapletBoundaryPopupMenu(getModelManager(), getRenderer());
            registerPopup(getModel(ModelNames.DEM_BOUNDARY), popupMenu);
        }
    }

    protected void setupModelTab()
	{
		addTab(getPolyhedralModelConfig().getShapeModelName(), new SmallBodyControlPanel(getRenderer(), getModelManager(), getPolyhedralModelConfig().getShapeModelName()));

	}

	protected void setupSpectralImagingTabs()
	{

	}

	protected void setupNormalImagingTabs()
	{
//		setupOlderImageTabs();
		PerspectiveImageCollection collection = (PerspectiveImageCollection)getModelManager().getModel(ModelNames.IMAGES_V2);
//		for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//	    {
			if (getPolyhedralModelConfig().imagingInstruments.length == 0)
			{
				ImageSearchController cont  = cont = new ImageSearchController(getPolyhedralModelConfig(), collection, Optional.ofNullable(null), getModelManager(), getPopupManager(), getRenderer(), getPickManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getLegacyStatusHandler());
				addTab("Images", cont.getView());

			}
			else
			{
				for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
			    {
					ImageSearchController cont  = new ImageSearchController(getPolyhedralModelConfig(), collection, Optional.of(instrument), getModelManager(), getPopupManager(), getRenderer(), getPickManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getLegacyStatusHandler());
					addTab(instrument.instrumentName.toString(), cont.getView());

					pomListeners.add(new PositionOrientationManagerListener()
					{
						@Override
						public void managerUpdated(IPositionOrientationManager manager)
						{
							((ImageSearchController)cont).setPositionOrientationManager(manager);
						}
					});
			    }
			}

//	    }
	}

//	private void setupOlderImageTabs()
//	{
//		for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//        {
//			Controller<ImageSearchModel, ?> controller = null;
//			if (instrument.spectralMode == SpectralImageMode.MONO)
//            {
//                // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
////                if (getPolyhedralModelConfig().body == ShapeModelBody.EROS || getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA || getPolyhedralModelConfig().body == ShapeModelBody.CERES || getPolyhedralModelConfig().body == ShapeModelBody.VESTA)
//                if (getPolyhedralModelConfig().imageSearchFilterNames != null && getPolyhedralModelConfig().imageSearchFilterNames.length > 0 && !(getPolyhedralModelConfig().body == ShapeModelBody._67P))
//                {
//					controller =
//							new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//
//                }
//                else if (Configuration.isAPLVersion() || (getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA && ShapeModelType.GASKELL == getPolyhedralModelConfig().author))
//                {
//                    if (getPolyhedralModelConfig().body == ShapeModelBody._67P)
//                    {
//
////                        JComponent component = new OsirisImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//						controller =
//								new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//                    }
//                    else
//                    {
////                        JComponent component = new ImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//						controller =
//								new ImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//                    }
//                }
//            }
//
//			else if (instrument.spectralMode == SpectralImageMode.MULTI)
//            {
//                if (Configuration.isAPLVersion())
//                {
////                    JComponent component = new QuadraspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//					JComponent component =
//							new QuadSpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel();
//                    addTab(instrument.instrumentName.toString(), component);
//                }
//            }
//			else if (instrument.spectralMode == SpectralImageMode.HYPER)
//            {
//                if (Configuration.isAPLVersion())
//                {
//					JComponent component =
//							new HyperspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).init();
////                    JComponent component = new HyperspectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).getPanel();
//                    addTab(instrument.instrumentName.toString(), component);
//                }
//            }
//
//			if (controller != null)
//			{
//				metadataManagers.put(instrument.instrumentName.toString(), controller.getModel());
//				addTab(instrument.instrumentName.toString(), controller.getView().getComponent());
//			}
//		}
//	}

	protected void setupSpectrumTabs()
	{
		for (BasicSpectrumInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
        {
	        SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
	        SpectrumBoundaryCollection boundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);

            String displayName = instrument.getDisplayName();
//			if (displayName.equals(SpectraType.NIS_SPECTRA.getDisplayName()))
			if (displayName.equals("NIS"))
            {
				NEARSpectraFactory.initializeModels(smallBodyModels.get(0));
				NISSearchModel model = new NISSearchModel(getModelManager(), instrument);
//				SpectraCollection<NISSpectrum> nisSpectrumCollection = new SpectraCollection<>(smallBodyModel);
//				SpectrumBoundaryCollection<NISSpectrum> boundaryCollection = new SpectrumBoundaryCollection(smallBodyModel, nisSpectrumCollection);
//				double[] rgbMaxVals = new double[] {0.05, 0.05, 0.05};
//	            int[] rgbIndices = new int[] { 1, 25, 50 };
				JComponent component = new OREXSpectrumSearchController<NISSpectrum>(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
						getPolyhedralModelConfig().hasHierarchicalSpectraSearch, getPolyhedralModelConfig().hasHypertreeBasedSpectraSearch,
						getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, new String[] {"L2"},
						getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
						getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
                addTab(instrument.getDisplayName(), component);

				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				registerPopup(spectrumCollection, popupMenu);

            }
			else if (displayName.equals("OTES"))
            {
//				SpectraCollection<OTESSpectrum> otesSpectrumCollection = new SpectraCollection<>(smallBodyModel);
//				SpectrumBoundaryCollection<OTESSpectrum> boundaryCollection = new SpectrumBoundaryCollection(smallBodyModel, otesSpectrumCollection);

				OREXSpectraFactory.initializeModels(smallBodyModels.get(0));
				JComponent component = new OREXSpectrumTabbedPane<OTESSpectrum>(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, spectrumCollection);

//				OTESSearchModel model = new OTESSearchModel(getModelManager(), instrument);
//				JComponent component = new OREXSpectrumSearchController<OTESSpectrum>(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
//						/*getPolyhedralModelConfig().hasHierarchicalSpectraSearch*/ false, getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
//						getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
                addTab(instrument.getDisplayName(), component);


				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				registerPopup(spectrumCollection, popupMenu);
            }
			else if (displayName.equals("OVIRS"))
            {
//				SpectraCollection<OVIRSSpectrum> ovirsSpectrumCollection = new SpectraCollection<>(smallBodyModel);
//				SpectrumBoundaryCollection<OVIRSSpectrum> boundaryCollection = new SpectrumBoundaryCollection(smallBodyModel, ovirsSpectrumCollection);
				JComponent component = new OREXSpectrumTabbedPane<OVIRSSpectrum>(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, spectrumCollection);
                addTab(instrument.getDisplayName(), component);
				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				registerPopup(spectrumCollection, popupMenu);
            }
			else if (displayName.equals("NIRS3"))
            {
				H2SpectraFactory.initializeModels(smallBodyModels.get(0));
				NIRS3SearchModel model = new NIRS3SearchModel(getModelManager(), instrument);
//				SpectraCollection<NIRS3Spectrum> nirs3SpectrumCollection = new SpectraCollection<>(smallBodyModel);
//				SpectrumBoundaryCollection<NIRS3Spectrum> boundaryCollection = new SpectrumBoundaryCollection(smallBodyModel, nirs3SpectrumCollection);
//				double[] rgbMaxVals = new double[] {0.00005, 0.0001, 0.002};
//	            int[] rgbIndices = new int[] { 100, 70, 40 };
				JComponent component = new OREXSpectrumSearchController<NIRS3Spectrum>(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
						getPolyhedralModelConfig().hasHierarchicalSpectraSearch, getPolyhedralModelConfig().hasHypertreeBasedSpectraSearch,
						getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, new String[] {"L2"},
						getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
						getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
                addTab(instrument.getDisplayName(), component);
				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				registerPopup(spectrumCollection, popupMenu);
			}
			else if (displayName.equals("MEGANE"))
			{
//				MEGANECollection collection = new MEGANECollection();
				MEGANECollection collection = (MEGANECollection)getModelManager().getModel(ModelNames.GRNS_SPECTRA);
				CumulativeMEGANECollection cumulativeCollection = (CumulativeMEGANECollection)getModelManager().getModel(ModelNames.GRNS_CUSTOM_SPECTRA);
				MEGANEController meganeController = new MEGANEController(collection, cumulativeCollection, smallBodyModels.get(0), getModelManager(), getPickManager());
				rendererManager.addListener(new ItemEventListener()
				{
					@Override
					public void handleItemEvent(Object aSource, ItemEventType aEventType)
					{
						if (aEventType == ItemEventType.ItemsSelected)
						{
							StateHistory currentRun = rendererManager.getHistoryCollection().getCurrentRun();
							if (currentRun == null) return;
							meganeController.propertyChange(new PropertyChangeEvent(meganeController, "SPICEPROVIDER", null, currentRun.getLocationProvider().getPointingProvider()));
						}
					}
				});
				addTab(instrument.getDisplayName(), meganeController.getPanel());
            }

        }
	}

	protected void setupLidarTabs()
	{
		// Lidar tab
		SmallBodyViewConfig tmpSmallBodyConfig = getPolyhedralModelConfig();
		String lidarInstrName = "Tracks";
		if (tmpSmallBodyConfig.hasLidarData == true)
			lidarInstrName = tmpSmallBodyConfig.lidarInstrumentName.toString();

		try
		{
		    JComponent lidarPanel = new LidarPanel(getRenderer(), getStatusNotifier(), getPickManager(), tmpSmallBodyConfig, getModelManager().getPolyhedralModel(), getModelManager());
		    addTab(lidarInstrName, lidarPanel);
		} catch (Exception e)
		{
		    e.printStackTrace();
		}
	}

	protected void setupLineamentTab()
	{
        if (getPolyhedralModelConfig().hasLineamentData)
        {
            JComponent component = new LineamentControlPanel(getModelManager());
            addTab("Lineament", component);
        }
	}

	protected void setupStructuresTab()
	{
		addTab("Structures", new StructureMainPanel(getPickManager(), getRenderer(), getStatusNotifier(), getModelManager()));
	}

	protected void setupCustomDataTab()
	{
        JTabbedPane customDataPane=new JTabbedPane();
        customDataPane.setBorder(BorderFactory.createEmptyBorder());


//        if (!getPolyhedralModelConfig().customTemporary)
//        {
//            ImagingInstrument instrument = null;
//            for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
//            {
//                instrument = i;
//                break;
//            }
//
//			ImageCollection images = (ImageCollection) getModel(ModelNames.CUSTOM_IMAGES);
//			PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
//			registerPopup(getModel(ModelNames.CUSTOM_IMAGES), popupMenu);
//            customDataPane.addTab("Images", new CustomImageController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel());
//        }

		for (BasicSpectrumInstrument i : getPolyhedralModelConfig().spectralInstruments)
		{
//			if (i.getDisplayName().equals("NIS"))
//				continue; //we can't properly handle NIS custom data for now without info files, which we don't have.
			customDataPane.addTab(i.getDisplayName() + " Spectra", new CustomSpectraSearchController(getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification, i).getPanel());
//
			SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.CUSTOM_SPECTRA);
			SpectrumBoundaryCollection boundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.CUSTOM_SPECTRA_BOUNDARIES);
			PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
			registerPopup(spectrumCollection, popupMenu);
		}

		if (customDataPane.getTabCount() != 0)
			addTab("Custom Data", customDataPane);
	}

	protected void setupDEMTab()
	{
		DEMCollection dems = (DEMCollection) getModel(ModelNames.DEM);
		DEMBoundaryCollection demBoundaries = (DEMBoundaryCollection) getModel(ModelNames.DEM_BOUNDARY);
		DEMPopupMenu demPopupMenu = new DEMPopupMenu(getModelManager().getPolyhedralModel(), dems, demBoundaries,
				renderer, getRenderer(), new DEMPopupMenuActionListener(dems, demBoundaries));
		registerPopup(getModel(ModelNames.DEM), demPopupMenu);
		registerPopup(getModel(ModelNames.DEM_BOUNDARY), demPopupMenu);

		DEMCreator creationTool = null;
		if (getPolyhedralModelConfig().hasRemoteMapmaker) // config builds DEMs from the server
		{
			creationTool = new MapmakerRemoteDEMCreator(
					Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()),
					getPolyhedralModelConfig());
		}
		else if (getPolyhedralModelConfig().hasMapmaker) // config builds DEMs locally.
		{
			creationTool = new MapmakerDEMCreator(
					Paths.get(getPolyhedralModelConfig().rootDirOnServer + File.separator + "mapmaker.zip"),
					Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()));
		}

		addTab("Regional DTMs", new DemMainPanel(getRenderer(), getModelManager().getPolyhedralModel(),
				getStatusNotifier(), getPickManager(), getPolyhedralModelConfig()));

		//if ( getPolyhedralModelConfig().rootDirOnServer != null)
		//{
		//	DEMCreator creationTool=new MapmakerDEMCreator(Paths.get(getPolyhedralModelConfig().rootDirOnServer), Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()));
		//	addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), creationTool, getPolyhedralModelConfig(), getRenderer()).getPanel());
		//}
		//else
		//{
		//// 	getPolyhedralModelConfig().hasMapmaker = false;
		////	getPolyhedralModelConfig().hasBigmap = false;
		////	addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), null, getPolyhedralModelConfig(), getRenderer()).getPanel());
		//
		//	JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getPolyhedralModelConfig().rootDirOnServer,
		//      getPolyhedralModelConfig().hasMapmaker, getPolyhedralModelConfig().hasBigmap, renderer, getPolyhedralModelConfig());
		//	addTab("Regional DTMs", component);
		////}
	}

	protected void setupStateHistoryTab()
	{
		planningController = new ObservationPlanningController(getModelManager(), smallBodyModels.get(0), rendererManager, getPolyhedralModelConfig(), smallBodyModels.get(0).getColoringDataManager(), getStatusNotifier());
        addTab("Observing Conditions", planningController.getView());
        planningController.setPositionOrientationManager(positionOrientationManager);
        pomListeners.add(new PositionOrientationManagerListener()
		{
			@Override
			public void managerUpdated(IPositionOrientationManager manager)
			{
				planningController.setPositionOrientationManager(manager);
			}
		});
	}

    @Override
    protected void setupTabs()
    {
//		addTab(getPolyhedralModelConfig().getShapeModelName(), new SmallBodyControlPanel(getRenderer(), getModelManager(), getPolyhedralModelConfig().getShapeModelName()));
		setupModelTab();
        setupNormalImagingTabs();
        setupSpectrumTabs();
		setupLidarTabs();
		setupLineamentTab();
		setupStructuresTab();
		setupCustomDataTab();
		setupDEMTab();
    	setupStateHistoryTab();

//        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//        {
//			Controller<ImageSearchModel, ?> controller = null;
//			if (instrument.spectralMode == SpectralImageMode.MONO)
//            {
//                // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
////                if (getPolyhedralModelConfig().body == ShapeModelBody.EROS || getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA || getPolyhedralModelConfig().body == ShapeModelBody.CERES || getPolyhedralModelConfig().body == ShapeModelBody.VESTA)
//                if (getPolyhedralModelConfig().imageSearchFilterNames != null && getPolyhedralModelConfig().imageSearchFilterNames.length > 0 && !(getPolyhedralModelConfig().body == ShapeModelBody._67P))
//                {
//					controller =
//							new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//
//                }
//                else if (Configuration.isAPLVersion() || (getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA && ShapeModelType.GASKELL == getPolyhedralModelConfig().author))
//                {
//                    if (getPolyhedralModelConfig().body == ShapeModelBody._67P)
//                    {
//
////                        JComponent component = new OsirisImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//						controller =
//								new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//                    }
//                    else
//                    {
////                        JComponent component = new ImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//						controller =
//								new ImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//                    }
//                }
//            }
//
//			else if (instrument.spectralMode == SpectralImageMode.MULTI)
//            {
//                if (Configuration.isAPLVersion())
//                {
////                    JComponent component = new QuadraspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
//					JComponent component =
//							new QuadSpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel();
//                    addTab(instrument.instrumentName.toString(), component);
//                }
//            }
//			else if (instrument.spectralMode == SpectralImageMode.HYPER)
//            {
//                if (Configuration.isAPLVersion())
//                {
//					JComponent component =
//							new HyperspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).init();
////                    JComponent component = new HyperspectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).getPanel();
//                    addTab(instrument.instrumentName.toString(), component);
//                }
//            }
//
//			if (controller != null)
//			{
//				metadataManagers.put(instrument.instrumentName.toString(), controller.getModel());
//				addTab(instrument.instrumentName.toString(), controller.getView().getComponent());
//			}
//
//			PerspectiveImageCollection collection = (PerspectiveImageCollection)getModelManager().getModel(ModelNames.IMAGES_V2);
//			addTab(instrument.instrumentName.toString() + "2", new ImageSearchController(getPolyhedralModelConfig(), collection, instrument, getModelManager(), getPopupManager(), getRenderer(), getPickManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager()).getView());
//
//		}


    }

    @Override
    protected void setupPickManager()
    {
		PickManager tmpPickManager = new PickManager(getRenderer(), getModelManager());
		setPickManager(tmpPickManager);

		// Manually register the Renderer with the DefaultPicker
		tmpPickManager.getDefaultPicker().addListener(getRenderer());

		// Manually register the PopupManager with the DefaultPicker
		tmpPickManager.getDefaultPicker().addListener(getPopupManager());

		// TODO: This should be moved out of here to a logical relevant location
//		tmpPickManager.getDefaultPicker().addListener(new ImageDefaultPickHandler2(getModelManager()));
    }

    @Override
    protected void setupInfoPanelManager()
    {
		setInfoPanelManager(new SbmtInfoWindowManager(getModelManager()));
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
	protected void setupPositionOrientationManager()
	{
		if (getPolyhedralModelConfig().spiceInfo == null || (getModelManager().getModels(ModelNames.SMALL_BODY).size() == 1)) return;

		KernelSelectionFrame kernelSelectionFrame = new KernelSelectionFrame(getModelManager(), new Function<String, Void>()
		{

			@Override
			public Void apply(String arg0)
			{
				SpiceInfo spiceInfo = getPolyhedralModelConfig().spiceInfo;
				List<SmallBodyModel> bodies = getModelManager().getModels(ModelNames.SMALL_BODY).stream().map(body -> { return (SmallBodyModel)body; }).toList();
				SpiceInfo firstSpiceInfo = spiceInfo;
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		    	String dateTimeString = dateFormatter.format(getPolyhedralModelConfig().stateHistoryStartDate);
		    	double time = TimeUtil.str2et(dateTimeString);
				positionOrientationManager = new PositionOrientationManager(bodies, arg0, firstSpiceInfo, firstSpiceInfo.getInstrumentNamesToBind()[0],
																			spiceInfo.getBodyName(), time);
				planningController.setPositionOrientationManager(positionOrientationManager);
				HashMap<ModelNames, List<Model>> allModels = new HashMap(getModelManager().getAllModels());
				allModels.put(ModelNames.SMALL_BODY, positionOrientationManager.getUpdatedBodies());
				setModelManager(new ModelManager(bodies.get(0), allModels));
				pomListeners.forEach(listener -> listener.managerUpdated(positionOrientationManager));
				return null;
			}
		});
		kernelSelectionFrame.setLocationRelativeTo(this);


	}

    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
			renderer.notifySceneChange();
        else
            renderer.getRenderWindowPanel().Render();
    }

    @Override
    public void setRenderer(Renderer renderer)
    {
        this.renderer = renderer;
        if (rendererManager == null) return;
        rendererManager.setRenderer(renderer);
        if (meganeCollection == null) return;
        meganeCollection.setRenderer(renderer);
        cumulativeMeganeCollection.setRenderer(renderer);
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

                    Renderer localRenderer = SbmtView.this.getRenderer();
					if (localRenderer != null)
					{
						RenderPanel panel = localRenderer.getRenderWindowPanel();
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
                        Renderer localRenderer = SbmtView.this.getRenderer();
                        if (localRenderer != null)
                        {
                        RenderPanel panel = localRenderer.getRenderWindowPanel();
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

	static public LineamentModel createLineament()
    {
        return new LineamentModel();
    }

    static public HashMap<ModelNames, Model> createSpectralModels(ConfigurableSceneNotifier tmpSceneChangeNotifier, SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        ShapeModelBody body=((SmallBodyViewConfig)smallBodyModel.getConfig()).body;
        ShapeModelType author=((SmallBodyViewConfig)smallBodyModel.getConfig()).author;
        String version=((SmallBodyViewConfig)smallBodyModel.getConfig()).version;

        models.put(ModelNames.SPECTRA_HYPERTREE_SEARCH, new SpectraSearchDataCollection(smallBodyModel));

        SpectraCollection collection = new SpectraCollection(tmpSceneChangeNotifier, smallBodyModel);

        models.put(ModelNames.SPECTRA, collection);
        return models;
    }

	public BasicConfigInfo getConfigInfo()
	{
		return configInfo;
	}

}

