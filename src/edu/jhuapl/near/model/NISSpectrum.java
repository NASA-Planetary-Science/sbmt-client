package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.WeakHashMap;

import org.joda.time.LocalDateTime;

import edu.jhuapl.near.util.FileCache;
import edu.jhuapl.near.util.FileUtil;
import edu.jhuapl.near.util.LatLon;

import vtk.*;

public class NISSpectrum extends Model
{
	private String fullpath; // The actual path of the spectrum stored on the local disk (after downloading from the server)
	private String serverpath; // The path of the spectrum as passed into the constructor. This is not the 
	   // same as fullpath but instead corresponds to the name needed to download
	   // the file from the server (excluding the hostname).

	static public final int DATE_TIME_OFFSET = 0;
	static public final int DURATION_OFFSET = 3+2;
	static public final int MET_OFFSET_TO_MIDDLE_OFFSET = 4+2;
	static public final int RANGE_OFFSET = 252+2;
	static public final int POLYGON_TYPE_FLAG_OFFSET = 258+2;
	static public final int NUMBER_OF_VERTICES_OFFSET = 259+2;
	static public final int POLYGON_START_COORDINATES_OFFSET = 260+2;
	
	private LocalDateTime dateTime;
	private double duration;
	private short polygon_type_flag;
	private double range;
	private ArrayList<LatLon> latLons = new ArrayList<LatLon>();
	private vtkPolyData footprint;
    private vtkActor footprintActor;
    private ArrayList<vtkActor> footprintActors = new ArrayList<vtkActor>();
    private ErosModel erosModel;
    
	/**
	 * Because instances of NISSpectrum can be expensive, we want to there to be
	 * no more than one instance of this class per image file on the server.
	 * Hence this class was created to manage the creation and deletion of
	 * NISSpectrums. Anyone needing a NISSpectrum should use this factory class to
	 * create NISSpectrums and should NOT call the constructor directly.
	 */
	public static class NISSpectrumFactory
	{
		static private WeakHashMap<NISSpectrum, Object> spectra = 
			new WeakHashMap<NISSpectrum, Object>();
		
		static public NISSpectrum createSpectrum(String name, ErosModel eros) throws IOException
		{
			for (NISSpectrum spectrum : spectra.keySet())
			{
				if (spectrum.getServerPath().equals(name))
					return spectrum;
			}

			NISSpectrum spectrum = new NISSpectrum(name, eros);
			spectra.put(spectrum, null);
			return spectrum;
		}
	}

	
	public NISSpectrum(String filename, ErosModel eros) throws IOException
	{
		// Download the spectrum.
		this(FileCache.getFileFromServer(filename), eros);
		this.serverpath = filename;
	}

	public NISSpectrum(File nisFile, ErosModel eros) throws IOException
	{
		this.erosModel = eros;
		
		String filename = nisFile.getAbsolutePath();
		this.fullpath = filename;


		ArrayList<String> values = FileUtil.getFileWordsAsStringList(fullpath);

		dateTime = new LocalDateTime(values.get(DATE_TIME_OFFSET));

		double metOffsetToMiddle = Double.parseDouble(values.get(MET_OFFSET_TO_MIDDLE_OFFSET));
		dateTime = dateTime.plusMillis((int)metOffsetToMiddle);
		
		duration = Double.parseDouble(values.get(DURATION_OFFSET));
		range = Double.parseDouble(values.get(RANGE_OFFSET));
		polygon_type_flag = Short.parseShort(values.get(POLYGON_TYPE_FLAG_OFFSET));
		
		int polygonSize = Integer.parseInt(values.get(NUMBER_OF_VERTICES_OFFSET));
		for (int i=0; i<polygonSize; ++i)
		{
			int latIdx = POLYGON_START_COORDINATES_OFFSET + i*2;
			int lonIdx = POLYGON_START_COORDINATES_OFFSET + i*2 + 1;
			
			latLons.add(new LatLon(Double.parseDouble(values.get(latIdx)) * Math.PI / 180.0,
								   (360.0-Double.parseDouble(values.get(lonIdx))) * Math.PI / 180.0));
		}
		
		this.convertLatLonsToVtkPolyData();
	}
	
	private void convertLatLonsToVtkPolyData()
	{
		footprint = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray lines = new vtkCellArray();
        
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(latLons.size() + 1);
        points.SetNumberOfPoints(latLons.size());
        
        int i=0;
        for (LatLon ll : latLons)
        {
        	//double[] xyz = LatLon.latLonToRec(ll);
        	double[] xyz = erosModel.latLonToXyz(ll.lat, ll.lon);
        	points.SetPoint(i, xyz);
        	idList.SetId(i, i);
        	++i;
        }
    	idList.SetId(i, 0);
    	
    	lines.InsertNextCell(idList);
    	
    	footprint.SetPoints(points);
        footprint.SetLines(lines);
        
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		if (footprintActor == null)
		{
			vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
			footprintMapper.SetInput(footprint);
			//footprintMapper.SetResolveCoincidentTopologyToPolygonOffset();
			//footprintMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1000.0, -1000.0);

			footprintActor = new vtkActor();
			footprintActor.SetMapper(footprintMapper);
	        footprintActor.GetProperty().SetColor(0.0, 1.0, 0.0);
	        footprintActor.GetProperty().SetLineWidth(2.0);

			footprintActors.add(footprintActor);
		}
		
		return footprintActors;
	}

	public String getServerPath()
	{
		return serverpath;
	}
	
	public double getRange()
	{
		return range;
	}

	public double getDuration()
	{
		return duration;
	}
	
	public LocalDateTime getDateTime()
	{
		return dateTime;
	}
	
	public short getPolygonTypeFlag()
	{
		return polygon_type_flag;
	}
}
