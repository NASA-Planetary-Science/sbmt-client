package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.gui.popupmenus.LineamentPopupMenu;
import edu.jhuapl.near.gui.popupmenus.MSIPopupMenu;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.*;
import vtk.*;

public class PickManager implements 
			MouseListener, 
			MouseMotionListener,
			PropertyChangeListener
{
    private vtkRenderWindowPanel renWin;
    private LineamentPopupMenu lineamentPopupMenu;
    private MSIPopupMenu msiImagesPopupMenu;
    private MSIPopupMenu msiBoundariesPopupMenu;
    private StatusBar statusBar;
    private ModelManager modelManager;
    private vtkCellPicker mouseMovedCellPicker;
    private vtkCellPicker mousePressCellPicker;
    private DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
    private DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");
    
	public PickManager(
			vtkRenderWindowPanel renWin, 
			StatusBar statusBar,
			ModelManager modelManager)
	{
		this.renWin = renWin;
		this.statusBar = statusBar;
		this.modelManager = modelManager;

		modelManager.addPropertyChangeListener(this);
		
		renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);

		mouseMovedCellPicker = new vtkCellPicker();
		mouseMovedCellPicker.SetTolerance(0.002);

		// See comment in the propertyChange function below as to why
		// we use a custom pick list for this picker.
		mousePressCellPicker = new vtkCellPicker();
		mousePressCellPicker.SetTolerance(0.002);
		mousePressCellPicker.PickFromListOn();
		mousePressCellPicker.InitializePickList();
		
		lineamentPopupMenu = 
			new LineamentPopupMenu((LineamentModel)modelManager.getModel(ModelManager.LINEAMENT));
		
		msiBoundariesPopupMenu= 
			new MSIPopupMenu(modelManager, 1, renWin);
		
		msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, 2, renWin);
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mousePressed(MouseEvent e) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    		return;
		
		int pickSucceeded = mousePressCellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = mousePressCellPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);
			String text = model.getClickStatusBarText(pickedActor, mousePressCellPicker.GetCellId());
			statusBar.setLeftText(text);
		}		
		else
		{
			statusBar.setLeftText(" ");
		}

		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) 
	{
		maybeShowPopup(e);
	}

	public void mouseDragged(MouseEvent e) 
	{
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
    		return;
		
		int pickSucceeded = mouseMovedCellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded == 1)
		{
			double[] pos = mouseMovedCellPicker.GetPickPosition();
			LatLon llr = LatLon.recToLatLon(pos);

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
	        // is never negative. Also by convention the calculated 
	        // longitude needs to be subtracted from 360.
	        double lon = llr.lon*180/Math.PI;
			if (lon < 0.0)
				lon += 360.0;
			lon = 360.0 - lon;
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
	        radStr += "km";


			statusBar.setRightText("Lat: " + latStr + "  Lon: " + lonStr + "  Diam: " + radStr + " ");
		}
		else
		{
			statusBar.setRightText(" ");
		}
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
        	if (renWin.GetRenderWindow().GetNeverRendered() > 0)
        		return;
    		
    		int pickSucceeded = mousePressCellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
    		if (pickSucceeded == 1)
    		{
    			vtkActor pickedActor = mousePressCellPicker.GetActor();
    			if (modelManager.getModel(pickedActor) instanceof LineamentModel)
    			{
        			LineamentModel linModel = (LineamentModel)modelManager.getModel(ModelManager.LINEAMENT);
    				LineamentModel.Lineament lin = linModel.getLineament(mousePressCellPicker.GetCellId());
    	    		if (lin != null)
    	    			lineamentPopupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
    			}
    			else if (modelManager.getModel(pickedActor) instanceof MSIBoundaryCollection)
    			{
        			MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
        			String name = msiBoundaries.getBoundaryName(pickedActor);
        			msiBoundariesPopupMenu.show(e.getComponent(), e.getX(), e.getY(), name);
    			}
    			else if (modelManager.getModel(pickedActor) instanceof NearImageCollection)
    			{
        			NearImageCollection msiImages = (NearImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
        			String name = msiImages.getImageName(pickedActor);
        			msiBoundariesPopupMenu.show(e.getComponent(), e.getX(), e.getY(), name);
    			}
    		}		
        }
    }

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName().equals(Properties.MODEL_CHANGED))
		{
			// Whenever the model actors change, we need to update the pickers 
			// internal list of all actors to pick from. The Eros actor is excluded
			// from this list since many other actors occupy the same position
			// as parts of Eros and we want the picker to pick these other
			// actors rather than Eros. Note that this exclusion only applies 
			// the picker used for mouse presses. The picker used for mouse moves
			// however includes all actors, including Eros.
			ArrayList<vtkActor> actors = modelManager.getActorsExceptEros();
			mousePressCellPicker.GetPickList().RemoveAllItems();
			for (vtkActor act : actors)
			{
				mousePressCellPicker.AddPickList(act);
			}
		}
	}
}
