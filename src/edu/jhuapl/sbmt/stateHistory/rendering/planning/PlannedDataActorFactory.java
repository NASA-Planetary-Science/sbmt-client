package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.stateHistory.model.planning.PlannedInstrumentData;

import nom.tam.fits.FitsException;

public class PlannedDataActorFactory
{
	static HashMap<String, IPlanningDataActorBuilder<PlannedInstrumentData>> registeredInstruments
			= new HashMap<String, IPlanningDataActorBuilder<PlannedInstrumentData>>();

	static HashMap<String, Color> registeredInstrumentColors = new HashMap<String, Color>();


	static public void registerInstrument(String[] instrumentNames, IPlanningDataActorBuilder<PlannedInstrumentData> builder, Color color)
	{
		for (String uniqueName : instrumentNames)
		{
			registeredInstruments.put(uniqueName, builder);
			registeredInstrumentColors.put(uniqueName, color);
		}
	}

	static public PlannedDataActor createPlannedDataActorFor(PlannedInstrumentData data, SmallBodyModel model)
			throws FitsException, IOException
    {
    	IPlanningDataActorBuilder<PlannedInstrumentData> builder = registeredInstruments.get(data.getInstrumentName());
    	PlannedDataActor actor = builder.buildActorForPlanningData(data, model);
    	actor.setColor(registeredInstrumentColors.get(data.getInstrumentName()));
    	return actor;
    }

	static public Color getColorForInstrument(String instrumentName)
	{
		return registeredInstrumentColors.get(instrumentName);
	}
}
