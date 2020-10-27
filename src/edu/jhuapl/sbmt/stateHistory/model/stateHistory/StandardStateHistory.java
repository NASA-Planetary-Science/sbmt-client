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
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    private Double currentTime;

    /**
     *
     */
    @Setter
    private Double startTime;

    /**
     *
     */
    @Setter
    private Double endTime;

    /**
     *
     */
    @Getter
    private StateHistoryKey key;

    /**
     *
     */
    @Getter @Setter
    private Trajectory trajectory;

    /**
     *
     */
    @Getter @Setter
    private String stateHistoryName = "";

    /**
     *
     */
    @Getter @Setter
    private String stateHistoryDescription = "";

    /**
     *
     */
    private Double[] color;

    @Getter @Setter
    private StateHistorySourceType type;

    @Getter @Setter
    private String sourceFile;

    @Getter @Setter
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
    		result.put(START_TIME_KEY, stateHistory.getStartTime());
    		result.put(END_TIME_KEY, stateHistory.getEndTime());
    		result.put(STATE_HISTORY_NAME_KEY, stateHistory.getStateHistoryName());
    		result.put(STATE_HISTORY_DESCRIPTION_KEY, stateHistory.getStateHistoryDescription());
    		result.put(TYPE_KEY, stateHistory.getType().toString());
    		result.put(SOURCE_FILE, stateHistory.getSourceFile());
    		result.put(COLOR_KEY, new Double[] { stateHistory.getTrajectory().getColor()[0], stateHistory.getTrajectory().getColor()[1], stateHistory.getTrajectory().getColor()[2], stateHistory.getTrajectory().getColor()[3]});
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
    	this.stateHistoryName = name;
    	this.stateHistoryDescription = description;
    	this.type = type;
    	this.sourceFile = sourceFile;
    }

    /**
     *
     */
    public void setCurrentTime(Double dt) throws StateHistoryInvalidTimeException
    {
        if( dt < getStartTime() || dt > getEndTime())
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
    public Double getStartTime()
    {
    	if (startTime != null) return startTime;
        return timeToStateMap.firstKey();
    }


    public Double getEndTime()
    {
    	if (endTime != null) return endTime;
        return timeToStateMap.lastKey();
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
        return getEndTime() - getStartTime();
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
	public void setTrajectoryColor(Double[] color)
	{
		this.color = color;
		this.trajectory.setColor(new double[] {color[0], color[1], color[2], color[3]});
	}

	@Override
	public double[] getInstrumentLookDirection(String instrumentFrameName)
	{
		return getCurrentState().getInstrumentLookDirection(instrumentFrameName);
	}

	@Override
	public double[] getInstrumentLookDirectionAtTime(String instrumentFrameName, double time)
	{
		return getStateAtTime(time).getInstrumentLookDirection(instrumentFrameName);
	}

	@Override
	public UnwritableVectorIJK getFrustum(String instrumentFrameName, int index)
	{
		return getCurrentState().getFrustum(instrumentFrameName, index);
	}

	@Override
	public UnwritableVectorIJK getFrustumAtTime(String instrumentFrameName, int index, double time)
	{
		return getStateAtTime(time).getFrustum(instrumentFrameName, index);
	}
}