package edu.jhuapl.sbmt.spectrum.rendering;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.spectrum.model.EnabledState;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumBoundary;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;

/**
 * Collection that holds the spectrum boundary objects
 * @author steelrj1
 *
 * @param <S>
 */
public class SpectrumBoundaryCollection<S extends BasicSpectrum> extends AbstractModel implements PropertyChangeListener
{
    private HashMap<SpectrumBoundary<S>, List<vtkProp>> boundaryToActorsMap = new HashMap<SpectrumBoundary<S>, List<vtkProp>>();
    private HashMap<vtkProp, SpectrumBoundary<S>> actorToBoundaryMap = new HashMap<vtkProp, SpectrumBoundary<S>>();
    private HashMap<BasicSpectrum, SpectrumBoundary<S>> spectrumToBoundaryMap = new HashMap<BasicSpectrum, SpectrumBoundary<S>>();
    private SmallBodyModel smallBodyModel;
    // Create a buffer of initial boundary colors to use. We cycle through these colors when creating new boundaries
    private Color[] initialColors = {Color.RED, Color.PINK.darker(), Color.ORANGE.darker(),
            Color.GREEN.darker(), Color.MAGENTA, Color.CYAN.darker(), Color.BLUE,
            Color.GRAY, Color.DARK_GRAY, Color.BLACK};
    private int initialColorIndex = 0;
    private SpectraCollection<S> spectrumCollection;
//    HashMap<String, List<SpectrumBoundary<S>>> collections = new HashMap<String, List<SpectrumBoundary<S>>>();

    /**
     * @param smallBodyModel
     * @param spectrumCollection
     */
    public SpectrumBoundaryCollection(SmallBodyModel smallBodyModel, SpectraCollection<S> spectrumCollection)
    {
        this.smallBodyModel = smallBodyModel;
        this.spectrumToBoundaryMap = new HashMap<BasicSpectrum, SpectrumBoundary<S>>();
        this.spectrumCollection = spectrumCollection;
    }

    /**
     * Creates a bondary for the given spectrum and small body model
     * @param spec
     * @param smallBodyModel
     * @return
     */
    protected SpectrumBoundary<S> createBoundary(
            BasicSpectrum spec,
            SmallBodyModel smallBodyModel)
    {
        IBasicSpectrumRenderer<S> spectrum = spectrumCollection.getRendererForSpectrum(spec);
        SpectrumBoundary<S> boundary = new SpectrumBoundary<S>(spectrum, smallBodyModel);
        boundary.setBoundaryColor(initialColors[initialColorIndex++]);
        if (initialColorIndex >= initialColors.length)
            initialColorIndex = 0;
        spectrumToBoundaryMap.put(spec, boundary);
        return boundary;
    }

    /**
     * Adds a boundary for the given spectrum tot he collection, and sets up what's required to render it on screen
     * @param spec
     * @return
     */
    public SpectrumBoundary<S> addBoundary(BasicSpectrum spec)
    {
    	SpectrumBoundary<S> boundary = null;
        if (spectrumToBoundaryMap.get(spec) != null)
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

    /**
     * Removes the boundary for the given spectrum from the collection, taking it out of the renderer
     * @param spectrum
     */
    public void removeBoundary(BasicSpectrum spectrum)
    {
    	if (!spectrumCollection.getActiveInstrument().getDisplayName().equals(spectrum.getInstrument().getDisplayName())) return;
        SpectrumBoundary<S> boundary = spectrumToBoundaryMap.get(spectrum);
        if (boundary == null) return;

        List<vtkProp> actors = boundaryToActorsMap.get(boundary);
        if(actors != null)
        {
            for (vtkProp act : actors)
                actorToBoundaryMap.remove(act);
        }

        spectrumToBoundaryMap.put(spectrum, null);
        boundaryToActorsMap.remove(boundary);
        boundary.setVisibility(false);
        boundary.removePropertyChangeListener(this);
        smallBodyModel.removePropertyChangeListener(boundary);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, boundary);
    }

    /**
     * Removes all of the boundaries
     */
    public void removeAllBoundaries()
    {
        for (BasicSpectrum spectrum : spectrumToBoundaryMap.keySet())
            removeBoundary(spectrum);
    }

    /**
     *	Gets the vtkProps for rendering on screen
     */
    public List<vtkProp> getProps()
    {
        return new ArrayList<vtkProp>(actorToBoundaryMap.keySet());
    }

    /**
     *	Returns the information for a given boundary so it can be rendered on the status bar
     */
    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        SpectrumBoundary<S> boundary = actorToBoundaryMap.get(prop);
        if(boundary == null) return "";
        File file = new File(boundary.getSpectrum().getSpectrumName());
        return "Boundary of image " + file.getName();
    }

    /**
     * Returns the boundary name for the <pre>actor</pre>
     * @param actor
     * @return
     */
    public String getBoundaryName(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor).getSpectrum().getSpectrumName();
    }

    /**
     * Returns the boundary for the <pre>actor</pre>
     * @param actor
     * @return
     */
    public SpectrumBoundary<S> getBoundary(vtkActor actor)
    {
        return actorToBoundaryMap.get(actor);
    }

    /**
     * Returns the boundary for the given <pre>spectrum</pre>
     * @param spectrum
     * @return
     */
    public SpectrumBoundary<S> getBoundary(BasicSpectrum spectrum)
    {
        return spectrumToBoundaryMap.get(spectrum);
    }

    /**
     * Checks to see if the collection contains a boundary for the given <pre>spectrum</pre>
     * @param spectrum
     * @return
     */
    public boolean containsBoundary(BasicSpectrum spectrum)
    {
    	return spectrumToBoundaryMap.containsKey(spectrum);
    }

    /**
     * Sets the visibility for the given <pre>spec</pre> to <pre>visible</pre>
     * @param spec
     * @param visible
     */
    public void setVisibility(BasicSpectrum spec, boolean visible)
    {
    	SpectrumBoundary<S> boundary = spectrumToBoundaryMap.get(spec);
    	if (boundary == null)
    		boundary = addBoundary(spec);
    	boundary.setVisibility(visible);
    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, boundary);
    }

    /**
     * Returns the visiblity for the given <pre>spec</pre>
     * @param spec
     * @return
     */
    public boolean getVisibility(BasicSpectrum spec)
    {
    	if (!spectrumCollection.getActiveInstrument().getDisplayName().equals(spec.getInstrument().getDisplayName())) return false;
    	if (spectrumToBoundaryMap.get(spec) == null) return false;
    	return spectrumToBoundaryMap.get(spec).getVisibility();
    }

    public EnabledState getBoundaryVisbility(ImmutableSet<S> spectra)
    {
    	int numEnabled = 0;
    	int numDisabled = 0;
    	for (BasicSpectrum spec : spectra)
    	{
    		if (getVisibility(spec) == false) numDisabled++; else numEnabled++;
    	}
    	if (spectra.size() == numEnabled) return EnabledState.ALL;
    	else if (spectra.size() == numDisabled) return EnabledState.NONE;
    	else return EnabledState.PARTIAL;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }
}
