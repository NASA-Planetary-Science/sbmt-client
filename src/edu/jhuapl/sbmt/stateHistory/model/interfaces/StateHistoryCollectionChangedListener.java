package edu.jhuapl.sbmt.stateHistory.model.interfaces;

/**
 * Listener that can be used to broadcast when the state
 * history collection has changed
 * @author steelrj1
 *
 */
public interface StateHistoryCollectionChangedListener
{

//	/**
//	 * Notifies listeners that the <pre>history</pre> object has changed
//	 * @param segment
//	 */
//	public void historySegmentMapped(StateHistory history);

	public void historySegmentUpdated(StateHistory history);


}
