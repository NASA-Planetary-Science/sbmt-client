package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;

import altwg.util.MathUtil;
import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Standard class for holding history information.  A timeToStateMap keeps a correlation between ephemeris time
 * and an object that obeys the <pre>State</pre> interface.
 *
 * This class contains a reference to an object that implements the <pre>Trajectory</pre> interface, which helps describe
 * how this state history can be rendered.
 * @author steelrj1
 *
 */
public class StandardStateHistory implements StateHistory
{
    /**
     *
     */
    private NavigableMap<Double, State> timeToStateMap = new TreeMap<Double, State>();

    /**
     *
     */
    private Double currentTime;

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
    private String name = "";

    /**
     *
     */
    private String description = "";

    /**
     *
     */
    private Double[] color;

    private StateHistorySourceType type;

    private String sourceFile;

    private IPointingProvider pointingProvider;


    //Metadata Information
    private static final Key<StandardStateHistory> STANDARD_STATE_HISTORY_KEY = Key.of("StandardStateHistory");
	private static final Key<StateHistoryKey> STATEHISTORY_KEY_KEY = Key.of("key");
	private static final Key<Double> CURRENT_TIME_KEY = Key.of("currentTime");
	private static final Key<Double> START_TIME_KEY = Key.of("startTime");
	private static final Key<Double> END_TIME_KEY = Key.of("stopTime");
	private static final Key<String> STATE_HISTORY_NAME_KEY = Key.of("name");
	private static final Key<String> STATE_HISTORY_DESCRIPTION_KEY = Key.of("description");
	private static final Key<Double[]> COLOR_KEY = Key.of("color");
	private static final Key<String> TYPE_KEY = Key.of("type");
	private static final Key<String> SOURCE_FILE = Key.of("sourceFile");

    public static void initializeSerializationProxy()
	{
    	InstanceGetter.defaultInstanceGetter().register(STANDARD_STATE_HISTORY_KEY, (source) -> {

    		StateHistoryKey key = source.get(STATEHISTORY_KEY_KEY);
    		Double currentTime = source.get(CURRENT_TIME_KEY);
    		Double startTime = source.get(START_TIME_KEY);
    		Double endTime = source.get(END_TIME_KEY);
    		String name = source.get(STATE_HISTORY_NAME_KEY);
    		StateHistorySourceType type = StateHistorySourceType.valueOf(source.get(TYPE_KEY));
    		String sourceFile = source.get(SOURCE_FILE);
    		String description = "";
    		try
    		{
    			description = source.get(STATE_HISTORY_DESCRIPTION_KEY);
    		}
    		catch (IllegalArgumentException iae) {}
    		if (name == null) name = "";
    		Double[] color = source.get(COLOR_KEY);

    		StandardStateHistory stateHistory = new StandardStateHistory(key, currentTime, startTime, endTime, name, description, color, type, sourceFile);
    		return stateHistory;

    	}, StandardStateHistory.class, stateHistory -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(STATEHISTORY_KEY_KEY, stateHistory.getKey());
    		result.put(CURRENT_TIME_KEY, stateHistory.getCurrentTime());
    		result.put(START_TIME_KEY, stateHistory.getMinTime());
    		result.put(END_TIME_KEY, stateHistory.getMaxTime());
    		result.put(STATE_HISTORY_NAME_KEY, stateHistory.getStateHistoryName());
    		result.put(STATE_HISTORY_DESCRIPTION_KEY, stateHistory.getStateHistoryDescription());
    		result.put(TYPE_KEY, stateHistory.getType().toString());
    		result.put(SOURCE_FILE, stateHistory.getSourceFile());
    		result.put(COLOR_KEY, new Double[] { stateHistory.getTrajectory().getTrajectoryColor()[0], stateHistory.getTrajectory().getTrajectoryColor()[1], stateHistory.getTrajectory().getTrajectoryColor()[2], stateHistory.getTrajectory().getTrajectoryColor()[3]});
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

    /**
     * @param key
     * @param currentTime
     * @param startTime
     * @param endTime
     * @param name
     * @param color
     */
    public StandardStateHistory(StateHistoryKey key, Double currentTime, Double startTime, Double endTime, String name, String description, Double[] color, StateHistorySourceType type, String sourceFile)
    {
    	this.key = key;
    	this.currentTime = currentTime;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	this.color = color;
    	this.name = name;
    	this.description = description;
    	this.type = type;
    	this.sourceFile = sourceFile;
    }

    /**
     *
     */
    public StateHistoryKey getKey()
    {
    	return key;
    }

    public StateHistorySourceType getType()
	{
		return type;
	}

	public String getSourceFile()
	{
		return sourceFile;
	}

	public void setType(StateHistorySourceType type)
	{
		this.type = type;
	}

	/**
     *
     */
    public Double getCurrentTime()
    {
        return currentTime;
    }

    /**
     *
     */
    public void setCurrentTime(Double dt) throws StateHistoryInvalidTimeException
    {
        if( dt < getMinTime() || dt > getMaxTime())
        {
        	throw new StateHistoryInvalidTimeException("Entered time is outside the range of the selected interval.");
//            JOptionPane.showMessageDialog(null, "Entered time is outside the range of the selected interval.", "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
        }
//        Interval interval1 = new Interval(getMinTime().longValue(), dt.longValue());
//        Interval interval2 = new Interval(getMinTime().longValue(), getMaxTime().longValue());

//        org.joda.time.Duration duration1 = interval1.toDuration();
//        org.joda.time.Duration duration2 = interval2.toDuration();

//        BigDecimal num1 = new BigDecimal(duration1.getMillis());
//        BigDecimal num2 = new BigDecimal(duration2.getMillis());
//        BigDecimal tf = num1.divide(num2,50,RoundingMode.UP);
        this.currentTime = dt;
//        this.time = Double.parseDouble(tf.toString());
    }

    /**
     *
     */
    public Double getMinTime()
    {
    	if (startTime != null) return startTime;
        return timeToStateMap.firstKey();
    }


    public Double getMaxTime()
    {
    	if (endTime != null) return endTime;
        return timeToStateMap.lastKey();
    }

    /**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Double startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Double endTime)
	{
		this.endTime = endTime;
	}

    /**
     *
     */
    public Double getTimeFraction()
    {
        double min = getMinTime() + trajectory.getMinDisplayFraction()*(getMaxTime() - getMinTime());
        double max = getMaxTime() - (1-trajectory.getMaxDisplayFraction())*(getMaxTime()-getMinTime());
        double time = getCurrentTime();
        double result = (time - min) / (max - min);
        return result;
    }

    /**
     *
     */
    public void setTimeFraction(Double timeFraction) throws StateHistoryInvalidTimeException
    {
        double min = getMinTime() + trajectory.getMinDisplayFraction()*(getMaxTime() - getMinTime());
        double max = getMaxTime() - (1-trajectory.getMaxDisplayFraction())*(getMaxTime()-getMinTime());
        double time = min + timeFraction * (max - min);
        setCurrentTime(time);
    }

    /**
     *
     */
    public void addState(State flybyState)
    {
        addStateAtTime(flybyState.getEphemerisTime(), flybyState);
    }

    /**
     *
     */
    public void addStateAtTime(Double time, State flybyState)
    {
        timeToStateMap.put(time, flybyState);
    }

    /**
     *
     */
    public Entry<Double, State> getStateBeforeOrAtTime(Double time)
    {
        return timeToStateMap.floorEntry(time);
    }

    /**
     *
     */
    public Entry<Double, State> getStateAtOrAfter(Double time)
    {
        return timeToStateMap.ceilingEntry(time);
    }

    /**
     *
     */
    public State getStateAtTime(Double time)
    {
        // for now, just return floor
        return getStateBeforeOrAtTime(time).getValue();
    }

    /**
     *
     */
    public State getCurrentState()
    {
        // for now, just return floor
        return getStateAtTime(getCurrentTime());
    }

    /**
     *
     */
    public Double getTimeWindow()
    {
        return getMaxTime() - getMinTime();
    }


    //Heavenly body position getters

    @Override
    public double[] getSpacecraftPositionAtTime(double time)
    {
//    	System.out.println("StandardStateHistory: getSpacecraftPositionAtTime: time " + time);
    	State floor = getStateBeforeOrAtTime(time).getValue();
    	if (getStateAtOrAfter(time) == null) return floor.getSpacecraftPosition();
        State ceiling = getStateAtOrAfter(time).getValue();
        double[] floorPosition = floor.getSpacecraftPosition();
        double[] ceilingPosition = ceiling.getSpacecraftPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, time);
    }
    /**
     *
     */
    public double[] getSpacecraftPosition()
    {
        return getSpacecraftPositionAtTime(currentTime);
    }

    /**
     *
     */
    public double[] getSunPosition()
    {
        State floor = getStateBeforeOrAtTime(currentTime).getValue();
        if (getStateAtOrAfter(currentTime) == null) return floor.getSunPosition();
        State ceiling = getStateAtOrAfter(currentTime).getValue();
        double[] floorPosition = floor.getSunPosition();
        double[] ceilingPosition = ceiling.getSunPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, currentTime);
    }

    /**
     *
     */
    public double[] getEarthPosition()
    {
        State floor = getStateBeforeOrAtTime(currentTime).getValue();
        if (getStateAtOrAfter(currentTime) == null) return floor.getEarthPosition();
        State ceiling = getStateAtOrAfter(currentTime).getValue();
        double[] floorPosition = floor.getEarthPosition();
        double[] ceilingPosition = ceiling.getEarthPosition();
        double floorTime = floor.getEphemerisTime();
        double ceilingTime = ceiling.getEphemerisTime();

        return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, currentTime);
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

    @Override
    public Set<Double> getAllTimes()
    {
        return timeToStateMap.keySet();
    }

	@Override
	public Trajectory getTrajectory()
	{
		return trajectory;
	}

	@Override
	public void setTrajectory(Trajectory trajectory)
	{
		this.trajectory = trajectory;
	}

	@Override
	public void setTrajectoryColor(Double[] color)
	{
		this.color = color;
		this.trajectory.setTrajectoryColor(new double[] {color[0], color[1], color[2], color[3]});
	}

	@Override
	public String getStateHistoryName()
	{
		return name;
	}

	@Override
	public void setStateHistoryName(String name)
	{
		this.name = name;
	}

	@Override
	public String getStateHistoryDescription()
	{
		return description;
	}

	@Override
	public void setStateHistoryDescription(String description)
	{
		this.description = description;
	}

	@Override
	public double[] getInstrumentLookDirection(String instrumentFrameName)
	{
		double[] lookDir = getCurrentState().getInstrumentLookDirection(instrumentFrameName);
		return lookDir;
	}

	@Override
	public double[] getInstrumentLookDirectionAtTime(String instrumentFrameName, double time)
	{
		double[] lookDir = getStateAtTime(time).getInstrumentLookDirection(instrumentFrameName);
		return lookDir;
	}

	@Override
	public UnwritableVectorIJK getFrustum(String instrumentFrameName, int index)
	{
		return getCurrentState().getFrustum(instrumentFrameName, index);
	}

	@Override
	public UnwritableVectorIJK getFrustumAtTime(String instrumentFrameName, int index, double time)
	{
//		System.out.println("StandardStateHistory: getFrustumAtTime: getting frustum at time " + time);
		return getStateAtTime(time).getFrustum(instrumentFrameName, index);
	}

	@Override
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
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
}