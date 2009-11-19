package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.vtkActor;

public class NISSpectraCollection extends Model implements PropertyChangeListener
{
	private ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();

	private HashMap<NISSpectrum, ArrayList<vtkActor>> spectraActors = new HashMap<NISSpectrum, ArrayList<vtkActor>>();

	private HashMap<String, NISSpectrum> fileToSpectrumMap = new HashMap<String, NISSpectrum>();

	private HashMap<vtkActor, String> actorToFileMap = new HashMap<vtkActor, String>();
	private ErosModel erosModel;
	
	public NISSpectraCollection(ErosModel eros) 
	{
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
		spectraActors.put(spectrum, new ArrayList<vtkActor>());
				
		// Now texture map this image onto the Eros model.
		//spectrum.setPolygonOffset(-10.0);

		ArrayList<vtkActor> imagePieces = spectrum.getActors();

		spectraActors.get(spectrum).addAll(imagePieces);

		for (vtkActor act : imagePieces)
			actorToFileMap.put(act, path);
		
		allActors.addAll(imagePieces);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeImage(String path)
	{
		ArrayList<vtkActor> actors = spectraActors.get(fileToSpectrumMap.get(path));
		allActors.removeAll(actors);
		
		for (vtkActor act : actors)
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

	public ArrayList<vtkActor> getActors() 
	{
		return allActors;
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	//File file = new File(actorToFileMap.get(actor));
    	return "NIS Spectrum";// " + file.getName().substring(2, 11);
    }

    public String getSpectrumName(vtkActor actor)
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
}
