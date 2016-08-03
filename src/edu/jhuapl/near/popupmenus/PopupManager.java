package edu.jhuapl.near.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.ModelSpectrumWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ImageCollection;
import edu.jhuapl.near.model.LidarSearchDataCollection;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PerspectiveImageBoundaryCollection;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 */
public class PopupManager
{
    private ModelManager modelManager;
    private HashMap<Model, PopupMenu> modelToPopupMap =
        new HashMap<Model, PopupMenu>();


    public PopupManager(ModelManager modelManager, ModelInfoWindowManager infoPanelManager, ModelSpectrumWindowManager spectrumPanelManager, Renderer renderer)
    {
        this.modelManager = modelManager;

        PopupMenu popupMenu = new LinesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.LINE_STRUCTURES), popupMenu);

        popupMenu = new PolygonsPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.POLYGON_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), popupMenu);

        popupMenu = new EllipsesPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), popupMenu);

        popupMenu = new PointsPopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.POINT_STRUCTURES), popupMenu);

        popupMenu = new GraticulePopupMenu(modelManager, renderer);
        registerPopup(modelManager.getModel(ModelNames.GRATICULE), popupMenu);

        ImageCollection imageCollection =
                (ImageCollection)modelManager.getModel(ModelNames.IMAGES);
        PerspectiveImageBoundaryCollection imageBoundaries =
                (PerspectiveImageBoundaryCollection)modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
        popupMenu = new ImagePopupMenu(modelManager, imageCollection, imageBoundaries, infoPanelManager, spectrumPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.IMAGES), popupMenu);

        LidarSearchDataCollection tracks = (LidarSearchDataCollection)modelManager.getModel(ModelNames.TRACKS);
        popupMenu = new LidarPopupMenu(tracks, renderer);
        registerPopup(tracks, popupMenu);
    }

    public PopupMenu getPopup(Model model)
    {
        return modelToPopupMap.get(model);
    }

//    public PopupMenu getPopup(String name)
//    {
//        Model model = modelManager.getModel(name);
//        if (model != null)
//            return modelToPopupMap.get(model);
//        else
//            return null;
//    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
    {
        PopupMenu popup = modelToPopupMap.get(modelManager.getModel(pickedProp));
        if (popup != null)
            popup.showPopup(e, pickedProp, pickedCellId, pickedPosition);
    }

    public void showPopup(MouseEvent e, ModelNames name)
    {
        PopupMenu popup = modelToPopupMap.get(modelManager.getModel(name));
        if (popup != null)
            popup.show(e.getComponent(), e.getX(), e.getY());
    }

    protected HashMap<Model, PopupMenu> getModelToPopupMap()
    {
        return modelToPopupMap;
    }

    public void registerPopup(Model model, PopupMenu menu)
    {
        modelToPopupMap.put(model, menu);
    }
}
