package edu.jhuapl.near.popupmenus.eros;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.popupmenus.CirclesPopupMenu;
import edu.jhuapl.near.popupmenus.LinesPopupMenu;
import edu.jhuapl.near.popupmenus.PointsPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class ErosPopupManager extends PopupManager
{
    public ErosPopupManager(
                Renderer erosRenderer,
                ModelManager modelManager,
                ModelInfoWindowManager infoPanelManager)
    {
        super(modelManager);

        PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);

        popupMenu = new MSIPopupMenu(modelManager, infoPanelManager, erosRenderer, erosRenderer);
        registerPopup(modelManager.getModel(ModelNames.MSI_BOUNDARY), popupMenu);

        popupMenu = new MSIPopupMenu(modelManager, infoPanelManager, erosRenderer, erosRenderer);
        registerPopup(modelManager.getModel(ModelNames.MSI_IMAGES), popupMenu);

        popupMenu = new MSIColorPopupMenu(modelManager, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.MSI_COLOR_IMAGES), popupMenu);

        popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.NIS_SPECTRA), popupMenu);

        popupMenu = new NLRPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.NLR_DATA_SEARCH), popupMenu);

        popupMenu = new LinesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.LINE_STRUCTURES), popupMenu);

        popupMenu = new CirclesPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), popupMenu);

        popupMenu = new PointsPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.POINT_STRUCTURES), popupMenu);
    }
}
