package edu.jhuapl.sbmt.stateHistory.model.planning;

import java.util.HashMap;

public class PlanningModelFactory
{
	private static HashMap<String, IInstrumentConfig> planningInstruments =
			new HashMap<String, IInstrumentConfig>();

	public static void registerPlanningInstrument(String name, IInstrumentConfig config)
	{
		planningInstruments.put(name, config);
	}

	public static IInstrumentConfig getConfigForInstrumentName(String name)
	{
		return planningInstruments.get(name);
	}

}
