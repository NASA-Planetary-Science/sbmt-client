package edu.jhuapl.sbmt.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.AbstractModel;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;

public class NISSpectraCollection extends AbstractModel implements PropertyChangeListener
{
    private HashMap<NISSpectrum, List<vtkProp>> spectraActors = new HashMap<NISSpectrum, List<vtkProp>>();

    private HashMap<String, NISSpectrum> fileToSpectrumMap = new HashMap<String, NISSpectrum>();

    private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
    private SmallBodyModel erosModel;

    boolean selectAll=false;
    final double minFootprintSeparation=0.001;
    double footprintSeparation=0.001;

    Map<NISSpectrum,Integer> ordinals=Maps.newHashMap();
    final static int defaultOrdinal=0;

    public NISSpectraCollection(SmallBodyModel eros)
    {
        this.erosModel = eros;
    }

    public void reshiftFootprints()
    {
        for (NISSpectrum spectrum : ordinals.keySet())
        {
            spectrum.shiftFootprintToHeight(footprintSeparation*(1+ordinals.get(spectrum)));
            //System.out.println(ordinals.get(spectrum)+" "+spectrum.isSelected);
        }
        //System.out.println();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public void setOrdinal(NISSpectrum spectrum, int ordinal)
    {
        //System.out.println(spectrum);
        if (ordinals.containsKey(spectrum))
            ordinals.remove(spectrum);
        ordinals.put(spectrum, ordinal);
        //System.out.println(ordinals);
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

        /*
        for (vtkProp p : props)
        {
            vtkActor a=(vtkActor)p;
            vtkPolyDataMapper m=(vtkPolyDataMapper)a.GetMapper();
            System.out.println(m);
            vtkPolyData polyData=m.GetInput();
            System.out.println(polyData.GetNumberOfCells());
        }*/

        spectraActors.get(spectrum).addAll(props);

        for (vtkProp act : props)
            actorToFileMap.put(act, path);


        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeSpectrum(String path)
    {
        NISSpectrum spectrum = fileToSpectrumMap.get(path);

        List<vtkProp> actors = spectraActors.get(spectrum);

        for (vtkProp act : actors)
            actorToFileMap.remove(act);

        spectraActors.remove(spectrum);

        fileToSpectrumMap.remove(path);

        spectrum.removePropertyChangeListener(this);
        erosModel.removePropertyChangeListener(spectrum);
        spectrum.setShowFrustum(false);

        ordinals.remove(spectrum);

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
        if (spectrum.isSelected)
            spectrum.setUnselected();
        else
            spectrum.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void select(NISSpectrum spectrum)
    {
        spectrum.setSelected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void deselect(NISSpectrum spectrum)
    {
        spectrum.setUnselected();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
        selectAll=false;
    }

    public void toggleSelectAll()
    {
        if (!selectAll) // we're not in "select all" mode so go ahead and select all actors
        {
            for (NISSpectrum spectrum : fileToSpectrumMap.values())
                spectrum.setSelected();
            selectAll=true;
        }
        else
        {
            for (NISSpectrum spectrum : fileToSpectrumMap.values())
                spectrum.setUnselected();
            selectAll=false;
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED,null,null);
    }

    public List<NISSpectrum> getSelectedSpectra()
    {
        List<NISSpectrum> spectra=Lists.newArrayList();
        for (NISSpectrum s : fileToSpectrumMap.values())
            if (s.isSelected)
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

    @Override
    public List<vtkProp> getProps()
    {
        List<vtkProp> allProps=Lists.newArrayList();
        for (NISSpectrum s : spectraActors.keySet())
            allProps.addAll(spectraActors.get(s));
        return allProps;
    }
}
