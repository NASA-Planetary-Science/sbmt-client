package edu.jhuapl.sbmt.stateHistory.model.time;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.core.util.TimeUtil;

import crucible.core.time.UTCEpoch;

/**
 * Time window in et
 *
 * @author steelrj1
 *
 */

public class TimeWindow
{
	public double startTime, stopTime;

	/**
	 * @param startTime
	 * @param stopTime
	 */
	public TimeWindow(double startTime, double stopTime)
	{
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

	/**
	 * @param start
	 * @param end
	 */
	public TimeWindow(DateTime start, DateTime end)
	{
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-D'T'HH:mm:ss.SSS");	//generates Year-DOY date format

		UTCEpoch startEpoch = UTCEpoch.fromString(dateFormatter.format(start.toDate()));
		UTCEpoch endEpoch = UTCEpoch.fromString(dateFormatter.format(end.toDate()));

		this.startTime = TimeUtil.str2et(startEpoch.toString());
		this.stopTime = TimeUtil.str2et(endEpoch.toString());
	}

	@Override
	public String toString()
	{
		return "Time window: ( " + TimeUtil.et2str(startTime) + " - " + TimeUtil.et2str(stopTime) + ")";
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public double getStopTime()
	{
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(double stopTime)
	{
		this.stopTime = stopTime;
	}
}
