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
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;
import edu.jhuapl.sbmt.spectrum.rendering.IBasicSpectrumRenderer;

public class SpectrumBoundary extends AbstractModel implements PropertyChangeListener
{
    private vtkActor actor;
    private IBasicSpectrumRenderer spectrumRenderer;

    public SpectrumBoundary(IBasicSpectrumRenderer spectrum, SmallBodyModel smallBodyModel) //throws IOException
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

    public SpectrumKeyInterface getKey()
    {
        return spectrumRenderer.getSpectrum().getKey();
    }

    public Spectrum getSpectrum()
    {
        return spectrumRenderer.getSpectrum();
    }

    public void setVisibility(boolean visible)
    {
        if (visible == true)  { actor.VisibilityOn(); } else actor.VisibilityOff();
    }

    public boolean getVisibility()
    {
    	return (actor.GetVisibility() == 0) ? false : true;
    }

    public void setBoundaryColor(Color color)
    {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        actor.VisibilityOn();
        actor.GetProperty().SetEdgeColor(r/255.0, g/255.0, b/255.0);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

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