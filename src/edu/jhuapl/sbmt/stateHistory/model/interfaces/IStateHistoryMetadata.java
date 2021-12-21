package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.List;

import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

public interface IStateHistoryMetadata
{
    /**
     * @return
     */
    public StateHistoryKey getKey();

	public boolean isMapped();

	public void setMapped(boolean isMapped);

	public boolean isVisible();

	public void setVisible(boolean visible);

	/**
	 * @param type
	 */
	public void setType(StateHistorySourceType type);



    /**
     * @return
     */
    public String getStateHistoryName();

    /**
     * @param name
     */
    public void setStateHistoryName(String name);

    /**
     * @return
     */
    public String getStateHistoryDescription();

    /**
     * @param name
     */
    public void setStateHistoryDescription(String description);

	/**
	 * @return
	 */
	public StateHistorySourceType getType();

	/**
	 * Returns the time window for this state history
     * @return
     */
    public Double getTimeWindow();

    /**
     * Returns the current time in the time window for this state history
     * @return
     */
    public Double getCurrentTime();

    /**
     * Sets the current time in the time window for this state history
     * @param time
     */
    public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException;

    /**
     * Returns the minimum available time in this state history
     * @return
     */
    public Double getStartTime();

    /**
     * Returns the maximum available time in this state history
     * @return
     */
    public Double getEndTime();

    /**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Double startTime);

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Double endTime);

	//FOV information
	public List<String> getFOVNames();

	public void setFOVNames(List<String> names);


}

