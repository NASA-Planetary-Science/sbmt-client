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
import edu.jhuapl.sbmt.stateHistory.rendering.model.StateHistoryRendererManager;

import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

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

	private String customDataFolder;

	/**
	 *
	 */
	private StateHistoryCollection runs;

	/**
	 *
	 */
	public static int INITIAL_SLIDER_VALUE = 0;

	/**
	 *
	 */
	public static int FINAL_SLIDER_VALUE = 900;

	/**
	 *
	 */
	private ISmallBodyViewConfig viewConfig;

	/**
	 *
	 */
	private IStateHistoryIntervalGenerator activeIntervalGenerator;

	private boolean initialized;

	private StateHistoryRendererManager rendererManager;

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
	public StateHistoryModel(SmallBodyModel smallBodyModel, StateHistoryRendererManager rendererManager) throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException
	{
		this.viewConfig = smallBodyModel.getSmallBodyConfig();
		this.customDataFolder = smallBodyModel.getCustomDataFolder();
		this.runs = rendererManager.getRuns();
		this.rendererManager = rendererManager;
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
		rendererManager.removeRun(historySegment);
		runs.removeRunFromList(historySegment);
		rendererManager.setAllItems(runs.getSimRuns());
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
				rendererManager.setAllItems(runs.getSimRuns());
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
		rendererManager.setAllItems(runs.getSimRuns());
		fireHistorySegmentCreatedListener(newRow);
		updateConfigFile();
	}

	public void addInterval(StateHistory history) throws StateHistoryIOException
	{
		runs.addRunToList(history);
		rendererManager.setAllItems(runs.getSimRuns());
		fireHistorySegmentCreatedListener(history);
		try {
			updateConfigFile();
		}
		catch (IOException e)
		{
			throw new StateHistoryIOException(e);
		}
	}

	public List<StateHistory> loadRunList() throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException, StateHistoryIOException
	{
		List<StateHistory> invalidHistories = new ArrayList<StateHistory>();
		if (initialized) return invalidHistories;
		if (!(new File(getConfigFilename()).exists())) return invalidHistories;

		FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "StateHistory");
		runs.retrieve(metadata);
		for (StateHistory history : runs.getSimRuns())
		{
			history.validate();
			if (history.isValid() == false)
			{
				boolean invalidKernelAlreadyExists = invalidHistories.stream().filter(his -> his.getSourceFile().equals(history.getSourceFile())).count() > 0;
				if (!invalidKernelAlreadyExists)
					invalidHistories.add(history);
			}
		}
		return invalidHistories;
	}

	/**
	 * @throws IOException
	 */
	public void initializeRunList() throws IOException, StateHistoryInputException, StateHistoryInvalidTimeException, StateHistoryIOException//, SpiceKernelNotFoundException
	{
		for (StateHistory history : rendererManager.getAllItems())
		{
			setIntervalGenerator(history.getType());
			SpiceInfo spice = null;
			if (history instanceof SpiceStateHistory) spice = ((SpiceStateHistory)history).getSpiceInfo();
			activeIntervalGenerator.setSourceFile(history.getSourceFile(), spice);
			activeIntervalGenerator.createNewTimeInterval(history, null);
		}

		initialized = true;
	}

	/**
	 * @return
	 */
	private String getConfigFilename()
	{
		File stateHistoryConfigFile = new File(customDataFolder, "stateHistory.txt");
		return stateHistoryConfigFile.getAbsolutePath();
	}

	/**
	*
	*/
	private void updateConfigFile() throws IOException
	{
		Serializers.serialize("StateHistory", runs, new File(getConfigFilename()));
	}

//	/**
//	 * @param statusBarString
//	 */
//	public void setStatusBarString(String statusBarString)
//	{
//		this.statusBarString = statusBarString;
//		// TODO fire something here?
//	}

	public void setIntervalGenerator(StateHistorySourceType generatorType)
	{
		this.activeIntervalGenerator = intervalGenerators.get(generatorType);
	}

	public void registerIntervalGenerator(StateHistorySourceType generatorType, IStateHistoryIntervalGenerator generator)
	{
		intervalGenerators.put(generatorType, generator);
	}

	/**
	 * @return the runs
	 */
	public StateHistoryCollection getRuns()
	{
		return runs;
	}

//	/**
//	 * @return the statusBarString
//	 */
//	public String getStatusBarString()
//	{
//		return statusBarString;
//	}

	/**
	 * @return the activeIntervalGenerator
	 */
	public IStateHistoryIntervalGenerator getActiveIntervalGenerator()
	{
		return activeIntervalGenerator;
	}

	/**
	 * @param activeIntervalGenerator the activeIntervalGenerator to set
	 */
	public void setActiveIntervalGenerator(IStateHistoryIntervalGenerator activeIntervalGenerator)
	{
		this.activeIntervalGenerator = activeIntervalGenerator;
	}

	/**
	 * @return the customDataFolder
	 */
	public String getCustomDataFolder()
	{
		return customDataFolder;
	}

	/**
	 * @return the viewConfig
	 */
	public ISmallBodyViewConfig getViewConfig()
	{
		return viewConfig;
	}
}