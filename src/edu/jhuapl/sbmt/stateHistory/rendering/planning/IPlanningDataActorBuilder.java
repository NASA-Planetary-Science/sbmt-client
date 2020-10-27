package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import edu.jhuapl.sbmt.client.SmallBodyModel;

@FunctionalInterface
public interface IPlanningDataActorBuilder<PlannedInstrumentData>
{
	/**
	 * @param data
	 * @param model
	 * @return
	 */
	PlannedDataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model);
}
