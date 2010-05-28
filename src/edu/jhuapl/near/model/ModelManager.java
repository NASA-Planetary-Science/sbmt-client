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
    private ArrayList<Model> allModels;
    
    public ModelManager()
    {
    }

    protected void setModels(ArrayList<Model> models)
    {
    	allModels = models;

    	for (Model model : allModels)
    		model.addPropertyChangeListener(this);
    	
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

		for (Model model : allModels)
		{
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
	
	public abstract Model getModel(String modelName);
	
	public abstract SmallBodyModel getSmallBodyModel();

	public abstract Graticule getGraticuleModel();

	public abstract LineModel getLineStructuresModel();

	public abstract CircleModel getCircleStructuresModel();

	public abstract PointModel getPointStructuresModel();

	public abstract RegularPolygonModel getCircleSelectionModel();
}
