package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import nom.tam.fits.FitsException;

public class PlannedInstrumentRendererManager
{
	List<PlannedInstrumentData> dataToRender = new ArrayList<PlannedInstrumentData>();
	private HashMap<PlannedInstrumentData, PlannedDataActor> plannedInstrumentDataToRendererMap = new HashMap<PlannedInstrumentData, PlannedDataActor>();

	/**
	 *
	 */
	private PropertyChangeSupport pcs;

	public PlannedInstrumentRendererManager(PropertyChangeSupport pcs)
	{
		// TODO Auto-generated constructor stub
		this.pcs = pcs;
	}

	public PlannedDataActor addPlannedData(PlannedInstrumentData data, SmallBodyModel model)
	{
//		if (plannedInstrumentDataToRendererMap.get(data) != null)
//		{
//			PlannedDataActor dataActor = plannedInstrumentDataToRendererMap.get(data);
//			dataActor.SetVisibility(1);
//			return dataActor;
//		}

		// Get the actor for the planned data
		PlannedDataActor dataActor = null;
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
//		dataActor.GetMapper().Update();

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
		System.out.println("PlannedInstrumentRendererManager: setVisibility: setting visibility to " + isVisible);
		data.setShowing(isVisible);
		plannedInstrumentDataToRendererMap.get(data).SetVisibility(isVisible ? 1: 0);
	}

	public void setFrustumVisibility(PlannedInstrumentData data, boolean isVisible)
	{
		data.setFrustumShowing(isVisible);
	}

}
