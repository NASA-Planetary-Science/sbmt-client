package edu.jhuapl.near.model;

import java.io.*;
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
	public static final int NUM_LAYERS = 14;
    public static final int TEXTURE_SIZE = 128;
    //public static final float PDS_NA = -1.e32f;
    public static final String START_TIME = "START_TIME";
    public static final String STOP_TIME = "STOP_TIME";
    public static final String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    public static final String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";
    public static final String SPACECRAFT_POSITION = "SPACECRAFT_POSITION";
    public static final String MSI_FRUSTUM1 = "MSI_FRUSTUM1";
    public static final String MSI_FRUSTUM2 = "MSI_FRUSTUM2";
    public static final String MSI_FRUSTUM3 = "MSI_FRUSTUM3";
    public static final String MSI_FRUSTUM4 = "MSI_FRUSTUM4";
    
	private vtkImageData rawImage;
	private vtkImageData displayedImage;

	private vtkPolyData footprint;
    private vtkActor footprintActor;

	//private float[][] incidence = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] emission = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] phase = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] latitude = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] longitude = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] x = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] y = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] z = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	
//	private float[] data = new float[NUM_LAYERS*IMAGE_HEIGHT*IMAGE_WIDTH];

    private double minIncidence = Double.MAX_VALUE;
    private double maxIncidence = -Double.MAX_VALUE;
    private double minEmission = Double.MAX_VALUE;
    private double maxEmission = -Double.MAX_VALUE;
    private double minPhase = Double.MAX_VALUE;
    private double maxPhase = -Double.MAX_VALUE;

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
	boolean hasLimb = false;

	private boolean showFrustum = true;
	static private vtkMath math = null;

	
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
		
		static public MSIImage createImage(String name) throws FitsException, IOException
		{
			for (MSIImage image : images.keySet())
			{
				if (image.getServerPath().equals(name))
					return image;
			}

			MSIImage image = new MSIImage(name);
			images.put(image, null);
			return image;
		}
	}

	

	public MSIImage(String filename) throws FitsException, IOException
	{
		this.serverpath = filename;
		
		// Download the image, and all the companion files if necessary.
		File fitFile = FileCache.getFileFromServer(filename);

		if (fitFile == null)
			throw new IOException("Could not download " + filename);
		
		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
		FileCache.getFileFromServer(lblFilename);
		//String imgFilename = filename.substring(0, filename.length()-4) + "_DDR.IMG.gz";
		//FileCache.getFileFromServer(imgFilename);
		String imgLblFilename = filename.substring(0, filename.length()-4) + "_DDR.LBL";
		FileCache.getFileFromServer(imgLblFilename);
		String boundaryFilename = filename.substring(0, filename.length()-4) + "_BOUNDARY.VTK";
		FileCache.getFileFromServer(boundaryFilename);

		this.initialize(fitFile);
	}
	
	public MSIImage(File fitFile) throws FitsException, IOException
	{
		this.initialize(fitFile);
	}
	
	private void initialize(File fitFile) throws FitsException, IOException
	{
		String filename = fitFile.getAbsolutePath();
		this.fullpath = filename;

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

        //if (showFrustum)
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

	/*
	public vtkImageData getSubImage(int size, int row, int col)
	{
		vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(displayedImage);
        reslice.SetInterpolationModeToNearestNeighbor();
        reslice.SetOutputSpacing(1.0, 1.0, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(col, col+size-1, row, row+size-1, 0, 0);
        reslice.Update();

        return reslice.GetOutput();
	}
	*/

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
        
        vtkPolyData polyData = new vtkPolyData();
		polyData.DeepCopy(footprintReader.GetOutput());
		
		return polyData;
	}


	private static ErosModel erosModel;
	//public ArrayList<vtkActor> getMappedImage(double offset)
    public ArrayList<vtkProp> getProps()
    {
    	ArrayList<vtkProp> imageActors = new ArrayList<vtkProp>();
    	
    	if (footprintActor == null)
    	{
    		if (footprint == null)
    		{
    			footprint = loadFootprint();
    		}

    		if (footprint != null)
    		{
    	        if (math == null)
    	        	math = new vtkMath();

    			int numberOfPoints = footprint.GetNumberOfPoints();
    			
    			vtkFloatArray tcoords = new vtkFloatArray();
                tcoords.SetNumberOfComponents(2);
                tcoords.SetNumberOfTuples(numberOfPoints);
                
                vtkPoints points = footprint.GetPoints();

                if (erosModel == null)
                	erosModel = new ErosModel();
                vtkPolyData footprint2 = erosModel.computeFrustumIntersection(spacecraftPosition, 
    					frustum1, frustum3, frustum4, frustum2);
                vtkPolyDataWriter writer = new vtkPolyDataWriter();
                writer.SetInput(footprint2);
                writer.SetFileName("/tmp/footprint.vtk");
                //writer.SetFileTypeToBinary();
                writer.Write();

                /*
                // First compute the vector that bisects two opposite corners of
                // the frustum. I.e. the "boresight" direction that points in the
                // direction of the center of the frustum. The following assumes
                // the frustum vectors all have unit length.
                double[] boresight = {
                		(frustum1[0]+frustum3[0])/2.0,
                		(frustum1[1]+frustum3[1])/2.0,
                		(frustum1[2]+frustum3[2])/2.0
                };
                math.Normalize(boresight);

                // Next compute the rotation matrix that rotates the boresight 
                // to the z axis.
                double[] zaxis = {0.0, 0.0, 1.0};
                double[] axisOfRotation = new double[3];
                math.Cross(boresight, zaxis, axisOfRotation);
        		math.Normalize(axisOfRotation);

        		double angle = Spice.vsep(boresight, zaxis) * 180.0 / Math.PI;
        		vtkTransform transform = new vtkTransform();
        		//transform.Translate(center);
        		transform.RotateWXYZ(angle, axisOfRotation);
        		//transform.Translate(-center[0],-center[1],-center[2]);

        		System.out.println(math.Distance2BetweenPoints(frustum1, frustum3));

                double[] vec1 = {
                		frustum2[0] - frustum1[0],
                		frustum2[1] - frustum1[1],
                		frustum2[2] - frustum1[2]
                };
        		*/

        		double b = Spice.vsep(frustum1, frustum2);
        		double a = Spice.vsep(frustum1, frustum3);

        		double[] vec = new double[3];
        		
                for (int i=0; i<numberOfPoints; ++i)
                {
                	double[] pt = points.GetPoint(i);
                	
                	vec[0] = pt[0] - spacecraftPosition[0];
                	vec[1] = pt[1] - spacecraftPosition[1];
                	vec[2] = pt[2] - spacecraftPosition[2];
                	math.Normalize(vec);
                	
                	double d1 = Spice.vsep(vec, frustum1);
                	double d2 = Spice.vsep(vec, frustum2);
                	
                	double v = (d1*d1 + b*b - d2*d2) / (2*b);
                	double u = Math.sqrt(d1*d1 - v*v);
                	
                	//System.out.println(v/b + " " + u/a);
                	tcoords.SetTuple2(i, v/b, u/a);
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

    			imageActors.add(footprintActor);

    		}
    	}
		
		if (showFrustum)
		{
			vtkPolyData frus = new vtkPolyData();
			
			vtkPoints points = new vtkPoints();
	        vtkCellArray lines = new vtkCellArray();
	        
	        vtkIdList idList = new vtkIdList();
	        idList.SetNumberOfIds(2);
	        
	        if (math == null)
	        	math = new vtkMath();
	        
	        double dx = math.Norm(spacecraftPosition)*2;
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

	        imageActors.add(frusActor);
		}

        return imageActors;
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
	
	/*
	public float getLatitude(int row, int col)
	{
		return latitude[row][col];
	}
	
	public float getLongitude(int row, int col)
	{
		return longitude[row][col];
	}
	
	public float getPhaseAngle(int row, int col)
	{
		return 0;
	}

	public float getEmissionAngle(int row, int col)
	{
		return 0;
	}

	public float getIncidenceAngle(int row, int col)
	{
		return 0;
	}
	*/
	
	/*
	public float getX(int row, int col)
	{
		return x[row][col];
	}

	public float getY(int row, int col)
	{
		return y[row][col];
	}

	public float getZ(int row, int col)
	{
		return z[row][col];
	}
	*/
	
	
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

	//public void setPolygonOffset(double offset)
	//{
	//	this.polygonOffset = offset;
	//}
	
	/*
	public vtkPolyData getImageBorder()
	{
    	String filename = getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + "_BOUNDARY.vtk";

    	vtkPolyDataReader reader = new vtkPolyDataReader();
    	reader.SetFileName(lblFilename);
    	
	}
	*/

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
    	String filename = getFullPath();
    	
		String lblFilename = filename.substring(0, filename.length()-4) + "_DDR.LBL";

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
		    	if (SPACECRAFT_POSITION.equals(token) ||
		    			MSI_FRUSTUM1.equals(token) ||
		    			MSI_FRUSTUM2.equals(token) ||
		    			MSI_FRUSTUM3.equals(token) ||
		    			MSI_FRUSTUM4.equals(token))
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
		    		}
		    		else if (MSI_FRUSTUM2.equals(token))
		    		{
		    			frustum2[0] = x;
		    			frustum2[1] = y;
		    			frustum2[2] = z;
		    		}
		    		else if (MSI_FRUSTUM3.equals(token))
		    		{
		    			frustum3[0] = x;
		    			frustum3[1] = y;
		    			frustum3[2] = z;
		    		}
		    		else if (MSI_FRUSTUM4.equals(token))
		    		{
		    			frustum4[0] = x;
		    			frustum4[1] = y;
		    			frustum4[2] = z;
		    		}
		    	}
		    }
		}

		in.close();
	}

    static public HashMap<String, String> parseLblFile(String lblFilename) throws IOException
    {
    	HashMap<String, String> imageProperties = new HashMap<String, String>();
    	
    	// Parse through the lbl file till we find the relevant strings
    	
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
		    		imageProperties.put(token, st.nextToken());
		    	}
		    	if (STOP_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		imageProperties.put(token, st.nextToken());
		    	}
		    	if (TARGET_CENTER_DISTANCE.equals(token))
		    	{
		    		st.nextToken();
		    		imageProperties.put(token, st.nextToken());
		    	}
		    	if (HORIZONTAL_PIXEL_SCALE.equals(token))
		    	{
		    		st.nextToken();
		    		imageProperties.put(token, st.nextToken());
		    	}
		    	if (SPACECRAFT_POSITION.equals(token) ||
		    			MSI_FRUSTUM1.equals(token) ||
		    			MSI_FRUSTUM2.equals(token) ||
		    			MSI_FRUSTUM3.equals(token) ||
		    			MSI_FRUSTUM4.equals(token))
		    	{
		    		st.nextToken();
		    		st.nextToken();
		    		String x = st.nextToken();
		    		st.nextToken();
		    		String y = st.nextToken();
		    		st.nextToken();
		    		String z = st.nextToken();
		    		imageProperties.put(token, x + " " + y + " " + z);
		    	}
		    }
		}

		in.close();
		
    	return imageProperties;
    }
    
    public HashMap<String, String> getProperties() throws IOException
    {
    	String filename = getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";

    	HashMap<String, String> properties = parseLblFile(lblFilename);
    	
		// The values of the hash map do not include units. Add them for certain properties.
		for (String key : properties.keySet())
		{
			if (key.equals(HORIZONTAL_PIXEL_SCALE))
				properties.put(key, properties.get(key) + " km/pixel");
			else if (key.equals(TARGET_CENTER_DISTANCE))
				properties.put(key, properties.get(key) + " km");
		}
		
		// Add a few more properties
		properties.put("FILTER", String.valueOf(filter));
		properties.put("DAY_OF_YEAR", (new File(this.fullpath)).getParentFile().getParentFile().getName());
		properties.put("YEAR", (new File(this.fullpath)).getParentFile().getParentFile().getParentFile().getName());
		properties.put("DEBLUR_TYPE", (new File(this.fullpath)).getParentFile().getName());
		
		// Note \u00B0 is the unicode degree symbol
		//String deg = "\u00B0";
		//properties.put("Minimum Incidence", Double.toString(minIncidence)+deg);
		//properties.put("Maximum Incidence", Double.toString(maxIncidence)+deg);
		//properties.put("Minimum Emission", Double.toString(minEmission)+deg);
		//properties.put("Maximum Emission", Double.toString(maxIncidence)+deg);
		//properties.put("Minimum Phase", Double.toString(minPhase)+deg);
		//properties.put("Maximum Phase", Double.toString(maxPhase)+deg);

    	return properties;
    }

    public boolean containsLimb()
    {
    	return hasLimb;
    }

	/////////////////////////////////////////////////////////////////
	// The remaining functions are used in the database generation
	/////////////////////////////////////////////////////////////////

    public StringPair getImageStartStopTime() throws IOException
    {
    	// Parse through the lbl file till we find the strings "START_TIME"                                                                 
    	// and "STOP_TIME"
    	StringPair startStop = new StringPair();
    	String filename = getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";

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
			boolean breakedOut = false;
			
		    StringTokenizer st = new StringTokenizer(str);
		    while (st.hasMoreTokens()) 
		    {
		    	String token = st.nextToken();
		    	if (START_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		startStop.s1 = st.nextToken();
		    	}
		    	if (STOP_TIME.equals(token))
		    	{
		    		st.nextToken();
		    		startStop.s2 = st.nextToken();
		    		breakedOut = true;
		    		break;
		    	}
		    }

		    if (breakedOut)
		    	break;
		}
		
    	in.close();
		
    	return startStop;
    }

    public double getSpaceCraftDistance() throws IOException
    {
    	// Parse through the lbl file till we find the strings "TARGET_CENTER_DISTANCE"                                                                 
    	String filename = getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";

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
		    	if (TARGET_CENTER_DISTANCE.equals(token))
		    	{
		    		st.nextToken();
		    		return Double.parseDouble(st.nextToken());
		    	}
		    }
		}
    	
		in.close();
		
    	return Double.MAX_VALUE;
    }

	public vtkPolyData generateImageBorder()
	{
		// TODO Auto-generated method stub
		return null;
	}

    /*
    public boolean isValidPoint(float x, float y, float z)
    {
    	if (x <= MSIImage.PDS_NA || y <= MSIImage.PDS_NA || z <= MSIImage.PDS_NA)
    		return false;
    	else
    		return true;
    }
    */

}
