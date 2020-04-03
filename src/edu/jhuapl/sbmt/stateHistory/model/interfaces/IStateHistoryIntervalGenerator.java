package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.function.Function;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

public interface IStateHistoryIntervalGenerator
{
    /***
     *  Creates a new time history for the body with the given time range.
     *
     * @param key 					StateHistoryKey representing this interval
     * @param length 				length of interval, in seconds
     * @param name 					name of interval
     * @param progressionFunction 	Closure-like Function that takes in progress and allows you to update completion status
     * @return 						StateHistory object if successful; null otherwise
	 */
	public StateHistory createNewTimeInterval(StateHistoryKey key,  DateTime startTime, DateTime endTime, double duration, String name, Function<Double, Void> progressFunction) throws StateHistoryInputException;


	public StateHistory createNewTimeInterval(StateHistory history, Function<Double, Void> progressFunction) throws StateHistoryInputException;
}
