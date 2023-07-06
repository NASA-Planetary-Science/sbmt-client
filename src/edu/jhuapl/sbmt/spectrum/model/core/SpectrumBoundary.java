package edu.jhuapl.sbmt.spectrum.model.core;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;

/**
 * Model that represents the Spectrum boundaries.
 * @author steelrj1
 *
 */
public class SpectrumBoundary<S extends BasicSpectrum> extends AbstractModel implements PropertyChangeListener
{
    private vtkActor actor;
    private IBasicSpectrumRenderer<S> spectrumRenderer;

    public SpectrumBoundary(IBasicSpectrumRenderer<S> spectrum, SmallBodyModel smallBodyModel) //throws IOException
    {
        this.spectrumRenderer = spectrum;
        this.actor = spectrum.getOutlineActor();
    }

    public void firePropertyChange()
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public List<vtkProp> getProps()
    {
        List<vtkProp> props = new ArrayList<vtkProp>();
        props.add(actor);
        return props;
    }

    /**
     * Returns the spectrum associated with this
     * @return
     */
    public Spectrum getSpectrum()
    {
        return spectrumRenderer.getSpectrum();
    }

    /**
     * Sets whether the boundary is visible or not
     * @param visible
     */
    public void setVisibility(boolean visible)
    {
        if (visible == true)  { actor.VisibilityOn(); } else actor.VisibilityOff();
    }

    /**
     * Returns boolean describing if the boundary is visible
     * @return
     */
    public boolean getVisibility()
    {
    	return (actor.GetVisibility() == 0) ? false : true;
    }

    /**
     * Sets the boundary color
     * @param color
     */
    public void setBoundaryColor(Color color)
    {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        actor.VisibilityOn();
        actor.GetProperty().SetEdgeColor(r/255.0, g/255.0, b/255.0);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    /**
     * Gets the boundary color
     * @return
     */
    public int[] getBoundaryColor()
    {
        double[] c = new double[3];
        c = actor.GetProperty().GetEdgeColor();
        return new int[] {(int) (c[0]*255.0), (int) (c[1]*255.0), (int) (c[2]*255.0)};
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
//      The following code seems broken and causes performance problems and issues with the colors
//      of the image list
//        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
//        {
////            System.out.println("Boundary MODEL_CHANGED event: " + evt.getSource().getClass().getSimpleName());
//            initialize();
//            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
//        }
    }
}