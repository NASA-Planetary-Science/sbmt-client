package edu.jhuapl.sbmt.stateHistory.model.planning.imagers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkFloatArray;
import vtk.vtkProp;

import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.model.image.perspectiveImage.PerspectiveImageFootprint;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.PlannedImageIOHelper;
import edu.jhuapl.sbmt.stateHistory.rendering.PlannedDataProperties;
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryPositionCalculator;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentRendererManager;

import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import glum.item.ItemEventType;

public class PlannedImageCollection extends SaavtkItemManager<PlannedImage>
		implements PropertyChangeListener, MetadataManager
{
	/**
	 *
	 */
	private List<PlannedImage> plannedImages = new ArrayList<PlannedImage>();
	private List<vtkProp> footprintActors = new ArrayList<vtkProp>();

	private List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();

	private HashMap<String, ColoringData> nameToColoringMap = new HashMap<String, ColoringData>();

	private PlannedInstrumentRendererManager renderManager;

	private SmallBodyModel smallBodyModel;

	private StateHistory stateHistorySource;

	private CustomizableColoringDataManager coloringDataManager;

	public PlannedImageCollection(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
		this.coloringDataManager = smallBodyModel.getColoringDataManager();
		renderManager = new PlannedInstrumentRendererManager(this.pcs);
		createColoringData("Emission");
	}

	@Override
	public List<vtkProp> getProps()
	{
		if (!footprintActors.isEmpty()) return footprintActors;
		for (PlannedDataActor actor : plannedDataActors)
		{
			footprintActors.add(actor.getFootprintBoundaryActor());

		}
		return footprintActors;
	}

	@Override
	public void retrieve(Metadata arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Metadata store()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}

		if (PlannedDataProperties.TIME_CHANGED.equals(evt.getPropertyName()))
		{
			double time = (double)evt.getNewValue();
			if (stateHistorySource == null) return;
			for (PlannedDataActor actor : plannedDataActors)
			{
				if (((PerspectiveImageFootprint)actor).isStaticFootprintSet() == false)
					StateHistoryPositionCalculator.updateFootprintPointing(stateHistorySource, actor.getTime(), (PerspectiveImageFootprint)actor);
				actor.getFootprintBoundaryActor().SetVisibility(time > actor.getTime() ? 1 : 0);
			}
		}
	}

	private ColoringData createColoringData(String dataName)
	{
		String name = "Planned Coverage - " + dataName;
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("Emission Angle");
		String unit = "degrees";
		int numberElements = smallBodyModel.getCellNormals().GetNumberOfTuples();
		System.out.println("PlannedImageCollection: createColoringData: number of elements " + numberElements);
		boolean hasNulls = true;
		vtkFloatArray values = new vtkFloatArray();
		values.SetNumberOfValues(numberElements);
		ColoringData coloringData = ColoringData.of(name, columnNames, unit, numberElements, hasNulls, values);
		coloringData.getData().SetValue(0, 500);
		nameToColoringMap.put(dataName, coloringData);
		coloringDataManager.addCustom(coloringData);
		return coloringData;

	}

	private void addDataToColoring(String dataName, int index, int value)
	{
		ColoringData data = nameToColoringMap.get(dataName);
		data.getData().SetValue(index, value);
	}

	/**
	 * @param run
	 */
	public void addImageToList(PlannedImage image)
	{
		plannedImages.add(image);
		PerspectiveImageFootprint actor = (PerspectiveImageFootprint)addImageToRenderer(image);
		actor.setStaticFootprint(true);
		plannedDataActors.add(actor);
		setAllItems(plannedImages);
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	/**
	 * @param run
	 * @return
	 */
	public PlannedDataActor addImageToRenderer(PlannedImage image)
	{
		return renderManager.addPlannedData(image, smallBodyModel);
	}

	/**
	 * @param key
	 */
	public void removeImageFromRenderer(PlannedImage image)
	{
		renderManager.removePlannedData(image);
	}

	/**
	 * @param keys
	 */
	public void removeRuns(PlannedImage[] keys)
	{
		for (PlannedImage key : keys)
		{
			removeImageFromRenderer(key);
		}
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<PlannedImage> getAllItems()
	{
		return ImmutableList.copyOf(plannedImages);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedImages.size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<PlannedImage> plannedImages)
	{
		for (PlannedImage image : getAllItems())
		{
			renderManager.setVisibility(image, plannedImages.contains(image));
		}
	}

	public void loadPlannedImagesFromFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.loadPlannedImagesFromFileWithName(filename, this);
	}

	public void savePlannedImagesToFileWithName(String filename) throws IOException
	{
		PlannedImageIOHelper.savePlannedImagesToFileWithName(filename, this);
	}

	public void updateStateHistorySource(StateHistory stateHistory)
	{
		this.stateHistorySource = stateHistory;
//		for (PlannedDataActor dataActor : plannedDataActors)
//		{
//			StateHistoryPositionCalculator.updateFootprintPointing(stateHistory, dataActor.getTime(), (PerspectiveImageFootprint)dataActor);
//		}
	}

}
