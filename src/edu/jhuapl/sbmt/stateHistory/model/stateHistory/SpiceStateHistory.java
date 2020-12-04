package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.SpiceState;

import crucible.core.math.vectorspace.UnwritableVectorIJK;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class SpiceStateHistory implements StateHistory
{

	boolean visible = false, mapped = false;

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
	private Color color;

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
			if ((name == null) || (name.equals("")))
				name = "Segment_" + key.getValue();
			Double[] colorAsDouble = source.get(COLOR_KEY);

			Color color = new Color(colorAsDouble[0].intValue(), colorAsDouble[1].intValue(), colorAsDouble[2].intValue(), colorAsDouble[3].intValue());

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
			result.put(START_TIME_KEY, stateHistory.getStartTime());
			result.put(END_TIME_KEY, stateHistory.getEndTime());
			result.put(STATE_HISTORY_NAME_KEY, stateHistory.getStateHistoryName());
			result.put(STATE_HISTORY_DESCRIPTION_KEY, stateHistory.getStateHistoryDescription());
			result.put(TYPE_KEY, stateHistory.getType().toString());
			result.put(SOURCE_FILE, stateHistory.getSourceFile());
			result.put(COLOR_KEY, new Double[]
			{ (float)stateHistory.getTrajectory().getColor().getRed()/255.0,
				(float)stateHistory.getTrajectory().getColor().getGreen()/255.0,
				(float)stateHistory.getTrajectory().getColor().getBlue()/255.0,
				(float)stateHistory.getTrajectory().getColor().getAlpha()/255.0 });
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
			String description, Color color, StateHistorySourceType type, String sourceFile)
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
		return getEndTime() - getStartTime();
	}

	@Override
	public Double getCurrentTime()
	{
		return currentTime;
	}

	@Override
	public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
	{
		if( time < getStartTime() || time > getEndTime())
        {
        	throw new StateHistoryInvalidTimeException("Entered time is outside the range of the selected interval.");
        }
		this.currentTime = time;
		state.setEphemerisTime(time);
	}

	@Override
	public Double getStartTime()
	{
		return startTime;
	}

	@Override
	public Double getEndTime()
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

	public boolean isMapped() { return mapped; }

	public void setMapped(boolean mapped) { this.mapped = mapped; }

	public boolean isVisible() { return visible; }

	public void setVisible(boolean visible) { this.visible = visible; }

	@Override
	public void setTrajectoryColor(Color color)
	{
		this.color = color;
		this.trajectory.setColor(color);
	}


	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		Metadata metadata = InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(SpiceStateHistory.class).provide(this);
		try
		{
			System.out.println("SpiceStateHistory: saveStateToFile: filename is " + fileName);
			Serializers.serialize("SpiceStateHistory", metadata, new File(fileName + ".spicestate"));
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
}
