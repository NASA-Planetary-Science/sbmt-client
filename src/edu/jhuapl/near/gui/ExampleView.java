package edu.jhuapl.near.gui;

import java.util.HashMap;

import javax.swing.JComponent;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.DEMBoundaryCollection;
import edu.jhuapl.near.model.DEMCollection;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.ExampleModelManager;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelFactory;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.SmallBodyConfig;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;


/**
 * A view is a container which contains a control panel and renderer
 * as well as a collection of managers. A view is unique to a specific
 * body. This class is used to build all built-in and custom views.
 * All the configuration details of all the built-in and custom views
 * are contained in this class.
 */
public class ExampleView extends View
{
    /**
     * By default a view should be created empty. Only when the user
     * requests to show a particular View, should the View's contents
     * be created in order to reduce memory and startup time. Therefore,
     * this function should be called prior to first time the View is
     * shown in order to cause it
     */
    public ExampleView(StatusBar statusBar, SmallBodyConfig smallBodyConfig)
    {
        super(statusBar, smallBodyConfig);
    }

    protected void setupModelManager()
    {
        setModelManager(new ExampleModelManager());

        SmallBodyModel smallBodyModel = ModelFactory.createSmallBodyModel(getSmallBodyConfig());
        Graticule graticule = ModelFactory.createGraticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);

        if (getSmallBodyConfig().hasLidarData)
        {
            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
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

        if (getSmallBodyConfig().hasLidarData)
        {
            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
            registerPopup(lidarSearch, popupMenu);
        }
    }

    protected void setupTabs()
    {
        addTab(getSmallBodyConfig().getShapeModelName(), new SmallBodyControlPanel(getModelManager(), getSmallBodyConfig().getShapeModelName()));

        if (getSmallBodyConfig().hasLidarData)
        {
            JComponent component = new LidarPanel(getSmallBodyConfig(), getModelManager(), getPickManager(), getRenderer());
            addTab(getSmallBodyConfig().lidarInstrumentName.toString(), component);
        }


        addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager()));

//        if (!getSmallBodyConfig().customTemporary)
//        {
//            ImagingInstrument instrument = null;
//            for (ImagingInstrument i : getSmallBodyConfig().imagingInstruments)
//            {
//                instrument = i;
//                break;
//            }
//
//            addTab("Images", new CustomImagesPanel(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init());
//        }
//
        addTab("Tracks", new TrackPanel(getSmallBodyConfig(), getModelManager(), getPickManager(), getRenderer()));

    }
}
