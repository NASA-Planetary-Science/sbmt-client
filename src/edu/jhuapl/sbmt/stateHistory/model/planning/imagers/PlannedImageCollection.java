package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedImageIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;

public class PlannedImageCollection extends BasePlannedDataCollection<PlannedImage> //SaavtkItemManager<PlannedImage>
		//implements PropertyChangeListener
{
	/**
	 *
	 */
//	private List<PlannedImage> plannedImages = new ArrayList<PlannedImage>();
//	private List<vtkProp> footprintActors = new ArrayList<vtkProp>();
//
//	private List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();
//
//	private PlannedInstrumentRendererManager renderManager;
//
//	private SmallBodyModel smallBodyModel;
//
//	private StateHistory stateHistorySource;

//	private CustomizableColoringDataManager coloringDataManager;

	public PlannedImageCollection(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
//		this.smallBodyModel = smallBodyModel;
////		this.coloringDataManager = smallBodyModel.getColoringDataManager();
//		renderManager = new PlannedInstrumentRendererManager(this.pcs);
//		createColoringData("Emission");
	}



	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			double time = (double)evt.getNewValue();
			if (stateHistorySource == null) return;
			for (PlannedDataActor actor : plannedDataActors)
			{
				if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
				{
//					System.out.println("PlannedImageCollection: propertyChange: static footprint not set at time " + TimeUtil.et2str(actor.getTime()));
					StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
				}
//				if (time > actor.getTime() && actor.getFootprintBoundaryActor().GetVisibility() == 0)
//					System.out.println("PlannedImageCollection: propertyChange: making visible for " + actor + " for time " + TimeUtil.et2str(time) + " at actor time " + TimeUtil.et2str(actor.getTime()));
				actor.getFootprintBoundaryActor().SetVisibility(time >= actor.getTime() ? 1 : 0);
				actor.getFootprintBoundaryActor().Modified();
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}
	}

//	private ColoringData createColoringData(String dataName)
//	{
//		String name = "Planned Coverage - " + dataName;
//		List<String> columnNames = new ArrayList<String>();
//		columnNames.add("Emission Angle");
//		String unit = "degrees";
//		int numberElements = smallBodyModel.getCellNormals().GetNumberOfTuples();
//		boolean hasNulls = true;
//		vtkFloatArray values = new vtkFloatArray();
//		values.SetNumberOfValues(numberElements);
//		values.SetNumberOfComponents(1);
//		values.FillComponent(0, 0);
//		IndexableTuple indexableTuple = ColoringDataUtils.createIndexableFromVtkArray(values);
//		ColoringData coloringData = ColoringDataFactory.of(name, unit, numberElements, columnNames, hasNulls, indexableTuple);
////		coloringData.getData().SetValue(0, 500);
//		nameToColoringMap.put(dataName, coloringData);
//		LoadableColoringData loadableColorData = ColoringDataFactory.of(coloringData, "PlannedCoverageEmission");
//		try
//		{
//			loadableColorData.save();
//		}
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		coloringDataManager.addCustom(loadableColorData);
//		return coloringData;
//
//	}

//	private void addDataToColoring(String dataName, int index, int value)
//	{
//		ColoringData data = nameToColoringMap.get(dataName);
//		data.getData().SetValue(index, value);
//	}

	/**
	 * @param run
	 */
	public void addImageToList(PlannedImage image)
	{
		plannedData.add(image);
		PerspectiveImageFootprint actor = (PerspectiveImageFootprint)addDataToRenderer(image);
		actor.setStaticFootprint(true);
		plannedDataActors.add(actor);
		if (this.stateHistorySource != null)
		{
			StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
		}
		footprintActors.add(actor.getFootprintBoundaryActor());
		setAllItems(plannedData);
		this.pcs.firePropertyChange("PLANNED_IMAGES_CHANGED", null, null);
	}

	public void loadPlannedImagesFromFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.loadPlannedImagesFromFileWithName(filename, this);
	}

	public void savePlannedImagesToFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.savePlannedImagesToFileWithName(filename, this);
	}
}