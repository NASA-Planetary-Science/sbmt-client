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

	private int numPoints;

	private double startTime, stopTime;

//	/**
//	 *
//	 */
//	private ArrayList<Double> x;
//
//	/**
//	 *
//	 */
//	private ArrayList<Double> y;
//
//	/**
//	 *
//	 */
//	private ArrayList<Double> z;
//
//	/**
//	 *
//	 */
//	private ArrayList<Double> times;

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
	private String description;

	/**
	 *
	 */
	private boolean isFaded;

	/**
	 *
	 */
	private double minDisplayFraction = 0.0, maxDisplayFraction = 1.0;

	private IPointingProvider pointingProvider;


	/**
	 *
	 */
	public int getCellId()
	{
		return cellId;
	}

	/**
	 *
	 */
	public void setCellId(int cellId)
	{
		this.cellId = cellId;
	}

	/**
	 *
	 */
	public int getId()
	{
		return id;
	}

	/**
	 *
	 */
	public void setId(int id)
	{
		this.id = id;
	}

//	/**
//	 *
//	 */
//	public ArrayList<Double> getTime()
//	{
//		return times;
//	}
//
//	/**
//	 *
//	 */
//	public void setTime(ArrayList<Double> times)
//	{
//		this.times = times;
//	}
//
//	/**
//	 *
//	 */
//	public ArrayList<Double> getX()
//	{
//		return x;
//	}
//
//	/**
//	 *
//	 */
//	public void setX(ArrayList<Double> x)
//	{
//		this.x = x;
//	}
//
//	/**
//	 *
//	 */
//	public ArrayList<Double> getY()
//	{
//		return y;
//	}
//
//	/**
//	 *
//	 */
//	public void setY(ArrayList<Double> y)
//	{
//		this.y = y;
//	}
//
//	/**
//	 *
//	 */
//	public ArrayList<Double> getZ()
//	{
//		return z;
//	}
//
//	/**
//	 *
//	 */
//	public void setZ(ArrayList<Double> z)
//	{
//		this.z = z;
//	}

	/**
	 *
	 */
	public StandardTrajectory()
	{
		super();
//		this.x = new ArrayList<Double>();
//		this.y = new ArrayList<Double>();
//		this.z = new ArrayList<Double>();
//		this.times = new ArrayList<Double>();
//		name = "";
		description = "";
		this.color = new double[]
		{ 0, 255, 255, 255 };
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
	@Override
	public double[] getTrajectoryColor()
	{
		// TODO Auto-generated method stub
		return color;
	}

	/**
	 *
	 */
	@Override
	public double getTrajectoryThickness()
	{
		// TODO Auto-generated method stub
		return thickness;
	}

	/**
	 * @param color
	 */
	public void setColor(double[] color)
	{
		this.color = color;
	}

	/**
	 * @param thickness
	 */
	public void setThickness(double thickness)
	{
		this.thickness = thickness;
	}

	/**
	 *
	 */
	@Override
	public void setTrajectoryColor(double[] color)
	{
		this.color = color;
	}

	/**
	 *
	 */
	@Override
	public void setTrajectoryDescription(String desc)
	{
		this.description = desc;
	}

	/**
	 *
	 */
	@Override
	public String getTrajectoryDescription()
	{
		return description;
	}

	/**
	 *
	 */
	@Override
	public void setTrajectoryLineThickness(double thickness)
	{
		this.thickness = thickness;
	}

	/**
	 *
	 */
	public boolean isFaded()
	{
		return isFaded;
	}

	/**
	 *
	 */
	public void setFaded(boolean isFaded)
	{
		this.isFaded = isFaded;
		this.color[3] = isFaded ? 50 : 255;
	}

//	@Override
//	public void addPositionAtTime(double[] spacecraftPosition, double time)
//	{
//		getX().add(spacecraftPosition[0]);
//		getY().add(spacecraftPosition[1]);
//		getZ().add(spacecraftPosition[2]);
//		getTime().add(time);
//	}

	// Display fraction (which portions of the overall time window are being
	// worked on)

	/**
	 *
	 */
	@Override
	public double getMinDisplayFraction()
	{
		return minDisplayFraction;
	}

	/**
	 *
	 */
	@Override
	public void setMinDisplayFraction(double minDisplayFraction)
	{
		this.minDisplayFraction = minDisplayFraction;
	}

	/**
	 *
	 */
	@Override
	public double getMaxDisplayFraction()
	{
		return maxDisplayFraction;
	}

	/**
	 *
	 */
	@Override
	public void setMaxDisplayFraction(double maxDisplayFraction)
	{
		this.maxDisplayFraction = maxDisplayFraction;
	}

	@Override
	public IPointingProvider getPointingProvider()
	{
		return pointingProvider;
	}

	@Override
	public void setPointingProvider(IPointingProvider pointingProvider)
	{
		this.pointingProvider = pointingProvider;
	}

	@Override
	public void setStartTime(double time)
	{
		this.startTime = time;
	}

	@Override
	public double getStartTime()
	{
		return startTime;
	}

	@Override
	public void setStopTime(double time)
	{
		this.stopTime = time;
	}

	@Override
	public double getStopTime()
	{
		return stopTime;
	}

	/**
	 * @return the numPoints
	 */
	public int getNumPoints()
	{
		return numPoints;
	}

	/**
	 * @param numPoints the numPoints to set
	 */
	public void setNumPoints(int numPoints)
	{
		this.numPoints = numPoints;
	}

	/**
	 * @return the hasInstrumentPointingInfo
	 */
	public boolean isHasInstrumentPointingInfo()
	{
		return pointingProvider instanceof SpicePointingProvider;
	}

}