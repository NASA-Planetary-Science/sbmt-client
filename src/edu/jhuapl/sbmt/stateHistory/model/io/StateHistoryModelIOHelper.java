package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.SpiceStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;

/**
 * Helper class for saving/loading state history intervals to the filesystem.
 * @author steelrj1
 *
 */
public class StateHistoryModelIOHelper
{

	/**
	 * Saves state history interval to file. Format is the full expanded "Mark 1"
	 * format, used before SPICE integration.
	 * @param shapeModelName
	 * @param interval
	 * @param fileName
	 * @throws StateHistoryIOException
	 */
	public static void saveIntervalToFile(String shapeModelName, StateHistory interval, String fileName) throws StateHistoryIOException
    {
		interval.saveStateToFile(shapeModelName, fileName);
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
	public static StateHistory loadStateHistoryFromFile(File runFile, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException
    {
		String extension = FilenameUtils.getExtension(runFile.getAbsolutePath());
		StateHistory history = null;
		if (extension.equals("spicestate"))
		{
			history = new SpiceStateHistory(key);
			history = history.loadStateHistoryFromFile(runFile, shapeModelName, key);
		}
		else if (extension.equals("csvstate"))
		{
			history = new StandardStateHistory(key);
			history = history.loadStateHistoryFromFile(runFile, shapeModelName, key);
		}
		else
			throw new StateHistoryIOException("Invalid file extension used; please load a .csvstate or .spicestate file");
		return history;
    }
}
