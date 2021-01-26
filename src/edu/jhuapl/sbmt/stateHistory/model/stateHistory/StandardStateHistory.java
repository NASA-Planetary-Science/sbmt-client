package edu.jhuapl.sbmt.stateHistory.model.stateHistory;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.pointing.IPointingProvider;
import edu.jhuapl.sbmt.stateHistory.model.StateHistorySourceType;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

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
	private boolean mapped = false, visible = false;

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
    private String stateHistoryName = "";

    /**
     *
     */
    private String stateHistoryDescription = "";

    /**
     *
     */
    private Color color;

    private StateHistorySourceType type;

    private String sourceFile;

    private IPointingProvider pointingProvider;

    private boolean isValid = false;


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
    		StandardStateHistory stateHistory = new StandardStateHistory(key, currentTime, startTime, endTime, name, description, ColorUtil.getColorFromRGBA(colorAsDouble), type, sourceFile);
    		return stateHistory;

    	}, StandardStateHistory.class, stateHistory -> {

    		float[] colorComponents = ColorUtil.getRGBColorComponents(stateHistory.getTrajectory().getColor());
    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(STATEHISTORY_KEY_KEY, stateHistory.getKey());
    		result.put(CURRENT_TIME_KEY, stateHistory.getCurrentTime());
    		result.put(START_TIME_KEY, stateHistory.getStartTime());
    		result.put(END_TIME_KEY, stateHistory.getEndTime());
    		result.put(STATE_HISTORY_NAME_KEY, stateHistory.getStateHistoryName());
    		result.put(STATE_HISTORY_DESCRIPTION_KEY, stateHistory.getStateHistoryDescription());
    		result.put(TYPE_KEY, stateHistory.getType().toString());
    		result.put(SOURCE_FILE, stateHistory.getSourceFile());
    		result.put(COLOR_KEY, new Double[] { (double)colorComponents[0], (double)colorComponents[1], (double)colorComponents[2], (double)stateHistory.getTrajectory().getColor().getAlpha()/255.0});
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
    public StandardStateHistory(StateHistoryKey key, Double currentTime, Double startTime, Double endTime, String name, String description, Color color, StateHistorySourceType type, String sourceFile)
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
        	throw new StateHistoryInvalidTimeException("Entered time (" + dt + ") is outside the range of the selected interval (" + getStartTime() + "-" + getEndTime() + ").");
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
	public void setTrajectoryColor(Color color)
	{
		this.color = color;
		this.trajectory.setColor(color);
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

	/**
	 * @return the currentTime
	 */
	public Double getCurrentTime()
	{
		return currentTime;
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
	 * @return the key
	 */
	public StateHistoryKey getKey()
	{
		return key;
	}

	/**
	 * @return the trajectory
	 */
	public Trajectory getTrajectory()
	{
		return trajectory;
	}

	/**
	 * @param trajectory the trajectory to set
	 */
	public void setTrajectory(Trajectory trajectory)
	{
		this.trajectory = trajectory;
	}

	/**
	 * @return the stateHistoryName
	 */
	public String getStateHistoryName()
	{
		return stateHistoryName;
	}

	/**
	 * @param stateHistoryName the stateHistoryName to set
	 */
	public void setStateHistoryName(String stateHistoryName)
	{
		this.stateHistoryName = stateHistoryName;
	}

	/**
	 * @return the stateHistoryDescription
	 */
	public String getStateHistoryDescription()
	{
		return stateHistoryDescription;
	}

	/**
	 * @param stateHistoryDescription the stateHistoryDescription to set
	 */
	public void setStateHistoryDescription(String stateHistoryDescription)
	{
		this.stateHistoryDescription = stateHistoryDescription;
	}

	/**
	 * @return the type
	 */
	public StateHistorySourceType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(StateHistorySourceType type)
	{
		this.type = type;
	}

	/**
	 * @return the sourceFile
	 */
	public String getSourceFile()
	{
		return sourceFile;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile)
	{
		this.sourceFile = sourceFile;
	}

	/**
	 * @return the pointingProvider
	 */
	public IPointingProvider getPointingProvider()
	{
		return pointingProvider;
	}

	/**
	 * @param pointingProvider the pointingProvider to set
	 */
	public void setPointingProvider(IPointingProvider pointingProvider)
	{
		this.pointingProvider = pointingProvider;
	}

	@Override
	public void reloadPointingProvider() throws StateHistoryIOException {
		// TODO Auto-generated method stub

	}

	public boolean isMapped() { return mapped; }

	public void setMapped(boolean mapped) { this.mapped = mapped; }

	public boolean isVisible() { return visible; }

	public void setVisible(boolean visible) { this.visible = visible; }

	public void saveStateToFile(String shapeModelName, String fileName) throws StateHistoryIOException
	{
		String fileNameWithExtension = fileName + ".csvstate";
		// writes the header for the new history
		try
        {
            FileWriter writer = new FileWriter(fileNameWithExtension);
//		            writer.append(config.getShapeModelName());
            writer.append(',');
            writer.append('\n');

            // Create header of name, description, color
            writer.append(getStateHistoryName() + ',');
            writer.append(getTrajectory().getTrajectoryDescription() + ',');
            for (double colorElement : getTrajectory().getColor().getColorComponents(null)) {
                writer.append(Double.toString(colorElement));
                writer.append(',');
            }
            writer.append(Double.toString(getTrajectory().getThickness()));
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
        Set<Double> keySet = getAllTimes();
        for (Double key : keySet) {
            State history = getStateAtTime(key);
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
		if (!extension.equals("csvstate") || !extension.equals("csv")) throw new StateHistoryIOException("Invalid file format");
		ArrayList<String[]> timeArray = new ArrayList<>(3);
        Integer firstIndex = null;
        String runDirName = file.getAbsolutePath();
        StandardTrajectory trajectory;

        try
        {
            String runName = file.getName();
            if (!runName.endsWith(".csv")) throw new StateHistoryIOException("File does not have a csv extension; please choose another file");

            BufferedReader in = new BufferedReader(new FileReader(file));
            in.close();


            StateHistory history = null;
            in = new BufferedReader(new FileReader(runDirName));

            if (firstIndex == null)
                firstIndex = 0;

         // create a new history instance and add it to the Map
            history = new StandardStateHistory(key);

            trajectory = new StandardTrajectory(history);
            // fill in the Trajectory parameters
            trajectory.setId(0);



            // discard first line of body name
            in.readLine();

            // get name, desc, color form second line
            String info = in.readLine();
            String[] data = info.split(",");
//            trajectory.setName(data[0]);
            trajectory.setTrajectoryDescription(data[1]);

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
                history.addState(flybyState);

                double[] spacecraftPosition = flybyState.getSpacecraftPosition();

                //TODO go back and look at this once pointing provider implementation in place - needed?
//                trajectory.getX().add(spacecraftPosition[0]);
//                trajectory.getY().add(spacecraftPosition[1]);
//                trajectory.getZ().add(spacecraftPosition[2]);

                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
                    timeArray.get(0)[0] = flybyState.getUtc();
                }
                timeArray.get(0)[1] = flybyState.getUtc();
            }
            in.close();

            history.setTrajectory(trajectory);

            history.setCurrentTime(history.getStartTime());
            trajectory.setStartTime(history.getStartTime());
            trajectory.setStopTime(history.getEndTime());
            trajectory.setNumPoints(Math.abs((int)(history.getEndTime() - history.getStartTime())/60));

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

		isValid = true;
	}

	public boolean isValid()
	{
		return isValid;
	}
}