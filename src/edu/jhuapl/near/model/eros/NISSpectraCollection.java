package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Properties;

public class NISSpectraCollection extends Model implements PropertyChangeListener
{
    private ArrayList<vtkProp> allActors = new ArrayList<vtkProp>();

    private HashMap<NISSpectrum, ArrayList<vtkProp>> spectraActors = new HashMap<NISSpectrum, ArrayList<vtkProp>>();

    private HashMap<String, NISSpectrum> fileToSpectrumMap = new HashMap<String, NISSpectrum>();

    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private SmallBodyModel erosModel;

    private Map<NISSpectrum, vtkActor> selectionActors=Maps.newHashMap();
    boolean selectAll=false;

    double footprintDecimationFactor=0.1;

    public NISSpectraCollection(SmallBodyModel eros)
    {
        this.erosModel = eros;
    }

    public void addSpectrum(String path) throws IOException
    {
        if (fileToSpectrumMap.containsKey(path))
            return;

        //NISSpectrum spectrum = NISSpectrum.NISSpectrumFactory.createSpectrum(path, erosModel);
        NISSpectrum spectrum = new NISSpectrum(path, erosModel);

        erosModel.addPropertyChangeListener(spectrum);
        spectrum.addPropertyChangeListener(this);

        fileToSpectrumMap.put(path, spectrum);
        spectraActors.put(spectrum, new ArrayList<vtkProp>());

        List<vtkProp> props = spectrum.getProps();

        spectraActors.get(spectrum).addAll(props);

        vtkFeatureEdges edgeFilter=new vtkFeatureEdges();
        edgeFilter.SetInputData(spectrum.getShiftedFootprint());
        edgeFilter.BoundaryEdgesOn();
        edgeFilter.FeatureEdgesOff();
        edgeFilter.ManifoldEdgesOff();
        edgeFilter.NonManifoldEdgesOff();
        edgeFilter.Update();
        vtkPolyDataMapper mapper=new vtkPolyDataMapper();
        mapper.SetInputData(edgeFilter.GetOutput());
        mapper.Update();
        vtkActor actor=new vtkActor();
        actor.SetMapper(mapper);
        actor.VisibilityOff();
        actor.GetProperty().EdgeVisibilityOn();
        actor.GetProperty().SetColor(1,0,0);
        actor.GetProperty().SetLineWidth(3);
        selectionActors.put(spectrum, actor);

        for (vtkProp act : props)
            actorToFileMap.put(act, path);

        allActors.addAll(props);
        allActors.add(actor);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeSpectrum(String path)
    {
        NISSpectrum spectrum = fileToSpectrumMap.get(path);

        ArrayList<vtkProp> actors = spectraActors.get(spectrum);
        allActors.removeAll(actors);

        for (vtkProp act : actors)
            actorToFileMap.remove(act);

        spectraActors.remove(spectrum);
        selectionActors.remove(spectrum);

        fileToSpectrumMap.remove(path);

        spectrum.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(spectrum);
        spectrum.setShowFrustum(false);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_REMOVED, null, spectrum);
    }

    public void removeAllSpectra()
    {
        HashMap<String, NISSpectrum> map = (HashMap<String, NISSpectrum>)fileToSpectrumMap.clone();
        for (String path : map.keySet())
            removeSpectrum(path);
    }

    public void toggleSelect(NISSpectrum spectrum)
    {
        boolean selected=selectionActors.get(spectrum).GetVisibility()==1;
        if (selected)
            selectionActors.get(spectrum).VisibilityOff();
        else
            selectionActors.get(spectrum).VisibilityOn();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void select(NISSpectrum spectrum)
    {
        selectionActors.get(spectrum).VisibilityOn();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void deselect(NISSpectrum spectrum)
    {
        selectionActors.get(spectrum).VisibilityOff();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void toggleSelectAll()
    {
        if (!selectAll) // we're not in "select all" mode so go ahead and select all actors
        {
            for (vtkActor actor : selectionActors.values())
                actor.VisibilityOn();
            selectAll=true;
        }
        else
        {
            for (vtkActor actor : selectionActors.values())
                actor.VisibilityOff();
            selectAll=false;
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);

    }

    public List<NISSpectrum> getSelectedSpectra()
    {
        List<NISSpectrum> spectra=Lists.newArrayList();
        for (NISSpectrum s : selectionActors.keySet())
            if (selectionActors.get(s).GetVisibility()==1)
                spectra.add(s);
        return spectra;
    }

    public List<vtkProp> getProps()
    {
        return allActors;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        String filename = actorToFileMap.get(prop);
        NISSpectrum spectrum = this.fileToSpectrumMap.get(filename);
        if (spectrum==null)
            return "";
        return "NIS Spectrum " + filename.substring(16, 25) + " acquired at " + spectrum.getDateTime().toString();
    }

    public String getSpectrumName(vtkProp actor)
    {
        return actorToFileMap.get(actor);
    }

    public NISSpectrum getSpectrum(String file)
    {
        return fileToSpectrumMap.get(file);
    }

    public boolean containsSpectrum(String file)
    {
        return fileToSpectrumMap.containsKey(file);
    }

    public void setChannelColoring(int[] channels, double[] mins, double[] maxs)
    {
        NISSpectrum.setChannelColoring(channels, mins, maxs);

        for (String file : this.fileToSpectrumMap.keySet())
        {
            this.fileToSpectrumMap.get(file).updateChannelColoring();
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
