package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import vtk.*;

public abstract class ModelManager extends Model implements PropertyChangeListener
{
    private ArrayList<vtkProp> props = new ArrayList<vtkProp>();
    private ArrayList<vtkProp> propsExceptSmallBody = new ArrayList<vtkProp>();
    private HashMap<vtkProp, Model> propToModelMap = new HashMap<vtkProp, Model>();
//    private ArrayList<Model> allModels;
    private HashMap<String, Model> allModels = new HashMap<String, Model>();
    
    protected void setModels(HashMap<String, Model> models)
    {
    	allModels = models;

    	for (String modelName : allModels.keySet())
    	{
    		Model model = allModels.get(modelName);
    		model.addPropertyChangeListener(this);
    	}    	

    	updateProps();
    }
    
    public ArrayList<vtkProp> getProps()
	{
		return props;
	}

    public ArrayList<vtkProp> getPropsExceptSmallBody()
	{
		return propsExceptSmallBody;
	}

	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			updateProps();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}
	
	private void updateProps()
	{
		props.clear();
		propsExceptSmallBody.clear();
		propToModelMap.clear();

		for (String modelName : allModels.keySet())
		{
			Model model = allModels.get(modelName);
			if (model.isVisible())
			{
				props.addAll(model.getProps());

				for (vtkProp prop : model.getProps())
					propToModelMap.put(prop, model);

				if (!(model instanceof SmallBodyModel))
					propsExceptSmallBody.addAll(model.getProps());
			}
		}
	}

	public Model getModel(vtkProp prop)
	{
		return propToModelMap.get(prop);
	}
	
	public Model getModel(String modelName)
	{
		return allModels.get(modelName);
	}
	
	public abstract SmallBodyModel getSmallBodyModel();
}
