package edu.jhuapl.sbmt.stateHistory.model.trajectory;

import java.util.ArrayList;

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
    private String name;
    /**
     *
     */
    private int id;
    /**
     *
     */
    private ArrayList<Double> x;
    /**
     *
     */
    private ArrayList<Double> y;
    /**
     *
     */
    private ArrayList<Double> z;
    /**
     *
     */
    private ArrayList<Double> times;
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
    public String getName()
    {
        return name;
    }


    /**
     *
     */
    public void setName(String name)
    {
        this.name = name;
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

    /**
     *
     */
    public ArrayList<Double> getTime()
    {
        return times;
    }


    /**
     *
     */
    public void setTime(ArrayList<Double> times)
    {
        this.times = times;
    }

    /**
     *
     */
    public ArrayList<Double> getX()
    {
        return x;
    }


    /**
     *
     */
    public void setX(ArrayList<Double> x)
    {
        this.x = x;
    }


    /**
     *
     */
    public ArrayList<Double> getY()
    {
        return y;
    }


    /**
     *
     */
    public void setY(ArrayList<Double> y)
    {
        this.y = y;
    }


    /**
     *
     */
    public ArrayList<Double> getZ()
    {
        return z;
    }


    /**
     *
     */
    public void setZ(ArrayList<Double> z)
    {
        this.z = z;
    }

    /**
     *
     */
    public StandardTrajectory()
    {
        super();
        this.x = new ArrayList<Double>();
        this.y = new ArrayList<Double>();
        this.z = new ArrayList<Double>();
        this.times = new ArrayList<Double>();
        name = "";
        description = "";
        this.color = new double[] {0, 255, 255, 255};
    }

    /**
     *
     */
    public String toString()
    {
        return "Trajectory " + getId() + " = " + getName() + " contains " + getX().size() + " vertices";

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
	public void setTrajectoryName(String name)
	{
		this.name = name;
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
}