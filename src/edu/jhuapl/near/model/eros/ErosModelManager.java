package edu.jhuapl.near.model.eros;

import java.util.HashMap;

import edu.jhuapl.near.model.CircleModel;
import edu.jhuapl.near.model.LineModel;
import edu.jhuapl.near.model.Model;
import edu.jhuapl.near.model.ModelManager;
import edu.jhuapl.near.model.ModelNames;
import edu.jhuapl.near.model.PointModel;
import edu.jhuapl.near.model.RegularPolygonModel;
import edu.jhuapl.near.model.SmallBodyModel;

public class ErosModelManager extends ModelManager
{
	private LineamentModel lineamentModel;
	private MSIImageCollection msiImages;
	private ErosModel erosModel;
	private MSIBoundaryCollection msiBoundaries;
	private NISSpectraCollection nisSpectra;
	private NLRBrowseDataCollection nlrDataBrowse;
	private NLRSearchDataCollection nlrDataSearch;
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
    	nlrDataBrowse = new NLRBrowseDataCollection();
    	nlrDataSearch = new NLRSearchDataCollection(erosModel);
    	lineStructuresModel = new LineModel(erosModel);
    	circleStructuresModel = new CircleModel(erosModel);
    	pointStructuresModel = new PointModel(erosModel);
    	circleSelectionModel = new RegularPolygonModel(
    			erosModel,
    			20,
    			false,
    			"Selection",
    			ModelNames.CIRCLE_SELECTION);
    	graticule = new ErosGraticule(erosModel);
    	
        HashMap<String, Model> allModels = new HashMap<String, Model>();
        allModels.put(ModelNames.EROS, erosModel);
    	allModels.put(ModelNames.LINEAMENT, lineamentModel);
        allModels.put(ModelNames.MSI_IMAGES, msiImages);
        allModels.put(ModelNames.MSI_BOUNDARY, msiBoundaries);
        allModels.put(ModelNames.NIS_SPECTRA, nisSpectra);
        allModels.put(ModelNames.NLR_DATA_BROWSE, nlrDataBrowse);
        allModels.put(ModelNames.NLR_DATA_SEARCH, nlrDataSearch);
    	allModels.put(ModelNames.LINE_STRUCTURES, lineStructuresModel);
    	allModels.put(ModelNames.CIRCLE_STRUCTURES, circleStructuresModel);
    	allModels.put(ModelNames.POINT_STRUCTURES, pointStructuresModel);
    	allModels.put(ModelNames.CIRCLE_SELECTION, circleSelectionModel);
    	allModels.put(ModelNames.GRATICULE, graticule);

    	setModels(allModels);
    }

	public SmallBodyModel getSmallBodyModel()
	{
		return erosModel;
	}
}
