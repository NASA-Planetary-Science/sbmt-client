package edu.jhuapl.sbmt.stateHistory.model.time;

import java.util.Vector;

public class StateHistoryTimeModel
{
	public TimeWindow twindow;

	public double et;

	public double minFractionDisplayed = 0.0, maxFractionDisplayed = 1.0;

	public Vector<StateHistoryTimeModelChangedListener> changeListeners;

	public StateHistoryTimeModel()
	{
		this.changeListeners = new Vector<StateHistoryTimeModelChangedListener>();
	}

	public StateHistoryTimeModel(TimeWindow twindow)
	{
		this.twindow = twindow;
		this.et = twindow.getStartTime();
		this.changeListeners = new Vector<StateHistoryTimeModelChangedListener>();
	}

	public void setTime(double et)
	{
		this.et = et;
		fireTimeChangedListeners();
	}

	public void setTimeFraction(double fraction)
	{
		TimeWindow twindow = getDisplayedTimeWindow();
		this.et = twindow.getStartTime() + fraction*(twindow.getStopTime() - twindow.getStartTime());
		fireTimeChangedListeners();
	}

	public void setTimeWindow(TimeWindow twindow)
	{
		this.twindow = twindow;
		System.out.println("StateHistoryTimeModel: setTimeWindow: setting time window to " + twindow);
		fireTimeWindowChangedListeners();
	}

	public void setFractionDisplayed(double minFraction, double maxFraction)
	{
		this.minFractionDisplayed = minFraction;
		this.maxFractionDisplayed = maxFraction;
		setTimeFraction(0.0);
		fireFractionDisplayedChangedListeners();
	}

	public TimeWindow getDisplayedTimeWindow()
	{
		double start = twindow.getStartTime() + minFractionDisplayed*(twindow.getStopTime() - twindow.getStartTime());
		double stop = twindow.getStopTime() - (1-maxFractionDisplayed)*(twindow.getStopTime() - twindow.getStartTime());
		return new TimeWindow(start, stop);
	}

	//Listeners
	public void addTimeModelChangeListener(StateHistoryTimeModelChangedListener listener)
	{
		changeListeners.add(listener);
	}

	private void fireTimeChangedListeners()
	{
		changeListeners.forEach( e -> e.timeChanged(et));
	}

	private void fireTimeWindowChangedListeners()
	{
		changeListeners.forEach( e -> e.timeWindowChanged(twindow));
	}

	private void fireFractionDisplayedChangedListeners()
	{
		changeListeners.forEach( e -> e.fractionDisplayedChanged(minFractionDisplayed, maxFractionDisplayed));
	}

	/**
	 * @return the twindow
	 */
	public TimeWindow getTwindow()
	{
		return twindow;
	}

	/**
	 * @return the et
	 */
	public double getEt()
	{
		return et;
	}

	/**
	 * @return the minFractionDisplayed
	 */
	public double getMinFractionDisplayed()
	{
		return minFractionDisplayed;
	}

	/**
	 * @return the maxFractionDisplayed
	 */
	public double getMaxFractionDisplayed()
	{
		return maxFractionDisplayed;
	}

}
