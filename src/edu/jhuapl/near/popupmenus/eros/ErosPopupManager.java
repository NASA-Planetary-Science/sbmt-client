package edu.jhuapl.near.popupmenus.eros;

import java.util.HashMap;

import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.model.Model;
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
    private LineamentPopupMenu lineamentPopupMenu;
    private MSIPopupMenu msiImagesPopupMenu;
    private MSIPopupMenu msiBoundariesPopupMenu;
    private NISPopupMenu nisSpectraPopupMenu;
    private LinesPopupMenu linesPopupMenu;
    private CirclesPopupMenu circlesPopupMenu;
    private PointsPopupMenu pointsPopupMenu;
    
    private HashMap<Model, PopupMenu> modelToPopupMap =
    	new HashMap<Model, PopupMenu>();
    
	public ErosPopupManager(
				Renderer erosRenderer, 
				ModelManager modelManager,
				ModelInfoWindowManager infoPanelManager)
	{
		super(modelManager);
		
		lineamentPopupMenu = 
			new LineamentPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelNames.LINEAMENT), lineamentPopupMenu);
		
		msiBoundariesPopupMenu= 
			new MSIPopupMenu(modelManager, infoPanelManager, erosRenderer, erosRenderer);
		modelToPopupMap.put(modelManager.getModel(ModelNames.MSI_BOUNDARY), msiBoundariesPopupMenu);
		
		msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, infoPanelManager, erosRenderer, erosRenderer);
		modelToPopupMap.put(modelManager.getModel(ModelNames.MSI_IMAGES), msiImagesPopupMenu);
		
		nisSpectraPopupMenu = 
			new NISPopupMenu(modelManager, infoPanelManager);
		modelToPopupMap.put(modelManager.getModel(ModelNames.NIS_SPECTRA), nisSpectraPopupMenu);
		
		linesPopupMenu =
			new LinesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelNames.LINE_STRUCTURES), linesPopupMenu);
		
		circlesPopupMenu =
			new CirclesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelNames.CIRCLE_STRUCTURES), circlesPopupMenu);

		pointsPopupMenu =
			new PointsPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelNames.POINT_STRUCTURES), pointsPopupMenu);
	}
	
	protected HashMap<Model, PopupMenu> getModelToPopupMap()
	{
		return modelToPopupMap;
	}
}
