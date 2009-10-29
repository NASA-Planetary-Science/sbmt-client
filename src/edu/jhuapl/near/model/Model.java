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
}
