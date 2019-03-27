package edu.jhuapl.sbmt.gui.lidar;

/**
 * Interface that provides for the callback mechanism for notification of Track
 * changes.
 */
public interface TrackEventListener
{
	/**
	 * Notification method that the state of the Tracks has changed
	 *
	 * @param aSource The object that generated this event.
	 * @param aEvent The type that describes the event.
	 */
	public void handleTrackEvent(Object aSource, ItemEventType aEventType);

}
