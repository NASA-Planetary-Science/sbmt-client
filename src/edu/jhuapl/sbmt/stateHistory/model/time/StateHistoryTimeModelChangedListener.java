package edu.jhuapl.sbmt.stateHistory.model.time;

public interface StateHistoryTimeModelChangedListener
{

	/**
	 * @param et
	 */
	public void timeChanged(double et);

	/**
	 * @param twindow
	 */
	public void timeWindowChanged(TimeWindow twindow);

	/**
	 * @param minFractionDisplayed
	 * @param maxFractionDisplayed
	 */
	public void fractionDisplayedChanged(double minFractionDisplayed, double maxFractionDisplayed);

	/**
	 * @param fraction
	 */
	public void timeFractionChanged(double fraction);

}
