package edu.jhuapl.near.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;
import vtk.vtkRenderWindowPanel;
import edu.jhuapl.near.gui.ErosRenderer;
import edu.jhuapl.near.gui.ModelInfoWindowManager;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public class PopupManager
{
    private LineamentPopupMenu lineamentPopupMenu;
    private MSIPopupMenu msiImagesPopupMenu;
    private MSIPopupMenu msiBoundariesPopupMenu;
    private NISPopupMenu nisSpectraPopupMenu;
    private LinesPopupMenu linesPopupMenu;
    private CirclesPopupMenu circlesPopupMenu;
    private PointsPopupMenu pointsPopupMenu;
    private ModelManager modelManager;
    
    private HashMap<Model, PopupMenu> modelToPopupMap =
    	new HashMap<Model, PopupMenu>();
    
	public PopupManager(
				ErosRenderer erosRenderer, 
				ModelManager modelManager,
				ModelInfoWindowManager infoPanelManager)
	{
		vtkRenderWindowPanel renWin = erosRenderer.getRenderWindowPanel();
		this.modelManager = modelManager;
		
		lineamentPopupMenu = 
			new LineamentPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelManager.LINEAMENT), lineamentPopupMenu);
		
		msiBoundariesPopupMenu= 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		modelToPopupMap.put(modelManager.getModel(ModelManager.MSI_BOUNDARY), msiBoundariesPopupMenu);
		
		msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		modelToPopupMap.put(modelManager.getModel(ModelManager.MSI_IMAGES), msiImagesPopupMenu);
		
		nisSpectraPopupMenu = 
			new NISPopupMenu(modelManager, infoPanelManager, renWin);
		modelToPopupMap.put(modelManager.getModel(ModelManager.NIS_SPECTRA), nisSpectraPopupMenu);
		
		linesPopupMenu =
			new LinesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelManager.LINE_STRUCTURES), linesPopupMenu);
		
		circlesPopupMenu =
			new CirclesPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelManager.CIRCLE_STRUCTURES), circlesPopupMenu);

		pointsPopupMenu =
			new PointsPopupMenu(modelManager);
		modelToPopupMap.put(modelManager.getModel(ModelManager.POINT_STRUCTURES), pointsPopupMenu);
	}
	
    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
    {
    	PopupMenu popup = modelToPopupMap.get(modelManager.getModel(pickedProp));
    	if (popup != null)
    		popup.showPopup(e, pickedProp, pickedCellId, pickedPosition);
    }
}
