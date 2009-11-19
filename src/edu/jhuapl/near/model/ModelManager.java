package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import vtk.vtkActor;

public class ModelManager extends Model implements PropertyChangeListener
{
	static public final String LINEAMENT = "lineament";
	static public final String MSI_IMAGES = "msi-images";
	static public final String EROS= "eros";
	static public final String MSI_BOUNDARY = "msi-boundary";
	static public final String NIS_SPECTRA = "nis-spectra";
	
	private LineamentModel lineamentModel;
	private NearImageCollection msiImages;
	private ErosModel erosModel;
	private MSIBoundaryCollection msiBoundaries;
	private NISSpectraCollection nisSpectra;
	
    private ArrayList<vtkActor> actors = new ArrayList<vtkActor>();
    private ArrayList<vtkActor> actorsExceptEros = new ArrayList<vtkActor>();
    private HashMap<vtkActor, Model> actorToModelMap = new HashMap<vtkActor, Model>();
    
    private ArrayList<Model> allModels;
    
    public ModelManager()
    {
    	lineamentModel = new LineamentModel();
    	erosModel = new ErosModel();
    	msiImages = new NearImageCollection();
    	msiBoundaries = new MSIBoundaryCollection();
    	nisSpectra = new NISSpectraCollection(erosModel);
    	
    	lineamentModel.addPropertyChangeListener(this);
    	erosModel.addPropertyChangeListener(this);
    	msiImages.addPropertyChangeListener(this);
    	msiBoundaries.addPropertyChangeListener(this);
    	nisSpectra.addPropertyChangeListener(this);
    	
    	allModels = new ArrayList<Model>();
    	allModels.add(lineamentModel);
    	allModels.add(erosModel);
    	allModels.add(msiImages);
    	allModels.add(msiBoundaries);
    	allModels.add(nisSpectra);
    	
		updateActors();
    }

    public ArrayList<vtkActor> getActors()
	{
		return actors;
	}

    public ArrayList<vtkActor> getActorsExceptEros()
	{
		return actorsExceptEros;
	}

	public void propertyChange(PropertyChangeEvent arg0) 
	{
		updateActors();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}
	
	private void updateActors()
	{
		actors.clear();
		actorsExceptEros.clear();
		actorToModelMap.clear();

		for (Model model : allModels)
		{
			actors.addAll(model.getActors());
						
			for (vtkActor act : model.getActors())
	    		actorToModelMap.put(act, model);
			
			if (!(model instanceof ErosModel))
				actorsExceptEros.addAll(model.getActors());
		}
	}

	public Model getModel(vtkActor actor)
	{
		return actorToModelMap.get(actor);
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
		else if (NIS_SPECTRA.equals(modelName))
			return nisSpectra;
		else
			return null;
	}
}
