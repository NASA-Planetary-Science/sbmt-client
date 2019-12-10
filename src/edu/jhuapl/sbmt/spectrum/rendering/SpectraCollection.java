package edu.jhuapl.sbmt.spectrum.rendering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.SpectrumIOException;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.IBasicSpectrumRenderer;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SpectrumCollectionChangedListener;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;

import glum.item.ItemEventType;

public class SpectraCollection<S extends BasicSpectrum> extends SaavtkItemManager<S> implements PropertyChangeListener //, ItemProcessor<BasicSpectrum>, ItemManager<BasicSpectrum>
{
    private HashMap<IBasicSpectrumRenderer<S>, List<vtkProp>> spectraActors = new HashMap<IBasicSpectrumRenderer<S>, List<vtkProp>>();
    private HashMap<BasicSpectrum, IBasicSpectrumRenderer<S>> spectrumToRendererMap = new HashMap<BasicSpectrum, IBasicSpectrumRenderer<S>>();
    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private HashMap<IBasicSpectrumRenderer<S>, List<vtkProp>> spectrumToActorsMap = new HashMap<IBasicSpectrumRenderer<S>, List<vtkProp>>();
    private HashMap<vtkProp, IBasicSpectrumRenderer<S>> actorToSpectrumMap = new HashMap<vtkProp, IBasicSpectrumRenderer<S>>();
    private ISmallBodyModel shapeModel;
    int[] selectedIndices;
    private List<SpectrumCollectionChangedListener<S>> listeners;
    private ISpectralInstrument activeInstrument;
    boolean selectAll=false;
    final double minFootprintSeparation=0.001;
    double footprintSeparation=0.001;
    HashMap<String, List<S>> collections = new HashMap<String, List<S>>();

    Map<IBasicSpectrumRenderer<S>,Integer> ordinals=Maps.newHashMap();
    final static int defaultOrdinal=0;

    public SpectraCollection(ISmallBodyModel smallBody)
    {
        this.shapeModel = smallBody;
        this.listeners = new ArrayList<SpectrumCollectionChangedListener<S>>();
    }

    public void addSpectrumCollectionChangedListener(SpectrumCollectionChangedListener<S> sccl)
    {
    	this.listeners.add(sccl);
    }

    public void fireSpectrumRenderedListeners(IBasicSpectrumRenderer<S> renderer)
    {
    	for (SpectrumCollectionChangedListener<S> sccl : listeners) sccl.spectumRendered(renderer);
    }

    public void reshiftFootprints()
    {
        for (IBasicSpectrumRenderer<S> spectrum : ordinals.keySet())
        {
            spectrum.shiftFootprintToHeight(footprintSeparation*(1+ordinals.get(spectrum)));
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void setOrdinal(IBasicSpectrumRenderer<S> spectrum, int ordinal)
    {
        if (ordinals.containsKey(spectrum))
            ordinals.remove(spectrum);
        ordinals.put(spectrum, ordinal);
    }

    public void clearOrdinals()
    {
        ordinals.clear();
    }

    public void setFootprintSeparation(double val)
    {
        footprintSeparation=val;
        reshiftFootprints();
    }

    public void increaseFootprintSeparation(double val)
    {
        footprintSeparation+=val;
        reshiftFootprints();
    }

    public void decreaseFootprintSeparation(double val)
    {
        footprintSeparation-=val;
        if (footprintSeparation<minFootprintSeparation)
            footprintSeparation=minFootprintSeparation;
        reshiftFootprints();
    }

    public double getFootprintSeparation()
    {
        return footprintSeparation;
    }

    public double getMinFootprintSeparation()
    {
        return minFootprintSeparation;
    }

    public Set<IBasicSpectrumRenderer<S>> getSpectra()
    {
        return spectrumToActorsMap.keySet();
    }

    public IBasicSpectrumRenderer<S> addSpectrum(S spectrum, boolean isCustom) throws SpectrumIOException
    {
        if (spectrumToRendererMap.get(spectrum) != null)
        {
        	IBasicSpectrumRenderer<S> spec = spectrumToRendererMap.get(spectrum);
            select(spec);
            spec.setVisible(true);
            return spec;
        }

        IBasicSpectrumRenderer<S> spectrumRenderer = null;
        try
        {
        	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(spectrum, spectrum.getInstrument());
        	spectrumRenderer.getSpectrum().readSpectrumFromFile();
        }
        catch (SpectrumIOException sioe)
        {
        	throw new SpectrumIOException(sioe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        spectrumRenderer.getSpectrum().isCustomSpectra = isCustom;
        shapeModel.addPropertyChangeListener(spectrumRenderer);
        spectrumRenderer.addPropertyChangeListener(this);

        spectrumToRendererMap.put(spectrum, spectrumRenderer);
        spectraActors.put(spectrumRenderer, new ArrayList<vtkProp>());

        List<vtkProp> props = spectrumRenderer.getProps();

        spectrumToActorsMap.put(spectrumRenderer, new ArrayList<vtkProp>());

        spectrumToActorsMap.get(spectrumRenderer).addAll(props);

        for (vtkProp act : props)
            actorToSpectrumMap.put(act, spectrumRenderer);

        spectraActors.get(spectrumRenderer).addAll(props);


        for (vtkProp act : props)
            actorToFileMap.put(act, spectrum.getServerpath());
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spectrumRenderer);
    	fireSpectrumRenderedListeners(spectrumRenderer);

        return spectrumRenderer;
    }

    public void removeSpectrum(BasicSpectrum spectrum)
    {
    	IBasicSpectrumRenderer<S> spectrumRenderer = spectrumToRendererMap.get(spectrum);
    	if (spectrumRenderer == null) return;
        spectrumRenderer.setUnselected();

        List<vtkProp> actors = spectraActors.get(spectrumRenderer);

        for (vtkProp act : actors)
            actorToFileMap.remove(act);

        spectraActors.remove(spectrumRenderer);

        spectrumToActorsMap.remove(spectrumRenderer);

        spectrumToRendererMap.put(spectrum, null);

        spectrumRenderer.removePropertyChangeListener(this);
        shapeModel.removePropertyChangeListener(spectrumRenderer);
        spectrumRenderer.setShowFrustum(false);

        ordinals.remove(spectrumRenderer);

        for (vtkProp act : actors)
            actorToSpectrumMap.remove(act);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, spectrumRenderer);
    }

    public void removeAllSpectra()
    {
        for (BasicSpectrum spectrum : spectrumToRendererMap.keySet())
            removeSpectrum(spectrum);
    }

    public void removeAllSpectraForInstrument(ISpectralInstrument instrument)
    {
        for (BasicSpectrum spectrum : spectrumToRendererMap.keySet())
        {
            if (spectrum.getInstrument().getClass() == instrument.getClass())
                removeSpectrum(spectrum);
        }
    }

    public boolean isSpectrumMapped(BasicSpectrum spec)
    {
    	if (!activeInstrument.getDisplayName().equals(spec.getInstrument().getDisplayName())) return false;
    	return spectrumToRendererMap.get(spec) != null;
    }

    public void setVisibility(IBasicSpectrumRenderer<S> spectrumRenderer, boolean visibility)
    {
        spectrumRenderer.setVisible(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
    }

    public void setVisibility(BasicSpectrum spec, boolean visibility)
    {
    	IBasicSpectrumRenderer<S> spectrumRenderer = spectrumToRendererMap.get(spec);
        spectrumRenderer.setVisible(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
    }

    public boolean getVisibility(BasicSpectrum spec)
    {
    	if (!activeInstrument.getDisplayName().equals(spec.getInstrument().getDisplayName())) return false;
    	if (isSpectrumMapped(spec) == false ) return false;
    	IBasicSpectrumRenderer<S> spectrumRenderer = spectrumToRendererMap.get(spec);
    	if (spectrumRenderer == null) return false;
        return spectrumRenderer.isVisible();
    }

    public void setFrustumVisibility(BasicSpectrum spec, boolean visibility)
    {
        spectrumToRendererMap.get(spec).setShowFrustum(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spec);
    }

    public boolean getFrustumVisibility(BasicSpectrum spec)
    {
    	if (!activeInstrument.getDisplayName().equals(spec.getInstrument().getDisplayName())) return false;
    	IBasicSpectrumRenderer<S> spectrumRenderer = spectrumToRendererMap.get(spec);
    	if (spectrumRenderer == null) return false;
        return spectrumRenderer.isFrustumShowing();
    }

    public boolean getBoundaryVisibility(BasicSpectrum spec)
    {
    	IBasicSpectrumRenderer<S> spectrumRenderer = spectrumToRendererMap.get(spec);
    	if (spectrumRenderer == null) return false;
        return spectrumRenderer.isOutlineShowing();
    }

    public IBasicSpectrumRenderer<S> getRendererForSpectrum(BasicSpectrum spec)
    {
    	return spectrumToRendererMap.get(spec);
    }

    public void toggleSelect(IBasicSpectrumRenderer<S> spectrumRenderer)
    {
        if (spectrumRenderer.isSelected())
            spectrumRenderer.setUnselected();
        else
            spectrumRenderer.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void select(IBasicSpectrumRenderer<S> spectrumRenderer)
    {
        spectrumRenderer.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void deselect(IBasicSpectrumRenderer<S> spectrumRenderer)
    {
        spectrumRenderer.setUnselected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void toggleSelectAll()
    {
        if (!selectAll) // we're not in "select all" mode so go ahead and select all actors
        {
            for (IBasicSpectrumRenderer<S> spectrumRenderer : spectrumToRendererMap.values())
            {
                spectrumRenderer.setSelected();
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
            }
            selectAll=true;
        }
        else
        {
            for (IBasicSpectrumRenderer<S> spectrumRenderer : spectrumToRendererMap.values())
            {
                spectrumRenderer.setUnselected();
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
            }
            selectAll=false;
        }

    }

    public void deselectAll()
    {
        for (IBasicSpectrumRenderer<S> spectrumRenderer : spectrumToRendererMap.values())
        {
        	if (spectrumRenderer == null) continue;
            spectrumRenderer.setUnselected();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        }
        selectAll=false;

    }

    public List<IBasicSpectrumRenderer<S>> getSelectedSpectra()
    {
        List<IBasicSpectrumRenderer<S>> spectra=Lists.newArrayList();
        for (IBasicSpectrumRenderer<S> s : spectrumToRendererMap.values())
            if ((s != null) && s.isSelected())
                spectra.add(s);
        return spectra;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, evt.getNewValue());
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        String filename = actorToFileMap.get(prop);
        IBasicSpectrumRenderer<S> spectrum = this.spectrumToRendererMap.get(filename);
        if (spectrum==null)
            return "";
        return spectrum.getSpectrum().getInstrument().getDisplayName() + " spectrum " + filename.substring(16, 25) + " acquired at " + spectrum.getSpectrum().getDateTime().toString() /*+ "(SCLK: " + ((OTESSpectrum)spectrum).getTime() + ")"*/;
    }

    public String getSpectrumName(vtkProp actor)
    {
        return actorToFileMap.get(actor);
    }

    public void tagSpectraWithMetadata(S spectrum, SearchSpec spec)
    {
        if (spectrum != null) spectrum.setMetadata(spec);
    }

    public void tagSpectraWithMetadata(List<S> filenames, SearchSpec spec)
    {
        for (S list : filenames)
        {
        	tagSpectraWithMetadata(list, spec);
        }
    }

    public IBasicSpectrumRenderer<S> getSpectrum(vtkActor actor)
    {
        return actorToSpectrumMap.get(actor);
    }

    public String getSpectrumName(vtkActor actor)
    {
        return actorToSpectrumMap.get(actor).getSpectrum().getSpectrumName();
    }

    public boolean containsSpectrum(BasicSpectrum spec)
    {
    	return spectrumToRendererMap.keySet().contains(spec);
    }

    public boolean containsSpectrumName(String name)
    {
        for (IBasicSpectrumRenderer<S> spec : actorToSpectrumMap.values())
        {
            if (spec.getSpectrum().getSpectrumName().equals(name)) return true;
        }
        return false;
    }

    public IBasicSpectrumRenderer<S> getSpectrumForName(String name)
    {
        for (IBasicSpectrumRenderer<S> spec : actorToSpectrumMap.values())
        {
            if (spec.getSpectrum().getSpectrumName().equals(name)) return spec;
        }
        return null;
    }

    @Override
    public List<vtkProp> getProps()
    {
        List<vtkProp> allProps=Lists.newArrayList();
        for (IBasicSpectrumRenderer<S> s : spectraActors.keySet())
            allProps.addAll(spectraActors.get(s));
        return allProps;
    }

    public int getCount()
    {
        return spectrumToRendererMap.size();
    }

	public ISmallBodyModel getShapeModel()
	{
		return shapeModel;
	}

	@Override
	public void removeItems(List<S> specs)
	{
		// Remove relevant state and VTK mappings
		for (BasicSpectrum spec : specs)
		{
			removeSpectrum(spec);
		}

		// Delegate
		super.removeItems(specs);

//		List<LidarTrack> tmpL = ImmutableList.of();
//		updateVtkVars(tmpL);
	}

	public void setAllItems(List<S> specs)
	{
		List<S> instrumentList = new ArrayList<S>();
		for (S spec : specs)
		{
			if (spec.getInstrument().getDisplayName().equals(activeInstrument.getDisplayName()))
				instrumentList.add(spec);
		}
		collections.put(activeInstrument.getDisplayName(), instrumentList);
		super.setAllItems(instrumentList);
		// Send out the appropriate notifications
		notifyListeners(this, ItemEventType.ItemsSelected);
	}

	@Override
	public ImmutableList<S> getAllItems()
	{
		List<S> instrumentList = new ArrayList<S>();
		ImmutableList<S> allItems = super.getAllItems();
		int i=0;
		for (S spec : allItems)
		{
			if (spec.getInstrument().getDisplayName().equals(activeInstrument.getDisplayName()))
			{
				spec.setId(i++ + 1);
				instrumentList.add(spec);
			}
		}
		return ImmutableList.copyOf(instrumentList);
	}

	@Override
	public ImmutableSet<S> getSelectedItems()
	{
		List<S> instrumentList = new ArrayList<S>();
		ImmutableSet<S> allItems = super.getSelectedItems();
		for (S spec : allItems)
		{
			if (spec.getInstrument().getDisplayName().equals(activeInstrument.getDisplayName()))
				instrumentList.add(spec);
		}
		return ImmutableSet.copyOf(instrumentList);
	}

	@Override
	public void setSelectedItems(List<S> specs)
	{
		super.setSelectedItems(specs);
	}

	public void setSelectedIndices(int[] indices)
	{
		this.selectedIndices = indices;
	}

	public int[] getSelectedIndices()
	{
		return selectedIndices;
	}

	public List<S> getCurrentList()
	{
		return collections.get(activeInstrument.getDisplayName());
	}

	/**
	 * Sets the active instrument for this collection - this allows it to support
	 * @param activeInstrument
	 */
	public void setActiveInstrument(ISpectralInstrument activeInstrument)
	{
		this.activeInstrument = activeInstrument;
		if (collections.get(activeInstrument.getDisplayName()) != null)
			setAllItems(collections.get(activeInstrument.getDisplayName()));
		else
			collections.put(activeInstrument.getDisplayName(), getAllItems());
	}

	public ISpectralInstrument getActiveInstrument()
	{
		return activeInstrument;
	}
}
