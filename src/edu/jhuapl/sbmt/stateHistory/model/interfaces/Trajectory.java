package edu.jhuapl.sbmt.stateHistory.model.interfaces;

import edu.jhuapl.sbmt.pointing.IPointingProvider;


/**
 * @author steelrj1
 *
 */
public interface Trajectory
{

    /**
     * @return
     */
    public int getCellId();

    /**
     * @param cellId
     */
    public void setCellId(int cellId);

    /**
     * @return
     */
    public int getId();

    /**
     * @param id
     */
    public void setId(int id);

    /**
     * @return
     */
    public double getTimeStep();

    /**
     * @param time
     */
    public void setStartTime(double time);

    /**
     * @return
     */
    public double getStartTime();

    /**
     * @param time
     */
    public void setStopTime(double time);

    /**
     * @return
     */
    public double getStopTime();

    /**
     * @param numPoints
     */
    public void setNumPoints(int numPoints);

    /**
     * @return
     */
    public int getNumPoints();

    /**
     * @return
     */
    public boolean isHasInstrumentPointingInfo();

    /**
     * @return
     */
    public IPointingProvider getPointingProvider();

    /**
     * @param pointingProvider
     */
    public void setPointingProvider(IPointingProvider pointingProvider);

    /**
     * @return
     */
    public double[] getColor();

    /**
     * @return
     */
    public double getThickness();

    /**
     * @param color
     */
    public void setColor(double[] color);

    /**
     * @param desc
     */
    public void setTrajectoryDescription(String desc);

    /**
     * @return
     */
    public String getTrajectoryDescription();

    /**
     * @param thickness
     */
    public void setThickness(double thickness);

    /**
     * @param isFaded
     */
    public void setFaded(boolean isFaded);

    /**
     * @return
     */
    public boolean isFaded();

    //Display fraction (which portions of the overall time window are being worked on)

    /**
     * Returns the min display fraction
     * @return
     */
    public double getMinDisplayFraction();

	/**
	 * Set the min display fraction (a value between 0.0 and 1.0), representing the min
	 * portion of this state history trajectory is displayed
	 * @param minDisplayFraction
	 */
	public void setMinDisplayFraction(double minDisplayFraction);

	/**
	 * Returns the max display fraction
	 * @return
	 */
	public double getMaxDisplayFraction();

	/**
	 * Set the max display fraction (a value between 0.0 and 1.0) representing the max
	 * portion of this state history trajectory is displayed
	 * @param maxDisplayFraction
	 */
	public void setMaxDisplayFraction(double maxDisplayFraction);

}
