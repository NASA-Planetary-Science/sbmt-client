package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.*;

public class NISSpectraCollection extends Model implements PropertyChangeListener
{
	private ArrayList<vtkProp> allActors = new ArrayList<vtkProp>();

	private HashMap<NISSpectrum, ArrayList<vtkProp>> spectraActors = new HashMap<NISSpectrum, ArrayList<vtkProp>>();

	private HashMap<String, NISSpectrum> fileToSpectrumMap = new HashMap<String, NISSpectrum>();

	private HashMap<vtkProp, String> actorToFileMap = new HashMap<vtkProp, String>();
	private SmallBodyModel erosModel;
	
	public NISSpectraCollection(SmallBodyModel eros) 
	{
		super(ModelNames.NIS_SPECTRA);
		
		this.erosModel = eros;
	}

	public void addSpectrum(String path) throws FitsException, IOException
	{
		if (fileToSpectrumMap.containsKey(path))
			return;
		
		//NISSpectrum spectrum = new NISSpectrum(path, erosModel);
		NISSpectrum spectrum = NISSpectrum.NISSpectrumFactory.createSpectrum(path, erosModel);		
		
		spectrum.addPropertyChangeListener(this);

		fileToSpectrumMap.put(path, spectrum);
		spectraActors.put(spectrum, new ArrayList<vtkProp>());
				
		// Now texture map this image onto the Eros model.
		//spectrum.setPolygonOffset(-10.0);

		ArrayList<vtkProp> imagePieces = spectrum.getProps();

		spectraActors.get(spectrum).addAll(imagePieces);

		for (vtkProp act : imagePieces)
			actorToFileMap.put(act, path);
		
		allActors.addAll(imagePieces);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeImage(String path)
	{
		ArrayList<vtkProp> actors = spectraActors.get(fileToSpectrumMap.get(path));
		allActors.removeAll(actors);
		
		for (vtkProp act : actors)
			actorToFileMap.remove(act);

		spectraActors.remove(fileToSpectrumMap.get(path));
		
		fileToSpectrumMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllImages()
	{
		allActors.clear();
		actorToFileMap.clear();
		spectraActors.clear();
		fileToSpectrumMap.clear();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public ArrayList<vtkProp> getProps() 
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
    
    public void setChannelColoring(int channel, double min, double max)
    {
    	NISSpectrum.setChannelColoring(channel, min, max);
    	for (String file : this.fileToSpectrumMap.keySet())
    	{
    		this.fileToSpectrumMap.get(file).updateChannelColoring();
    	}

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}
