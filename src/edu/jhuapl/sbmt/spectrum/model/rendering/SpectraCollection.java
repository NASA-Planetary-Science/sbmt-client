package edu.jhuapl.sbmt.spectrum.model.rendering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.ISmallBodyModel;
import edu.jhuapl.sbmt.client.SbmtSpectrumModelFactory;
import edu.jhuapl.sbmt.spectrum.model.core.BasicSpectrum;
import edu.jhuapl.sbmt.spectrum.model.core.interfaces.SearchSpec;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.ISpectralInstrument;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.Spectrum;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumColoringStyle;
import edu.jhuapl.sbmt.spectrum.model.sbmtCore.spectra.SpectrumKeyInterface;

public class SpectraCollection extends AbstractModel implements PropertyChangeListener
{
    private HashMap<IBasicSpectrumRenderer, List<vtkProp>> spectraActors = new HashMap<IBasicSpectrumRenderer, List<vtkProp>>();

    private HashMap<BasicSpectrum, IBasicSpectrumRenderer> fileToSpectrumMap = new HashMap<BasicSpectrum, IBasicSpectrumRenderer>();
//    private HashMap<BasicSpectrum, SearchSpec> fileToSpecMap = new HashMap<BasicSpectrum, SearchSpec>();
    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private HashMap<IBasicSpectrumRenderer, List<vtkProp>> spectrumToActorsMap = new HashMap<IBasicSpectrumRenderer, List<vtkProp>>();
    private HashMap<vtkProp, IBasicSpectrumRenderer> actorToSpectrumMap = new HashMap<vtkProp, IBasicSpectrumRenderer>();
    private ISmallBodyModel shapeModel;

    boolean selectAll=false;
    final double minFootprintSeparation=0.001;
    double footprintSeparation=0.001;

    Map<IBasicSpectrumRenderer,Integer> ordinals=Maps.newHashMap();
    final static int defaultOrdinal=0;

    public SpectraCollection(ISmallBodyModel eros)
    {
        this.shapeModel = eros;
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

    public IBasicSpectrumRenderer addSpectrum(SpectrumKeyInterface key) throws IOException
    {
        return addSpectrum(key.getName(), key.getInstrument(), false);
    }

    public IBasicSpectrumRenderer addSpectrum(SpectrumKeyInterface key, boolean isCustom) throws IOException
    {
        return addSpectrum(key.getName(), key.getInstrument(), isCustom);
    }

    public IBasicSpectrumRenderer addSpectrum(String path, ISpectralInstrument instrument, SpectrumColoringStyle coloringStyle) throws IOException
    {
        return addSpectrum(path, instrument, coloringStyle, false);
    }

    public IBasicSpectrumRenderer addSpectrum(String path, ISpectralInstrument instrument, SpectrumColoringStyle coloringStyle, boolean isCustom) throws IOException
    {
    	IBasicSpectrumRenderer spec = addSpectrum(path, instrument, isCustom);
        spec.getSpectrum().setColoringStyle(coloringStyle);
        return spec;
    }

    public IBasicSpectrumRenderer addSpectrum(BasicSpectrum spectrum, SpectrumColoringStyle coloringStyle) throws IOException
    {
        spectrum.setColoringStyle(coloringStyle);
        return fileToSpectrumMap.get(spectrum);
    }


    public IBasicSpectrumRenderer addSpectrum(String path, ISpectralInstrument instrument, boolean isCustom) throws IOException
    {
        if (fileToSpectrumMap.containsKey(path))
        {
        	IBasicSpectrumRenderer spec = fileToSpectrumMap.get(path);
            select(spec);
            spec.setVisible(true);
            return spec;
        }

        IBasicSpectrumRenderer spectrumRenderer = null;
        try
        {
        	spectrumRenderer = SbmtSpectrumModelFactory.createSpectrumRenderer(path, instrument);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        spectrumRenderer.getSpectrum().isCustomSpectra = isCustom;
        shapeModel.addPropertyChangeListener(spectrumRenderer);
        spectrumRenderer.addPropertyChangeListener(this);

        fileToSpectrumMap.put(spectrumRenderer.getSpectrum(), spectrumRenderer);
        spectraActors.put(spectrumRenderer, new ArrayList<vtkProp>());

//        spectrumRenderer.getSpectrum().setMetadata(fileToSpecMap.get(path));

        List<vtkProp> props = spectrumRenderer.getProps();

        spectrumToActorsMap.put(spectrumRenderer, new ArrayList<vtkProp>());

        spectrumToActorsMap.get(spectrumRenderer).addAll(props);

        for (vtkProp act : props)
            actorToSpectrumMap.put(act, spectrumRenderer);

        spectraActors.get(spectrumRenderer).addAll(props);


        for (vtkProp act : props)
            actorToFileMap.put(act, path);
        select(spectrumRenderer);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
    	IBasicSpectrumRenderer spectrum = fileToSpectrumMap.get(path);
        spectrum.setUnselected();

        List<vtkProp> actors = spectraActors.get(spectrum);

        for (vtkProp act : actors)
            actorToFileMap.remove(act);

        spectraActors.remove(spectrum);

        spectrumToActorsMap.remove(spectrum);

        fileToSpectrumMap.remove(path);

        spectrum.removePropertyChangeListener(this);
        shapeModel.removePropertyChangeListener(spectrum);
        spectrum.setShowFrustum(false);

        ordinals.remove(spectrum);

        for (vtkProp act : actors)
            actorToSpectrumMap.remove(act);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, spectrum);
    }

    public void removeAllSpectra()
    {
        HashMap<String, Spectrum> map = (HashMap<String, Spectrum>)fileToSpectrumMap.clone();
        for (String path : map.keySet())
            removeSpectrum(path);
    }

    public void removeAllSpectraForInstrument(ISpectralInstrument instrument)
    {
        HashMap<String, Spectrum> map = (HashMap<String, Spectrum>)fileToSpectrumMap.clone();
        for (String path : map.keySet())
        {
            if (map.get(path).getInstrument() == instrument)
                removeSpectrum(path);
        }
    }

    public void setVisibility(IBasicSpectrumRenderer spectrum, boolean visibility)
    {
        spectrum.setVisible(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void setFrustumVisibility(IBasicSpectrumRenderer spectrum, boolean visibility)
    {
        spectrum.setShowFrustum(visibility);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void toggleSelect(IBasicSpectrumRenderer spectrum)
    {
        if (spectrum.isSelected())
            spectrum.setUnselected();
        else
            spectrum.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void select(IBasicSpectrumRenderer spectrum)
    {
        spectrum.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void deselect(IBasicSpectrumRenderer spectrum)
    {
        spectrum.setUnselected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void toggleSelectAll()
    {
        if (!selectAll) // we're not in "select all" mode so go ahead and select all actors
        {
            for (IBasicSpectrumRenderer spectrum : fileToSpectrumMap.values())
                spectrum.setSelected();
            selectAll=true;
        }
        else
        {
            for (IBasicSpectrumRenderer spectrum : fileToSpectrumMap.values())
                spectrum.setUnselected();
            selectAll=false;
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void deselectAll()
    {
        for (IBasicSpectrumRenderer spectrum : fileToSpectrumMap.values())
            spectrum.setUnselected();
        selectAll=false;
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public List<IBasicSpectrumRenderer> getSelectedSpectra()
    {
        List<IBasicSpectrumRenderer> spectra=Lists.newArrayList();
        for (IBasicSpectrumRenderer s : fileToSpectrumMap.values())
            if (s.isSelected())
                spectra.add(s);
        return spectra;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        String filename = actorToFileMap.get(prop);
        IBasicSpectrumRenderer spectrum = this.fileToSpectrumMap.get(filename);
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
        return fileToSpectrumMap.get(file);
    }

//    public SearchSpec getSearchSpec(String file)
//    {
//        return fileToSpecMap.get(file);
//    }

    public boolean containsSpectrum(String file)
    {
        return fileToSpectrumMap.containsKey(file);
    }

    public void tagSpectraWithMetadata(BasicSpectrum spectrum, SearchSpec spec)
    {
//        fileToSpecMap.put(spectrum, spec);
        IBasicSpectrumRenderer spectrumRenderer = fileToSpectrumMap.get(spectrum);
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
        for (BasicSpectrum file : this.fileToSpectrumMap.keySet())
        {
        	IBasicSpectrumRenderer spectrum=this.fileToSpectrumMap.get(file);
            spectrum.getSpectrum().setColoringStyle(style);
            spectrum.updateChannelColoring();
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setColoringStyleForInstrument(SpectrumColoringStyle style, ISpectralInstrument instrument)
    {
        for (BasicSpectrum file : this.fileToSpectrumMap.keySet())
        {
        	IBasicSpectrumRenderer spectrum=this.fileToSpectrumMap.get(file);
            if (spectrum.getSpectrum().getInstrument() == instrument)
            {
                spectrum.getSpectrum().setColoringStyle(style);
                spectrum.updateChannelColoring();
            }
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setChannelColoring(int[] channels, double[] mins, double[] maxs, ISpectralInstrument instrument)
    {
        for (BasicSpectrum file : this.fileToSpectrumMap.keySet())
        {
        	IBasicSpectrumRenderer spectrum=this.fileToSpectrumMap.get(file);
            if (spectrum.getSpectrum().getInstrument() == instrument)
            {
                spectrum.getSpectrum().setChannelColoring(channels, mins, maxs);
                spectrum.updateChannelColoring();
            }
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public IBasicSpectrumRenderer getSpectrum(vtkActor actor)
    {
        return actorToSpectrumMap.get(actor);
    }

    public String getSpectrumName(vtkActor actor)
    {
        return actorToSpectrumMap.get(actor).getSpectrum().getSpectrumName();
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
        return fileToSpectrumMap.size();
    }

	public ISmallBodyModel getShapeModel()
	{
		return shapeModel;
	}
}
