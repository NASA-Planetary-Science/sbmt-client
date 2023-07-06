package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.beans.PropertyChangeEvent;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.PerspectiveFootprint;
import edu.jhuapl.sbmt.stateHistory.model.planning.BasePlannedDataCollection;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;

public class PlannedImageCollection extends BasePlannedDataCollection<PlannedImage>
{

//	private CustomizableColoringDataManager coloringDataManager;

	public PlannedImageCollection(String filename, SmallBodyModel smallBodyModel)
	{
		super(smallBodyModel);
		this.filename = filename;
////		this.coloringDataManager = smallBodyModel.getColoringDataManager();
//		createColoringData("Emission");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		super.propertyChange(evt);
		if (!PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName())) return;
		time = (double)evt.getNewValue();
		updateFootprints();
	}

	@Override
	public void updateFootprints()
	{
		if (stateHistorySource == null) return;
		for (PlannedImage data : plannedData)
		{
			PerspectiveFootprint actor = (PerspectiveFootprint)plannedDataActors.get(plannedData.indexOf(data));
			if (((PerspectiveFootprint)actor).isStaticFootprintSet() == false)
			{
				StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveFootprint)actor);
			}
			setDataShowing(data, (time >= actor.getTime()) && (showing));
		}
	}

	/**
	 * @param run
	 */
	public void addImageToList(PlannedImage image, ProgressStatusListener listener)
	{
		plannedData.add(image);
		PerspectiveFootprint actor = (PerspectiveFootprint)addDataToRenderer(image);
		actor.setStaticFootprint(true);
		plannedDataActors.add(actor);
		if (this.stateHistorySource != null)
		{
			StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveFootprint)actor);
		}
		footprintActors.add(actor.getFootprintBoundaryActor());
		listener.setProgressStatus("Adding image " + plannedData.size(), 0);
		setAllItems(plannedData);
//		this.pcs.firePropertyChange("PLANNED_IMAGES_CHANGED", null, null);
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

}