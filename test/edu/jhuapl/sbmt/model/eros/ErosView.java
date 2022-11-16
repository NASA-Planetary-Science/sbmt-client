//package edu.jhuapl.sbmt.model.eros;
//
//import java.util.HashMap;
//
//import javax.swing.BorderFactory;
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//
//import edu.jhuapl.saavtk.gui.StatusBar;
//import edu.jhuapl.saavtk.model.Controller;
//import edu.jhuapl.saavtk.model.Graticule;
//import edu.jhuapl.saavtk.model.Model;
//import edu.jhuapl.saavtk.model.ModelManager;
//import edu.jhuapl.saavtk.model.ModelNames;
//import edu.jhuapl.saavtk.model.structure.CircleModel;
//import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
//import edu.jhuapl.saavtk.model.structure.EllipseModel;
//import edu.jhuapl.saavtk.model.structure.LineModel;
//import edu.jhuapl.saavtk.model.structure.PointModel;
//import edu.jhuapl.saavtk.model.structure.PolygonModel;
//import edu.jhuapl.saavtk.popup.PopupMenu;
//import edu.jhuapl.sbmt.client.BasicConfigInfo;
//import edu.jhuapl.sbmt.client.SBMTInfoWindowManagerFactory;
//import edu.jhuapl.sbmt.client.SBMTModelBootstrap;
//import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
//import edu.jhuapl.sbmt.client.SbmtModelFactory;
//import edu.jhuapl.sbmt.client.SbmtModelManager;
//import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
//import edu.jhuapl.sbmt.client.SmallBodyControlPanel;
//import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
//import edu.jhuapl.sbmt.dtm.model.DEMBoundaryCollection;
//import edu.jhuapl.sbmt.dtm.model.DEMCollection;
//import edu.jhuapl.sbmt.dtm.ui.menu.MapletBoundaryPopupMenu;
//import edu.jhuapl.sbmt.gui.eros.LineamentPopupMenu;
//import edu.jhuapl.sbmt.gui.image.controllers.custom.CustomImageController;
//import edu.jhuapl.sbmt.gui.image.controllers.spectral.SpectralImagingSearchController;
//import edu.jhuapl.sbmt.gui.image.model.images.ImageSearchModel;
//import edu.jhuapl.sbmt.gui.image.ui.color.ColorImagePopupMenu;
//import edu.jhuapl.sbmt.gui.image.ui.cubes.ImageCubePopupMenu;
//import edu.jhuapl.sbmt.gui.image.ui.images.ImagePopupManager;
//import edu.jhuapl.sbmt.gui.image.ui.images.ImagePopupMenu;
//import edu.jhuapl.sbmt.lidar.LidarTrackManager;
//import edu.jhuapl.sbmt.lidar.gui.LidarLoadPanel;
//import edu.jhuapl.sbmt.lidar.gui.LidarTrackPanel;
//import edu.jhuapl.sbmt.lidar.gui.action.LidarGuiUtil;
//import edu.jhuapl.sbmt.model.AbstractBodyView;
//import edu.jhuapl.sbmt.model.bennu.spectra.OREXSpectrumSearchController;
//import edu.jhuapl.sbmt.model.eros.nis.NEARSpectraFactory;
//import edu.jhuapl.sbmt.model.eros.nis.NISSearchModel;
//import edu.jhuapl.sbmt.model.eros.nis.NISSpectrum;
//import edu.jhuapl.sbmt.model.image.ColorImageCollection;
//import edu.jhuapl.sbmt.model.image.ImageCollection;
//import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
//import edu.jhuapl.sbmt.model.image.ImagingInstrument;
//import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
//import edu.jhuapl.sbmt.model.image.SpectralImageMode;
//import edu.jhuapl.sbmt.spectrum.controllers.custom.CustomSpectraSearchController;
//import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrumInstrument;
//import edu.jhuapl.sbmt.spectrum.model.statistics.SpectrumStatisticsCollection;
//import edu.jhuapl.sbmt.spectrum.rendering.SpectraCollection;
//import edu.jhuapl.sbmt.spectrum.rendering.SpectrumBoundaryCollection;
//import edu.jhuapl.sbmt.spectrum.ui.SpectrumPopupMenu;
//import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
//
//import net.miginfocom.swing.MigLayout;
//
//public class ErosView extends AbstractBodyView
//{
//	public ErosView(StatusBar statusBar, BasicConfigInfo config)
//	{
//		super(statusBar, config);
//		// TODO Auto-generated constructor stub
//	}
//
//	public ErosView(StatusBar statusBar, SmallBodyViewConfig smallBodyConfig)
//	{
//		super(statusBar, smallBodyConfig);
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	protected void setupModelManager()
//	{
//		smallBodyModel = SbmtModelFactory.createSmallBodyModel(getPolyhedralModelConfig());
//		SBMTModelBootstrap.initialize(smallBodyModel);
////		BasicSpectrumInstrument.initializeSerializationProxy();
//		Graticule graticule = createGraticule(smallBodyModel);
//
//        HashMap<ModelNames, Model> allModels = new HashMap<>();
//        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
//        allModels.put(ModelNames.GRATICULE, graticule);
//        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));
//        allModels.put(ModelNames.CUSTOM_IMAGES, new ImageCollection(smallBodyModel));
//        ImageCubeCollection customCubeCollection = new ImageCubeCollection(smallBodyModel, getModelManager());
//        ColorImageCollection customColorImageCollection = new ColorImageCollection(smallBodyModel, getModelManager());
//        allModels.put(ModelNames.CUSTOM_CUBE_IMAGES, customCubeCollection);
//        allModels.put(ModelNames.CUSTOM_COLOR_IMAGES, customColorImageCollection);
//
//        //all bodies can potentially have at least custom images, color images, and cubes, so these models must exist for everything.  Same will happen for spectra when it gets enabled.
//        allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
//        allModels.put(ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
//        allModels.put(ModelNames.PERSPECTIVE_COLOR_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
//        allModels.put(ModelNames.PERSPECTIVE_IMAGE_CUBE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
//        ImageCubeCollection cubeCollection = new ImageCubeCollection(smallBodyModel, getModelManager());
//        ColorImageCollection colorImageCollection = new ColorImageCollection(smallBodyModel, getModelManager());
//        allModels.put(ModelNames.COLOR_IMAGES, colorImageCollection);
//        allModels.put(ModelNames.CUBE_IMAGES, cubeCollection);
//
//        if (getPolyhedralModelConfig().hasSpectralData)
//        {
//			allModels.putAll(createSpectralModels(smallBodyModel));
//			allModels.put(ModelNames.SPECTRA_BOUNDARIES, new SpectrumBoundaryCollection(smallBodyModel, (SpectraCollection)allModels.get(ModelNames.SPECTRA)));
//            //if (getPolyhedralModelConfig().body == ShapeModelBody.EROS)
//                allModels.put(ModelNames.STATISTICS, new SpectrumStatisticsCollection());
//
//			SpectraCollection customCollection = new SpectraCollection(smallBodyModel);
//			allModels.put(ModelNames.CUSTOM_SPECTRA, customCollection);
//			allModels.put(ModelNames.CUSTOM_SPECTRA_BOUNDARIES, new SpectrumBoundaryCollection(smallBodyModel, (SpectraCollection)allModels.get(ModelNames.CUSTOM_SPECTRA)));
//        }
//
//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//			allModels.putAll(createLidarModels(smallBodyModel));
//        }
//
//        if (getPolyhedralModelConfig().hasLineamentData)
//        {
//			allModels.put(ModelNames.LINEAMENT, createLineament());
//        }
//
//        if (getPolyhedralModelConfig().hasStateHistory)
//        {
//            allModels.put(ModelNames.STATE_HISTORY_COLLECTION, new StateHistoryCollection(smallBodyModel));
//        }
//
//		allModels.put(ModelNames.LINE_STRUCTURES, new LineModel<>(smallBodyModel));
//        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
//        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
//        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
//        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
//        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));
//		allModels.put(ModelNames.TRACKS, new LidarTrackManager(smallBodyModel));
//        DEMCollection demCollection = new DEMCollection(smallBodyModel, getModelManager());
//        allModels.put(ModelNames.DEM, demCollection);
//        DEMBoundaryCollection demBoundaryCollection = new DEMBoundaryCollection(smallBodyModel, getModelManager());
//        allModels.put(ModelNames.DEM_BOUNDARY, demBoundaryCollection);
//
//        setModelManager(new SbmtModelManager(smallBodyModel, allModels));
//        colorImageCollection.setModelManager(getModelManager());
//        cubeCollection.setModelManager(getModelManager());
//        customColorImageCollection.setModelManager(getModelManager());
//        customCubeCollection.setModelManager(getModelManager());
//        demCollection.setModelManager(getModelManager());
//        demBoundaryCollection.setModelManager(getModelManager());
//
//        getModelManager().addPropertyChangeListener(this);
//
//		SBMTInfoWindowManagerFactory.initializeModels(getModelManager(), getStatusBar());
//
//	}
//
//	@Override
//	protected void setupPopupManager()
//	{
//		setPopupManager(new ImagePopupManager(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer()));
//
//        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//        {
//			if (instrument.spectralMode == SpectralImageMode.MONO)
//            {
//            	//regular perspective images
//                ImageCollection images = (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);
//                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModelManager().getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);
//
//                //color perspective images
//                ColorImageCollection colorImages = (ColorImageCollection)getModelManager().getModel(ModelNames.COLOR_IMAGES);
//                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
//                PerspectiveImageBoundaryCollection colorImageBoundaries = (PerspectiveImageBoundaryCollection)getModelManager().getModel(ModelNames.PERSPECTIVE_COLOR_IMAGE_BOUNDARIES);
//                PopupMenu colorImagePopupMenu = new ImagePopupMenu(getModelManager(), images, colorImageBoundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.PERSPECTIVE_COLOR_IMAGE_BOUNDARIES), colorImagePopupMenu);
//
//                //perspective image cubes
//                ImageCubeCollection imageCubes = (ImageCubeCollection)getModelManager().getModel(ModelNames.CUBE_IMAGES);
//                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
//                PerspectiveImageBoundaryCollection imageCubeBoundaries = (PerspectiveImageBoundaryCollection)getModelManager().getModel(ModelNames.PERSPECTIVE_IMAGE_CUBE_BOUNDARIES);
//                PopupMenu imageCubePopupMenu = new ImagePopupMenu(getModelManager(), images, imageCubeBoundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_CUBE_BOUNDARIES), imageCubePopupMenu);
//            }
//
//			else if (instrument.spectralMode == SpectralImageMode.MULTI)
//            {
//                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
//                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);
//
//				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);
//
//                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
//                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);
//            }
//			else if (instrument.spectralMode == SpectralImageMode.HYPER)
//            {
//                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
//                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);
//                ImageCubeCollection imageCubes = (ImageCubeCollection)getModel(ModelNames.CUBE_IMAGES);
//
//				PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);
//
//                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
//                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);
//
//                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
//                registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
//            }
//            }
//
//        if (getPolyhedralModelConfig().hasSpectralData)
//        {
//            //This needs to be updated to handle the multiple Spectral Instruments that can exist on screen at the same time....
////            for (SpectralInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
//            {
//				SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
//				SpectrumBoundaryCollection spectrumBoundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);
//				PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, spectrumBoundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
//				registerPopup(getModel(ModelNames.SPECTRA), popupMenu);
//			}
//		}
//
//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//			LidarTrackManager tmpTrackManager = (LidarTrackManager)getModel(ModelNames.LIDAR_SEARCH);
//			PopupMenu popupMenu = LidarGuiUtil.formLidarTrackPopupMenu(tmpTrackManager, getRenderer());
//			registerPopup(tmpTrackManager, popupMenu);
//        }
//
//        if (getPolyhedralModelConfig().hasLineamentData)
//        {
//            PopupMenu popupMenu = new LineamentPopupMenu(getModelManager());
//            registerPopup(getModel(ModelNames.LINEAMENT), popupMenu);
//        }
//
//        if (getPolyhedralModelConfig().hasMapmaker || getPolyhedralModelConfig().hasBigmap)
//        {
//            PopupMenu popupMenu = new MapletBoundaryPopupMenu(getModelManager(), getRenderer());
//            registerPopup(getModel(ModelNames.DEM_BOUNDARY), popupMenu);
//        }
//    }
//
//	@Override
//	protected void setupTabs()
//	{
//		//small body control panel
//		addTab(getPolyhedralModelConfig().getShapeModelName(), new SmallBodyControlPanel(getModelManager(), getPolyhedralModelConfig().getShapeModelName()));
//
//		//imaging
//        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
//        {
//			Controller<ImageSearchModel, ?> controller =
//						new SpectralImagingSearchController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(),
//															(SbmtSpectrumWindowManager) getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument);
//
//			metadataManagers.put(instrument.instrumentName.toString(), controller.getModel());
//			addTab(instrument.instrumentName.toString(), controller.getView().getComponent());
//		}
//
//        //spectra
//        SpectraCollection spectrumCollection = (SpectraCollection)getModel(ModelNames.SPECTRA);
//        SpectrumBoundaryCollection boundaryCollection = (SpectrumBoundaryCollection)getModel(ModelNames.SPECTRA_BOUNDARIES);
//        for (BasicSpectrumInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
//        {
//            String displayName = instrument.getDisplayName();
//
//			NEARSpectraFactory.initializeModels(smallBodyModel);
//			NISSearchModel model = new NISSearchModel(getModelManager(), instrument);
//			JComponent component = new OREXSpectrumSearchController<NISSpectrum>(getPolyhedralModelConfig().imageSearchDefaultStartDate, getPolyhedralModelConfig().imageSearchDefaultEndDate,
//					getPolyhedralModelConfig().hasHierarchicalSpectraSearch, getPolyhedralModelConfig().imageSearchDefaultMaxSpacecraftDistance, getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification,
//					getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), instrument, model).getPanel();
//            addTab(instrument.getDisplayName(), component);
//
//			PopupMenu popupMenu = new SpectrumPopupMenu(spectrumCollection, boundaryCollection, getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getRenderer());
//			registerPopup(spectrumCollection, popupMenu);
//        }
//
//        addLidarTab();
//
//        addLineamentTab();
//
//        addStructuresTab();
//
//        JTabbedPane customDataPane=new JTabbedPane();
//        customDataPane.setBorder(BorderFactory.createEmptyBorder());
//        addTab("Custom Data", customDataPane);
//
//        ImagingInstrument instrument = null;
//        for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
//        {
//            instrument = i;
//            break;
//        }
//
//		ImageCollection images = (ImageCollection) getModel(ModelNames.CUSTOM_IMAGES);
//		PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection) getModel(ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES);
//		PopupMenu popupMenu = new ImagePopupMenu<>(getModelManager(), images, boundaries, (SbmtInfoWindowManager) getInfoPanelManager(), (SbmtSpectrumWindowManager) getSpectrumPanelManager(), getRenderer(), getRenderer());
//		registerPopup(getModel(ModelNames.CUSTOM_IMAGES), popupMenu);
//        customDataPane.addTab("Images", new CustomImageController(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).getPanel());
//
//		for (BasicSpectrumInstrument i : getPolyhedralModelConfig().spectralInstruments)
//		{
//			customDataPane.addTab(i.getDisplayName() + " Spectra", new CustomSpectraSearchController(getModelManager(), (SbmtInfoWindowManager) getInfoPanelManager(), getPickManager(), getRenderer(), getPolyhedralModelConfig().hierarchicalSpectraSearchSpecification, i).getPanel());
//		}
//
//        // Add the "lidar tracks" tab
//        ModelManager tmpModelManager = getModelManager();
//		LidarTrackManager tmpTrackManager = (LidarTrackManager)tmpModelManager.getModel(ModelNames.TRACKS);
//		LidarLoadPanel tmpLidarLoadPanel = new LidarLoadPanel(tmpTrackManager);
//		LidarTrackPanel tmpLidarListPanel = new LidarTrackPanel(tmpTrackManager, tmpModelManager, getPolyhedralModelConfig(), getPickManager(), getRenderer());
//        JPanel tmpPanel = new JPanel(new MigLayout("", "", "0[]0"));
//        tmpPanel.add(tmpLidarLoadPanel, "growx,wrap");
//        tmpPanel.add(tmpLidarListPanel, "growx,growy,pushx,pushy");
//        customDataPane.addTab("Tracks", tmpPanel);
//
//        addDEMTab();
//
//    	addStateHistoryTab();
//	}
//}
