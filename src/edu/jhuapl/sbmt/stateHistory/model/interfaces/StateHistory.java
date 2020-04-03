package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import java.util.Map.Entry;
import java.util.Set;

import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

/**
 * @author steelrj1
 *
 */
public interface StateHistory
{
	/**
     * @return
     */
    public Double getPeriod();

    /**
     * @return
     */
    public Double getTimeFraction();

    /**
     * @param state
     * @param timeFraction
     */
    public void setTimeFraction(Double timeFraction);

    /**
     *
     */
    public static final double epsilon = 0.0000001;

    /**
     * @return
     */
    public Double getTime();

    /**
     * @param time
     */
    public void setTime(Double time);

    /**
     * @return
     */
    public Double getMinTime();

    /**
     * @return
     */
    public Double getMaxTime();

    /**
     * @param flybyState
     */
    public void put(State flybyState);

    /**
     * @param time
     * @param flybyState
     */
    public void put(Double time, State flybyState);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getFloorEntry(Double time);

    /**
     * @param time
     * @return
     */
    public Entry<Double, State> getCeilingEntry(Double time);

    /**
     * @param time
     * @return
     */
    public State getValue(Double time);

    /**
     * @return
     */
    public State getCurrentValue();

    /**
     * @return
     */
    public double[] getSpacecraftPosition();

    /**
     * @return
     */
    public double[] getSunPosition();

    /**
     * @return
     */
    public double[] getEarthPosition();

    /**
     * @return
     */
    public Set<Double> getAllKeys();

    /**
     * @return
     */
    public StateHistoryKey getKey();

    /**
     * @return
     */
    public String getTrajectoryName();

    /**
     * @return
     */
    public String getTrajectoryDescription();

    /**
     * @return
     */
    public double[] getTrajectoryColor();

    /**
     * @return
     */
    public double getTrajectoryThickness();

    /**
     * @return
     */
    public Trajectory getTrajectory();

    /**
     * @param traj
     */
    public void setTrajectory(Trajectory traj);

    /**
     * @param traj
     */
    public void setTrajectoryName(String name);

    /**
     * @return
     */
    public double getMinDisplayFraction();

	/**
	 * @param minDisplayFraction
	 */
	public void setMinDisplayFraction(double minDisplayFraction);

	/**
	 * @return
	 */
	public double getMaxDisplayFraction();

	/**
	 * @param maxDisplayFraction
	 */
	public void setMaxDisplayFraction(double maxDisplayFraction);

}
