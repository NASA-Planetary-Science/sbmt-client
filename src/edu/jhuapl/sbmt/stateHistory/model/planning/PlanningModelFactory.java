package edu.jhuapl.sbmt.stateHistory.model.planning;

import java.util.HashMap;

public class PlanningModelFactory
{
	private static HashMap<String, IInstrumentConfig> planningInstruments =
			new HashMap<String, IInstrumentConfig>();

	/**
	 * @param name
	 * @param config
	 */
	public static void registerPlanningInstrument(String name, IInstrumentConfig config)
	{
		planningInstruments.put(name, config);
	}

	/**
	 * @param name
	 * @return
	 */
	public static IInstrumentConfig getConfigForInstrumentName(String name)
	{
		return planningInstruments.get(name);
	}

}
