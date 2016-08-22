package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import vtk.vtkProp;
import vtk.vtksbCellLocator;

import edu.jhuapl.near.util.Properties;

public abstract class AbstractModelManager extends DefaultDatasourceModel implements ModelManager, PropertyChangeListener
{
    private ArrayList<vtkProp> props = new ArrayList<vtkProp>();
    private ArrayList<vtkProp> propsExceptSmallBody = new ArrayList<vtkProp>();
    private HashMap<vtkProp, Model> propToModelMap = new HashMap<vtkProp, Model>();
    private HashMap<ModelNames, Model> allModels = new HashMap<ModelNames, Model>();
    private boolean mode2D = false;



    @Override
    public void updateScaleBarValue(double pixelSizeInKm)
    {
    }

    @Override
    public void updateScaleBarPosition(int windowWidth, int windowHeight)
    {
    }

    @Override
    public vtksbCellLocator getCellLocator()
    {
        return null;
    }

    public boolean isBuiltIn()
    {
        return getSmallBodyModel().isBuiltIn();
    }

    public void setModels(HashMap<ModelNames, Model> models)
    {
        allModels.clear();

        CommonData commonData = new CommonData();
        setCommonData(commonData);

        for (ModelNames modelName : models.keySet())
        {
            Model model = models.get(modelName);
            model.addPropertyChangeListener(this);
            model.setCommonData(commonData);
            allModels.put(modelName, model);
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

    protected abstract void updateProps();

    public Model getModel(vtkProp prop)
    {
        return propToModelMap.get(prop);
    }

    public Model getModel(ModelNames modelName)
    {
        return allModels.get(modelName);
    }

    abstract public PolyhedralModel getSmallBodyModel();

    public void deleteAllModels()
    {
        for (ModelNames modelName : allModels.keySet())
            allModels.get(modelName).delete();
    }

    public void set2DMode(boolean enable)
    {
        mode2D = enable;

        for (ModelNames modelName : allModels.keySet())
            allModels.get(modelName).set2DMode(enable);
    }

    public boolean is2DMode()
    {
        return mode2D;
    }
}
