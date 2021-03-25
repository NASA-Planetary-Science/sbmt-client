package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.SpiceState;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;
import edu.jhuapl.sbmt.util.TimeUtil;

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
	private SpicePointingProvider pointingProvider;
    protected static final TimeSystems DefaultTimeSystems = TimeSystems.builder().build();
    private TimeSystem<Double> tdbTs = DefaultTimeSystems.getTDB();
    private TimeSystem<UTCEpoch> utcTs = DefaultTimeSystems.getUTC();
    private String sourceFile;
    private SpiceInfo spiceInfo;

	public SpiceStateHistoryIntervalGenerator(double cadence)
	{
		this.cadence = cadence;
	}

	public void setSourceFile(String sourceFile, SpiceInfo spiceInfo)
	{
		this.sourceFile = sourceFile;
		setMetaKernelFile(sourceFile, spiceInfo);
	}

	public void setMetaKernelFile(String mkFilename, SpiceInfo spice)
	{
		Path mkPath = Paths.get(mkFilename);
		this.spiceInfo = spice;
		this.sourceFile = mkFilename;
		try
		{
			SpicePointingProvider.Builder builder =
					SpicePointingProvider.builder(ImmutableList.copyOf(new Path[] {mkPath}), spice.getBodyName(),
							spice.getBodyFrameName(), spice.getScId(), spice.getScFrameName());

			for (String bodyNameToBind : spice.getBodyNamesToBind()) builder.bindEphemeris(bodyNameToBind);
			for (String instrumentFrameToBind : spice.getInstrumentFrameNamesToBind())
			{
				builder.bindFrame(instrumentFrameToBind);
			}

            pointingProvider = builder.build();
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
		String startString = edu.jhuapl.sbmt.util.TimeUtil.et2str(history.getStartTime());
		String endString = edu.jhuapl.sbmt.util.TimeUtil.et2str(history.getEndTime());
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
		if (tempHistory == null) history = new SpiceStateHistory(key);
		Trajectory trajectory = new StandardTrajectory(history);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-D'T'HH:mm:ss.SSS");	//generates Year-DOY date format

		UTCEpoch startEpoch = UTCEpoch.fromString(dateFormatter.format(startTime.toDate()));
		UTCEpoch endEpoch = UTCEpoch.fromString(dateFormatter.format(endTime.toDate()));
		double timeWindowDuration = utcTs.difference(startEpoch, endEpoch);

		//add the pointing provider to the history and trajectory objects
		trajectory.setPointingProvider(pointingProvider);
		trajectory.setStartTime(TimeUtil.str2et(startEpoch.toString()));
		trajectory.setStopTime(TimeUtil.str2et(endEpoch.toString()));
		trajectory.setNumPoints(Math.abs((int)timeWindowDuration/60));

		State state = new SpiceState(pointingProvider);
		// add to history
		history.addState(state);
		history.setTrajectory(trajectory);
		if (progressFunction != null)  progressFunction.apply(100.0);
		history.setStartTime(TimeUtil.str2et(startEpoch.toString()));
		history.setEndTime(TimeUtil.str2et(endEpoch.toString()));
		history.setCurrentTime(TimeUtil.str2et(startEpoch.toString()));

		history.setType(StateHistorySourceType.SPICE);
		history.setSourceFile(sourceFile);
		history.setPointingProvider(pointingProvider);
		((SpiceStateHistory)history).setSpiceInfo(spiceInfo);
		return history;
	}
}
