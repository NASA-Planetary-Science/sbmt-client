package edu.jhuapl.sbmt.stateHistory.model.stateHistory.spice;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.pointing.scState.SpiceState;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.AbstractStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class SpiceStateHistory extends AbstractStateHistory
{
	private HashMap<String, String> plateColoringForInstrument = new HashMap<String, String>();

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
			if ((name == null) || (name.equals("")))
				name = "Segment_" + key.getValue();
			Double[] colorAsDouble = source.get(COLOR_KEY);

			Color color = new Color(colorAsDouble[0].intValue(), colorAsDouble[1].intValue(), colorAsDouble[2].intValue(), colorAsDouble[3].intValue());


			SpiceInfo spiceInfo = null;
			if (source.hasKey(SPICE_INFO_KEY))
				spiceInfo = source.get(SPICE_INFO_KEY);

			StateHistoryMetadata metadata = new StateHistoryMetadata(key, currentTime, startTime, endTime, name,
					description, type);
			SpiceStateHistory stateHistory = new SpiceStateHistory(metadata, sourceFile);
			stateHistory.getTrajectoryMetadata().setTrajectory(new StandardTrajectory(stateHistory));
			stateHistory.getTrajectoryMetadata().setTrajectoryColor(color);
			((SpiceStateHistoryLocationProvider)stateHistory.getLocationProvider()).setSpiceInfo(spiceInfo);

			try
			{
				stateHistory.getMetadata().setCurrentTime(startTime);
			}
			catch (StateHistoryInvalidTimeException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return stateHistory;

		}, SpiceStateHistory.class, stateHistory ->
		{

    		IStateHistoryMetadata metadata = stateHistory.getMetadata();
    		IStateHistoryLocationProvider locationProvider = stateHistory.getLocationProvider();
    		IStateHistoryTrajectoryMetadata trajectoryMetadata = stateHistory.getTrajectoryMetadata();
			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
			result.put(STATEHISTORY_KEY_KEY, metadata.getKey());
			result.put(CURRENT_TIME_KEY, metadata.getCurrentTime());
			result.put(START_TIME_KEY, metadata.getStartTime());
			result.put(END_TIME_KEY, metadata.getEndTime());
			result.put(STATE_HISTORY_NAME_KEY, metadata.getStateHistoryName());
			result.put(STATE_HISTORY_DESCRIPTION_KEY, metadata.getStateHistoryDescription());
			result.put(TYPE_KEY, metadata.getType().toString());
			result.put(SOURCE_FILE, locationProvider.getSourceFile());
			if (trajectoryMetadata.getTrajectory() != null)
			{
				result.put(COLOR_KEY, new Double[]
				{ (float)trajectoryMetadata.getTrajectory().getColor().getRed()/255.0,
					(float)trajectoryMetadata.getTrajectory().getColor().getGreen()/255.0,
					(float)trajectoryMetadata.getTrajectory().getColor().getBlue()/255.0,
					(float)trajectoryMetadata.getTrajectory().getColor().getAlpha()/255.0 });
			}
			else
			{
				result.put(COLOR_KEY, new Double[]
						{ 0.0, 1.0, 1.0, 1.0 });
			}
			result.put(SPICE_INFO_KEY, ((SpiceStateHistoryLocationProvider)stateHistory.getLocationProvider()).getSpiceInfo());
			return result;
		});
	}

	public SpiceStateHistory(StateHistoryMetadata metadata)
	{
		this.locationProvider = new SpiceStateHistoryLocationProvider(this);
		this.metadata = new StateHistoryMetadata(metadata) {
			@Override
			public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
			{
				if( time < getStartTime() || time > getEndTime())
		        {
		        	throw new StateHistoryInvalidTimeException("Entered time is outside the range of the selected interval.");
		        }
				currentTime = time;
				SpiceState state = ((SpiceState)locationProvider.getCurrentState());
				if (state != null)
					state.setEphemerisTime(time);
			}
		};
		this.trajectoryMetadata = new StateHistoryTrajectoryMetadata();
	}

	public SpiceStateHistory(StateHistoryMetadata metadata, String sourceFile)
	{
		this(metadata);
		this.locationProvider.setSourceFile(sourceFile);
	}

	public void validate()
	{
		isValid = locationProvider.validate();
	}

	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		Metadata metadata = InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(SpiceStateHistory.class).provide(this);
		try
		{
			Serializers.serialize("SpiceStateHistory", metadata, new File(fileName));
		}
		catch (IOException e)
		{
			throw new StateHistoryIOException("Error saving state to file " + fileName, e);
		}
	}

	public StateHistory loadStateHistoryFromFile(File file, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException
    {
		String extension = FilenameUtils.getExtension(file.getAbsolutePath());
		if (!extension.equals("spicestate")) throw new StateHistoryIOException("Invalid file format");
		FixedMetadata metadata;
		try
		{
			metadata = Serializers.deserialize(file, "SpiceStateHistory");
		}
		catch (IOException e)
		{
			throw new StateHistoryIOException("Problem loading state history from file " + file, e);
		}
		SpiceStateHistory stateHistory = InstanceGetter.defaultInstanceGetter().providesGenericObjectFromMetadata(SPICE_STATE_HISTORY_KEY).provide(metadata);
		return stateHistory;

    }

	public String getPlateColoringForInstrument(String fov)
	{
		return plateColoringForInstrument.get(fov);
	}

	public void setPlateColoringForInstrument(String plateColoring, String fov)
	{
		plateColoringForInstrument.put(fov, plateColoring);
	}
}