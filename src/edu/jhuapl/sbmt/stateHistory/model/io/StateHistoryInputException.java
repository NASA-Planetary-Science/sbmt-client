package edu.jhuapl.sbmt.stateHistory.model.io;

/**
 * Exception that is thrown when detecting users input errors when building
 * a state history segment (i.e. the times range is too small)
 * @author steelrj1
 *
 */
public class StateHistoryInputException extends Exception
{

	public StateHistoryInputException()
	{
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInputException(String message)
	{
		super(message);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInputException(Throwable cause)
	{
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInputException(String message, Throwable cause)
	{
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public StateHistoryInputException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
