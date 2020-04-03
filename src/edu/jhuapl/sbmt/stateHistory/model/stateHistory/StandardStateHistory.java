package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.joda.time.Interval;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;

import altwg.util.MathUtil;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class StandardStateHistory implements StateHistory
{
    /**
     *
     */
    private NavigableMap<Double, State> timeToFlybyState = new TreeMap<Double, State>();

    /**
     *
     */
    private Double time;

    /**
     *
     */
    private Double startTime;

    /**
     *
     */
    private Double endTime;

    /**
     *
     */
    private StateHistoryKey key;

    /**
     *
     */
    private Trajectory trajectory;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private Double[] color;

    /**
     *
     */
    private double minDisplayFraction = 0.0, maxDisplayFraction = 1.0;
//    private double[] trajectoryColor;
//    private String trajectoryName;
//    private String trajectoryDescription;
//    private double trajectoryThickness;

    //Metadata Information
    private static final Key<StandardStateHistory> STANDARD_STATE_HISTORY_KEY = Key.of("StandardStateHistory");
	private static final Key<StateHistoryKey> STATEHISTORY_KEY_KEY = Key.of("key");
	private static final Key<Double> CURRENT_TIME_KEY = Key.of("currentTime");
	private static final Key<Double> START_TIME_KEY = Key.of("startTime");
	private static final Key<Double> END_TIME_KEY = Key.of("stopTime");
	private static final Key<String> STATE_HISTORY_NAME_KEY = Key.of("name");
	private static final Key<Double[]> COLOR_KEY = Key.of("color");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(STANDARD_STATE_HISTORY_KEY, (source) -> {

    		StateHistoryKey key = source.get(STATEHISTORY_KEY_KEY);
    		Double currentTime = source.get(CURRENT_TIME_KEY);
    		Double startTime = source.get(START_TIME_KEY);
    		Double endTime = source.get(END_TIME_KEY);
    		String name = source.get(STATE_HISTORY_NAME_KEY);
    		Double[] color = source.get(COLOR_KEY);

    		StandardStateHistory stateHistory = new StandardStateHistory(key, currentTime, startTime, endTime, name, color);
    		return stateHistory;

    	}, StandardStateHistory.class, stateHistory -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(STATEHISTORY_KEY_KEY, stateHistory.getKey());
    		result.put(CURRENT_TIME_KEY, stateHistory.getTime());
    		result.put(START_TIME_KEY, stateHistory.getMinTime());
    		result.put(END_TIME_KEY, stateHistory.getMaxTime());
    		result.put(STATE_HISTORY_NAME_KEY, stateHistory.getTrajectoryName());
    		result.put(COLOR_KEY, new Double[] { stateHistory.getTrajectoryColor()[0], stateHistory.getTrajectoryColor()[1], stateHistory.getTrajectoryColor()[2], stateHistory.getTrajectoryColor()[3]});
    		return result;
    	});

	}

    /**
     * @param key
     */
    public StandardStateHistory(StateHistoryKey key)
    {
    	this.key = key;
    }

    public StandardStateHistory(StateHistoryKey key, Double currentTime, Double startTime, Double endTime, String name, Double[] color)
    {
    	this.key = key;
    	this.time = currentTime;
//    	this.setTrajectoryName(name);
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.color = color;
    }

    /**
     *
     */
    public StateHistoryKey getKey()
    {
    	return key;
    }

    /**
     *
     */
    public Double getTime()
    {
        return time;
    }

    /**
     *
     */
    public void setTime(Double dt)
    {
        if( dt < getMinTime() || dt > getMaxTime())
        {
            JOptionPane.showMessageDialog(null, "Entered time is outside the range of the selected interval.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        Interval interval1 = new Interval(getMinTime().longValue(), dt.longValue());
        Interval interval2 = new Interval(getMinTime().longValue(), getMaxTime().longValue());

        org.joda.time.Duration duration1 = interval1.toDuration();
        org.joda.time.Duration duration2 = interval2.toDuration();

        BigDecimal num1 = new BigDecimal(duration1.getMillis());
        BigDecimal num2 = new BigDecimal(duration2.getMillis());
        BigDecimal tf = num1.divide(num2,50,RoundingMode.UP);
        this.time = dt;
//        this.time = Double.parseDouble(tf.toString());
    }

    /**
     *
     */
    public Double getMinTime()
    {
    	if (startTime != null) return startTime;
        return timeToFlybyState.firstKey();
    }

    /**
     *
     */
    public Double getMaxTime()
    {
    	if (endTime != null) return endTime;
        return timeToFlybyState.lastKey();
    }

    /**
     *
     */
    public Double getTimeFraction()
    {
        double min = getMinTime() + minDisplayFraction*(getMaxTime() - getMinTime());
        double max = getMaxTime() - (1-maxDisplayFraction)*(getMaxTime()-getMinTime());
        double time = getTime();
        double result = (time - min) / (max - min);
        return result;
    }

    /**
     *
     */
    public void setTimeFraction(Double timeFraction)
    {
        double min = getMinTime() + minDisplayFraction*(getMaxTime() - getMinTime());
        double max = getMaxTime() - (1-maxDisplayFraction)*(getMaxTime()-getMinTime());
        double time = min + timeFraction * (max - min);
        setTime(time);
    }

    /**
     *
     */
    public void put(State flybyState)
    {
        put(flybyState.getEphemerisTime(), flybyState);
    }

    /**
     *
     */
    public void put(Double time, State flybyState)
    {
        timeToFlybyState.put(time, flybyState);
    }

    /**
     *
     */
    public Entry<Double, State> getFloorEntry(Double time)
    {
        return timeToFlybyState.floorEntry(time);
    }

    /**
     *
     */
    public Entry<Double, State> getCeilingEntry(Double time)
    {
        return timeToFlybyState.ceilingEntry(time);
    }

    /**
     *
     */
    public State getValue(Double time)
    {
        // for now, just return floor
        return getFloorEntry(time).getValue();
    }

    /**
     *
     */
    public State getCurrentValue()
    {
        // for now, just return floor
        return getValue(getTime());
    }

    /**
     *
     */
    public Double getPeriod()
    {
        return getMaxTime() - getMinTime();
    }

    /**
     *
     */
    public double[] getSpacecraftPosition()
    {
        State floor = getFloorEntry(time).getValue();
        State ceiling = getCeilingEntry(time).getValue();
        double[] floorPosition = floor.getSpacecraftPosition();
        double[] ceilingPosition = ceiling.getSpacecraftPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, time);
    }

    /**
     *
     */
    public double[] getSunPosition()
    {
        State floor = getFloorEntry(time).getValue();
        State ceiling = getCeilingEntry(time).getValue();
        double[] floorPosition = floor.getSunPosition();
        double[] ceilingPosition = ceiling.getSunPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, time);
    }

    /**
     *
     */
    public double[] getEarthPosition()
    {
        State floor = getFloorEntry(time).getValue();
        State ceiling = getCeilingEntry(time).getValue();
        double[] floorPosition = floor.getEarthPosition();
        double[] ceilingPosition = ceiling.getEarthPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, time);
    }


    /**
     * @param floorPosition
     * @param ceilingPosition
     * @param floorTime
     * @param ceilingTime
     * @param time
     * @return
     */
    private double[] interpolateDouble(double[] floorPosition, double[] ceilingPosition, double floorTime, double ceilingTime, double time)
    {
        double timeDelta = ceilingTime - floorTime;
        if (timeDelta < epsilon)
        {
            return floorPosition;
        }
        else
        {
            //System.out.println(floorPosition[0] + " " + floorPosition[1] + " " + floorPosition[2]);
            //System.out.println(ceilingPosition[0] + " " + ceilingPosition[1] + " " + ceilingPosition[2]);
            double timeFraction = (time - floorTime) / timeDelta;
            double[] positionDelta = new double[3];
            MathUtil.vsub(ceilingPosition, floorPosition, positionDelta);
            double[] positionFraction = new double[3];
            MathUtil.vscl(timeFraction, positionDelta, positionFraction);
            double[] result = new double[3];
//            System.out.println("Time: " + time + " FloorTime: " + floorTime + " timeDelta: " + timeDelta);
//            System.out.println("TF: " + timeFraction);
            MathUtil.vadd(floorPosition, positionFraction, result);
            //System.out.println(result[0] + " " + result[1] + " " + result[2]);
            return result;
        }
    }

    /**
     *
     */
    @Override
    public Set<Double> getAllKeys()
    {
        return timeToFlybyState.keySet();

    }

	/**
	 *
	 */
	@Override
	public String getTrajectoryName()
	{
		return name;
//		return trajectory.getName();
	}

	/**
	 *
	 */
	@Override
	public String getTrajectoryDescription()
	{
		if (trajectory == null) return "";
		return trajectory.toString();
	}

	/**
	 *
	 */
	@Override
	public double[] getTrajectoryColor()
	{
		if (color == null) return trajectory.getTrajectoryColor();
		return new double[] {color[0], color[1], color[2], color[3]};
	}

	/**
	 *
	 */
	@Override
	public double getTrajectoryThickness()
	{
		return trajectory.getTrajectoryThickness();
	}

	/**
	 *
	 */
	@Override
	public Trajectory getTrajectory()
	{
		return trajectory;
	}

	/**
	 *
	 */
	public void setTrajectory(Trajectory trajectory)
	{
		this.trajectory = trajectory;
	}

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
	public void setTrajectoryName(String name)
	{
		this.trajectory.setName(name);
	}

	public void setTrajectoryColor(Double[] color)
	{
		this.color = color;
		this.trajectory.setTrajectoryColor(new double[] {color[0], color[1], color[2], color[3]});
	}

}
