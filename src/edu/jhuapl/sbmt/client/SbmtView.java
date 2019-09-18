package edu.jhuapl.sbmt.client;

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

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.collect.ImmutableList;

import vtk.vtkCamera;

import edu.jhuapl.saavtk.colormap.Colorbar;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.panel.StructuresControlPanel;
import edu.jhuapl.saavtk.gui.render.RenderPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.dtm.controller.DEMPopupMenuActionListener;
import edu.jhuapl.sbmt.dtm.controller.ExperimentalDEMController;
import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
import edu.jhuapl.sbmt.dtm.model.DEMCollection;
import edu.jhuapl.sbmt.dtm.model.creation.DEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerDEMCreator;
import edu.jhuapl.sbmt.dtm.service.demCreators.MapmakerRemoteDEMCreator;
import edu.jhuapl.sbmt.dtm.ui.menu.DEMPopupMenu;
import edu.jhuapl.sbmt.dtm.ui.menu.MapletBoundaryPopupMenu;
import edu.jhuapl.sbmt.gui.eros.LineamentControlPanel;
import edu.jhuapl.sbmt.gui.eros.LineamentPopupMenu;
import edu.jhuapl.sbmt.gui.image.HyperspectralImagingSearchPanel;
import edu.jhuapl.sbmt.gui.image.controllers.custom.CustomImageController;
import edu.jhuapl.sbmt.gui.image.controllers.images.ImagingSearchController;
import edu.jhuapl.sbmt.gui.image.controllers.quadspectral.QuadSpectralImagingSearchController;
import edu.jhuapl.sbmt.gui.image.controllers.spectral.SpectralImagingSearchController;
import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.gui.image.ui.color.ColorImagePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.ui.images.ImagePickManager;
import edu.jhuapl.sbmt.gui.image.ui.images.ImagePopupManager;
import edu.jhuapl.sbmt.gui.image.ui.images.ImagePopupMenu;
import edu.jhuapl.sbmt.gui.lidar.LidarLoadPanel;
import edu.jhuapl.sbmt.gui.lidar.LidarPanel;
import edu.jhuapl.sbmt.gui.lidar.LidarTrackPanel;
import edu.jhuapl.sbmt.gui.lidar.popup.LidarGuiUtil;
import edu.jhuapl.sbmt.gui.time.version2.StateHistoryController;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectraFactory;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectrumSearchController;
import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectrumTabbedPane;
import edu.jhuapl.sbmt.model.custom.CustomGraticule;
import edu.jhuapl.sbmt.model.eros.LineamentModel;
import edu.jhuapl.sbmt.model.eros.nis.NEARSpectraFactory;
import edu.jhuapl.sbmt.model.eros.nis.NISSearchModel;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.image.SpectralImageMode;
import edu.jhuapl.sbmt.model.lidar.LidarFileSpecManager;
import edu.jhuapl.sbmt.model.lidar.LidarTrackManager;
import edu.jhuapl.sbmt.model.ryugu.nirs3.H2SpectraFactory;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SearchModel;
import edu.jhuapl.sbmt.model.time.StateHistoryCollection;
import edu.jhuapl.sbmt.spectrum.controllers.custom.CustomSpectraSearchController;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
import edu.jhuapl.sbmt.spectrum.model.hypertree.SpectraSearchDataCollection;
import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatisticsCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.EmptyMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.TrackedMetadataManager;
import crucible.crust.metadata.impl.Utilities;
import net.miginfocom.swing.MigLayout;

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
	private Colorbar smallBodyColorbar;
	private BasicConfigInfo configInfo;
	private SmallBodyModel smallBodyModel;

	public SbmtView(StatusBar statusBar, BasicConfigInfo configInfo)
	{
		super(statusBar, null);
		this.configInfo = configInfo;
		uniqueName = configInfo.uniqueName;
		shapeModelName = configInfo.shapeModelName;
    	this.stateManager = TrackedMetadataManager.of("View " + configInfo.uniqueName);
		this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.configURL;
		initializeStateManager();
	}

	/**
	 * By default a view should be created empty. Only when the user requests to
	 * show a particular View, should the View's contents be created in order to
	 * reduce memory and startup time. Therefore, this function should be called
	 * prior to first time the View is shown in order to cause it
	 */
	public SbmtView(StatusBar statusBar, SmallBodyViewConfig smallBodyConfig)
	{
		super(statusBar, smallBodyConfig);
		this.configInfo = new BasicConfigInfo(smallBodyConfig);
		uniqueName = configInfo.uniqueName;
		shapeModelName = configInfo.shapeModelName;
		this.stateManager = TrackedMetadataManager.of("View " + getUniqueName());
		this.metadataManagers = new HashMap<>();
		this.configURL = configInfo.configURL;

		initializeStateManager();
	}

	@Override
	protected void initialize() throws InvocationTargetException, InterruptedException
	{
		if (configInfo != null)
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
		return (SmallBodyViewConfig) super.getConfig();
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
			author = configInfo.author;
			modelLabel = configInfo.modelLabel;
			type = configInfo.type;
			population = configInfo.population;
			dataUsed = configInfo.dataUsed;
			body = configInfo.body;
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
			if (configInfo.modelLabel != null)
				result = configInfo.modelLabel;
			else if (configInfo.author == null)
				result = configInfo.body.toString();
			else
				result = configInfo.author.toString();

			if (configInfo.version != null)
				result = result + " (" + configInfo.version + ")";
		}


		return result;
	}

	@Override
	public String getModelDisplayName()
	{
		ShapeModelBody body = null;
		body = configInfo == null ?  getConfig().body : configInfo.body;
		return body != null ? body + " / " + getDisplayName() : getDisplayName();
	}

	@Override
	protected void setupModelManager()
	{
		smallBodyModel = SbmtModelFactory.createSmallBodyModel(getPolyhedralModelConfig());
		SBMTModelBootstrap.initialize(smallBodyModel);
//		BasicSpectrumInstrument.initializeSerializationProxy();
		Graticule graticule = createGraticule(smallBodyModel);

		HashMap<ModelNames, Model> allModels = new HashMap<>();
		allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
		allModels.put(ModelNames.GRATICULE, graticule);
		allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));
		allModels.put(ModelNames.CUSTOM_IMAGES, new ImageCollection(smallBodyModel));
		ImageCubeCollection customCubeCollection = new ImageCubeCollection(smallBodyModel, getModelManager());
		ColorImageCollection customColorImageCollection = new ColorImageCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.CUSTOM_CUBE_IMAGES, customCubeCollection);
		allModels.put(ModelNames.CUSTOM_COLOR_IMAGES, customColorImageCollection);

		//all bodies can potentially have at least custom images, color images, and cubes, so these models must exist for everything.  Same will happen for spectra when it gets enabled.
		allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
		allModels.put(ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
		ImageCubeCollection cubeCollection = new ImageCubeCollection(smallBodyModel, getModelManager());
		ColorImageCollection colorImageCollection = new ColorImageCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.COLOR_IMAGES, colorImageCollection);
		allModels.put(ModelNames.CUBE_IMAGES, cubeCollection);

		//        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
		//        {
		//            if (instrument.spectralMode == SpectralMode.MONO)
		//            {
		//                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
		//                allModels.put(ModelNames.CUBE_IMAGES, new ImageCubeCollection(smallBodyModel, getModelManager()));
		////                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
		//            }
		//
		//            else if (instrument.spectralMode == SpectralMode.MULTI)
		//            {
		//                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
		////                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
		//            }
		//            else if (instrument.spectralMode == SpectralMode.HYPER)
		//            {
		//                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
		////                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
		//            }
		//        }

		if (getPolyhedralModelConfig().hasSpectralData)
		{
			allModels.putAll(createSpectralModels(smallBodyModel));
			allModels.put(ModelNames.SPECTRA_BOUNDARIES, new SpectrumBoundaryCollection(smallBodyModel, (SpectraCollection)allModels.get(ModelNames.SPECTRA)));
			//if (getPolyhedralModelConfig().body == ShapeModelBody.EROS)
			allModels.put(ModelNames.STATISTICS, new SpectrumStatisticsCollection());
		}

		if (getPolyhedralModelConfig().hasLidarData)
		{
			allModels.putAll(createLidarModels(smallBodyModel));
		}

		if (getPolyhedralModelConfig().hasLineamentData)
		{
			allModels.put(ModelNames.LINEAMENT, createLineament());
		}

		if (getPolyhedralModelConfig().hasFlybyData)
		{
			//            allModels.put(ModelNames.FLYBY, ModelFactory.createFlyby(smallBodyModel));
			//            allModels.put(ModelNames.SIMULATION_RUN_COLLECTION, new SimulationRunCollection(smallBodyModel));
		}

		if (getPolyhedralModelConfig().hasStateHistory)
		{
			allModels.put(ModelNames.STATE_HISTORY_COLLECTION, new StateHistoryCollection(smallBodyModel));
		}

		allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
		allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
		allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
		allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
		allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
		allModels.put(ModelNames.TRACKS, new LidarTrackManager(smallBodyModel));
		DEMCollection demCollection = new DEMCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.DEM, demCollection);
		DEMBoundaryCollection demBoundaryCollection = new DEMBoundaryCollection(smallBodyModel, getModelManager());
		allModels.put(ModelNames.DEM_BOUNDARY, demBoundaryCollection);

		setModelManager(new SbmtModelManager(smallBodyModel, allModels));
		colorImageCollection.setModelManager(getModelManager());
		cubeCollection.setModelManager(getModelManager());
		customColorImageCollection.setModelManager(getModelManager());
		customCubeCollection.setModelManager(getModelManager());
		demCollection.setModelManager(getModelManager());
		demBoundaryCollection.setModelManager(getModelManager());

		getModelManager().addPropertyChangeListener(this);

		SBMTInfoWindowManagerFactory.initializeModels(getModelManager(), getStatusBar());
	}

	@Override
	protected void setupPopupManager()
	{
		setPopupManager(new ImagePopupManager(getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer()));

		for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
		{
			if (instrument.spectralMode == SpectralImageMode.MONO)
			{
				ImageCollection images = (ImageCollection) getModelManager().getModel(ModelNames.IMAGES);
				PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) getModelManager().getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
				ColorImageCollection colorImages = (ColorImageCollection) getModelManager().getModel(ModelNames.COLOR_IMAGES);
				ImageCubeCollection imageCubes = (ImageCubeCollection) getModelManager().getModel(ModelNames.CUBE_IMAGES);

				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

				popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager) getInfoPanelManager(), getModelManager(), getRenderer());
				registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

				popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
			}

			else if (instrument.spectralMode == SpectralImageMode.MULTI)
			{
				ImageCollection images = (ImageCollection) getModel(ModelNames.IMAGES);
				PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
				ColorImageCollection colorImages = (ColorImageCollection) getModel(ModelNames.COLOR_IMAGES);

				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

				popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager) getInfoPanelManager(), getModelManager(), getRenderer());
				registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);
			}
			else if (instrument.spectralMode == SpectralImageMode.HYPER)
			{
				ImageCollection images = (ImageCollection) getModel(ModelNames.IMAGES);
				PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
				ColorImageCollection colorImages = (ColorImageCollection) getModel(ModelNames.COLOR_IMAGES);
				ImageCubeCollection imageCubes = (ImageCubeCollection) getModel(ModelNames.CUBE_IMAGES);

				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

				popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager) getInfoPanelManager(), getModelManager(), getRenderer());
				registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

				popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
			}
		}

		if (getPolyhedralModelConfig().hasSpectralData)
		{
			//This needs to be updated to handle the multiple Spectral Instruments that can exist on screen at the same time....
			//            for (SpectralInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
			{
				SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
				SpectrumBoundaryCollection spectrumBoundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);
				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, spectrumBoundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
				((SpectrumPopupMenu) popupMenu).setInstrument(getPolyhedralModelConfig().spectralInstruments.get(0));
				registerPopup(getModel(ModelNames.SPECTRA), popupMenu);
			}
		}

		if (getPolyhedralModelConfig().hasLidarData)
		{
			LidarTrackManager tmpTrackManager = (LidarTrackManager)getModel(ModelNames.LIDAR_SEARCH);
			PopupMenu popupMenu = LidarGuiUtil.formLidarTrackPopupMenu(tmpTrackManager, getRenderer());
			registerPopup(tmpTrackManager, popupMenu);
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

	@Override
	protected void setupTabs()
	{
		addTab(getPolyhedralModelConfig().getShapeModelName(), new SmallBodyControlPanel(getModelManager(), getPolyhedralModelConfig().getShapeModelName()));

		if (getConfig().hasFlybyData)
		{
			//            addTab("Runs", new SimulationRunsPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer()));
		}

		for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
		{
			Controller<ImageSearchModel, ?> controller = null;
			if (instrument.spectralMode == SpectralImageMode.MONO)
			{
				// For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
				//                if (getPolyhedralModelConfig().body == ShapeModelBody.EROS || getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA || getPolyhedralModelConfig().body == ShapeModelBody.CERES || getPolyhedralModelConfig().body == ShapeModelBody.VESTA)
				if (getPolyhedralModelConfig().imageSearchFilterNames != null && getPolyhedralModelConfig().imageSearchFilterNames.length > 0 && !(getPolyhedralModelConfig().body == ShapeModelBody._67P))
				{
					controller =
							new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);

				}
				else if (Configuration.isAPLVersion() || (getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA && ShapeModelType.GASKELL == getPolyhedralModelConfig().author))
				{
					if (getPolyhedralModelConfig().body == ShapeModelBody._67P)
					{

						//                        JComponent component = new OsirisImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
						controller =
								new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
					}
					else
					{
						//                        JComponent component = new ImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
						controller =
								new ImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
					}
				}

			}

			else if (instrument.spectralMode == SpectralImageMode.MULTI)
			{
				if (Configuration.isAPLVersion())
				{
					//                    JComponent component = new QuadraspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
					JComponent component =
							new QuadSpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel();
					addTab(instrument.instrumentName.toString(), component);
				}
			}
			else if (instrument.spectralMode == SpectralImageMode.HYPER)
			{
				if (Configuration.isAPLVersion())
				{
					JComponent component =
							new HyperspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).init();
					//                    JComponent component = new HyperspectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).getPanel();
					addTab(instrument.instrumentName.toString(), component);
				}
			}

			if (controller != null)
			{
				metadataManagers.put(instrument.instrumentName.toString(), controller.getModel());
				addTab(instrument.instrumentName.toString(), controller.getView().getComponent());
			}

		}

		for (BasicSpectrumInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
		{
	        SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);

			String displayName = instrument.getDisplayName();
//			if (displayName.equals(SpectraType.NIS_SPECTRA.getDisplayName()))
			if (displayName.equals("NIS"))
			{
				NEARSpectraFactory.initializeModels(smallBodyModel);
				NISSearchModel model = new NISSearchModel(getModelManager(), instrument);
				JComponent component = new OREXSpectrumSearchController(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
						getPolyhedralModelConfig().hasHierarchicalSpectraSearch, getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
						getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
				addTab(instrument.getDisplayName(), component);
			}
			else if (displayName.equals("OTES"))
			{
				OREXSpectraFactory.initializeModels(smallBodyModel);
				JComponent component = new OREXSpectrumTabbedPane(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, spectrumCollection);
				addTab(instrument.getDisplayName(), component);
			}
			else if (displayName.equals("OVIRS"))
			{
				JComponent component = new OREXSpectrumTabbedPane(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, spectrumCollection);
				addTab(instrument.getDisplayName(), component);
			}
			else if (displayName.equals("NIRS3"))
			{
				H2SpectraFactory.initializeModels(smallBodyModel);
				NIRS3SearchModel model = new NIRS3SearchModel(getModelManager(), instrument);
				JComponent component = new OREXSpectrumSearchController(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
						getPolyhedralModelConfig().hasHierarchicalSpectraSearch, getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
						getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
				addTab(instrument.getDisplayName(), component);
			}
			else if (displayName.equals("MEGANE"))
			{
				addTab(instrument.getDisplayName(), new JPanel());
			}

		}

		if (getPolyhedralModelConfig().hasLidarData)
		{
			JComponent component = new LidarPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer());
			addTab(getPolyhedralModelConfig().lidarInstrumentName.toString(), component);
		}

		if (Configuration.isAPLVersion())
		{
			if (getPolyhedralModelConfig().hasLineamentData)
			{
				JComponent component = new LineamentControlPanel(getModelManager());
				addTab("Lineament", component);
			}

			addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager(), getStatusBar()));

			JTabbedPane customDataPane = new JTabbedPane();
			customDataPane.setBorder(BorderFactory.createEmptyBorder());
			addTab("Custom Data", customDataPane);

			//            if (!getPolyhedralModelConfig().customTemporary)
			{
				ImagingInstrument instrument = null;
				for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
				{
					instrument = i;
					break;
				}

				ImageCollection images = (ImageCollection) getModel(ModelNames.CUSTOM_IMAGES);
				PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) getModel(ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES);
				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
				registerPopup(getModel(ModelNames.CUSTOM_IMAGES), popupMenu);
				customDataPane.addTab("Images", new CustomImageController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel());
			}

			for (BasicSpectrumInstrument i : getPolyhedralModelConfig().spectralInstruments)
			{
				if (i.getDisplayName().equals("NIS"))
					continue; //we can't properly handle NIS custom data for now without info files, which we don't have.
				customDataPane.addTab(i.getDisplayName() + " Spectra", new CustomSpectraSearchController(getPolyhedralModelConfig().hasHierarchicalSpectraSearch,
			    		getPolyhedralModelConfig().hasHypertreeBasedSpectraSearch,
			    		getPolyhedralModelConfig().getHierarchicalSpectraSearchSpecification(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), i).getPanel());
				break;
			}

			// Add the "lidar tracks" tab
			ModelManager tmpModelManager = getModelManager();
			LidarTrackManager tmpTrackManager = (LidarTrackManager)tmpModelManager.getModel(ModelNames.TRACKS);
			LidarLoadPanel tmpLidarLoadPanel = new LidarLoadPanel(tmpTrackManager);
			LidarTrackPanel tmpLidarListPanel = new LidarTrackPanel(tmpTrackManager, tmpModelManager, getPolyhedralModelConfig(), getPickManager(), getRenderer());
			JPanel tmpPanel = new JPanel(new MigLayout("", "", "0[]0"));
			tmpPanel.add(tmpLidarLoadPanel, "growx,wrap");
			tmpPanel.add(tmpLidarListPanel, "growx,growy,pushx,pushy");
			customDataPane.addTab("Tracks", tmpPanel);

			//            JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getPolyhedralModelConfig().rootDirOnServer,
			//                    getPolyhedralModelConfig().hasMapmaker, getPolyhedralModelConfig().hasBigmap, renderer);
			//            addTab("Regional DTMs", component);

            DEMCollection dems = (DEMCollection)getModel(ModelNames.DEM);
            DEMBoundaryCollection demBoundaries = (DEMBoundaryCollection)getModel(ModelNames.DEM_BOUNDARY);
        	DEMPopupMenu demPopupMenu = new DEMPopupMenu(getModelManager().getPolyhedralModel(), dems, demBoundaries, renderer, getRenderer(), new DEMPopupMenuActionListener(dems, demBoundaries));
            registerPopup(getModel(ModelNames.DEM), demPopupMenu);
            registerPopup(getModel(ModelNames.DEM_BOUNDARY), demPopupMenu);

            DEMCreator creationTool = null;
            if (getPolyhedralModelConfig().hasRemoteMapmaker)	//config builds DEMs from the server
            {
            	creationTool = new MapmakerRemoteDEMCreator(Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()), getPolyhedralModelConfig());
//            	JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getPolyhedralModelConfig().rootDirOnServer,
//                        getPolyhedralModelConfig().hasMapmaker, getPolyhedralModelConfig().hasBigmap, renderer, getPolyhedralModelConfig());
//                addTab("Regional DTMs", component);
            }
            else if (getPolyhedralModelConfig().hasMapmaker)	//config builds DEMs locally.
            {
            	creationTool=new MapmakerDEMCreator(Paths.get(getPolyhedralModelConfig().rootDirOnServer + File.separator + "mapmaker.zip"), Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()));
            }

        	addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), creationTool, getPolyhedralModelConfig(), getRenderer()).getPanel());


//            if ( getPolyhedralModelConfig().rootDirOnServer != null)
//            {
//            	DEMCreator creationTool=new MapmakerDEMCreator(Paths.get(getPolyhedralModelConfig().rootDirOnServer), Paths.get(getModelManager().getPolyhedralModel().getCustomDataFolder()));
//            	addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), creationTool, getPolyhedralModelConfig(), getRenderer()).getPanel());
//            }
//            else
//            {
////               	getPolyhedralModelConfig().hasMapmaker = false;
////            	getPolyhedralModelConfig().hasBigmap = false;
////            	addTab("Regional DTMs", new ExperimentalDEMController(getModelManager(), getPickManager(), null, getPolyhedralModelConfig(), getRenderer()).getPanel());
//
//            	JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getPolyhedralModelConfig().rootDirOnServer,
//                    getPolyhedralModelConfig().hasMapmaker, getPolyhedralModelConfig().hasBigmap, renderer, getPolyhedralModelConfig());
//            	addTab("Regional DTMs", component);
////			}
            if (getConfig().hasStateHistory)
            {
                StateHistoryController controller = null;
                if (getConfig().body == ShapeModelBody.EARTH)
                    controller = new StateHistoryController(getModelManager(), getRenderer(), false);
                else
                    controller = new StateHistoryController(getModelManager(), getRenderer(), true);
                addTab("Observing Conditions", controller.getView());

			}
		}
	}

	@Override
	protected void setupPickManager()
	{
		setPickManager(new ImagePickManager(getRenderer(), getStatusBar(), getModelManager(), getPopupManager()));
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
	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
		{
			renderer.setProps(getModelManager().getProps());

			if (smallBodyColorbar == null)
				return;

			PolyhedralModel sbModel = (PolyhedralModel) getModelManager().getModel(ModelNames.SMALL_BODY);
			if (sbModel.isColoringDataAvailable() && sbModel.getColoringIndex() >= 0)
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
				if (renderer.getRenderWindowPanel().getRenderer().HasViewProp(smallBodyColorbar.getActor()) == 0)
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

	@Override
	public void setRenderer(Renderer renderer)
	{
		this.renderer = renderer;
		smallBodyColorbar = new Colorbar(renderer);
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
                    Renderer localRenderer = SbmtView.this.getRenderer();
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

    static public HashMap<ModelNames, Model> createLidarModels(SmallBodyModel smallBodyModel)
    {
        HashMap<ModelNames, Model> models = new HashMap<ModelNames, Model>();

        models.put(ModelNames.LIDAR_BROWSE, new LidarFileSpecManager(smallBodyModel));
        models.put(ModelNames.LIDAR_SEARCH, new LidarTrackManager(smallBodyModel));
        if (smallBodyModel.getSmallBodyConfig().hasHypertreeLidarSearch())
        {
            switch (smallBodyModel.getSmallBodyConfig().getLidarInstrument())
            {
            case MOLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
            case OLA:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
            case LASER:
                models.put(ModelNames.LIDAR_HYPERTREE_SEARCH, new LidarTrackManager(smallBodyModel));
                break;
                default:
                	throw new AssertionError();
            }


        }

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

}
