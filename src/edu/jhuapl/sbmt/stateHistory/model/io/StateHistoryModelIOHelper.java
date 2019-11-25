package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.stateHistory.model.StateHistoryModel;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.State;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.StateHistory;
import edu.jhuapl.sbmt.stateHistory.model.scState.CsvState;
import edu.jhuapl.sbmt.stateHistory.model.stateHistory.StandardStateHistory;
import edu.jhuapl.sbmt.stateHistory.model.trajectory.StandardTrajectory;

public class StateHistoryModelIOHelper
{

	public static void saveIntervalToFile(StateHistoryModel model, String fileName)
    {
        SmallBodyViewConfig config = (SmallBodyViewConfig) smallBodyModel.getConfig();
        // writes the header for the new history
        try
        {
            FileWriter writer = new FileWriter(fileName);
            writer.append(config.getShapeModelName());
            writer.append(',');
            writer.append('\n');

            // Create header of name, description, color
            writer.append(model.trajectoryName + ',');
            writer.append(model.description + ',');
            for (double colorElement : model.trajectoryColor) {
                writer.append(Double.toString(colorElement));
                writer.append(',');
            }
            writer.append(Double.toString(model.trajectoryLineThickness));
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // get each flyby state in currentFlybyStateHistory, and write to CSV
        Set<Double> keySet = model.currentFlybyStateHistory.getAllKeys();
        for (Double key : keySet) {
            CsvState history = (CsvState)model.currentFlybyStateHistory.getValue(key);
            history.writeToCSV(fileName);
        }

    }

	public StateHistoryModel loadStateHistoryFromFile(File runFile)
    {
        Integer firstIndex = null;
        String runDirName = runFile.getAbsolutePath();

        initialize();

        try
        {
            String runName = runFile.getName();
            if (runName.endsWith(".csv"))
            {
                BufferedReader in = new BufferedReader(new FileReader(runFile));
                String beforeParse = in.readLine();
                String input = beforeParse.substring(0, beforeParse.indexOf(','));
                if(input.equals(smallBodyModel.getConfig().getShapeModelName())){
                    passFileNames.add(runName);
                }
                in.close();
            }
        }
        catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        try {
            BufferedReader in = new BufferedReader(new FileReader(runDirName));

            if (firstIndex == null)
                firstIndex = 0;

            trajectory = new StandardTrajectory();
            // fill in the Trajectory parameters
            trajectory.setId(0);

            // create a new history instance and add it to the Map
            StateHistory history = new StandardStateHistory();

            // discard first line of body name
            in.readLine();

            // get name, desc, color form second line
            String info = in.readLine();
            String[] data = info.split(",");
            trajectory.setName(data[0]);
            setTrajectoryName(data[0]);
            setDescription(data[1]);
            setTrajectoryColor(new double[]{Double.parseDouble(data[2]), Double.parseDouble(data[3]),
                                            Double.parseDouble(data[4]),Double.parseDouble(data[5])});
            setTrajectoryLineThickness(Double.parseDouble(data[6]));


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
                history.put(flybyState);

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

            this.currentFlybyStateHistory = history;


        } catch (Exception e) {
            e.printStackTrace();
        }

        DateTime start = ISODateTimeFormat.dateTimeParser().parseDateTime(timeArray.get(0)[0]);
        DateTime stop = ISODateTimeFormat.dateTimeParser().parseDateTime(timeArray.get(0)[1]);
        this.startTime = start;
        this.endTime = stop;


        // set up vtk stuff
        createTrajectoryPolyData();
        trajectoryMapper.SetInputData(trajectoryPolylines);
        trajectoryActor.SetMapper(trajectoryMapper);
        setTimeFraction(0.0);


        initialize();
        spacecraftBody.Modified();
        trajectoryActor.Modified();
        return this;

    }

}
