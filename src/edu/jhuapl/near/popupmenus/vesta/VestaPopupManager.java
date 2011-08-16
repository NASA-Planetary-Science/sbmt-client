package edu.jhuapl.near.popupmenus.vesta;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.vesta.FcBoundaryCollection;
import edu.jhuapl.near.model.vesta.FcColorImageCollection;
import edu.jhuapl.near.model.vesta.FcImageCollection;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class VestaPopupManager extends PopupManager
{
    public VestaPopupManager(
                Renderer renderer,
                ModelManager modelManager,
                ModelInfoWindowManager infoPanelManager)
    {
        super(modelManager);

        FcImageCollection fcImages = (FcImageCollection)modelManager.getModel(ModelNames.FC_IMAGES);
        FcBoundaryCollection fcBoundaries = (FcBoundaryCollection)modelManager.getModel(ModelNames.FC_BOUNDARY);
        FcColorImageCollection fcColorImages = (FcColorImageCollection)modelManager.getModel(ModelNames.FC_COLOR_IMAGES);

        PopupMenu popupMenu = new ImagePopupMenu(fcImages, fcBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.FC_BOUNDARY), popupMenu);

        popupMenu = new ImagePopupMenu(fcImages, fcBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.FC_IMAGES), popupMenu);

        popupMenu = new ColorImagePopupMenu(fcColorImages, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.FC_COLOR_IMAGES), popupMenu);
    }
}
