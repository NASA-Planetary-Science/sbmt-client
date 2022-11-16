package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.rendering.DataActor;

@FunctionalInterface
public interface IPlanningDataActorBuilder<PlannedInstrumentData>
{
	/**
	 * @param data
	 * @param model
	 * @return
	 */
	DataActor buildActorForPlanningData(PlannedInstrumentData data, SmallBodyModel model);
}
