package edu.jhuapl.sbmt.spectrum.rendering;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumBoundary;

public class SpectrumBoundaryCollection<S extends BasicSpectrum> extends AbstractModel implements PropertyChangeListener
{
    private HashMap<SpectrumBoundary, List<vtkProp>> boundaryToActorsMap = new HashMap<SpectrumBoundary, List<vtkProp>>();
    private HashMap<vtkProp, SpectrumBoundary> actorToBoundaryMap = new HashMap<vtkProp, SpectrumBoundary>();
    private HashMap<BasicSpectrum, SpectrumBoundary> spectrumToBoundaryMap = new HashMap<BasicSpectrum, SpectrumBoundary>();
    private SmallBodyModel smallBodyModel;
    // Create a buffer of initial boundary colors to use. We cycle through these colors when creating new boundaries
    private Color[] initialColors = {Color.RED, Color.PINK.darker(), Color.ORANGE.darker(),
            Color.GREEN.darker(), Color.MAGENTA, Color.CYAN.darker(), Color.BLUE,
            Color.GRAY, Color.DARK_GRAY, Color.BLACK};
    private int initialColorIndex = 0;
    private SpectraCollection<S> spectrumCollection;

    public SpectrumBoundaryCollection(SmallBodyModel smallBodyModel, SpectraCollection<S> spectrumCollection)
    {
        this.smallBodyModel = smallBodyModel;
        this.spectrumToBoundaryMap = new HashMap<BasicSpectrum, SpectrumBoundary>();
        this.spectrumCollection = spectrumCollection;
    }

    protected SpectrumBoundary createBoundary(
            BasicSpectrum spec,
            SmallBodyModel smallBodyModel) //throws IOException, FitsException
    {
        IBasicSpectrumRenderer<S> spectrum = spectrumCollection.getRendererForSpectrum(spec);
        System.out.println("SpectrumBoundaryCollection: createBoundary: spectrum " + spectrum);
        SpectrumBoundary boundary = new SpectrumBoundary(spectrum, smallBodyModel);
        boundary.setBoundaryColor(initialColors[initialColorIndex++]);
        if (initialColorIndex >= initialColors.length)
            initialColorIndex = 0;
        spectrumToBoundaryMap.put(spec, boundary);
        return boundary;
    }

    public SpectrumBoundary addBoundary(BasicSpectrum spec) //throws FitsException, IOException
    {
    	SpectrumBoundary boundary = null;
        if (spectrumToBoundaryMap.containsKey(spec))
            boundary = spectrumToBoundaryMap.get(spec);
        else
        	boundary = createBoundary(spec, smallBodyModel);

        smallBodyModel.addPropertyChangeListener(boundary);
        boundary.addPropertyChangeListener(this);
        boundary.setVisibility(true);
        boundaryToActorsMap.put(boundary, new ArrayList<vtkProp>());

        List<vtkProp> boundaryPieces = boundary.getProps();
        boundaryToActorsMap.get(boundary).addAll(boundaryPieces);

        for (vtkProp act : boundaryPieces)
            actorToBoundaryMap.put(act, boundary);
        spectrumToBoundaryMap.put(spec, boundary);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, boundary);
        return boundary;
    }

    public void removeBoundary(BasicSpectrum spectrum)
    {
        SpectrumBoundary boundary = spectrumToBoundaryMap.get(spectrum);

        if(boundary != null)
        {
            List<vtkProp> actors = boundaryToActorsMap.get(boundary);

            if(actors != null)
            {
                for (vtkProp act : actors)
                    actorToBoundaryMap.remove(act);
            }

            boundaryToActorsMap.remove(boundary);
            boundary.setVisibility(false);
            boundary.removePropertyChangeListener(this);
            smallBodyModel.removePropertyChangeListener(boundary);

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, boundary);
        }
    }

    public void removeAllBoundaries()
    {
        for (BasicSpectrum spectrum : spectrumToBoundaryMap.keySet())
            removeBoundary(spectrum);
    }

    public List<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToBoundaryMap.keySet());
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        SpectrumBoundary boundary = actorToBoundaryMap.get(prop);
        if(boundary == null)
        {
            return "";
        }
        File file = new File(boundary.getSpectrum().getSpectrumName());
        return "Boundary of image " + file.getName();
    }

    public String getBoundaryName(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor).getSpectrum().getSpectrumName();
    }

    public SpectrumBoundary getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    public SpectrumBoundary getBoundary(BasicSpectrum spectrum)
    {
        return spectrumToBoundaryMap.get(spectrum);
    }

    public boolean containsBoundary(BasicSpectrum spectrum)
    {
    	return spectrumToBoundaryMap.containsKey(spectrum);
    }

    public void setVisibility(BasicSpectrum spec, boolean visible)
    {
    	SpectrumBoundary boundary = spectrumToBoundaryMap.get(spec);
    	if (boundary == null)
    		boundary = addBoundary(spec);

    	boundary.setVisibility(visible);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, boundary);
    }

    public boolean getVisibility(BasicSpectrum spec)
    {
    	if (spectrumToBoundaryMap.get(spec) == null) return false;
    	return spectrumToBoundaryMap.get(spec).getVisibility();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }
}
