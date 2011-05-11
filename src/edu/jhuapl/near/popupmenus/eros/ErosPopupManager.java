package edu.jhuapl.near.popupmenus.eros;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.eros.MSIBoundaryCollection;
import edu.jhuapl.near.model.eros.MSIColorImageCollection;
import edu.jhuapl.near.model.eros.MSIImageCollection;
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
public class ErosPopupManager extends PopupManager
{
    public ErosPopupManager(
                Renderer renderer,
                ModelManager modelManager,
                ModelInfoWindowManager infoPanelManager)
    {
        super(modelManager);

        PopupMenu popupMenu = new LineamentPopupMenu(modelManager);
        registerPopup(modelManager.getModel(ModelNames.LINEAMENT), popupMenu);

        MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelNames.MSI_IMAGES);
        MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelNames.MSI_BOUNDARY);
        MSIColorImageCollection msiColorImages = (MSIColorImageCollection)modelManager.getModel(ModelNames.MSI_COLOR_IMAGES);

        popupMenu = new ImagePopupMenu(msiImages, msiBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.MSI_BOUNDARY), popupMenu);

        popupMenu = new ImagePopupMenu(msiImages, msiBoundaries, infoPanelManager, renderer, renderer);
        registerPopup(modelManager.getModel(ModelNames.MSI_IMAGES), popupMenu);

        popupMenu = new ColorImagePopupMenu(msiColorImages, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.MSI_COLOR_IMAGES), popupMenu);

        popupMenu = new NISPopupMenu(modelManager, infoPanelManager);
        registerPopup(modelManager.getModel(ModelNames.NIS_SPECTRA), popupMenu);

        //popupMenu = new NLRPopupMenu(modelManager);
        //registerPopup(modelManager.getModel(ModelNames.NLR_DATA_SEARCH), popupMenu);
    }
}
