package edu.jhuapl.near;

import java.io.*;
import java.nio.ByteBuffer;


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
//	private QImage image;
	private vtkImageData image;
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
	
	private byte[][] imageValues = new byte[IMAGE_HEIGHT][IMAGE_WIDTH];

	private Point center = new Point();

	private BoundingBox bb = new BoundingBox();
	private String name = ""; // The name is a 9 digit number at the beginning of the filename
						 	  // (starting after the initial M0). This is how the lineament 
							  // model names them.

    private static class IMGFilter implements java.io.FileFilter 
    {
    	String prefix;
    	IMGFilter(String prefix)
    	{
    		this.prefix = prefix;
    	}
        //Accept all directories and all img files.
        public boolean accept(File f) 
        {
            if (f.isDirectory()) 
            {
                return true;
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

        float max = -Float.MAX_VALUE;
        float min = Float.MAX_VALUE;
        for (int i=0; i<originalHeight; ++i)
        {
            for (int j=0; j<originalWidth; ++j)
            {
            	if (array[i][j] > max)
            		max = array[i][j];
            	if (array[i][j] < min)
            		min = array[i][j];
            }
        }

//        {
//        	vtkJPEGReader r = new vtkJPEGReader();
//        	r.SetFileName("/home/kahneg1/src/gmti/tiles/LandSatI3_Iraq_jpeg/8/891/891_1612.jpeg");
//        	r.Update();
//        	//System.out.println(r.GetOutput());
//        	//System.out.println(r.GetOutput().GetScalarTypeAsString());
//        }
        
        System.out.println("Begin vtkImageData create");
        // Create a vtk image data holding this image
        image = new vtkImageData();
        image.SetScalarTypeToUnsignedChar();
        image.SetDimensions(originalWidth, originalHeight, 1);
        image.SetSpacing(1, 1, 1);
        image.SetOrigin(0.0, 0.0, 0.0);
        image.SetNumberOfScalarComponents(4);
        for (int i=0; i<originalHeight; ++i)
        	for (int j=0; j<originalWidth; ++j)
        	{
            	int val = (int)((array[i][j]-min) * 255.0f / (max-min));
        		image.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 0, val);
        		image.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 1, val);
        		image.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 2, val);
        		image.SetScalarComponentFromDouble(j, originalHeight-1-i, 0, 3, 255);
        	}
        System.out.println("End vtkImageData create");
        
        
        // Now scale this image
        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(image);
        reslice.SetInterpolationModeToLinear();
        reslice.SetOutputSpacing(1.0, (double)originalHeight/(double)IMAGE_HEIGHT, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, IMAGE_WIDTH-1, 0, IMAGE_HEIGHT-1, 0, 0);
        reslice.Update();
        
        image = reslice.GetOutput();
        image.SetSpacing(1, 1, 1);
        
        //System.out.println(image);
        
        //System.out.println("Hash Map size: " + values.size());

        /*

        image = new QImage(originalWidth, originalHeight, QImage.Format.Format_RGB32);

        QColor c = new QColor();
        for (int i=0; i<originalHeight; ++i)
        {
            for (int j=0; j<originalWidth; ++j)
            {
            	int val = (int)((array[i][j]-min) * 255.0f / (max-min));
            	c.setRgb(val,val,val);
            	image.setPixel(j, originalHeight-i-1, c.rgb());
            }
        }

        image = image.scaled(IMAGE_WIDTH, IMAGE_HEIGHT, 
        		Qt.AspectRatioMode.IgnoreAspectRatio, 
        		Qt.TransformationMode.SmoothTransformation);

        for (int i=0; i<IMAGE_HEIGHT; ++i)
        {
            for (int j=0; j<IMAGE_WIDTH; ++j)
            {
            	int pix = image.pixel(j, IMAGE_HEIGHT-i-1);
            	c.setRgb(pix);
            	imageValues[i][j] = (byte)c.red();
            }
        }
	*/

        loadImgFile(filename);
	}
	
    
	private void loadImgFile(String filename) throws IOException
	{
        // Search for an IMG with the same prefix in the directory containing the FIT image.
        File file = new File(filename);
		//QFileInfo fileinfo = new QFileInfo(file);

		// Get the part of the file name up to the first underscore
		String basename = file.getName();
		int k = basename.indexOf("_");
		if (k == -1)
			k = basename.length() - 4;

        if (basename.length() >= 11)
        	name = basename.substring(2,11);

        basename = basename.substring(0, k);
        File parent = file.getParentFile();
        //System.out.println("basename  " +basename);
        //System.out.println(parent);
        		
        File[] list = parent.listFiles(new IMGFilter(basename));
        //for (File f : list)
        //	System.out.println(f);
		
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
	        		bb.update(xx, yy, zz);
	        		center.x += xx;
	        		center.y += yy;
	        		center.z += zz;
	        		
	        		++numValidPixels;
	        	}
	        	else
	        	{
	        		++numInvalidPixels;
	        	}
	        		        	
	        	x[j][i] = xx;
	        	y[j][i] = yy;
	        	z[j][i] = zz;  
	        }
	    }
	
	    //System.out.println("numInvalidPixels:  " + numInvalidPixels);
	    //System.out.println("bb:  " + bb);
	    
	    if (numValidPixels > 0)
	    {
	    	center.x /= (double)numValidPixels;
	    	center.y /= (double)numValidPixels;
	    	center.z /= (double)numValidPixels;
	    }
	}
	
	public vtkImageData getSubImage2(int size, int row, int col)
	{
        vtkImageReslice reslice = new vtkImageReslice();
        reslice.SetInput(image);
        reslice.SetInterpolationModeToNearestNeighbor();
        reslice.SetOutputSpacing(1.0, 1.0, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(col, col+size-1, row, row+size-1, 0, 0);
        reslice.Update();

        return reslice.GetOutput();
	}
	
	
	public ByteBuffer getSubImage(int size, int row, int col)
	{
		ByteBuffer buffer = ByteBuffer.allocate(size*size*4);
		
		for (int i=row; i<row+size; ++i)
			for (int j=col; j<col+size; ++j)
			{
				byte pix = 0;
				if (i<IMAGE_HEIGHT && j<IMAGE_WIDTH)
					pix = imageValues[i][j];
				buffer.put(pix);
				buffer.put(pix);
				buffer.put(pix);
				buffer.put((byte)255);
			}
		
		buffer.rewind();
		return buffer;
	}
	
	public BoundingBox getBoundingBox()
	{
		return bb;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Point getCenter()
	{
		return center;
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
//		return col;
//		if (row < IMAGE_HEIGHT && col < IMAGE_WIDTH)
		{
			//System.out.println(x[row][col]);
			return x[row][col];
			
		}
//		else
//			return 0.0;
	}

	public float getY(int row, int col)
	{
//		return row;
//		if (row < IMAGE_HEIGHT && col < IMAGE_WIDTH)
		{
			//System.out.println(y[row][col]);
			return y[row][col];
		}
//		else
//			return 0.0;
	}

	public float getZ(int row, int col)
	{
//		return 0;
//		if (row < IMAGE_HEIGHT && col < IMAGE_WIDTH)
		{
			//System.out.println(z[row][col]);
			return z[row][col];
		}
//		else
//			return 0.0;
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
