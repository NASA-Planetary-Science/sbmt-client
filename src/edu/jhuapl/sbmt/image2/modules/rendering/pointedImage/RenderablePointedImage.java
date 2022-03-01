package edu.jhuapl.sbmt.image2.modules.rendering.pointedImage;

import java.util.HashMap;
import java.util.Optional;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image2.api.Layer;
import edu.jhuapl.sbmt.image2.modules.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.model.image.PointingFileReader;

public class RenderablePointedImage
{
	private PointingFileReader pointing;
	private Optional<PointingFileReader> modifiedPointing = Optional.ofNullable(null);
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;
	private LayerMasking masking = new LayerMasking(0, 0, 0, 0);
	private double offset;
	private double defaultOffset;
	private IntensityRange intensityRange;
	private IntensityRange offlimbIntensityRange;
	private double minFrustumLength, maxFrustumLength;
	private double offlimbDepth;
	private boolean isLinearInterpolation = true;

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


	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}


	public void setIntensityRange(IntensityRange intensityRange)
	{
		this.intensityRange = intensityRange;
	}

	public IntensityRange getOfflimbIntensityRange()
	{
		return offlimbIntensityRange;
	}


	public void setOfflimbIntensityRange(IntensityRange intensityRange)
	{
		this.offlimbIntensityRange = intensityRange;
	}


	public double getMinFrustumLength()
	{
		return minFrustumLength;
	}


	public void setMinFrustumLength(double minFrustumLength)
	{
		this.minFrustumLength = minFrustumLength;
	}


	public double getMaxFrustumLength()
	{
		return maxFrustumLength;
	}


	public void setMaxFrustumLength(double maxFrustumLength)
	{
		this.maxFrustumLength = maxFrustumLength;
	}


	public double getOfflimbDepth()
	{
		return offlimbDepth;
	}


	public void setOfflimbDepth(double offlimbDepth)
	{
		this.offlimbDepth = offlimbDepth;
	}


	/**
	 * @return the modifiedPointing
	 */
	public Optional<PointingFileReader> getModifiedPointing()
	{
		return modifiedPointing;
	}


	/**
	 * @param modifiedPointing the modifiedPointing to set
	 */
	public void setModifiedPointing(Optional<PointingFileReader> modifiedPointing)
	{
		this.modifiedPointing = modifiedPointing;
	}


	/**
	 * @return the isLinearInterpolation
	 */
	public boolean isLinearInterpolation()
	{
		return isLinearInterpolation;
	}


	/**
	 * @param isLinearInterpolation the isLinearInterpolation to set
	 */
	public void setLinearInterpolation(boolean isLinearInterpolation)
	{
		this.isLinearInterpolation = isLinearInterpolation;
	}

}
