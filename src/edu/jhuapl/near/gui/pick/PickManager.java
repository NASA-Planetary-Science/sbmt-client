package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
    private vtkPropCollection erosProp;
    
	public PickManager(
			vtkRenderWindowPanel renWin, 
			StatusBar statusBar,
			ModelManager modelManager)
	{
		this.renWin = renWin;
		this.statusBar = statusBar;
		this.modelManager = modelManager;
		
		erosProp = new vtkPropCollection();
		ErosModel eros = (ErosModel)modelManager.getModel(ModelManager.EROS);
		vtkActor actor = eros.getActors().get(0);
		erosProp.AddItem(actor);
	}

	/*
	public void propertyChange(PropertyChangeEvent evt) 
	{
		System.out.println("11XXXXXXXXXx");

		if (evt.getPropertyName().equals(Properties.PICK_OCCURED))
		{
			System.out.println("22XXXXXXXXXx");
			PickEvent pickEvent = (PickEvent)evt.getNewValue();
			vtkPoints pts = pickEvent.getCellPicker().GetPickedPositions();
			System.out.println(pts.GetNumberOfPoints());
			
			//for (int i=0;i<cellPicker.GetActors().GetNumberOfItems();++i)
			//	System.out.println("--"+(cellPicker.GetActors().GetItemAsObject(i)==this.erosActor));

		}
	}
	 */
	
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
	}

	public void mouseMoved(MouseEvent e) 
	{
		vtkPropPicker propPicker = new vtkPropPicker();
		//propPicker.InitializePickList();
		
		// get the eros actor
		
		
		//propPicker.AddPickList(actor);
		
		int pickSucceeded = propPicker.Pick(e.getX(), renWin.getIren().GetSize()[1]-e.getY()-1, 0.0, renWin.GetRenderer());
		if (pickSucceeded == 1)
		{
			double[] pos = propPicker.GetPickPosition();
			System.out.println(pos);
			LatLon ll = LatLon.recToLatLon(pos);
			statusBar.setRightText(ll.lat*180/Math.PI + " " + ll.lon*180/Math.PI);
		}
		else
		{
			statusBar.setRightText(" ");
			System.out.println("nothing picked");
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
