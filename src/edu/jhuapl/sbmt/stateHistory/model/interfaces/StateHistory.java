package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.io.File;

import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

/**
 * Interface for classes that store state history segment information
 * @author steelrj1
 *
 */
public interface StateHistory
{

	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException;

	public StateHistory loadStateHistoryFromFile(File runFile, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException;

	public void validate();

	public boolean isValid();

	public IStateHistoryMetadata getMetadata();

	public IStateHistoryLocationProvider getLocationProvider();

	public IStateHistoryTrajectoryMetadata getTrajectoryMetadata();

}
