package edu.jhuapl.near.model.deimos;

import java.util.HashMap;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosModelManager extends ModelManager
{
	private DeimosModel deimosModel;
	private LineModel lineStructuresModel;
	private CircleModel circleStructuresModel;
	private PointModel pointStructuresModel;
	private RegularPolygonModel circleSelectionModel;
	private DeimosGraticule graticule;
    
    public DeimosModelManager()
    {
    	deimosModel = new DeimosModel();
    	lineStructuresModel = new LineModel(deimosModel);
    	circleStructuresModel = new CircleModel(deimosModel);
    	pointStructuresModel = new PointModel(deimosModel);
    	circleSelectionModel = new RegularPolygonModel(
    			deimosModel,
    			20,
    			false,
    			"Selection",
    			ModelNames.CIRCLE_SELECTION);
    	graticule = new DeimosGraticule(deimosModel);
    	
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.DEIMOS, deimosModel);
    	allModels.put(ModelNames.LINE_STRUCTURES, lineStructuresModel);
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, circleStructuresModel);
    	allModels.put(ModelNames.POINT_STRUCTURES, pointStructuresModel);
    	allModels.put(ModelNames.CIRCLE_SELECTION, circleSelectionModel);
    	allModels.put(ModelNames.GRATICULE, graticule);

    	setModels(allModels);
    }

	public SmallBodyModel getSmallBodyModel()
	{
		return deimosModel;
	}
}
