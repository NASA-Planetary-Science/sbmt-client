package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.util.Map.Entry;
import java.util.Set;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.SpiceState;
import edu.jhuapl.sbmt.util.TimeUtil;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class SpiceStateHistory implements StateHistory
{

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

	private SpiceState state;

	private SpiceInfo spiceInfo;

	// Metadata Information
	private static final Key<SpiceStateHistory> SPICE_STATE_HISTORY_KEY = Key.of("SpiceStateHistory");
	private static final Key<StateHistoryKey> STATEHISTORY_KEY_KEY = Key.of("key");
	private static final Key<Double> CURRENT_TIME_KEY = Key.of("currentTime");
	private static final Key<Double> START_TIME_KEY = Key.of("startTime");
	private static final Key<Double> END_TIME_KEY = Key.of("stopTime");
	private static final Key<String> STATE_HISTORY_NAME_KEY = Key.of("name");
	private static final Key<String> STATE_HISTORY_DESCRIPTION_KEY = Key.of("description");
	private static final Key<Double[]> COLOR_KEY = Key.of("color");
	private static final Key<String> TYPE_KEY = Key.of("type");
	private static final Key<String> SOURCE_FILE = Key.of("sourceFile");
	private static final Key<SpiceInfo> SPICE_INFO_KEY = Key.of("spiceInfo");

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(SPICE_STATE_HISTORY_KEY, (source) ->
		{

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
			catch (IllegalArgumentException iae)
			{
			}
			if (name == null)
				name = "";
			Double[] color = source.get(COLOR_KEY);
			SpiceInfo spiceInfo = null;
			if (source.hasKey(SPICE_INFO_KEY))
				spiceInfo = source.get(SPICE_INFO_KEY);

			SpiceStateHistory stateHistory = new SpiceStateHistory(key, currentTime, startTime, endTime, name,
					description, color, type, sourceFile);
			stateHistory.setSpiceInfo(spiceInfo);
			return stateHistory;

		}, SpiceStateHistory.class, stateHistory ->
		{

			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
			result.put(STATEHISTORY_KEY_KEY, stateHistory.getKey());
			result.put(CURRENT_TIME_KEY, stateHistory.getCurrentTime());
			result.put(START_TIME_KEY, stateHistory.getMinTime());
			result.put(END_TIME_KEY, stateHistory.getMaxTime());
			result.put(STATE_HISTORY_NAME_KEY, stateHistory.getStateHistoryName());
			result.put(STATE_HISTORY_DESCRIPTION_KEY, stateHistory.getStateHistoryDescription());
			result.put(TYPE_KEY, stateHistory.getType().toString());
			result.put(SOURCE_FILE, stateHistory.getSourceFile());
			result.put(COLOR_KEY, new Double[]
			{ stateHistory.getTrajectory().getTrajectoryColor()[0],
					stateHistory.getTrajectory().getTrajectoryColor()[1],
					stateHistory.getTrajectory().getTrajectoryColor()[2],
					stateHistory.getTrajectory().getTrajectoryColor()[3] });
			result.put(SPICE_INFO_KEY, stateHistory.getSpiceInfo());
			return result;
		});
	}

	public SpiceStateHistory(StateHistoryKey key)
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
	public SpiceStateHistory(StateHistoryKey key, Double currentTime, Double startTime, Double endTime, String name,
			String description, Double[] color, StateHistorySourceType type, String sourceFile)
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

	@Override
	public StateHistorySourceType getType()
	{
		return type;
	}

	@Override
	public String getSourceFile()
	{
		return sourceFile;
	}

	@Override
	public Double getTimeWindow()
	{
		return getMaxTime() - getMinTime();
	}

//	@Override
//	public Double getTimeFraction()
//	{
//		double min = getMinTime() + trajectory.getMinDisplayFraction()*(getMaxTime() - getMinTime());
//        double max = getMaxTime() - (1-trajectory.getMaxDisplayFraction())*(getMaxTime()-getMinTime());
//        double time = getCurrentTime();
//        double result = (time - min) / (max - min);
//        return result;
//	}
//
//	@Override
//	public void setTimeFraction(Double timeFraction) throws StateHistoryInvalidTimeException
//	{
//		double min = getMinTime() + trajectory.getMinDisplayFraction()*(getMaxTime() - getMinTime());
//        double max = getMaxTime() - (1-trajectory.getMaxDisplayFraction())*(getMaxTime()-getMinTime());
//        double time = min + timeFraction * (max - min);
////        System.out.println("SpiceStateHistory: setTimeFraction: srtting time " + time);
//        setCurrentTime(time);
//	}

	@Override
	public Double getCurrentTime()
	{
//		System.out.println("SpiceStateHistory: getCurrentTime: returning " + currentTime);
		return currentTime;
	}

	@Override
	public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
	{
		if( time < getMinTime() || time > getMaxTime())
        {
			System.out.println("SpiceStateHistory: setCurrentTime: time is " + TimeUtil.et2str(time));
			System.out.println("SpiceStateHistory: setCurrentTime: min time is " + TimeUtil.et2str(getMinTime()));
			System.out.println("SpiceStateHistory: setCurrentTime: max time is " + TimeUtil.et2str(getMaxTime()));
        	throw new StateHistoryInvalidTimeException("Entered time is outside the range of the selected interval.");

        }
		this.currentTime = time;
		state.setEphemerisTime(time);
	}

	@Override
	public Double getMinTime()
	{
		return startTime;
	}

	@Override
	public Double getMaxTime()
	{
		return endTime;
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

	@Override
	public void addState(State flybyState)
	{
		this.state = (SpiceState)flybyState;

	}

	@Override
	public void addStateAtTime(Double time, State flybyState)
	{
		this.state = (SpiceState)flybyState;
	}

	@Override
	public Entry<Double, State> getStateBeforeOrAtTime(Double time)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<Double, State> getStateAtOrAfter(Double time)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State getStateAtTime(Double time)
	{
		return state;
	}

	@Override
	public State getCurrentState()
	{
		return state;
	}

	@Override
	public Set<Double> getAllTimes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StateHistoryKey getKey()
	{
		return key;
	}

	@Override
	public double[] getSpacecraftPosition()
	{
		return state.getSpacecraftPosition();
	}

	@Override
	public double[] getSpacecraftPositionAtTime(double time)
	{
		state.setEphemerisTime(time);
		return state.getSpacecraftPosition();
	}

	@Override
	public double[] getInstrumentLookDirection(String instrumentFrameName)
	{
		return state.getInstrumentLookDirection(instrumentFrameName);
	}

	@Override
	public double[] getInstrumentLookDirectionAtTime(String instrumentFrameName, double time)
	{
		state.setEphemerisTime(time);
		return state.getInstrumentLookDirection(instrumentFrameName);
	}

	@Override
	public UnwritableVectorIJK getFrustum(String instrumentFrameName, int index)
	{
		return state.getFrustum(instrumentFrameName, index);
	}

	@Override
	public UnwritableVectorIJK getFrustumAtTime(String instrumentFrameName, int index, double time)
	{
		state.setEphemerisTime(time);
		return state.getFrustum(instrumentFrameName, index);
	}

	@Override
	public double[] getSunPosition()
	{
		return state.getSunPosition();
	}

	@Override
	public double[] getEarthPosition()
	{
		return state.getEarthPosition();
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
	public Trajectory getTrajectory()
	{
		return trajectory;
	}

	@Override
	public void setTrajectory(Trajectory traj)
	{
		this.trajectory = traj;
	}

	@Override
	public void setTrajectoryColor(Double[] color)
	{
		this.color = color;
		this.trajectory.setTrajectoryColor(new double[] {color[0], color[1], color[2], color[3]});
	}

	@Override
	public void setType(StateHistorySourceType type)
	{
		this.type = type;
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

	/**
	 * @return the spiceInfo
	 */
	public SpiceInfo getSpiceInfo()
	{
		return spiceInfo;
	}

	/**
	 * @param spiceInfo the spiceInfo to set
	 */
	public void setSpiceInfo(SpiceInfo spiceInfo)
	{
		this.spiceInfo = spiceInfo;
	}

//	@Override
//	public double getCurrentMinValue()
//    {
//    	return getMinTime() + trajectory.getMinDisplayFraction()*(getMaxTime() - getMinTime());
//    }
//
//	@Override
//    public double getCurrentMaxValue()
//    {
//    	return getMaxTime() - (1-trajectory.getMaxDisplayFraction())*(getMaxTime()-getMinTime());
//    }

}
