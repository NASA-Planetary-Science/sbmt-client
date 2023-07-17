package edu.jhuapl.sbmt.stateHistory.rendering.planning;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
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
