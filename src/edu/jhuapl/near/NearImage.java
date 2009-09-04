package edu.jhuapl.near;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QImage;

/**
 * This class represents a near image. It allows retrieving the 
 * image itself as well as the associated IMG file
 * 
 * @author kahneg1
 *
 */
public class NearImage 
{
	private QImage image;
	public static final int IMAGE_WIDTH = 537;
	public static final int IMAGE_HEIGHT = 412;
	
	private float[][] incidence = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] emission = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] phase = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] lat = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] lon = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] x = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] y = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private float[][] z = new float[IMAGE_HEIGHT][IMAGE_WIDTH];
	private byte[][] imageValues = new byte[IMAGE_HEIGHT][IMAGE_WIDTH];
	
	
	public NearImage(String filename) throws FitsException, IOException
	{
        Fits f = new Fits(filename);
        BasicHDU h = f.getHDU(0);

        float[][] array = (float[][])h.getData().getData();
        int[] axes = h.getAxes();
        int width = axes[1];
        int height = axes[0];
        System.out.println(axes[0]);
        System.out.println(axes[1]);

        image = new QImage(width, height, QImage.Format.Format_RGB32);

        //HashMap<Float, Integer> values = new HashMap<Float, Integer>();
        
        float max = 0.0f;
        for (int i=0; i<height; ++i)
        {
            for (int j=0; j<width; ++j)
            {
            	//values.put(array[j][i], 0);
            	
            	if (array[i][j] > max)
            		max = array[i][j];
            	if (array[i][j] < 0.0)
            		System.out.println("Ahhhhh less than zero");
            }
        }

        //System.out.println("Hash Map size: " + values.size());
        
        QColor c = new QColor();
        for (int i=0; i<height; ++i)
        {
            for (int j=0; j<width; ++j)
            {
            	int val = (int)(array[i][j] * 255.0f / max);
            	c.setRgb(val,val,val);
            	image.setPixel(j, height-i-1, c.rgb());
            }
        }

        image = image.scaled(IMAGE_WIDTH, IMAGE_HEIGHT, 
        		Qt.AspectRatioMode.IgnoreAspectRatio, 
        		Qt.TransformationMode.SmoothTransformation);

        for (int i=0; i<IMAGE_HEIGHT; ++i)
        {
            for (int j=0; j<IMAGE_WIDTH; ++j)
            {
            	int pix = image.pixel(j, height-i-1);
            	c.setRgb(pix);
            	imageValues[i][j] = (byte)c.red();
            }
        }

        loadImgFile(filename);
	}
	
	private void loadImgFile(String filename) throws IOException
	{
        // Search for an IMG with the same prefix in the directory containing the FIT image.
        QFile file = new QFile(filename);
		QFileInfo fileinfo = new QFileInfo(file);

		// Get the part of the file name up to the first underscore
		String basename = fileinfo.fileName();
		int k = basename.indexOf("_");
		if (k == -1)
			k = basename.length() - 4;
		
		String namePrefix = basename.substring(0, k);
		
        List<String> filterList = new ArrayList<String>();
        filterList.add(namePrefix + "*.IMG");

        QDir parent = fileinfo.dir();
		parent.setNameFilters(filterList);
        parent.setFilter(new QDir.Filters(QDir.Filter.Files));
        		
		List<QFileInfo> list = parent.entryInfoList();
		System.out.println(list);
		
		if (list.size() == 0)
			throw new IOException("Could not find IMG image corresponding to " + basename);

		QFileInfo imgFileName = list.get(0);

		//DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(imgFile.filePath())));
		
		QFile imgFile = new QFile(imgFileName.filePath());
		QFile.OpenMode mode = new QFile.OpenMode();
		mode.set(QFile.OpenModeFlag.ReadOnly);
		imgFile.open(mode);
		QDataStream in = new QDataStream(imgFile);

		float a = in.readFloat();

					
	}
	
	public ByteBuffer getSubImage(int size, int x, int y)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(size*size*4);
		
		for (int i=x; i<x+size; ++i)
			for (int j=y; j<y+size; ++j)
			{
				byte pix = 0;
				if (i<IMAGE_HEIGHT && j<IMAGE_WIDTH)
					pix = imageValues[i][j];;
				buffer.put(pix);
				buffer.put(pix);
				buffer.put(pix);
				buffer.put((byte)255);
			}
		
		buffer.rewind();
		return buffer;
	}
	
	public double getLatitude(double x, double y)
	{
		return 0;
	}
	
	public double getLongitude(double x, double y)
	{
		return 0;
	}
	
	public double getPhaseAngle(double x, double y)
	{
		return 0;
	}

	public double getEmissionAngle(double x, double y)
	{
		return 0;
	}

	public double getIncidenceAngle(double x, double y)
	{
		return 0;
	}
	
	public double getX(double x, double y)
	{
		return x;
	}

	public double getY(double x, double y)
	{
		return y;
	}

	public double getZ(double x, double y)
	{
		return 0;
	}

}
