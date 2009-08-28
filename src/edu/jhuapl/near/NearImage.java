package edu.jhuapl.near;

import java.io.IOException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

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
	QImage image;
	float [][] incidence;
	float [][] emission;
	float [][] phase;
	float [][] latitude;
	float [][] longitude;
	float [][] x;
	float [][] y;
	float [][] z;
	
	
	public NearImage(String filename) throws FitsException, IOException
	{
        Fits f = new Fits(filename);
        BasicHDU h = f.getHDU(0);

        float [][] array = (float [][])h.getData().getData();
        int [] axes = h.getAxes();
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
            		System.out.println("Ahh less than zero");
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
