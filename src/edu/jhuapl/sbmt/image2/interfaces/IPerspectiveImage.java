package edu.jhuapl.sbmt.image2.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.image2.model.CylindricalBounds;

public interface IPerspectiveImage
{
	public String getFilename();

	public void setImageType(ImageType imageType);

	public ImageType getImageType();

	public String getPointingSource();

	public void setPointingSource(String pointingSource);

	public Optional<String> getModifiedPointingSource();

	public void setModifiedPointingSource(Optional<String> modifiedPointingSource);

	public boolean isSimulateLighting();

	public void setSimulateLighting(boolean simulateLighting);

	public double getOffset();

	public void setOffset(double offset);

	public double getDefaultOffset();

	public void setDefaultOffset(double defaultOffset);

	public int getNumberOfLayers();

	public int getCurrentLayer();

	public void setCurrentLayer(int index);

	public String getName();

	public void setName(String name);

	public CylindricalBounds getBounds();

	public void setBounds(CylindricalBounds bounds);

	public int[] getMaskValues();

	public void setMaskValues(int[] maskValues);

	public int[] getTrimValues();

	public void setTrimValues(int[] trimValues);

	public int[] getLinearInterpolatorDims();

	public void setLinearInterpolatorDims(int[] linearInterpolatorDims);

	public double getRotation();

	public void setRotation(double rotation);

	public String getFlip();

	public void setFlip(String flip);

	public ImageSource getPointingSourceType();

	public void setPointingSourceType(ImageSource pointingSourceType);

	public double[] getFillValues();

	public void setFillValues(double[] fillValues);

	public double getEt();

	public void setEt(double et);

	public Date getDate();

	public Long getLongTime();

	public void setLongTime(Long longTime);

	public List<IPerspectiveImage> getImages();

	public IntensityRange getIntensityRange();

	public void setIntensityRange(IntensityRange intensityRange);

	public IntensityRange getOfflimbIntensityRange();

	public void setOfflimbIntensityRange(IntensityRange intensityRange);

	public double getOfflimbDepth();

	public void setOfflimbDepth(double depth);

	public double getMinFrustumLength();

	public void setMinFrustumLength(double minFrustumLength);

	public double getMaxFrustumLength();

	public void setMaxFrustumLength(double maxFrustumLength);

	public boolean getInterpolateState();

	public void setInterpolateState(boolean isLinear);
}
