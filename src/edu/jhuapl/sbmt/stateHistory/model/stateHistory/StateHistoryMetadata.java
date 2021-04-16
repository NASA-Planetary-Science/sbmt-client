package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;

public class StateHistoryMetadata implements IStateHistoryMetadata
{

	/**
	*
	*/
	private StateHistoryKey key;

	/**
	 *
	 */
	protected boolean mapped = false;

	/**
	 *
	 */
	protected boolean visible = false;

	/**
	 *
	 */
	private StateHistorySourceType type;



	/**
	*
	*/
	private String stateHistoryName = "";

	/**
	*
	*/
	private String stateHistoryDescription = "";

	/**
	*
	*/
	protected Double currentTime;

	/**
	*
	*/
	protected Double startTime;

	/**
	*
	*/
	protected Double endTime;

	/**
	 *
	 */
	List<String> fovNames = new ArrayList<String>();

	public StateHistoryMetadata(StateHistoryKey key)
	{
		this.key = key;
	}

	public StateHistoryMetadata(StateHistoryKey key, Double currentTime,
								Double startTime, Double endTime,
								String name, String description,
								StateHistorySourceType type)
	{
		this.key = key;
		this.currentTime = currentTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.stateHistoryName = name;
		this.stateHistoryDescription = description;
		this.type = type;
	}

	public StateHistoryMetadata(StateHistoryMetadata metadata)
	{
		this.key = metadata.getKey();
		this.currentTime = metadata.getCurrentTime();
		this.startTime = metadata.getStartTime();
		this.endTime = metadata.getEndTime();
		this.stateHistoryName = metadata.getStateHistoryName();
		this.stateHistoryDescription = metadata.getStateHistoryDescription();
		this.type = metadata.getType();
	}

	@Override
	public StateHistoryKey getKey()
	{
		return key;
	}

	@Override
	public boolean isMapped()
	{
		return mapped;
	}

	@Override
	public void setMapped(boolean isMapped)
	{
		this.mapped = isMapped;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public void setType(StateHistorySourceType type)
	{
		this.type = type;
	}

	@Override
	public StateHistorySourceType getType()
	{
		return type;
	}

	@Override
	public String getStateHistoryName()
	{
		return stateHistoryName;
	}

	@Override
	public void setStateHistoryName(String name)
	{
		this.stateHistoryName = name;
	}

	@Override
	public String getStateHistoryDescription()
	{
		return stateHistoryDescription;
	}

	@Override
	public void setStateHistoryDescription(String description)
	{
		this.stateHistoryDescription = description;
	}

	@Override
	public Double getTimeWindow()
	{
		return getEndTime() - getStartTime();
	}

	@Override
	public Double getCurrentTime()
	{
		return currentTime;
	}

	@Override
	public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
	{
		this.currentTime = time;
	}

	@Override
	public Double getStartTime()
	{
		return startTime;
	}

	@Override
	public void setStartTime(Double startTime)
	{
		this.startTime = startTime;
	}

	@Override
	public Double getEndTime()
	{
		return endTime;
	}

	@Override
	public void setEndTime(Double endTime)
	{
		this.endTime = endTime;
	}

	@Override
	public List<String> getFOVNames()
	{
		return fovNames;
	}

	@Override
	public void setFOVNames(List<String> names)
	{
		this.fovNames = names;
	}

}
