package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.LocalDateTime;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;

import vtk.vtkActor;

public class NISSpectrum extends Model
{
	private String fullpath; // The actual path of the spectrum stored on the local disk (after downloading from the server)
	private String serverpath; // The path of the spectrum as passed into the constructor. This is not the 
	   // same as fullpath but instead corresponds to the name needed to download
	   // the file from the server (excluding the hostname).

	static public final int DATE_TIME_OFFSET = 0;
	static public final int DURATION_OFFSET = 3;
	static public final int RANGE_OFFSET = 252;
	static public final int POLYGON_TYPE_FLAG_OFFSET = 258;
	static public final int NUMBER_OF_VERTICES_OFFSET = 259;
	static public final int POLYGON_START_COORDINATES_OFFSET = 260;
	
	LocalDateTime dateTime;
	double duration;
	byte polygon_type_flag;
	ArrayList<LatLon> latLons = new ArrayList<LatLon>();
	
	public NISSpectrum(String filename) throws IOException
	{
		this.serverpath = filename;
		
		// Download the spectrum.
		File nisFile = FileCache.getFileFromServer(filename);

		filename = nisFile.getAbsolutePath();
		this.fullpath = filename;


		ArrayList<String> values = FileUtil.getFileWordsAsStringList(fullpath);
		dateTime = new LocalDateTime(values.get(DATE_TIME_OFFSET));
		
		duration = Double.parseDouble(values.get(DURATION_OFFSET));
		polygon_type_flag = Byte.parseByte(values.get(POLYGON_TYPE_FLAG_OFFSET));
		
		int polygonSize = Integer.parseInt(values.get(NUMBER_OF_VERTICES_OFFSET));
		for (int i=0; i<polygonSize; ++i)
		{
			int latIdx = POLYGON_START_COORDINATES_OFFSET + i*2;
			int lonIdx = POLYGON_START_COORDINATES_OFFSET + i*2 + 1;
			
			latLons.add(new LatLon(Double.parseDouble(values.get(latIdx)),
								   Double.parseDouble(values.get(lonIdx))));
		}
	}
	
	private void convertLatLonsToVtkPolyData()
	{
		
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		return null;
	}

}
