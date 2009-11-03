package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.vtkActor;

public class NearImageCollection extends Model implements PropertyChangeListener
{
	private ArrayList<vtkActor> dummyActors = new ArrayList<vtkActor>();
	private boolean hidden = false;

	private ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();

	private HashMap<NearImage, ArrayList<vtkActor>> nearImageActors = new HashMap<NearImage, ArrayList<vtkActor>>();

	private HashMap<String, NearImage> fileToImageMap = new HashMap<String, NearImage>();

	public void addImage(String path) throws FitsException, IOException
	{
		NearImage image = new NearImage(path);

		image.addPropertyChangeListener(this);

		fileToImageMap.put(path, image);
		nearImageActors.put(image, new ArrayList<vtkActor>());

		// Now texture map this image onto the Eros model.
		image.setPolygonOffset(-10.0);
		mapImage(image);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeImage(String path)
	{
		ArrayList<vtkActor> actors = nearImageActors.get(fileToImageMap.get(path));
		allActors.removeAll(actors);
		nearImageActors.remove(fileToImageMap.get(path));
		fileToImageMap.remove(path);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void removeAllImages()
	{
		allActors.clear();
		nearImageActors.clear();
		fileToImageMap.clear();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	private void mapImage(NearImage nearImage)
	{
		ArrayList<vtkActor> imagePieces = nearImage.getActors();

		nearImageActors.get(nearImage).addAll(imagePieces);

		allActors.addAll(imagePieces);
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

}
