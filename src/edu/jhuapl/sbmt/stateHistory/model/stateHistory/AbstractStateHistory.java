package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.io.File;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;

public abstract class AbstractStateHistory implements StateHistory
{

	protected boolean isValid = false;
	protected IStateHistoryLocationProvider locationProvider;
	protected IStateHistoryMetadata metadata;
	protected IStateHistoryTrajectoryMetadata trajectoryMetadata;

	public AbstractStateHistory()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public StateHistory loadStateHistoryFromFile(File runFile, String shapeModelName, StateHistoryKey key)
			throws StateHistoryIOException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid()
	{
		return isValid;
	}

	@Override
	public IStateHistoryMetadata getMetadata()
	{
		return metadata;
	}

	@Override
	public IStateHistoryLocationProvider getLocationProvider()
	{
		return locationProvider;
	}

	@Override
	public IStateHistoryTrajectoryMetadata getTrajectoryMetadata()
	{
		return trajectoryMetadata;
	}

}
