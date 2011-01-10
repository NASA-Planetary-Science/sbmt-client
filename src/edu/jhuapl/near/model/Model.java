package edu.jhuapl.near.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import vtk.vtkProp;

import edu.jhuapl.near.util.Properties;

public abstract class Model
{
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    public void addPropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.addPropertyChangeListener( listener ); }
    public void removePropertyChangeListener( PropertyChangeListener listener )
    { this.pcs.removePropertyChangeListener( listener ); }

    private boolean visible = true;
    private String name = null;

    public Model()
    {
    }

    public Model(String name)
    {
        this.name = name;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean b)
    {
        if (this.visible != b)
        {
            this.visible = b;
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public abstract ArrayList<vtkProp> getProps();

    /**
     * Return what text should be displayed if the user clicks on one of the
     * props of this model and the specified cellId and point. By default an empty string
     * is returned. Subclasses may override this behavior.
     * @param prop
     * @param cellId
     * @param pickPosition
     * @return
     */
    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        return "";
    }

    /**
     * Some models have vertex values that overlap the SmallBodyModel and are thus obscured.
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

    public void setPolygonOffset(double offset)
    {

    }

    public double getPolygonOffset()
    {
        return 0.0;
    }

    public String getName()
    {
        return name;
    }

    public void delete()
    {

    }
}
