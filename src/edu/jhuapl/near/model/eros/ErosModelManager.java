package edu.jhuapl.near.model.eros;

import java.util.ArrayList;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.Graticule;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class ErosModelManager extends ModelManager
{
	static public final String LINEAMENT = "lineament";
	static public final String MSI_IMAGES = "msi-images";
	static public final String EROS = "eros";
	static public final String MSI_BOUNDARY = "msi-boundary";
	static public final String NIS_SPECTRA = "nis-spectra";
	static public final String NLR_DATA = "nlr-data";
	static public final String LINE_STRUCTURES = "line-structures";
	static public final String CIRCLE_STRUCTURES = "circle-structures";
	static public final String POINT_STRUCTURES = "point-structures";
	static public final String CIRCLE_SELECTION = "circle-selection";
	static public final String GRATICULE = "graticule";
	
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
	private ErosGraticule graticule;
    
    public ErosModelManager()
    {
    	lineamentModel = new LineamentModel();
    	erosModel = new ErosModel();
    	msiImages = new MSIImageCollection(erosModel);
    	msiBoundaries = new MSIBoundaryCollection(erosModel);
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
    	graticule = new ErosGraticule(erosModel);
    	
        ArrayList<Model> allModels = new ArrayList<Model>();
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
    	allModels.add(graticule);

    	setModels(allModels);
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
		else if (GRATICULE.equals(modelName))
				return graticule;
		else
			return null;
	}

	public SmallBodyModel getSmallBodyModel()
	{
		return erosModel;
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
