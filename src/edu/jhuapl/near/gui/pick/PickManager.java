package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;

import edu.jhuapl.near.gui.*;
import edu.jhuapl.near.model.*;
import edu.jhuapl.near.util.*;
import vtk.*;

public class PickManager implements 
			MouseListener, 
			MouseMotionListener 
{
    private vtkRenderWindowPanel renWin;
    private LineamentPopupMenu lineamentPopupMenu;
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

		renWin.addMouseListener(this);
        renWin.addMouseMotionListener(this);

		mouseMovedCellPicker = new vtkCellPicker();
		mouseMovedCellPicker.SetTolerance(0.002);
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
    		
    		/*
    		LineamentModel.Lineament lin = pickLineament(e);
    		
    		if (lin != null)
            	popupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
            */
        }
    }

	/*
	private LineamentModel.Lineament pickLineament(MouseEvent e)
	{
		LineamentModel lineamentModel = (LineamentModel)modelManager.getModel(ModelManager.LINEAMENT);
		vtkCellPicker cellPicker = new vtkCellPicker();
		cellPicker.SetTolerance(0.002);
		int pickSucceeded = cellPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded != 0 && cellPicker.GetActor() == this.lineamentActor)
			return lineamentModel.getLineament(cellPicker.GetCellId());
		else
			return null;
	}
*/
}
