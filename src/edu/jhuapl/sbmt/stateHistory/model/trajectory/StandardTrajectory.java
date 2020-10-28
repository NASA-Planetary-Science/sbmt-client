package edu.jhuapl.sbmt.stateHistory.model.trajectory;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

public class StandardTrajectory implements Trajectory
{
	/**
	 *
	 */
	private int cellId;

	/**
	 *
	 */

	private int id;

	/**
	 *
	 */
	private String trajectoryDescription;

	/**
	 *
	 */

	private boolean isFaded;

	private int numPoints;

	private double startTime, stopTime;

	/**
	 *
	 */
	private double[] color;

	/**
	 *
	 */
	private double thickness;


	/**
	 *
	 */
	private double minDisplayFraction = 0.0, maxDisplayFraction = 1.0;

	private IPointingProvider pointingProvider;

	private double timeStep;

	/**
	 *
	 */
	public StandardTrajectory()
	{
		super();
		trajectoryDescription = "";
		this.color = new double[] { 0, 255, 255, 255 };
	}

	/**
	 *
	 */
	public String toString()
	{
		return "Trajectory " + getId() + " = contains " + numPoints + " vertices";
	}

	/**
	 *
	 */
	public void setFaded(boolean isFaded)
	{
		this.isFaded = isFaded;
		this.color[3] = isFaded ? 50 : 255;
	}

	/**
	 * @param numPoints the numPoints to set
	 */
	public void setNumPoints(int numPoints)
	{
		this.numPoints = numPoints;
		this.timeStep = (stopTime - startTime)/(double)numPoints;
	}

	/**
	 * @return the hasInstrumentPointingInfo
	 */
	public boolean isHasInstrumentPointingInfo()
	{
		return pointingProvider instanceof SpicePointingProvider;
	}

	/**
	 * @return the cellId
	 */
	public int getCellId()
	{
		return cellId;
	}

	/**
	 * @param cellId the cellId to set
	 */
	public void setCellId(int cellId)
	{
		this.cellId = cellId;
	}

	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return the trajectoryDescription
	 */
	public String getTrajectoryDescription()
	{
		return trajectoryDescription;
	}

	/**
	 * @param trajectoryDescription the trajectoryDescription to set
	 */
	public void setTrajectoryDescription(String trajectoryDescription)
	{
		this.trajectoryDescription = trajectoryDescription;
	}

	/**
	 * @return the isFaded
	 */
	public boolean isFaded()
	{
		return isFaded;
	}

	/**
	 * @return the numPoints
	 */
	public int getNumPoints()
	{
		return numPoints;
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public double getStopTime()
	{
		return stopTime;
	}

	/**
	 * @param stopTime the stopTime to set
	 */
	public void setStopTime(double stopTime)
	{
		this.stopTime = stopTime;
	}

	/**
	 * @return the color
	 */
	public double[] getColor()
	{
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(double[] color)
	{
		this.color = color;
	}

	/**
	 * @return the thickness
	 */
	public double getThickness()
	{
		return thickness;
	}

	/**
	 * @param thickness the thickness to set
	 */
	public void setThickness(double thickness)
	{
		this.thickness = thickness;
	}

	/**
	 * @return the minDisplayFraction
	 */
	public double getMinDisplayFraction()
	{
		return minDisplayFraction;
	}

	/**
	 * @param minDisplayFraction the minDisplayFraction to set
	 */
	public void setMinDisplayFraction(double minDisplayFraction)
	{
		this.minDisplayFraction = minDisplayFraction;
	}

	/**
	 * @return the maxDisplayFraction
	 */
	public double getMaxDisplayFraction()
	{
		return maxDisplayFraction;
	}

	/**
	 * @param maxDisplayFraction the maxDisplayFraction to set
	 */
	public void setMaxDisplayFraction(double maxDisplayFraction)
	{
		this.maxDisplayFraction = maxDisplayFraction;
	}

	/**
	 * @return the pointingProvider
	 */
	public IPointingProvider getPointingProvider()
	{
		return pointingProvider;
	}

	/**
	 * @param pointingProvider the pointingProvider to set
	 */
	public void setPointingProvider(IPointingProvider pointingProvider)
	{
		this.pointingProvider = pointingProvider;
	}

	/**
	 * @return the timeStep
	 */
	public double getTimeStep()
	{
		return timeStep;
	}

}