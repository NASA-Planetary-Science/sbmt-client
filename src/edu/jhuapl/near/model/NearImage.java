package edu.jhuapl.near.model;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.*;

import edu.jhuapl.near.pair.*;
import edu.jhuapl.near.util.*;
import edu.jhuapl.near.util.Properties;

/**
 * This class represents a near image. It allows retrieving the 
 * image itself as well as the associated IMG file
 * 
 * @author kahneg1
 *
 */
public class NearImage extends Model
{
	
	public static final int IMAGE_WIDTH = 537;
	public static final int IMAGE_HEIGHT = 412;
	public static final int NUM_LAYERS = 14;
    public static final int TEXTURE_SIZE = 128;
    public static final float PDS_NA = -1.e32f;
    public static String START_TIME = "START_TIME";
    public static String STOP_TIME = "STOP_TIME";
    public static String TARGET_CENTER_DISTANCE = "TARGET_CENTER_DISTANCE";
    public static String HORIZONTAL_PIXEL_SCALE = "HORIZONTAL_PIXEL_SCALE";

	private vtkImageData rawImage;
	private vtkImageData displayedImage;
	
	//private float[][] incidence = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] emission = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] phase = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] latitude = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	//private float[][] longitude = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] x = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] y = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] z = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	
	private float[] data = new float[NUM_LAYERS*IMAGE_HEIGHT*IMAGE_WIDTH];

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
	private double polygonOffset = -10.0;

	boolean hasLimb = false;
	
	/**
	 * Because instances of NearImage can be expensive, we want there to be
	 * no more than one instance of this class per image file on the server.
	 * Hence this class was created to manage the creation and deletion of
	 * NearImage's. Anyone needing a NearImage should use this factory class to
	 * create NearImages's and should NOT call the constructor directly.
	 */
	public static class NearImageFactory
	{
		static private WeakHashMap<NearImage, Object> images = 
			new WeakHashMap<NearImage, Object>();
		
		static public NearImage createImage(String name) throws FitsException, IOException
		{
			for (NearImage image : images.keySet())
			{
				if (image.getServerPath().equals(name))
					return image;
			}

			NearImage image = new NearImage(name);
			images.put(image, null);
			return image;
		}
	}

	
	private static class IMGFilter implements java.io.FileFilter 
    {
    	String prefix;
    	IMGFilter(String prefix)
    	{
    		this.prefix = prefix;
    	}
        //Accept only img files.
        public boolean accept(File f) 
        {
            if (f.isDirectory()) 
            {
                return false;
            }

            String name = f.getName();
            if (name != null) 
            {
                if ((name.toLowerCase().endsWith(".img") ||
                	 name.toLowerCase().endsWith(".img.gz")) && 
                	f.getName().startsWith(prefix))
                {
                	return true;
                } 
                else 
                {
                    return false;
                }
            }

            return false;
        }
    }

	public NearImage(String filename) throws FitsException, IOException
	{
		this.serverpath = filename;
		
		// Download the image, and all the companion files if necessary.
		File fitFile = FileCache.getFileFromServer(filename);

		if (fitFile == null)
			throw new IOException("Could not download " + filename);
		
		String lblFilename = filename.substring(0, filename.length()-4) + ".LBL";
		FileCache.getFileFromServer(lblFilename);
		String imgFilename = filename.substring(0, filename.length()-4) + "_DDR.IMG.gz";
		FileCache.getFileFromServer(imgFilename);
		String imgLblFilename = filename.substring(0, filename.length()-4) + "_DDR.LBL";
		FileCache.getFileFromServer(imgLblFilename);
		String boundaryFilename = filename.substring(0, filename.length()-4) + "_BOUNDARY.VTK";
		FileCache.getFileFromServer(boundaryFilename);

		this.initialize(fitFile);
	}
	
	public NearImage(File fitFile) throws FitsException, IOException
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

        // For now only support images with type float or short since that what near images
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

        loadImgFile(filename);
	}
	
    
	private void loadImgFile(String filename) throws IOException
	{
        // Search for an IMG with the same prefix in the directory containing the FIT image.
        File file = new File(filename);

		// Get the part of the file name up to the first underscore
		String basename = file.getName();
		int k = basename.indexOf("_");
		if (k == -1)
			k = basename.length() - 4;

        if (basename.length() >= 11)
        	name = basename.substring(2,11);

        basename = basename.substring(0, k);
        File parent = file.getParentFile();
        		
        File[] list = parent.listFiles(new IMGFilter(basename));
		
        if (list.length == 0)
			throw new IOException("Could not find IMG file corresponding to " + basename);

		File imgFileName = list[0];

		//if (imgFileName.length() != 4*NUM_LAYERS*IMAGE_HEIGHT*IMAGE_WIDTH)
		//	throw new IOException("Corresponding IMG file is not in the correct format");
	
		System.out.println("Using IMG  " + imgFileName);
		
		FileInputStream fs = new FileInputStream(imgFileName.getAbsolutePath());
		InputStream is = fs;
		if (imgFileName.getAbsolutePath().toLowerCase().endsWith(".gz"))
		{
			is = new GZIPInputStream(fs);
		}
		BufferedInputStream bs = new BufferedInputStream(is);
		DataInputStream in = new DataInputStream(bs);
		
		for (int i=0;i<data.length; ++i)
		{
			data[i] = swap(in.readFloat());
		}

		in.close();
		
        //vtkImageData tmp = new vtkImageData();
        //tmp.SetScalarTypeToUnsignedChar();
        //tmp.SetDimensions(IMAGE_WIDTH, IMAGE_HEIGHT, 1);
        //tmp.SetSpacing(1.0, 1.0, 1.0);
        //tmp.SetOrigin(0.0, 0.0, 0.0);
        //tmp.SetNumberOfScalarComponents(4);

		int numValidPixels = 0;
		int numInvalidPixels = 0;
	    for (int j=0; j<IMAGE_HEIGHT; ++j)
	    {
	        for (int i=0; i<IMAGE_WIDTH; ++i)
	        {
	        	float xx = data[index(i,j,7)];
	        	float yy = data[index(i,j,8)];
	        	float zz = data[index(i,j,9)];
	        	//float lat = data[index(i,j,0)];
	        	//float lon = data[index(i,j,1)];
	        	float inc = data[index(i,j,4)];
	        	float emi = data[index(i,j,3)];
	        	float pha = data[index(i,j,2)];
	        	
	        	if (xx != PDS_NA && yy != PDS_NA && zz != PDS_NA)
	        	{
	        		boundingBox.update(xx, yy, zz);

	        		if (inc < minIncidence)
	        			minIncidence = inc;
	        		if (inc > maxIncidence)
	        			maxIncidence = inc;
	        		if (emi < minEmission)
	        			minEmission = emi;
	        		if (emi > maxEmission)
	        			maxEmission = emi;
	        		if (pha < minPhase)
	        			minPhase = pha;
	        		if (pha > maxPhase)
	        			maxPhase = pha;

	        		++numValidPixels;
	        	}
	        	else
	        	{
	        		++numInvalidPixels;
	        		hasLimb = true;
	        	}
	        		        	
	        	x[j][i] = xx;
	        	y[j][i] = yy;
	        	z[j][i] = zz;
	        	//latitude[j][i] = lat;
	        	//longitude[j][i] = lon;
	        	
	        	//int v = xx != PDS_NA ? 255 : (int)xx;
	        	//tmp.SetScalarComponentFromDouble(i, j, 0, 0, v);
        		//tmp.SetScalarComponentFromDouble(i, j, 0, 1, v);
        		//tmp.SetScalarComponentFromDouble(i, j, 0, 2, v);
        		//tmp.SetScalarComponentFromDouble(i, j, 0, 3, 255);
        		//System.out.print(xx + " ");
	        }
	        //System.out.println();
	    }
	
	    //vtkPNGWriter writer = new vtkPNGWriter();
	    //writer.SetFileName("xx.png");
	    //writer.SetInput(tmp);
	    //writer.Write();
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

	
	//public ArrayList<vtkActor> getMappedImage(double offset)
    public ArrayList<vtkProp> getProps()
    {
    	ArrayList<vtkProp> imageActors = new ArrayList<vtkProp>();
    	
        int numTexturesWidth = (int)Math.ceil((double)(NearImage.IMAGE_WIDTH-1) / (double)(TEXTURE_SIZE-1));
        int numTexturesHeight = (int)Math.ceil((double)(NearImage.IMAGE_HEIGHT-1) / (double)(TEXTURE_SIZE-1));
        
        //int totalNumTextures = numTexturesWidth * numTexturesHeight;
        //System.out.println("totalNumTextures  " + totalNumTextures);

        float x, y, z, s, t;
        int i0, i1, i2, i3;
        
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(3);
        
        for (int i=0; i<numTexturesHeight; ++i)
        	for (int j=0; j<numTexturesWidth; ++j)
        	{
                int corner1 = i*(TEXTURE_SIZE-1);
                int corner2 = j*(TEXTURE_SIZE-1);
        
                vtkPolyData piece = new vtkPolyData();
                vtkPoints points = new vtkPoints();
                vtkCellArray polys = new vtkCellArray();
                vtkFloatArray tcoords = new vtkFloatArray();
                int[][] indices;
                
                int c = 0;
                int maxM = TEXTURE_SIZE;
                if (i==numTexturesHeight-1)
                	maxM = (NearImage.IMAGE_HEIGHT) - i*(TEXTURE_SIZE-1);
                int maxN = TEXTURE_SIZE;
                if (j==numTexturesWidth-1)
                	maxN = (NearImage.IMAGE_WIDTH) - j*(TEXTURE_SIZE-1);

                //points.SetNumberOfPoints(maxM*maxN);
                tcoords.SetNumberOfComponents(2);
                //tcoords.SetNumberOfTuples(maxM*maxN);
                indices = new int[maxM][maxN];
                
                // First add points and texture coordinates to the vtkPoints array
                for (int m=0; m<maxM; ++m)
        			for (int n=0; n<maxN; ++n)
        			{
        				x = getX(corner1+m, corner2+n);
        				y = getY(corner1+m, corner2+n);
        				z = getZ(corner1+m, corner2+n);
        		
        				if (isValidPoint(x, y, z))
        				{
        					s = (float)(n)/(float)(TEXTURE_SIZE-1);
        					t = (float)(m)/(float)(TEXTURE_SIZE-1);

        					//points.SetPoint(c, x, y, z);
        					//tcoords.SetTuple2(c, s, t);
        					points.InsertNextPoint(x, y, z);
        					tcoords.InsertNextTuple2(s, t);
        					
        					indices[m][n] = c;

        					++c;
        				}
        				else
        				{
        					indices[m][n] = -1;
        				}
        			}

                // Now add connectivity information
                for (int m=1; m<maxM; ++m)
        			for (int n=1; n<maxN; ++n)
        			{
        				// Get the indices of the 4 corners of the rectangle to the upper left
        				i0 = indices[m-1][n-1];
        				i1 = indices[m][n-1];
        				i2 = indices[m-1][n];
        				i3 = indices[m][n];

        				// Add upper left triangle
        				if (i0>=0 && i1>=0 && i2>=0)
        				{
        					idList.SetId(0, i0);
        					idList.SetId(1, i1);
        					idList.SetId(2, i2);
        					polys.InsertNextCell(idList);
        				}
        				// Add bottom right triangle
        				if (i2>=0 && i1>=0 && i3>=0)
        				{
        					idList.SetId(0, i2);
        					idList.SetId(1, i1);
        					idList.SetId(2, i3);
        					polys.InsertNextCell(idList);
        				}
        			}

                // Now map the data to 
                piece.SetPoints(points);
                piece.SetPolys(polys);
                piece.GetPointData().SetTCoords(tcoords);

                vtkTexture texture = new vtkTexture();
                texture.InterpolateOn();
                texture.RepeatOff();
                texture.EdgeClampOn();

                vtkImageData imagePiece = getSubImage(TEXTURE_SIZE, corner1, corner2);
                //System.out.println("\n\n\n\nnext image piece " + ccc++ + "\n\n");
                //System.out.println(imagePiece.GetDimensions()[0]);
                //System.out.println(imagePiece.GetDimensions()[1]);
                //System.out.println(imagePiece.GetDimensions()[2]);

                
                texture.SetInput(imagePiece);
                
                vtkPolyDataMapper pieceMapper = new vtkPolyDataMapper();
                pieceMapper.SetResolveCoincidentTopologyToPolygonOffset();
                pieceMapper.SetResolveCoincidentTopologyPolygonOffsetParameters(-1.0, polygonOffset);
                pieceMapper.SetInput(piece);
                pieceMapper.Update();
                
                vtkActor pieceActor = new vtkActor();
                pieceActor.SetMapper(pieceMapper);
                pieceActor.SetTexture(texture);
                pieceActor.GetProperty().LightingOff();
        
                imageActors.add(pieceActor);
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

	    	this.pcs.firePropertyChange(Properties.MSI_CONTRAST_CHANGED, null, null);
		}
	}

	public void setPolygonOffset(double offset)
	{
		this.polygonOffset = offset;
	}
	
	/*
	public vtkPolyData getImageBorder()
	{
    	String filename = getFullPath();
    	
    	String lblFilename = filename.substring(0, filename.length()-4) + "_BOUNDARY.vtk";

    	vtkPolyDataReader reader = new vtkPolyDataReader();
    	reader.SetFileName(lblFilename);
    	
	}
	*/

	private int index(int i, int j, int k)
	{
		return ((k * IMAGE_HEIGHT + j) * IMAGE_WIDTH + i);
	}
	  
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
		String deg = "\u00B0";
		properties.put("Minimum Incidence", Double.toString(minIncidence)+deg);
		properties.put("Maximum Incidence", Double.toString(maxIncidence)+deg);
		properties.put("Minimum Emission", Double.toString(minEmission)+deg);
		properties.put("Maximum Emission", Double.toString(maxIncidence)+deg);
		properties.put("Minimum Phase", Double.toString(minPhase)+deg);
		properties.put("Maximum Phase", Double.toString(maxPhase)+deg);

    	return properties;
    }

    public boolean containsLimb()
    {
    	return hasLimb;
    }

	/////////////////////////////////////////////////////////////////
	// The remaining functions are used in the database generation
	/////////////////////////////////////////////////////////////////

//	public ObjectPair getLatLonBoundary()
//	{
//		ObjectPair latlon = new ObjectPair();
//		
//		return latlon;
//	}
	
	public vtkPolyData generateImageBorder()
	{
		// First create the mesh
        vtkPolyData polydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();
        int[][] indices;
        
        int c = 0;
        float x, y, z;
        int i0, i1, i2, i3;
        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(3);

        BoundingBox bb = new BoundingBox();

        //points.SetNumberOfPoints(maxM*maxN);
        indices = new int[NearImage.IMAGE_HEIGHT][NearImage.IMAGE_WIDTH];
        
        // First add points to the vtkPoints array
        for (int m=0; m<NearImage.IMAGE_HEIGHT; ++m)
			for (int n=0; n<NearImage.IMAGE_WIDTH; ++n)
			{
				x = getX(m, n);
				y = getY(m, n);
				z = getZ(m, n);
		
				if (isValidPoint(x, y, z))
				{
					//points.SetPoint(c, x, y, z);
					points.InsertNextPoint(x, y, z);
					
					indices[m][n] = c;

					bb.update(x, y, z);
					
					++c;
				}
				else
				{
					indices[m][n] = -1;
				}
			}

        // Now add connectivity information
        for (int m=1; m<NearImage.IMAGE_HEIGHT; ++m)
			for (int n=1; n<NearImage.IMAGE_WIDTH; ++n)
			{
				// Get the indices of the 4 corners of the rectangle to the upper left
				i0 = indices[m-1][n-1];
				i1 = indices[m][n-1];
				i2 = indices[m-1][n];
				i3 = indices[m][n];

				// Add upper left triangle
				if (i0>=0 && i1>=0 && i2>=0)
				{
					idList.SetId(0, i0);
					idList.SetId(1, i1);
					idList.SetId(2, i2);
					polys.InsertNextCell(idList);
				}
				// Add bottom right triangle
				if (i2>=0 && i1>=0 && i3>=0)
				{
					idList.SetId(0, i2);
					idList.SetId(1, i1);
					idList.SetId(2, i3);
					polys.InsertNextCell(idList);
				}
			}

        // Now map the data to 
        polydata.SetPoints(points);
        polydata.SetPolys(polys);

        vtkDecimatePro decimate = new vtkDecimatePro();
        decimate.SetInput(polydata);
        decimate.PreserveTopologyOn();
        decimate.SplittingOff();
        decimate.BoundaryVertexDeletionOn();
        decimate.SetMaximumError(1.0e+299);
        decimate.SetTargetReduction(0.99);
        //decimate.Update();
        
        vtkFillHolesFilter fillHoles = new vtkFillHolesFilter();
        //fillHoles.SetInputConnection(decimate.GetOutputPort());
        fillHoles.SetInput(polydata);
        fillHoles.SetHoleSize(bb.getLargestSide()/2.0);
        //fillHoles.Update();
        
        //vtkPolyDataWriter writer1 = new vtkPolyDataWriter();
        //writer1.SetInputConnection(fillHoles.GetOutputPort());
        //writer1.SetFileName("piece1.vtk");
        //writer1.Write();
        
        /*
         * as a test, write out this polydata to a file
         */
		
        /////////////////////////////////////////////////////////////
        // Next 
        vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
        edgeExtracter.SetInputConnection(fillHoles.GetOutputPort());
        edgeExtracter.BoundaryEdgesOn();
        edgeExtracter.FeatureEdgesOff();
        edgeExtracter.NonManifoldEdgesOff();
        edgeExtracter.ManifoldEdgesOff();
        edgeExtracter.Update();
        
        vtkPolyData output = new vtkPolyData();
        output.DeepCopy(edgeExtracter.GetOutput());
        output.GetCellData().SetScalars(null);
        
        boolean succeeded = this.convertLinesToPolyLine(output);
        if (succeeded == false)
        	return null;
        
        //vtkPolyDataWriter writer2 = new vtkPolyDataWriter();
        //writer2.SetInput(output);
        //writer2.SetFileName("boundary1.vtk");
        ////writer2.SetFileTypeToBinary();
        //writer2.Write();
        
		return output;
	}

	/*
	// This version does not work. Do not use it.
	public ArrayList<LatLon> getImageBorder()
	{
		ArrayList<LatLon> pixels = new ArrayList<LatLon>();
		
		// Start out at the top left and move to the right and shoot out a ray
		// downward till we hit a valid pixel
		
		int farthestDown = 0;
		int farthestLeft = NearImage.IMAGE_WIDTH-1;
		int farthestUp = NearImage.IMAGE_HEIGHT-1;
		//int farthestRight = 0;
		
		// Top
		for (int i=0; i<NearImage.IMAGE_WIDTH; ++i)
		{
			for (int j=0; j<NearImage.IMAGE_HEIGHT; ++j)
			{
				float lat = getLatitude(j, i);
				float lon = getLongitude(j, i);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestDown = j+1;
					break;
				}
			}
		}
		// If nothing was found, we can return
		if (pixels.size() == 0)
			return pixels;
		
		// Right
		for (int i=farthestDown; i<NearImage.IMAGE_HEIGHT; ++i)
		{
			for (int j=NearImage.IMAGE_WIDTH-1; j>=0; --j)
			{
				float lat = getLatitude(i, j);
				float lon = getLongitude(i, j);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestLeft = j-1;
					break;
				}
			}
		}
		// Bottom
		for (int i=farthestLeft; i>=0; --i)
		{
			for (int j=NearImage.IMAGE_HEIGHT-1; j>=0; --j)
			{
				float lat = getLatitude(j, i);
				float lon = getLongitude(j, i);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					farthestUp = j-1;
					break;
				}
			}
		}
		// Left
		for (int i=farthestUp; i>=0; --i)
		{
			for (int j=0; j<NearImage.IMAGE_WIDTH; ++j)
			{
				float lat = getLatitude(i, j);
				float lon = getLongitude(i, j);
				if (isValidPixel(lat, lon))
				{
					pixels.add(new LatLon(lat, lon));
					//farthestRight = j+1;
					break;
				}
			}
		}
		
		return pixels;
	}
	 */

	private boolean convertLinesToPolyLine(vtkPolyData polydata)
	{
		// The boundary generated in getImageBorder is great, unfortunately the
		// border consists of many lines of 2 vertices each. We, however, need a
		// single polyline consisting of all the points. I was not able to find
		// something in vtk that can convert this, so we will have to implement it
		// here. Fortunately, the algorithm is pretty simple (assuming the list of
		// lines forms a true closed loop with no intersections or other anomalies):
		// Start with the first 2-vertex line segment. These 2 points will
		// be the first 2 points of our new polyline we're creating. 
		// Choose the second point. Now
		// in addition to this line segment, there is only one other line segment 
		// that contains the second point. Search for that line segment and let the
		// other point in that line segment be the 3rd point of our polyline. Repeat
		// this till we've formed the polyline.
		
		vtkCellArray lines_orig = polydata.GetLines();
		vtkPoints points_orig = polydata.GetPoints();

		vtkIdTypeArray idArray = lines_orig.GetData();
		int size = idArray.GetNumberOfTuples();
		//System.out.println(size);
		//System.out.println(idArray.GetNumberOfComponents());

		if (size < 9 || points_orig.GetNumberOfPoints() < 3)
		{
			System.out.println("Error: polydata too small");
			return false;
		}
		
		ArrayList<IdPair> lines = new ArrayList<IdPair>();
		for (int i=0; i<size; i+=3)
		{
			//System.out.println(idArray.GetValue(i));
			if (idArray.GetValue(i) != 2)
			{
				System.out.println("Big problem");
				return false;
			}
			lines.add(new IdPair(idArray.GetValue(i+1), idArray.GetValue(i+2)));
		}

        vtkIdList idList = new vtkIdList();
        IdPair line = lines.get(0);
        idList.InsertNextId(line.id1);
        idList.InsertNextId(line.id2);
        
        int numPoints = polydata.GetNumberOfPoints();
        for (int i=2; i<numPoints; ++i)
        {
        	int id = line.id2;

        	// Find the other line segment that contains id
        	for (int j=1; j<lines.size(); ++j)
        	{
        		IdPair nextLine = lines.get(j);
        		if (id == nextLine.id1)
        		{
        			idList.InsertNextId(nextLine.id2);
        			
        			line = nextLine;
        			break;
        		}
        		else if (id == nextLine.id2 && line.id1 != nextLine.id1)
        		{
        			idList.InsertNextId(nextLine.id1);

        			// swap the ids
        			int tmp = nextLine.id1;
        			nextLine.id1 = nextLine.id2;
        			nextLine.id2 = tmp;
        			
        			line = nextLine;
        			break;
        		}
        
        		if (j==lines.size()-1)
        		{
        			System.out.println("Error: Could not find other line segment");
        			System.out.println("i, j = " + i + " " + j);
        			return false;
        		}
        	}
        }

        idList.InsertNextId(idList.GetId(0));
        
        // It would be nice if the points were in the order they are drawn rather
        // than some other arbitrary order. Therefore reorder the points so that
        // the id list will just be increasing numbers in order
        vtkPoints points = new vtkPoints();
        points.SetNumberOfPoints(numPoints);
        for (int i=0; i<numPoints; ++i)
        {
        	int id = idList.GetId(i);
        	points.SetPoint(i, points_orig.GetPoint(id));
        }
        for (int i=0; i<numPoints; ++i)
        {
        	idList.SetId(i, i);
        }
    	idList.SetId(numPoints, 0);
        
    	polydata.SetPoints(null);
    	polydata.SetPoints(points);
    	
        System.out.println("num points: " + numPoints);
        System.out.println("num ids: " + idList.GetNumberOfIds());
        polydata.SetLines(null);
        vtkCellArray new_lines = new vtkCellArray();
        new_lines.InsertNextCell(idList);
        polydata.SetLines(new_lines);
        
        return true;
	}

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

//    public String getYear()
//    {
//    	
//    }
//
//    public String getDayOfYear();
//    {
//    	
//    }
//    
//    public int getIofdblOrCifdbl()
//    {
//    	
//    }
    
    public boolean isValidPoint(float x, float y, float z)
    {
    	if (x <= NearImage.PDS_NA || y <= NearImage.PDS_NA || z <= NearImage.PDS_NA)
    		return false;
    	else
    		return true;
    }
    

}
