package edu.jhuapl.near.model;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.*;

import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

/**
 * This class represents an image of the NEAR MSI instrument. It allows 
 * retrieving the image itself as well as the associated IMG file.
 * 
 * @author kahneg1
 *
 */
public class MSIImage extends Model
{
	
	public static final int IMAGE_WIDTH = 537;
	public static final int IMAGE_HEIGHT = 412;
    public static final float PDS_NA = -1.e32f;
	//public static final int NUM_LAYERS = 14;
    //public static final int TEXTURE_SIZE = 128;
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    //public static final String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    //public static final String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String MSI_FRUSTUM1 = "MSI_FRUSTUM1";
    public static final String MSI_FRUSTUM2 = "MSI_FRUSTUM2";
    public static final String MSI_FRUSTUM3 = "MSI_FRUSTUM3";
    public static final String MSI_FRUSTUM4 = "MSI_FRUSTUM4";
    public static final String SUN_POSITION_LT = "SUN_POSITION_LT";

    private ErosModel erosModel;

	private vtkImageData rawImage;
	private vtkImageData displayedImage;

	private vtkPolyData footprint;
    private vtkActor footprintActor;
    private ArrayList<vtkProp> footprintActors = new ArrayList<vtkProp>();

    private vtkPolyDataNormals normalsFilter;

    private double minIncidence = Double.MAX_VALUE;
    private double maxIncidence = -Double.MAX_VALUE;
    private double minEmission = Double.MAX_VALUE;
    private double maxEmission = -Double.MAX_VALUE;
    private double minPhase = Double.MAX_VALUE;
    private double maxPhase = -Double.MAX_VALUE;
    private double minHorizontalPixelScale = Double.MAX_VALUE;
    private double maxHorizontalPixelScale = -Double.MAX_VALUE;
    private double minVerticalPixelScale = Double.MAX_VALUE;
    private double maxVerticalPixelScale = -Double.MAX_VALUE;

	private BoundingBox boundingBox = new BoundingBox();
	private String name = ""; // The name is a 9 digit number at the beginning of the filename
						 	  // (starting after the initial M0). This is how the lineament 
							  // model names them.

	private float minValue;
	private float maxValue;
	private IntensityRange displayedRange = new IntensityRange(1,0);

	private String fullpath; // The actual path of the image stored on the local disk (after downloading from the server)
	private String serverpath; // The path of the image as passed into the constructor. This is not the 
							   // same as fullpath but instead corresponds to the name needed to download
							   // the file from the server (excluding the hostname).
	
	private int filter; // 1 through 7
	//private double polygonOffset = -10.0;

    private double[] spacecraftPosition = new double[3];
    private double[] frustum1 = new double[3];
    private double[] frustum2 = new double[3];
    private double[] frustum3 = new double[3];
    private double[] frustum4 = new double[3];
	private boolean hasLimb = false;
    private double[] sunPosition = new double[3];

	private boolean showFrustum = false;

	private String startTime = "";
	private String stopTime = "";
	
	/**
	 * Because instances of MSIImage can be expensive, we want there to be
	 * no more than one instance of this class per image file on the server.
	 * Hence this class was created to manage the creation and deletion of
	 * MSIImage's. Anyone needing a MSIImage should use this factory class to
	 * create MSIImages's and should NOT call the constructor directly.
	 */
	public static class MSIImageFactory
	{
		static private WeakHashMap<MSIImage, Object> images = 
			new WeakHashMap<MSIImage, Object>();
		
		static public MSIImage createImage(String name, ErosModel eros) throws FitsException, IOException
		{
			for (MSIImage image : images.keySet())
			{
				if (image.getServerPath().equals(name))
					return image;
			}

			MSIImage image = new MSIImage(name, eros);
			images.put(image, null);
			return image;
		}
	}

	
	/**
	 * This constructor should only be used in the GUI program since
	 * this constructor makes sure the relevant files get downloaded
	 * from the server.
	 * @param filename name of fit file
	 * @throws FitsException
	 * @throws IOException
	 */
	public MSIImage(String filename, ErosModel eros) throws FitsException, IOException
	{
		this.serverpath = filename;
		
		// Download the image, and all the companion files if necessary.
		File fitFile = FileCache.getFileFromServer(filename);

		if (fitFile == null)
			throw new IOException("Could not download " + filename);
		
		String imgLblFilename = filename.substring(0, filename.length()-4) + "_DDR.LBL";
		FileCache.getFileFromServer(imgLblFilename);
		String footprintFilename = filename.substring(0, filename.length()-4) + "_FOOTPRINT.VTK";
		FileCache.getFileFromServer(footprintFilename);

		this.erosModel = eros;
		this.initialize(fitFile);
	}
	
	/**
	 * This constructor should only be used in the database/server generation/processing
	 * where there is no need to download files from the server (since we're running on
	 * server)
	 * @param fitFile
	 * @throws FitsException
	 * @throws IOException
	 */
	public MSIImage(File fitFile, ErosModel eros) throws FitsException, IOException
	{
		this.erosModel = eros;
		this.initialize(fitFile);
	}
	
	private void initialize(File fitFile) throws FitsException, IOException
	{
		String filename = fitFile.getAbsolutePath();
		this.fullpath = filename;

		name = fitFile.getName().substring(2, 11);
		
		// Set the filter name
		filter = Integer.parseInt(fitFile.getName().substring(12,13));
		
        Fits f = new Fits(filename);
        BasicHDU h = f.getHDU(0);

        float[][] array = null;

        int[] axes = h.getAxes();
        int originalWidth = axes[1];
        int originalHeight = axes[0];

        //HashMap<Short, Integer> values = new HashMap<Short, Integer>();

        // For now only support images with type float or short since that what MSI images
        // seem to be in.
        try
        {
        	array = (float[][])h.getData().getData();
        }
        catch (ClassCastException e)
        {
        	short[][] arrayS = (short[][])h.getData().getData();
        	array = new float[originalHeight][originalWidth];
            
        	for (int i=0; i<originalHeight; ++i)
                for (int j=0; j<originalWidth; ++j)
                {
                	//values.put(arrayS[i][j], 0);
                	array[i][j] = arrayS[i][j];
                }
        }

        rawImage = new vtkImageData();
        rawImage.SetScalarTypeToFloat();
        rawImage.SetDimensions(originalWidth, originalHeight, 1);
        rawImage.SetSpacing(1.0, 1.0, 1.0);
        rawImage.SetOrigin(0.0, 0.0, 0.0);
        rawImage.SetNumberOfScalarComponents(1);
        
        maxValue = -Float.MAX_VALUE;
        minValue = Float.MAX_VALUE;
        for (int i=0; i<originalHeight; ++i)
            for (int j=0; j<originalWidth; ++j)
            {
        		rawImage.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 0, array[i][j]);

        		if (array[i][j] > maxValue)
            		maxValue = array[i][j];
            	if (array[i][j] < minValue)
            		minValue = array[i][j];
            }
        
        // Now scale this image
        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(rawImage);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, IMAGE_WIDTH-1, 0, IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();
        
        rawImage.DeepCopy(reslice.GetOutput());
        rawImage.SetSpacing(1, 1, 1);
        
        setDisplayedImageRange(new IntensityRange(0, 255));

        loadImageInfo();
	}
	
    
	
	public vtkImageData getRawImage()
	{
		return rawImage;
	}
	
	public vtkImageData getDisplayedImage()
	{
		return displayedImage;
	}
	
	public int getFilter()
	{
		return filter;
	}

	private vtkPolyData loadFootprint()
	{
		String footprintFilename = fullpath.substring(0, fullpath.length()-4) + "_FOOTPRINT.VTK";
		
		File file = new File(footprintFilename);
		
		if (!file.exists())
		{
			System.out.println("Warning: " + footprintFilename + " not found");
			return null;
		}

		vtkPolyDataReader footprintReader = new vtkPolyDataReader();
        footprintReader.SetFileName(file.getAbsolutePath());
        footprintReader.Update();
        
        vtkPolyData polyData = new vtkPolyData();
		polyData.DeepCopy(footprintReader.GetOutput());
		
		return polyData;
		

		/*
        // for testing
        vtkPolyData footprint2 = erosModel.computeFrustumIntersection(spacecraftPosition, 
				frustum1, frustum3, frustum4, frustum2);
        
        //vtkPolyDataWriter writer = new vtkPolyDataWriter();
        //writer.SetInput(footprint2);
        //writer.SetFileName("/tmp/footprint.vtk");
        ////writer.SetFileTypeToBinary();
        //writer.Write();

        vtkPolyData polyData = new vtkPolyData();
		polyData.DeepCopy(footprint2);

        return polyData;
        */
	}


	public ArrayList<vtkProp> getProps()
	{
		if (footprintActor == null)
		{
			if (footprint == null)
			{
				footprint = loadFootprint();
			}

			if (footprint != null)
			{
                int numberOfPoints = footprint.GetNumberOfPoints();

				vtkFloatArray tcoords = new vtkFloatArray();
				tcoords.SetNumberOfComponents(2);
				tcoords.SetNumberOfTuples(numberOfPoints);

				vtkPoints points = footprint.GetPoints();
				

				double a = GeometryUtil.vsep(frustum1, frustum3);
				double b = GeometryUtil.vsep(frustum1, frustum2);

				double[] vec = new double[3];

				for (int i=0; i<numberOfPoints; ++i)
				{
					double[] pt = points.GetPoint(i);

					vec[0] = pt[0] - spacecraftPosition[0];
					vec[1] = pt[1] - spacecraftPosition[1];
					vec[2] = pt[2] - spacecraftPosition[2];
					GeometryUtil.vhat(vec, vec);

					double d1 = GeometryUtil.vsep(vec, frustum1);
					double d2 = GeometryUtil.vsep(vec, frustum2);

					double v = (d1*d1 + b*b - d2*d2) / (2.0*b);
					double u = d1*d1 - v*v;
					if (u <= 0.0)
						u = 0.0;
					else
						u = Math.sqrt(u);

					//System.out.println(v/b + " " + u/a + " " + d1 + " " + d2);
					
					v = v/b;
					u = u/a;
					
					if (v < 0.0) v = 0.0;
					if (v > 1.0) v = 1.0;
					if (u < 0.0) u = 0.0;
					if (u > 1.0) u = 1.0;
					
					tcoords.SetTuple2(i, v, u);
				}


				// Now map the data to 
				footprint.GetPointData().SetTCoords(tcoords);

				PolyDataUtil.shiftPolyDataInNormalDirection(footprint, 0.002);

				vtkTexture texture = new vtkTexture();
				texture.InterpolateOn();
				texture.RepeatOff();
				texture.EdgeClampOn();
				texture.SetInput(displayedImage);

				vtkPolyDataMapper footprintMapper = new vtkPolyDataMapper();
				footprintMapper.SetInput(footprint);
				footprintMapper.Update();

				footprintActor = new vtkActor();
				footprintActor.SetMapper(footprintMapper);
				footprintActor.SetTexture(texture);
				footprintActor.GetProperty().LightingOff();

				footprintActors.add(footprintActor);
			}
		}

		if (showFrustum)
		{
			vtkPolyData frus = new vtkPolyData();

			vtkPoints points = new vtkPoints();
			vtkCellArray lines = new vtkCellArray();

			vtkIdList idList = new vtkIdList();
			idList.SetNumberOfIds(2);

			double dx = GeometryUtil.vnorm(spacecraftPosition)*2.0;
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

		return footprintActors;
	}
    

	public BoundingBox getBoundingBox()
	{
		return boundingBox;
	}
	
	public String getName()
	{
		return name;
	}

	public String getFullPath()
	{
		return fullpath;
	}
	
	public String getServerPath()
	{
		return serverpath;
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
	
	public IntensityRange getDisplayedRange()
	{
		return displayedRange;
	}
	
	public void setDisplayedImageRange(IntensityRange range)
	{
		if (displayedRange.min != range.min ||
				displayedRange.max != range.max)
		{
			displayedRange = range;

			float dx = (maxValue-minValue)/255.0f;
			float min = minValue + range.min*dx;
			float max = minValue + range.max*dx;

			// Update the displayed image
			vtkLookupTable lut = new vtkLookupTable();
			lut.SetTableRange(min, max);
			lut.SetValueRange(0.0, 1.0);
			lut.SetHueRange(0.0, 0.0);
			lut.SetSaturationRange(0.0, 0.0);
			//lut.SetNumberOfTableValues(402);
			lut.SetRampToLinear();
			lut.Build();

			vtkImageMapToColors mapToColors = new vtkImageMapToColors();
			mapToColors.SetInput(rawImage);
			mapToColors.SetOutputFormatToRGB();
			mapToColors.SetLookupTable(lut);
			mapToColors.Update();

			if (displayedImage == null)
				displayedImage = new vtkImageData();
			displayedImage.DeepCopy(mapToColors.GetOutput());

			//vtkPNGWriter writer = new vtkPNGWriter();
			//writer.SetFileName("fit.png");
			//writer.SetInput(displayedImage);
			//writer.Write();

			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	/*
	// This function is taken from http://www.java2s.com/Code/Java/Language-Basics/Utilityforbyteswappingofalljavadatatypes.htm
	private static int swap(int value)
	{
		int b1 = (value >>  0) & 0xff;
	    int b2 = (value >>  8) & 0xff;
	    int b3 = (value >> 16) & 0xff;
	    int b4 = (value >> 24) & 0xff;

	    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	}
	
	// This function is taken from http://www.java2s.com/Code/Java/Language-Basics/Utilityforbyteswappingofalljavadatatypes.htm
	private static float swap(float value)
	{
		int intValue = Float.floatToRawIntBits(value);
		intValue = swap(intValue);
		return Float.intBitsToFloat(intValue);
	}
	*/
	
	private void loadImageInfo() throws NumberFormatException, IOException
	{
		String lblFilename = fullpath.substring(0, fullpath.length()-4) + "_DDR.LBL";

    	FileInputStream fs = null;
		try {
			fs = new FileInputStream(lblFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(fs);
		BufferedReader in = new BufferedReader(isr);

		String str;
		while ((str = in.readLine()) != null)
		{
		    StringTokenizer st = new StringTokenizer(str);
		    while (st.hasMoreTokens()) 
		    {
		    	String token = st.nextToken();
		    	if (START_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		startTime = st.nextToken();
		    	}
		    	if (STOP_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		stopTime = st.nextToken();
		    	}
		    	if (SPACECRAFT_POSITION.equals(token) ||
		    			MSI_FRUSTUM1.equals(token) ||
		    			MSI_FRUSTUM2.equals(token) ||
		    			MSI_FRUSTUM3.equals(token) ||
		    			MSI_FRUSTUM4.equals(token) ||
		    			SUN_POSITION_LT.equals(token))
		    	{
		    		st.nextToken();
		    		st.nextToken();
		    		double x = Double.parseDouble(st.nextToken());
		    		st.nextToken();
		    		double y = Double.parseDouble(st.nextToken());
		    		st.nextToken();
		    		double z = Double.parseDouble(st.nextToken());
		    		if (SPACECRAFT_POSITION.equals(token))
		    		{
		    			spacecraftPosition[0] = x;
		    			spacecraftPosition[1] = y;
		    			spacecraftPosition[2] = z;
		    		}
		    		else if (MSI_FRUSTUM1.equals(token))
		    		{
		    			frustum1[0] = x;
		    			frustum1[1] = y;
		    			frustum1[2] = z;
		    			GeometryUtil.vhat(frustum1, frustum1);
		    		}
		    		else if (MSI_FRUSTUM2.equals(token))
		    		{
		    			frustum2[0] = x;
		    			frustum2[1] = y;
		    			frustum2[2] = z;
		    			GeometryUtil.vhat(frustum2, frustum2);
		    		}
		    		else if (MSI_FRUSTUM3.equals(token))
		    		{
		    			frustum3[0] = x;
		    			frustum3[1] = y;
		    			frustum3[2] = z;
		    			GeometryUtil.vhat(frustum3, frustum3);
		    		}
		    		else if (MSI_FRUSTUM4.equals(token))
		    		{
		    			frustum4[0] = x;
		    			frustum4[1] = y;
		    			frustum4[2] = z;
		    			GeometryUtil.vhat(frustum4, frustum4);
		    		}
		    		if (SUN_POSITION_LT.equals(token))
		    		{
		    			sunPosition[0] = x;
		    			sunPosition[1] = y;
		    			sunPosition[2] = z;
		    		}
		    	}
		    }
		}

		in.close();
	}

    
    public HashMap<String, String> getProperties() throws IOException
    {
    	HashMap<String, String> properties = new HashMap<String, String>();
    	
    	if (maxPhase < minPhase)
    	{
    		this.computeIlluminationAngles();
    		this.computePixelScale();
    	}

    	DecimalFormat df = new DecimalFormat("#.######");

    	properties.put("Spacecraft Distance", df.format(getSpacecraftDistance()) + " km");
    	properties.put("Name", name);
    	properties.put("Start Time", startTime);
		properties.put("Stop Time", stopTime);
		properties.put("Filter", String.valueOf(filter));
		properties.put("Day of Year", (new File(this.fullpath)).getParentFile().getParentFile().getName());
		properties.put("Year", (new File(this.fullpath)).getParentFile().getParentFile().getParentFile().getName());
		properties.put("Deblur Type", (new File(this.fullpath)).getParentFile().getName());
		
		// Note \u00B0 is the unicode degree symbol
		String deg = "\u00B0";
		properties.put("Minimum Incidence", df.format(minIncidence)+deg);
		properties.put("Maximum Incidence", df.format(maxIncidence)+deg);
		properties.put("Minimum Emission", df.format(minEmission)+deg);
		properties.put("Maximum Emission", df.format(maxIncidence)+deg);
		properties.put("Minimum Phase", df.format(minPhase)+deg);
		properties.put("Maximum Phase", df.format(maxPhase)+deg);
		properties.put("Minimum Horizontal Pixel Scale", df.format(1000.0*minHorizontalPixelScale) + " meters/pixel");
		properties.put("Maximum Horizontal Pixel Scale", df.format(1000.0*maxHorizontalPixelScale) + " meters/pixel");
		properties.put("Minimum Vertical Pixel Scale", df.format(1000.0*minVerticalPixelScale) + " meters/pixel");
		properties.put("Maximum Vertical Pixel Scale", df.format(1000.0*maxVerticalPixelScale) + " meters/pixel");

    	return properties;
    }

    public boolean containsLimb()
    {
    	return hasLimb;
    }

	public vtkPolyData generateFootprint()
	{
		return erosModel.computeFrustumIntersection(spacecraftPosition, 
				frustum1, frustum3, frustum4, frustum2);
	}
	
	public vtkPolyData generateBoundary()
	{
		vtkPolyData polyData = loadFootprint();

		vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
        edgeExtracter.SetInput(polyData);
        edgeExtracter.BoundaryEdgesOn();
        edgeExtracter.FeatureEdgesOff();
        edgeExtracter.NonManifoldEdgesOff();
        edgeExtracter.ManifoldEdgesOff();
        edgeExtracter.Update();
        
        polyData.DeepCopy(edgeExtracter.GetOutput());

		return polyData;
	}

	public String getStartTime()
	{
		return startTime;
	}

	public String getStopTime()
	{
		return stopTime;
	}
	
	public double getMinimumHorizontalPixelScale()
	{
		return minHorizontalPixelScale;
	}
	
	public double getMaximumHorizontalPixelScale()
	{
		return maxHorizontalPixelScale;
	}
	
	public double getMinimumVerticalPixelScale()
	{
		return minVerticalPixelScale;
	}
	
	public double getMaximumVerticalPixelScale()
	{
		return maxVerticalPixelScale;
	}
	
	public double getSpacecraftDistance()
	{
		return GeometryUtil.vnorm(spacecraftPosition);
 	}
	
	private void computeCellNormals()
	{
		if (normalsFilter == null)
		{
			normalsFilter = new vtkPolyDataNormals();
			normalsFilter.SetInput(footprint);
			normalsFilter.SetComputeCellNormals(1);
			normalsFilter.SetComputePointNormals(0);
			normalsFilter.Update();

			footprint.DeepCopy(normalsFilter.GetOutput());
		}
	}
	
	// Computes the incidence, emission, and phase at a point with a given normal.
	// The output is a 3-vector with the first component equal to the incidence,
	// the second component equal to the emission and the third component equal to
	// the phase.
	private double[] computeIlluminationAnglesAtPoint(
			double[] pt,
			double[] normal)
	{
		double[] scvec = {
			spacecraftPosition[0] - pt[0],
			spacecraftPosition[1] - pt[1],
			spacecraftPosition[2] - pt[2]};
		                                      
		
		double[] sunvec = {
				sunPosition[0] - pt[0],
				sunPosition[1] - pt[1],
				sunPosition[2] - pt[2]};
		
		double incidence = GeometryUtil.vsep(normal, sunvec) * 180.0 / Math.PI;
		double emission = GeometryUtil.vsep(normal, scvec) * 180.0 / Math.PI;
		double phase = GeometryUtil.vsep(sunvec, scvec) * 180.0 / Math.PI;

		double[] angles = {incidence, emission, phase};
		
		return angles;
	}
	
	private void computeIlluminationAngles()
	{
		if (footprint == null)
			footprint = loadFootprint();

		computeCellNormals();
		
        int numberOfCells = footprint.GetNumberOfCells();

		vtkPoints points = footprint.GetPoints();
		vtkDataArray normals = footprint.GetCellData().GetNormals();

		this.minEmission  =  Double.MAX_VALUE;
		this.maxEmission  = -Double.MAX_VALUE;
		this.minIncidence =  Double.MAX_VALUE;
		this.maxIncidence = -Double.MAX_VALUE;
		this.minPhase     =  Double.MAX_VALUE;
		this.maxPhase     = -Double.MAX_VALUE;

		double[] scvec = new double[3];
		double[] sunvec = new double[3];

		for (int i=0; i<numberOfCells; ++i)
		{
			vtkCell cell = footprint.GetCell(i);
    		double[] pt0 = points.GetPoint( cell.GetPointId(0) );
    		double[] pt1 = points.GetPoint( cell.GetPointId(1) );
    		double[] pt2 = points.GetPoint( cell.GetPointId(2) );
    		double[] centroid = {
    				(pt0[0] + pt1[0] + pt2[0]) / 3.0,
    				(pt0[1] + pt1[1] + pt2[1]) / 3.0,
    				(pt0[2] + pt1[2] + pt2[2]) / 3.0
    		};
    		double[] normal = normals.GetTuple3(i);
    		
			double[] angles = computeIlluminationAnglesAtPoint(centroid, normal);
			double incidence = angles[0];
			double emission  = angles[1];
			double phase     = angles[2];
			
			if (incidence < minIncidence)
				minIncidence = incidence;
			if (incidence > maxIncidence)
				maxIncidence = incidence;
			if (emission < minEmission)
				minEmission = emission;
			if (emission > maxEmission)
				maxEmission = emission;
			if (phase < minPhase)
				minPhase = phase;
			if (phase > maxPhase)
				maxPhase = phase;
		}		
	}
	
	private void computePixelScale()
	{
		if (footprint == null)
			footprint = loadFootprint();

		int numberOfPoints = footprint.GetNumberOfPoints();

		vtkPoints points = footprint.GetPoints();
		
		minHorizontalPixelScale = Double.MAX_VALUE;
	    maxHorizontalPixelScale = -Double.MAX_VALUE;
	    minVerticalPixelScale = Double.MAX_VALUE;
	    maxVerticalPixelScale = -Double.MAX_VALUE;

		double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
		double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

	    double[] vec = new double[3];

		for (int i=0; i<numberOfPoints; ++i)
		{
			double[] pt = points.GetPoint(i);

			vec[0] = pt[0] - spacecraftPosition[0];
			vec[1] = pt[1] - spacecraftPosition[1];
			vec[2] = pt[2] - spacecraftPosition[2];
			double dist = GeometryUtil.vnorm(vec);

			double horizPixelScale = dist * horizScaleFactor;
			double vertPixelScale = dist * vertScaleFactor;

			if (horizPixelScale < minHorizontalPixelScale)
				minHorizontalPixelScale = horizPixelScale;
			if (horizPixelScale > maxHorizontalPixelScale)
				maxHorizontalPixelScale = horizPixelScale;
			if (vertPixelScale < minVerticalPixelScale)
				minVerticalPixelScale = vertPixelScale;
			if (vertPixelScale > maxVerticalPixelScale)
				maxVerticalPixelScale = vertPixelScale;
		}
	}
	
	public float[] generateBackplanes()
	{
		if (footprint == null)
			footprint = loadFootprint();

		computeCellNormals();
		
		int numLayers = 11;
		float[] data = new float[numLayers*IMAGE_HEIGHT*IMAGE_WIDTH];

		vtkOBBTree cellLocator = new vtkOBBTree();
        cellLocator.SetDataSet(footprint);
        cellLocator.CacheCellBoundsOn();
        cellLocator.AutomaticOn();
        //cellLocator.SetMaxLevel(10);
        //cellLocator.SetNumberOfCellsPerNode(15);
        cellLocator.BuildLocator();

		long t1 = System.currentTimeMillis();

        vtkPoints intersectPoints = new vtkPoints();
        vtkIdList intersectCells = new vtkIdList();
        
		// For each pixel in the image we need to compute the vector
		// from the spacecraft pointing in the direction of that pixel.
		// To do this, for each row in the image compute the left and
		// right vectors of the entire row. Then for each pixel in 
		// the row use the two vectors from either side to compute
		// the vector of that pixel.
		double[] corner1 = {
				spacecraftPosition[0] + frustum1[0],
				spacecraftPosition[1] + frustum1[1],
				spacecraftPosition[2] + frustum1[2]
		};
		double[] corner2 = {
				spacecraftPosition[0] + frustum2[0],
				spacecraftPosition[1] + frustum2[1],
				spacecraftPosition[2] + frustum2[2]
		};
		double[] corner3 = {
				spacecraftPosition[0] + frustum3[0],
				spacecraftPosition[1] + frustum3[1],
				spacecraftPosition[2] + frustum3[2]
		};
		double[] vec12 = {
				corner2[0] - corner1[0],
				corner2[1] - corner1[1],
				corner2[2] - corner1[2]
		};
		double[] vec13 = {
				corner3[0] - corner1[0],
				corner3[1] - corner1[1],
				corner3[2] - corner1[2]
		};
		
		double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
		double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

		double scdist = GeometryUtil.vnorm(spacecraftPosition);

		vtkDataArray normals = footprint.GetCellData().GetNormals();
		
		for (int i=0; i<IMAGE_HEIGHT; ++i)
		{
			// Compute the vector on the left of the row.
			double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
			double[] left = {
					corner1[0] + fracHeight*vec13[0],
					corner1[1] + fracHeight*vec13[1],
					corner1[2] + fracHeight*vec13[2]
			};

			for (int j=0; j<IMAGE_WIDTH; ++j)
			{
				double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
				double[] vec = {
						left[0] + fracWidth*vec12[0],
						left[1] + fracWidth*vec12[1],
						left[2] + fracWidth*vec12[2]
				};
				vec[0] -= spacecraftPosition[0];
				vec[1] -= spacecraftPosition[1];
				vec[2] -= spacecraftPosition[2];
				GeometryUtil.unorm(vec, vec);

				double[] lookPt = {
						spacecraftPosition[0] + 2.0*scdist*vec[0],	
						spacecraftPosition[1] + 2.0*scdist*vec[1],	
						spacecraftPosition[2] + 2.0*scdist*vec[2]
				};

				cellLocator.IntersectWithLine(spacecraftPosition, lookPt, intersectPoints, intersectCells);
				//if (intersectPoints_f4.GetNumberOfPoints() == 0)
				//	System.out.println(i + " " + j + " " + intersectPoints_f4.GetNumberOfPoints());

				int numberOfPoints = intersectPoints.GetNumberOfPoints();

				if (numberOfPoints > 0)
				{
					double[] closestPoint = intersectPoints.GetPoint(0);
					int closestCell = intersectCells.GetId(0);
					double closestDist = GeometryUtil.distanceBetween(closestPoint, spacecraftPosition);
					
					// compute the closest point to the spacecraft of all the intersecting points.
					if (numberOfPoints > 1)
					{
						for (int k=1; k<numberOfPoints; ++k)
						{
							double[] pt = intersectPoints.GetPoint(k);
							double dist = GeometryUtil.distanceBetween(pt, spacecraftPosition);
							if (dist < closestDist)
							{
								closestDist = dist;
								closestCell = intersectCells.GetId(k);
								closestPoint = pt;
							}
						}
					}

					LatLon llr = GeometryUtil.reclat(closestPoint);
					double lon = llr.lon*180/Math.PI;
					if (lon < 0.0)
						lon += 360.0;

					double[] normal = normals.GetTuple3(closestCell);
					double[] illumAngles = computeIlluminationAnglesAtPoint(closestPoint, normal);

					double horizPixelScale = closestDist * horizScaleFactor;
					double vertPixelScale = closestDist * vertScaleFactor;

					data[index(j,i,0)] = (float)closestPoint[0];
					data[index(j,i,1)] = (float)closestPoint[1];
					data[index(j,i,2)] = (float)closestPoint[2];
					data[index(j,i,3)] = (float)(llr.lat * 180.0 / Math.PI);
					data[index(j,i,4)] = (float)(lon);
					data[index(j,i,5)] = (float)(llr.rad);
					data[index(j,i,6)] = (float)(illumAngles[0] * 180.0 / Math.PI);
					data[index(j,i,7)] = (float)(illumAngles[1] * 180.0 / Math.PI);
					data[index(j,i,8)] = (float)(illumAngles[2] * 180.0 / Math.PI);
					data[index(j,i,9)] = (float)(horizPixelScale);
					data[index(j,i,10)] = (float)(vertPixelScale);
				}
				else
				{
					for (int k=1; k<numLayers; ++k)
						data[index(j,i,k)] = PDS_NA;
				}
			}
			
			this.pcs.firePropertyChange(Properties.MSI_IMAGE_BACKPLANE_GENERATION_UPDATE, -1, i);
		}

		System.out.println((System.currentTimeMillis() - t1)/1000.0);
		return data;
	}
	
	private int index(int i, int j, int k)
	{
		return ((k * IMAGE_HEIGHT + j) * IMAGE_WIDTH + i);
	}
}
