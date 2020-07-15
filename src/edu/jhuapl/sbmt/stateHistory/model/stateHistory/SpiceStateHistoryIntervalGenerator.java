package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.pointing.Pointing;
import edu.jhuapl.sbmt.pointing.PointingProvider;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.SpiceState;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;
import edu.jhuapl.sbmt.util.TimeUtil;

import crucible.core.mechanics.EphemerisID;
import crucible.core.mechanics.FrameID;
import crucible.core.mechanics.utilities.SimpleEphemerisID;
import crucible.core.mechanics.utilities.SimpleFrameID;
import crucible.core.time.TimeSystem;
import crucible.core.time.TimeSystems;
import crucible.core.time.UTCEpoch;

/**
 * Class to generate a state history interval using SPICE kernels
 *
 * @author steelrj1
 *
 */
public class SpiceStateHistoryIntervalGenerator implements IStateHistoryIntervalGenerator
{
	private double cadence;
	private PointingProvider pointingProvider;
    protected static final TimeSystems DefaultTimeSystems = TimeSystems.builder().build();
    private TimeSystem<Double> tdbTs = DefaultTimeSystems.getTDB();
    private TimeSystem<UTCEpoch> utcTs = DefaultTimeSystems.getUTC();

	public SpiceStateHistoryIntervalGenerator(double cadence)
	{
		this.cadence = cadence;
	}

	public void setMetaKernelFile(String mkFilename)
	{
		Path mkPath = Paths.get(mkFilename);

		//get these from the config maybe?  The instrument may have to come from somewhere else....
		EphemerisID bodyId = new SimpleEphemerisID("BENNU");
		EphemerisID scId = new SimpleEphemerisID("ORX");

		FrameID bodyFrame = new SimpleFrameID("IAU_BENNU");
		FrameID scFrame = new SimpleFrameID("ORX");
		FrameID instrumentFrame = new SimpleFrameID("ORX_OCAMS_POLYCAM");

		int sclkIdCode = -64;

		try
		{
			System.out.println("SpiceStateHistoryIntervalGenerator: setMetaKernelFile: making pointing provider " + mkPath);
			pointingProvider = SpicePointingProvider.of(ImmutableList.copyOf(new Path[] {mkPath}), bodyId, bodyFrame,
					scId, scFrame, sclkIdCode, instrumentFrame);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public StateHistory createNewTimeInterval(StateHistory history, Function<Double, Void> progressFunction)
			throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		String startString = edu.jhuapl.sbmt.util.TimeUtil.et2str(history.getMinTime());
		String endString = edu.jhuapl.sbmt.util.TimeUtil.et2str(history.getMaxTime());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DateTime start = formatter.parseDateTime(startString.substring(0, 23));
		DateTime end = formatter.parseDateTime(endString.substring(0, 23));
		return createNewTimeInterval(history, history.getKey(), start, end,
										history.getTimeWindow()/(24.0 * 60.0 * 60.0 * 1000.0), history.getStateHistoryName(), progressFunction);
	}

	public StateHistory createNewTimeInterval(StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		return createNewTimeInterval(null, key, startTime, endTime, duration, name, progressFunction);
	}

	/**
	 * @param tempHistory
	 * @param key
	 * @param startTime
	 * @param endTime
	 * @param duration
	 * @param name
	 * @param progressFunction
	 * @return
	 * @throws StateHistoryInputException
	 * @throws StateHistoryInvalidTimeException
	 * @throws RuntimeException the pointingProvider.provide() call may throw a RuntimeException if certain SPICE issues can't be resolved
	 */
	public StateHistory createNewTimeInterval(StateHistory tempHistory, StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException
	{
		if (pointingProvider == null) return null;
		StateHistory history = tempHistory;
		// creates the trajectory
		Trajectory trajectory = new StandardTrajectory();
		if (tempHistory == null) history = new StandardStateHistory(key);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-D'T'HH:mm:ss.SSS");	//generates Year-DOY date format

		UTCEpoch startEpoch = UTCEpoch.fromString(dateFormatter.format(startTime.toDate()));
		UTCEpoch endEpoch = UTCEpoch.fromString(dateFormatter.format(endTime.toDate()));
		double timeWindowDuration = utcTs.difference(startEpoch, endEpoch);

		for (UTCEpoch time = startEpoch; time.compareTo(endEpoch) == -1; time = advanceUTCEpochByTime(time, cadence) )
		{
			//populate a flyby state object, and use it to populate the history and trajectory
			double tdbTime = getTDBTimeForUTCEpoch(time);

			Pointing pointing = pointingProvider.provide(tdbTs.getTSEpoch(tdbTime));

			State flybyState = new SpiceState(pointing, TimeUtil.str2et(time.toString()));
			// add to history
			history.addState(flybyState);
			trajectory.addPositionAtTime(flybyState.getSpacecraftPosition(), flybyState.getEphemerisTime());
			double completion = Math.abs(100 * ((double) (utcTs.difference(startEpoch, time))) / timeWindowDuration);
			if (progressFunction != null) progressFunction.apply(completion);
		}
		progressFunction.apply(100.0);
		history.setCurrentTime(history.getMinTime());
		history.setTrajectory(trajectory);
		return history;
	}

	private double getTDBTimeForUTCEpoch(UTCEpoch epoch)
	{
		return tdbTs.getTime(utcTs.getTSEpoch(epoch));
	}

	private UTCEpoch advanceUTCEpochByTime(UTCEpoch epoch, double delta)
	{
		UTCEpoch newEpoch = utcTs.add(epoch, delta);
		System.out.println("SpiceStateHistoryIntervalGenerator: advanceUTCEpochByTime: new epoch " + newEpoch);
		return newEpoch;
	}


}
