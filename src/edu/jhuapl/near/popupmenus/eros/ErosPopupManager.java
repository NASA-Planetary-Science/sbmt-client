package edu.jhuapl.near.popupmenus.eros;

import java.util.HashMap;

import vtk.vtkRenderWindowPanel;
import edu.jhuapl.near.gui.Renderer;
import edu.jhuapl.near.gui.eros.ModelInfoWindowManager;
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
				ErosModelManager modelManager,
				ModelInfoWindowManager infoPanelManager)
	{
		super(modelManager);
		
		vtkRenderWindowPanel renWin = erosRenderer.getRenderWindowPanel();
		
		lineamentPopupMenu = 
			new LineamentPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.LINEAMENT), lineamentPopupMenu);
		
		msiBoundariesPopupMenu= 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.MSI_BOUNDARY), msiBoundariesPopupMenu);
		
		msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.MSI_IMAGES), msiImagesPopupMenu);
		
		nisSpectraPopupMenu = 
			new NISPopupMenu(modelManager, infoPanelManager, renWin);
		modelToPopupMap.put(modelManager.getModel(ErosModelManager.NIS_SPECTRA), nisSpectraPopupMenu);
		
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
