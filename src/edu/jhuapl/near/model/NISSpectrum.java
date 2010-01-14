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
	static public final int SPACECRAFT_POSITION_OFFSET = 224+2;
	static public final int FRUSTUM_OFFSET = 230+2;
	static public final int INCIDENCE_OFFSET = 242+2;
	static public final int EMISSION_OFFSET = 245+2;
	static public final int PHASE_OFFSET = 248+2;
	static public final int RANGE_OFFSET = 252+2;
	static public final int POLYGON_TYPE_FLAG_OFFSET = 258+2;
	static public final int NUMBER_OF_VERTICES_OFFSET = 259+2;
	static public final int POLYGON_START_COORDINATES_OFFSET = 260+2;
	
	static private vtkMath math = null;
	
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
    private double[] spectrumEros = new double[64];
    private double[] spacecraftPosition = new double[3];
    private double[] frustum1 = new double[3];
    private double[] frustum2 = new double[3];
    private double[] frustum3 = new double[3];
    private double[] frustum4 = new double[3];
    private double minIncidence; 
    private double maxIncidence; 
    private double minEmission; 
    private double maxEmission; 
    private double minPhase; 
    private double maxPhase; 
    private boolean showFrustum = false;
    static private int channelToColorBy = 0;
    static private double channelColoringMinValue= 0.0;
    static private double channelColoringMaxValue = 0.05;
    
    // These values were taken from Table 1 of "Spectral properties and geologic
    // processes on Eros from combined NEAR NIS and MSI data sets" 
    // by Noam Izenberg et. al.
    static final public double[] bandCenters = {
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

		if (math == null)
			math = new vtkMath();

		ArrayList<String> values = FileUtil.getFileWordsAsStringList(fullpath);

		dateTime = new DateTime(values.get(DATE_TIME_OFFSET), DateTimeZone.UTC);

		double metOffsetToMiddle = Double.parseDouble(values.get(MET_OFFSET_TO_MIDDLE_OFFSET));
		dateTime = dateTime.plusMillis((int)metOffsetToMiddle);
		
		duration = Double.parseDouble(values.get(DURATION_OFFSET));
		minIncidence = Double.parseDouble(values.get(INCIDENCE_OFFSET+1));
		maxIncidence = Double.parseDouble(values.get(INCIDENCE_OFFSET+2));
		minEmission= Double.parseDouble(values.get(EMISSION_OFFSET+1));
		maxEmission = Double.parseDouble(values.get(EMISSION_OFFSET+2));
		minPhase = Double.parseDouble(values.get(PHASE_OFFSET+1));
		maxPhase= Double.parseDouble(values.get(PHASE_OFFSET+2));
		range = Double.parseDouble(values.get(RANGE_OFFSET));
		polygon_type_flag = Short.parseShort(values.get(POLYGON_TYPE_FLAG_OFFSET));
		
		int footprintSize = Integer.parseInt(values.get(NUMBER_OF_VERTICES_OFFSET));
		for (int i=0; i<footprintSize; ++i)
		{
			int latIdx = POLYGON_START_COORDINATES_OFFSET + i*2;
			int lonIdx = POLYGON_START_COORDINATES_OFFSET + i*2 + 1;
			
			latLons.add(new LatLon(Double.parseDouble(values.get(latIdx)) * Math.PI / 180.0,
								   (360.0-Double.parseDouble(values.get(lonIdx))) * Math.PI / 180.0));
		}
		
		for (int i=0; i<64; ++i)
		{
			// The following min and max clamps the value between 0 and 1.
			spectrum[i] = Math.min(1.0, Math.max(0.0, Double.parseDouble(values.get(CALIBRATED_GE_DATA_OFFSET + i))));
			spectrumEros[i] = Double.parseDouble(values.get(CALIBRATED_GE_NOISE_OFFSET + i));
		}

		for (int i=0; i<3; ++i)
			spacecraftPosition[i] = Double.parseDouble(values.get(SPACECRAFT_POSITION_OFFSET + i));
		for (int i=0; i<3; ++i)
			frustum1[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + i));
		for (int i=0; i<3; ++i)
			frustum2[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 3 + i));
		for (int i=0; i<3; ++i)
			frustum3[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 6 + i));
		for (int i=0; i<3; ++i)
			frustum4[i] = Double.parseDouble(values.get(FRUSTUM_OFFSET + 9 + i));
		math.Normalize(frustum1);
		math.Normalize(frustum2);
		math.Normalize(frustum3);
		math.Normalize(frustum4);
	}

	public vtkPolyData generateFootprint()
	{
		if (!latLons.isEmpty())
		{
			return erosModel.computeFrustumIntersection(spacecraftPosition, 
					frustum1, frustum2, frustum3, frustum4);
		}
		else
		{
			return null;
		}
	}
	
	private vtkPolyData loadFootprint()
	{
		String footprintFilename = serverpath.substring(0, serverpath.length()-4) + "_FOOTPRINT.VTK";
		File file = FileCache.getFileFromServer(footprintFilename);
		
		if (file == null)
		{
			return null;
		}

		vtkPolyDataReader footprintReader = new vtkPolyDataReader();
        footprintReader.SetFileName(file.getAbsolutePath());
        footprintReader.Update();
        
        return footprintReader.GetOutput();
	}

	public vtkPolyData getFootprint()
	{
		if (footprint == null)
			footprint = loadFootprint();

		return footprint;
	}
	
	public ArrayList<vtkActor> getActors() 
	{
		if (footprintActor == null && !latLons.isEmpty())
		{
			if (footprint == null)
				footprint = loadFootprint();
			
			if (footprint != null)
			{
				vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
				footprintMapper.SetInput(footprint);
				footprintMapper.SetResolveCoincidentTopologyToPolygonOffset();
				footprintMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-.002, -2.0);
				footprintMapper.Update();
				
				footprintActor = new vtkActor();
				footprintActor.SetMapper(footprintMapper);
				footprintActor.GetProperty().SetColor(
						getChannelColor(),
						getChannelColor(),
						getChannelColor());
				footprintActor.GetProperty().SetLineWidth(2.0);
				footprintActor.GetProperty().LightingOff();
				
				footprintActors.add(footprintActor);

				// Compute the bounding edges of this surface
		        vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
		        edgeExtracter.SetInput(footprint);
		        edgeExtracter.BoundaryEdgesOn();
		        edgeExtracter.FeatureEdgesOff();
		        edgeExtracter.NonManifoldEdgesOff();
		        edgeExtracter.ManifoldEdgesOff();
		        edgeExtracter.Update();

				vtkPolyDataMapper edgeMapper = new vtkPolyDataMapper();
				edgeMapper.SetInputConnection(edgeExtracter.GetOutputPort());
				edgeMapper.ScalarVisibilityOff();
				edgeMapper.SetResolveCoincidentTopologyToPolygonOffset();
				edgeMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-.004, -4.0);
				edgeMapper.Update();
				
				vtkActor edgeActor = new vtkActor();
				edgeActor.SetMapper(edgeMapper);
				edgeActor.GetProperty().SetColor(0.0, 0.39, 0.0);
				edgeActor.GetProperty().SetLineWidth(2.0);
				edgeActor.GetProperty().LightingOff();
				footprintActors.add(edgeActor);
			}
			
			if (showFrustum)
			{
				vtkPolyData frus = new vtkPolyData();
				
		        vtkPoints points = new vtkPoints();
		        vtkCellArray lines = new vtkCellArray();
		        
		        vtkIdList idList = new vtkIdList();
		        idList.SetNumberOfIds(2);
		        
		        double dx = math.Norm(spacecraftPosition);
				double[] origin = spacecraftPosition;
				double[] UL = {origin[0]+frustum1[0]*dx, origin[1]+frustum1[1]*dx, origin[2]+frustum1[2]*dx};
				double[] UR = {origin[0]+frustum2[0]*dx, origin[1]+frustum2[1]*dx, origin[2]+frustum2[2]*dx};
				double[] LL = {origin[0]+frustum3[0]*dx, origin[1]+frustum3[1]*dx, origin[2]+frustum3[2]*dx};
				double[] LR = {origin[0]+frustum4[0]*dx, origin[1]+frustum4[1]*dx, origin[2]+frustum4[2]*dx};

		        points.InsertNextPoint(spacecraftPosition);
		        points.InsertNextPoint(UL);
		        points.InsertNextPoint(UR);
		        points.InsertNextPoint(LL);
		        points.InsertNextPoint(LR);

		    	idList.SetId(0, 0);
		    	idList.SetId(1, 1);
		    	lines.InsertNextCell(idList);
		    	idList.SetId(0, 0);
		    	idList.SetId(1, 2);
		    	lines.InsertNextCell(idList);
		    	idList.SetId(0, 0);
		    	idList.SetId(1, 3);
		    	lines.InsertNextCell(idList);
		    	idList.SetId(0, 0);
		    	idList.SetId(1, 4);
		    	lines.InsertNextCell(idList);
		    	
		    	frus.SetPoints(points);
		        frus.SetLines(lines);


		        vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
				frusMapper.SetInput(frus);

				vtkActor frusActor = new vtkActor();
				frusActor.SetMapper(frusMapper);
		        frusActor.GetProperty().SetColor(0.0, 1.0, 0.0);
		        frusActor.GetProperty().SetLineWidth(2.0);

				footprintActors.add(frusActor);
			}
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
		return spectrumEros;
	}
	
	public double[] getBandCenters()
	{
		return bandCenters;
	}
	
    public HashMap<String, String> getProperties() throws IOException
    {
    	HashMap<String, String> properties = new HashMap<String, String>();
    	System.out.println(this.fullpath);
		properties.put("DAY_OF_YEAR", (new File(this.fullpath)).getParentFile().getName());
		
		//properties.put("YEAR", (new File(this.fullpath)).getParentFile().getParentFile().getName());
		
		properties.put("MET", (new File(this.fullpath)).getName().substring(2,11));
		
		properties.put("DURATION", Double.toString(duration) + " seconds");
		
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
    	
		// Note \u00B0 is the unicode degree symbol
		String deg = "\u00B0";
		properties.put("Minimum Incidence", Double.toString(minIncidence)+deg);
		properties.put("Maximum Incidence", Double.toString(maxIncidence)+deg);
		properties.put("Minimum Emission", Double.toString(minEmission)+deg);
		properties.put("Maximum Emission", Double.toString(maxIncidence)+deg);
		properties.put("Minimum Phase", Double.toString(minPhase)+deg);
		properties.put("Maximum Phase", Double.toString(maxPhase)+deg);
		
		return properties;
    }
    
    static public void setChannelColoring(int channel, double min, double max)
    {
    	channelToColorBy = channel;
    	channelColoringMinValue = min;
    	channelColoringMaxValue = max;
    }
    
    static public int getChannelToColorBy()
    {
    	return channelToColorBy;
    }

    static public double getChannelColoringMinValue()
    {
    	return channelColoringMinValue;
    }

    static public double getChannelColoringMaxValue()
    {
    	return channelColoringMaxValue;
    }

    public void updateChannelColoring()
    {
		footprintActor.GetProperty().SetColor(
				getChannelColor(),
				getChannelColor(),
				getChannelColor());
    }

	public double getMinIncidence() 
	{
		return minIncidence;
	}

	public double getMaxIncidence() 
	{
		return maxIncidence;
	}

	public double getMinEmission() 
	{
		return minEmission;
	}

	public double getMaxEmission() 
	{
		return maxEmission;
	}

	public double getMinPhase() 
	{
		return minPhase;
	}

	public double getMaxPhase() 
	{
		return maxPhase;
	}

	private double getChannelColor()
	{
		double val = spectrum[channelToColorBy];
		if (val < 0.0)
			val = 0.0;
		else if (val > 1.0)
			val = 1.0;
		
		double slope = 1.0 / (channelColoringMaxValue - channelColoringMinValue);
		return slope * (val - channelColoringMinValue);
	}
}
