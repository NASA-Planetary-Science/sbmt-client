package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.jhuapl.near.util.Properties;
import vtk.*;

public class PickManager implements PropertyChangeListener, 
			MouseListener, 
			MouseMotionListener 
{

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

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	private void maybeShowPopup(MouseEvent e) 
	{
        if (e.isPopupTrigger()) 
        {
//    		if (renWin.GetRenderWindow().GetNeverRendered() > 0)
//    			return;
    		
    		/*
    		LineamentModel.Lineament lin = pickLineament(e);
    		
    		if (lin != null)
            	popupMenu.show(e.getComponent(), e.getX(), e.getY(), lin);
            */
        }
    }


}
