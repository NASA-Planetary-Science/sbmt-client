package edu.jhuapl.near.popupmenus;

import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class GenericPopupManager extends PopupManager
{
    public GenericPopupManager(
            ModelManager modelManager)
    {
        super(modelManager);

        PopupMenu popupMenu = new LinesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.LINE_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), popupMenu);

        popupMenu = new PointsPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.POINT_STRUCTURES), popupMenu);
    }
}
