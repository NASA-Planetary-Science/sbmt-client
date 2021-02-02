package edu.jhuapl.sbmt.stateHistory.model.io;

/**
 * Exception that is thrown when an invalid time is used to attempt
 * to generate a StateHistory segment
 * @author steelrj1
 *
 */
public class StateHistoryInvalidTimeException extends Exception
{

	public StateHistoryInvalidTimeException()
	{
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInvalidTimeException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInvalidTimeException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInvalidTimeException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInvalidTimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
