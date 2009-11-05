package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.vtkActor;

public class NearImageCollection extends Model implements PropertyChangeListener
{
	/**
	 * return this when images are hidden
	 */
	private ArrayList<vtkActor> dummyActors = new ArrayList<vtkActor>();
	private boolean hidden = false;

	private ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();

	private HashMap<NearImage, ArrayList<vtkActor>> nearImageActors = new HashMap<NearImage, ArrayList<vtkActor>>();

	private HashMap<String, NearImage> fileToImageMap = new HashMap<String, NearImage>();

	private HashMap<vtkActor, String> actorToFileMap = new HashMap<vtkActor, String>();

	public void addImage(String path) throws FitsException, IOException
	{
		if (fileToImageMap.containsKey(path))
			return;
		
		//NearImage image = new NearImage(path);
		NearImage image = NearImage.NearImageFactory.createImage(path);

		image.addPropertyChangeListener(this);

		fileToImageMap.put(path, image);
		nearImageActors.put(image, new ArrayList<vtkActor>());
				
		// Now texture map this image onto the Eros model.
		image.setPolygonOffset(-10.0);

		ArrayList<vtkActor> imagePieces = image.getActors();

		nearImageActors.get(image).addAll(imagePieces);

		for (vtkActor act : imagePieces)
			actorToFileMap.put(act, path);
		
		allActors.addAll(imagePieces);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeImage(String path)
	{
		ArrayList<vtkActor> actors = nearImageActors.get(fileToImageMap.get(path));
		allActors.removeAll(actors);
		
		for (vtkActor act : actors)
			actorToFileMap.remove(act);

		nearImageActors.remove(fileToImageMap.get(path));
		
		fileToImageMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllImages()
	{
		allActors.clear();
		actorToFileMap.clear();
		nearImageActors.clear();
		fileToImageMap.clear();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		if (!hidden)
			return allActors;
		else
			return dummyActors;
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

    public String getClickStatusBarText(vtkActor actor, int cellId)
    {
    	File file = new File(actorToFileMap.get(actor));
    	return "MSI image " + file.getName().substring(2, 11);
    }

    public String getImageName(vtkActor actor)
    {
    	return actorToFileMap.get(actor);
    }
    
    public NearImage getImage(String file)
    {
    	return fileToImageMap.get(file);
    }
    
    public boolean containsImage(String file)
    {
    	return fileToImageMap.containsKey(file);
    }
}
