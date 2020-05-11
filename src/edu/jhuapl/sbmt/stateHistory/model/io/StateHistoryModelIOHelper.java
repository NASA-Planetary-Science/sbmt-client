package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StateHistoryKey;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

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
        // writes the header for the new history
		try
        {
            FileWriter writer = new FileWriter(fileName);
//            writer.append(config.getShapeModelName());
            writer.append(',');
            writer.append('\n');

            // Create header of name, description, color
            writer.append(interval.getStateHistoryName() + ',');
            writer.append(interval.getTrajectory().getTrajectoryDescription() + ',');
            for (double colorElement : interval.getTrajectory().getTrajectoryColor()) {
                writer.append(Double.toString(colorElement));
                writer.append(',');
            }
            writer.append(Double.toString(interval.getTrajectory().getTrajectoryThickness()));
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
        Set<Double> keySet = interval.getAllTimes();
        for (Double key : keySet) {
            CsvState history = (CsvState)interval.getStateAtTime(key);
            history.writeToCSV(fileName);
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
	public static StateHistory loadStateHistoryFromFile(File runFile, String shapeModelName, StateHistoryKey key) throws StateHistoryIOException
    {
		ArrayList<String[]> timeArray = new ArrayList<>(3);
        Integer firstIndex = null;
        String runDirName = runFile.getAbsolutePath();
        StandardTrajectory trajectory;

        try
        {
            String runName = runFile.getName();
            if (!runName.endsWith(".csv")) throw new StateHistoryIOException("File does not have a csv extension; please choose another file");

            BufferedReader in = new BufferedReader(new FileReader(runFile));
//                String beforeParse = in.readLine();
//                String input = beforeParse.substring(0, beforeParse.indexOf(','));
//                if(input.equals(shapeModelName)){
//                    passFileNames.add(runName);
//                }
            in.close();


            StateHistory history = null;
            in = new BufferedReader(new FileReader(runDirName));

            if (firstIndex == null)
                firstIndex = 0;

            trajectory = new StandardTrajectory();
            // fill in the Trajectory parameters
            trajectory.setId(0);

            // create a new history instance and add it to the Map
            history = new StandardStateHistory(key);

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

                trajectory.getX().add(spacecraftPosition[0]);
                trajectory.getY().add(spacecraftPosition[1]);
                trajectory.getZ().add(spacecraftPosition[2]);

                if(com.mysql.jdbc.StringUtils.isNullOrEmpty(timeArray.get(0)[0])){
                    timeArray.get(0)[0] = flybyState.getUtc();
                }
                timeArray.get(0)[1] = flybyState.getUtc();
            }
            in.close();

            history.setTrajectory(trajectory);

            history.setCurrentTime(history.getMinTime());
            return history;
        }
        catch (Exception e1)
        {
        	e1.printStackTrace();
            throw new StateHistoryIOException("There was a problem reading the state history file.  See the console for details, and please make sure it is the right format", e1);
        }
    }
}
