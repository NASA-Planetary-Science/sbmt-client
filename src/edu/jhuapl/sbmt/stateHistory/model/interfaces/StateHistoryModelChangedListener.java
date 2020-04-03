package edu.jhuapl.sbmt.stateHistory.model.interfaces;

/**
 * @author steelrj1
 *
 */
public interface StateHistoryModelChangedListener
{
	/**
	 * @param t
	 */
	public void timeChanged(Double t);

	/**
	 * @param color
	 */
	public void trajectoryColorChanged(double[] color);

	/**
	 * @param thickness
	 */
	public void trajectoryThicknessChanged(Double thickness);

	/**
	 * @param distanceText
	 */
	public void distanceTextChanged(String distanceText);

	/**
	 * @param trajectory
	 */
	public void historySegmentCreated(StateHistory trajectory);

}
