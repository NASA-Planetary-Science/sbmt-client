package edu.jhuapl.sbmt.image2.modules.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Range;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image2.model.ImageOrigin;
import edu.jhuapl.sbmt.image2.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image2.model.PerspectiveImage;
import edu.jhuapl.sbmt.image2.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.IImagingInstrument;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification.Selection;
import edu.jhuapl.sbmt.query.database.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;

public class ImageSearchOperator extends BasePipelineOperator<ImageSearchParametersModel, IPerspectiveImage>
{
	private ImageSearchParametersModel searchParameterModel;
	private SmallBodyViewConfig viewConfig;
	private ModelManager modelManager;
//	private List<PerspectiveImage> images = Lists.newArrayList();

	public ImageSearchOperator(SmallBodyViewConfig viewConfig, ModelManager modelManager)
	{

		this.modelManager = modelManager;
		this.viewConfig = viewConfig;
	}


	@Override
	public void processData() throws IOException, Exception
	{
		this.searchParameterModel = inputs.get(0);
		runSearch();
	}

	private void runSearch()
	{
		double minDistanceQuery = searchParameterModel.getMinDistanceQuery();
		double maxDistanceQuery = searchParameterModel.getMaxDistanceQuery();
		double minEmissionQuery = searchParameterModel.getMinEmissionQuery();
		double maxEmissionQuery = searchParameterModel.getMaxEmissionQuery();
		double minIncidenceQuery = searchParameterModel.getMinIncidenceQuery();
		double maxIncidenceQuery = searchParameterModel.getMaxIncidenceQuery();
		double minPhaseQuery = searchParameterModel.getMinPhaseQuery();
		double maxPhaseQuery = searchParameterModel.getMaxPhaseQuery();
		double minResolutionQuery = searchParameterModel.getMinResolutionQuery();
		double maxResolutionQuery = searchParameterModel.getMaxResolutionQuery();
		List<Integer> camerasSelected = searchParameterModel.getCamerasSelected();
		List<Integer> filtersSelected = searchParameterModel.getFiltersSelected();
		int selectedLimbIndex = searchParameterModel.getSelectedLimbIndex();
		String searchFilename = searchParameterModel.getSearchFilename();
		boolean excludeGaskell = searchParameterModel.isExcludeGaskell();
		IImagingInstrument instrument = searchParameterModel.getInstrument();

		GregorianCalendar startDateGreg = new GregorianCalendar();
        GregorianCalendar endDateGreg = new GregorianCalendar();
        startDateGreg.setTime(searchParameterModel.getStartDate());
        endDateGreg.setTime(searchParameterModel.getEndDate());
        DateTime startDateJoda = new DateTime(
                startDateGreg.get(GregorianCalendar.YEAR),
                startDateGreg.get(GregorianCalendar.MONTH)+1,
                startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                startDateGreg.get(GregorianCalendar.MINUTE),
                startDateGreg.get(GregorianCalendar.SECOND),
                startDateGreg.get(GregorianCalendar.MILLISECOND),
                DateTimeZone.UTC);
        DateTime endDateJoda = new DateTime(
                endDateGreg.get(GregorianCalendar.YEAR),
                endDateGreg.get(GregorianCalendar.MONTH)+1,
                endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                endDateGreg.get(GregorianCalendar.MINUTE),
                endDateGreg.get(GregorianCalendar.SECOND),
                endDateGreg.get(GregorianCalendar.MILLISECOND),
                DateTimeZone.UTC);

        TreeSet<Integer> cubeList = null;
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        if (selectionModel.getNumItems() > 0)
        {
            int numberOfSides = selectionModel.getNumberOfSides();
            Ellipse region = selectionModel.getItem(0);

            // Always use the lowest resolution model for getting the intersection cubes list.
            // Therefore, if the selection region was created using a higher resolution model,
            // we need to recompute the selection region using the low res model.
            if (smallBodyModel.getModelResolution() > 0)
            {
                vtkPolyData interiorPoly = new vtkPolyData();
                smallBodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(), numberOfSides, interiorPoly, null);
                cubeList = smallBodyModel.getIntersectingCubes(interiorPoly);
            }
            else
            {
                cubeList = smallBodyModel.getIntersectingCubes(selectionModel.getVtkInteriorPolyDataFor(region));
            }
            smallBodyModel.setCubeVisibility(cubeList);
            smallBodyModel.calculateCubeSize(false, 0.0);
            smallBodyModel.clearCubes();
        }

        ImageSource imageSource = searchParameterModel.getImageSourceOfLastQuery(); // ImageSource.valueOf(((Enum)panel.getSourceComboBox().getSelectedItem()).name());
        // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
        boolean sumOfProductsSearch;
//        List<Integer> camerasSelected;
//        List<Integer> filtersSelected;
        SmallBodyViewConfig smallBodyConfig = viewConfig;
        if(smallBodyConfig.hasHierarchicalImageSearch)
        {
            // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
            sumOfProductsSearch = true;
            Selection selection = smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections();
            camerasSelected = selection.getSelectedCameras();
            filtersSelected = selection.getSelectedFilters();
        }
        else
        {
            // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
            sumOfProductsSearch = false;
        }
        List<List<String>> results = null;
        if (instrument.getSearchQuery() instanceof FixedListQuery)
        {
            FixedListQuery query = (FixedListQuery) instrument.getSearchQuery();
            results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource, searchFilename)).getResultlist();
        }
        else
        {
            // Run queries based on user specifications
            ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                    Range.closed(minDistanceQuery, maxDistanceQuery),
                    searchFilename, null,
                    Range.closed(minIncidenceQuery, maxIncidenceQuery),
                    Range.closed(minEmissionQuery, maxEmissionQuery),
                    Range.closed(minPhaseQuery, maxPhaseQuery),
                    sumOfProductsSearch, camerasSelected, filtersSelected,
                    Range.closed(minResolutionQuery, maxResolutionQuery),
                    cubeList, imageSource, selectedLimbIndex);
            results = searchParameterModel.getInstrument().getSearchQuery().runQuery(searchMetadata).getResultlist();
       }

        // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
        // then remove from the list images which are contained in the other list by doing
        // an additional search.
        if (imageSource == ImageSource.SPICE && excludeGaskell)
        {
            List<List<String>> resultsOtherSource = null;
            if (instrument.getSearchQuery() instanceof FixedListQuery)
            {
                FixedListQuery query = (FixedListQuery)instrument.getSearchQuery();
                resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource)).getResultlist();
            }
            else
            {

                ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                        Range.closed(minDistanceQuery, maxDistanceQuery),
                        searchFilename, null,
                        Range.closed(minIncidenceQuery, maxIncidenceQuery),
                        Range.closed(minEmissionQuery, maxEmissionQuery),
                        Range.closed(minPhaseQuery, maxPhaseQuery),
                        sumOfProductsSearch, camerasSelected, filtersSelected,
                        Range.closed(minResolutionQuery, maxResolutionQuery),
                        cubeList, imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE, selectedLimbIndex);

                    resultsOtherSource = instrument.getSearchQuery().runQuery(searchMetadataOther).getResultlist();

            }

            int numOtherResults = resultsOtherSource.size();
            for (int i=0; i<numOtherResults; ++i)
            {
                String imageName = resultsOtherSource.get(i).get(0);
                int numResults = results.size();
                for (int j=0; j<numResults; ++j)
                {
                    if (results.get(j).get(0).startsWith(imageName))
                    {
                        results.remove(j);
                        break;
                    }
                }
            }
        }

        outputs = new ArrayList<IPerspectiveImage>();
        int i=1;
        for (List<String> imageInfo : results)
        {
//        	HashMap<ImageSource, String> pointingSources = new HashMap<ImageSource, String>();
        	//TODO make this generic to handle the available server directory structures
//        	pointingSources.put(ImageSource.SPICE, FilenameUtils.removeExtension(imageInfo.get(0)).replace("images/public", "infofiles") + ".INFO");
//        	pointingSources.put(ImageSource.GASKELL, FilenameUtils.removeExtension(imageInfo.get(0)).replace("images/public", "sumfiles") + ".SUM");
//        	pointingSources.put(ImageSource.LABEL, FilenameUtils.removeExtension(imageInfo.get(0)).replace("images/public", "labelfiles") + ".LBL");


        	String extension = ".INFO";
        	String pointingDir = "infofiles";
        	if (imageSource == ImageSource.GASKELL || imageSource == ImageSource.GASKELL_UPDATED)
    		{
        		extension = ".SUM";
        		pointingDir = "sumfiles";
        		if (viewConfig.getUniqueName().contains("Eros")) pointingDir = "sumfiles_to_be_delivered";
    		}
        	if (imageSource == ImageSource.LABEL)
    		{
        		extension = ".LBL";
        		pointingDir = "labels";
    		}

        	String imagePath = "images";
        	if (viewConfig.getUniqueName().contains("Bennu")) imagePath = "images/public";

        	String infoBaseName = FilenameUtils.removeExtension(imageInfo.get(0)).replace(imagePath, pointingDir);
        	if (viewConfig.getUniqueName().contains("Eros"))
    		{
        		String filename = FilenameUtils.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
            	String filenamePrefix = filename.substring(0, filename.indexOf("_"));
        		infoBaseName = infoBaseName.replace(filename, filenamePrefix.substring(0, filenamePrefix.length()-2));
    		}

        	PerspectiveImage image = new PerspectiveImage(imageInfo.get(0), instrument.getType(), imageSource, infoBaseName + extension, new double[] {});
        	image.setFlip(instrument.getFlip());
//        	image.setFlip("Y");
        	image.setRotation(instrument.getRotation());
//        	image.setRotation(90.0);
        	image.setImageOrigin(ImageOrigin.SERVER);
        	//TODO should be replaced with parameters from ImagingInstrument
        	image.setLinearInterpolatorDims(new int[] {537, 412});
        	image.setMaskValues(new int[] {2, 14, 2, 14});
//        	image.setFillValues(instrument.getFillDetector(image));
        	image.setLongTime(Long.parseLong(imageInfo.get(1)));
//        	image.setIndex(i++);
        	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
        	compImage.setIndex(i++);
        	outputs.add(compImage);
        }
	}
}
