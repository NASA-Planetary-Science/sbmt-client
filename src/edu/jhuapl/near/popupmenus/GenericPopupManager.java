package edu.jhuapl.near.popupmenus;

import java.util.HashMap;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.eros.ErosModelManager;
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
public class GenericPopupManager extends PopupManager
{
    private LinesPopupMenu linesPopupMenu;
    private CirclesPopupMenu circlesPopupMenu;
    private PointsPopupMenu pointsPopupMenu;
    
    private HashMap<Model, PopupMenu> modelToPopupMap =
    	new HashMap<Model, PopupMenu>();
    
	public GenericPopupManager(
				ErosModelManager modelManager)
	{
		super(modelManager);
		
		linesPopupMenu =
			new LinesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.LINE_STRUCTURES), linesPopupMenu);
		
		circlesPopupMenu =
			new CirclesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.CIRCLE_STRUCTURES), circlesPopupMenu);

		pointsPopupMenu =
			new PointsPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.POINT_STRUCTURES), pointsPopupMenu);
	}
	
	protected HashMap<Model, PopupMenu> getModelToPopupMap()
	{
		return modelToPopupMap;
	}
}
