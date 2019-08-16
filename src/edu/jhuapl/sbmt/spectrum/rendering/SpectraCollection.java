package edu.jhuapl.sbmt.spectrum.rendering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.model.lidar.SaavtkItemManager;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;

import glum.item.ItemEventListener;
import glum.item.ItemEventType;

public class SpectraCollection extends SaavtkItemManager<BasicSpectrum> implements PropertyChangeListener //, ItemProcessor<BasicSpectrum>, ItemManager<BasicSpectrum>
{
    private HashMap<IBasicSpectrumRenderer, List<vtkProp>> spectraActors = new HashMap<IBasicSpectrumRenderer, List<vtkProp>>();
    private HashMap<BasicSpectrum, IBasicSpectrumRenderer> spectrumToRendererMap = new HashMap<BasicSpectrum, IBasicSpectrumRenderer>();
    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private HashMap<IBasicSpectrumRenderer, List<vtkProp>> spectrumToActorsMap = new HashMap<IBasicSpectrumRenderer, List<vtkProp>>();
    private HashMap<vtkProp, IBasicSpectrumRenderer> actorToSpectrumMap = new HashMap<vtkProp, IBasicSpectrumRenderer>();
    private ISmallBodyModel shapeModel;

    boolean selectAll=false;
    final double minFootprintSeparation=0.001;
    double footprintSeparation=0.001;

    Map<IBasicSpectrumRenderer,Integer> ordinals=Maps.newHashMap();
    final static int defaultOrdinal=0;

    private List<ItemEventListener> listeners;

    public SpectraCollection(ISmallBodyModel eros)
    {
        this.shapeModel = eros;
        this.listeners = new ArrayList<ItemEventListener>();
    }

    public void reshiftFootprints()
    {
        for (IBasicSpectrumRenderer spectrum : ordinals.keySet())
        {
            spectrum.shiftFootprintToHeight(footprintSeparation*(1+ordinals.get(spectrum)));
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void setOrdinal(IBasicSpectrumRenderer spectrum, int ordinal)
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

    public boolean containsKey(SpectrumKeyInterface key)
    {
        for (IBasicSpectrumRenderer spectrum : spectrumToActorsMap.keySet())
        {
            if (spectrum.getSpectrum().getKey().equals(key))
                return true;
        }

        return false;
    }

    public Set<IBasicSpectrumRenderer> getSpectra()
    {
        return spectrumToActorsMap.keySet();
    }


    public IBasicSpectrumRenderer getSpectrumFromKey(SpectrumKeyInterface key)
    {
        for (IBasicSpectrumRenderer spectrum : spectrumToActorsMap.keySet())
        {
            if (spectrum.getSpectrum().getKey().equals(key))
                return spectrum;
        }

        return null;
    }

//    public IBasicSpectrumRenderer addSpectrum(SpectrumKeyInterface key) throws IOException
//    {
//        return addSpectrum(key.getName(), key.getInstrument(), false);
//    }

//    public IBasicSpectrumRenderer addSpectrum(SpectrumKeyInterface key, boolean isCustom) throws IOException
//    {
//        return addSpectrum(key.getName(), key.getInstrument(), isCustom);
//    }

//    public IBasicSpectrumRenderer addSpectrum(String path, BasicSpectrumInstrument instrument, SpectrumColoringStyle coloringStyle) throws IOException
//    {
//        return addSpectrum(path, instrument, coloringStyle, false);
//    }

//    public IBasicSpectrumRenderer addSpectrum(String path, BasicSpectrumInstrument instrument, SpectrumColoringStyle coloringStyle, boolean isCustom) throws IOException
//    {
//    	IBasicSpectrumRenderer spec = addSpectrum(path, instrument, isCustom);
//        spec.getSpectrum().setColoringStyle(coloringStyle);
//        return spec;
//    }

    public IBasicSpectrumRenderer addSpectrum(BasicSpectrum spectrum, SpectrumColoringStyle coloringStyle) //throws IOException
    {
        spectrum.setColoringStyle(coloringStyle);
        return spectrumToRendererMap.get(spectrum);
    }

    public void addSpectrum(BasicSpectrum spec) //throws IOException
    {
    	spectrumToRendererMap.put(spec, null);
    }


//    public IBasicSpectrumRenderer addSpectrum(String path, BasicSpectrumInstrument instrument, boolean isCustom) throws IOException
    public IBasicSpectrumRenderer addSpectrum(BasicSpectrum spectrum, boolean isCustom) //throws IOException
    {
        if (spectrumToRendererMap.get(spectrum) != null)
        {
        	IBasicSpectrumRenderer spec = spectrumToRendererMap.get(spectrum);
            select(spec);
            spec.setVisible(true);
            return spec;
        }

        IBasicSpectrumRenderer spectrumRenderer = null;
        try
        {
        	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(spectrum, spectrum.getInstrument());
        	spectrumRenderer.getSpectrum().readSpectrumFromFile();
        }
        catch (Exception e) {
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
        //select(spectrumRenderer);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spectrumRenderer);
        return spectrumRenderer;
    }

    public void removeSpectrum(SpectrumKeyInterface key)
    {
        if (!containsKey(key))
            return;

        removeSpectrum(key.getName());
    }

    public void removeSpectrum(String path)
    {
    	IBasicSpectrumRenderer spectrumRenderer = spectrumToRendererMap.get(path);
        spectrumRenderer.setUnselected();

        List<vtkProp> actors = spectraActors.get(spectrumRenderer);

        for (vtkProp act : actors)
            actorToFileMap.remove(act);

        spectraActors.remove(spectrumRenderer);

        spectrumToActorsMap.remove(spectrumRenderer);

        spectrumToRendererMap.remove(path);

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
        HashMap<String, Spectrum> map = (HashMap<String, Spectrum>)spectrumToRendererMap.clone();
        for (String path : map.keySet())
            removeSpectrum(path);
    }

    public void removeAllSpectraForInstrument(ISpectralInstrument instrument)
    {
        HashMap<String, Spectrum> map = (HashMap<String, Spectrum>)spectrumToRendererMap.clone();
        for (String path : map.keySet())
        {
            if (map.get(path).getInstrument() == instrument)
                removeSpectrum(path);
        }
    }

    public boolean isSpectrumMapped(BasicSpectrum spec)
    {
    	return spectrumToRendererMap.get(spec) != null;
    }

    public void setVisibility(IBasicSpectrumRenderer spectrumRenderer, boolean visibility)
    {
        spectrumRenderer.setVisible(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
    }

    public void setVisibility(BasicSpectrum spec, boolean visibility)
    {
    	IBasicSpectrumRenderer spectrumRenderer = spectrumToRendererMap.get(spec);
        spectrumRenderer.setVisible(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
    }

    public boolean getVisibility(BasicSpectrum spec)
    {
    	IBasicSpectrumRenderer spectrumRenderer = spectrumToRendererMap.get(spec);
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
    	IBasicSpectrumRenderer spectrumRenderer = spectrumToRendererMap.get(spec);
    	if (spectrumRenderer == null) return false;
        return spectrumRenderer.isFrustumShowing();
    }

    public boolean getBoundaryVisibility(BasicSpectrum spec)
    {
    	IBasicSpectrumRenderer spectrumRenderer = spectrumToRendererMap.get(spec);
    	if (spectrumRenderer == null) return false;
        return spectrumRenderer.isOutlineShowing();
    }

    public IBasicSpectrumRenderer getRendererForSpectrum(BasicSpectrum spec)
    {
    	return spectrumToRendererMap.get(spec);
    }

    public void toggleSelect(IBasicSpectrumRenderer spectrumRenderer)
    {
        if (spectrumRenderer.isSelected())
            spectrumRenderer.setUnselected();
        else
            spectrumRenderer.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void select(IBasicSpectrumRenderer spectrumRenderer)
    {
        spectrumRenderer.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void deselect(IBasicSpectrumRenderer spectrumRenderer)
    {
        spectrumRenderer.setUnselected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        selectAll=false;
    }

    public void toggleSelectAll()
    {
        if (!selectAll) // we're not in "select all" mode so go ahead and select all actors
        {
            for (IBasicSpectrumRenderer spectrumRenderer : spectrumToRendererMap.values())
            {
                spectrumRenderer.setSelected();
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
            }
            selectAll=true;
        }
        else
        {
            for (IBasicSpectrumRenderer spectrumRenderer : spectrumToRendererMap.values())
            {
                spectrumRenderer.setUnselected();
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
            }
            selectAll=false;
        }

    }

    public void deselectAll()
    {
        for (IBasicSpectrumRenderer spectrumRenderer : spectrumToRendererMap.values())
        {
            spectrumRenderer.setUnselected();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,spectrumRenderer);
        }
        selectAll=false;

    }

    public List<IBasicSpectrumRenderer> getSelectedSpectra()
    {
        List<IBasicSpectrumRenderer> spectra=Lists.newArrayList();
        for (IBasicSpectrumRenderer s : spectrumToRendererMap.values())
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
        IBasicSpectrumRenderer spectrum = this.spectrumToRendererMap.get(filename);
        if (spectrum==null)
            return "";
        return spectrum.getSpectrum().getInstrument().getDisplayName() + " spectrum " + filename.substring(16, 25) + " acquired at " + spectrum.getSpectrum().getDateTime().toString() /*+ "(SCLK: " + ((OTESSpectrum)spectrum).getTime() + ")"*/;
    }

    public String getSpectrumName(vtkProp actor)
    {
        return actorToFileMap.get(actor);
    }

    public IBasicSpectrumRenderer getSpectrum(String file)
    {
        return spectrumToRendererMap.get(file);
    }

    public boolean containsSpectrum(String file)
    {
        return spectrumToRendererMap.containsKey(file);
    }

    public void tagSpectraWithMetadata(BasicSpectrum spectrum, SearchSpec spec)
    {
        if (spectrum != null) spectrum.setMetadata(spec);
    }

    public void tagSpectraWithMetadata(List<BasicSpectrum> filenames, SearchSpec spec)
    {
        for (BasicSpectrum list : filenames)
        {
        	tagSpectraWithMetadata(list, spec);
//            fileToSpecMap.put(list, spec);
//            IBasicSpectrumRenderer spectrum = fileToSpectrumMap.get(list);
//            if (spectrum != null) spectrum.getSpectrum().setMetadata(spec);
        }
    }

    public void setColoringStyle(SpectrumColoringStyle style)
    {
        for (BasicSpectrum spec : this.spectrumToRendererMap.keySet())
        {
        	IBasicSpectrumRenderer spectrumRenderer=this.spectrumToRendererMap.get(spec);
        	if (spectrumRenderer == null) continue;
            spectrumRenderer.getSpectrum().setColoringStyle(style);
            spectrumRenderer.updateChannelColoring();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spectrumRenderer);
        }


    }

    public void setColoringStyleForInstrument(SpectrumColoringStyle style, ISpectralInstrument instrument)
    {
        for (BasicSpectrum spec : this.spectrumToRendererMap.keySet())
        {
        	IBasicSpectrumRenderer spectrumRenderer = this.spectrumToRendererMap.get(spec);
        	if (spectrumRenderer == null) continue;
        	if (spectrumRenderer.getSpectrum().getInstrument().getClass() == instrument.getClass() )
            {
                spectrumRenderer.getSpectrum().setColoringStyle(style);
                spectrumRenderer.updateChannelColoring();
//                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spectrumRenderer);
            }
        }


    }

    public void setChannelColoring(int[] channels, double[] mins, double[] maxs, ISpectralInstrument instrument)
    {
        for (BasicSpectrum spec : this.spectrumToRendererMap.keySet())
        {
        	IBasicSpectrumRenderer spectrumRenderer = this.spectrumToRendererMap.get(spec);
        	if (spectrumRenderer == null) continue;
            if (spectrumRenderer.getSpectrum().getInstrument().getClass() == instrument.getClass() )
            {
                spectrumRenderer.getSpectrum().setChannelColoring(channels, mins, maxs);
                spectrumRenderer.updateChannelColoring();
//                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, spectrumRenderer);
            }
        }


    }

    public IBasicSpectrumRenderer getSpectrum(vtkActor actor)
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
        for (IBasicSpectrumRenderer spec : actorToSpectrumMap.values())
        {
            if (spec.getSpectrum().getSpectrumName().equals(name)) return true;
        }
        return false;
    }

    public IBasicSpectrumRenderer getSpectrumForName(String name)
    {
        for (IBasicSpectrumRenderer spec : actorToSpectrumMap.values())
        {
            if (spec.getSpectrum().getSpectrumName().equals(name)) return spec;
        }
        return null;
    }

    @Override
    public List<vtkProp> getProps()
    {
        List<vtkProp> allProps=Lists.newArrayList();
        for (IBasicSpectrumRenderer s : spectraActors.keySet())
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
	public void removeItems(List<BasicSpectrum> specs)
	{
		// Remove relevant state and VTK mappings
		for (BasicSpectrum spec : specs)
		{
			removeSpectrum(spec.getFullPath());
		}

		// Delegate
		super.removeItems(specs);

//		List<LidarTrack> tmpL = ImmutableList.of();
//		updateVtkVars(tmpL);
	}

	@Override
	public void setAllItems(List<BasicSpectrum> specs)
	{
		for (BasicSpectrum spec : specs)
		{
			addSpectrum(spec);
		}

		super.setAllItems(specs);
		// Send out the appropriate notifications
		notifyListeners(this, ItemEventType.ItemsSelected);
	}

	@Override
	public void setSelectedItems(List<BasicSpectrum> specs)
	{
		super.setSelectedItems(specs);
	}
}
