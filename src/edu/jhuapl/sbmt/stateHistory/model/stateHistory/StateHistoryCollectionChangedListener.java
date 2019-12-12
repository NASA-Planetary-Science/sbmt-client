package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;

public interface StateHistoryCollectionChangedListener
{

	public void historySegmentMapped(StateHistory segment);

}
