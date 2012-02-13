package edu.jhuapl.near.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyImageMapCollection;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class PopupManager
{
    private ModelManager modelManager;
    private HashMap<Model, PopupMenu> modelToPopupMap =
        new HashMap<Model, PopupMenu>();


    public PopupManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;

        PopupMenu popupMenu = new LinesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.LINE_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.ELLIPSE_STRUCTURES), popupMenu);

        popupMenu = new PointsPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.POINT_STRUCTURES), popupMenu);

        SmallBodyImageMapCollection imageMapCollection =
            (SmallBodyImageMapCollection)modelManager.getModel(ModelNames.SMALL_BODY_IMAGE_MAP);
        popupMenu = new SmallBodyImagePopupMenu(imageMapCollection);
        registerPopup(modelManager.getModel(ModelNames.SMALL_BODY_IMAGE_MAP), popupMenu);
    }

//    public PopupMenu getPopup(Model model)
//    {
//        return modelToPopupMap.get(model);
//    }
//
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

    public void showPopup(MouseEvent e, String name)
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
