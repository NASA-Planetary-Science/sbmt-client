package edu.jhuapl.near.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
	static public final int MET_OFFSET = 1;
	static public final int CURRENT_SEQUENCE_NUM_OFFSET = 1;
	static public final int DURATION_OFFSET = 3+2;
	static public final int MET_OFFSET_TO_MIDDLE_OFFSET = 4+2;
	static public final int CALIBRATED_GE_DATA_OFFSET = 96+2;
	static public final int CALIBRATED_GE_NOISE_OFFSET = 160+2;
	static public final int RANGE_OFFSET = 252+2;
	static public final int POLYGON_TYPE_FLAG_OFFSET = 258+2;
	static public final int NUMBER_OF_VERTICES_OFFSET = 259+2;
	static public final int POLYGON_START_COORDINATES_OFFSET = 260+2;
	
	private DateTime dateTime;
	private double duration;
	private short polygon_type_flag;
	private double range;
	private ArrayList<LatLon> latLons = new ArrayList<LatLon>();
	private vtkPolyData footprint;
    private vtkActor footprintActor;
    private ArrayList<vtkActor> footprintActors = new ArrayList<vtkActor>();
    private ErosModel erosModel;
    private double[] spectrum = new double[64];
    private double[] spectrumErros = new double[64];
    
    // These values were taken from Table 1 of "Spectral properties and geologic
    // processes on Eros from combined NEAR NIS and MSI data sets" 
    // by Noam Izenberg et. al.
    static final private double[] bandCenters = {
    	816.2,  837.8,  859.4,  881.0,  902.7,  924.3,  945.9,  967.5,
    	989.1,  1010.7, 1032.3, 1053.9, 1075.5, 1097.1,	1118.8, 1140.4,
    	1162.0,	1183.6, 1205.2, 1226.8, 1248.4, 1270.0,	1291.6, 1313.2,
    	1334.9,	1356.5, 1378.1, 1399.7, 1421.3, 1442.9,	1464.5, 1486.1,
    	1371.8,	1414.9, 1458.0, 1501.1, 1544.2, 1587.3,	1630.4, 1673.6,
    	1716.7,	1759.8, 1802.9, 1846.0, 1889.1, 1932.2,	1975.3, 2018.4,
    	2061.5,	2104.7, 2147.8, 2190.9, 2234.0, 2277.1,	2320.2, 2363.3,
    	2406.4,	2449.5, 2492.6, 2535.8, 2578.9, 2622.0,	2665.1, 2708.2
    };
    
    
	/**
	 * Because instances of NISSpectrum can be expensive, we want there to be
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

		dateTime = new DateTime(values.get(DATE_TIME_OFFSET), DateTimeZone.UTC);

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
		
		for (int i=0; i<64; ++i)
		{
			spectrum[i] = Double.parseDouble(values.get(CALIBRATED_GE_DATA_OFFSET + i));
			spectrumErros[i] = Double.parseDouble(values.get(CALIBRATED_GE_NOISE_OFFSET + i));
		}
		
		this.convertLatLonsToVtkPolyData();
	}
	
	private ArrayList<LatLon> subdivideLatLons(ArrayList<LatLon> latLons, double maxAngularSep)
	{
		ArrayList<LatLon> origLatLons = new ArrayList<LatLon>(latLons);
		ArrayList<LatLon> subdividedLatLons = new ArrayList<LatLon>();

		boolean needToSubdivide = true;

		while (needToSubdivide)
		{
			subdividedLatLons.add(origLatLons.get(0));

			int N = origLatLons.size() - 1;
			for (int i=0; i<N; ++i)
			{
				LatLon ll1 = origLatLons.get(i);
				LatLon ll2 = origLatLons.get(i+1);

				LatLon midll = LatLon.midpoint(ll1, ll2);

				subdividedLatLons.add(midll);
				subdividedLatLons.add(ll2);
			}
		}

		return subdividedLatLons;
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
	
	public DateTime getDateTime()
	{
		return dateTime;
	}
	
	public short getPolygonTypeFlag()
	{
		return polygon_type_flag;
	}
	
	public double[] getSpectrum()
	{
		return spectrum;
	}

	public double[] getSpectrumErrors()
	{
		return spectrumErros;
	}
	
	public double[] getBandCenters()
	{
		return bandCenters;
	}
	
    public HashMap<String, String> getProperties() throws IOException
    {
    	HashMap<String, String> properties = new HashMap<String, String>();
    	
		properties.put("DAY_OF_YEAR", (new File(this.fullpath)).getParentFile().getParentFile().getName());
		
		properties.put("YEAR", (new File(this.fullpath)).getParentFile().getParentFile().getParentFile().getName());
		
		//properties.put("MET", (new File(this.fullpath)).getName());
		
		properties.put("DURATION", Double.toString(duration));
		
		properties.put("Date", dateTime.toString());
		
		String polygonTypeStr = "Missing value";
		switch(this.polygon_type_flag)
		{
		case 0:
			polygonTypeStr = "Full (all vertices on shape)";
			break;
		case 1:
			polygonTypeStr = "Partial (single contiguous set of vertices on shape)";
			break;
		case 2:
			polygonTypeStr = "Degenerate (multiple contiguous sets of vertices on shape)";
			break;
		case 3:
			polygonTypeStr = "Empty (no vertices on shape)";
			break;
		}
		properties.put("POLYGON_TYPE_FLAG", polygonTypeStr);
    	
		
		return properties;
    }


}
