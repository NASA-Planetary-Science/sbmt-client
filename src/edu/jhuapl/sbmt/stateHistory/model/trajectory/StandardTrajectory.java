package edu.jhuapl.sbmt.stateHistory.model.trajectory;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

import lombok.Getter;
import lombok.Setter;

public class StandardTrajectory implements Trajectory
{
	/**
	 *
	 */
	@Getter @Setter
	private int cellId;

	/**
	 *
	 */
	@Getter @Setter
	private int id;

	/**
	 *
	 */
	@Getter @Setter
	private String trajectoryDescription;

	/**
	 *
	 */
	@Getter
	private boolean isFaded;

	@Getter
	private int numPoints;

	@Getter @Setter
	private double startTime, stopTime;

	/**
	 *
	 */
	@Getter @Setter
	private double[] color;

	/**
	 *
	 */
	@Getter @Setter
	private double thickness;


	/**
	 *
	 */
	@Getter @Setter
	private double minDisplayFraction = 0.0, maxDisplayFraction = 1.0;

	@Getter @Setter
	private IPointingProvider pointingProvider;

	@Getter
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

}