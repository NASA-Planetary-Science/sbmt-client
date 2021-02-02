package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedImageIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;

public class PlannedImageCollection extends BasePlannedDataCollection<PlannedImage>
{

//	private CustomizableColoringDataManager coloringDataManager;

	private double time;

	public PlannedImageCollection(SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
////		this.coloringDataManager = smallBodyModel.getColoringDataManager();
//		createColoringData("Emission");
	}



	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			time = (double)evt.getNewValue();
			updateFootprints();
		}
	}

	public void updateFootprints()
	{
		if (stateHistorySource == null) return;
		for (PlannedDataActor actor : plannedDataActors)
		{
			if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
			{
				StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
			}
			actor.getFootprintBoundaryActor().SetVisibility(time >= actor.getTime() ? 1 : 0);
			plannedData.get(plannedDataActors.indexOf(actor)).setShowing(time >= actor.getTime());
			actor.getFootprintBoundaryActor().Modified();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
	public void addImageToList(PlannedImage image, ProgressStatusListener listener)
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
		listener.setProgressStatus("Adding image " + plannedData.size(), 0);
		setAllItems(plannedData);
		this.pcs.firePropertyChange("PLANNED_IMAGES_CHANGED", null, null);
	}

	public void loadPlannedImagesFromFileWithName(String filename, ProgressStatusListener listener, Runnable completion) throws IOException
	{
		PlannedImageIOHelper.loadPlannedImagesFromFileWithName(filename, this, listener, completion);
	}

	public void savePlannedImagesToFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.savePlannedImagesToFileWithName(filename, this);
	}
}