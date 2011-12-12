package edu.jhuapl.near.popupmenus.itokawa;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.itokawa.AmicaBoundaryCollection;
import edu.jhuapl.near.model.itokawa.AmicaColorImageCollection;
import edu.jhuapl.near.model.itokawa.AmicaImageCollection;
import edu.jhuapl.near.model.itokawa.HayLidarSearchDataCollection;
import edu.jhuapl.near.model.itokawa.HayLidarUnfilteredSearchDataCollection;
import edu.jhuapl.near.popupmenus.ColorImagePopupMenu;
import edu.jhuapl.near.popupmenus.ImagePopupMenu;
import edu.jhuapl.near.popupmenus.LidarPopupMenu;
import edu.jhuapl.near.popupmenus.PopupManager;
import edu.jhuapl.near.popupmenus.PopupMenu;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class ItokawaPopupManager extends PopupManager
{
    public ItokawaPopupManager(
                Renderer renderer,
                ModelManager modelManager,
                ModelInfoWindowManager infoPanelManager)
    {
        super(modelManager);

        AmicaImageCollection amicaImages = (AmicaImageCollection)modelManager.getModel(ModelNames.AMICA_IMAGES);
        AmicaBoundaryCollection amicaBoundaries = (AmicaBoundaryCollection)modelManager.getModel(ModelNames.AMICA_BOUNDARY);
        AmicaColorImageCollection amicaColorImages = (AmicaColorImageCollection)modelManager.getModel(ModelNames.AMICA_COLOR_IMAGES);
        HayLidarSearchDataCollection lidarSearch = (HayLidarSearchDataCollection)modelManager.getModel(ModelNames.HAYLIDAR_SEARCH);
        HayLidarUnfilteredSearchDataCollection lidarSearchUnfiltered =
            (HayLidarUnfilteredSearchDataCollection)modelManager.getModel(ModelNames.HAYLIDAR_SEARCH_UNFILTERED);

        PopupMenu popupMenu = new ImagePopupMenu(amicaImages, amicaBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.AMICA_BOUNDARY), popupMenu);

        popupMenu = new ImagePopupMenu(amicaImages, amicaBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.AMICA_IMAGES), popupMenu);

        popupMenu = new ColorImagePopupMenu(amicaColorImages, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.AMICA_COLOR_IMAGES), popupMenu);

        popupMenu = new LidarPopupMenu(lidarSearch, renderer);
        registerPopup(modelManager.getModel(ModelNames.HAYLIDAR_SEARCH), popupMenu);

        popupMenu = new LidarPopupMenu(lidarSearchUnfiltered, renderer);
        registerPopup(modelManager.getModel(ModelNames.HAYLIDAR_SEARCH_UNFILTERED), popupMenu);
    }
}
