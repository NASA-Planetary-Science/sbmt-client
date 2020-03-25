package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author steelrj1
 *
 */
public class DateTimeSpinner extends JSpinner
{

	/**
	 *
	 */
	public DateTimeSpinner()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param model
	 */
	public DateTimeSpinner(SpinnerModel model)
	{
		super(model);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	public static double getTimeSpanBetween(DateTimeSpinner start, DateTimeSpinner end)
	{
        Date beginTime = (Date)start.getModel().getValue();
        Date stopTime = (Date)end.getModel().getValue();
        double total = (stopTime.getTime() - beginTime.getTime())
                / (24.0 * 60.0 * 60.0 * 1000.0);
        return total;
	}

	/**
	 * @return
	 */
	public DateTime getISOFormattedTime()
	{
		Date date = ((Date)getModel().getValue());
		DateTime dateTime = new DateTime(date);
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateTime.toString());
	}

}
