package edu.jhuapl.near;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

        float max = 0.0f;
        for (int i=0; i<width; ++i)
        {
            for (int j=0; j<height; ++j)
            {
            	if (array[j][i] > max)
            		max = array[j][i];
            	if (array[j][i] < 0.0)
            		System.out.println("Ahhhhh less than zero");
            }
        }

        QColor c = new QColor();
        for (int i=0; i<width; ++i)
        {
            for (int j=0; j<height; ++j)
            {
            	int val = (int)(array[j][i] * 255.0f / max);
            	c.setRgb(val,val,val);
            	image.setPixel(i, height-j-1, c.rgb());
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
        		
		List<String> list = parent.entryList();
		System.out.println(list);
		
		if (list.size() == 0)
			throw new IOException("Could not find IMG image corresponding to " + basename);

		String imgFile = list.get(0);

		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(imgFile)));
		
		
					
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
		return 0;
	}

	public double getY(double x, double y)
	{
		return 0;
	}

	public double getZ(double x, double y)
	{
		return 0;
	}

}
