package edu.jhuapl.near.model.itokawa;

import java.util.HashMap;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class ItokawaModelManager extends ModelManager
{
	private ItokawaModel itokawaModel;
	private LineModel lineStructuresModel;
	private CircleModel circleStructuresModel;
	private PointModel pointStructuresModel;
	private RegularPolygonModel circleSelectionModel;
	private ItokawaGraticule graticule;
    
    public ItokawaModelManager()
    {
    	itokawaModel = new ItokawaModel();
    	lineStructuresModel = new LineModel(itokawaModel);
    	circleStructuresModel = new CircleModel(itokawaModel);
    	pointStructuresModel = new PointModel(itokawaModel);
    	circleSelectionModel = new RegularPolygonModel(
    			itokawaModel,
    			20,
    			false,
    			"Selection",
    			ModelNames.CIRCLE_SELECTION);
    	graticule = new ItokawaGraticule(itokawaModel);
    	
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.ITOKAWA, itokawaModel);
    	allModels.put(ModelNames.LINE_STRUCTURES, lineStructuresModel);
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, circleStructuresModel);
    	allModels.put(ModelNames.POINT_STRUCTURES, pointStructuresModel);
    	allModels.put(ModelNames.CIRCLE_SELECTION, circleSelectionModel);
    	allModels.put(ModelNames.GRATICULE, graticule);

    	setModels(allModels);
    }

	public SmallBodyModel getSmallBodyModel()
	{
		return itokawaModel;
	}
}
