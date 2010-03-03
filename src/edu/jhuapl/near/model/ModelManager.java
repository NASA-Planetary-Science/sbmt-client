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
	//static public final String STRUCTURES = "structures";
	
	private LineamentModel lineamentModel;
	private MSIImageCollection msiImages;
	private ErosModel erosModel;
	private MSIBoundaryCollection msiBoundaries;
	private NISSpectraCollection nisSpectra;
	private NLRDataCollection nlrData;
	private LineModel lineModel;
	private CircleModel circleModel;
	//private StructureModel structureModel;
	
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
    	lineModel = new LineModel(erosModel);
    	circleModel = new CircleModel();
    	//structureModel = new StructureModel(erosModel);
    	
    	lineamentModel.addPropertyChangeListener(this);
    	erosModel.addPropertyChangeListener(this);
    	msiImages.addPropertyChangeListener(this);
    	msiBoundaries.addPropertyChangeListener(this);
    	nisSpectra.addPropertyChangeListener(this);
    	nlrData.addPropertyChangeListener(this);
    	lineModel.addPropertyChangeListener(this);
    	circleModel.addPropertyChangeListener(this);
    	//structureModel.addPropertyChangeListener(this);
    	
    	allModels = new ArrayList<Model>();
    	allModels.add(erosModel);
    	allModels.add(lineamentModel);
    	allModels.add(msiImages);
    	allModels.add(msiBoundaries);
    	allModels.add(nisSpectra);
    	allModels.add(nlrData);
    	allModels.add(lineModel);
    	allModels.add(circleModel);
    	//allModels.add(structureModel);
    	
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

	public void propertyChange(PropertyChangeEvent arg0) 
	{
		updateProps();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
			return lineModel;
		else if (CIRCLE_STRUCTURES.equals(modelName))
			return circleModel;
		//else if (STRUCTURES.equals(modelName))
		//	return structureModel;
		else
			return null;
	}
}
