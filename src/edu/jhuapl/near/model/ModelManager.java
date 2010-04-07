package edu.jhuapl.near.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.near.util.Properties;

import vtk.*;

public class ModelManager extends Model implements PropertyChangeListener
{
	static public final String LINEAMENT = "lineament";
	static public final String MSI_IMAGES = "msi-images";
	static public final String EROS= "eros";
	static public final String MSI_BOUNDARY = "msi-boundary";
	static public final String NIS_SPECTRA = "nis-spectra";
	static public final String NLR_DATA = "nlr-data";
	static public final String LINE_STRUCTURES = "line-structures";
	static public final String CIRCLE_STRUCTURES = "circle-structures";
	static public final String POINT_STRUCTURES = "point-structures";
	static public final String CIRCLE_SELECTION = "circle-selection";
	
	private LineamentModel lineamentModel;
	private MSIImageCollection msiImages;
	private ErosModel erosModel;
	private MSIBoundaryCollection msiBoundaries;
	private NISSpectraCollection nisSpectra;
	private NLRDataCollection nlrData;
	private LineModel lineStructuresModel;
	private CircleModel circleStructuresModel;
	private PointModel pointStructuresModel;
	private RegularPolygonModel circleSelectionModel;
	
    private ArrayList<vtkProp> props = new ArrayList<vtkProp>();
    private ArrayList<vtkProp> propsExceptEros = new ArrayList<vtkProp>();
    private HashMap<vtkProp, Model> propToModelMap = new HashMap<vtkProp, Model>();
    
    private ArrayList<Model> allModels;
    
    public ModelManager()
    {
    	lineamentModel = new LineamentModel();
    	erosModel = new ErosModel();
    	msiImages = new MSIImageCollection();
    	msiBoundaries = new MSIBoundaryCollection();
    	nisSpectra = new NISSpectraCollection(erosModel);
    	nlrData = new NLRDataCollection();
    	lineStructuresModel = new LineModel(erosModel);
    	circleStructuresModel = new CircleModel(erosModel);
    	pointStructuresModel = new PointModel(erosModel);
    	circleSelectionModel = new RegularPolygonModel(
    			erosModel,
    			20,
    			false,
    			"Selection");
    	
    	allModels = new ArrayList<Model>();
    	allModels.add(erosModel);
    	allModels.add(lineamentModel);
    	allModels.add(msiImages);
    	allModels.add(msiBoundaries);
    	allModels.add(nisSpectra);
    	allModels.add(nlrData);
    	allModels.add(lineStructuresModel);
    	allModels.add(circleStructuresModel);
    	allModels.add(pointStructuresModel);
    	allModels.add(circleSelectionModel);

    	for (Model model : allModels)
    		model.addPropertyChangeListener(this);
    	
		updateProps();
    }

    public ArrayList<vtkProp> getProps()
	{
		return props;
	}

    public ArrayList<vtkProp> getPropsExceptEros()
	{
		return propsExceptEros;
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
		propsExceptEros.clear();
		propToModelMap.clear();

		for (Model model : allModels)
		{
			props.addAll(model.getProps());
						
			for (vtkProp prop : model.getProps())
	    		propToModelMap.put(prop, model);
			
			if (!(model instanceof ErosModel))
				propsExceptEros.addAll(model.getProps());
		}
	}

	public Model getModel(vtkProp prop)
	{
		return propToModelMap.get(prop);
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
		else if (NLR_DATA.equals(modelName))
			return nlrData;
		else if (LINE_STRUCTURES.equals(modelName))
			return lineStructuresModel;
		else if (CIRCLE_STRUCTURES.equals(modelName))
			return circleStructuresModel;
		else if (POINT_STRUCTURES.equals(modelName))
			return pointStructuresModel;
		else if (CIRCLE_SELECTION.equals(modelName))
			return circleSelectionModel;
		else
			return null;
	}
}
