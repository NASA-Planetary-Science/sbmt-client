package edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage;

import java.util.HashMap;

import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;

public class RenderableCylindricalImage
{
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;
	private LayerMasking masking;
	private double offset;
	private double defaultOffset;
	private CylindricalBounds bounds;

	public RenderableCylindricalImage(Layer layer, HashMap<String, String> metadata, CylindricalBounds bounds)
	{
		this.layer = layer;
		this.bounds = bounds;
		this.metadata = metadata;
		this.imageWidth = layer.iSize();
		this.imageHeight = layer.jSize();
	}


	/**
	 * @return the layer
	 */
	public Layer getLayer()
	{
		return layer;
	}


	/**
	 * @return the metadata
	 */
	public HashMap<String, String> getMetadata()
	{
		return metadata;
	}


	/**
	 * @return the imageWidth
	 */
	public int getImageWidth()
	{
		return imageWidth;
	}


	/**
	 * @return the imageHeight
	 */
	public int getImageHeight()
	{
		return imageHeight;
	}


	public LayerMasking getMasking()
	{
		return masking;
	}


	public void setMasking(LayerMasking masking)
	{
		this.masking = masking;
	}


	public double getOffset()
	{
		return offset;
	}


	public void setOffset(double offset)
	{
		this.offset = offset;
	}


	public double getDefaultOffset()
	{
		return defaultOffset;
	}


	public void setDefaultOffset(double defaultOffset)
	{
		this.defaultOffset = defaultOffset;
	}


	public CylindricalBounds getBounds()
	{
		return bounds;
	}


	public void setBounds(CylindricalBounds bounds)
	{
		this.bounds = bounds;
	}
}
