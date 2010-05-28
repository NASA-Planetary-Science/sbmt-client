package edu.jhuapl.near.model.deimos;

import java.util.ArrayList;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class DeimosModelManager extends ModelManager
{
	static public final String DEIMOS = "deimos";
	static public final String LINE_STRUCTURES = "line-structures";
	static public final String CIRCLE_STRUCTURES = "circle-structures";
	static public final String POINT_STRUCTURES = "point-structures";
	static public final String CIRCLE_SELECTION = "circle-selection";
	static public final String GRATICULE = "graticule";
	
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
    			"Selection");
    	graticule = new DeimosGraticule(deimosModel);
    	
        ArrayList<Model> allModels = new ArrayList<Model>();
    	allModels.add(deimosModel);
    	allModels.add(lineStructuresModel);
    	allModels.add(circleStructuresModel);
    	allModels.add(pointStructuresModel);
    	allModels.add(circleSelectionModel);
    	allModels.add(graticule);

    	setModels(allModels);
    }

	public Model getModel(String modelName)
	{
		if (DEIMOS.equals(modelName))
			return deimosModel;
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
		return deimosModel;
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
