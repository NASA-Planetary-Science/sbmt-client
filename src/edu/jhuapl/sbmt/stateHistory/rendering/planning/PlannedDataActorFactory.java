package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.io.IOException;
import java.util.HashMap;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import nom.tam.fits.FitsException;

public class PlannedDataActorFactory
{
	static HashMap<String, IPlanningDataActorBuilder<PlannedInstrumentData>> registeredModels
			= new HashMap<String, IPlanningDataActorBuilder<PlannedInstrumentData>>();


	static public void registerModel(String uniqueName, IPlanningDataActorBuilder<PlannedInstrumentData>  builder)
	{
		registeredModels.put(uniqueName, builder);
	}

	static public PlannedDataActor createPlannedDataActorFor(PlannedInstrumentData data, SmallBodyModel model)
			throws FitsException, IOException
    {
    	IPlanningDataActorBuilder<PlannedInstrumentData> builder = registeredModels.get(data.getInstrumentName());
    	return builder.buildActorForPlanningData(data, model);
    }
}
