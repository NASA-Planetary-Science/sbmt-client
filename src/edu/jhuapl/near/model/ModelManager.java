package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import edu.jhuapl.near.util.Properties;

import vtk.vtkActor;

public class ModelManager extends Model implements PropertyChangeListener
{
	static public final String LINEAMENT = "lineament";
	static public final String MSI_IMAGES = "msi-images";
	static public final String EROS= "eros";
	static public final String MSI_BOUNDARY = "msi-boundary";
	
	private LineamentModel lineamentModel;
	private NearImageCollection msiImages;
	private ErosModel erosModel;
	private MSIBoundaryCollection msiBoundaries;
	
    private ArrayList<vtkActor> actors = new ArrayList<vtkActor>();

    public ModelManager()
    {
    	lineamentModel = new LineamentModel();
    	erosModel = new ErosModel();
    	msiImages = new NearImageCollection();
    	msiBoundaries = new MSIBoundaryCollection();
    	
    	lineamentModel.addPropertyChangeListener(this);
    	erosModel.addPropertyChangeListener(this);
    	msiImages.addPropertyChangeListener(this);
    	msiBoundaries.addPropertyChangeListener(this);
    	
    	updateActors();
    }

    public ArrayList<vtkActor> getActors()
	{
		return actors;
	}

	public void propertyChange(PropertyChangeEvent arg0) 
	{
		updateActors();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	private void updateActors()
	{
		actors.clear();
    	actors.addAll(lineamentModel.getActors());
    	actors.addAll(erosModel.getActors());
    	actors.addAll(msiImages.getActors());
    	actors.addAll(msiBoundaries.getActors());
	}
	
	public Model getModel(String modelName)
	{
		if (LINEAMENT.equals(modelName))
			return lineamentModel;
		else if (EROS.equals(modelName))
			return erosModel;
		else if (MSI_IMAGES.equals(modelName))
			return msiImages;
		else if (MSI_BOUNDARY.equals(modelName))
			return msiBoundaries;
		else
			return null;
	}
}
