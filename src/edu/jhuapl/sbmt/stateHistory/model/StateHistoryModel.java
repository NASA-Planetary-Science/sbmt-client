package edu.jhuapl.sbmt.stateHistory.model;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.controllers.StateHistoryController.RunInfo;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.Trajectory;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;


public class StateHistoryModel //implements HasTime // extends AbstractModel implements PropertyChangeListener, /*TableModel,*/ //HasTime//, ActionListener
{
	List<StateHistoryModelChangedListener> listeners = new ArrayList<StateHistoryModelChangedListener>();
    private ModelManager modelManager;

    private Double time;

    public static final String RUN_NAMES = "RunNames"; // What name to give this image for display
    public static final String RUN_FILENAMES = "RunFilenames"; // Filename of image on disk


    private boolean initialized =false;
    private String description = "desc";
    private File path = null;
    final int lineLength = 121;

    private double timeStep;


    private ArrayList<String[]> timeArray = new ArrayList<>(3);

    private boolean visible; // able to be shown
    private boolean showing = false; // currently showing
    public static final double offsetHeight = 2.0;
    private double offset = offsetHeight;

//    protected final StateHistoryKey key;


    private StateHistory currentFlybyStateHistory;
    private DateTime startTime;
    private DateTime endTime;

    private SmallBodyModel smallBodyModel;
    private double scalingFactor = 0.0;


    private StateHistoryCollection runs;

    // variables related to the scalar bar
//    private int coloringIndex = 1;

    private int defaultSliderValue = 0;
    private int sliderFinalValue = 900;

    private Double timeBarValue;
//    private Double currentTime;

    private String statusBarString;

    static public StateHistoryModel createStateHistory(DateTime start, DateTime end, SmallBodyModel smallBodyModel, Renderer renderer, ModelManager modelManager)
    {
        return new StateHistoryModel(start, end, smallBodyModel, renderer, modelManager);
    }

    public StateHistoryModel(DateTime start, DateTime end, SmallBodyModel smallBodyModel, Renderer renderer, ModelManager modelManager)
    {
        this.smallBodyModel = smallBodyModel;
        this.startTime = start;
        this.endTime = end;
        this.modelManager = modelManager;
        this.runs = (StateHistoryCollection)modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
        initialize();

    }

    public StateHistoryModel(SmallBodyModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;
    }

    private List<String> passFileNames = new ArrayList<String>();

    private void initialize()
    {
        BoundingBox bb = smallBodyModel.getBoundingBox();
        double width = Math.max((bb.xmax-bb.xmin), Math.max((bb.ymax-bb.ymin), (bb.zmax-bb.zmin)));
        scalingFactor = 30.62*width + -0.0002237;
    }

    public void addStateHistoryModelChangedListener(StateHistoryModelChangedListener listener)
    {
    	listeners.add(listener);
    }


    /***
     *
     * Interval Generation
     *
     ***/

    private void fireHistorySegmentCreatedListener(StateHistory historySegment)
	{
		for (StateHistoryModelChangedListener listener : listeners)
		{
			listener.historySegmentCreated(historySegment);
		}
	}

    /****
     * Interval Selection
     ****/
    // gets the start and end times of a trajectory - Alex W
    public String[] getIntervalTime()
    {
        return timeArray.get(0);
    }

    public DateTime getStartTime()
    {
        return startTime;
    }

    public DateTime getEndTime()
    {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String desc)
    {
        description = desc;
    }

    private void fireTrajectoryColorChangedListener(double[] color)
	{
		for (StateHistoryModelChangedListener listener : listeners)
		{
			listener.trajectoryColorChanged(color);
		}
	}

	private void fireTrajectoryThicknessChangedListener(double thickness)
	{
		for (StateHistoryModelChangedListener listener : listeners)
		{
			listener.trajectoryThicknessChanged(thickness);
		}
	}


//    /****
//     *
//     * Interval Playback
//     *
//     ****/
//    public void setTimeFraction(Double timeFraction)
//	{
//
//	}

    public Double getTime()
    {
        return time;
    }

    public void setTime(Double time)
    {
        this.time = time;
        if (currentFlybyStateHistory != null)
            currentFlybyStateHistory.setTime(time);
    }

    public void setStartTime(DateTime startTime)
	{
		this.startTime = startTime;
	}

	public void setEndTime(DateTime endTime)
	{
		this.endTime = endTime;
	}

	public Double getTimeFraction()
    {
        if (currentFlybyStateHistory != null)
            return currentFlybyStateHistory.getTimeFraction();
        else
            return null;
    }

    public Double getPeriod()
    {
        if (currentFlybyStateHistory != null)
            return currentFlybyStateHistory.getPeriod();
        else
            return 0.0;
    }

    // set time of animation - Alex W
    public boolean setInputTime(DateTime dt)
    {
        try
        {
            String dtString = dt.toString().substring(0, 23);
            String start = timeArray.get(0)[0];
            String end = timeArray.get(0)[1];

            if(dtString.compareTo(start) < 0 || dtString.compareTo(end) > 0)
            {
                JOptionPane.showMessageDialog(null, "Entered time is outside the range of the selected interval.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Interval interval1 = new Interval(startTime, dt);
            Interval interval2 = new Interval(startTime, endTime);

            org.joda.time.Duration duration1 = interval1.toDuration();
            org.joda.time.Duration duration2 = interval2.toDuration();

            BigDecimal num1 = new BigDecimal(duration1.getMillis());
            BigDecimal num2 = new BigDecimal(duration2.getMillis());
            BigDecimal tf = num1.divide(num2,50,RoundingMode.UP);
//            setTimeFraction(Double.parseDouble(tf.toString()));
            fireTimeChangedListener(Double.parseDouble(tf.toString()));
//            panel.setTimeSlider(Double.parseDouble(tf.toString()));
        }
        catch (Exception e)
        {

        }
        return true;
    }

    private void fireTimeChangedListener(Double t)
	{
		for (StateHistoryModelChangedListener listener : listeners)
		{
			listener.timeChanged(t);
		}
	}

    public boolean getInitialized(){
        return initialized;
    }

    public String getTrajectoryName()
    {
    	return currentFlybyStateHistory.getTrajectoryName();
    }

    public String defaultTrajectoryName() {
        return startTime.toString() + "_" + endTime.toString();
    }

    // returns sun position - Alex W
    public double[] getSunPosition()
    {
        return currentFlybyStateHistory.getSunPosition();
    }

    public void setDistanceText(String distanceText)
    {
    	fireDistanceTextChangedListener(distanceText);
    }

    public void setTimeBarValue(Double timeBarValue)
    {
    	this.timeBarValue = timeBarValue;
    }

	private void fireDistanceTextChangedListener(String distanceText)
	{
		for (StateHistoryModelChangedListener listener : listeners)
		{
			listener.distanceTextChanged(distanceText);
		}
	}

    /***
     * Interval Creation
     ***/


    /***
     *  goes to the server and creates a new time history for the body with the given time range.
     *
     * @param panel - main panel
     * @param length - length of interval
     * @param name - name of interval
     * @return -1 if error thrown on creation, 1 if successfully created
     */
    // returns
    public int createNewTimeInterval(StateHistoryKey key, double length, String name)
    {

        // gets the history file from the server
        SmallBodyViewConfig config = (SmallBodyViewConfig) smallBodyModel.getConfig();
        path = FileCache.getFileFromServer(config.timeHistoryFile);

        // removes the time zone from the time
        String startString = startTime.toString().substring(0,23);
        String endString = endTime.toString().substring(0,23);

        // searches the file for the specified times
        String queryStart = StateHistoryUtil.readString(lineLength, path);
        String queryEnd = StateHistoryUtil.readString((int)StateHistoryUtil.getBinaryFileLength(path, lineLength)*lineLength-lineLength, path);

        // error checking
        if(startTime.compareTo(endTime) > 0)
        {
            JOptionPane.showMessageDialog(null, "The entered times are not in the correct order.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        if(startString.compareTo(queryStart) < 0 || endString.compareTo(queryEnd) > 0)
        {
            JOptionPane.showMessageDialog(null, "One or more of the query times are out of range of the available data.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        // get start and stop positions in file
        int positionStart = StateHistoryUtil.binarySearch(1, (int) StateHistoryUtil.getBinaryFileLength(path, lineLength), startString, false, lineLength, path);
        int positionEnd = StateHistoryUtil.binarySearch(1, (int) StateHistoryUtil.getBinaryFileLength(path, lineLength), endString, true, lineLength, path);

        // check length of time
        if(StateHistoryUtil.readString(positionStart, path).compareTo(StateHistoryUtil.readString(positionEnd, path)) == 0)
        {
            JOptionPane.showMessageDialog(null, "The queried time interval is too small.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        //sets the default name to "startTime_endTime"
        if(name.equals(""))
        {
            name = StateHistoryUtil.readString(positionStart, path) + "_" + StateHistoryUtil.readString(positionEnd, path);
        }

        // check length of interval
        if(length > 10.0)
        {
            int result = JOptionPane.showConfirmDialog(null, "The interval you selected is longer than 10 days and may take a while to generate. \nAre you sure you want to create it?");
            if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
                return -1;
        }

        // creates the trajectory
        Trajectory temp = new StandardTrajectory();
        StateHistory history = new StandardStateHistory(key);

        this.currentFlybyStateHistory = history;

        //  reads the binary file and writes the data to a CSV file
        String[] timeSet = new String[2];
        timeArray.add(timeSet);
        for(int i = positionStart; i <= positionEnd; i+=lineLength)
        {
            int[] position = new int[12];
            for(int j = 0; j<position.length; j++)
            {
                position[j] = i + 25 + (j * 8);
            }
            State flybyState = new CsvState(StateHistoryUtil.readString(i, path),
            		StateHistoryUtil.readBinary(position[0], path), StateHistoryUtil.readBinary(position[1], path), StateHistoryUtil.readBinary(position[2], path),
            		StateHistoryUtil.readBinary(position[3], path), StateHistoryUtil.readBinary(position[4], path), StateHistoryUtil.readBinary(position[5], path),
            		StateHistoryUtil.readBinary(position[6], path), StateHistoryUtil.readBinary(position[7], path), StateHistoryUtil.readBinary(position[8], path),
            		StateHistoryUtil.readBinary(position[9], path), StateHistoryUtil.readBinary(position[10], path), StateHistoryUtil.readBinary(position[11], path));

            // add to history
            history.put(flybyState);

            double[] spacecraftPosition = flybyState.getSpacecraftPosition();

            temp.getX().add(spacecraftPosition[0]);
            temp.getY().add(spacecraftPosition[1]);
            temp.getZ().add(spacecraftPosition[2]);

            if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0]))
            {
                timeArray.get(0)[0] = flybyState.getUtc();
            }
            timeArray.get(0)[1] = flybyState.getUtc();

        }

        history.setTime(history.getMinTime());
        history.setTrajectory(temp);
        runs.addRunToList(history);
        fireHistorySegmentCreatedListener(history);

        return 1;
    }

	public StateHistoryCollection getRuns()
	{
		return runs;
	}

	 public void saveRowToFile(StateHistory history, File file)
     {
         StateHistoryModelIOHelper.saveIntervalToFile(smallBodyModel.getConfig().getShapeModelName(), history, file.getAbsolutePath());
     }

     public void loadIntervalFromFile(File runFile, SmallBodyModel bodyModel)
     {
         StateHistory newRow = StateHistoryModelIOHelper.loadStateHistoryFromFile(runFile, smallBodyModel.getConfig().getShapeModelName(), new StateHistoryKey(runs));
         runs.addRunToList(newRow);
         fireHistorySegmentCreatedListener(newRow);
     }

     public void initializeRunList() throws IOException
     {
         if (initialized)
             return;

         MapUtil configMap = new MapUtil(getConfigFilename());

         boolean needToUpgradeConfigFile = false;
         String[] runNames = configMap.getAsArray(StateHistoryModel.RUN_NAMES);
         String[] runFilenames = configMap.getAsArray(StateHistoryModel.RUN_FILENAMES);
         if (runFilenames == null)
         {
             // Mark that we need to upgrade config file to latest version
             // which we'll do at end of function.
             needToUpgradeConfigFile = true;
             initialized = true;
             return;
         }

         int numRuns = runFilenames.length;
         for (int i=0; i<numRuns; ++i)
         {
             RunInfo runInfo = new RunInfo();
             runInfo.name = runNames[i];
             runInfo.runfilename = runFilenames[i];

         }

         if (needToUpgradeConfigFile)
             updateConfigFile();

         initialized = true;
     }

     private String getConfigFilename()
     {
         return modelManager.getPolyhedralModel().getConfigFilename();
     }

     private void updateConfigFile()
     {
//         MapUtil configMap = new MapUtil(getConfigFilename());
//
//         String runNames = "";
//         String runFilenames = "";
//
//         DefaultListModel runListModel = (DefaultListModel)optionsTable.getModel();
//         for (int i=0; i<runListModel.size(); ++i)
//         {
//             RunInfo runInfo = (RunInfo)runListModel.get(i);
//
//             runFilenames += runInfo.runfilename;
//             runNames += runInfo.name;
//
//             if (i < runListModel.size()-1)
//             {
//                 runNames += CustomShapeModel.LIST_SEPARATOR;
//                 runFilenames += CustomShapeModel.LIST_SEPARATOR;
//             }
//         }
//
//         Map<String, String> newMap = new LinkedHashMap<String, String>();
//
//         newMap.put(StateHistoryModel.RUN_NAMES, runNames);
//         newMap.put(StateHistoryModel.RUN_FILENAMES, runFilenames);
//
//         configMap.put(newMap);
     }

	public int getDefaultSliderValue()
	{
		return defaultSliderValue;
	}

	public void setDefaultSliderValue(int defaultSliderValue)
	{
		this.defaultSliderValue = defaultSliderValue;
	}

	public int getSliderFinalValue()
	{
		return sliderFinalValue;
	}

	public void setSliderFinalValue(int sliderFinalValue)
	{
		this.sliderFinalValue = sliderFinalValue;
	}

	public String getStatusBarString()
	{
		return statusBarString;
	}

	public void setStatusBarString(String statusBarString)
	{
		this.statusBarString = statusBarString;
		//TODO fire something here?
	}

	public StateHistory getCurrentFlybyStateHistory()
	{
		return currentFlybyStateHistory;
	}

	public SmallBodyModel getSmallBodyModel()
	{
		return smallBodyModel;
	}

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        Trajectory traj = currentFlybyStateHistory.getTrajectory();
        if (traj == null) return "";
        return traj.toString();
    }

	public double getScalingFactor()
	{
		return scalingFactor;
	}

//    private void setCurrentTrajectory(Trajectory temp)
//    {
//        trajectory = temp;
//    }

//    // sets the renderer to the information in the animation frame
//    private void setAnimationFrame(AnimationFrame frame)
//    {
//        setTimeFraction(frame.timeFraction);
//        frame.panel.setTimeSlider(frame.timeFraction);
//    }

//    public void setTrajectoryName(String name)
//    {
//    	this.trajectory.setName(name);
//    }


}