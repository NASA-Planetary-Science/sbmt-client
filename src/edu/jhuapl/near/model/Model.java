package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import vtk.vtkActor;

public abstract class Model 
{
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    public void addPropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.addPropertyChangeListener( listener ); }
    public void removePropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.removePropertyChangeListener( listener ); }
    
    public abstract ArrayList<vtkActor> getActors();
    
    /**
     * Return what text should be displayed if the user clicks on one of the
     * actors of this model and the specified cellId. By default an empty string
     * is returned. Subclasses may override this behavior.
     * @param actor
     * @param cellId
     * @return
     */
    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	return "";
    }
    
    /**
     * Some models have vertex values that overlap the Eros Model and are thus obscured. 
     * In these cases it may be helpful to shift the vertices slightly in the radial direction
     * so they are not obscured. This function, which by default does nothing may be used
     * by subclasses for this purpose. 
     * 
     * @param offset
     */
    public void setRadialOffset(double offset)
    {
    	// Do nothing
    }
}
