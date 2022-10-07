package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.rendering.DataActor;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import nom.tam.fits.FitsException;

public class PlannedInstrumentRendererManager
{
	List<PlannedInstrumentData> dataToRender = new ArrayList<PlannedInstrumentData>();
	private HashMap<PlannedInstrumentData, DataActor> plannedInstrumentDataToRendererMap = new HashMap<PlannedInstrumentData, DataActor>();

	/**
	 *
	 */
	private PropertyChangeSupport pcs;

	public PlannedInstrumentRendererManager(PropertyChangeSupport pcs)
	{
		this.pcs = pcs;
	}

	public DataActor addPlannedData(PlannedInstrumentData data, SmallBodyModel model)
	{
		// Get the actor for the planned data
		DataActor dataActor = null;
		try
		{
			dataActor = PlannedDataActorFactory.createPlannedDataActorFor(data, model);
		}
		catch (FitsException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		plannedInstrumentDataToRendererMap.put(data, dataActor);

		dataActor.SetVisibility(1);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, dataActor);
		return dataActor;
	}

	public void removePlannedData(PlannedInstrumentData data)
	{
		dataToRender.remove(data);
	}

	public void setVisibility(PlannedInstrumentData data, boolean isVisible)
	{
		data.setShowing(isVisible);
		DataActor actor = plannedInstrumentDataToRendererMap.get(data);
		actor.getFootprintBoundaryActor().SetVisibility(isVisible ? 1: 0);
		actor.getFootprintBoundaryActor().Modified();
		this.pcs.firePropertyChange("PLANNED_IMAGES_CHANGED", null, actor);
	}

	public void setFrustumVisibility(PlannedInstrumentData data, boolean isVisible)
	{
		data.setFrustumShowing(isVisible);
	}

}
