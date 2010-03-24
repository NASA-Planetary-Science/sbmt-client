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
    private vtkCellPicker mouseMovedCellPicker; // includes all props including Eros
    private vtkCellPicker mousePressNonErosCellPicker; // includes all props EXCEPT Eros
    private vtkCellPicker mousePressErosCellPicker; // only includes Eros prop
    private DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
    private DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");
    private boolean suppressPopups = false;
    
	public DefaultPicker(
			ErosRenderer erosRenderer, 
			StatusBar statusBar,
			ModelManager modelManager,
			ModelInfoWindowManager infoPanelManager,
			PopupManager popupManager)
	{
		this.renWin = erosRenderer.getRenderWindowPanel();
		this.statusBar = statusBar;
		this.modelManager = modelManager;
		this.popupManager = popupManager;

		modelManager.addPropertyChangeListener(this);
		
		mouseMovedCellPicker = new vtkCellPicker();
		mouseMovedCellPicker.SetTolerance(0.002);

		// See comment in the propertyChange function below as to why
		// we use a custom pick list for these pickers.
		mousePressNonErosCellPicker = new vtkCellPicker();
		mousePressNonErosCellPicker.SetTolerance(0.002);
		mousePressNonErosCellPicker.PickFromListOn();
		mousePressNonErosCellPicker.InitializePickList();

		mousePressErosCellPicker = new vtkCellPicker();
		mousePressErosCellPicker.SetTolerance(0.002);
		mousePressErosCellPicker.PickFromListOn();
		mousePressErosCellPicker.InitializePickList();
		/*
		lineamentPopupMenu = 
			new LineamentPopupMenu((LineamentModel)modelManager.getModel(ModelManager.LINEAMENT));
		
		msiBoundariesPopupMenu= 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		
		msiImagesPopupMenu = 
			new MSIPopupMenu(modelManager, infoPanelManager, renWin, renWin);
		
		nisSpectraPopupMenu = 
			new NISPopupMenu(modelManager, infoPanelManager, renWin);
		
		structuresPopupMenu =
			new StructuresPopupMenu();
			*/
	}

	public void setSuppressPopups(boolean b)
	{
		this.suppressPopups = b;
	}
	
	public void mousePressed(MouseEvent e) 
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
			return;

		// First try picking on the non-eros picker. If that fails try the eros picker.
		renWin.lock();
		int pickSucceeded = mousePressNonErosCellPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
		renWin.unlock();
		
		if (pickSucceeded == 1)
		{
			vtkActor pickedActor = mousePressNonErosCellPicker.GetActor();
			Model model = modelManager.getModel(pickedActor);

			if (model != null)
			{
				String text = model.getClickStatusBarText(pickedActor, mousePressNonErosCellPicker.GetCellId());
				statusBar.setLeftText(text);
			}
		}		
		else
		{
			// If the non-eros picker failed, see if the user clicked on eros itself.
			renWin.lock();
			pickSucceeded = mousePressErosCellPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
			renWin.unlock();

			if (pickSucceeded == 1)
			{
				vtkActor pickedActor = mousePressErosCellPicker.GetActor();
				Model model = modelManager.getModel(pickedActor);

				if (model != null)
				{
					String text = model.getClickStatusBarText(pickedActor, mousePressErosCellPicker.GetCellId());
					statusBar.setLeftText(text);
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
    		
    		renWin.lock();
    		int pickSucceeded = mousePressNonErosCellPicker.Pick(e.getX(), renWin.getHeight()-e.getY()-1, 0.0, renWin.GetRenderer());
    		renWin.unlock();
    		if (pickSucceeded == 1)
    		{
    			vtkActor pickedActor = mousePressNonErosCellPicker.GetActor();
    			popupManager.showPopup(
    					e,
    					pickedActor,
    					mousePressNonErosCellPicker.GetCellId(),
    					mousePressNonErosCellPicker.GetPickPosition());
    					
    			
    			/*
    			if (modelManager.getModel(pickedActor) instanceof LineamentModel)
    			{
        			LineamentModel linModel = (LineamentModel)modelManager.getModel(ModelManager.LINEAMENT);
    				LineamentModel.Lineament lin = linModel.getLineament(mousePressNonErosCellPicker.GetCellId());
    	    		if (lin != null)
    	    			lineamentPopupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
    			}
    			else if (modelManager.getModel(pickedActor) instanceof MSIBoundaryCollection)
    			{
        			MSIBoundaryCollection msiBoundaries = (MSIBoundaryCollection)modelManager.getModel(ModelManager.MSI_BOUNDARY);
        			String name = msiBoundaries.getBoundaryName(pickedActor);
        			msiBoundariesPopupMenu.setCurrentImage(name);
        			msiBoundariesPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    			}
    			else if (modelManager.getModel(pickedActor) instanceof MSIImageCollection)
    			{
        			MSIImageCollection msiImages = (MSIImageCollection)modelManager.getModel(ModelManager.MSI_IMAGES);
        			String name = msiImages.getImageName(pickedActor);
        			msiImagesPopupMenu.setCurrentImage(name);
        			msiImagesPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    			}
    			else if (modelManager.getModel(pickedActor) instanceof NISSpectraCollection)
    			{
    				NISSpectraCollection msiImages = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);
        			String name = msiImages.getSpectrumName(pickedActor);
        			nisSpectraPopupMenu.setCurrentSpectrum(name);
        			nisSpectraPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    			}
    			else if (modelManager.getModel(pickedActor) instanceof StructureModel)
    			{
    				//NISSpectraCollection msiImages = (NISSpectraCollection)modelManager.getModel(ModelManager.NIS_SPECTRA);
        			//String name = msiImages.getSpectrumName(pickedActor);
        			//nisSpectraPopupMenu.setCurrentSpectrum(name);
    				structuresPopupMenu.setModel((StructureModel)modelManager.getModel(pickedActor),
    						mousePressNonErosCellPicker.GetCellId());
        			structuresPopupMenu.show(e.getComponent(), e.getX(), e.getY());
    			}
    			*/
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
			// to the following picker.
			ArrayList<vtkProp> actors = modelManager.getPropsExceptEros();
			mousePressNonErosCellPicker.GetPickList().RemoveAllItems();
			for (vtkProp act : actors)
			{
				mousePressNonErosCellPicker.AddPickList(act);
			}

			// Note that this picker includes only the eros prop so that if the
			// the previous picker, we then invoke this picker on eros itself.
			actors = modelManager.getModel(ModelManager.EROS).getProps();
			mousePressErosCellPicker.GetPickList().RemoveAllItems();
			for (vtkProp act : actors)
			{
				mousePressErosCellPicker.AddPickList(act);
			}
		}
	}
	
	private void showPositionInfoInStatusBar(MouseEvent e)
	{
		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
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
			LatLon llr = Spice.reclat(pos);

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
}
