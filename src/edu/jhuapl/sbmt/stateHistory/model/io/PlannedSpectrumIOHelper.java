package edu.jhuapl.sbmt.stateHistory.model.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.jhuapl.saavtk.util.ProgressStatusListener;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.stateHistory.model.interfaces.IStateHistoryMetadata;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrum;
import edu.jhuapl.sbmt.stateHistory.model.planning.spectrometers.PlannedSpectrumCollection;

public class PlannedSpectrumIOHelper
{

	/**
	 * Reads in planned spectra from a list.  Assumes the following format
	 * TimeInET,InstrumentName
	 *
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void loadPlannedSpectraFromFileWithName(String filename, IStateHistoryMetadata metadata, PlannedSpectrumCollection collection, ProgressStatusListener listener, Runnable completion) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = reader.readLine()) != null)
		{
			//Skip the headers
			if (line.startsWith("#")) continue;
			//Now we've reached entries - build the
			String[] parts = line.split(",");
			Double etStart = TimeUtil.str2et(parts[0]);
			Double etEnd = TimeUtil.str2et(parts[1]);
			String instrumentName = parts[2];
			Integer cadence = Integer.parseInt(parts[3]);
			PlannedSpectrum plannedSpectrum = new PlannedSpectrum(etStart, etEnd, cadence, instrumentName);
			plannedSpectrum.setStateHistoryMetadata(metadata);
			collection.addSpectrumToList(plannedSpectrum, listener);
		}
		reader.close();
		completion.run();
	}

	/**
	 * Saves current planned spectra collection list to file
	 * @param filename
	 * @param collection
	 * @throws IOException
	 */
	public static void savePlannedSpectraToFileWithName(String filename, PlannedSpectrumCollection collection) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		writer.write("## Planned Spectrum list, updated on " + dtf.format(now));
		writer.newLine();
		for (PlannedSpectrum spectrum : collection.getAllItems())
		{
			writer.write(spectrum.getTime() + "," + spectrum.getStopTime() + "," + spectrum.getCadence() + "," + spectrum.getInstrumentName());
			writer.newLine();
		}

		writer.close();
	}
}
