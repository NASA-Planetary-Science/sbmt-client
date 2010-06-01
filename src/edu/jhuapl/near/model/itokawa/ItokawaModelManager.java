package edu.jhuapl.near.model.itokawa;

import java.util.ArrayList;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class ItokawaModelManager extends ModelManager
{
	static public final String ITOKAWA = "itokawa";
	static public final String LINE_STRUCTURES = "line-structures";
	static public final String CIRCLE_STRUCTURES = "circle-structures";
	static public final String POINT_STRUCTURES = "point-structures";
	static public final String CIRCLE_SELECTION = "circle-selection";
	static public final String GRATICULE = "graticule";
	
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
    			"Selection");
    	graticule = new ItokawaGraticule(itokawaModel);
    	
        ArrayList<Model> allModels = new ArrayList<Model>();
    	allModels.add(itokawaModel);
    	allModels.add(lineStructuresModel);
    	allModels.add(circleStructuresModel);
    	allModels.add(pointStructuresModel);
    	allModels.add(circleSelectionModel);
    	allModels.add(graticule);

    	setModels(allModels);
    }

	public Model getModel(String modelName)
	{
		if (ITOKAWA.equals(modelName))
			return itokawaModel;
		else if (LINE_STRUCTURES.equals(modelName))
			return lineStructuresModel;
		else if (CIRCLE_STRUCTURES.equals(modelName))
			return circleStructuresModel;
		else if (POINT_STRUCTURES.equals(modelName))
			return pointStructuresModel;
		else if (CIRCLE_SELECTION.equals(modelName))
			return circleSelectionModel;
		else if (GRATICULE.equals(modelName))
				return graticule;
		else
			return null;
	}

	public SmallBodyModel getSmallBodyModel()
	{
		return itokawaModel;
	}

	public Graticule getGraticuleModel()
	{
		return graticule;
	}

	public LineModel getLineStructuresModel()
	{
		return lineStructuresModel;
	}

	public CircleModel getCircleStructuresModel()
	{
		return circleStructuresModel;
	}

	public PointModel getPointStructuresModel()
	{
		return pointStructuresModel;
	}

	public RegularPolygonModel getCircleSelectionModel()
	{
		return circleSelectionModel;
	}
}
