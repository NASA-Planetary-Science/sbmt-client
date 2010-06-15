package edu.jhuapl.near.model.eros;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.SmallBodyModel;
import edu.jhuapl.near.model.eros.MSIImage.MSIKey;
import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.*;

public class MSIImageCollection extends Model implements PropertyChangeListener
{
	private SmallBodyModel erosModel;
	
	private ArrayList<vtkProp> allActors = new ArrayList<vtkProp>();

	private HashMap<MSIImage, ArrayList<vtkProp>> msiImageActors = new HashMap<MSIImage, ArrayList<vtkProp>>();

	private HashMap<vtkProp, MSIImage> actorToImageMap = new HashMap<vtkProp, MSIImage>();

	public MSIImageCollection(SmallBodyModel eros)
	{
		this.erosModel = eros;
	}
	
	private boolean containsKey(MSIKey key)
	{
		for (MSIImage image : msiImageActors.keySet())
		{
			if (image.getKey().equals(key))
				return true;
		}
	
		return false;
	}
	
	private MSIImage getImageFromKey(MSIKey key)
	{
		for (MSIImage image : msiImageActors.keySet())
		{
			if (image.getKey().equals(key))
				return image;
		}
	
		return null;
	}
	
	public void addImage(MSIKey key) throws FitsException, IOException
	{
		if (containsKey(key))
			return;
		
		MSIImage image = MSIImage.MSIImageFactory.createImage(key.name, erosModel, key.source);

		image.addPropertyChangeListener(this);

		msiImageActors.put(image, new ArrayList<vtkProp>());
				
		ArrayList<vtkProp> imagePieces = image.getProps();

		msiImageActors.get(image).addAll(imagePieces);

		for (vtkProp act : imagePieces)
			actorToImageMap.put(act, image);
		
		allActors.addAll(imagePieces);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeImage(MSIKey key)
	{
		ArrayList<vtkProp> actors = msiImageActors.get(getImageFromKey(key));
		allActors.removeAll(actors);
		
		for (vtkProp act : actors)
			actorToImageMap.remove(act);

		msiImageActors.remove(getImageFromKey(key));
		
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllImages()
	{
		allActors.clear();
		actorToImageMap.clear();
		msiImageActors.clear();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public ArrayList<vtkProp> getProps() 
	{
		return allActors;
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

    public String getClickStatusBarText(vtkProp prop, int cellId)
    {
    	File file = new File(actorToImageMap.get(prop).getKey().name);
    	return "MSI image " + file.getName().substring(2, 11);
    }

    public String getImageName(vtkActor actor)
    {
    	return actorToImageMap.get(actor).getKey().name;
    }
    
    public MSIImage getImage(MSIKey key)
    {
    	return getImageFromKey(key);
    }
    
    public boolean containsImage(MSIKey key)
    {
    	return containsKey(key);
    }
}
