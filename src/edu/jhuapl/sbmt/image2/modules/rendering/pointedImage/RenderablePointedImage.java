package edu.jhuapl.sbmt.image2.modules.rendering.pointedImage;

import java.util.HashMap;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class RenderablePointedImage
{
	private PointingFileReader pointing;
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;
	private LayerMasking masking;
	private double offset;
	private double defaultOffset;

	public RenderablePointedImage(Layer layer, HashMap<String, String> metadata, PointingFileReader pointing)
	{
		this.layer = layer;
		this.pointing = pointing;
		this.metadata = metadata;
		this.imageWidth = layer.iSize();
		this.imageHeight = layer.jSize();
	}


	/**
	 * @return the pointing
	 */
	public PointingFileReader getPointing()
	{
		return pointing;
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

	//MOVE THESE TO POINTINGFILEREADER?
	public double getMaxFovAngle()
    {
        return Math.max(getHorizontalFovAngle(), getVerticalFovAngle());
    }

    public double getHorizontalFovAngle()
    {
        double fovHoriz = MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum3()) * 180.0 / Math.PI;
        return fovHoriz;
    }

    public double getVerticalFovAngle()
    {
        double fovVert = MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum2()) * 180.0 / Math.PI;
        return fovVert;
    }

}
