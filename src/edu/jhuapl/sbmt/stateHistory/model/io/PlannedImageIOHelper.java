package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImage;
import edu.jhuapl.sbmt.stateHistory.model.planning.imagers.PlannedImageCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

public class PlannedImageIOHelper
{
	/**
	 * Reads in planned images from a list.  Assumes the following format
	 * TimeInET,InstrumentName
	 *
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void loadPlannedImagesFromFileWithName(String filename, PlannedImageCollection collection, ProgressStatusListener listener, Runnable completion) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = reader.readLine()) != null)
		{
			if (collection.getNumItems() > 100) break;
			//Skip the headers
			if (line.startsWith("#")) continue;
			//Now we've reached entries - build the
			String[] parts = line.split(",");
			String instrumentName = parts[1];
			double et = TimeUtil.str2et(parts[2]);
			PlannedImage plannedImage = new PlannedImage(et, instrumentName.toUpperCase());
			collection.addImageToList(plannedImage, listener);
		}
		reader.close();
		completion.run();
	}

	/**
	 * Saves current planned image collection list to file
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void savePlannedImagesToFileWithName(String filename, PlannedImageCollection collection) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		writer.write("## Planned Image list, updated on " + dtf.format(now));
		writer.newLine();
		for (PlannedImage image : collection.getAllItems())
		{
			writer.write(image.getTime() + "," + image.getInstrumentName());
			writer.newLine();
		}

		writer.close();
	}
}