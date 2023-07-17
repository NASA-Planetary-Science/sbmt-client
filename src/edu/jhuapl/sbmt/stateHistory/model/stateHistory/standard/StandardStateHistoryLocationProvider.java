package edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;

import altwg.util.MathUtil;
import crucible.core.math.vectorspace.UnwritableVectorIJK;

public class StandardStateHistoryLocationProvider implements IStateHistoryLocationProvider
{
    /**
    *
    */
	public static final double epsilon = 0.0000001;

	/**
	 *
	 */
	private String sourceFile;

	/**
	*
	*/
	private NavigableMap<Double, State> timeToStateMap = new TreeMap<Double, State>();

	private IPointingProvider pointingProvider;

	private StateHistory stateHistory;

	public StandardStateHistoryLocationProvider(StateHistory history)
	{
		this.stateHistory = history;
	}

	@Override
	public double[] getSpacecraftPosition()
	{
		return getSpacecraftPositionAtTime(stateHistory.getMetadata().getCurrentTime());
	}

	@Override
	public double[] getSpacecraftPositionAtTime(double time)
	{
		// System.out.println("StandardStateHistory:
		// getSpacecraftPositionAtTime: time " + time);
		State floor = getStateBeforeOrAtTime(time).getValue();
		if (getStateAtOrAfter(time) == null)
			return floor.getSpacecraftPosition();
		State ceiling = getStateAtOrAfter(time).getValue();
		double[] floorPosition = floor.getSpacecraftPosition();
		double[] ceilingPosition = ceiling.getSpacecraftPosition();
		double floorTime = floor.getEphemerisTime();
		double ceilingTime = ceiling.getEphemerisTime();

		return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, time);

	}

	@Override
	public double[] getInstrumentLookDirection(String instrumentName)
	{
		return getCurrentState().getInstrumentLookDirection(instrumentName);
	}

	@Override
	public double[] getInstrumentLookDirectionAtTime(String instrumentName, double time)
	{
		return getStateAtTime(time).getInstrumentLookDirection(instrumentName);
	}

	@Override
	public UnwritableVectorIJK getFrustum(String instrumentName, int index)
	{
		return getCurrentState().getFrustum(instrumentName, index);
	}

	@Override
	public UnwritableVectorIJK getFrustumAtTime(String instrumentName, int index, double time)
	{
		return getStateAtTime(time).getFrustum(instrumentName, index);
	}

	@Override
	public double[] getSunPosition()
	{
		double currentTime = stateHistory.getMetadata().getCurrentTime();
		State floor = getStateBeforeOrAtTime(currentTime).getValue();
		if (getStateAtOrAfter(currentTime) == null)
			return floor.getSunPosition();
		State ceiling = getStateAtOrAfter(currentTime).getValue();
		double[] floorPosition = floor.getSunPosition();
		double[] ceilingPosition = ceiling.getSunPosition();
		double floorTime = floor.getEphemerisTime();
		double ceilingTime = ceiling.getEphemerisTime();

		return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, currentTime);
	}

	@Override
	public double[] getEarthPosition()
	{
		double currentTime = stateHistory.getMetadata().getCurrentTime();
		State floor = getStateBeforeOrAtTime(currentTime).getValue();
		if (getStateAtOrAfter(currentTime) == null)
			return floor.getEarthPosition();
		State ceiling = getStateAtOrAfter(currentTime).getValue();
		double[] floorPosition = floor.getEarthPosition();
		double[] ceilingPosition = ceiling.getEarthPosition();
		double floorTime = floor.getEphemerisTime();
		double ceilingTime = ceiling.getEphemerisTime();

		return interpolateDouble(floorPosition, ceilingPosition, floorTime, ceilingTime, currentTime);
	}

	/**
	*
	*/
	@Override
	public void addState(State flybyState)
	{
		addStateAtTime(flybyState.getEphemerisTime(), flybyState);
	}

	/**
	*
	*/
	@Override
	public void addStateAtTime(Double time, State flybyState)
	{
		timeToStateMap.put(time, flybyState);
	}

	/**
	*
	*/
	@Override
	public Entry<Double, State> getStateBeforeOrAtTime(Double time)
	{
		return timeToStateMap.floorEntry(time);
	}

	/**
	*
	*/
	@Override
	public Entry<Double, State> getStateAtOrAfter(Double time)
	{
		return timeToStateMap.ceilingEntry(time);
	}

	/**
	*
	*/
	@Override
	public State getStateAtTime(Double time)
	{
		// for now, just return floor
		return getStateBeforeOrAtTime(time).getValue();
	}

	/**
	*
	*/
	@Override
	public State getCurrentState()
	{
		// for now, just return floor
		return getStateAtTime(stateHistory.getMetadata().getCurrentTime());
	}

	@Override
	public Set<Double> getAllTimes()
	{
		return timeToStateMap.keySet();
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
	public void reloadPointingProvider() throws StateHistoryIOException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}

	@Override
	public String getSourceFile()
	{
		return sourceFile;
	}

	/**
	 * @param floorPosition
	 * @param ceilingPosition
	 * @param floorTime
	 * @param ceilingTime
	 * @param time
	 * @return
	 */
	private double[] interpolateDouble(double[] floorPosition, double[] ceilingPosition, double floorTime,
			double ceilingTime, double time)
	{
		double timeDelta = ceilingTime - floorTime;
		if (timeDelta < epsilon)
		{
			return floorPosition;
		}
		else
		{
			// System.out.println(floorPosition[0] + " " + floorPosition[1] + "
			// " + floorPosition[2]);
			// System.out.println(ceilingPosition[0] + " " + ceilingPosition[1]
			// + " " + ceilingPosition[2]);
			double timeFraction = (time - floorTime) / timeDelta;
			double[] positionDelta = new double[3];
			MathUtil.vsub(ceilingPosition, floorPosition, positionDelta);
			double[] positionFraction = new double[3];
			MathUtil.vscl(timeFraction, positionDelta, positionFraction);
			double[] result = new double[3];
			// System.out.println("Time: " + time + " FloorTime: " + floorTime +
			// " timeDelta: " + timeDelta);
			// System.out.println("TF: " + timeFraction);
			MathUtil.vadd(floorPosition, positionFraction, result);
			// System.out.println(result[0] + " " + result[1] + " " +
			// result[2]);
			return result;
		}
	}

	@Override
	public boolean validate()
	{
		return true;
	}

	/**
	 * @return the timeToStateMap
	 */
	public NavigableMap<Double, State> getTimeToStateMap()
	{
		return timeToStateMap;
	}

}
