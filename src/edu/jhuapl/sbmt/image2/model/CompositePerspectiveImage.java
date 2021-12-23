package edu.jhuapl.sbmt.image2.model;

import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.CylindricalBounds;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;

public class CompositePerspectiveImage implements IPerspectiveImage, IPerspectiveImageTableRepresentable
{
	private boolean mapped = false;
	private boolean frustumShowing = false;
	private boolean offlimbShowing = false;
	private boolean boundaryShowing = false;
	private String status = "Unloaded";
	private int index;
	private String filename;
	private ImageSource pointingSourceType;
	private String pointingSource;
	private ImageType imageType;
	private boolean simulateLighting = false;
//	private double offset = -1.0;
//	private double defaultOffset = -1.0;
	private String name;
	private double rotation = 0.0;
    private String flip = "None";
	//Linear interpolation dimensions
	private int[] linearInterpolatorDims = null;
	private double et;
	private Long longTime;

	//fill values
	private double[] fillValues = new double[] {};

	//masking
	private int[] maskValues = new int[] {0, 0, 0, 0};
	private CylindricalBounds bounds = null;

	List<IPerspectiveImage> images;

	public CompositePerspectiveImage(List<IPerspectiveImage> images)
	{
		this.images = images;
	}


	public boolean isMapped()
	{
		return mapped;
	}

	public void setMapped(boolean mapped)
	{
		this.mapped = mapped;
	}

	public boolean isFrustumShowing()
	{
		return frustumShowing;
	}

	public void setFrustumShowing(boolean frustumShowing)
	{
		this.frustumShowing = frustumShowing;
	}

	public boolean isOfflimbShowing()
	{
		return offlimbShowing;
	}

	public void setOfflimbShowing(boolean offlimbShowing)
	{
		this.offlimbShowing = offlimbShowing;
	}

	public boolean isBoundaryShowing()
	{
		return boundaryShowing;
	}

	public void setBoundaryShowing(boolean boundaryShowing)
	{
		this.boundaryShowing = boundaryShowing;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public void setImageType(ImageType imageType)
	{
		this.imageType = imageType;
	}

	public ImageType getImageType()
	{
		return images.get(0).getImageType();
	}

	public String getFilename()
	{
		return images.get(0).getFilename();
	}

	public ImageSource getPointingSourceType()
	{
		System.out.println("CompositePerspectiveImage: getPointingSourceType: pointing type " + images.get(0).getPointingSourceType());
		return images.get(0).getPointingSourceType();
	}

	public String getPointingSource()
	{
		System.out.println("CompositePerspectiveImage: getPointingSource: pointing source " + images.get(0).getPointingSource());
		return images.get(0).getPointingSource();
	}

	public boolean isSimulateLighting()
	{
		return images.get(0).isSimulateLighting();
	}

	public void setSimulateLighting(boolean simulateLighting)
	{
		this.simulateLighting = simulateLighting;
	}

	public double getOffset()
	{
		return images.get(0).getOffset();
	}

	public void setOffset(double offset)
	{
		images.get(0).setOffset(offset);
//		this.offset = offset;
	}

	public double getDefaultOffset()
	{
		return images.get(0).getDefaultOffset();
	}

	public void setDefaultOffset(double defaultOffset)
	{
		this.images.get(0).setDefaultOffset(defaultOffset);
//		this.defaultOffset = defaultOffset;
	}

	public int getNumberOfLayers()
	{
		return images.size();
	}

	public String getName()
	{
		return images.get(0).getName().isEmpty() ? FilenameUtils.getBaseName(images.get(0).getFilename()) : images.get(0).getName();
	}

	public CylindricalBounds getBounds()
	{
		return images.get(0).getBounds();
	}

	public void setBounds(CylindricalBounds bounds)
	{
		this.bounds = bounds;
	}

	public int[] getMaskValues()
	{
		return images.get(0).getMaskValues();
	}

	public void setMaskValues(int[] maskValues)
	{
		this.maskValues = maskValues;
	}

	public int[] getLinearInterpolatorDims()
	{
		return images.get(0).getLinearInterpolatorDims();
	}

	public void setLinearInterpolatorDims(int[] linearInterpolatorDims)
	{
		this.linearInterpolatorDims = linearInterpolatorDims;
	}

	public double getRotation()
	{
		return images.get(0).getRotation();
	}

	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}

	public String getFlip()
	{
		return images.get(0).getFlip();
	}

	public void setFlip(String flip)
	{
		this.flip = flip;
	}

	public void setPointingSourceType(ImageSource pointingSourceType)
	{
		this.pointingSourceType = pointingSourceType;
	}

	public double[] getFillValues()
	{
		return images.get(0).getFillValues();
	}

	public void setFillValues(double[] fillValues)
	{
		this.fillValues = fillValues;
	}

	public double getEt()
	{
		return images.get(0).getEt();
	}

	public void setEt(double et)
	{
		this.et = et;
	}

	public Date getDate()
	{
		return images.get(0).getDate();
	}

	public Long getLongTime()
	{
		return images.get(0).getLongTime();
	}

	public void setLongTime(Long longTime)
	{
		this.longTime = longTime;
	}


	/**
	 * @return the images
	 */
	public List<IPerspectiveImage> getImages()
	{
		return images;
	}
}
