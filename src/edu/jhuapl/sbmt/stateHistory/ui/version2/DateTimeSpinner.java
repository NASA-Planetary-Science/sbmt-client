package edu.jhuapl.sbmt.stateHistory.ui.version2;

import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Custom JSpinner to handle DateTime entries
 * @author steelrj1
 *
 */
public class DateTimeSpinner extends JSpinner
{

	/**
	 * Default Constructor
	 */
	public DateTimeSpinner()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor.  Takes in SpinnerModel to init parent
	 * @param model	SpinnerModel to initialize the parent JSpinner
	 */
	public DateTimeSpinner(SpinnerModel model)
	{
		super(model);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns the number of seconds between the DateTime values denoted
	 * by the <pre>start</start> and <pre>end</pre> arguments
	 * @param start	The DateTimeSpinner denoting the start time
	 * @param end	The DateTimeSpinner denoting the end time
	 * @return		The number of seconds between <pre>start</start> and <pre>end</pre>
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
	 * Returns the DateTime for the ISO compliant string in this spinner
	 * @return	the DateTime for the ISO compliant string
	 */
	public DateTime getISOFormattedTime()
	{
		Date date = ((Date)getModel().getValue());
		DateTime dateTime = new DateTime(date);
		return ISODateTimeFormat.dateTimeParser().parseDateTime(dateTime.toString());
	}

}
