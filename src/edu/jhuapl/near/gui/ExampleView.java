package edu.jhuapl.near.gui;

import java.util.HashMap;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.CircleSelectionModel;
import edu.jhuapl.near.model.EllipseModel;
import edu.jhuapl.near.model.ExampleModelManager;
import edu.jhuapl.near.model.ExamplePolyhedralModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.PolygonModel;
import edu.jhuapl.near.model.PolyhedralModel;
import edu.jhuapl.near.model.PolyhedralModelConfig;
import edu.jhuapl.near.model.ShapeModelAuthor;
import edu.jhuapl.near.popupmenus.PopupManager;


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
    public ExampleView(StatusBar statusBar, PolyhedralModelConfig polyhedralModelConfig)
    {
        super(statusBar, polyhedralModelConfig);
    }

    public String getDisplayName()
    {
        if (getPolyhedralModelConfig().author == ShapeModelAuthor.CUSTOM)
            return getPolyhedralModelConfig().customName;
        else
        {
            String version = "";
            if (getPolyhedralModelConfig().version != null)
                version += " (" + getPolyhedralModelConfig().version + ")";
            return getPolyhedralModelConfig().author.toString() + version;
        }
    }

    protected void setupModelManager()
    {
        setModelManager(new ExampleModelManager());

        PolyhedralModel smallBodyModel = new ExamplePolyhedralModel((PolyhedralModelConfig)getPolyhedralModelConfig());

        Graticule graticule = new Graticule(smallBodyModel);

        HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
        allModels.put(ModelNames.SMALL_BODY, smallBodyModel);
        allModels.put(ModelNames.GRATICULE, graticule);

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            allModels.putAll(ModelFactory.createLidarModels(smallBodyModel));
//        }

        allModels.put(ModelNames.LINE_STRUCTURES, new LineModel(smallBodyModel));
        allModels.put(ModelNames.POLYGON_STRUCTURES, new PolygonModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_STRUCTURES, new CircleModel(smallBodyModel));
        allModels.put(ModelNames.ELLIPSE_STRUCTURES, new EllipseModel(smallBodyModel));
        allModels.put(ModelNames.POINT_STRUCTURES, new PointModel(smallBodyModel));
        allModels.put(ModelNames.CIRCLE_SELECTION, new CircleSelectionModel(smallBodyModel));

//        allModels.put(ModelNames.TRACKS, new LidarSearchDataCollection(smallBodyModel));

        setModels(allModels);
    }

    protected void setupPopupManager()
    {
        setPopupManager(new PopupManager(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getRenderer()));

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            LidarSearchDataCollection lidarSearch = (LidarSearchDataCollection)getModel(ModelNames.LIDAR_SEARCH);
//            PopupMenu popupMenu = new LidarPopupMenu(lidarSearch, getRenderer());
//            registerPopup(lidarSearch, popupMenu);
//        }
    }

    protected void setupTabs()
    {
        addTab(getPolyhedralModelConfig().getShapeModelName(), new SmallBodyControlPanel(getModelManager(), getPolyhedralModelConfig().getShapeModelName()));

//        if (getPolyhedralModelConfig().hasLidarData)
//        {
//            JComponent component = new LidarPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer());
//            addTab(getPolyhedralModelConfig().lidarInstrumentName.toString(), component);
//        }


        addTab("Structures", new StructuresControlPanel(getModelManager(), getPickManager()));

//        if (!getPolyhedralModelConfig().customTemporary)
//        {
//            ImagingInstrument instrument = null;
//            for (ImagingInstrument i : getPolyhedralModelConfig().imagingInstruments)
//            {
//                instrument = i;
//                break;
//            }
//
//            addTab("Images", new CustomImagesPanel(getModelManager(), getInfoPanelManager(), getSpectrumPanelManager(), getPickManager(), getRenderer(), instrument).init());
//        }
//

//        addTab("Tracks", new TrackPanel(getPolyhedralModelConfig(), getModelManager(), getPickManager(), getRenderer()));

    }
}
