package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrack;
import edu.jhuapl.sbmt.stateHistory.model.planning.lidar.PlannedLidarTrackCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

public class PlannedLidarTrackIOHelper
{
	/**
	 * Reads in planned images from a list.  Assumes the following format
	 * TimeInET,InstrumentName
	 *
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void loadPlannedLidarTracksFromFileWithName(String filename, PlannedLidarTrackCollection collection) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = reader.readLine()) != null)
		{
			//Skip the headers
			if (line.startsWith("#")) continue;
			//Now we've reached entries - build the
			String[] parts = line.split(",");
			String instrumentName = parts[2];
			double etStart = TimeUtil.str2et(parts[0]);
			double etStop = TimeUtil.str2et(parts[1]);
//			Double et = Double.parseDouble(parts[0]);
//			String instrumentName = parts[1];
			PlannedLidarTrack plannedLidarTrack = new PlannedLidarTrack(etStart, etStop, instrumentName.toUpperCase());
			collection.addLidarTrackToList(plannedLidarTrack);
		}
		reader.close();
	}

	/**
	 * Saves current planned image collection list to file
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void savePlannedLidarTracksToFileWithName(String filename, PlannedLidarTrackCollection collection) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		writer.write("## Planned Lidar Track list, updated on " + dtf.format(now));
		writer.newLine();
		for (PlannedLidarTrack image : collection.getAllItems())
		{
			writer.write(image.getTime() + "," + image.getInstrumentName());
			writer.newLine();
		}

		writer.close();
	}

}
