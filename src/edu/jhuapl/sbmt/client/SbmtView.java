package edu.jhuapl.sbmt.client;

import java.util.HashMap;

import javax.swing.JComponent;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.View;
import edu.jhuapl.saavtk.gui.panel.StructuresControlPanel;
import edu.jhuapl.saavtk.model.Graticule;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.CircleSelectionModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.gui.dem.CustomDEMPanel;
import edu.jhuapl.sbmt.gui.dem.MapletBoundaryPopupMenu;
import edu.jhuapl.sbmt.gui.eros.LineamentControlPanel;
import edu.jhuapl.sbmt.gui.eros.LineamentPopupMenu;
import edu.jhuapl.sbmt.gui.eros.NISSearchPanel;
import edu.jhuapl.sbmt.gui.image.ColorImagePopupMenu;
import edu.jhuapl.sbmt.gui.image.CubicalImagingSearchPanel;
import edu.jhuapl.sbmt.gui.image.CustomImagesPanel;
import edu.jhuapl.sbmt.gui.image.HyperspectralImagingSearchPanel;
import edu.jhuapl.sbmt.gui.image.ImageCubePopupMenu;
import edu.jhuapl.sbmt.gui.image.ImagePickManager;
import edu.jhuapl.sbmt.gui.image.ImagePopupManager;
import edu.jhuapl.sbmt.gui.image.ImagePopupMenu;
import edu.jhuapl.sbmt.gui.image.ImagingSearchPanel;
import edu.jhuapl.sbmt.gui.image.QuadraspectralImagingSearchPanel;
import edu.jhuapl.sbmt.gui.lidar.LidarPanel;
import edu.jhuapl.sbmt.gui.lidar.LidarPopupMenu;
import edu.jhuapl.sbmt.gui.lidar.TrackPanel;
import edu.jhuapl.sbmt.gui.spectrum.SpectrumPopupMenu;
import edu.jhuapl.sbmt.gui.time.StateHistoryPanel;
import edu.jhuapl.sbmt.model.bennu.otes.OTES;
import edu.jhuapl.sbmt.model.bennu.otes.OTESSearchPanel;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRS;
import edu.jhuapl.sbmt.model.bennu.ovirs.OVIRSSearchPanel;
import edu.jhuapl.sbmt.model.dem.DEMBoundaryCollection;
import edu.jhuapl.sbmt.model.dem.DEMCollection;
import edu.jhuapl.sbmt.model.eros.NIS;
import edu.jhuapl.sbmt.model.eros.SpectrumStatisticsCollection;
import edu.jhuapl.sbmt.model.image.ColorImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCollection;
import edu.jhuapl.sbmt.model.image.ImageCubeCollection;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;
import edu.jhuapl.sbmt.model.image.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.lidar.LidarSearchDataCollection;
import edu.jhuapl.sbmt.model.rosetta.OsirisImagingSearchPanel;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3;
import edu.jhuapl.sbmt.model.ryugu.nirs3.NIRS3SearchPanel;
import edu.jhuapl.sbmt.model.spectrum.SpectralInstrument;
import edu.jhuapl.sbmt.model.time.StateHistoryCollection;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. This class is used to build all built-in and custom views.
 * All the configuration details of all the built-in and custom views
 * are contained in this class.
 */
public class SbmtView extends View
{
    /**
     * By default a view should be created empty. Only when the user
     * requests to show a particular View, should the View's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the View is
     * shown in order to cause it
     */
    public SbmtView(StatusBar statusBar, SmallBodyViewConfig smallBodyConfig)
    {
        super(statusBar, smallBodyConfig);
    }


    public SmallBodyViewConfig getPolyhedralModelConfig()
    {
        return (SmallBodyViewConfig)super.getConfig();
    }

    @Override
    public String getDisplayName()
    {
    	String result = "";
    	SmallBodyViewConfig config = getPolyhedralModelConfig();
    	if (config.customName != null)
    	    result = config.customName;
    	else if (config.author == null)
    	    result = config.body.toString();
    	else
    	    result = config.author.toString();

    	if (config.version != null)
    	    result = result + " (" + config.version + ")";

    	return result;
    }

    @Override
    protected void setupModelManager()
    {
        SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(getPolyhedralModelConfig());
        setModelManager(new SbmtModelManager(smallBodyModel));

        Graticule graticule = SbmtModelFactory.createGraticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));

        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
                allModels.put(ModelNames.CUBE_IMAGES, new ImageCubeCollection(smallBodyModel, getModelManager()));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                allModels.put(ModelNames.COLOR_IMAGES, new ColorImageCollection(smallBodyModel, getModelManager()));
                allModels.put(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES, new PerspectiveImageBoundaryCollection(smallBodyModel));
            }
        }

        for (SpectralInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
        {
            allModels.put(ModelNames.SPECTRA, SbmtModelFactory.createSpectralModel(smallBodyModel));
            //if (getPolyhedralModelConfig().body == ShapeModelBody.EROS)
                allModels.put(ModelNames.STATISTICS, new SpectrumStatisticsCollection());
        }

        if (getPolyhedralModelConfig().hasLidarData)
        {
            allModels.putAll(SbmtModelFactory.createLidarModels(smallBodyModel));
        }

        if (getPolyhedralModelConfig().hasLineamentData)
        {
            allModels.put(ModelNames.LINEAMENT, SbmtModelFactory.createLineament());
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
        allModels.put(ModelNames.TRACKS, new LidarSearchDataCollection(smallBodyModel));
        allModels.put(ModelNames.DEM, new DEMCollection(smallBodyModel, getModelManager()));
        allModels.put(ModelNames.DEM_BOUNDARY, new DEMBoundaryCollection(smallBodyModel, getModelManager()));

        setModels(allModels);
    }

    @Override
    protected void setupPopupManager()
    {
        setPopupManager(new ImagePopupManager(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer()));

        for (ImagingInstrument instrument : getPolyhedralModelConfig().imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                ImageCollection images = (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModelManager().getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModelManager().getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)getModelManager().getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, (SbmtInfoWindowManager)getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }
            }

        if (getPolyhedralModelConfig().hasSpectralData)
        {
            PopupMenu popupMenu = new SpectrumPopupMenu(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getRenderer());
            registerPopup(getModel(ModelNames.SPECTRA), popupMenu);
        }

        if (getPolyhedralModelConfig().hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
            registerPopup(lidarSearch, popupMenu);
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
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
                if (getPolyhedralModelConfig().body == ShapeModelBody.EROS)
                {
                    JComponent component = new CubicalImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
                else if (Configuration.isAPLVersion() || (getPolyhedralModelConfig().body == ShapeModelBody.ITOKAWA && ShapeModelAuthor.GASKELL == getPolyhedralModelConfig().author))
                {
                    if (getPolyhedralModelConfig().body == ShapeModelBody._67P)
                    {

                        JComponent component = new OsirisImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                        addTab(instrument.instrumentName.toString(), component);
                    }
                    else
                    {
                        JComponent component = new ImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                        addTab(instrument.instrumentName.toString(), component);
                    }
                }
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new QuadraspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new HyperspectralImagingSearchPanel(getPolyhedralModelConfig(), getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyViewConfig.LEISA_NBANDS).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
            }
        }

        for (SpectralInstrument instrument : getPolyhedralModelConfig().spectralInstruments)
        {
            if (instrument instanceof NIS)
            {
            JComponent component = new NISSearchPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer(), instrument);
            addTab(instrument.getDisplayName(), component);
            }
            else
                if (instrument instanceof OTES)
                {
               JComponent component = new OTESSearchPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer(), instrument);
                addTab(instrument.getDisplayName(), component);
                }
                else if (instrument instanceof OVIRS)
                {
               JComponent component = new OVIRSSearchPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer(), instrument);
                addTab(instrument.getDisplayName(), component);
                }
                else if (instrument instanceof NIRS3)
                    {
                    JComponent component = new NIRS3SearchPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer(), instrument);
                    addTab(instrument.getDisplayName(), component);
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

            addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager()));
            if (!getPolyhedralModelConfig().customTemporary)
            {
                ImagingInstrument instrument = null;
                for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
                {
                    instrument = i;
                    break;
                }

                addTab("Images", new CustomImagesPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), (SbmtSpectrumWindowManager)getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init());
            }

            addTab("Tracks", new TrackPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer()));

            /*if (getSmallBodyConfig().hasMapmaker)
            {
                JComponent component = new MapmakerPanel(getModelManager(), getPickManager(), getSmallBodyConfig().rootDirOnServer + "/mapmaker.zip");
                addTab("Mapmaker", component);
            }

            if (getSmallBodyConfig().hasBigmap)
            {
                JComponent component = new BigmapPanel(getModelManager(), getPickManager(), getSmallBodyConfig().rootDirOnServer + "/bigmap.zip");
                addTab("Bigmap", component);
            }*/

            /*if(getSmallBodyConfig().hasMapmaker || getSmallBodyConfig().hasBigmap)
            {
                JComponent component = new DEMPanel(getModelManager(), getPickManager(), getSmallBodyConfig().rootDirOnServer,
                        getSmallBodyConfig().hasMapmaker, getSmallBodyConfig().hasBigmap);
                addTab("DEMs", component);
            }*/

            JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getPolyhedralModelConfig().rootDirOnServer,
                    getPolyhedralModelConfig().hasMapmaker, getPolyhedralModelConfig().hasBigmap);
            addTab("DEMs", component);

            if (getConfig().hasStateHistory)
            {
                addTab("Observing Conditions", new StateHistoryPanel(getModelManager(), (SbmtInfoWindowManager)getInfoPanelManager(), getPickManager(), getRenderer()));
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
        setSpectrumPanelManager(new SbmtSpectrumWindowManager(getModelManager()));
    }




}
