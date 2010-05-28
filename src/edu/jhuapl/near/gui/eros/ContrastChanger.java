package edu.jhuapl.near.gui.eros;

import javax.swing.*;
import javax.swing.event.*;
import com.jidesoft.swing.*;

import edu.jhuapl.near.model.eros.MSIImage;
import edu.jhuapl.near.util.IntensityRange;


public class ContrastChanger extends JPanel implements ChangeListener
{
	private RangeSlider slider;
	
	private MSIImage msiImage;
	
	public ContrastChanger()
	{
		setBorder(BorderFactory.createTitledBorder("Contrast"));

		//this.setPreferredSize(new Dimension(300,300));
		slider = new RangeSlider(0, 255, 0, 255);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(10);
		slider.setPaintTrack(true);
		slider.addChangeListener(this);
		slider.setEnabled(false);
		add(slider);
	}
	
	public void setMSIImage(MSIImage image)
	{
		if (image != null)
		{
			msiImage = image;
			IntensityRange range = image.getDisplayedRange();
			slider.setLowValue(range.min);
			slider.setHighValue(range.max);
			slider.setEnabled(true);
		}
		else
		{
			slider.setEnabled(false);
		}
	}

	public void stateChanged(ChangeEvent e) 
	{
		int lowVal = slider.getLowValue();
		int highVal = slider.getHighValue();
		if (msiImage != null)
			msiImage.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
	}
}
