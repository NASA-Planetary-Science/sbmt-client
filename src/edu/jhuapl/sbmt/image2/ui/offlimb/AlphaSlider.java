package edu.jhuapl.sbmt.image2.ui.offlimb;

import javax.swing.JSlider;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;

public class AlphaSlider<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JSlider
{
	/**
	 *
	 */
//	private final OfflimbControlsController<G1> offlimbControlsController;

	public AlphaSlider(/*OfflimbControlsController<G1> offlimbControlsController*/)
	{
//		this.offlimbControlsController = offlimbControlsController;
		setMinimum(0);
		setMaximum(100);
	}

//	public void applyAlphaToImage()
//	{
//		this.offlimbControlsController.collection.setOfflimbOpactity(getAlphaValue());
//		// image.setOffLimbFootprintAlpha(getAlphaValue());
//	}

	public double getAlphaValue()
	{
		return (double) (getValue() - getMinimum()) / (double) (getMaximum() - getMinimum());
	}
}