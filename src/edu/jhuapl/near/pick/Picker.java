package edu.jhuapl.near.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import vtk.vtkCellPicker;
import vtk.vtkRenderWindowPanel;

/**
 * A picker is a class that listens on mouse events on the renderer and
 * responds appropriately. There can be more than 1 picker active at any
 * given time. The PickManager class (also a subclass of this) is responsible
 * for initializing and managing all the pickers.
 * 
 * @author eli
 *
 */
public abstract class Picker implements 
	MouseListener, 
	MouseMotionListener,
	MouseWheelListener,
	PropertyChangeListener
{
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    public void addPropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.addPropertyChangeListener( listener ); }
    public void removePropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.removePropertyChangeListener( listener ); }

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
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
	}

	protected int doPick(MouseEvent e, vtkCellPicker picker, vtkRenderWindowPanel renWin)
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
