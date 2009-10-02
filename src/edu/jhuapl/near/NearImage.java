package edu.jhuapl.near;

import java.io.*;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import vtk.*;


/**
 * This class represents a near image. It allows retrieving the 
 * image itself as well as the associated IMG file
 * 
 * @author kahneg1
 *
 */
public class NearImage 
{
	private vtkImageData rawImage;
	private vtkImageData displayedImage;
	
	public static final int IMAGE_WIDTH = 537;
	public static final int IMAGE_HEIGHT = 412;
	public static final int NUM_LAYERS = 14;

	public static final float PDS_NA = -1.e32f;
	
	private float[][] incidence = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] emission = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] phase = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] lat = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] lon = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] x = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] y = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] z = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	
	private float[] data = new float[NUM_LAYERS*IMAGE_HEIGHT*IMAGE_WIDTH];
	
	//private BoundingBox bb = new BoundingBox();
	private String name = ""; // The name is a 9 digit number at the beginning of the filename
						 	  // (starting after the initial M0). This is how the lineament 
							  // model names them.

	private float minValue;
	private float maxValue;
	private Range displayedRange = new Range(1,0);

    public static class Range
    {
        public int min;
        public int max;
        public Range(int min, int max)
        {
        	this.min = min;
        	this.max = max;
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

            String extension = getExtension(f);
            if (extension != null) 
            {
                if (extension.equals("img") && f.getName().startsWith(prefix))
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

        private String getExtension(File f) 
        {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) 
            {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

	public NearImage(String filename) throws FitsException, IOException
	{
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
        
        setDisplayedImageRange(new Range(0, 255));

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

		if (imgFileName.length() != 4*NUM_LAYERS*IMAGE_HEIGHT*IMAGE_WIDTH)
			throw new IOException("Corresponding IMG file is not in the correct format");
	
		System.out.println("Using IMG  " + imgFileName);
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(imgFileName.getAbsolutePath())));
		
		for (int i=0;i<data.length; ++i)
		{
			data[i] = swap(in.readFloat());
		}

		
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
	        	
	        	if (xx != PDS_NA && yy != PDS_NA && zz != PDS_NA)
	        	{
	        		//bb.update(xx, yy, zz);
	        		
	        		++numValidPixels;
	        		//if (xx > 100)
	        			//System.out.println()
	        	}
	        	else
	        	{
	        		++numInvalidPixels;
	        	}
	        		        	
	        	x[j][i] = xx;
	        	y[j][i] = yy;
	        	z[j][i] = zz;
	        	
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
	
//	public BoundingBox getBoundingBox()
//	{
//		return bb;
//	}
	
	public String getName()
	{
		return name;
	}

	/*
	public float getLatitude(float x, float y)
	{
		return 0;
	}
	
	public float getLongitude(float x, float y)
	{
		return 0;
	}
	
	public float getPhaseAngle(float x, float y)
	{
		return 0;
	}

	public float getEmissionAngle(float x, float y)
	{
		return 0;
	}

	public float getIncidenceAngle(float x, float y)
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
	
	public Range getDisplayedRange()
	{
		return displayedRange;
	}
	
	public void setDisplayedImageRange(Range range)
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

		}
	}

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
}
