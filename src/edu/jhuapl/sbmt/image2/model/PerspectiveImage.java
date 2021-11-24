package edu.jhuapl.sbmt.image2.model;

import java.util.Date;
import java.util.HashMap;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.model.image.ImageSource;

public class PerspectiveImage
{
	private String filename;

	//pointing
	private HashMap<ImageSource, String> pointingSources;

	//masking
	private int[] maskValues = new int[] {0, 0, 0, 0};

	//trim
	private int[] trimValues = new int[] {0, 0, 0, 0};

	//Linear interpolation dimensions
	private int[] linearInterpolatorDims = new int[2];

	//default contrast stretch
	private IntensityRange intensityRange = new IntensityRange(0, 255);

	//number of layers
	private int numberOfLayers = 1;

	//fill values
	private double[] fillValues = new double[] {};

	//flip and rotation
    private double rotation;
    private String flip;

    //source of image
    ImageOrigin imageOrigin;

	//needs: adjusted pointing (load and save), rendering, header/metadata

	private boolean mapped = false;
	private boolean frustumShowing = false;
	private boolean offlimbShowing = false;
	private boolean boundaryShowing = false;
	private String status = "Unloaded";
	private int index;
	private double et;
	private Long longTime;
	private boolean simulateLigting;

	public PerspectiveImage(String filename, HashMap<ImageSource, String> pointingSources, double[] fillValues)
	{
		this.filename = filename;
		this.pointingSources = pointingSources;
		this.fillValues = fillValues;
	}

	public int[] getMaskValues()
	{
		return maskValues;
	}

	public void setMaskValues(int[] maskValues)
	{
		this.maskValues = maskValues;
	}

	public int[] getTrimValues()
	{
		return trimValues;
	}

	public void setTrimValues(int[] trimValues)
	{
		this.trimValues = trimValues;
	}

	public int[] getLinearInterpolatorDims()
	{
		return linearInterpolatorDims;
	}

	public void setLinearInterpolatorDims(int[] linearInterpolatorDims)
	{
		this.linearInterpolatorDims = linearInterpolatorDims;
	}

	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}

	public void setIntensityRange(IntensityRange intensityRange)
	{
		this.intensityRange = intensityRange;
	}

	public String getFilename()
	{
		return filename;
	}

	public HashMap<ImageSource, String> getPointingSources()
	{
		return pointingSources;
	}

	public int getNumberOfLayers()
	{
		return numberOfLayers;
	}

	public double[] getFillValues()
	{
		return fillValues;
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

	public double getEt()
	{
		return et;
	}

	public void setEt(double et)
	{
		this.et = et;
	}

	public Date getDate()
	{
		return new Date(longTime);
	}

	public void setLongTime(Long longTime)
	{
		this.longTime = longTime;
	}

	public double getRotation()
	{
		return rotation;
	}

	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}

	public String getFlip()
	{
		return flip;
	}

	public void setFlip(String flip)
	{
		this.flip = flip;
	}

	public String getImageOrigin()
	{
		return imageOrigin.getFullName();
	}

	public void setImageOrigin(ImageOrigin imageOrigin)
	{
		this.imageOrigin = imageOrigin;
	}

	public boolean isSimulateLigting()
	{
		return simulateLigting;
	}

	public void setSimulateLigting(boolean simulateLigting)
	{
		this.simulateLigting = simulateLigting;
	}
}
