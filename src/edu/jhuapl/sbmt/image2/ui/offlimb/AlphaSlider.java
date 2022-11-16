package edu.jhuapl.sbmt.image2.ui.offlimb;

import javax.swing.JSlider;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;

public class AlphaSlider<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JSlider
{

	public AlphaSlider()
	{
		setMinimum(0);
		setMaximum(100);
	}

	public double getAlphaValue()
	{
		return (double) (getValue() - getMinimum()) / (double) (getMaximum() - getMinimum());
	}
}