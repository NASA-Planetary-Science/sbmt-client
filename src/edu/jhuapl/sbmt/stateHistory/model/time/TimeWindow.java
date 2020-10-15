package edu.jhuapl.sbmt.stateHistory.model.time;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.util.TimeUtil;

import crucible.core.time.UTCEpoch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Time window in et
 *
 * @author steelrj1
 *
 */
@AllArgsConstructor
public class TimeWindow
{
	@Getter
	@Setter
	public double startTime, stopTime;

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

}
