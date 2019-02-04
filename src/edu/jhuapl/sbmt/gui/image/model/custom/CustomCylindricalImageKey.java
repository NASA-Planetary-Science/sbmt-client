package edu.jhuapl.sbmt.gui.image.model.custom;

import java.text.DecimalFormat;
import java.util.Date;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.gui.image.model.CustomImageKeyInterface;
import edu.jhuapl.sbmt.gui.image.ui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.ImageType;
import edu.jhuapl.sbmt.model.image.ImagingInstrument;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.StorableAsMetadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class CustomCylindricalImageKey implements StorableAsMetadata<CustomCylindricalImageKey>, CustomImageKeyInterface
{
	public String name = ""; // name to call this image for display purposes
    public String imagefilename = ""; // filename of image on disk
    public ProjectionType projectionType = ProjectionType.CYLINDRICAL;
    public double lllat = -90.0;
    public double lllon = 0.0;
    public double urlat = 90.0;
    public double urlon = 360.0;
    public ImageType imageType;
    public final ImageSource source;
    private final Date date;
    private String originalName;

    private static final  Key<String> nameKey = Key.of("name");
    private static final  Key<String> imageFileNameKey = Key.of("imagefilename");
    private static final  Key<String> imageTypeKey = Key.of("imageType");
    private static final  Key<String> sourceKey = Key.of("source");
    private static final  Key<Double> lllatKey = Key.of("lllat");
    private static final  Key<Double> lllonKey = Key.of("lllon");
    private static final  Key<Double> urlatKey = Key.of("urlat");
    private static final  Key<Double> urlonKey = Key.of("urlon");
    private static final  Key<Date> dateKey = Key.of("date");
    private static final Key<CustomCylindricalImageKey> CUSTOM_CYLINDRICAL_IMAGE_KEY = Key.of("customCylindricalImage");
    private static final Key<String> originalNameKey = Key.of("originalname");


	public CustomCylindricalImageKey(String name, String imagefilename, ImageType imageType, ImageSource source, Date date, String originalName)
	{
		this.name = name;
		this.imagefilename = imagefilename;
		this.imageType = imageType;
		this.source = source;
		this.date = date;
		this.originalName = originalName;
	}

    public String getName()
    {
    	return name;
    }

    public String getImageFilename()
    {
    	return imagefilename;
    }

    public double getLllat()
	{
		return lllat;
	}

	public void setLllat(double lllat)
	{
		this.lllat = lllat;
	}

	public double getLllon()
	{
		return lllon;
	}

	public void setLllon(double lllon)
	{
		this.lllon = lllon;
	}

	public double getUrlat()
	{
		return urlat;
	}

	public void setUrlat(double urlat)
	{
		this.urlat = urlat;
	}

	public double getUrlon()
	{
		return urlon;
	}

	public void setUrlon(double urlon)
	{
		this.urlon = urlon;
	}

	public ProjectionType getProjectionType()
	{
		return projectionType;
	}

	public ImageSource getSource()
	{
		return source;
	}

	public ImageType getImageType()
	{
		return imageType;
	}

	@Override
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("#.#####");

        return name + ", Cylindrical  ["
                + df.format(lllat) + ", "
                + df.format(lllon) + ", "
                + df.format(urlat) + ", "
                + df.format(urlon)
                + "]";
    }

	@Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(Key.of("customimagetype"), CUSTOM_CYLINDRICAL_IMAGE_KEY.toString());
        result.put(nameKey, name);
        result.put(imageFileNameKey, imagefilename);
        result.put(imageTypeKey, imageType.toString());
        result.put(sourceKey, source.toString());
        result.put(lllatKey, lllat);
        result.put(lllonKey, lllon);
        result.put(urlatKey, urlat);
        result.put(urlonKey, urlon);
        result.put(dateKey, date);
        return result;
    }

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(CUSTOM_CYLINDRICAL_IMAGE_KEY, (metadata) -> {

	        String name = metadata.get(nameKey);
	        String imagefilename = metadata.get(imageFileNameKey);
	        ImageType imageType = ImageType.valueOf(metadata.get(imageTypeKey));
	        ImageSource source = ImageSource.valueFor(metadata.get(sourceKey));
	        double lllat = metadata.get(lllatKey);
	        double lllon = metadata.get(lllonKey);
	        double urlat = metadata.get(urlatKey);
	        double urlon = metadata.get(urlonKey);
	        Date date = metadata.get(dateKey);
	        String originalName = metadata.get(originalNameKey);
	        CustomCylindricalImageKey result = new CustomCylindricalImageKey(name, imagefilename, imageType, source, date, originalName);
	        result.setLllat(lllat);
	        result.setLllon(lllon);
	        result.setUrlat(urlat);
	        result.setUrlon(urlon);

			return result;
		});
	}

	@Override
	public Key<CustomCylindricalImageKey> getKey()
	{
		return CUSTOM_CYLINDRICAL_IMAGE_KEY;
	}

	@Override
	public void setImagefilename(String imagefilename)
	{
		this.imagefilename = imagefilename;
	}

	@Override
	public int getSlice()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImagingInstrument getInstrument()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileType getFileType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBand()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPointingFile()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getOriginalName()
	{
		return originalName;
	}

	public void setOriginalName(String originalName)
	{
		this.originalName = originalName;
	}

	@Override
	public Date getDate()
	{
		return date;
	}

//    @Override
//    public void retrieve(Metadata source)
//    {
//        name = source.get(nameKey);
//        imagefilename = source.get(imageFileNameKey);
//        projectionType = ProjectionType.valueOf(source.get(projectionKey));
//        imageType = ImageType.valueOf(source.get(imageTypeKey));
//        lllat = source.get(lllatKey);
//        lllon = source.get(lllonKey);
//        urlat = source.get(urlatKey);
//        urlon = source.get(urlonKey);
//    }

}
