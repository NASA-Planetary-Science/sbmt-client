package edu.jhuapl.near.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.popupmenus.*;
import edu.jhuapl.near.util.*;
import vtk.*;

/**
 * This is the picker normally in use by default.
 * @author eli
 *
 */
public class DefaultPicker extends Picker
{
    private vtkRenderWindowPanel renWin;
    //private LineamentPopupMenu lineamentPopupMenu;
    //private MSIPopupMenu msiImagesPopupMenu;
    //private MSIPopupMenu msiBoundariesPopupMenu;
    //private NISPopupMenu nisSpectraPopupMenu;
    //private StructuresPopupMenu structuresPopupMenu;
    private StatusBar statusBar;
    private ModelManager modelManager;
    private PopupManager popupManager;
    private vtkCellPicker mouseMovedCellPicker; // includes all props including the small body
    private vtkCellPicker mousePressNonSmallBodyCellPicker; // includes all props EXCEPT the small body
    private vtkCellPicker mousePressSmallBodyCellPicker; // only includes small body prop
    private DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
    private DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");
    private boolean suppressPopups = false;
    
	public DefaultPicker(
			Renderer renderer, 
			StatusBar statusBar,
			ModelManager modelManager,
			PopupManager popupManager)
	{
		this.renWin = renderer.getRenderWindowPanel();
		this.statusBar = statusBar;
		this.modelManager = modelManager;
		this.popupManager = popupManager;

		modelManager.addPropertyChangeListener(this);
		
		SmallBodyModel smallBodyModel = modelManager.getSmallBodyModel();
		mouseMovedCellPicker = new vtkCellPicker();
		mouseMovedCellPicker.SetTolerance(0.002);
		mouseMovedCellPicker.AddLocator(smallBodyModel.getLocator());
		
		// See comment in the propertyChange function below as to why
		// we use a custom pick list for these pickers.
		mousePressNonSmallBodyCellPicker = new vtkCellPicker();
		mousePressNonSmallBodyCellPicker.SetTolerance(0.002);
		mousePressNonSmallBodyCellPicker.PickFromListOn();
		mousePressNonSmallBodyCellPicker.InitializePickList();

		mousePressSmallBodyCellPicker = new vtkCellPicker();
		mousePressSmallBodyCellPicker.SetTolerance(0.002);
		mousePressSmallBodyCellPicker.PickFromListOn();
		mousePressSmallBodyCellPicker.InitializePickList();
		mousePressSmallBodyCellPicker.AddLocator(smallBodyModel.getLocator());
	}

	public void setSuppressPopups(boolean b)
	{
		this.suppressPopups = b;
	}
	
	public void mousePressed(MouseEvent e) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;

		// First try picking on the non-small-body picker. If that fails try the small body picker.
		int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker);
		
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model != null)
			{
				int cellId = mousePressNonSmallBodyCellPicker.GetCellId();
				String text = model.getClickStatusBarText(pickedActor, cellId);
				statusBar.setLeftText(text);
				pcs.firePropertyChange(
						Properties.MODEL_PICKED,
						null,
						new PickEvent(e, pickedActor, cellId, mousePressNonSmallBodyCellPicker.GetPickPosition()));
			}
		}		
		else
		{
			// If the non-small-body picker failed, see if the user clicked on the small body itself.
			renWin.lock();
			pickSucceeded = mousePressSmallBodyCellPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();

			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = mousePressSmallBodyCellPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model != null)
				{
					int cellId = mousePressSmallBodyCellPicker.GetCellId();
					String text = model.getClickStatusBarText(pickedActor, cellId);
					statusBar.setLeftText(text);
					pcs.firePropertyChange(
							Properties.MODEL_PICKED,
							null,
							new PickEvent(e, pickedActor, cellId, mousePressSmallBodyCellPicker.GetPickPosition()));
				}
			}		
			else
			{
				statusBar.setLeftText(" ");
			}
		}

		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		showPositionInfoInStatusBar(e);
	}

	public void mouseDragged(MouseEvent e) 
	{
		showPositionInfoInStatusBar(e);
	}

	public void mouseMoved(MouseEvent e) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    		return;

		showPositionInfoInStatusBar(e);
	}

	private void maybeShowPopup(MouseEvent e) 
	{
		if (suppressPopups)
			return;
		
        if (e.isPopupTrigger()) 
        {
        	if (renWin.GetRenderWindow().GetNeverRendered() > 0)
        		return;
    		
            int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker);
    		if (pickSucceeded == 1)
    		{
    			vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
    			popupManager.showPopup(
    					e,
    					pickedActor,
    					mousePressNonSmallBodyCellPicker.GetCellId(),
    					mousePressNonSmallBodyCellPicker.GetPickPosition());
    		}		
        }
    }

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName().equals(Properties.MODEL_CHANGED))
		{
			// Whenever the model actors change, we need to update the pickers 
			// internal list of all actors to pick from. The small body actor is excluded
			// from this list since many other actors occupy the same position
			// as parts of the small body and we want the picker to pick these other
			// actors rather than the small body. Note that this exclusion only applies 
			// to the following picker.
			ArrayList<vtkProp> actors = modelManager.getPropsExceptSmallBody();
			mousePressNonSmallBodyCellPicker.GetPickList().RemoveAllItems();
			for (vtkProp act : actors)
			{
				mousePressNonSmallBodyCellPicker.AddPickList(act);
			}

			// Note that this picker includes only the small body prop so that if the
			// the previous picker, we then invoke this picker on small body itself.
			actors = modelManager.getSmallBodyModel().getProps();
			mousePressSmallBodyCellPicker.GetPickList().RemoveAllItems();
			for (vtkProp act : actors)
			{
				mousePressSmallBodyCellPicker.AddPickList(act);
			}
		}
	}
	
	private void showPositionInfoInStatusBar(MouseEvent e)
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    		return;
		
		// Don't respond if the event is more than a third of a second old
		if (System.currentTimeMillis() - e.getWhen() > 333)
			return;

		double[] cameraPos = renWin.GetRenderer().GetActiveCamera().GetPosition();
		double distance = Math.sqrt(
				cameraPos[0]*cameraPos[0] +
				cameraPos[1]*cameraPos[1] +
				cameraPos[2]*cameraPos[2]);
        String distanceStr = decimalFormatter.format(distance);
        if (distanceStr.length() == 5)
        	distanceStr = "  " + distanceStr;
        else if (distanceStr.length() == 6)
        	distanceStr = " " + distanceStr;
        distanceStr += " km";

		renWin.lock();
        int pickSucceeded = mouseMovedCellPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
		renWin.unlock();
		if (pickSucceeded == 1)
		{
			double[] pos = mouseMovedCellPicker.GetPickPosition();
			LatLon llr = MathUtil.reclat(pos);

			// Note \u00B0 is the unicode degree symbol
			
			//double sign = 1.0;
			double lat = llr.lat*180/Math.PI;
			//if (lat < 0.0)
			//	sign = -1.0;
	        String latStr = decimalFormatter.format(lat);
	        if (latStr.length() == 5)
	        	latStr = "  " + latStr;
	        else if (latStr.length() == 6)
	        	latStr = " " + latStr;
	        //if (lat >= 0.0)
	        //	latStr += "\u00B0N";
	        //else
	        //	latStr += "\u00B0S";
	        latStr += "\u00B0";
	        
	        // Note that the convention seems to be that longitude
	        // is never negative and is shown as E. longitude.
	        double lon = llr.lon*180/Math.PI;
			if (lon < 0.0)
				lon += 360.0;
	        String lonStr = decimalFormatter.format(lon);
	        if (lonStr.length() == 5)
	        	lonStr = "  " + lonStr;
	        else if (lonStr.length() == 6)
	        	lonStr = " " + lonStr;
	        //if (lon >= 0.0)
	        //	lonStr += "\u00B0E";
	        //else
	        //	lonStr += "\u00B0W";
	        lonStr += "\u00B0";

	        double rad = llr.rad;
	        String radStr = decimalFormatter2.format(rad);
	        if (radStr.length() == 5)
	        	radStr = " " + radStr;
	        radStr += " km";

	        statusBar.setRightText("Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  Distance: " + distanceStr + " ");
		}
		else
		{
			statusBar.setRightText("Distance: " + distanceStr + " ");
		}
	}
	
	private int doPick(MouseEvent e, vtkCellPicker picker)
	{
        // When picking, choosing the right tolerance is not simple. If it's too small, then
        // the pick will only work well if we are zoomed in very close to the object. If it's
        // too large, the pick will only work well when we are zoomed out a lot. To deal
        // with this situation, do a series of picks starting out with a low tolerance
        // and increase the tolerance after each new pick. Stop as soon as the pick succeeds
        // or we reach the maximum tolerance.
        int pickSucceeded = 0;
        double tolerance = 0.0002;
        final double originalTolerance = picker.GetTolerance();
        final double maxTolerance = 0.002;
        final double incr = 0.0002;
        renWin.lock();
        picker.SetTolerance(tolerance);
        while (tolerance <= maxTolerance)
        {
            picker.SetTolerance(tolerance);
            pickSucceeded = picker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
            
            if (pickSucceeded == 1)
                break;

            tolerance += incr;
        }
        picker.SetTolerance(originalTolerance);
        renWin.unlock();
        
        return pickSucceeded;
	}
}
