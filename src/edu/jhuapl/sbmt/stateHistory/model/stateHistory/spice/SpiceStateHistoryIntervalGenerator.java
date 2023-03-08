package edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice;

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
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.SpiceState;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryMetadata;
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
	private SpicePointingProvider pointingProvider;
    protected static final TimeSystems DefaultTimeSystems = TimeSystems.builder().build();
    private TimeSystem<UTCEpoch> utcTs = DefaultTimeSystems.getUTC();
    private String sourceFile;
    private SpiceInfo spiceInfo;

	public SpiceStateHistoryIntervalGenerator()
	{
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
			for (String instrumentToBind : spice.getInstrumentNamesToBind())
			{
				builder.addInstrumentFrame(instrumentToBind);
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
		IStateHistoryMetadata metadata = history.getMetadata();
		String startString = edu.jhuapl.sbmt.util.TimeUtil.et2str(metadata.getStartTime());
		String endString = edu.jhuapl.sbmt.util.TimeUtil.et2str(metadata.getEndTime());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		DateTime start = formatter.parseDateTime(startString.substring(0, 23));
		DateTime end = formatter.parseDateTime(endString.substring(0, 23));
		return createNewTimeInterval(history, metadata.getKey(), start, end,
										metadata.getTimeWindow()/(24.0 * 60.0 * 60.0 * 1000.0), metadata.getStateHistoryName(), progressFunction);
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
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-D'T'HH:mm:ss.SSS");	//generates Year-DOY date format

		UTCEpoch startEpoch = UTCEpoch.fromString(dateFormatter.format(startTime.toDate()));
		UTCEpoch endEpoch = UTCEpoch.fromString(dateFormatter.format(endTime.toDate()));
		double timeWindowDuration = utcTs.difference(startEpoch, endEpoch);
		// creates the trajectory
		StateHistoryMetadata metadata = new StateHistoryMetadata(key, TimeUtil.str2et(startEpoch.toString()),
				TimeUtil.str2et(startEpoch.toString()), TimeUtil.str2et(endEpoch.toString()),
				name, "", StateHistorySourceType.SPICE);
		if (tempHistory == null) history = new SpiceStateHistory(metadata, sourceFile);
		Trajectory trajectory = new StandardTrajectory(history);
		IStateHistoryTrajectoryMetadata trajectoryMetadata = history.getTrajectoryMetadata();
		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();

		//add the pointing provider to the history and trajectory objects
		trajectory.setPointingProvider(pointingProvider);
		trajectory.setStartTime(TimeUtil.str2et(startEpoch.toString()));
		trajectory.setStopTime(TimeUtil.str2et(endEpoch.toString()));

		double timeDelta = (endTime.getMillis() - startTime.getMillis())/1000;
		double timeStep = 60;
//		if (timeDelta > 3*24*3600) timeStep = 3*3600;
		if (timeDelta > 2*24*3600) timeStep = 600;
		else if (timeDelta > 12*3600) timeStep = 120;
		else if (timeDelta < 300) timeStep = .01;

		trajectory.setNumPoints(Math.abs((int)(timeWindowDuration/timeStep)));

		State state = new SpiceState(pointingProvider);
		// add to history
		locationProvider.addState(state);

		if (progressFunction != null)  progressFunction.apply(100.0);
		trajectoryMetadata.setTrajectory(trajectory);
		locationProvider.setSourceFile(sourceFile);
		locationProvider.setPointingProvider(pointingProvider);
		((SpiceStateHistoryLocationProvider)(history.getLocationProvider())).setSpiceInfo(spiceInfo);
		return history;
	}
}
