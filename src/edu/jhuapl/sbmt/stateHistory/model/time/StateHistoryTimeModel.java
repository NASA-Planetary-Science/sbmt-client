package edu.jhuapl.sbmt.stateHistory.model.time;

import java.beans.PropertyChangeSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.util.TimeUtil;

public class StateHistoryTimeModel
{
	private TimeWindow twindow;

	private double et;

	private double currentTimeFraction = 0;

	private double minFractionDisplayed = 0.0, maxFractionDisplayed = 1.0;

	private Vector<StateHistoryTimeModelChangedListener> changeListeners;

	private static StateHistoryTimeModel instance;

	private PropertyChangeSupport pcs;

	public static StateHistoryTimeModel getInstance() {
		if (instance == null) instance = new StateHistoryTimeModel();
		return instance;
	}

	public StateHistoryTimeModel()
	{
		this.changeListeners = new Vector<StateHistoryTimeModelChangedListener>();
	}

	public void setPcs(PropertyChangeSupport pcs)
	{
		this.pcs = pcs;
	}

	public void setTime(double et)
	{
		if (Double.compare(this.et, et) == 0) return;
		this.et = et;
		fireTimeChangedListeners();
	}

	public void setTimeFraction(double fraction)
	{
		if (Double.compare(currentTimeFraction, fraction) == 0) return;
		TimeWindow twindow = getDisplayedTimeWindow();
		this.et = twindow.getStartTime() + fraction*(twindow.getStopTime() - twindow.getStartTime());
		fireTimeChangedListeners();
	}

	public void setTimeWindow(TimeWindow twindow)
	{
		this.twindow = twindow;
		this.et = twindow.getStartTime() + currentTimeFraction*(twindow.getStopTime() - twindow.getStartTime());
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
		changeListeners.forEach( e -> { e.timeChanged(et);});
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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

    public static Date getDateForET(double et)
    {
    	Date date = null;
 		try
 		{
 			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(TimeUtil.et2str(et).substring(0, 23));
 		} catch (ParseException pe)
 		{
 			// TODO Auto-generated catch block
 			pe.printStackTrace();
 		}
 		return date;
    }

    public static DateTime getDateTimeForET(double et)
    {
    	Date date = getDateForET(et);
    	DateTime dt = new DateTime(date);
        DateTime dt1 = ISODateTimeFormat.dateTimeParser().parseDateTime(dt.toString());
 		return dt1;
    }

    public static double getETForDate(Date dateTime)
    {
        DateTime dt = new DateTime(dateTime);
        DateTime dt1 = ISODateTimeFormat.dateTimeParser().parseDateTime(dt.toString());
        return new Double(dt1.toDate().getTime());
    }
}
