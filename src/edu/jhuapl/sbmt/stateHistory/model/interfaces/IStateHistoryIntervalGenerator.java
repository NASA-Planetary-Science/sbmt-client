package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.function.Function;

import org.joda.time.DateTime;

import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

/**
 * Interface for classes that can generate state history intervals
 * @author steelrj1
 *
 */
public interface IStateHistoryIntervalGenerator
{
    /***
     *  Creates a new time history for the body with the given time range, based on the given StateHistoryKey.
     *
     * @param key 					StateHistoryKey representing this interval
     * @param length 				length of interval, in seconds
     * @param name 					name of interval
     * @param progressionFunction 	Closure-like Function that takes in progress and allows you to update completion status
     * @return 						StateHistory object if successful; null otherwise
     * @throws StateHistoryInputException
	 */
	public StateHistory createNewTimeInterval(StateHistoryKey key,  DateTime startTime, DateTime endTime, double duration, String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException;


	/**
	 * Creates a new time interval based on a skeleton history object, typically generated from metadata.  The fully populated StateHistory is returned
	 * @param history						Partially populated StateHistory object
	 * @param progressFunction				Closure-like Function that takes in progress and allows you to update completion status
	 * @return								StateHistory object if successful; null otherwise
	 * @throws StateHistoryInputException
	 */
	public StateHistory createNewTimeInterval(StateHistory history, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException;


	/**
	 * @param sourceFile
	 * @param spice
	 */
	public void setSourceFile(String sourceFile, SpiceInfo spice);
}
