package edu.jhuapl.near.gui;

import java.util.HashMap;

import javax.swing.JComponent;

import edu.jhuapl.near.gui.eros.LineamentControlPanel;
import edu.jhuapl.near.gui.eros.NISSearchPanel;
import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.ColorImageCollection;
import edu.jhuapl.near.model.DEMBoundaryCollection;
import edu.jhuapl.near.model.DEMCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.Image.SpectralMode;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.ImageCubeCollection;
import edu.jhuapl.near.model.ImagingInstrument;
import edu.jhuapl.near.model.Instrument;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SbmtModelManager;
import edu.jhuapl.near.model.ShapeModelAuthor;
import edu.jhuapl.near.model.ShapeModelBody;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.NISStatisticsCollection;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImageCubePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.popupmenus.MapletBoundaryPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;
import edu.jhuapl.near.popupmenus.eros.LineamentPopupMenu;
import edu.jhuapl.near.popupmenus.eros.NISPopupMenu;
import edu.jhuapl.near.util.Configuration;


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
    public SbmtView(StatusBar statusBar, SmallBodyConfig smallBodyConfig)
    {
        super(statusBar, smallBodyConfig);
    }

    protected void setupModelManager()
    {
        setModelManager(new SbmtModelManager());

        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(getSmallBodyConfig());
        Graticule graticule = ModelFactory.createGraticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);
        allModels.put(ModelNames.IMAGES, new ImageCollection(smallBodyModel));

        for (ImagingInstrument instrument : getSmallBodyConfig().imagingInstruments)
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

        if (getSmallBodyConfig().hasSpectralData)
        {
            allModels.put(ModelNames.SPECTRA, ModelFactory.createSpectralModel(smallBodyModel));
            if (getSmallBodyConfig().body == ShapeModelBody.EROS)
                allModels.put(ModelNames.STATISTICS, new NISStatisticsCollection());
        }

        if (getSmallBodyConfig().hasLidarData)
        {
            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
        }

        if (getSmallBodyConfig().hasLineamentData)
        {
            allModels.put(ModelNames.LINEAMENT, ModelFactory.createLineament());
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

    protected void setupPopupManager()
    {
        setPopupManager(new PopupManager(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getRenderer()));

        for (ImagingInstrument instrument : getSmallBodyConfig().imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                ImageCollection images = (ImageCollection)getModelManager().getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModelManager().getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModelManager().getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)getModelManager().getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, getInfoPanelManager(), getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, getInfoPanelManager(), getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, getInfoPanelManager(), getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                ImageCollection images = (ImageCollection)getModel(ModelNames.IMAGES);
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
                ColorImageCollection colorImages = (ColorImageCollection)getModel(ModelNames.COLOR_IMAGES);
                ImageCubeCollection imageCubes = (ImageCubeCollection)getModel(ModelNames.CUBE_IMAGES);

                PopupMenu popupMenu = new ImagePopupMenu(getModelManager(), images, boundaries, getInfoPanelManager(), getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES), popupMenu);

                popupMenu = new ColorImagePopupMenu(colorImages, getInfoPanelManager(), getModelManager(), getRenderer());
                registerPopup(getModel(ModelNames.COLOR_IMAGES), popupMenu);

                popupMenu = new ImageCubePopupMenu(imageCubes, boundaries, getInfoPanelManager(), getSpectrumPanelManager(), getRenderer(), getRenderer());
                registerPopup(getModel(ModelNames.CUBE_IMAGES), popupMenu);
            }
            }

        if (getSmallBodyConfig().hasSpectralData)
        {
            PopupMenu popupMenu = new NISPopupMenu(getModelManager(), getInfoPanelManager());
            registerPopup(getModel(ModelNames.SPECTRA), popupMenu);
        }

        if (getSmallBodyConfig().hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
            registerPopup(lidarSearch, popupMenu);
        }

        if (getSmallBodyConfig().hasLineamentData)
        {
            PopupMenu popupMenu = new LineamentPopupMenu(getModelManager());
            registerPopup(getModel(ModelNames.LINEAMENT), popupMenu);
        }

        if (getSmallBodyConfig().hasMapmaker || getSmallBodyConfig().hasBigmap)
        {
            PopupMenu popupMenu = new MapletBoundaryPopupMenu(getModelManager(), getRenderer());
            registerPopup(getModel(ModelNames.DEM_BOUNDARY), popupMenu);
        }
    }

    protected void setupTabs()
    {
        addTab(getSmallBodyConfig().getShapeModelName(), new SmallBodyControlPanel(getModelManager(), getSmallBodyConfig().getShapeModelName()));

        for (ImagingInstrument instrument : getSmallBodyConfig().imagingInstruments)
        {
            if (instrument.spectralMode == SpectralMode.MONO)
            {
                // For the public version, only include image tab for Eros (all) and Gaskell's Itokawa shape models.
                if (getSmallBodyConfig().body == ShapeModelBody.EROS)
                {
                    JComponent component = new CubicalImagingSearchPanel(getSmallBodyConfig(), getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
                else if (Configuration.isAPLVersion() || (getSmallBodyConfig().body == ShapeModelBody.ITOKAWA && ShapeModelAuthor.GASKELL == getSmallBodyConfig().author))
                {
                    JComponent component = new ImagingSearchPanel(getSmallBodyConfig(), getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
            }

            else if (instrument.spectralMode == SpectralMode.MULTI)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new QuadraspectralImagingSearchPanel(getSmallBodyConfig(), getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
            }
            else if (instrument.spectralMode == SpectralMode.HYPER)
            {
                if (Configuration.isAPLVersion())
                {
                    JComponent component = new HyperspectralImagingSearchPanel(getSmallBodyConfig(), getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument, SmallBodyConfig.LEISA_NBANDS).init();
                    addTab(instrument.instrumentName.toString(), component);
                }
            }
        }

        if (getSmallBodyConfig().hasSpectralData)
        {
            JComponent component = new NISSearchPanel(getModelManager(), getInfoPanelManager(), getPickManager(), getRenderer());
            addTab(Instrument.NIS.toString(), component);
        }

        if (getSmallBodyConfig().hasLidarData)
        {
            JComponent component = new LidarPanel(getSmallBodyConfig(), getModelManager(), getPickManager(), getRenderer());
            addTab(getSmallBodyConfig().lidarInstrumentName.toString(), component);
        }

        if (Configuration.isAPLVersion())
        {
            if (getSmallBodyConfig().hasLineamentData)
            {
                JComponent component = new LineamentControlPanel(getModelManager());
                addTab("Lineament", component);
            }

            addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager()));
            if (!getSmallBodyConfig().customTemporary)
            {
                ImagingInstrument instrument = null;
                for (ImagingInstrument i : getSmallBodyConfig().imagingInstruments)
                {
                    instrument = i;
                    break;
                }

                addTab("Images", new CustomImagesPanel(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init());
            }

            addTab("Tracks", new TrackPanel(getSmallBodyConfig(), getModelManager(), getPickManager(), getRenderer()));

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

            JComponent component = new CustomDEMPanel(getModelManager(), getPickManager(), getSmallBodyConfig().rootDirOnServer,
                    getSmallBodyConfig().hasMapmaker, getSmallBodyConfig().hasBigmap);
            addTab("DEMs", component);
        }
    }

}
