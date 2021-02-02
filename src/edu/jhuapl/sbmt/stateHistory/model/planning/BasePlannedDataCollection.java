package edu.jhuapl.sbmt.stateHistory.model.planning;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import vtk.vtkProp;

import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedDataActor;
import edu.jhuapl.sbmt.stateHistory.rendering.planning.PlannedInstrumentRendererManager;

import glum.item.ItemEventType;

public class BasePlannedDataCollection<T extends PlannedInstrumentData> extends SaavtkItemManager<T> implements PropertyChangeListener
{

	protected List<T> plannedData = new ArrayList<T>();
	protected List<vtkProp> footprintActors = new ArrayList<vtkProp>();

	protected List<PlannedDataActor> plannedDataActors = new ArrayList<PlannedDataActor>();

	protected PlannedInstrumentRendererManager renderManager;

	protected SmallBodyModel smallBodyModel;

	protected StateHistory stateHistorySource;

	public BasePlannedDataCollection(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
		renderManager = new PlannedInstrumentRendererManager(this.pcs);
	}

	@Override
	public List<vtkProp> getProps()
	{
		return footprintActors;
	}

	public void notify(Object obj, ItemEventType type)
	{
		notifyListeners(obj, type);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
		{
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	/**
	 * @param run
	 * @return
	 */
	public PlannedDataActor addDataToRenderer(T data)
	{
		return renderManager.addPlannedData(data, smallBodyModel);
	}

	/**
	 * @param key
	 */
	public void removeDataFromRenderer(T data)
	{
		renderManager.removePlannedData(data);
	}

	/**
	 * @param keys
	 */
	public void removeRuns(T[] keys)
	{
		for (T key : keys)
		{
			removeDataFromRenderer(key);
		}
	}

	/**
	 *
	 */
	@Override
	public ImmutableList<T> getAllItems()
	{
		return ImmutableList.copyOf(plannedData);
	}

	/**
	 *
	 */
	@Override
	public int getNumItems()
	{
		return plannedData.size();
	}

	/**
	 * @param history
	 */
	public void setOthersHiddenExcept(List<T> plannedData)
	{
		for (T image : getAllItems())
		{
			renderManager.setVisibility(image, plannedData.contains(image));
		}
	}

	public void updateStateHistorySource(StateHistory stateHistory)
	{
		this.stateHistorySource = stateHistory;
	}

	public void setDataShowing(T plannedDataObject, boolean visible)
	{
		renderManager.setVisibility(plannedDataObject, visible);
	}
}
