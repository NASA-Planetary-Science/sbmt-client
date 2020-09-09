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
		System.out.println("PlannedDataActorFactory: registerModel: adding unique name " + uniqueName);
		registeredModels.put(uniqueName, builder);
	}

	static public PlannedDataActor createPlannedDataActorFor(PlannedInstrumentData data, SmallBodyModel model)
			throws FitsException, IOException
    {
//    	System.out.println("PlannedDataActorFactory: createImage: getting " + data.getInstrumentName());
    	IPlanningDataActorBuilder<PlannedInstrumentData> builder = registeredModels.get(data.getInstrumentName());
    	return builder.buildActorForPlanningData(data, model);
    }
}
