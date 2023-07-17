package edu.jhuapl.sbmt.stateHistory.model.stateHistory.standard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.pointing.pregen.PregenPointingProvider;
import edu.jhuapl.sbmt.pointing.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryLocationProvider;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.AbstractStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryTrajectoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

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
public class StandardStateHistory extends AbstractStateHistory
{

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
    		if ((name == null) || (name.equals("")))
				name = "Segment_" + key.getValue();
    		Double[] colorAsDouble = source.get(COLOR_KEY);
    		StateHistoryMetadata metadata = new StateHistoryMetadata(key, currentTime, startTime, endTime, name, description, type);
    		StandardStateHistory stateHistory = new StandardStateHistory(metadata, sourceFile);
    		stateHistory.getTrajectoryMetadata().setTrajectory(new StandardTrajectory(stateHistory));
    		stateHistory.getTrajectoryMetadata().setTrajectoryColor(ColorUtil.getColorFromRGBA(colorAsDouble));
    		return stateHistory;

    	}, StandardStateHistory.class, stateHistory -> {

    		IStateHistoryMetadata metadata = stateHistory.getMetadata();
    		IStateHistoryLocationProvider locationProvider = stateHistory.getLocationProvider();
    		IStateHistoryTrajectoryMetadata trajectoryMetadata = stateHistory.getTrajectoryMetadata();
    		float[] colorComponents = ColorUtil.getRGBColorComponents(trajectoryMetadata.getTrajectory().getColor());
    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(STATEHISTORY_KEY_KEY, metadata.getKey());
    		result.put(CURRENT_TIME_KEY, metadata.getCurrentTime());
    		result.put(START_TIME_KEY, metadata.getStartTime());
    		result.put(END_TIME_KEY, metadata.getEndTime());
    		result.put(STATE_HISTORY_NAME_KEY, metadata.getStateHistoryName());
    		result.put(STATE_HISTORY_DESCRIPTION_KEY, metadata.getStateHistoryDescription());
    		if (metadata.getType() != null)
    			result.put(TYPE_KEY, metadata.getType().toString());
    		else
    			result.put(TYPE_KEY, StateHistorySourceType.PREGEN.toString());
    		result.put(SOURCE_FILE, locationProvider.getSourceFile());
    		result.put(COLOR_KEY, new Double[] { (double)colorComponents[0], (double)colorComponents[1], (double)colorComponents[2], (double)trajectoryMetadata.getTrajectory().getColor().getAlpha()/255.0});
    		return result;
    	});
	}

    public StandardStateHistory(StateHistoryMetadata metadata)
    {
    	this.metadata = new StateHistoryMetadata(metadata) {
			@Override
			public void setCurrentTime(Double time) throws StateHistoryInvalidTimeException
			{
				if( time < getStartTime() || time > getEndTime())
		        {
		        	throw new StateHistoryInvalidTimeException("Entered time (" + time + ") is outside the range of the selected interval (" + getStartTime() + "-" + getEndTime() + ").");
		        }
				currentTime = time;
			}

			/**
		     *
		     */
			@Override
		    public Double getStartTime()
		    {
		    	if (startTime != null) return startTime;
		        return ((StandardStateHistoryLocationProvider)locationProvider).getTimeToStateMap().firstKey();
		    }

			@Override
		    public Double getEndTime()
		    {
		    	if (endTime != null) return endTime;
		        return ((StandardStateHistoryLocationProvider)locationProvider).getTimeToStateMap().lastKey();
		    }
		};
    	this.locationProvider = new StandardStateHistoryLocationProvider(this);
    	this.trajectoryMetadata = new StateHistoryTrajectoryMetadata();

    }

    public StandardStateHistory(StateHistoryMetadata metadata, String sourceFile)
    {
    	this(metadata);
    	locationProvider.setSourceFile(sourceFile);
    }

    public void buildHistory(List<State> stateSegments, IPointingProvider pointingProvider, double startTime, double stopTime)
    {
    	Trajectory trajectory = new StandardTrajectory(this);
		IStateHistoryLocationProvider locationProvider = getLocationProvider();
		IStateHistoryTrajectoryMetadata trajectoryMetadata = getTrajectoryMetadata();

		trajectory.setPointingProvider(pointingProvider);
		trajectory.setStartTime(startTime);
		trajectory.setStopTime(stopTime);
		trajectory.setNumPoints(1000);

		for (State state : stateSegments)
		{
			locationProvider.addState(state);
		}

		try
		{
			metadata.setCurrentTime(startTime);
		}
		catch (StateHistoryInvalidTimeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trajectoryMetadata.setTrajectory(trajectory);
		metadata.setType(StateHistorySourceType.PREGEN);
		locationProvider.setPointingProvider(pointingProvider);

    }

	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		String fileNameWithExtension = fileName;
		// writes the header for the new history
		try
        {
            FileWriter writer = new FileWriter(fileNameWithExtension);
            writer.append(',');
            writer.append('\n');

            // Create header of name, description, color
            writer.append(getMetadata().getStateHistoryName() + ',');
            writer.append(getMetadata().getStateHistoryDescription() + ',');
            for (double colorElement : trajectoryMetadata.getTrajectory().getColor().getColorComponents(null)) {
                writer.append(Double.toString(colorElement));
                writer.append(',');
            }
            writer.append(Double.toString(trajectoryMetadata.getTrajectory().getThickness()));
            writer.append('\n');

            // header of column names for each entry
            writer.append("#UTC");
            writer.append(',');
            writer.append(" Sun x");
            writer.append(',');
            writer.append(" Sun y");
            writer.append(',');
            writer.append(" Sun z");
            writer.append(',');
            writer.append(" Earth x");
            writer.append(',');
            writer.append(" Earth y");
            writer.append(',');
            writer.append(" Earth z");
            writer.append(',');
            writer.append(" SC x");
            writer.append(',');
            writer.append(" SC y");
            writer.append(',');
            writer.append(" SC z");
            writer.append(',');
            writer.append(" SCV x");
            writer.append(',');
            writer.append(" SCV y");
            writer.append(',');
            writer.append(" SCV z");
            writer.append('\n');
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            throw new StateHistoryIOException("A problem occurred when saving the state history; please see the console for a stack trace", e);
        }

        // get each flyby state in currentFlybyStateHistory, and write to CSV
        Set<Double> keySet = locationProvider.getAllTimes();
        for (Double key : keySet) {
            State history = locationProvider.getStateAtTime(key);
            history.writeToCSV(fileNameWithExtension);
        }
	}

	/**
	 * Loads state history interval from a file.  Format is the full expanded "Mark 1"
	 * format, used before SPICE integration.
	 * @param runFile
	 * @param shapeModelName
	 * @param key
	 * @return
	 * @throws StateHistoryIOException
	 */
	public StateHistory loadStateHistoryFromFile(File file, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException
    {
		String extension = FilenameUtils.getExtension(file.getAbsolutePath());
		if (!(extension.equals("csvstate") || extension.equals("csv"))) throw new StateHistoryIOException("Invalid file format");
		ArrayList<String[]> timeArray = new ArrayList<>(3);
        Integer firstIndex = null;
        String runDirName = file.getAbsolutePath();
        Trajectory trajectory;
        try
        {
            String runName = file.getName();
//            if (!runName.endsWith(".csv")) throw new StateHistoryIOException("File does not have a csv extension; please choose another file");

            StandardStateHistory history = null;
            BufferedReader in = new BufferedReader(new FileReader(runDirName));

            if (firstIndex == null)
                firstIndex = 0;

         // create a new history instance and add it to the Map
            StateHistoryMetadata metadata = new StateHistoryMetadata(key);
            history = new StandardStateHistory(metadata, file.getAbsolutePath());
            history.getMetadata().setStateHistoryName(runName);
            List<State> segments = new ArrayList<State>();

            in.readLine();

            // get name, desc, color form second line
            String info = in.readLine();
            String[] data = info.split(",");


            // discard third line of headers
            in.readLine();

            String line;
            String[] timeSet = new String[2];
            timeArray.add(timeSet);

            while ((line = in.readLine()) != null)
            {
                // parse line of file
                State flybyState = new CsvState(line);

                // add to history
                segments.add(flybyState);

                if(com.mysql.cj.util.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
                    timeArray.get(0)[0] = flybyState.getUtc();
                }
                timeArray.get(0)[1] = flybyState.getUtc();
            }
            in.close();
            IPointingProvider pointingProvider = PregenPointingProvider.builder(file.getAbsolutePath(), TimeUtil.str2et(timeArray.get(0)[0]), TimeUtil.str2et(timeArray.get(0)[1])).build();
            history.buildHistory(segments, pointingProvider, TimeUtil.str2et(timeArray.get(0)[0]), TimeUtil.str2et(timeArray.get(0)[1]));
            trajectory = history.getTrajectoryMetadata().getTrajectory();
            trajectory.setTrajectoryDescription(data[1]);
            history.getMetadata().setStateHistoryName(data[0]);
            history.getMetadata().setStateHistoryDescription(data[1]);
//            history = new StandardStateHistory(metadata, file.getAbsolutePath());
//            Trajectory trajectory = new StandardTrajectory(history);
//    		IStateHistoryLocationProvider locationProvider = history.getLocationProvider();
//    		IStateHistoryTrajectoryMetadata trajectoryMetadata = history.getTrajectoryMetadata();
//
//    		trajectory.setPointingProvider(locationProvider.getPointingProvider());
////    		trajectory.setStartTime(TimeUtil.str2et(startString));
////    		trajectory.setStopTime(TimeUtil.str2et(endString));
//    		trajectory.setNumPoints(1000);
//
//            // discard first line of body name
//            in.readLine();
//
//            // get name, desc, color form second line
//            String info = in.readLine();
//            String[] data = info.split(",");
//            trajectory.setTrajectoryDescription(data[1]);
//
//            // discard third line of headers
//            in.readLine();
//
//            String line;
//            String[] timeSet = new String[2];
//            timeArray.add(timeSet);
//            while ((line = in.readLine()) != null)
//            {
//                // parse line of file
//                State flybyState = new CsvState(line);
//
//                // add to history
//                history.getLocationProvider().addState(flybyState);
//
////                double[] spacecraftPosition = flybyState.getSpacecraftPosition();
//
//                //TODO go back and look at this once pointing provider implementation in place - needed?
////                trajectory.getX().add(spacecraftPosition[0]);
////                trajectory.getY().add(spacecraftPosition[1]);
////                trajectory.getZ().add(spacecraftPosition[2]);
//
//                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
//                    timeArray.get(0)[0] = flybyState.getUtc();
//                }
//                timeArray.get(0)[1] = flybyState.getUtc();
//            }
//            in.close();
//
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: traj is " + trajectory + " metadata start " + metadata.getStartTime());
//            trajectory.setStartTime(metadata.getStartTime());
//    		trajectory.setStopTime(metadata.getEndTime());
//    		metadata.setCurrentTime(metadata.getStartTime());
//    		trajectoryMetadata.setTrajectory(trajectory);
//    		metadata.setType(StateHistorySourceType.PREGEN);

    		//OLD WAY
//    		locationProvider.setSourceFile(sourceFile);
//    		locationProvider.setPointingProvider(pointingProvider);
//            history = new StandardStateHistory(metadata, file.getAbsolutePath());
//            history.getMetadata().setStateHistoryName(runName);
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: metadata " + metadata);
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: location prov " + history.getLocationProvider());
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: traj metadata " + history.getTrajectoryMetadata());
//
//
//            trajectory = new StandardTrajectory(history);
//            trajectory.setPointingProvider(locationProvider.getPointingProvider());
//            // fill in the Trajectory parameters
//            trajectory.setId(0);
//
//            // discard first line of body name
//            in.readLine();
//
//            // get name, desc, color form second line
//            String info = in.readLine();
//            String[] data = info.split(",");
//            trajectory.setTrajectoryDescription(data[1]);
//
//            // discard third line of headers
//            in.readLine();
//
//            String line;
//            String[] timeSet = new String[2];
//            timeArray.add(timeSet);
//
//            while ((line = in.readLine()) != null)
//            {
//                // parse line of file
//                State flybyState = new CsvState(line);
//
//                // add to history
//                history.getLocationProvider().addState(flybyState);
//
////                double[] spacecraftPosition = flybyState.getSpacecraftPosition();
//
//                //TODO go back and look at this once pointing provider implementation in place - needed?
////                trajectory.getX().add(spacecraftPosition[0]);
////                trajectory.getY().add(spacecraftPosition[1]);
////                trajectory.getZ().add(spacecraftPosition[2]);
//
//                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
//                    timeArray.get(0)[0] = flybyState.getUtc();
//                }
//                timeArray.get(0)[1] = flybyState.getUtc();
//            }
//            in.close();
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: metadata start " + history.getMetadata().getStartTime());
//
//            history.getTrajectoryMetadata().setTrajectory(trajectory);
//            history.getMetadata().setType(StateHistorySourceType.PREGEN);
//            System.out.println("StandardStateHistory: loadStateHistoryFromFile: metadata start " + history.getMetadata().getStartTime());
//            history.getMetadata().setCurrentTime(history.getMetadata().getStartTime());
//            trajectory.setStartTime(history.getMetadata().getStartTime());
//            trajectory.setStopTime(history.getMetadata().getEndTime());
//            trajectory.setNumPoints(Math.abs((int)(history.getMetadata().getEndTime() - history.getMetadata().getStartTime())/60));

            return history;
        }
        catch (Exception e1)
        {
        	e1.printStackTrace();
            throw new StateHistoryIOException("There was a problem reading the state history file.  See the console for details, and please make sure it is the right format", e1);
        }
    }

	public void validate()
	{
		isValid = locationProvider.validate();
	}
}