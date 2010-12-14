package edu.jhuapl.near.popupmenus;

import java.awt.event.MouseEvent;
import java.util.HashMap;

import vtk.vtkProp;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.popupmenus.PopupMenu;

/**
 * This class is responsible for the creation of popups and for the routing
 * of the right click events (i.e. show popup events) to the correct model.
 * @author eli
 *
 */
public abstract class PopupManager
{
    private ModelManager modelManager;

	public PopupManager(ModelManager modelManager)
	{
		this.modelManager = modelManager;
	}

	protected abstract HashMap<Model, PopupMenu> getModelToPopupMap();

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
    {
    	PopupMenu popup = getModelToPopupMap().get(modelManager.getModel(pickedProp));
    	if (popup != null)
    		popup.showPopup(e, pickedProp, pickedCellId, pickedPosition);
    }
}
