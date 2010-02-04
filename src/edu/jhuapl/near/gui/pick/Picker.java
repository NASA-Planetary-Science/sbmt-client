package edu.jhuapl.near.gui.pick;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
}
