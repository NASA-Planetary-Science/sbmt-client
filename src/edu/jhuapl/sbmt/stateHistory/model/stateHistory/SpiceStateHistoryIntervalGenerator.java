package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.function.Function;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;

public class SpiceStateHistoryIntervalGenerator implements IStateHistoryIntervalGenerator
{
	private double cadence;

	public SpiceStateHistoryIntervalGenerator(double cadence)
	{
		this.cadence = cadence;
	}

	@Override
	public StateHistory createNewTimeInterval(StateHistoryKey key, DateTime startTime, DateTime endTime,
			double duration, String name, Function<Double, Void> progressFunction) throws StateHistoryInputException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StateHistory createNewTimeInterval(StateHistory history, Function<Double, Void> progressFunction)
			throws StateHistoryInputException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
