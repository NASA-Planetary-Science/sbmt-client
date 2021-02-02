package edu.jhuapl.sbmt.gui.image.ui.images;

import java.awt.AWTException;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.GraticulePopupMenu;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.model.image.ImageCollection;

/**
 * This class is responsible for the creation of popups and for the routing of
 * the right click events (i.e. show popup events) to the correct model.
 */
public class ImagePopupManager extends PopupManager
{
	public ImagePopupManager(ModelManager modelManager, SbmtInfoWindowManager infoPanelManager,
			SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
	{
		super(modelManager);

		try
		{
			PopupMenu popupMenu = new GraticulePopupMenu(modelManager, renderer);
			registerPopup(modelManager.getModel(ModelNames.GRATICULE), popupMenu);
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}

		ImageCollection imageCollection = (ImageCollection) modelManager.getModel(ModelNames.IMAGES);
		PopupMenu popupMenu = new ImagePopupMenu(modelManager, imageCollection, infoPanelManager,
				spectrumPanelManager, renderer, renderer);
		registerPopup(modelManager.getModel(ModelNames.IMAGES), popupMenu);
	}

}
