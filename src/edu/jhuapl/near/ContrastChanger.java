package edu.jhuapl.near;

import javax.swing.*;
import com.jidesoft.swing.*;
import javax.swing.event.*;

public class ContrastChanger extends JPanel implements ChangeListener
{
	private RangeSlider slider;
	
	private NearImage nearImage;
	
	private ImageGLWidget viewer;
	
	public ContrastChanger(ImageGLWidget glWidget)
	{
		this.viewer = glWidget;
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
	
	void setNearImage(NearImage image)
	{
		if (image != null)
		{
			nearImage = image;
			NearImage.Range range = image.getDisplayedRange();
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
		if (nearImage != null)
			viewer.setDisplayedImageRange(nearImage, new NearImage.Range(lowVal, highVal));
	}
}
