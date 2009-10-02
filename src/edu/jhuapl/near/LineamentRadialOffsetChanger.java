package edu.jhuapl.near;

import javax.swing.*;
import javax.swing.event.*;

public class LineamentRadialOffsetChanger extends JPanel implements ChangeListener
{
	private JSlider slider;
	
	private LineamentModel model;

	public LineamentRadialOffsetChanger(LineamentModel model)
	{
		this.model = model;
		setBorder(BorderFactory.createTitledBorder("Lineament Radial Offset"));

		//this.setPreferredSize(new Dimension(300,300));
		slider = new JSlider(0, 30, 15);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(5);
		slider.setPaintTrack(true);
		slider.addChangeListener(this);
		add(slider);
	}
	
	public void stateChanged(ChangeEvent e) 
	{
		if (slider.getValueIsAdjusting())
		{
			int val = slider.getValue();
			int max = slider.getMaximum();
			int min = slider.getMinimum();
			double offset = (val - (max-min)/2.0) * 0.025;
			model.setLineamentRadialOffset(offset);
		}
	}
}
