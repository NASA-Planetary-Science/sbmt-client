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

    public void setStartTime(double time);
    public double getStartTime();

    public void setStopTime(double time);
    public double getStopTime();

    public void setNumPoints(int numPoints);
    public int getNumPoints();

    public boolean isHasInstrumentPointingInfo();
//    /**
//     * @return
//     */
//    public ArrayList<Double> getTime();
//
//    /**
//     * @param times
//     */
//    public void setTime(ArrayList<Double> times);
//
//    /**
//     * @return
//     */
//    public ArrayList<Double> getX();
//
//    /**
//     * @param x
//     */
//    public void setX(ArrayList<Double> x);
//
//    /**
//     * @return
//     */
//    public ArrayList<Double> getY();
//
//    /**
//     * @param y
//     */
//    public void setY(ArrayList<Double> y);
//
//    /**
//     * @return
//     */
//    public ArrayList<Double> getZ();
//
//    /**
//     * @param z
//     */
//    public void setZ(ArrayList<Double> z);

    public IPointingProvider getPointingProvider();

    public void setPointingProvider(IPointingProvider pointingProvider);

    /**
     * @return
     */
    public double[] getTrajectoryColor();

    /**
     * @return
     */
    public double getTrajectoryThickness();

    /**
     * @param color
     */
    public void setTrajectoryColor(double[] color);

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
    public void setTrajectoryLineThickness(double thickness);

    /**
     * @param isFaded
     */
    public void setFaded(boolean isFaded);

    /**
     * @return
     */
    public boolean isFaded();

//    /**
//     * @param position
//     * @param time
//     */
//    public void addPositionAtTime(double[] spacecraftPosition, double time);

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
