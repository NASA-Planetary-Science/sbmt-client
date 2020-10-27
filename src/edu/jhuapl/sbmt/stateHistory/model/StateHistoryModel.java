package edu.jhuapl.sbmt.stateHistory.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.client.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryIntervalGenerator;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryCollectionChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistoryModelChangedListener;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryIOException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInputException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryInvalidTimeException;
import edu.jhuapl.sbmt.stateHistory.model.io.StateHistoryModelIOHelper;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryCollection;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.gson.Serializers;
import lombok.Getter;
import lombok.Setter;

/**
 * @author steelrj1
 *
 */
public class StateHistoryModel
{
	/**
	 *
	 */
	Map<StateHistorySourceType, IStateHistoryIntervalGenerator> intervalGenerators = new HashMap<StateHistorySourceType, IStateHistoryIntervalGenerator>();

	/**
	 *
	 */
	List<StateHistoryModelChangedListener> listeners = new ArrayList<StateHistoryModelChangedListener>();

	/**
	 *
	 */
	private ModelManager modelManager;

	/**
	 * Start time of the available state history
	 */
	@Getter
	private DateTime startTime;

	/**
	 * End time of the available state history
	 */
	@Getter
	private DateTime endTime;

	/**
	 *
	 */
	@Getter
	private StateHistoryCollection runs;

	/**
	 *
	 */
	@Getter
	private int defaultSliderValue = 0;

	/**
	 *
	 */
	@Getter
	private int sliderFinalValue = 900;

	/**
	 *
	 */
	@Getter
	private String statusBarString;

	/**
	 *
	 */
	private ISmallBodyViewConfig viewConfig;

	/**
	 *
	 */
	@Getter @Setter
	private IStateHistoryIntervalGenerator activeIntervalGenerator;

	private boolean initialized;

	/**
	 * @param start
	 *            Start time of the inclusive range for this body model's state
	 *            history
	 * @param end
	 *            End time of the inclusive range for this body model's state
	 *            history
	 * @param smallBodyModel
	 * @param renderer
	 * @param modelManager
	 * @throws StateHistoryInputException
	 * @throws IOException
	 */
	public StateHistoryModel(DateTime start, DateTime end, SmallBodyModel smallBodyModel, Renderer renderer,
			ModelManager modelManager) throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException
	{
		this.viewConfig = smallBodyModel.getSmallBodyConfig();
		this.startTime = start;
		this.endTime = end;
		this.modelManager = modelManager;
		this.runs = (StateHistoryCollection) modelManager.getModel(ModelNames.STATE_HISTORY_COLLECTION);
		this.runs.addStateHistoryCollectionChangedListener(new StateHistoryCollectionChangedListener()
		{

			@Override
			public void historySegmentUpdated(StateHistory history)
			{
				try
				{
					updateConfigFile();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @param listener
	 */
	public void addStateHistoryModelChangedListener(StateHistoryModelChangedListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * @param historySegment
	 */
	private void fireTimeChangeListener(double t)
	{
		listeners.forEach(listener -> listener.timeChanged(t));
	}

	/**
	 * @param historySegment
	 */
	private void fireHistorySegmentCreatedListener(StateHistory historySegment)
	{
		listeners.forEach(listener -> listener.historySegmentCreated(historySegment));
	}

	/**
	 * @param historySegment
	 */
	private void fireHistorySegmentRemovedListener(StateHistory historySegment)
	{
		listeners.forEach(listener -> listener.historySegmentRemoved(historySegment));
	}

	public void removeRun(StateHistory historySegment) throws IOException
	{
		runs.removeRun(historySegment.getKey());
		runs.removeRunFromList(historySegment);
		fireHistorySegmentRemovedListener(historySegment);
		updateConfigFile();
	}

	/***
	 * Interval Creation
	 ***/

	/***
	 * Creates a new time history for the body with the given time range.
	 *
	 * @param key
	 *            StateHistoryKey representing this interval
	 * @param length
	 *            length of interval
	 * @param name
	 *            name of interval
	 * @param progressionFunction
	 *            Closure-like Function that takes in progress and allows you to
	 *            update completion status
	 * @return StateHistory object if successful; null otherwise
	 */
	public void createNewTimeInterval(StateHistoryKey key, DateTime startTime, DateTime endTime, double duration,
			String name, Function<Double, Void> progressFunction) throws StateHistoryInputException, StateHistoryInvalidTimeException, IOException, InvocationTargetException, InterruptedException
	{
		StateHistory history = activeIntervalGenerator.createNewTimeInterval(key, startTime, endTime, duration, name,
				progressFunction);

		SwingUtilities.invokeAndWait(new Runnable()
		{
			@Override
			public void run()
			{
				runs.addRunToList(history);
				fireHistorySegmentCreatedListener(history);
			}
		});
		updateConfigFile();
	}

	/**
	 * @param history
	 * @param file
	 * @throws StateHistoryIOException
	 */
	public void saveHistoryToFile(StateHistory history, File file) throws StateHistoryIOException
	{
		StateHistoryModelIOHelper.saveIntervalToFile(viewConfig.getShapeModelName(), history, file.getAbsolutePath());
	}

	/**
	 * @param runFile
	 * @param bodyModel
	 * @throws StateHistoryIOException
	 */
	public void loadIntervalFromFile(File runFile, SmallBodyModel bodyModel) throws StateHistoryIOException, IOException
	{
		StateHistory newRow = StateHistoryModelIOHelper.loadStateHistoryFromFile(runFile,
				viewConfig.getShapeModelName(), new StateHistoryKey(runs));
		runs.addRunToList(newRow);
		fireHistorySegmentCreatedListener(newRow);
		updateConfigFile();
	}

	/**
	 * @throws IOException
	 */
	public void initializeRunList() throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException
	{
		if (initialized)
			return;
		if (!(new File(getConfigFilename()).exists())) return;

		FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "StateHistory");
		runs.retrieve(metadata);
		for (StateHistory history : runs.getAllItems())
		{
			setIntervalGenerator(history.getType());
			SpiceInfo spice = null;
			if (history instanceof SpiceStateHistory) spice = ((SpiceStateHistory)history).getSpiceInfo();
			activeIntervalGenerator.setSourceFile(history.getSourceFile(), spice);
			activeIntervalGenerator.createNewTimeInterval(history, null);
			if (runs.getCurrentRun() == null) runs.setCurrentRun(history);
			//TODO needs fixing?
			runs.setTimeFraction(0.0);
		}

		initialized = true;
	}

	/**
	 * @return
	 */
	private String getConfigFilename()
	{
		String parentDirectory = modelManager.getPolyhedralModel().getCustomDataFolder();
		File stateHistoryConfigFile = new File(parentDirectory, "stateHistory.txt");
		return stateHistoryConfigFile.getAbsolutePath();
	}

	/**
	*
	*/
	private void updateConfigFile() throws IOException
	{
		Serializers.serialize("StateHistory", runs, new File(getConfigFilename()));
	}

	/**
	 * @param statusBarString
	 */
	public void setStatusBarString(String statusBarString)
	{
		this.statusBarString = statusBarString;
		// TODO fire something here?
	}

	public void setIntervalGenerator(StateHistorySourceType generatorType)
	{
		this.activeIntervalGenerator = intervalGenerators.get(generatorType);
	}

	public void registerIntervalGenerator(StateHistorySourceType generatorType, IStateHistoryIntervalGenerator generator)
	{
		intervalGenerators.put(generatorType, generator);
	}

	public void setTime(double time)
	{
		fireTimeChangeListener(time);
	}
}