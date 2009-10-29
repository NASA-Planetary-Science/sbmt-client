package edu.jhuapl.near.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import nom.tam.fits.FitsException;

import vtk.vtkActor;

public class NearImageCollection extends Model
{
//	private ArrayList<NearImage> nearImages = new ArrayList<NearImage>();
    private ArrayList<vtkActor> allActors = new ArrayList<vtkActor>();

    private HashMap<NearImage, ArrayList<vtkActor>> nearImageActors = new HashMap<NearImage, ArrayList<vtkActor>>();
    
    private HashMap<String, NearImage> fileToImageMap = new HashMap<String, NearImage>();

    public void addImage(String path) throws FitsException, IOException
    {
    	NearImage image = new NearImage(path);

    	fileToImageMap.put(path, image);
    	nearImageActors.put(image, new ArrayList<vtkActor>());
    	
    	// Now texture map this image onto the Eros model.
    	mapImage(image, -10);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
    
    public void removeImage(String path)
    {
    	
    	//for (vtkActor act : nearImageActors.get(fileToImageMap.get(file)))
    	//	renWin.GetRenderer().RemoveActor(act);

    	nearImageActors.remove(fileToImageMap.get(path));
    	fileToImageMap.remove(path);

    	this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private void mapImage(NearImage nearImage, double offset)
    {
    	ArrayList<vtkActor> imagePieces = nearImage.getMappedImage(offset);
    	
    	for (vtkActor piece : imagePieces)
    	{
    		//renWin.GetRenderer().AddActor(piece);
            
    		nearImageActors.get(nearImage).add(piece);
    	}
    }
    
	public ArrayList<vtkActor> getActors() 
	{
		return allActors;
	}

}
