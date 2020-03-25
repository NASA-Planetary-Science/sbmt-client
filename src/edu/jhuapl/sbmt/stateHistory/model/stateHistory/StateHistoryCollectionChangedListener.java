package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;

public interface StateHistoryCollectionChangedListener
{

	/**
	 * @param segment
	 */
	public void historySegmentMapped(StateHistory segment);

}
