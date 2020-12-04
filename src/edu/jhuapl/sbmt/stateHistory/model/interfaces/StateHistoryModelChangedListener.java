package edu.jhuapl.sbmt.stateHistory.model.interfaces;

/**
 * Listener that can be used to broadcast when certain aspects
 * of the StateHistoryModel have changed
 * @author steelrj1
 *
 */
public interface StateHistoryModelChangedListener
{
	/**
	 * Notifies when the trajectory color has changed
	 * @param color
	 */
	public void trajectoryColorChanged(double[] color);

	/**
	 * Notifies when the trajectory thickness has changed
	 * @param thickness
	 */
	public void trajectoryThicknessChanged(Double thickness);

	/**
	 * Notifies when the distance text has changed
	 * @param distanceText
	 */
	public void distanceTextChanged(String distanceText);

	/**
	 * Notifies when a new StateHistory has been created
	 * @param trajectory
	 */
	public void historySegmentCreated(StateHistory history);

	/**
	 * Notifies when a new StateHistory has been created
	 * @param trajectory
	 */
	public void historySegmentRemoved(StateHistory history);

}
