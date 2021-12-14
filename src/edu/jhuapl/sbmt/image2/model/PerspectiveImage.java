package edu.jhuapl.sbmt.image2.model;

import java.util.Date;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image2.modules.rendering.cylindricalImage.CylindricalBounds;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class PerspectiveImage
{
	private static final  Key<String> nameKey = Key.of("name");
    private static final  Key<String> imageFileNameKey = Key.of("imagefilename");
    private static final  Key<String> pointingSourceKey = Key.of("pointingSource");
    private static final  Key<String> pointingSourceTypeKey = Key.of("pointingSourceType");
    private static final  Key<Integer[]> intensityRangeKey = Key.of("intensityRange");
    private static final  Key<Integer> numberOfLayersKey = Key.of("numberOfLayers");
    private static final  Key<String> flipKey = Key.of("flip");
    private static final  Key<Double> rotationKey = Key.of("rotation");
    private static final  Key<String> imageOriginKey = Key.of("imageOrigin");
    private static final  Key<Boolean> mappedKey = Key.of("mapped");
    private static final  Key<Boolean> frustumKey = Key.of("frustum");
    private static final  Key<Boolean> boundaryKey = Key.of("boundary");
    private static final  Key<Boolean> offLimbKey = Key.of("offLimb");
    private static final  Key<Integer> indexKey = Key.of("index");
    private static final  Key<Double> etKey = Key.of("et");
    private static final  Key<Long> longTimeKey = Key.of("longTime");
    private static final  Key<Boolean> simulateLightingKey = Key.of("simulateLighting");
    private static final  Key<Double> offsetKey = Key.of("offset");
    private static final  Key<Double> defaultOffsetKey = Key.of("defaultOffset");
    private static final  Key<String> imageTypeKey = Key.of("imageType");
    private static final  Key<CylindricalBounds> boundsKey = Key.of("cylindricalBounds");
    private static final Key<PerspectiveImage> PERSPECTIVE_IMAGE_KEY = Key.of("perspectiveImage");

//    private static final  Key<String> sourceKey = Key.of("source");
//    private static final  Key<Double> lllatKey = Key.of("lllat");
//    private static final  Key<Double> lllonKey = Key.of("lllon");
//    private static final  Key<Double> urlatKey = Key.of("urlat");
//    private static final  Key<Double> urlonKey = Key.of("urlon");
//    private static final  Key<Date> dateKey = Key.of("date");

	private String name;

	private String filename;

	String pointingSource;

	ImageSource pointingSourceType = ImageSource.SPICE;

	//masking
	private int[] maskValues = new int[] {0, 0, 0, 0};

	//trim
	private int[] trimValues = new int[] {0, 0, 0, 0};

	//Linear interpolation dimensions
	private int[] linearInterpolatorDims = null;

	//default contrast stretch
	private IntensityRange intensityRange = new IntensityRange(0, 255);

	//number of layers
	private int numberOfLayers = 1;

	//fill values
	private double[] fillValues = new double[] {};

	//flip and rotation
    private double rotation = 0.0;
    private String flip = "None";

    //source of image
    ImageOrigin imageOrigin = ImageOrigin.LOCAL;

	//needs: adjusted pointing (load and save), rendering, header/metadata

	private boolean mapped = false;
	private boolean frustumShowing = false;
	private boolean offlimbShowing = false;
	private boolean boundaryShowing = false;
	private String status = "Unloaded";
	private int index;
	private double et;
	private Long longTime;
	private boolean simulateLighting = false;
	private double offset = -1.0;
	private double defaultOffset = -1.0;
	private ImageType imageType;
	private CylindricalBounds bounds = null;

	public PerspectiveImage(String filename, ImageType imageType, ImageSource pointingSourceType, String pointingSource, double[] fillValues)
	{
		this.filename = filename;
		this.imageType = imageType;
		this.pointingSourceType = pointingSourceType;
		this.pointingSource = pointingSource;
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

	public Long getLongTime()
	{
		return this.longTime;
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

	public boolean isSimulateLighting()
	{
		return simulateLighting;
	}

	public void setSimulateLighting(boolean simulateLighting)
	{
		this.simulateLighting = simulateLighting;
	}

	public String getPointingSource()
	{
		return pointingSource;
	}

	public void setPointingSource(String pointingSource)
	{
		this.pointingSource = pointingSource;
	}

	public ImageSource getPointingSourceType()
	{
		return pointingSourceType;
	}

	public void setPointingSourceType(ImageSource pointingSourceType)
	{
		this.pointingSourceType = pointingSourceType;
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

	public ImageType getImageType()
	{
		return imageType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setNumberOfLayers(int numberOfLayers)
	{
		this.numberOfLayers = numberOfLayers;
	}

	public void setFillValues(double[] fillValues)
	{
		this.fillValues = fillValues;
	}

	public void setImageType(ImageType imageType)
	{
		this.imageType = imageType;
	}

	public CylindricalBounds getBounds()
	{
		return bounds;
	}

	public void setBounds(CylindricalBounds bounds)
	{
		this.bounds = bounds;
	}

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(PERSPECTIVE_IMAGE_KEY, (metadata) -> {

	        String name = metadata.get(nameKey);
	        String imagefilename = metadata.get(imageFileNameKey);
	        ImageType imageType = ImageType.valueOf(metadata.get(imageTypeKey));
	        String pointingSource = metadata.get(pointingSourceKey);
	        ImageSource pointingSourceType = ImageSource.valueFor(metadata.get(pointingSourceTypeKey));
	        IntensityRange intensityRange = new IntensityRange(metadata.get(intensityRangeKey)[0], metadata.get(intensityRangeKey)[1]);
	        int numberOfLayers = metadata.get(numberOfLayersKey);
	        String flip = metadata.get(flipKey);
	        Double rotation = metadata.get(rotationKey);
	        String imageOrigin = metadata.get(imageOriginKey);
	        boolean mapped = metadata.get(mappedKey);
	        boolean frustum = metadata.get(frustumKey);
	        boolean boundary = metadata.get(boundaryKey);
	        boolean offlimb = metadata.get(offLimbKey);
	        boolean simulateLighting = metadata.get(simulateLightingKey);
	        Integer index = metadata.get(indexKey);
	        Double et = metadata.get(etKey);
	        Long longTime = metadata.get(longTimeKey);
	        Double offset = metadata.get(offsetKey);
	        Double defaultOffset = metadata.get(defaultOffsetKey);


	        double[] fillValues = new double[] {};
	        PerspectiveImage result = new PerspectiveImage(imagefilename, imageType, pointingSourceType, pointingSource, fillValues);
	        result.setName(name);
	        result.setIntensityRange(intensityRange);
	        result.setNumberOfLayers(numberOfLayers);
	        result.setFlip(flip);
	        result.setRotation(rotation);
	        result.setImageOrigin(ImageOrigin.valueFor(imageOrigin));
	        result.setMapped(mapped);
	        result.setFrustumShowing(frustum);
	        result.setBoundaryShowing(boundary);
	        result.setOfflimbShowing(offlimb);
	        result.setSimulateLighting(simulateLighting);
	        result.setIndex(index);
	        result.setEt(et);
	        result.setLongTime(longTime);
	        result.setOffset(offset);
	        result.setDefaultOffset(defaultOffset);
	        if (metadata.hasKey(boundsKey))
	        {
		        CylindricalBounds bounds = metadata.get(boundsKey);
		        result.setBounds(bounds);
	        }
			return result;
		}, PerspectiveImage.class, image -> {
			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
	        result.put(nameKey, image.getFilename());
	        result.put(imageFileNameKey, image.getFilename());
	        result.put(imageTypeKey, image.getImageType().toString());
	        result.put(pointingSourceKey, image.getPointingSource());
	        result.put(pointingSourceTypeKey, image.getPointingSourceType().toString());
	        result.put(intensityRangeKey, new Integer[] {image.getIntensityRange().min, image.getIntensityRange().max});
	        result.put(numberOfLayersKey, image.getNumberOfLayers());
	        result.put(flipKey, image.getFlip());
	        result.put(rotationKey, image.getRotation());
	        result.put(imageOriginKey, image.getImageOrigin());
	        result.put(mappedKey, image.isMapped());
	        result.put(frustumKey, image.isFrustumShowing());
	        result.put(boundaryKey, image.isBoundaryShowing());
	        result.put(offLimbKey, image.isOfflimbShowing());
	        result.put(simulateLightingKey, image.isSimulateLighting());
	        result.put(indexKey, image.getIndex());
	        result.put(etKey, image.getEt());
	        result.put(longTimeKey, image.getLongTime());
	        result.put(offsetKey, image.getOffset());
	        result.put(defaultOffsetKey, image.getDefaultOffset());
	        if (image.getBounds() != null) result.put(boundsKey, image.getBounds());
	        return result;
		});
	}

}
