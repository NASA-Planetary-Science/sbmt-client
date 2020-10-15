package edu.jhuapl.sbmt.stateHistory.model.time;

public interface StateHistoryTimeModelChangedListener
{

	public void timeChanged(double et);

	public void timeWindowChanged(TimeWindow twindow);

	public void fractionDisplayedChanged(double minFractionDisplayed, double maxFractionDisplayed);

	public void timeFractionChanged(double fraction);

}
